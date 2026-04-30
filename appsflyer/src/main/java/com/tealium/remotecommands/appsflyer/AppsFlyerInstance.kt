package com.tealium.remotecommands.appsflyer

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerConsent
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerProperties
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
        registerActivityLifecycleCallbacks()
    }

    override fun initialize(
        devKey: String?,
        configSettings: Map<String, Any>?
    ) {
        configSettings?.let { settings ->
            // Must run before init() — Facebook SDK integration for deferred app links.
            if (settings.containsKey(Settings.ENABLE_FACEBOOK_DEFERRED_APPLINKS)) {
                (settings[Settings.ENABLE_FACEBOOK_DEFERRED_APPLINKS] as? Boolean)?.let { isEnabled ->
                    AppsFlyerLib.getInstance().enableFacebookDeferredApplinks(isEnabled)
                }
            }

            if (settings.containsKey(Settings.TIME_BETWEEN_SESSIONS)) {
                (settings[Settings.TIME_BETWEEN_SESSIONS] as? Int)?.let { timeBetweenSessions ->
                    setMinsBetweenSessions(timeBetweenSessions)
                }
            }

            if (settings.containsKey(Settings.ANONYMIZE_USER)) {
                (settings[Settings.ANONYMIZE_USER] as? Boolean)?.let { shouldAnonymizeUser ->
                    this.anonymizeUser(shouldAnonymizeUser)
                }
            }

            if (settings.containsKey(Settings.CUSTOM_DATA)) {
                (settings[Settings.CUSTOM_DATA] as? JSONObject)?.let { customDataJson ->
                    addCustomData(HashMap(toMap(customDataJson)))
                }
            }

            if (settings.containsKey(Settings.DEBUG)) {
                (settings[Settings.DEBUG] as? Boolean)?.let { shouldEnableDebugLog ->
                    enableDebugLog(shouldEnableDebugLog)
                }
            }

            if (settings.containsKey(Settings.LOG_LEVEL)) {
                (settings[Settings.LOG_LEVEL] as? String)?.let { logLevel ->
                    setLogLevel(logLevel)
                }
            }

            if (settings.containsKey(Settings.PUSH_NOTIFICATION_DEEP_LINK_PATH)) {
                (settings[Settings.PUSH_NOTIFICATION_DEEP_LINK_PATH] as? List<*>)?.let { pathList ->
                    val stringPathList = pathList.filterIsInstance<String>()
                    if (stringPathList.isNotEmpty()) {
                        addPushNotificationDeepLinkPath(stringPathList)
                    }
                }
            }

            if (settings.containsKey(Settings.ENABLE_TCF_DATA_COLLECTION)) {
                (settings[Settings.ENABLE_TCF_DATA_COLLECTION] as? Boolean)?.let { isEnabled ->
                    AppsFlyerLib.getInstance().enableTCFDataCollection(isEnabled)
                }
            }

            if (settings.containsKey(Settings.ONE_LINK_CUSTOM_DOMAINS)) {
                (settings[Settings.ONE_LINK_CUSTOM_DOMAINS] as? List<*>)?.let { domainList ->
                    val domains = domainList.filterIsInstance<String>().toTypedArray()
                    AppsFlyerLib.getInstance().setOneLinkCustomDomain(*domains)
                }
            }

            val disableAdTrackingValue = (settings[Settings.DISABLE_ADVERTISING_IDENTIFIERS]
                ?: settings[Settings.DISABLE_AD_TRACKING_ALIAS]) as? Boolean
            disableAdTrackingValue?.let { isDisabled ->
                AppsFlyerLib.getInstance().setDisableAdvertisingIdentifiers(isDisabled)
            }

            if (settings.containsKey(Settings.DISABLE_APP_SET_ID)) {
                (settings[Settings.DISABLE_APP_SET_ID] as? Boolean)?.let { isDisabled ->
                    if (isDisabled) {
                        AppsFlyerLib.getInstance().disableAppSetId()
                    }
                }
            }

            if (settings.containsKey(Settings.COLLECT_ANDROID_ID)) {
                (settings[Settings.COLLECT_ANDROID_ID] as? Boolean)?.let { shouldCollect ->
                    AppsFlyerLib.getInstance().setCollectAndroidID(shouldCollect)
                }
            }

            if (settings.containsKey(Settings.COLLECT_IMEI)) {
                (settings[Settings.COLLECT_IMEI] as? Boolean)?.let { shouldCollect ->
                    AppsFlyerLib.getInstance().setCollectIMEI(shouldCollect)
                }
            }

            // Must be called before start().
            if (settings.containsKey(Settings.DEEP_LINK_PARAMETERS)) {
                (settings[Settings.DEEP_LINK_PARAMETERS] as? List<*>)?.forEach { entry ->
                    (entry as? Map<*, *>)?.let { map ->
                        val contains = map[DeepLinkParameterEntry.CONTAINS] as? String
                        @Suppress("UNCHECKED_CAST")
                        val parameters = map[DeepLinkParameterEntry.PARAMETERS] as? Map<String, String>
                        if (!contains.isNullOrEmpty() && (parameters != null)) {
                            AppsFlyerLib.getInstance().appendParametersToDeepLinkingURL(contains, parameters)
                        }
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
            RemoteCommandLogger.error("${Config.DEV_KEY} is a required key")
        }
    }

    override fun trackLocation(latitude: Double, longitude: Double) {
        AppsFlyerLib.getInstance().logLocation(application, latitude, longitude)
    }

    override fun trackEvent(eventType: String, eventParameters: Map<String, Any>?) {
        AppsFlyerLib.getInstance().logEvent(application, eventType, eventParameters)
    }

    override fun setHost(host: String, hostPrefix: String) {
        AppsFlyerLib.getInstance().setHost(hostPrefix, host)
    }

    override fun setUserEmails(emails: List<String>, cryptType: Int) {
        val emailCryptType = EmailCryptTypeMapping.fromInt(cryptType) ?: AppsFlyerProperties.EmailsCryptType.NONE
        AppsFlyerLib.getInstance().setUserEmails(emailCryptType, *emails.toTypedArray())
    }

    override fun setCurrencyCode(currency: String) {
        AppsFlyerLib.getInstance().setCurrencyCode(currency)
    }

    override fun setCustomerId(id: String) {
        AppsFlyerLib.getInstance().setCustomerUserId(id)
    }

    override fun setPhoneNumber(phoneNumber: String) {
        AppsFlyerLib.getInstance().setPhoneNumber(phoneNumber)
    }

    override fun logAdRevenue(adRevenueData: AFAdRevenueData, additionalParameters: Map<String, Any>?) {
        AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParameters)
    }

    override fun setConsentData(consent: AppsFlyerConsent) {
        AppsFlyerLib.getInstance().setConsentData(consent)
    }

    override fun setPartnerData(partnerId: String, partnerInfo: Map<String, Any>?) {
        AppsFlyerLib.getInstance().setPartnerData(partnerId, partnerInfo)
    }

    override fun setSharingFilterForPartners(partners: Array<String>?) {
        AppsFlyerLib.getInstance().setSharingFilterForPartners(*(partners ?: emptyArray()))
    }

    override fun anonymizeUser(anonymize: Boolean) {
        AppsFlyerLib.getInstance().anonymizeUser(anonymize)
    }

    override fun resolveDeepLinkUrls(links: List<String>) {
        val urlLinks = links.toTypedArray()
        AppsFlyerLib.getInstance().setResolveDeepLinkURLs(*urlLinks)
    }

    override fun start() {
        AppsFlyerLib.getInstance().start(weakActivity?.get() ?: application.applicationContext)
    }

    override fun stopTracking(isTrackingStopped: Boolean) {
        AppsFlyerLib.getInstance().stop(isTrackingStopped, application.applicationContext)
    }

    override fun addPushNotificationDeepLinkPath(deepLinkPath: List<String>) {
        val pathArray = deepLinkPath.toTypedArray()
        AppsFlyerLib.getInstance().addPushNotificationDeepLinkPath(*pathArray)
    }

    override fun logSession() {
        AppsFlyerLib.getInstance().logSession(application.applicationContext)
    }

    override fun setOaid(oaid: String) {
        AppsFlyerLib.getInstance().setOaidData(oaid)
    }

    override fun setAndroidId(androidId: String) {
        AppsFlyerLib.getInstance().setAndroidIdData(androidId)
    }

    override fun setImei(imei: String) {
        AppsFlyerLib.getInstance().setImeiData(imei)
    }

    override fun setOutOfStore(storeName: String) {
        AppsFlyerLib.getInstance().setOutOfStore(storeName)
    }

    override fun setDisableNetworkData(disable: Boolean) {
        AppsFlyerLib.getInstance().setDisableNetworkData(disable)
    }

    override fun setAppInviteOneLink(oneLinkId: String) {
        AppsFlyerLib.getInstance().setAppInviteOneLink(oneLinkId)
    }

    override fun setPreinstallAttribution(mediaSource: String, campaign: String, siteId: String) {
        AppsFlyerLib.getInstance().setPreinstallAttribution(mediaSource, campaign, siteId)
    }

    override fun setIsUpdate(isUpdate: Boolean) {
        AppsFlyerLib.getInstance().setIsUpdate(isUpdate)
    }

    private fun setLogLevel(logLevel: String) {
        val level = LogLevelMapping.fromString(logLevel) ?: run {
            RemoteCommandLogger.error(
                "Invalid log_level: '$logLevel'. Accepted values: ${LogLevelMapping.validValues.joinToString()}"
            )
            return
        }
        AppsFlyerLib.getInstance().setLogLevel(level)
    }

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
            RemoteCommandLogger.error("Error in JSON Config", ex)
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

    private fun registerActivityLifecycleCallbacks() {
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

                if (conversionData.containsKey(Tracking.GCD_IS_FIRST_LAUNCH)) {
                    (conversionData[Tracking.GCD_IS_FIRST_LAUNCH] as? Boolean)?.let { isFirstLaunch ->
                        if (isFirstLaunch) {
                            remoteCommandContext.track(
                                AttributionEvents.CONVERSION_DATA_RECEIVED,
                                conversionData.toMap()
                            )
                        }
                    }
                }
            }

            override fun onConversionDataFail(errorMessage: String) {
                val map = HashMap<String, Any>()
                map[AttributionEvents.KEY_ERROR_NAME] = AttributionEvents.ERROR_CONVERSION_DATA_REQUEST_FAILURE
                map[AttributionEvents.KEY_ERROR_MESSAGE] = errorMessage

                remoteCommandContext.track(AttributionEvents.APPSFLYER_ERROR, map)
            }

            override fun onAppOpenAttribution(attributionData: MutableMap<String, String>?) {
                remoteCommandContext.track(
                    AttributionEvents.APP_OPEN_ATTRIBUTION,
                    attributionData as Map<String, Any>?
                )
            }

            override fun onAttributionFailure(errorMessage: String) {
                val map = HashMap<String, Any>()
                map[AttributionEvents.KEY_ERROR_NAME] = AttributionEvents.ERROR_APP_OPEN_ATTRIBUTION_FAILURE
                map[AttributionEvents.KEY_ERROR_MESSAGE] = errorMessage

                remoteCommandContext.track(AttributionEvents.APPSFLYER_ERROR, map)
            }
        }
    }
}