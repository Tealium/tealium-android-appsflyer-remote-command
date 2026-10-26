package com.tealium.demo

import android.app.Application
import android.webkit.WebView
import com.appsflyer.AppsFlyerLib
import com.tealium.core.*
import com.tealium.dispatcher.TealiumEvent
import com.tealium.dispatcher.TealiumView
import com.tealium.lifecycle.Lifecycle
import com.tealium.remotecommanddispatcher.RemoteCommands
import com.tealium.remotecommanddispatcher.remoteCommands
import com.tealium.remotecommands.appsflyer.AppsFlyerRemoteCommand
import com.tealium.remotecommands.appsflyer.AppsFlyerSessionMode
import com.tealium.tagmanagementdispatcher.TagManagement

object TealiumHelper {

    lateinit var tealium: Tealium
    val instanceName = "my_tealium_instance"

    fun initialize(application: Application) {

        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        val config = TealiumConfig(
            application,
            "tealiummobile",
            "appsflyer-tag",
            Environment.DEV,
            modules = mutableSetOf(Modules.Lifecycle),
            dispatchers = mutableSetOf(Dispatchers.RemoteCommands, Dispatchers.TagManagement)
        ).apply {
            useRemoteLibrarySettings = true
        }

        // AUTOMATIC: the command registers the AppsFlyer session-ready listener and starts the session
        // once the `initialize` command has run. Use APP_MANAGED to register your own listener and call
        // start() yourself; see the README.
        // Constructed before Tealium.create so it sees the launcher Activity resume.
        val appsFlyerRemoteCommand = AppsFlyerRemoteCommand(
            application,
            AppsFlyerSessionMode.AUTOMATIC
        )
        // Opt-in app-open attribution. The SDK drops deep links that arrive before a listener is subscribed.
        AppsFlyerLib.getInstance().subscribeForDeepLink(appsFlyerRemoteCommand)

        tealium = Tealium.create(instanceName, config) {
            // Remote Command Tag - requires TiQ
//            remoteCommands?.add(appsFlyerRemoteCommand)

            // JSON Remote Command - requires local filename
            remoteCommands?.add(appsFlyerRemoteCommand, filename = "appsflyer.json")

            // JSON Remote Command - requires url to hosted json file
//            remoteCommands?.add(appsFlyerRemoteCommand, remoteUrl = "appsflyer.json")
        }
    }

    fun trackView(viewName: String, data: Map<String, Any>? = null) {
        tealium.track(TealiumView(viewName, data))
    }

    fun trackEvent(tealiumEvent: String, data: Map<String, Any>? = null) {
        tealium.track(TealiumEvent(tealiumEvent, data))
    }
}