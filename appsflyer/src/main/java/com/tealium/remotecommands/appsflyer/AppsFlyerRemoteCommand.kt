package com.tealium.remotecommands.appsflyer

import android.app.Application
import com.appsflyer.AFAdRevenueData
import com.tealium.remotecommands.RemoteCommand
import com.tealium.remotecommands.RemoteCommandContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.jvm.Throws

open class AppsFlyerRemoteCommand @JvmOverloads constructor(
    private val application: Application,
    private val appsFlyerDevKey: String? = null,
    commandId: String = DEFAULT_COMMAND_ID,
    description: String = DEFAULT_COMMAND_DESCRIPTION,
    logLevel: RemoteCommandLogLevel = RemoteCommandLogLevel.SILENT
) : RemoteCommand(commandId, description, BuildConfig.TEALIUM_APPSFLYER_VERSION) {

    lateinit var appsFlyerInstance: AppsFlyerCommand

    init {
        RemoteCommandLogger.logLevel = logLevel
    }

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
                    Command.DISABLE_DEVICE_TRACKING -> anonymizeUser(payload)
                    Command.RESOLVE_DEEPLINK_URLS -> resolveDeepLinkUrls(payload)
                    Command.STOP_TRACKING,
                    Command.DISABLE_TRACKING -> stopTracking(payload)
                    Command.LOG_SESSION -> appsFlyerInstance.logSession()
                    Command.SET_OAID -> setOaid(payload)
                    Command.SET_OUT_OF_STORE -> setOutOfStore(payload)
                    Command.SET_DISABLE_NETWORK_DATA -> setDisableNetworkData(payload)
                    Command.SET_APP_INVITE_ONE_LINK -> setAppInviteOneLink(payload)
                    Command.SET_PREINSTALL_ATTRIBUTION -> setPreinstallAttribution(payload)
                    Command.SET_IS_UPDATE -> setIsUpdate(payload)
                    Command.SET_LOG_LEVEL -> setLogLevel(payload)
                    null -> dispatchCustomEvent(commandString, payload)
                }
            } catch (e: AppsFlyerCommandError) {
                RemoteCommandLogger.error("Command '$commandString' failed: ${e.message}")
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
        val devKey: String = payload.optString(Config.DEV_KEY)
        if (devKey.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(Config.DEV_KEY)
        }
        val config: JSONObject? = payload.optJSONObject(Config.SETTINGS)
        val configSettings: Map<String, Any>? = jsonToMap(config)
        RemoteCommandLogger.debug("Initializing AppsFlyer SDK")
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
        val host: String = payload.optString(Host.HOST)
        val hostPrefix: String = payload.optString(Host.HOST_PREFIX)

        if (host.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(Host.HOST)
        }
        if (hostPrefix.isNotEmpty()) {
            appsFlyerInstance.setHost(host, hostPrefix)
        } else {
            appsFlyerInstance.setHost(host)
        }
    }

    private fun setUserEmails(payload: JSONObject) {
        val emails: JSONArray = payload.optJSONArray(Customer.EMAILS)
            ?: throw AppsFlyerCommandError.missingParameter(Customer.EMAILS)
        appsFlyerInstance.setUserEmails(toList(emails))
    }

    private fun setCurrencyCode(payload: JSONObject) {
        val currencyCode: String = payload.optString(TransactionProperties.CURRENCY)
        if (currencyCode.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(TransactionProperties.CURRENCY)
        }
        appsFlyerInstance.setCurrencyCode(currencyCode)
    }

    private fun setCustomerId(payload: JSONObject) {
        val id: String = payload.optString(Customer.USER_ID)
        if (id.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(Customer.USER_ID)
        }
        appsFlyerInstance.setCustomerId(id)
    }

    private fun setPhoneNumber(payload: JSONObject) {
        val phoneNumber: String = payload.optString(PhoneNumberParam.PHONE_NUMBER)
        if (phoneNumber.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(PhoneNumberParam.PHONE_NUMBER)
        }
        appsFlyerInstance.setPhoneNumber(phoneNumber)
    }

    private fun logAdRevenue(payload: JSONObject) {
        val monetizationNetwork = payload.optString(AdRevenueParams.MONETIZATION_NETWORK)
        val mediationNetworkString = payload.optString(AdRevenueParams.MEDIATION_NETWORK)
        val currency = payload.optString(AdRevenueParams.AD_REVENUE_CURRENCY)
        val revenue = payload.optDouble(AdRevenueParams.AD_REVENUE_AMOUNT)

        if (monetizationNetwork.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(AdRevenueParams.MONETIZATION_NETWORK)
        }
        if (mediationNetworkString.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(AdRevenueParams.MEDIATION_NETWORK)
        }
        if (currency.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(AdRevenueParams.AD_REVENUE_CURRENCY)
        }
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
        val isUserSubjectToGDPR = requireBoolean(payload, ConsentDataParams.IS_USER_SUBJECT_TO_GDPR)
        val hasConsentForDataUsage = requireBoolean(payload, ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE)
        val hasConsentForAdsPersonalization = requireBoolean(payload, ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION)
        val hasConsentForAdStorage = requireBoolean(payload, ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE)

        appsFlyerInstance.setConsentData(
            isUserSubjectToGDPR,
            hasConsentForDataUsage,
            hasConsentForAdsPersonalization,
            hasConsentForAdStorage
        )
    }

    private fun setPartnerData(payload: JSONObject) {
        val partnerId: String = payload.optString(PartnerDataParams.PARTNER_ID)
        if (partnerId.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(PartnerDataParams.PARTNER_ID)
        }
        val partnerInfoMap = jsonToMap(payload.optJSONObject(PartnerDataParams.PARTNER_INFO))
        appsFlyerInstance.setPartnerData(partnerId, partnerInfoMap)
    }

    private fun setSharingFilterForPartners(payload: JSONObject) {
        val sharingFilterJsonArray: JSONArray? = payload.optJSONArray(SharingFilterParams.SHARING_FILTER)
        // A missing array resets the filter by passing null to the SDK wrapper.
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
        appsFlyerInstance.stopTracking(payload.optBoolean(Tracking.STOP_TRACKING))
    }

    private fun setOaid(payload: JSONObject) {
        val oaid = payload.optString(StringCommandParams.OAID)
        if (oaid.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.OAID)
        }
        appsFlyerInstance.setOaid(oaid)
    }

    private fun setOutOfStore(payload: JSONObject) {
        val storeName = payload.optString(StringCommandParams.STORE_NAME)
        if (storeName.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.STORE_NAME)
        }
        appsFlyerInstance.setOutOfStore(storeName)
    }

    private fun setDisableNetworkData(payload: JSONObject) {
        if (!payload.has(StringCommandParams.DISABLE_NETWORK_DATA)) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.DISABLE_NETWORK_DATA)
        }
        appsFlyerInstance.setDisableNetworkData(payload.optBoolean(StringCommandParams.DISABLE_NETWORK_DATA))
    }

    private fun setAppInviteOneLink(payload: JSONObject) {
        val oneLinkId = payload.optString(StringCommandParams.APP_INVITE_ONE_LINK_ID)
        if (oneLinkId.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.APP_INVITE_ONE_LINK_ID)
        }
        appsFlyerInstance.setAppInviteOneLink(oneLinkId)
    }

    private fun setPreinstallAttribution(payload: JSONObject) {
        val mediaSource = payload.optString(PreinstallParams.MEDIA_SOURCE)
        val campaign = payload.optString(PreinstallParams.CAMPAIGN)
        val siteId = payload.optString(PreinstallParams.SITE_ID)
        if (mediaSource.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(PreinstallParams.MEDIA_SOURCE)
        }
        if (campaign.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(PreinstallParams.CAMPAIGN)
        }
        if (siteId.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(PreinstallParams.SITE_ID)
        }
        appsFlyerInstance.setPreinstallAttribution(mediaSource, campaign, siteId)
    }

    private fun setIsUpdate(payload: JSONObject) {
        if (!payload.has(StringCommandParams.IS_UPDATE)) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.IS_UPDATE)
        }
        appsFlyerInstance.setIsUpdate(payload.optBoolean(StringCommandParams.IS_UPDATE))
    }

    private fun setLogLevel(payload: JSONObject) {
        val logLevel = payload.optString(StringCommandParams.LOG_LEVEL)
        if (logLevel.isEmpty()) {
            throw AppsFlyerCommandError.missingParameter(StringCommandParams.LOG_LEVEL)
        }
        appsFlyerInstance.setLogLevel(logLevel)
    }

    private fun dispatchCustomEvent(commandString: String, payload: JSONObject) {
        val eventType = standardEvent(commandString) ?: commandString
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
                it
            )
        }
    }

    private fun jsonToMap(jsonObject: JSONObject?): Map<String, Any> {
        val map = HashMap<String, Any>()

        jsonObject?.let {
            it.keys().forEach { key ->
                val value = it[key]
                map[key] = value
            }
        }
        return map
    }

    private fun toList(jsonArray: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getString(i)
            list.add(i, item)
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
            METHOD_KEY,
            APP_ID_KEY,
        )
        for (key in jsonObject.keys()) {
            if (toRemove.contains(key)) continue

            toCopy.add(key)
        }

        return JSONObject(jsonObject, toCopy.toTypedArray())
    }
}

private const val METHOD_KEY = "method"
private const val APP_ID_KEY = "app_id"
