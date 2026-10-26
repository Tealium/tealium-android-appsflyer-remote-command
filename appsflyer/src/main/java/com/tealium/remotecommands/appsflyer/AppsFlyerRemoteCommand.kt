package com.tealium.remotecommands.appsflyer

import android.app.Application
import com.appsflyer.share.AFAdRevenueData
import com.appsflyer.share.AppsFlyerConsent
import com.appsflyer.share.deeplink.DeepLinkListener
import com.appsflyer.share.deeplink.DeepLinkResult
import com.tealium.remotecommands.RemoteCommand
import com.tealium.remotecommands.RemoteCommandContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.jvm.Throws

open class AppsFlyerRemoteCommand internal constructor(
    private val application: Application,
    private val sessionMode: AppsFlyerSessionMode,
    private val appsFlyerDevKey: String?,
    commandId: String,
    description: String,
    internal val logger: RemoteCommandLogger
) : RemoteCommand(commandId, description, BuildConfig.TEALIUM_APPSFLYER_VERSION), DeepLinkListener {

    /**
     * @param application The host application, passed to the AppsFlyer SDK as its context.
     * @param sessionMode Who registers the SDK's session-ready listener and calls `AppsFlyerLib.start()`
     *   in it. See [AppsFlyerSessionMode].
     * @param appsFlyerDevKey Dev key used when the `initialize` payload has no `app_dev_key`.
     * @param commandId The remote command ID.
     * @param description The remote command description.
     * @param logLevel Log verbosity of the command and its instance. Defaults to [RemoteCommandLogLevel.ERROR] (errors only).
     */
    @JvmOverloads
    constructor(
        application: Application,
        sessionMode: AppsFlyerSessionMode,
        appsFlyerDevKey: String? = null,
        commandId: String = DEFAULT_COMMAND_ID,
        description: String = DEFAULT_COMMAND_DESCRIPTION,
        logLevel: RemoteCommandLogLevel = RemoteCommandLogLevel.ERROR
    ) : this(application, sessionMode, appsFlyerDevKey, commandId, description, RemoteCommandLogger(logLevel))

    lateinit var appsFlyerInstance: AppsFlyerCommand

    private val activityTracker = ResumedActivityTracker().also { application.registerActivityLifecycleCallbacks(it) }

    companion object {
        const val DEFAULT_COMMAND_ID = "appsflyer"
        const val DEFAULT_COMMAND_DESCRIPTION = "Tealium-AppsFlyer Remote Command"
    }

    /**
     * Triggered by RemoteCommand response data and parses commands to execute
     *
     * @param response - response containing commands and payload to process
     */
    @Throws(Exception::class)
    override fun onInvoke(response: Response) {
        val payload = response.requestPayload
        val commands = splitCommands(payload)
        parseCommands(commands, payload)
    }

    /**
     * Process commands and parameter data with each command
     *
     * @param commands - list of command names to be processed
     * @param payload - parameter data to process with command name
     */
    fun parseCommands(commands: Array<String>, payload: JSONObject) {
        commands.forEach { commandString ->
            if (commandString.isBlank()) return@forEach

            try {
                when (Command.fromString(commandString)) {
                    Command.INITIALIZE -> initialize(payload)
                    Command.TRACK_LOCATION -> trackLocation(payload)
                    Command.SET_HOST -> setHost(payload)
                    Command.SET_USER_EMAIL -> appsFlyerInstance.setUserEmail(requireString(payload, Customer.EMAIL))
                    Command.SET_USER_FIRST_NAME -> appsFlyerInstance.setUserFirstName(requireString(payload, Customer.FIRST_NAME))
                    Command.SET_USER_LAST_NAME -> appsFlyerInstance.setUserLastName(requireString(payload, Customer.LAST_NAME))
                    Command.SET_USER_FB_LOGIN_ID -> appsFlyerInstance.setUserFbLoginId(requireLong(payload, Customer.FB_LOGIN_ID))
                    Command.CLEAR_USER_PII -> appsFlyerInstance.clearUserPii()
                    Command.SET_CURRENCY_CODE -> setCurrencyCode(payload)
                    Command.SET_CUSTOMER_ID -> setCustomerId(payload)
                    Command.SET_USER_PHONE -> setUserPhone(payload)
                    Command.LOG_AD_REVENUE -> logAdRevenue(payload)
                    Command.SET_CONSENT_DATA -> setConsentData(payload)
                    Command.SET_PARTNER_DATA -> setPartnerData(payload)
                    Command.SET_SHARING_FILTER_FOR_PARTNERS -> setSharingFilterForPartners(payload)
                    Command.ANONYMIZE_USER,
                    Command.DISABLE_DEVICE_TRACKING -> anonymizeUser(payload) // DISABLE_DEVICE_TRACKING is a backwards-compatible alias
                    Command.RESOLVE_DEEPLINK_URLS -> resolveDeepLinkUrls(payload)
                    Command.STOP_TRACKING,
                    Command.DISABLE_TRACKING -> stopTracking(payload) // DISABLE_TRACKING is the iOS command name accepted here for cross-platform payloads
                    Command.LOG_SESSION -> appsFlyerInstance.logSession()
                    Command.SET_OAID -> setOaid(payload)
                    Command.SET_ANDROID_ID -> setAndroidId(payload)
                    Command.SET_IMEI -> setImei(payload)
                    Command.SET_OUT_OF_STORE -> setOutOfStore(payload)
                    Command.SET_DISABLE_NETWORK_DATA -> setDisableNetworkData(payload)
                    Command.SET_APP_INVITE_ONE_LINK -> setAppInviteOneLink(payload)
                    Command.SET_PREINSTALL_ATTRIBUTION -> setPreinstallAttribution(payload)
                    Command.SET_IS_UPDATE -> setIsUpdate(payload)
                    null -> dispatchCustomEvent(commandString, payload)
                }
            } catch (e: AppsFlyerCommandError) {
                logger.error("Command '$commandString' failed: ${e.message}")
            } catch (e: Exception) {
                // One misbehaving command must not abort the remaining commands in the list.
                logger.error("Command '$commandString' failed unexpectedly: ${e.message}", e)
            }
        }
    }

    /**
     * Validate and return standard event name for AppsFlyer events
     *
     * @param commandName - name of Tealium command name, matched case-insensitively
     */
    fun standardEvent(commandName: String): String? {
        return StandardEvents.eventNames[commandName.lowercase(Locale.ROOT)]
    }

    /**
     * Tracks app-open attribution from a resolved AppsFlyer deep link: `app_open_attribution` with the OneLink
     * click event as payload, or `appsflyer_error` with `error_name` `app_open_attribution_failure`. Deferred deep
     * links are skipped, because `conversion_data_received` reports them.
     *
     * The RemoteCommand never subscribes itself, so app-open attribution is opt-in. The SDK holds a single
     * [DeepLinkListener] and drops results that arrive with no listener set, so subscribe in
     * `Application.onCreate`, before the first Activity resumes:
     *
     * ```kotlin
     * AppsFlyerLib.getInstance().subscribeForDeepLink(command)
     * ```
     *
     * An app that routes deep links itself subscribes its own listener and forwards every result:
     *
     * ```kotlin
     * AppsFlyerLib.getInstance().subscribeForDeepLink { result ->
     *     // The app's own deep link routing.
     *     command.onDeepLinking(result)
     * }
     * ```
     *
     * Called on an SDK background thread. A result that arrives before the command is added to Tealium is dropped.
     */
    override fun onDeepLinking(result: DeepLinkResult) {
        if (!::appsFlyerInstance.isInitialized) {
            logger.debug("Dropping a deep link result: the command has not been added to Tealium yet.")
            return
        }
        appsFlyerInstance.onDeepLinking(result)
    }

    private fun initialize(payload: JSONObject) {
        val devKey: String = payload.optString(Config.DEV_KEY).takeIf { it.isNotBlank() }
            ?: appsFlyerDevKey?.takeIf { it.isNotBlank() }
            ?: throw AppsFlyerCommandError.missingParameter(Config.DEV_KEY)
        val config: JSONObject? = payload.optJSONObject(Config.SETTINGS)
        val configSettings: Map<String, Any> = config.toMap()
        logger.debug("Initializing AppsFlyer SDK")
        appsFlyerInstance.initialize(devKey, configSettings)
    }

    private fun trackLocation(payload: JSONObject) {
        val latitude: Double = payload.optDouble(Location.LATITUDE)
        val longitude: Double = payload.optDouble(Location.LONGITUDE)

        if (latitude.isNaN()) {
            throw AppsFlyerCommandError.missingParameter(Location.LATITUDE)
        }
        if (longitude.isNaN()) {
            throw AppsFlyerCommandError.missingParameter(Location.LONGITUDE)
        }
        appsFlyerInstance.trackLocation(latitude, longitude)
    }

    private fun setHost(payload: JSONObject) {
        if (!payload.has(Host.HOST)) {
            throw AppsFlyerCommandError.missingParameter(Host.HOST)
        }
        if (!payload.has(Host.HOST_PREFIX)) {
            throw AppsFlyerCommandError.missingParameter(Host.HOST_PREFIX)
        }
        val host: String = payload.optString(Host.HOST)
        val hostPrefix: String = payload.optString(Host.HOST_PREFIX)
        appsFlyerInstance.setHost(host, hostPrefix)
    }

    private fun setCurrencyCode(payload: JSONObject) {
        if (!payload.has(TransactionProperties.CURRENCY)) {
            throw AppsFlyerCommandError.missingParameter(TransactionProperties.CURRENCY)
        }
        val currencyCode: String = payload.optString(TransactionProperties.CURRENCY)
        appsFlyerInstance.setCurrencyCode(currencyCode)
    }

    private fun setCustomerId(payload: JSONObject) {
        if (!payload.has(Customer.USER_ID)) {
            throw AppsFlyerCommandError.missingParameter(Customer.USER_ID)
        }
        val id: String = payload.optString(Customer.USER_ID)
        appsFlyerInstance.setCustomerId(id)
    }

    private fun setUserPhone(payload: JSONObject) {
        val countryCode = requireString(payload, PhoneNumberParam.COUNTRY_CODE)
        val phoneNumber = requireString(payload, PhoneNumberParam.PHONE_NUMBER)
        appsFlyerInstance.setUserPhone(countryCode, phoneNumber)
    }

    private fun logAdRevenue(payload: JSONObject) {
        if (!payload.has(AdRevenueParams.MONETIZATION_NETWORK)) {
            throw AppsFlyerCommandError.missingParameter(AdRevenueParams.MONETIZATION_NETWORK)
        }
        if (!payload.has(AdRevenueParams.MEDIATION_NETWORK)) {
            throw AppsFlyerCommandError.missingParameter(AdRevenueParams.MEDIATION_NETWORK)
        }
        if (!payload.has(AdRevenueParams.AD_REVENUE_CURRENCY)) {
            throw AppsFlyerCommandError.missingParameter(AdRevenueParams.AD_REVENUE_CURRENCY)
        }

        val monetizationNetwork = payload.optString(AdRevenueParams.MONETIZATION_NETWORK)
        val mediationNetworkString = payload.optString(AdRevenueParams.MEDIATION_NETWORK)
        val currency = payload.optString(AdRevenueParams.AD_REVENUE_CURRENCY)
        val revenue = payload.optDouble(AdRevenueParams.AD_REVENUE_AMOUNT)

        if (revenue.isNaN()) {
            throw AppsFlyerCommandError.missingParameter(AdRevenueParams.AD_REVENUE_AMOUNT)
        }

        val mediationNetwork = mediationNetworkString.toMediationNetwork()
            ?: throw AppsFlyerCommandError.invalidParameterValue(
                key = AdRevenueParams.MEDIATION_NETWORK,
                value = mediationNetworkString,
                allowedValues = mediationNetworkValidValues
            )

        val adRevenueData = AFAdRevenueData(
            monetizationNetwork,
            mediationNetwork,
            currency,
            revenue
        )
        val additionalParams = payload.optJSONObject(AdRevenueParams.AD_REVENUE_ADDITIONAL_PARAMS).toMap()
        appsFlyerInstance.logAdRevenue(adRevenueData, additionalParams)
    }

    private fun setConsentData(payload: JSONObject) {
        val isUserSubjectToGDPR = requireBoolean(payload, ConsentDataParams.IS_USER_SUBJECT_TO_GDPR)
        val hasConsentForDataUsage = optBooleanValue(payload, ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE)
        val hasConsentForAdsPersonalization = optBooleanValue(payload, ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION)
        val hasConsentForAdStorage = optBooleanValue(payload, ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE)
        val consent = AppsFlyerConsent(isUserSubjectToGDPR, hasConsentForDataUsage, hasConsentForAdsPersonalization, hasConsentForAdStorage)
        appsFlyerInstance.setConsentData(consent)
    }

    private fun setPartnerData(payload: JSONObject) {
        if (!payload.has(PartnerDataParams.PARTNER_ID)) {
            throw AppsFlyerCommandError.missingParameter(PartnerDataParams.PARTNER_ID)
        }
        val partnerId: String = payload.optString(PartnerDataParams.PARTNER_ID)
        val partnerInfoMap = payload.optJSONObject(PartnerDataParams.PARTNER_INFO).toMap()
        appsFlyerInstance.setPartnerData(partnerId, partnerInfoMap)
    }

    private fun setSharingFilterForPartners(payload: JSONObject) {
        val sharingFilterJsonArray: JSONArray? = payload.optJSONArray(SharingFilterParams.SHARING_FILTER)
        // A missing array resets the filter — the wrapper calls setSharingFilterForPartners() with no arguments.
        val sharingFilterArray = sharingFilterJsonArray?.let { toList(it).toTypedArray() }
        appsFlyerInstance.setSharingFilterForPartners(sharingFilterArray)
    }

    private fun anonymizeUser(payload: JSONObject) {
        appsFlyerInstance.anonymizeUser(requireBoolean(payload, Tracking.ANONYMIZE_USER))
    }

    private fun resolveDeepLinkUrls(payload: JSONObject) {
        val deepLinkJsonArray: JSONArray = payload.optJSONArray(DeepLink.URLS)
            ?: payload.optJSONArray(DeepLink.URLS_LEGACY_TIQ)
            ?: throw AppsFlyerCommandError.missingParameter(DeepLink.URLS)
        appsFlyerInstance.resolveDeepLinkUrls(toList(deepLinkJsonArray))
    }

    private fun stopTracking(payload: JSONObject) {
        appsFlyerInstance.stopTracking(requireBoolean(payload, Tracking.STOP_TRACKING))
    }

    private fun setOaid(payload: JSONObject) {
        if (!payload.has(StringCommandParams.OAID)) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.OAID)
        }
        val oaid = payload.optString(StringCommandParams.OAID)
        appsFlyerInstance.setOaid(oaid)
    }

    private fun setAndroidId(payload: JSONObject) {
        if (!payload.has(StringCommandParams.ANDROID_ID)) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.ANDROID_ID)
        }
        val androidId = payload.optString(StringCommandParams.ANDROID_ID)
        appsFlyerInstance.setAndroidId(androidId)
    }

    private fun setImei(payload: JSONObject) {
        if (!payload.has(StringCommandParams.IMEI)) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.IMEI)
        }
        val imei = payload.optString(StringCommandParams.IMEI)
        appsFlyerInstance.setImei(imei)
    }

    private fun setOutOfStore(payload: JSONObject) {
        if (!payload.has(StringCommandParams.STORE_NAME)) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.STORE_NAME)
        }
        val storeName = payload.optString(StringCommandParams.STORE_NAME)
        appsFlyerInstance.setOutOfStore(storeName)
    }

    private fun setDisableNetworkData(payload: JSONObject) {
        appsFlyerInstance.setDisableNetworkData(requireBoolean(payload, StringCommandParams.DISABLE_NETWORK_DATA))
    }

    private fun setAppInviteOneLink(payload: JSONObject) {
        if (!payload.has(StringCommandParams.APP_INVITE_ONE_LINK_ID)) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.APP_INVITE_ONE_LINK_ID)
        }
        val oneLinkId = payload.optString(StringCommandParams.APP_INVITE_ONE_LINK_ID)
        appsFlyerInstance.setAppInviteOneLink(oneLinkId)
    }

    private fun setPreinstallAttribution(payload: JSONObject) {
        if (!payload.has(PreinstallParams.MEDIA_SOURCE)) {
            throw AppsFlyerCommandError.missingParameter(PreinstallParams.MEDIA_SOURCE)
        }
        if (!payload.has(PreinstallParams.CAMPAIGN)) {
            throw AppsFlyerCommandError.missingParameter(PreinstallParams.CAMPAIGN)
        }
        if (!payload.has(PreinstallParams.SITE_ID)) {
            throw AppsFlyerCommandError.missingParameter(PreinstallParams.SITE_ID)
        }
        val mediaSource = payload.optString(PreinstallParams.MEDIA_SOURCE)
        val campaign = payload.optString(PreinstallParams.CAMPAIGN)
        val siteId = payload.optString(PreinstallParams.SITE_ID)
        appsFlyerInstance.setPreinstallAttribution(mediaSource, campaign, siteId)
    }

    private fun setIsUpdate(payload: JSONObject) {
        appsFlyerInstance.setIsUpdate(requireBoolean(payload, StringCommandParams.IS_UPDATE))
    }

    private fun dispatchCustomEvent(commandString: String, payload: JSONObject) {
        // Only the standard-event lookup is case-insensitive; a custom event keeps its original case.
        val eventName = commandString.trim()
        val eventType = standardEvent(eventName) ?: eventName
        val eventParameters: JSONObject =
            payload.optJSONObject(StandardEvents.EVENT_PARAMETERS)
                ?: payload.optJSONObject(StandardEvents.EVENT_PARAMETERS_SHORT)
                ?: filterPayload(payload)
        appsFlyerInstance.trackEvent(eventType, eventParameters.toMap())
    }

    private fun requireBoolean(payload: JSONObject, key: String): Boolean {
        if (!payload.has(key)) {
            throw AppsFlyerCommandError.missingParameter(key)
        }
        return optBooleanValue(payload, key)
            ?: throw AppsFlyerCommandError.invalidParameterType(key, "Boolean")
    }

    private fun requireString(payload: JSONObject, key: String): String {
        if (!payload.has(key)) {
            throw AppsFlyerCommandError.missingParameter(key)
        }
        return payload.opt(key) as? String
            ?: throw AppsFlyerCommandError.invalidParameterType(key, "String")
    }

    // Webview data layers send numeric variables as Strings, so a numeric String is accepted as on iOS.
    // A Double is accepted only when it is a whole number within the Long range.
    private fun requireLong(payload: JSONObject, key: String): Long {
        if (!payload.has(key)) {
            throw AppsFlyerCommandError.missingParameter(key)
        }
        return when (val value = payload.opt(key)) {
            is Int -> value.toLong()
            is Long -> value
            is Double -> value.takeIf { it % 1.0 == 0.0 && it >= Long.MIN_VALUE && it < Long.MAX_VALUE }?.toLong()
            is String -> value.toLongOrNull()
            else -> null
        } ?: throw AppsFlyerCommandError.invalidParameterType(key, "Long or numeric String")
    }

    private fun optBooleanValue(payload: JSONObject, key: String): Boolean? = toBooleanValue(payload.opt(key))

    internal fun splitCommands(payload: JSONObject): Array<String> {
        val command = payload.optString(Commands.COMMAND_KEY, "")
        return command.split(Commands.SEPARATOR)
            .dropLastWhile { it.isEmpty() }
            .map { it.trim() }
            .toTypedArray()
    }

    override fun setContext(context: RemoteCommandContext?) {
        context ?: return
        // A second instance would re-register the SDK listeners, reset readiness and drop queued calls.
        getContext()?.let { current ->
            if (current !== context) {
                logger.warning("Ignoring a new RemoteCommandContext; the AppsFlyer command keeps the context it was first added with.")
            }
            return
        }
        super.setContext(context)
        appsFlyerInstance = AppsFlyerInstance(
            application,
            sessionMode,
            appsFlyerDevKey,
            context,
            logger,
            activityTracker::currentActivity
        )
    }

    private fun toList(jsonArray: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }

    private fun filterPayload(jsonObject: JSONObject): JSONObject {
        val toCopy = mutableListOf<String>()
        val toRemove = listOf(
            Settings.DEBUG,
            Config.DEV_KEY,
            Config.SETTINGS,
            Commands.COMMAND_KEY,
            Commands.METHOD_KEY,
            Config.APP_ID,
        )
        for (key in jsonObject.keys()) {
            if (toRemove.contains(key)) continue

            toCopy.add(key)
        }

        return JSONObject(jsonObject, toCopy.toTypedArray())
    }
}
