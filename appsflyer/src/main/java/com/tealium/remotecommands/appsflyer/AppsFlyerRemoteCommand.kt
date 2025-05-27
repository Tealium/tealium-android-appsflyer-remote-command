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

                Commands.SET_HOST -> {
                    setHost(payload)
                }

                Commands.SET_USER_EMAILS -> {
                    val emails: JSONArray? = payload.optJSONArray(Customer.EMAILS)
                    val hashTypeString: String? = payload.optString(Customer.EMAIL_HASH_TYPE).takeIf { it.isNotEmpty() }
                    val hashType: EmailHashType? = EmailHashType.fromString(hashTypeString)
                    
                    // Log warning if invalid hash type was provided
                    if (hashTypeString != null && hashType == null) {
                        Log.w(TAG, "Invalid email hash type: $hashTypeString. Valid values are: none, sha256")
                    }
                    
                    emails?.let {
                        val emailList = toList(emails)
                        appsFlyerInstance.setUserEmails(emailList, hashType)
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

                Commands.ANONYMIZE_USER -> {
                    val anonymizeUser: Boolean =
                        payload.optBoolean(Tracking.ANONYMIZE_USER, false)

                    appsFlyerInstance.anonymizeUser(anonymizeUser)
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
                    val stopTracking: Boolean = payload.optBoolean(Tracking.STOP_TRACKING)
                    stopTracking.let {
                        appsFlyerInstance.stopTracking(it)
                    }
                }

                Commands.ENABLE_APPSET_ID -> {
                    val enable = payload.optBoolean(Config.ENABLE_APPSET_ID, true)
                    appsFlyerInstance.enableAppSetIdCollection(enable)
                }

                Commands.SET_DISABLE_NETWORK_DATA -> {
                    val disable = payload.optBoolean(Config.DISABLE_NETWORK_DATA, false)
                    appsFlyerInstance.setDisableNetworkData(disable)
                }

                Commands.SET_DMA_CONSENT -> {
                    // Check if the required GDPR_APPLIES parameter exists
                    if (!payload.has(DMAConsent.GDPR_APPLIES)) {
                        Log.e(TAG, "${DMAConsent.GDPR_APPLIES} is a required key")
                        return@forEach
                    }
                    
                    val gdprApplies = payload.optBoolean(DMAConsent.GDPR_APPLIES)
                    val consentData = mutableMapOf<String, Any>(DMAConsent.GDPR_APPLIES to gdprApplies)
                    
                    // If GDPR applies, collect the other consent parameters if available
                    if (gdprApplies) {
                        if (payload.has(DMAConsent.CONSENT_FOR_DATA_USAGE)) {
                            consentData[DMAConsent.CONSENT_FOR_DATA_USAGE] = payload.optBoolean(DMAConsent.CONSENT_FOR_DATA_USAGE)
                        }
                        
                        if (payload.has(DMAConsent.CONSENT_FOR_ADS_PERSONALIZATION)) {
                            consentData[DMAConsent.CONSENT_FOR_ADS_PERSONALIZATION] = payload.optBoolean(DMAConsent.CONSENT_FOR_ADS_PERSONALIZATION)
                        }
                        
                        if (payload.has(DMAConsent.CONSENT_FOR_AD_STORAGE)) {
                            consentData[DMAConsent.CONSENT_FOR_AD_STORAGE] = payload.optBoolean(DMAConsent.CONSENT_FOR_AD_STORAGE)
                        }
                    }
                    
                    appsFlyerInstance.setDMAConsentData(consentData)
                }
                
                Commands.LOG_AD_REVENUE -> {
                    val monetizationNetwork = payload.optString(AdRevenue.MONETIZATION_NETWORK)
                    val mediationNetworkString = payload.optString(AdRevenue.MEDIATION_NETWORK)
                    val revenue = payload.optDouble(AdRevenue.REVENUE)
                    val currency = payload.optString(AdRevenue.CURRENCY, "USD")
                    
                    if (monetizationNetwork.isEmpty()) {
                        Log.e(TAG, "${AdRevenue.MONETIZATION_NETWORK} is a required key")
                        return
                    }
                    
                    if (mediationNetworkString.isEmpty()) {
                        Log.e(TAG, "${AdRevenue.MEDIATION_NETWORK} is a required key")
                        return
                    }
                    
                    // Convert string to MediationNetwork enum
                    val mediationNetwork = MediationNetworkType.fromString(mediationNetworkString)
                    if (mediationNetwork == null) {
                        Log.e(TAG, "Invalid mediation network value: $mediationNetworkString. Valid values are: ironsource, applovinmax, googleadmob, fyber, appodeal, admost, etc.")
                        return
                    }
                    
                    if (revenue.isNaN() || revenue <= 0) {
                        Log.e(TAG, "${AdRevenue.REVENUE} is a required key and must be positive")
                        return
                    }

                    if (currency.isEmpty()) {
                        Log.e(TAG, "${AdRevenue.CURRENCY} is a required key")
                        return
                    }
                    
                    // Get additional parameters if available
                    val additionalParams = payload.optJSONObject(AdRevenue.ADDITIONAL_PARAMETERS)
                    val additionalParamsMap = jsonToMap(additionalParams)
                    
                    appsFlyerInstance.logAdRevenue(
                        monetizationNetwork,
                        mediationNetwork.toMediationNetwork(),
                        revenue,
                        currency,
                        additionalParamsMap
                    )
                }

                Commands.SET_PHONE_NUMBER -> {
                    val phoneNumber = payload.optString(PhoneNumber.PHONE_NUMBER)
                    if (phoneNumber.isNotEmpty()) {
                        appsFlyerInstance.setPhoneNumber(phoneNumber)
                    } else {
                        Log.w(TAG, "${PhoneNumber.PHONE_NUMBER} is a required key")
                    }
                }

                Commands.SET_OUT_OF_STORE -> {
                    val sourceName = payload.optString(OutOfStore.SOURCE_NAME)
                    if (sourceName.isNotEmpty()) {
                        appsFlyerInstance.setOutOfStore(sourceName)
                    } else {
                        Log.w(TAG, "${OutOfStore.SOURCE_NAME} is a required key")
                    }
                }

                Commands.ADD_PUSH_NOTIFICATION_DEEP_LINK_PATH -> {
                    val deepLinkPathArray = payload.optJSONArray(PushNotification.DEEP_LINK_PATH)
                    deepLinkPathArray?.let {
                        val pathList = toList(it)
                        appsFlyerInstance.addPushNotificationDeepLinkPath(pathList)
                    } ?: run {
                        Log.w(TAG, "${PushNotification.DEEP_LINK_PATH} is a required key")
                    }
                }

                Commands.SEND_PUSH_NOTIFICATION_DATA -> {
                    appsFlyerInstance.sendPushNotificationData()
                }

                Commands.VALIDATE_AND_LOG_PURCHASE -> {
                    val purchaseTypeString = payload.optString(InAppPurchase.PURCHASE_TYPE)
                    val purchaseToken = payload.optString(InAppPurchase.PURCHASE_TOKEN)
                    val productId = payload.optString(InAppPurchase.PRODUCT_ID)
                    val price = payload.optString(InAppPurchase.PRICE)
                    val currency = payload.optString(InAppPurchase.CURRENCY)
                    
                    val purchaseType = PurchaseType.fromString(purchaseTypeString)
                    
                    if (purchaseType == null) {
                        Log.e(TAG, "Invalid or missing ${InAppPurchase.PURCHASE_TYPE}. Valid values are: one_time_purchase, subscription")
                        return@forEach
                    }
                    
                    if (purchaseToken.isEmpty()) {
                        Log.e(TAG, "${InAppPurchase.PURCHASE_TOKEN} is a required key")
                        return@forEach
                    }
                    
                    if (productId.isEmpty()) {
                        Log.e(TAG, "${InAppPurchase.PRODUCT_ID} is a required key")
                        return@forEach
                    }
                    
                    if (price.isEmpty()) {
                        Log.e(TAG, "${InAppPurchase.PRICE} is a required key")
                        return@forEach
                    }
                    
                    if (currency.isEmpty()) {
                        Log.e(TAG, "${InAppPurchase.CURRENCY} is a required key")
                        return@forEach
                    }
                    
                    val additionalParams = payload.optJSONObject(InAppPurchase.ADDITIONAL_PARAMETERS)
                    val additionalParamsMap = jsonToMap(additionalParams)
                    
                    appsFlyerInstance.validateAndLogInAppPurchase(
                        purchaseType,
                        purchaseToken,
                        productId,
                        price,
                        currency,
                        additionalParamsMap
                    )
                }

                Commands.LOG_SESSION -> {
                    appsFlyerInstance.logSession()
                }

                Commands.WAIT_FOR_CUSTOMER_USER_ID -> {
                    val wait = payload.optBoolean(CustomerUserID.WAIT_FOR_CUSTOMER_USER_ID, false)
                    appsFlyerInstance.waitForCustomerUserId(wait)
                }

                Commands.SET_CUSTOMER_ID_AND_LOG_SESSION -> {
                    val customerId = payload.optString(Customer.USER_ID)
                    if (customerId.isNotEmpty()) {
                        appsFlyerInstance.setCustomerIdAndLogSession(customerId)
                    } else {
                        Log.e(TAG, "${Customer.USER_ID} is a required key for setCustomerIdAndLogSession")
                    }
                }

                Commands.SET_MIN_TIME_BETWEEN_SESSIONS -> {
                    val seconds = payload.optInt(AppConfig.MIN_TIME_BETWEEN_SESSIONS, 5)
                    appsFlyerInstance.setMinTimeBetweenSessions(seconds)
                }

                Commands.SET_APP_ID -> {
                    val appId = payload.optString(AppConfig.APP_ID)
                    if (appId.isNotEmpty()) {
                        appsFlyerInstance.setAppId(appId)
                    } else {
                        Log.w(TAG, "${AppConfig.APP_ID} is a required key")
                    }
                }

                Commands.SET_DISABLE_ADVERTISING_IDENTIFIERS -> {
                    val disable = payload.optBoolean(Privacy.DISABLE_ADVERTISING_IDENTIFIERS, false)
                    appsFlyerInstance.setDisableAdvertisingIdentifiers(disable)
                }

                Commands.ENABLE_TCF_DATA_COLLECTION -> {
                    val enable = payload.optBoolean(Privacy.ENABLE_TCF_DATA_COLLECTION, false)
                    appsFlyerInstance.enableTCFDataCollection(enable)
                }

                Commands.SET_SHARING_FILTER_FOR_PARTNERS -> {
                    val partnersArray = payload.optJSONArray(Privacy.SHARING_FILTER_PARTNERS)
                    partnersArray?.let {
                        val partnersList = toList(it)
                        appsFlyerInstance.setSharingFilterForPartners(partnersList)
                    } ?: run {
                        // If no array provided, check for single string or "all"
                        val singlePartner = payload.optString(Privacy.SHARING_FILTER_PARTNERS)
                        if (singlePartner.isNotEmpty()) {
                            appsFlyerInstance.setSharingFilterForPartners(listOf(singlePartner))
                        } else {
                            Log.w(TAG, "${Privacy.SHARING_FILTER_PARTNERS} parameter is required")
                        }
                    }
                }

                Commands.UPDATE_SERVER_UNINSTALL_TOKEN -> {
                    val token = payload.optString(Analytics.UNINSTALL_TOKEN)
                    if (token.isNotEmpty()) {
                        appsFlyerInstance.updateServerUninstallToken(token)
                    } else {
                        Log.w(TAG, "${Analytics.UNINSTALL_TOKEN} is a required key")
                    }
                }

                Commands.SET_IS_UPDATE -> {
                    val isUpdate = payload.optBoolean(Analytics.IS_UPDATE, false)
                    appsFlyerInstance.setIsUpdate(isUpdate)
                }

                Commands.SET_ADDITIONAL_DATA -> {
                    val additionalDataObj = payload.optJSONObject(Analytics.ADDITIONAL_DATA)
                    additionalDataObj?.let {
                        val additionalDataMap = jsonToMap(it)
                        appsFlyerInstance.setAdditionalData(additionalDataMap)
                    } ?: run {
                        Log.w(TAG, "${Analytics.ADDITIONAL_DATA} parameter is required")
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
        
        // Collect all configuration parameters from payload (flat structure)
        val configSettings = mutableMapOf<String, Any>()
        
        // Add all config parameters if they exist in payload
        if (payload.has(Config.DEBUG)) {
            configSettings[Config.DEBUG] = payload.optBoolean(Config.DEBUG)
        }
        if (payload.has(Config.DISABLE_NETWORK_DATA)) {
            configSettings[Config.DISABLE_NETWORK_DATA] = payload.optBoolean(Config.DISABLE_NETWORK_DATA)
        }
        if (payload.has(Config.ANONYMIZE_USER)) {
            configSettings[Config.ANONYMIZE_USER] = payload.optBoolean(Config.ANONYMIZE_USER)
        }
        if (payload.has(Config.MIN_TIME_BETWEEN_SESSIONS)) {
            configSettings[Config.MIN_TIME_BETWEEN_SESSIONS] = payload.optInt(Config.MIN_TIME_BETWEEN_SESSIONS)
        }
        if (payload.has(Config.ENABLE_APPSET_ID)) {
            configSettings[Config.ENABLE_APPSET_ID] = payload.optBoolean(Config.ENABLE_APPSET_ID)
        }
        if (payload.has(Config.COLLECT_DEVICE_NAME)) {
            configSettings[Config.COLLECT_DEVICE_NAME] = payload.optBoolean(Config.COLLECT_DEVICE_NAME)
        }
        if (payload.has(Config.DISABLE_AD_TRACKING)) {
            configSettings[Config.DISABLE_AD_TRACKING] = payload.optBoolean(Config.DISABLE_AD_TRACKING)
        }
        if (payload.has(Config.DISABLE_APPLE_AD_TRACKING)) {
            configSettings[Config.DISABLE_APPLE_AD_TRACKING] = payload.optBoolean(Config.DISABLE_APPLE_AD_TRACKING)
        }
        if (payload.has(Config.CUSTOM_DATA)) {
            val customData = payload.optJSONObject(Config.CUSTOM_DATA)
            if (customData != null) {
                configSettings[Config.CUSTOM_DATA] = customData
            }
        }

        // Handle disable_network_data before initialization
        if (configSettings.containsKey(Config.DISABLE_NETWORK_DATA)) {
            (configSettings[Config.DISABLE_NETWORK_DATA] as? Boolean)?.let { disable ->
                appsFlyerInstance.setDisableNetworkData(disable)
            }
        }

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
            Config.DEBUG,
            Config.DEV_KEY,
            Config.ANONYMIZE_USER,
            Config.DISABLE_NETWORK_DATA,
            Config.ENABLE_APPSET_ID,
            Config.MIN_TIME_BETWEEN_SESSIONS,
            Config.COLLECT_DEVICE_NAME,
            Config.DISABLE_AD_TRACKING,
            Config.DISABLE_APPLE_AD_TRACKING,
            Config.CUSTOM_DATA,
            Commands.COMMAND_KEY,
            "method",
            "app_id",
        )
        for (key in jsonObject.keys()) {
            if (toRemove.contains(key)) continue

            toCopy.add(key)
        }

        return JSONObject(jsonObject, toCopy.toTypedArray())
    }
}