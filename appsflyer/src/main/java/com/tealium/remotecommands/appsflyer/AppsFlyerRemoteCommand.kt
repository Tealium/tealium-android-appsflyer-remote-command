package com.tealium.remotecommands.appsflyer

import android.app.Application
import android.util.Log
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

                Commands.LOG_SESSION -> {
                    appsFlyerInstance.logSession()
                }

                Commands.SET_HOST -> {
                    setHost(payload)
                }

                Commands.SET_USER_EMAILS -> {
                    val emails: JSONArray? = payload.optJSONArray(Customer.EMAILS)
                    emails?.let {
                        val emailList = toList(emails)
                        val cryptType = payload.optInt(Customer.EMAIL_HASH_TYPE, 0)
                        appsFlyerInstance.setUserEmails(emailList, cryptType)
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

                Commands.DISABLE_DEVICE_TRACKING -> {
                    val disableTracking: Boolean? =
                        payload.optBoolean(Tracking.DISABLE_DEVICE_TRACKING, false)
                    disableTracking?.let {
                        appsFlyerInstance.disableDeviceTracking(it)
                    } ?: run {
                        Log.w(
                            TAG,
                            "${Tracking.DISABLE_DEVICE_TRACKING} is a required key"
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

                Commands.DISABLE_TRACKING -> {
                    val stopTracking: Boolean = payload.optBoolean(Tracking.STOP_TRACKING)
                    stopTracking?.let {
                        appsFlyerInstance.stopTracking(it)
                    }
                }

                Commands.ADD_PUSH_NOTIFICATION_DEEP_LINK_PATH -> {
                    val pathJsonArray: JSONArray? = payload.optJSONArray(PushNotification.DEEP_LINK_PATH)
                    pathJsonArray?.let {
                        val pathList = toList(it)
                        appsFlyerInstance.addPushNotificationDeepLinkPath(pathList)
                    } ?: run {
                        Log.w(
                            TAG,
                            "${PushNotification.DEEP_LINK_PATH} is a required key"
                        )
                    }
                }

                Commands.ANONYMIZE_USER -> {
                    val shouldAnonymize: Boolean = payload.optBoolean(AnonymizeUser.SHOULD_ANONYMIZE, false)
                    appsFlyerInstance.anonymizeUser(shouldAnonymize)
                }

                // New commands for additional Android SDK features
                Commands.SET_IS_UPDATE -> {
                    val isUpdate: Boolean = payload.optBoolean(Config.IS_UPDATE, false)
                    appsFlyerInstance.setIsUpdate(isUpdate)
                }

                Commands.LOG_AD_REVENUE -> {
                    logAdRevenue(payload)
                }

                Commands.SET_DMA_CONSENT -> {
                    setDMAConsent(payload)
                }

                Commands.SET_PHONE_NUMBER -> {
                    val phoneNumber: String = payload.optString(PhoneNumber.PHONE_NUMBER)
                    if (phoneNumber.isNotEmpty()) {
                        appsFlyerInstance.setPhoneNumber(phoneNumber)
                    } else {
                        Log.w(TAG, "${PhoneNumber.PHONE_NUMBER} is a required key")
                    }
                }

                Commands.VALIDATE_AND_LOG_PURCHASE -> {
                    validateAndLogPurchase(payload)
                }

                Commands.SET_SHARING_FILTER_FOR_PARTNERS -> {
                    val partnersJsonArray: JSONArray? = payload.optJSONArray(Partner.SHARING_FILTER_PARTNERS)
                    partnersJsonArray?.let {
                        val partnersList = toList(it)
                        appsFlyerInstance.setSharingFilterForPartners(partnersList)
                    } ?: run {
                        Log.w(TAG, "${Partner.SHARING_FILTER_PARTNERS} is a required key")
                    }
                }

                Commands.APPEND_CUSTOM_DATA -> {
                    val customDataJson: JSONObject? = payload.optJSONObject(Partner.CUSTOM_DATA_TO_APPEND)
                    customDataJson?.let {
                        val customDataMap = jsonToMap(it)
                        appsFlyerInstance.appendCustomData(customDataMap)
                    } ?: run {
                        Log.w(TAG, "${Partner.CUSTOM_DATA_TO_APPEND} is a required key")
                    }
                }

                Commands.SET_DEVICE_LANGUAGE -> {
                    val language: String = payload.optString(Partner.DEVICE_LANGUAGE)
                    if (language.isNotEmpty()) {
                        appsFlyerInstance.setDeviceLanguage(language)
                    } else {
                        Log.w(TAG, "${Partner.DEVICE_LANGUAGE} is a required key")
                    }
                }

                Commands.SET_PARTNER_DATA -> {
                    setPartnerData(payload)
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
        val appId: String = payload.optString(Config.APP_ID)
        val config: JSONObject? = payload.optJSONObject(Config.SETTINGS)
        val configSettings: Map<String, Any> = jsonToMap(config)
        
        appsFlyerInstance.initialize(devKey, appId, configSettings)
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
            Config.DEBUG,
            Config.DEV_KEY,
            Config.APP_ID,
            Config.SETTINGS,
            Commands.COMMAND_KEY,
            "method",
        )
        for (key in jsonObject.keys()) {
            if (toRemove.contains(key)) continue

            toCopy.add(key)
        }

        return JSONObject(jsonObject, toCopy.toTypedArray())
    }

    private fun logAdRevenue(payload: JSONObject) {
        val monetizationNetwork: String = payload.optString(AdRevenue.AD_MONETIZATION_NETWORK)
        val mediationNetwork: String = payload.optString(AdRevenue.AD_MEDIATION_NETWORK)
        val revenue: Double = payload.optDouble(AdRevenue.AD_REVENUE)
        val currency: String = payload.optString(TransactionProperties.CURRENCY)
        val additionalParamsJson: JSONObject? = payload.optJSONObject(AdRevenue.AD_ADDITIONAL_PARAMETERS)

        if (monetizationNetwork.isNotEmpty() && mediationNetwork.isNotEmpty() && 
            !revenue.isNaN() && currency.isNotEmpty()) {
            
            val additionalParams = additionalParamsJson?.let { jsonToMap(it) }
            appsFlyerInstance.logAdRevenue(monetizationNetwork, mediationNetwork, revenue, currency, additionalParams)
        } else {
            Log.w(TAG, "logAdRevenue requires monetization_network, mediation_network, revenue, and currency")
        }
    }

    private fun setDMAConsent(payload: JSONObject) {
        val gdprApplies: Boolean = payload.optBoolean(DMAConsent.GDPR_APPLIES, false)
        val consentForDataUsage: Boolean = payload.optBoolean(DMAConsent.CONSENT_FOR_DATA_USAGE, false)
        val consentForAdsPersonalization: Boolean = payload.optBoolean(DMAConsent.CONSENT_FOR_ADS_PERSONALIZATION, false)
        val consentForAdStorage: Boolean = payload.optBoolean(DMAConsent.CONSENT_FOR_AD_STORAGE, false)

        appsFlyerInstance.setDMAConsent(gdprApplies, consentForDataUsage, consentForAdsPersonalization, consentForAdStorage)
    }

    private fun validateAndLogPurchase(payload: JSONObject) {
        val purchaseType: String = payload.optString(PurchaseValidation.PURCHASE_TYPE)
        val purchaseToken: String = payload.optString(PurchaseValidation.TRANSACTION_ID)
        val productId: String = payload.optString(PurchaseValidation.PRODUCT_ID)
        val price: String = payload.optString(PurchaseValidation.PRICE)
        val currency: String = payload.optString(PurchaseValidation.CURRENCY)
        val additionalParamsJson: JSONObject? = payload.optJSONObject(PurchaseValidation.ADDITIONAL_PARAMETERS)

        if (purchaseType.isNotEmpty() && purchaseToken.isNotEmpty() && 
            productId.isNotEmpty() && price.isNotEmpty() && currency.isNotEmpty()) {
            
            val additionalParams = additionalParamsJson?.let { 
                jsonToMap(it).mapValues { entry -> entry.value.toString() }
            }
            appsFlyerInstance.validateAndLogPurchase(purchaseType, purchaseToken, productId, price, currency, additionalParams)
        } else {
            Log.w(TAG, "validateAndLogPurchase requires purchase_type, transaction_id, product_id, price, and currency")
        }
    }

    private fun setPartnerData(payload: JSONObject) {
        val partnerId: String = payload.optString(Partner.PARTNER_ID)
        val partnerInfoJson: JSONObject? = payload.optJSONObject(Partner.PARTNER_INFO)

        if (partnerId.isNotEmpty() && partnerInfoJson != null) {
            val partnerData = jsonToMap(partnerInfoJson)
            appsFlyerInstance.setPartnerData(partnerId, partnerData)
        } else {
            Log.w(TAG, "setPartnerData requires partner_id and partner_info")
        }
    }
}