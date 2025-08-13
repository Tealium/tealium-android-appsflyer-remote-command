package com.tealium.remotecommands.appsflyer

import android.app.Application
import android.util.Log
import com.appsflyer.AFAdRevenueData
import com.appsflyer.MediationNetwork
import com.tealium.remotecommands.RemoteCommand
import com.tealium.remotecommands.RemoteCommandContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.jvm.Throws

open class AppsFlyerRemoteCommand(
    private val application: Application,
    private val appsFlyerDevKey: String? = null,
    commandId: String = DEFAULT_COMMAND_ID,
    description: String = DEFAULT_COMMAND_DESCRIPTION
) : RemoteCommand(commandId, description, BuildConfig.TEALIUM_APPSFLYER_VERSION) {

    private val TAG = this::class.java.simpleName

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
        commands.forEach { command ->
            if (command.isBlank()) return@forEach

            when (command) {
                Commands.INITIALIZE -> {
                    initialize(payload)
                }

                Commands.TRACK_LOCATION -> {
                    trackLocation(payload)
                }

                Commands.SET_HOST -> {
                    setHost(payload)
                }

                Commands.SET_USER_EMAILS -> {
                    val emails: JSONArray? = payload.optJSONArray(Customer.EMAILS)
                    emails?.let {
                        val emailList = toList(emails)
                        appsFlyerInstance.setUserEmails(emailList)
                    }
                }

                Commands.SET_CURRENCY_CODE -> {
                    val currencyCode: String = payload.optString(TransactionProperties.CURRENCY)

                    if (currencyCode.isNotEmpty()) {
                        appsFlyerInstance.setCurrencyCode(currencyCode)
                    } else {
                        Log.w(
                            TAG,
                            "${TransactionProperties.CURRENCY} is required key"
                        )
                    }
                }

                Commands.SET_CUSTOMER_ID -> {
                    val id: String = payload.optString(Customer.USER_ID)
                    if (id.isNotEmpty()) {
                        appsFlyerInstance.setCustomerId(id)
                    } else {
                        Log.e(
                            TAG,
                            "${Customer.USER_ID} is a required key"
                        )
                    }
                }

                Commands.SET_PHONE_NUMBER -> {
                    val phoneNumber: String = payload.optString(PhoneNumberParam.PHONE_NUMBER)
                    if (phoneNumber.isNotEmpty()) {
                        appsFlyerInstance.setPhoneNumber(phoneNumber)
                    } else {
                        Log.w(
                            TAG,
                            "${PhoneNumberParam.PHONE_NUMBER} is a required key"
                        )
                    }
                }

                Commands.LOG_AD_REVENUE -> {
                    val monetizationNetwork: String = payload.optString(AdRevenueParams.MONETIZATION_NETWORK)
                    val mediationNetwork: String = payload.optString(AdRevenueParams.MEDIATION_NETWORK)
                    val currency: String = payload.optString(AdRevenueParams.AD_REVENUE_CURRENCY)
                    val revenue: Double = payload.optDouble(AdRevenueParams.AD_REVENUE_AMOUNT)
                    
                    if (monetizationNetwork.isNotEmpty() && 
                        mediationNetwork.isNotEmpty() && 
                        currency.isNotEmpty() && 
                        !revenue.isNaN()
                    ) {
                        try {
                            // Convert mediation network string to MediationNetwork enum using mapping
                            val mediationNetworkEnumName = MediationNetworks.networkNames[mediationNetwork.lowercase()]
                            val mediationNetworkEnum = if (mediationNetworkEnumName != null) {
                                try {
                                    MediationNetwork.valueOf(mediationNetworkEnumName)
                                } catch (e: IllegalArgumentException) {
                                    Log.w(TAG, "Invalid enum value for mediation network: $mediationNetworkEnumName")
                                    null
                                }
                            } else {
                                Log.w(TAG, "Unknown mediation network: $mediationNetwork, supported networks: ${MediationNetworks.networkNames.keys.joinToString(", ")}")
                                null
                            }
                            
                            if (mediationNetworkEnum != null) {
                                val adRevenueData = AFAdRevenueData(
                                    monetizationNetwork,
                                    mediationNetworkEnum,
                                    currency,
                                    revenue
                                )
                                
                                val additionalParams: JSONObject? = payload.optJSONObject(AdRevenueParams.AD_REVENUE_ADDITIONAL_PARAMS)
                                val additionalParamsMap = jsonToMap(additionalParams)
                                
                                appsFlyerInstance.logAdRevenue(adRevenueData, additionalParamsMap)
                            } else {
                                Log.e(TAG, "Cannot log ad revenue: invalid mediation network '$mediationNetwork'")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error logging ad revenue: ${e.message}")
                        }
                    } else {
                        Log.w(
                            TAG,
                            "Ad revenue requires ${AdRevenueParams.MONETIZATION_NETWORK}, ${AdRevenueParams.MEDIATION_NETWORK}, ${AdRevenueParams.AD_REVENUE_CURRENCY}, and ${AdRevenueParams.AD_REVENUE_AMOUNT}"
                        )
                    }
                }

                Commands.SET_CONSENT_DATA -> {
                    val isUserSubjectToGDPR: Boolean? = payload.optBoolean(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR)
                    val hasConsentForDataUsage: Boolean? = payload.optBoolean(ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE)
                    val hasConsentForAdsPersonalization: Boolean? = payload.optBoolean(ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION)
                    val hasConsentForAdStorage: Boolean? = payload.optBoolean(ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE)
                    
                    if (isUserSubjectToGDPR != null && hasConsentForDataUsage != null && 
                        hasConsentForAdsPersonalization != null && hasConsentForAdStorage != null) {
                        
                        appsFlyerInstance.setConsentData(
                            isUserSubjectToGDPR,
                            hasConsentForDataUsage,
                            hasConsentForAdsPersonalization,
                            hasConsentForAdStorage
                        )
                    } else {
                        Log.w(
                            TAG,
                            "Consent data requires ${ConsentDataParams.IS_USER_SUBJECT_TO_GDPR}, ${ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE}, ${ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION}, and ${ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE}"
                        )
                    }
                }

                Commands.SET_PARTNER_DATA -> {
                    val partnerId: String = payload.optString(PartnerDataParams.PARTNER_ID)
                    
                    if (partnerId.isNotEmpty()) {
                        val partnerInfo: JSONObject? = payload.optJSONObject(PartnerDataParams.PARTNER_INFO)
                        val partnerInfoMap = jsonToMap(partnerInfo)
                        
                        appsFlyerInstance.setPartnerData(partnerId, partnerInfoMap)
                    } else {
                        Log.w(
                            TAG,
                            "${PartnerDataParams.PARTNER_ID} is a required key"
                        )
                    }
                }

                Commands.SET_SHARING_FILTER_FOR_PARTNERS -> {
                    val sharingFilterJsonArray: JSONArray? = payload.optJSONArray(SharingFilterParams.SHARING_FILTER)
                    val sharingFilterArray = if (sharingFilterJsonArray != null) {
                        toList(sharingFilterJsonArray).toTypedArray()
                    } else {
                        null // Reset filter
                    }
                    
                    appsFlyerInstance.setSharingFilterForPartners(sharingFilterArray)
                }

                Commands.ANONYMIZE_USER -> {
                    val anonymizeUser: Boolean? =
                        payload.optBoolean(Tracking.ANONYMIZE_USER, false)
                    anonymizeUser?.let {
                        appsFlyerInstance.anonymizeUser(it)
                    } ?: run {
                        Log.w(
                            TAG,
                            "${Tracking.ANONYMIZE_USER} is a required key"
                        )
                    }
                }

                Commands.RESOLVE_DEEPLINK_URLS -> {
                    val deepLinkJsonArray: JSONArray? = payload.optJSONArray(DeepLink.URLS)
                    deepLinkJsonArray?.let {
                        val deepLinkList = toList(it)
                        appsFlyerInstance.resolveDeepLinkUrls(deepLinkList)
                    } ?: run {
                        Log.w(
                            TAG,
                            "${DeepLink.URLS} is a required key"
                        )
                    }
                }

                Commands.STOP_TRACKING -> {
                    val stopTracking: Boolean? = payload.optBoolean(Tracking.STOP_TRACKING)
                    stopTracking?.let {
                        appsFlyerInstance.stopTracking(it)
                    }
                }

                else -> {
                    val eventType = standardEvent(command) ?: command
                    val eventParameters: JSONObject =
                        payload.optJSONObject(StandardEvents.EVENT_PARAMETERS)
                            ?: payload.optJSONObject(StandardEvents.EVENT_PARAMETERS_SHORT)
                            ?: filterPayload(payload)

                    val paramsMap = jsonToMap(eventParameters)
                    appsFlyerInstance.trackEvent(eventType, paramsMap)
                }
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
        val config: JSONObject? = payload.optJSONObject(Config.SETTINGS)
        val configSettings: Map<String, Any>? = jsonToMap(config)
        appsFlyerInstance.initialize(devKey, configSettings)
    }

    private fun trackLocation(payload: JSONObject) {
        val latitude: Double = payload.optDouble(Location.LATITUDE)
        val longitude: Double = payload.optDouble(Location.LONGITUDE)

        if (!latitude.isNaN() && !longitude.isNaN()) {
            appsFlyerInstance.trackLocation(latitude, longitude)
        } else {
            Log.w(
                TAG,
                "${Location.LATITUDE} and ${Location.LONGITUDE} are required keys"
            )
        }
    }

    private fun setHost(payload: JSONObject) {
        val host: String = payload.optString(Host.HOST)
        val hostPrefix: String = payload.optString(Host.HOST_PREFIX)

        if (host.isNotEmpty()) {
            if (hostPrefix.isNotEmpty()) {
                appsFlyerInstance.setHost(host, hostPrefix)
            } else {
                appsFlyerInstance.setHost(host)
            }
        } else {
            Log.w(
                TAG,
                "${Host.HOST} are required keys"
            )
        }
    }

    internal fun splitCommands(payload: JSONObject): Array<String> {
        val command = payload.optString(Commands.COMMAND_KEY, "")
        return command.split(Commands.SEPARATOR).map {
            it.trim().lowercase(Locale.ROOT)
        }.toTypedArray()
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
            "method",
            Config.APP_ID,
        )
        for (key in jsonObject.keys()) {
            if (toRemove.contains(key)) continue

            toCopy.add(key)
        }

        return JSONObject(jsonObject, toCopy.toTypedArray())
    }
}