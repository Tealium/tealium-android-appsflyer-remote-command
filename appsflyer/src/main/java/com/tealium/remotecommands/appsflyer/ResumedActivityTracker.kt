package com.tealium.remotecommands.appsflyer

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * Tracks the most recently resumed Activity, because `AppsFlyerLib.init` only treats the app as foregrounded when it
 * receives an Activity. [AppsFlyerRemoteCommand] registers it when it is constructed: Tealium creates the
 * [AppsFlyerInstance] later on its bootstrap thread, often after the launcher Activity has already resumed.
 */
internal class ResumedActivityTracker : Application.ActivityLifecycleCallbacks {

    // Written on the main thread, read on the command thread.
    @Volatile
    private var weakActivity: WeakReference<Activity>? = null

    val currentActivity: Activity?
        get() = weakActivity?.get()

    override fun onActivityResumed(activity: Activity) {
        weakActivity = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) = clear(activity)
    override fun onActivityDestroyed(activity: Activity) = clear(activity)
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    // A finished Activity can be destroyed after the next one has resumed, so only the current one clears it.
    private fun clear(activity: Activity) {
        if (weakActivity?.get() === activity) weakActivity = null
    }
}
