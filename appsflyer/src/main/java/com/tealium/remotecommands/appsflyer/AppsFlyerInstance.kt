package com.tealium.remotecommands.appsflyer

import android.app.Activity
import android.app.Application
import com.appsflyer.AppsFlyerLib
import com.appsflyer.share.AFAdRevenueData
import com.appsflyer.share.AppsFlyerConsent
import com.appsflyer.share.AppsFlyerConversionListener
import com.appsflyer.share.SessionReadyListener
import com.appsflyer.share.deeplink.DeepLinkResult
import com.tealium.remotecommands.RemoteCommandContext

class AppsFlyerInstance internal constructor(
    private val application: Application,
    private val sessionMode: AppsFlyerSessionMode,
    private var appsFlyerDevKey: String? = null,
    private val remoteCommandContext: RemoteCommandContext,
    private val logger: RemoteCommandLogger = RemoteCommandLogger(),
    private val currentActivity: () -> Activity? = { null }
) : AppsFlyerCommand {

    private enum class Readiness { PENDING, FLUSHING, READY }

    // Commands and SDK callbacks may arrive on different threads, so readiness and the queue share a lock.
    private val lock = Any()
    private var readiness = Readiness.PENDING
    private val pendingCalls = mutableListOf<() -> Unit>()

    override fun initialize(
        devKey: String?,
        configSettings: Map<String, Any>?
    ) {
        configSettings?.let { settings ->
            // AppsFlyer recommends setting debug logging before any other SDK call.
            if (settings.containsKey(Settings.DEBUG)) {
                booleanSetting(settings, Settings.DEBUG)?.let { shouldEnableDebugLog ->
                    enableDebugLog(shouldEnableDebugLog)
                }
            }

            if (settings.containsKey(Settings.LOG_LEVEL)) {
                (settings[Settings.LOG_LEVEL] as? String)?.let { logLevel ->
                    setLogLevel(logLevel)
                }
            }

            // Must run before init() — Facebook SDK integration for deferred app links.
            if (settings.containsKey(Settings.ENABLE_FACEBOOK_DEFERRED_APPLINKS)) {
                booleanSetting(settings, Settings.ENABLE_FACEBOOK_DEFERRED_APPLINKS)?.let { isEnabled ->
                    AppsFlyerLib.getInstance().enableFacebookDeferredApplinks(isEnabled)
                }
            }

            // init() starts processing the launch intent asynchronously, so the deep link settings must be in
            // place before it. The SDK keeps them on its deep link manager, which init() does not reset.
            if (settings.containsKey(Settings.ONE_LINK_CUSTOM_DOMAINS)) {
                (settings[Settings.ONE_LINK_CUSTOM_DOMAINS] as? List<*>)?.let { domainList ->
                    val domains = domainList.filterIsInstance<String>().toTypedArray()
                    AppsFlyerLib.getInstance().setOneLinkCustomDomain(*domains)
                }
            }

            if (settings.containsKey(Settings.DEEP_LINK_PARAMETERS)) {
                (settings[Settings.DEEP_LINK_PARAMETERS] as? List<*>)?.forEach { entry ->
                    (entry as? Map<*, *>)?.let { map ->
                        val contains = map[DeepLinkParameterEntry.CONTAINS] as? String
                        val rawParameters = map[DeepLinkParameterEntry.PARAMETERS] as? Map<*, *>
                        // The SDK expects Map<String, String>, so an entry with any other type is skipped.
                        val parameters = rawParameters?.takeIf { raw ->
                            raw.all { (key, value) -> key is String && value is String }
                        }
                        if (rawParameters != null && parameters == null) {
                            logger.warning("Skipping ${Settings.DEEP_LINK_PARAMETERS} entry: ${DeepLinkParameterEntry.PARAMETERS} must contain only String keys and values.")
                        }
                        if (!contains.isNullOrEmpty() && (parameters != null)) {
                            @Suppress("UNCHECKED_CAST")
                            AppsFlyerLib.getInstance().appendParametersToDeepLinkingURL(contains, parameters as Map<String, String>)
                        }
                    }
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

            if (settings.containsKey(Settings.DEEP_LINK_TIMEOUT)) {
                nonNegativeIntSetting(settings, Settings.DEEP_LINK_TIMEOUT)?.let { timeout ->
                    AppsFlyerLib.getInstance().setDeepLinkTimeout(timeout.toLong())
                }
            }

            // In APP_MANAGED mode the app's own session-ready listener may call start() on an SDK background thread
            // as soon as init() runs, so every setting that affects the first session must be applied before it.
            if (settings.containsKey(Settings.TIME_BETWEEN_SESSIONS)) {
                nonNegativeIntSetting(settings, Settings.TIME_BETWEEN_SESSIONS)?.let { timeBetweenSessions ->
                    setMinsBetweenSessions(timeBetweenSessions)
                }
            }

            if (settings.containsKey(Settings.ANONYMIZE_USER)) {
                booleanSetting(settings, Settings.ANONYMIZE_USER)?.let { shouldAnonymizeUser ->
                    this.anonymizeUser(shouldAnonymizeUser)
                }
            }

            if (settings.containsKey(Settings.CUSTOM_DATA)) {
                // AppsFlyerRemoteCommand deep-converts nested JSON objects, so custom_data arrives as a Map.
                @Suppress("UNCHECKED_CAST")
                val customData = settings[Settings.CUSTOM_DATA] as? Map<String, Any>
                customData?.let { addCustomData(it) }
            }

            if (settings.containsKey(Settings.ENABLE_TCF_DATA_COLLECTION)) {
                booleanSetting(settings, Settings.ENABLE_TCF_DATA_COLLECTION)?.let { isEnabled ->
                    AppsFlyerLib.getInstance().enableTCFDataCollection(isEnabled)
                }
            }

            val disableAdTrackingKey = if (settings.containsKey(Settings.DISABLE_ADVERTISING_IDENTIFIERS)) {
                Settings.DISABLE_ADVERTISING_IDENTIFIERS
            } else {
                Settings.DISABLE_AD_TRACKING_ALIAS
            }
            booleanSetting(settings, disableAdTrackingKey)?.let { isDisabled ->
                AppsFlyerLib.getInstance().setDisableAdvertisingIdentifiers(isDisabled)
            }

            if (settings.containsKey(Settings.DISABLE_APP_SET_ID)) {
                booleanSetting(settings, Settings.DISABLE_APP_SET_ID)?.let { isDisabled ->
                    if (isDisabled) {
                        AppsFlyerLib.getInstance().disableAppSetId()
                    }
                }
            }

            if (settings.containsKey(Settings.COLLECT_ANDROID_ID)) {
                booleanSetting(settings, Settings.COLLECT_ANDROID_ID)?.let { shouldCollect ->
                    AppsFlyerLib.getInstance().setCollectAndroidID(shouldCollect)
                }
            }
        }

        devKey?.takeIf { it.isNotBlank() }?.let { appsFlyerDevKey = it }
        val validDevKey = appsFlyerDevKey?.takeIf { it.isNotBlank() }
        if (validDevKey != null) {
            if (isReady()) {
                logger.warning("AppsFlyer is already initialized. Initializing again with the configured dev key; initialize should only be called once.")
            }
            // init() only treats the app as foregrounded when it receives an Activity, otherwise the first
            // session waits for the next foreground.
            AppsFlyerLib.getInstance().init(validDevKey, createConversionListener(), currentActivity() ?: application)
        } else {
            logger.error("${Config.DEV_KEY} is a required key")
        }

        if (validDevKey == null) return
        markReady()
        if (sessionMode == AppsFlyerSessionMode.AUTOMATIC) {
            registerSessionReadyListener()
        }
    }

    override fun trackLocation(latitude: Double, longitude: Double) {
        whenReady { AppsFlyerLib.getInstance().logLocation(application, latitude, longitude) }
    }

    override fun trackEvent(eventType: String, eventParameters: Map<String, Any>?) {
        whenReady { AppsFlyerLib.getInstance().logEvent(application, eventType, eventParameters) }
    }

    override fun setHost(host: String, hostPrefix: String) {
        AppsFlyerLib.getInstance().setHost(hostPrefix, host)
    }

    override fun setUserEmail(email: String) {
        AppsFlyerLib.getInstance().setUserEmail(email)
    }

    override fun setUserFirstName(firstName: String) {
        AppsFlyerLib.getInstance().setUserFirstName(firstName)
    }

    override fun setUserLastName(lastName: String) {
        AppsFlyerLib.getInstance().setUserLastName(lastName)
    }

    override fun setUserFbLoginId(fbLoginId: Long) {
        AppsFlyerLib.getInstance().setUserFbLoginId(fbLoginId)
    }

    override fun clearUserPii() {
        AppsFlyerLib.getInstance().clearUserPii()
    }

    override fun setCurrencyCode(currency: String) {
        AppsFlyerLib.getInstance().setCurrencyCode(currency)
    }

    override fun setCustomerId(id: String) {
        AppsFlyerLib.getInstance().setCustomerUserId(id)
    }

    override fun setUserPhone(countryCode: String, phoneNumber: String) {
        AppsFlyerLib.getInstance().setUserPhone(countryCode, phoneNumber)
    }

    override fun logAdRevenue(adRevenueData: AFAdRevenueData, additionalParameters: Map<String, Any>?) {
        whenReady { AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParameters) }
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

    override fun stopTracking(isTrackingStopped: Boolean) {
        AppsFlyerLib.getInstance().stop(isTrackingStopped, application.applicationContext)
        // The SDK restarts tracking with stop(false) followed by start(). In APP_MANAGED mode the app restarts it.
        if (!isTrackingStopped && (sessionMode == AppsFlyerSessionMode.AUTOMATIC) && isReady()) {
            AppsFlyerLib.getInstance().start()
        }
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

    // Called on an SDK background thread, like the conversion listener.
    override fun onDeepLinking(result: DeepLinkResult) {
        when (result.status) {
            DeepLinkResult.Status.FOUND -> {
                val deepLink = result.deepLink
                // A deferred link resolves on first install, which conversion_data_received already reports.
                // SDK 6 never fired onAppOpenAttribution for it, so tracking it here would double-report the install.
                if (deepLink?.isDeferred() == true) {
                    logger.debug("Deferred deep link resolved; reported through conversion data instead.")
                    return
                }
                remoteCommandContext.track(
                    AttributionEvents.APP_OPEN_ATTRIBUTION,
                    deepLink?.clickEvent.toMap(dropNulls = true)
                )
            }
            DeepLinkResult.Status.ERROR -> {
                val map = HashMap<String, Any>()
                map[AttributionEvents.KEY_ERROR_NAME] = AttributionEvents.ERROR_APP_OPEN_ATTRIBUTION_FAILURE
                map[AttributionEvents.KEY_ERROR_MESSAGE] = result.error?.name ?: ""

                remoteCommandContext.track(AttributionEvents.APPSFLYER_ERROR, map)
            }
            DeepLinkResult.Status.NOT_FOUND -> logger.debug("No deep link found.")
            // The status is a Java platform type, and a later SDK 7 release may add statuses.
            else -> Unit
        }
    }

    // An absent key yields null silently. An invalid value is ignored with a warning rather than treated as false,
    // so a malformed privacy setting cannot silently flip.
    private fun booleanSetting(settings: Map<String, Any>, key: String): Boolean? {
        val value = settings[key] ?: return null
        return toBooleanValue(value) ?: run {
            logger.warning("Ignoring invalid $key: expected a Boolean or \"true\"/\"false\", got $value")
            null
        }
    }

    // Webview data layers send numbers as Strings, so a numeric String is accepted like a Boolean String is.
    // An absent key yields null silently; an invalid or negative value is ignored with a warning.
    private fun nonNegativeIntSetting(settings: Map<String, Any>, key: String): Int? {
        val value = settings[key] ?: return null
        val number = when (value) {
            is Int -> value
            is String -> value.toIntOrNull()
            else -> null
        } ?: run {
            logger.warning("Ignoring invalid $key: expected a whole number or a numeric String, got $value")
            return null
        }
        if (number < 0) {
            logger.warning("Ignoring negative $key: $number")
            return null
        }
        return number
    }

    private fun setLogLevel(logLevel: String) {
        val level = LogLevelMapping.fromString(logLevel) ?: run {
            logger.error(
                "Invalid log_level: '$logLevel'. Accepted values: ${LogLevelMapping.validValues.joinToString()}"
            )
            return
        }
        AppsFlyerLib.getInstance().setLogLevel(level)
    }

    private fun setMinsBetweenSessions(seconds: Int) {
        AppsFlyerLib.getInstance().setMinTimeBetweenSessions(seconds)
    }

    private fun addCustomData(data: Map<String, Any>) {
        AppsFlyerLib.getInstance().setAdditionalData(data)
    }

    private fun enableDebugLog(shouldEnable: Boolean) {
        AppsFlyerLib.getInstance().setDebugLog(shouldEnable)
    }

    private fun isReady(): Boolean = synchronized(lock) { readiness != Readiness.PENDING }

    // The SDK has no isInitialized getter, so calls that need credentials wait for our own init().
    private fun whenReady(call: () -> Unit) {
        synchronized(lock) {
            if (readiness != Readiness.READY) {
                pendingCalls.add(call)
                logger.debug("Command queued until AppsFlyer is initialized.")
                return
            }
        }
        call()
    }

    // Flushes in batches so no lock is held during SDK calls. Calls that arrive during the flush are
    // queued behind it, which keeps arrival order until the queue is empty and the state becomes READY.
    private fun markReady() {
        synchronized(lock) {
            if (readiness != Readiness.PENDING) return
            readiness = Readiness.FLUSHING
        }
        while (true) {
            val batch = synchronized(lock) {
                if (pendingCalls.isEmpty()) {
                    readiness = Readiness.READY
                    return
                }
                pendingCalls.toList().also { pendingCalls.clear() }
            }
            batch.forEach { call ->
                try {
                    call()
                } catch (e: Exception) {
                    // One failing call must not leave the queue stuck in FLUSHING.
                    logger.error("Queued AppsFlyer call failed: ${e.message}", e)
                }
            }
        }
    }

    // Registers even if the session is already ready: on Android that state turns true once the SDK has seen a
    // foreground, before any listener fired. A late registration still fires once, and the SDK skips a duplicate start().
    private fun registerSessionReadyListener() {
        logger.info("Registering the AppsFlyer session-ready listener; this replaces any listener the app registered itself. Use APP_MANAGED to keep your own.")
        AppsFlyerLib.getInstance().registerSessionReadyListener(object : SessionReadyListener {
            // Called on an SDK background thread.
            override fun onSessionReady() {
                AppsFlyerLib.getInstance().start()
            }
        })
    }

    private fun createConversionListener(): AppsFlyerConversionListener {
        return object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: MutableMap<String, Any?>) {

                if (conversionData.containsKey(Tracking.GCD_IS_FIRST_LAUNCH)) {
                    (conversionData[Tracking.GCD_IS_FIRST_LAUNCH] as? Boolean)?.let { isFirstLaunch ->
                        if (isFirstLaunch) {
                            // The SDK documents that values can be null, which the tracking call cannot carry.
                            val data = buildMap<String, Any> {
                                conversionData.forEach { (key, value) -> if (value != null) put(key, value) }
                            }
                            remoteCommandContext.track(AttributionEvents.CONVERSION_DATA_RECEIVED, data)
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
        }
    }
}