package com.tealium.remotecommands.appsflyer

import android.app.Application
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AppsFlyerConsent
import com.tealium.remotecommands.RemoteCommand
import com.tealium.remotecommands.RemoteCommandContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.jvm.Throws

open class AppsFlyerRemoteCommand internal constructor(
    private val application: Application,
    private val appsFlyerDevKey: String?,
    commandId: String,
    description: String,
    internal val logger: RemoteCommandLogger
) : RemoteCommand(commandId, description, BuildConfig.TEALIUM_APPSFLYER_VERSION) {

    @JvmOverloads
    constructor(
        application: Application,
        appsFlyerDevKey: String? = null,
        commandId: String = DEFAULT_COMMAND_ID,
        description: String = DEFAULT_COMMAND_DESCRIPTION,
        logLevel: RemoteCommandLogLevel = RemoteCommandLogLevel.SILENT
    ) : this(application, appsFlyerDevKey, commandId, description, RemoteCommandLogger(logLevel))

    lateinit var appsFlyerInstance: AppsFlyerCommand

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
                    Command.SET_USER_EMAILS -> setUserEmails(payload)
                    Command.SET_CURRENCY_CODE -> setCurrencyCode(payload)
                    Command.SET_CUSTOMER_ID -> setCustomerId(payload)
                    Command.SET_PHONE_NUMBER -> setPhoneNumber(payload)
                    Command.LOG_AD_REVENUE -> logAdRevenue(payload)
                    Command.SET_CONSENT_DATA -> setConsentData(payload)
                    Command.SET_PARTNER_DATA -> setPartnerData(payload)
                    Command.SET_SHARING_FILTER_FOR_PARTNERS -> setSharingFilterForPartners(payload)
                    Command.ANONYMIZE_USER,
                    Command.DISABLE_DEVICE_TRACKING -> anonymizeUser(payload) // DISABLE_DEVICE_TRACKING is a backwards-compatible alias
                    Command.RESOLVE_DEEPLINK_URLS -> resolveDeepLinkUrls(payload)
                    Command.START -> appsFlyerInstance.start()
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
            }
        }
    }

    /**
     * Validate and return standard event name for AppsFlyer events
     *
     * @param commandName - name of Tealium command name
     */
    fun standardEvent(commandName: String): String? {
        return StandardEvents.eventNames[commandName]
    }

    private fun initialize(payload: JSONObject) {
        val devKey: String = payload.optString(Config.DEV_KEY).takeIf { it.isNotBlank() }
            ?: appsFlyerDevKey?.takeIf { it.isNotBlank() }
            ?: throw AppsFlyerCommandError.missingParameter(Config.DEV_KEY)
        val config: JSONObject? = payload.optJSONObject(Config.SETTINGS)
        val configSettings: Map<String, Any> = jsonToMap(config)
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

    private fun setUserEmails(payload: JSONObject) {
        val emails: JSONArray = payload.optJSONArray(Customer.EMAILS)
            ?: throw AppsFlyerCommandError.missingParameter(Customer.EMAILS)
        val cryptTypeInt = payload.optInt(Customer.EMAIL_HASH_TYPE, -1)
            .takeIf { payload.has(Customer.EMAIL_HASH_TYPE) }
            ?: throw AppsFlyerCommandError.missingParameter(Customer.EMAIL_HASH_TYPE)
        if (EmailCryptTypeMapping.fromInt(cryptTypeInt) == null) {
            throw AppsFlyerCommandError.invalidParameterValue(
                key = Customer.EMAIL_HASH_TYPE,
                value = cryptTypeInt.toString(),
                allowedValues = EmailCryptTypeMapping.validValues.map { it.toString() }
            )
        }
        appsFlyerInstance.setUserEmails(toList(emails), cryptTypeInt)
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

    private fun setPhoneNumber(payload: JSONObject) {
        if (!payload.has(PhoneNumberParam.PHONE_NUMBER)) {
            throw AppsFlyerCommandError.missingParameter(PhoneNumberParam.PHONE_NUMBER)
        }
        val phoneNumber: String = payload.optString(PhoneNumberParam.PHONE_NUMBER)
        appsFlyerInstance.setPhoneNumber(phoneNumber)
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
        val additionalParams = jsonToMap(payload.optJSONObject(AdRevenueParams.AD_REVENUE_ADDITIONAL_PARAMS))
        appsFlyerInstance.logAdRevenue(adRevenueData, additionalParams)
    }

    private fun setConsentData(payload: JSONObject) {
        val consent = AppsFlyerConsent(
            requireBoolean(payload, ConsentDataParams.IS_USER_SUBJECT_TO_GDPR),
            requireBoolean(payload, ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE),
            requireBoolean(payload, ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION),
            requireBoolean(payload, ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE)
        )
        appsFlyerInstance.setConsentData(consent)
    }

    private fun setPartnerData(payload: JSONObject) {
        if (!payload.has(PartnerDataParams.PARTNER_ID)) {
            throw AppsFlyerCommandError.missingParameter(PartnerDataParams.PARTNER_ID)
        }
        val partnerId: String = payload.optString(PartnerDataParams.PARTNER_ID)
        val partnerInfoMap = jsonToMap(payload.optJSONObject(PartnerDataParams.PARTNER_INFO))
        appsFlyerInstance.setPartnerData(partnerId, partnerInfoMap)
    }

    private fun setSharingFilterForPartners(payload: JSONObject) {
        val sharingFilterJsonArray: JSONArray? = payload.optJSONArray(SharingFilterParams.SHARING_FILTER)
        // A missing array resets the filter — the wrapper calls setSharingFilterForPartners() with no arguments.
        val sharingFilterArray = sharingFilterJsonArray?.let { toList(it).toTypedArray() }
        appsFlyerInstance.setSharingFilterForPartners(sharingFilterArray)
    }

    private fun anonymizeUser(payload: JSONObject) {
        if (!payload.has(Tracking.ANONYMIZE_USER)) {
            throw AppsFlyerCommandError.missingParameter(Tracking.ANONYMIZE_USER)
        }
        appsFlyerInstance.anonymizeUser(payload.optBoolean(Tracking.ANONYMIZE_USER, false))
    }

    private fun resolveDeepLinkUrls(payload: JSONObject) {
        val deepLinkJsonArray: JSONArray = payload.optJSONArray(DeepLink.URLS)
            ?: payload.optJSONArray(DeepLink.URLS_LEGACY_TIQ)
            ?: throw AppsFlyerCommandError.missingParameter(DeepLink.URLS)
        appsFlyerInstance.resolveDeepLinkUrls(toList(deepLinkJsonArray))
    }

    private fun stopTracking(payload: JSONObject) {
        if (!payload.has(Tracking.STOP_TRACKING)) {
            throw AppsFlyerCommandError.missingParameter(Tracking.STOP_TRACKING)
        }
        appsFlyerInstance.stopTracking(payload.optBoolean(Tracking.STOP_TRACKING))
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
        if (!payload.has(StringCommandParams.DISABLE_NETWORK_DATA)) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.DISABLE_NETWORK_DATA)
        }
        appsFlyerInstance.setDisableNetworkData(payload.optBoolean(StringCommandParams.DISABLE_NETWORK_DATA))
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
        if (!payload.has(StringCommandParams.IS_UPDATE)) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.IS_UPDATE)
        }
        appsFlyerInstance.setIsUpdate(payload.optBoolean(StringCommandParams.IS_UPDATE))
    }

    private fun dispatchCustomEvent(commandString: String, payload: JSONObject) {
        val normalizedCommand = commandString.trim().lowercase(Locale.ROOT)
        val eventType = standardEvent(normalizedCommand) ?: commandString
        val eventParameters: JSONObject =
            payload.optJSONObject(StandardEvents.EVENT_PARAMETERS)
                ?: payload.optJSONObject(StandardEvents.EVENT_PARAMETERS_SHORT)
                ?: filterPayload(payload)
        appsFlyerInstance.trackEvent(eventType, jsonToMap(eventParameters))
    }

    private fun requireBoolean(payload: JSONObject, key: String): Boolean {
        if (!payload.has(key)) {
            throw AppsFlyerCommandError.missingParameter(key)
        }
        return payload.optBoolean(key, false)
    }

    internal fun splitCommands(payload: JSONObject): Array<String> {
        val command = payload.optString(Commands.COMMAND_KEY, "")
        return command.split(Commands.SEPARATOR)
            .dropLastWhile { it.isEmpty() }
            .map { it.trim().lowercase(Locale.ROOT) }
            .toTypedArray()
    }

    override fun setContext(context: RemoteCommandContext?) {
        context?.let {
            appsFlyerInstance = AppsFlyerInstance(
                application,
                appsFlyerDevKey,
                it,
                logger
            )
        }
    }

    private fun jsonToMap(jsonObject: JSONObject?): Map<String, Any> {
        val map = HashMap<String, Any>()
        jsonObject?.let {
            it.keys().forEach { key ->
                map[key] = convertJsonValue(it[key])
            }
        }
        return map
    }

    private fun convertJsonValue(value: Any): Any = when (value) {
        is JSONObject -> jsonToMap(value)
        is JSONArray -> (0 until value.length()).map { convertJsonValue(value[it]) }
        else -> value
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
