package com.tealium.remotecommands.appsflyer

import android.util.Log

/**
 * Centralized logging utility for the AppsFlyer Remote Command.
 * Log verbosity is controlled via [logLevel].
 */
internal object RemoteCommandLogger {
    private const val TAG = "TealiumAppsFlyer"
    var logLevel: RemoteCommandLogLevel = RemoteCommandLogLevel.SILENT

    fun debug(message: String) {
        if (logLevel <= RemoteCommandLogLevel.DEBUG) Log.d(TAG, message)
    }

    fun info(message: String) {
        if (logLevel <= RemoteCommandLogLevel.INFO) Log.i(TAG, message)
    }

    fun warning(message: String) {
        if (logLevel <= RemoteCommandLogLevel.WARNING) Log.w(TAG, message)
    }

    fun error(message: String) {
        if (logLevel <= RemoteCommandLogLevel.ERROR) Log.e(TAG, message)
    }

    fun error(message: String, throwable: Throwable) {
        if (logLevel <= RemoteCommandLogLevel.ERROR) Log.e(TAG, message, throwable)
    }
}
