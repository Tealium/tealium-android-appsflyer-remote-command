package com.tealium.remotecommands.appsflyer

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerConsent
import com.appsflyer.AFAdRevenueData
import com.appsflyer.MediationNetwork
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
        if (host.isNotEmpty()) {
            val prefix = if (hostPrefix.isNullOrEmpty()) "" else hostPrefix
            AppsFlyerLib.getInstance().setHost(host, prefix)
        } else {
            Log.w(BuildConfig.TAG, "setHost: host parameter cannot be empty")
        }
    }

    override fun setUserEmails(emails: List<String>) {
        val userEmails = emails.toTypedArray()
        AppsFlyerLib.getInstance().setUserEmails(*userEmails)
    }

    override fun setCurrencyCode(currency: String) {
        AppsFlyerLib.getInstance().setCurrencyCode(currency)
    }

    override fun setCustomerId(id: String) {
        AppsFlyerLib.getInstance().setCustomerUserId(id)
    }

    override fun disableDeviceTracking(disable: Boolean) {
        AppsFlyerLib.getInstance().anonymizeUser(disable)
    }

    override fun resolveDeepLinkUrls(links: List<String>) {
        val urlLinks = links.toTypedArray()
        AppsFlyerLib.getInstance().setResolveDeepLinkURLs(*urlLinks)
    }

    override fun stopTracking(isTrackingStopped: Boolean) {
        AppsFlyerLib.getInstance().stop(isTrackingStopped, application.applicationContext)
    }

    override fun setDisableNetworkData(disable: Boolean) {
        AppsFlyerLib.getInstance().setDisableNetworkData(disable)
    }

    override fun setDMAConsentData(consentData: Map<String, Any>) {
        val gdprApplies = consentData[DMAConsent.GDPR_APPLIES] as? Boolean ?: false
        val dataUsageConsent = if (gdprApplies) consentData[DMAConsent.CONSENT_FOR_DATA_USAGE] as? Boolean else null
        val adsPersonalizationConsent = if (gdprApplies) consentData[DMAConsent.CONSENT_FOR_ADS_PERSONALIZATION] as? Boolean else null
        val adStorageConsent = if (gdprApplies) consentData[DMAConsent.CONSENT_FOR_AD_STORAGE] as? Boolean else null
        
        val consent = AppsFlyerConsent(
            isUserSubjectToGDPR = gdprApplies,
            hasConsentForDataUsage = dataUsageConsent,
            hasConsentForAdsPersonalization = adsPersonalizationConsent,
            hasConsentForAdStorage = adStorageConsent
        )
        
        AppsFlyerLib.getInstance().setConsentData(consent)
    }

    override fun enableAppSetIdCollection(enable: Boolean) {
        if (!enable) {
            AppsFlyerLib.getInstance().disableAppSetId()
        }
    }

    override fun logAdRevenue(
        monetizationNetwork: String,
        mediationNetwork: MediationNetwork,
        revenue: Double,
        currency: String,
        additionalParameters: Map<String, Any>?
    ) {
        try {
            // Create AdRevenueData object directly with the provided MediationNetwork
            val adRevenueData = AFAdRevenueData(
                monetizationNetwork,
                mediationNetwork,
                currency,
                revenue
            )
            
            // Log the ad revenue with additional parameters if provided
            AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParameters)
        } catch (e: Exception) {
            Log.e(BuildConfig.TAG, "Error logging ad revenue: ${e.message}")
        }
    }

    private fun setMinsBetweenSessions(seconds: Int) {
        AppsFlyerLib.getInstance().setMinTimeBetweenSessions(seconds)
    }

    private fun anonymizeUser(isDisabled: Boolean) {
        AppsFlyerLib.getInstance().anonymizeUser(isDisabled)
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
}