package com.tealium.remotecommands.appsflyer

/**
 * Who registers the AppsFlyer SDK's session-ready listener (`AppsFlyerLib.registerSessionReadyListener`)
 * and calls `AppsFlyerLib.start()` in it.
 *
 * A required constructor argument of [AppsFlyerRemoteCommand], not a remote setting: it has to match
 * what the app code does. The SDK holds a single `SessionReadyListener`, so the app and the remote
 * command must not both register one; a second registration replaces the first.
 *
 * "Initialized" means the `initialize` command has supplied a dev key and `AppsFlyerLib.init` has run.
 */
enum class AppsFlyerSessionMode {
    /**
     * When initialized, the remote command additionally registers the session-ready listener, which
     * calls `AppsFlyerLib.getInstance().start()`. The app must not register a listener of its own.
     *
     * The SDK calls the listener on a background thread.
     */
    AUTOMATIC,

    /**
     * When initialized, the remote command does not register a listener and never calls `start()`.
     * The app must register its own listener and call `AppsFlyerLib.getInstance().start()` in it, for
     * example only once the user has given consent:
     *
     * ```kotlin
     * AppsFlyerLib.getInstance().registerSessionReadyListener(object : SessionReadyListener {
     *     override fun onSessionReady() {
     *         // Runs on an SDK background thread.
     *         AppsFlyerLib.getInstance().start()
     *     }
     * })
     * ```
     *
     * After `stoptracking` with `stop_tracking = false`, the remote command only calls `stop(false)`;
     * the app restarts the session itself.
     */
    APP_MANAGED
}
