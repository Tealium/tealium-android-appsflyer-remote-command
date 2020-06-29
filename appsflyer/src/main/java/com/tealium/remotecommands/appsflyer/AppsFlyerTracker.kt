package com.tealium.remotecommands.appsflyer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.tealium.library.Tealium
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference

class AppsFlyerTracker(
    private val application: Application,
    private val instanceName: String
) : AppsFlyerTrackable {

    private var weakActivity: WeakReference<Activity>? = null

    init {
        getApplication()
    }

    override fun initialize(
        devKey: String,
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
                        entry.value?.let { value ->
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
        AppsFlyerLib.getInstance()
            .init(devKey, createConversionListener(), application.applicationContext)
        AppsFlyerLib.getInstance().startTracking(weakActivity?.get()?: application.applicationContext)
    }

    override fun trackLocation(latitude: Double, longitude: Double) {
        AppsFlyerLib.getInstance().trackLocation(application, latitude, longitude)
    }

    override fun trackEvent(eventType: String, eventParameters: Map<String, Any>?) {
        AppsFlyerLib.getInstance().trackEvent(application, eventType, eventParameters)
    }

    override fun setHost(host: String, hostPrefix: String?) {
        AppsFlyerLib.getInstance().setHost(host, hostPrefix)
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
        AppsFlyerLib.getInstance().setDeviceTrackingDisabled(disable)
    }

    override fun resolveDeepLinkUrls(links: List<String>) {
        val urlLinks = links.toTypedArray()
        AppsFlyerLib.getInstance().setResolveDeepLinkURLs(*urlLinks)
    }

    override fun stopTracking(isTrackingStopped: Boolean) {
        AppsFlyerLib.getInstance().stopTracking(isTrackingStopped, application.applicationContext)
    }

    fun setMinsBetweenSessions(seconds: Int) {
        AppsFlyerLib.getInstance().setMinTimeBetweenSessions(seconds)
    }

    fun anonymizeUser(isDisabled: Boolean) {
        AppsFlyerLib.getInstance().setDeviceTrackingDisabled(isDisabled)
    }

    fun addCustomData(data: HashMap<String, Any>) {
        AppsFlyerLib.getInstance().setAdditionalData(data)
    }

    fun enableDebugLog(shouldEnable: Boolean) {
        AppsFlyerLib.getInstance().setDebugLog(shouldEnable)
    }

    fun toMap(json: JSONObject): Map<String, Any> {
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
            override fun onConversionDataSuccess(conversionData: MutableMap<String, Any?>) {
                val tealium: Tealium? = Tealium.getInstance(instanceName)

                if (conversionData.containsKey(Tracking.GCD_IS_FIRST_LAUNCH) &&
                    (conversionData[Tracking.GCD_IS_FIRST_LAUNCH] as Boolean)
                ) {
                    tealium?.trackEvent("conversion_data_received", conversionData)
                }
            }

            override fun onConversionDataFail(errorMessage: String) {
                val map = HashMap<String, Any>()
                map.put("error_name", "conversion_data_request_failure")
                map.put("error_message", errorMessage)

                val tealium: Tealium? = Tealium.getInstance(instanceName)
                tealium?.trackEvent("appsflyer_error", map)
            }

            override fun onAppOpenAttribution(attributionData: MutableMap<String, String>?) {
                val tealium: Tealium? = Tealium.getInstance(instanceName)
                tealium?.trackEvent("app_open_attribution", attributionData)
            }

            override fun onAttributionFailure(errorMessage: String) {
                val map = HashMap<String, Any>()
                map.put("error_name", "app_open_attribution_failure")
                map.put("error_message", errorMessage)

                val tealium: Tealium? = Tealium.getInstance(instanceName)
                tealium?.trackEvent("appsflyer_error", map)
            }
        }
    }
}