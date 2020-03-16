package com.tealium.remotecommands.appsflyer

import android.app.Application
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.tealium.library.Tealium

class AppsFlyerTracker(
    private val application: Application,
    private val instanceName: String,
    private val devKey: String,
    configSettings: Map<String, Any>? = null
) : AppsFlyerTrackable {

    private val TAG = this::class.java.simpleName

    init {
        initialize(application, devKey, configSettings)
    }

    override fun initialize(
        application: Application,
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
                //val data: HashMap<String, Any>? = settings[Config.CUSTOM_DATA]

                addCustomData(settings[Config.CUSTOM_DATA] as HashMap<String, Any>)
            }

            if (settings.containsKey(Config.DEBUG)) {
                enableDebugLog(settings[Config.DEBUG] as Boolean)
            }
        }
        AppsFlyerLib.getInstance().init(devKey, createConversionListener(), application)
        AppsFlyerLib.getInstance().startTracking(application)
    }

    override fun trackLaunch() {
        AppsFlyerLib.getInstance().trackAppLaunch(application, devKey)
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

    override fun disableTracking(disable: Boolean) {
        AppsFlyerLib.getInstance().setDeviceTrackingDisabled(disable)
    }

    override fun resolveDeepLinkUrls(links: List<String>) {
        val urlLinks = links.toTypedArray()
        AppsFlyerLib.getInstance().setResolveDeepLinkURLs(*urlLinks)
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


    private fun createConversionListener(): AppsFlyerConversionListener {
        return object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(conversionData: MutableMap<String, Any>?) {
                val tealium: Tealium? = Tealium.getInstance(instanceName)
                tealium?.trackEvent("conversion_data_received", conversionData)
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