package com.tealium.remotecommands.appsflyer

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.appsflyer.*
import com.tealium.remotecommands.RemoteCommandContext
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

class AppsFlyerInstance(
    private val application: Application,
    private var appsFlyerDevKey: String? = null,
    private val remoteCommandContext: RemoteCommandContext
) : AppsFlyerCommand {

    private var weakActivity: WeakReference<Activity>? = null

    init {
        getApplication()
    }

    override fun initialize(
        devKey: String?,
        configSettings: Map<String, Any>?
    ) {
        configSettings?.let { settings ->
            if (settings.containsKey(Config.MIN_TIME_BETWEEN_SESSIONS)) {
                setMinsBetweenSessions(settings[Config.MIN_TIME_BETWEEN_SESSIONS] as Int)
            }

            if (settings.containsKey(Config.ANONYMIZE_USER)) {
                anonymizeUser(settings[Config.ANONYMIZE_USER] as Boolean)
            }

            if (settings.containsKey(Config.CUSTOM_DATA)) {
                val data = toMap(settings[Config.CUSTOM_DATA] as JSONObject)
                val iterator = data.entries.iterator()
                val dataMap = HashMap<String, Any>()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    (entry.key as? String)?.let { key ->
                        entry.value.let { value ->
                            dataMap.put(key, value)
                        }
                    }
                }
                addCustomData(dataMap)
            }

            if (settings.containsKey(Config.DEBUG)) {
                enableDebugLog(settings[Config.DEBUG] as Boolean)
            }

            // New configuration options
            if (settings.containsKey(Config.DISABLE_AD_TRACKING)) {
                AppsFlyerLib.getInstance().setDisableAdvertisingIdentifiers(settings[Config.DISABLE_AD_TRACKING] as Boolean)
            }

            if (settings.containsKey(Config.DISABLE_NETWORK_DATA)) {
                AppsFlyerLib.getInstance().setDisableNetworkData(settings[Config.DISABLE_NETWORK_DATA] as Boolean)
            }

            if (settings.containsKey(Config.DISABLE_APPSET_ID)) {
                if (settings[Config.DISABLE_APPSET_ID] as Boolean) {
                    AppsFlyerLib.getInstance().disableAppSetId()
                }
            }

            if (settings.containsKey(Config.ENABLE_TCF_DATA_COLLECTION)) {
                AppsFlyerLib.getInstance().enableTCFDataCollection(settings[Config.ENABLE_TCF_DATA_COLLECTION] as Boolean)
            }

            if (settings.containsKey(Config.APP_INVITE_ONELINK_ID)) {
                AppsFlyerLib.getInstance().setAppInviteOneLink(settings[Config.APP_INVITE_ONELINK_ID] as String)
            }

            if (settings.containsKey(Config.ONELINK_CUSTOM_DOMAINS)) {
                val domains = (settings[Config.ONELINK_CUSTOM_DOMAINS] as? List<String>)?.toTypedArray()
                domains?.let {
                    AppsFlyerLib.getInstance().setOneLinkCustomDomain(*it)
                }
            }

            if (settings.containsKey(Config.CUSTOMER_EMAILS)) {
                val emails = (settings[Config.CUSTOMER_EMAILS] as? List<String>)?.toTypedArray()
                emails?.let {
                    val hashType = settings[Config.EMAIL_HASH_TYPE] as? Int ?: 0
                    val cryptType = AppsFlyerProperties.EmailsCryptType.values()[hashType]
                    AppsFlyerLib.getInstance().setUserEmails(cryptType, *it)
                }
            }

            if (settings.containsKey(Config.HOST) && settings.containsKey(Config.HOST_PREFIX)) {
                val host = settings[Config.HOST] as String
                val hostPrefix = settings[Config.HOST_PREFIX] as String
                AppsFlyerLib.getInstance().setHost(host, hostPrefix)
            }

            if (settings.containsKey(Config.RESOLVE_DEEP_LINKS)) {
                val urls = (settings[Config.RESOLVE_DEEP_LINKS] as? List<String>)?.toTypedArray()
                urls?.let {
                    AppsFlyerLib.getInstance().setResolveDeepLinkURLs(*it)
                }
            }

            // New configuration options based on Android SDK
            if (settings.containsKey(Config.COLLECT_ANDROID_ID)) {
                AppsFlyerLib.getInstance().setCollectAndroidID(settings[Config.COLLECT_ANDROID_ID] as Boolean)
            }
            
            if (settings.containsKey(Config.COLLECT_IMEI)) {
                AppsFlyerLib.getInstance().setCollectIMEI(settings[Config.COLLECT_IMEI] as Boolean)
            }
            
            if (settings.containsKey(Config.LOG_LEVEL)) {
                val logLevelString = settings[Config.LOG_LEVEL] as String
                val logLevel = LogLevel.fromString(logLevelString)
                logLevel?.let {
                    AppsFlyerLib.getInstance().setLogLevel(AFLogger.LogLevel.valueOf(it.value))
                }
            }
            
            if (settings.containsKey(Config.WAIT_FOR_CUSTOMER_USER_ID)) {
                AppsFlyerLib.getInstance().waitForCustomerUserId(settings[Config.WAIT_FOR_CUSTOMER_USER_ID] as Boolean)
            }
            
            if (settings.containsKey(Config.ENABLE_FACEBOOK_DEFERRED_APPLINKS)) {
                AppsFlyerLib.getInstance().enableFacebookDeferredApplinks(settings[Config.ENABLE_FACEBOOK_DEFERRED_APPLINKS] as Boolean)
            }
            
            if (settings.containsKey(Config.OUT_OF_STORE)) {
                val outOfStore = settings[Config.OUT_OF_STORE] as? String
                outOfStore?.let {
                    AppsFlyerLib.getInstance().setOutOfStore(it)
                }
            }
            
            if (settings.containsKey(Config.IS_UPDATE)) {
                AppsFlyerLib.getInstance().setIsUpdate(settings[Config.IS_UPDATE] as Boolean)
            }
            
            if (settings.containsKey(Config.EXTENSION)) {
                val extension = settings[Config.EXTENSION] as? String
                extension?.let {
                    AppsFlyerLib.getInstance().setExtension(it)
                }
            }
            
            if (settings.containsKey(Config.PREINSTALL_ATTRIBUTION)) {
                val preinstallData = settings[Config.PREINSTALL_ATTRIBUTION] as? Map<String, Any>
                preinstallData?.let { data ->
                    val mediaSource = data["media_source"] as? String
                    val campaign = data["campaign"] as? String  
                    val siteId = data["site_id"] as? String
                    
                    if (!mediaSource.isNullOrEmpty() && !campaign.isNullOrEmpty()) {
                        AppsFlyerLib.getInstance().setPreinstallAttribution(mediaSource, campaign, siteId)
                    }
                }
            }
        }
        if (!devKey.isNullOrEmpty()) {
            appsFlyerDevKey = devKey
        }

        appsFlyerDevKey?.let {
            initAndStartAppsFlyer(it)
        } ?: run {
            Log.e(
                BuildConfig.TAG,
                "${Config.DEV_KEY} is a required key"
            )
        }
    }

    override fun trackLocation(latitude: Double, longitude: Double) {
        AppsFlyerLib.getInstance().logLocation(application, latitude, longitude)
    }

    override fun trackEvent(eventType: String, eventParameters: Map<String, Any>?) {
        AppsFlyerLib.getInstance().logEvent(application, eventType, eventParameters)
    }

    override fun setHost(host: String, hostPrefix: String?) {
        hostPrefix?.let { prefix ->
            AppsFlyerLib.getInstance().setHost(host, prefix)
        }
    }

    override fun setUserEmails(emails: List<String>, cryptType: Int) {
        val userEmails = emails.toTypedArray()
        val cryptMethod = AppsFlyerProperties.EmailsCryptType.values()[cryptType]
        AppsFlyerLib.getInstance().setUserEmails(cryptMethod, *userEmails)
    }

    override fun setCurrencyCode(currency: String) {
        AppsFlyerLib.getInstance().setCurrencyCode(currency)
    }

    override fun setCustomerId(id: String) {
        AppsFlyerLib.getInstance().setCustomerUserId(id)
    }

    override fun disableDeviceTracking(disable: Boolean) {
        // This method should disable device tracking, not anonymize user
        AppsFlyerLib.getInstance().setDisableAdvertisingIdentifiers(disable)
    }

    override fun resolveDeepLinkUrls(links: List<String>) {
        val urlLinks = links.toTypedArray()
        AppsFlyerLib.getInstance().setResolveDeepLinkURLs(*urlLinks)
    }

    override fun stopTracking(isTrackingStopped: Boolean) {
        AppsFlyerLib.getInstance().stop(isTrackingStopped, application.applicationContext)
    }

    override fun anonymizeUser(shouldAnonymize: Boolean) {
        AppsFlyerLib.getInstance().anonymizeUser(shouldAnonymize)
    }

    override fun logAdRevenue(
        monetizationNetwork: String,
        mediationNetwork: String,
        revenue: Double,
        currency: String,
        additionalParams: Map<String, Any>?
    ) {
        try {
            val mediationNetworkEnum = MediationNetwork.fromString(mediationNetwork)?.toAppsFlyerEnum()

            if (mediationNetworkEnum == null) {
                Log.e(BuildConfig.TAG, "Unknown mediation network: $mediationNetwork.")
                return
            }

            val adRevenueData = AFAdRevenueData(
                monetizationNetwork,
                mediationNetworkEnum,
                currency,
                revenue
            )

            AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParams)
        } catch (e: Exception) {
            Log.e(BuildConfig.TAG, "Error logging ad revenue: ${e.message}")
        }
    }

    override fun setDMAConsent(
        gdprApplies: Boolean,
        consentForDataUsage: Boolean,
        consentForAdsPersonalization: Boolean,
        consentForAdStorage: Boolean
    ) {
        val consent = AppsFlyerConsent(gdprApplies, consentForDataUsage, consentForAdsPersonalization, consentForAdStorage)
        AppsFlyerLib.getInstance().setConsentData(consent)
    }

    override fun setPhoneNumber(phoneNumber: String) {
        AppsFlyerLib.getInstance().setPhoneNumber(phoneNumber)
    }

    override fun addPushNotificationDeepLinkPath(path: List<String>) {
        val pathArray = path.toTypedArray()
        AppsFlyerLib.getInstance().addPushNotificationDeepLinkPath(*pathArray)
    }

    override fun validateAndLogPurchase(
        purchaseType: String,
        purchaseToken: String,
        productId: String,
        price: String,
        currency: String,
        additionalParams: Map<String, String>?
    ) {
        try {
            val afPurchaseType = if (purchaseType.equals("subscription", true)) {
                AFPurchaseType.SUBSCRIPTION
            } else {
                AFPurchaseType.ONE_TIME_PURCHASE
            }

            val purchaseDetails = AFPurchaseDetails(
                afPurchaseType,
                purchaseToken,
                productId,
                price,
                currency
            )

            AppsFlyerLib.getInstance().validateAndLogInAppPurchase(
                purchaseDetails,
                additionalParams,
                null // callback can be added if needed
            )
        } catch (e: Exception) {
            Log.e(BuildConfig.TAG, "Error validating purchase: ${e.message}")
        }
    }

    override fun setSharingFilterForPartners(partners: List<String>) {
        val partnersArray = partners.toTypedArray()
        AppsFlyerLib.getInstance().setSharingFilterForPartners(*partnersArray)
    }

    override fun appendCustomData(customData: Map<String, Any>) {
        AppsFlyerLib.getInstance().setAdditionalData(customData.toMutableMap())
    }

    override fun setDeviceLanguage(language: String) {
        // Android doesn't have this specific method like iOS
        // Language is typically handled automatically by the system
        Log.d(BuildConfig.TAG, "Device language setting: $language (handled automatically on Android)")
    }

    override fun setPartnerData(partnerId: String, partnerData: Map<String, Any>) {
        AppsFlyerLib.getInstance().setPartnerData(partnerId, partnerData)
    }

    override fun appendParametersToDeepLinkUrl(urlContains: String, parameters: Map<String, String>) {
        AppsFlyerLib.getInstance().appendParametersToDeepLinkingURL(urlContains, parameters)
    }

    // Private helper methods
    private fun setMinsBetweenSessions(seconds: Int) {
        AppsFlyerLib.getInstance().setMinTimeBetweenSessions(seconds)
    }

    private fun addCustomData(data: HashMap<String, Any>) {
        AppsFlyerLib.getInstance().setAdditionalData(data)
    }

    private fun enableDebugLog(shouldEnable: Boolean) {
        AppsFlyerLib.getInstance().setDebugLog(shouldEnable)
    }

    private fun toMap(json: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        try {
            json.keys().forEach { key ->
                (json[key] as? String)?.let { value ->
                    map[key] = value
                }
            }
        } catch (ex: JSONException) {
            Log.e("AppsFlyerTracker", "Error in JSON Config")
        }

        return map.toMap()
    }

    private fun initAndStartAppsFlyer(devKey: String) {
        AppsFlyerLib.getInstance()
            .init(
                devKey,
                createConversionListener(),
                application.applicationContext
            )
        AppsFlyerLib.getInstance()
            .start(weakActivity?.get() ?: application.applicationContext)
    }

    private fun getApplication() {
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(p0: Activity) = Unit

            override fun onActivityStarted(p0: Activity) = Unit

            override fun onActivityDestroyed(p0: Activity) = Unit

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) = Unit

            override fun onActivityStopped(p0: Activity) = Unit

            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                weakActivity = WeakReference(p0)
            }

            override fun onActivityResumed(p0: Activity) = Unit
        })
    }

    private fun createConversionListener(): AppsFlyerConversionListener {
        return object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>) {

                if (conversionData.containsKey(Tracking.GCD_IS_FIRST_LAUNCH) &&
                    (conversionData[Tracking.GCD_IS_FIRST_LAUNCH] as Boolean)
                ) {
                    remoteCommandContext.track("conversion_data_received", conversionData.toMap())
                }
            }

            override fun onConversionDataFail(errorMessage: String) {
                val map = HashMap<String, Any>()
                map["error_name"] = "conversion_data_request_failure"
                map["error_message"] = errorMessage

                remoteCommandContext.track("appsflyer_error", map)
            }

            override fun onAppOpenAttribution(attributionData: MutableMap<String, String>?) {
                remoteCommandContext.track(
                    "app_open_attribution",
                    attributionData as Map<String, Any>?
                )
            }

            override fun onAttributionFailure(errorMessage: String) {
                val map = HashMap<String, Any>()
                map["error_name"] = "app_open_attribution_failure"
                map["error_message"] = errorMessage

                remoteCommandContext.track("appsflyer_error", map)
            }
        }
    }

    // Implementation of new Android SDK functions
    override fun setIsUpdate(isUpdate: Boolean) {
        AppsFlyerLib.getInstance().setIsUpdate(isUpdate)
    }

    override fun setOutOfStore(outOfStore: String) {
        AppsFlyerLib.getInstance().setOutOfStore(outOfStore)
    }

    override fun setPreinstallAttribution(mediaSource: String, campaign: String, siteId: String?) {
        AppsFlyerLib.getInstance().setPreinstallAttribution(mediaSource, campaign, siteId)
    }

    override fun setAndroidIdData(androidId: String) {
        AppsFlyerLib.getInstance().setAndroidIdData(androidId)
    }

    override fun setImeiData(imei: String) {
        AppsFlyerLib.getInstance().setImeiData(imei)
    }

    override fun setOaidData(oaid: String) {
        AppsFlyerLib.getInstance().setOaidData(oaid)
    }
}