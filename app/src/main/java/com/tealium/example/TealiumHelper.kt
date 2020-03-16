package com.tealium.example

import android.app.Application
import android.os.Build
import android.webkit.WebView
import com.tealium.library.Tealium
import com.tealium.remotecommands.appsflyer.AppsFlyerRemoteCommand

object TealiumHelper {

    lateinit var tealium: Tealium
    val instanceName = "my_tealium_instance"
    private val devKey = "<your_dev_key>"

    fun initialize(application: Application) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        val config: Tealium.Config = Tealium.Config.create(application, "tealiummobile", "appsflyer-android", "dev")
        config.forceOverrideLogLevel = "dev"
        tealium = Tealium.createInstance(instanceName, config)

        val appsFlyerRemoteCommand = AppsFlyerRemoteCommand(instanceName, application, appsFlyerDevKey = devKey)
        tealium.addRemoteCommand(appsFlyerRemoteCommand)
    }

    fun trackView(viewName: String, data: Map<String, Any>? = null) {
        Tealium.getInstance(instanceName)?.trackView(viewName, data)
    }

    fun trackEvent(tealiumEvent: String, data: Map<String, Any>? = null) {
        Tealium.getInstance(instanceName)?.trackEvent(tealiumEvent, data)
    }

}