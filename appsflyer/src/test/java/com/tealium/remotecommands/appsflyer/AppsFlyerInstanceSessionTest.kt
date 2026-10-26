package com.tealium.remotecommands.appsflyer

import android.app.Activity
import android.app.Application
import com.appsflyer.AFLogger
import com.appsflyer.AppsFlyerLib
import com.appsflyer.share.AFAdRevenueData
import com.appsflyer.share.AppsFlyerConversionListener
import com.appsflyer.share.SessionReadyListener
import com.tealium.remotecommands.RemoteCommandContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Verifies session handling, the readiness queue and the conversion listener of the real [AppsFlyerInstance]
 * against a mocked [AppsFlyerLib] singleton.
 */
@RunWith(RobolectricTestRunner::class)
class AppsFlyerInstanceSessionTest {

    private val appsFlyerLib = mockk<AppsFlyerLib>(relaxed = true)
    private val application = mockk<Application>(relaxed = true)
    private val logger = mockk<RemoteCommandLogger>(relaxed = true)
    private val context = mockk<RemoteCommandContext>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(AppsFlyerLib::class)
        every { AppsFlyerLib.getInstance() } returns appsFlyerLib
    }

    @After
    fun tearDown() {
        unmockkStatic(AppsFlyerLib::class)
    }

    private fun instance(
        sessionMode: AppsFlyerSessionMode = AppsFlyerSessionMode.AUTOMATIC,
        devKey: String? = null
    ) = AppsFlyerInstance(application, sessionMode, devKey, context, logger)

    // The command registers the callbacks when it is constructed, so an Activity can resume before setContext.
    private fun lifecycleCallbacks(): Application.ActivityLifecycleCallbacks {
        val callbacks = slot<Application.ActivityLifecycleCallbacks>()
        verify(exactly = 1) { application.registerActivityLifecycleCallbacks(capture(callbacks)) }
        return callbacks.captured
    }

    @Test
    fun initialize_activityResumedBeforeSetContext_passesActivityToInit() {
        val activity = mockk<Activity>()
        val command = AppsFlyerRemoteCommand(application, AppsFlyerSessionMode.AUTOMATIC)
        lifecycleCallbacks().onActivityResumed(activity)
        command.setContext(context)

        command.appsFlyerInstance.initialize("key", null)

        verify(exactly = 1) { appsFlyerLib.init("key", any(), activity) }
    }

    @Test
    fun initialize_withoutResumedActivity_passesApplicationToInit() {
        val command = AppsFlyerRemoteCommand(application, AppsFlyerSessionMode.AUTOMATIC)
        command.setContext(context)

        command.appsFlyerInstance.initialize("key", null)

        verify(exactly = 1) { appsFlyerLib.init("key", any(), application) }
    }

    @Test
    fun initialize_afterPause_clearsActivityOnlyIfSame() {
        val resumed = mockk<Activity>()
        val command = AppsFlyerRemoteCommand(application, AppsFlyerSessionMode.AUTOMATIC)
        command.setContext(context)
        val callbacks = lifecycleCallbacks()
        callbacks.onActivityResumed(resumed)

        callbacks.onActivityPaused(mockk())
        command.appsFlyerInstance.initialize("key", null)
        verify(exactly = 1) { appsFlyerLib.init("key", any(), resumed) }

        callbacks.onActivityPaused(resumed)
        command.appsFlyerInstance.initialize("key", null)
        verify(exactly = 1) { appsFlyerLib.init("key", any(), application) }
    }

    @Test
    fun automatic_initialize_registersListenerThatStarts() {
        instance().initialize("key", null)

        verify(exactly = 1) { appsFlyerLib.init("key", any(), any()) }
        verify(exactly = 0) { appsFlyerLib.start() }
        val listener = slot<SessionReadyListener>()
        verify(exactly = 1) { appsFlyerLib.registerSessionReadyListener(capture(listener)) }

        listener.captured.onSessionReady()

        verify(exactly = 1) { appsFlyerLib.start() }
    }

    @Test
    fun automatic_sessionAlreadyReady_stillRegistersListenerThatStarts() {
        every { appsFlyerLib.isSessionReady() } returns true

        instance().initialize("key", null)

        val listener = slot<SessionReadyListener>()
        verify(exactly = 1) { appsFlyerLib.registerSessionReadyListener(capture(listener)) }
        listener.captured.onSessionReady()
        verify(exactly = 1) { appsFlyerLib.start() }
        verify(exactly = 0) { logger.error(any()) }
    }

    @Test
    fun appManaged_initialize_neitherRegistersNorStarts() {
        instance(AppsFlyerSessionMode.APP_MANAGED).initialize("key", null)

        verify(exactly = 1) { appsFlyerLib.init("key", any(), any()) }
        verify(exactly = 0) { appsFlyerLib.registerSessionReadyListener(any()) }
        verify(exactly = 0) { appsFlyerLib.start() }
    }

    @Test
    fun initialize_constructorDevKey_isUsedWhenPayloadKeyIsBlank() {
        instance(devKey = "constructorKey").initialize(" ", null)

        verify(exactly = 1) { appsFlyerLib.init("constructorKey", any(), any()) }
    }

    @Test
    fun initialize_missingOrBlankDevKey_skipsInitRegistrationAndFlush() {
        listOf(null, "", "  ").forEach { key ->
            val instance = instance(devKey = key)
            instance.trackEvent("queued", null)

            instance.initialize(key, null)
        }

        verify(exactly = 0) { appsFlyerLib.init(any(), any(), any()) }
        verify(exactly = 0) { appsFlyerLib.registerSessionReadyListener(any()) }
        verify(exactly = 0) { appsFlyerLib.logEvent(any(), any(), any()) }
        verify(exactly = 3) { logger.error(match { it.contains(Config.DEV_KEY) }) }
    }

    @Test
    fun eventsBeforeInitialize_areQueuedThenFlushedInOrderBeforeRegistration() {
        val adRevenueData = mockk<AFAdRevenueData>()
        val instance = instance()
        instance.trackEvent("first", mapOf("a" to 1))
        instance.trackLocation(1.0, 2.0)
        instance.logAdRevenue(adRevenueData, null)
        instance.trackEvent("last", null)

        verify(exactly = 0) { appsFlyerLib.logEvent(any(), any(), any()) }
        verify(exactly = 0) { appsFlyerLib.logLocation(any(), any(), any()) }
        verify(exactly = 0) { appsFlyerLib.logAdRevenue(any(), any()) }

        instance.initialize("key", null)

        verifyOrder {
            appsFlyerLib.init("key", any(), any())
            appsFlyerLib.logEvent(application, "first", mapOf("a" to 1))
            appsFlyerLib.logLocation(application, 1.0, 2.0)
            appsFlyerLib.logAdRevenue(adRevenueData, null)
            appsFlyerLib.logEvent(application, "last", null)
            appsFlyerLib.registerSessionReadyListener(any())
        }
    }

    @Test
    fun eventsAfterInitialize_areSentImmediately() {
        val adRevenueData = mockk<AFAdRevenueData>()
        val instance = instance()
        instance.initialize("key", null)

        instance.trackEvent("event", null)
        instance.trackLocation(1.0, 2.0)
        instance.logAdRevenue(adRevenueData, null)

        verify(exactly = 1) { appsFlyerLib.logEvent(application, "event", null) }
        verify(exactly = 1) { appsFlyerLib.logLocation(application, 1.0, 2.0) }
        verify(exactly = 1) { appsFlyerLib.logAdRevenue(adRevenueData, null) }
    }

    @Test
    fun initializeTwice_flushesQueueOnce() {
        val instance = instance()
        instance.trackEvent("queued", null)

        instance.initialize("key", null)
        instance.initialize("key", null)

        verify(exactly = 2) { appsFlyerLib.init("key", any(), any()) }
        verify(exactly = 1) { appsFlyerLib.logEvent(application, "queued", null) }
        verify(exactly = 1) { logger.warning(match { it.contains("already initialized") }) }
    }

    @Test
    fun initialize_debugAndLogLevel_appliedBeforeInit() {
        instance().initialize(
            "key",
            mapOf(
                Settings.ANONYMIZE_USER to true,
                Settings.DEBUG to true,
                Settings.LOG_LEVEL to "debug",
                Settings.ENABLE_FACEBOOK_DEFERRED_APPLINKS to true
            )
        )

        verifyOrder {
            appsFlyerLib.setDebugLog(true)
            appsFlyerLib.setLogLevel(AFLogger.LogLevel.DEBUG)
            appsFlyerLib.enableFacebookDeferredApplinks(true)
            appsFlyerLib.anonymizeUser(true)
            appsFlyerLib.init("key", any(), any())
            appsFlyerLib.registerSessionReadyListener(any())
        }
    }

    @Test
    fun initialize_deepLinkSettings_appliedBeforeInit() {
        val parameters = mapOf("pid" to "email", "is_retargeting" to "true")
        instance().initialize(
            "key",
            mapOf(
                Settings.ANONYMIZE_USER to true,
                Settings.ONE_LINK_CUSTOM_DOMAINS to listOf("click.example.com"),
                Settings.DEEP_LINK_PARAMETERS to listOf(
                    mapOf(DeepLinkParameterEntry.CONTAINS to "promo", DeepLinkParameterEntry.PARAMETERS to parameters)
                ),
                Settings.PUSH_NOTIFICATION_DEEP_LINK_PATH to listOf("af_push_link"),
                Settings.DEEP_LINK_TIMEOUT to 1500
            )
        )

        verifyOrder {
            appsFlyerLib.setOneLinkCustomDomain("click.example.com")
            appsFlyerLib.appendParametersToDeepLinkingURL("promo", parameters)
            appsFlyerLib.addPushNotificationDeepLinkPath("af_push_link")
            appsFlyerLib.setDeepLinkTimeout(1500L)
            appsFlyerLib.anonymizeUser(true)
            appsFlyerLib.init("key", any(), any())
            appsFlyerLib.registerSessionReadyListener(any())
        }
    }

    @Test
    fun initialize_firstSessionSettings_appliedBeforeInit() {
        instance(AppsFlyerSessionMode.APP_MANAGED).initialize(
            "key",
            mapOf(
                Settings.ANONYMIZE_USER to true,
                Settings.DISABLE_ADVERTISING_IDENTIFIERS to true,
                Settings.TIME_BETWEEN_SESSIONS to 10
            )
        )

        verifyOrder {
            appsFlyerLib.setMinTimeBetweenSessions(10)
            appsFlyerLib.anonymizeUser(true)
            appsFlyerLib.setDisableAdvertisingIdentifiers(true)
            appsFlyerLib.init("key", any(), any())
        }
    }

    @Test
    fun initialize_deepLinkParametersWithNonStringValue_entryIsSkippedWithWarning() {
        val valid = mapOf("pid" to "email")
        instance().initialize(
            "key",
            mapOf(
                Settings.DEEP_LINK_PARAMETERS to listOf(
                    mapOf(DeepLinkParameterEntry.CONTAINS to "retarget", DeepLinkParameterEntry.PARAMETERS to mapOf("is_retargeting" to true)),
                    mapOf(DeepLinkParameterEntry.CONTAINS to "promo", DeepLinkParameterEntry.PARAMETERS to valid)
                )
            )
        )

        verify(exactly = 0) { appsFlyerLib.appendParametersToDeepLinkingURL("retarget", any()) }
        verify(exactly = 1) { appsFlyerLib.appendParametersToDeepLinkingURL("promo", valid) }
        verify(exactly = 1) { logger.warning(match { it.contains(Settings.DEEP_LINK_PARAMETERS) }) }
    }

    @Test
    fun initialize_numericStringSettings_areAccepted() {
        instance().initialize(
            "key",
            mapOf(Settings.DEEP_LINK_TIMEOUT to "1500", Settings.TIME_BETWEEN_SESSIONS to "30")
        )

        verify(exactly = 1) { appsFlyerLib.setDeepLinkTimeout(1500L) }
        verify(exactly = 1) { appsFlyerLib.setMinTimeBetweenSessions(30) }
        verify(exactly = 0) { logger.warning(any()) }
    }

    @Test
    fun initialize_invalidNumericSettings_ignoredWithWarning() {
        instance().initialize(
            "key",
            mapOf(Settings.DEEP_LINK_TIMEOUT to "soon", Settings.TIME_BETWEEN_SESSIONS to "1.5")
        )

        verify(exactly = 0) { appsFlyerLib.setDeepLinkTimeout(any()) }
        verify(exactly = 0) { appsFlyerLib.setMinTimeBetweenSessions(any()) }
        verify(exactly = 1) { logger.warning(match { it.contains(Settings.DEEP_LINK_TIMEOUT) }) }
        verify(exactly = 1) { logger.warning(match { it.contains(Settings.TIME_BETWEEN_SESSIONS) }) }
    }

    @Test
    fun initialize_negativeDeepLinkTimeout_droppedWithWarning() {
        instance().initialize("key", mapOf(Settings.DEEP_LINK_TIMEOUT to -1))

        verify(exactly = 0) { appsFlyerLib.setDeepLinkTimeout(any()) }
        verify(exactly = 1) { logger.warning(match { it.contains(Settings.DEEP_LINK_TIMEOUT) }) }
    }

    @Test
    fun initialize_negativeTimeBetweenSessions_droppedWithWarning() {
        instance().initialize("key", mapOf(Settings.TIME_BETWEEN_SESSIONS to -5))

        verify(exactly = 0) { appsFlyerLib.setMinTimeBetweenSessions(any()) }
        verify(exactly = 1) { logger.warning(match { it.contains(Settings.TIME_BETWEEN_SESSIONS) }) }
    }

    @Test
    fun initialize_zeroTimeBetweenSessions_isForwarded() {
        instance().initialize("key", mapOf(Settings.TIME_BETWEEN_SESSIONS to 0))

        verify(exactly = 1) { appsFlyerLib.setMinTimeBetweenSessions(0) }
    }

    @Test
    fun initialize_stringBooleanSettings_areAccepted() {
        instance().initialize(
            "key",
            mapOf(
                Settings.ANONYMIZE_USER to "true",
                Settings.DISABLE_AD_TRACKING_ALIAS to "FALSE"
            )
        )

        verify(exactly = 1) { appsFlyerLib.anonymizeUser(true) }
        verify(exactly = 1) { appsFlyerLib.setDisableAdvertisingIdentifiers(false) }
    }

    @Test
    fun initialize_invalidBooleanSetting_ignoredWithWarning() {
        instance().initialize("key", mapOf(Settings.ANONYMIZE_USER to "yes"))

        verify(exactly = 0) { appsFlyerLib.anonymizeUser(any()) }
        verify(exactly = 1) { logger.warning(match { it.contains(Settings.ANONYMIZE_USER) }) }
    }

    @Test
    fun initialize_withoutBooleanSettings_doesNotWarn() {
        instance().initialize("key", emptyMap())

        verify(exactly = 0) { logger.warning(any()) }
    }

    @Test
    fun stopTracking_automaticAndReady_startsOnlyWhenResuming() {
        val instance = instance()
        instance.initialize("key", null)

        instance.stopTracking(true)
        verify(exactly = 0) { appsFlyerLib.start() }

        instance.stopTracking(false)
        verifyOrder {
            appsFlyerLib.stop(true, any())
            appsFlyerLib.stop(false, any())
            appsFlyerLib.start()
        }
        verify(exactly = 1) { appsFlyerLib.start() }
    }

    @Test
    fun stopTrackingFalse_automaticBeforeInitialize_doesNotStart() {
        instance().stopTracking(false)

        verify(exactly = 1) { appsFlyerLib.stop(false, any()) }
        verify(exactly = 0) { appsFlyerLib.start() }
    }

    @Test
    fun stopTrackingFalse_appManaged_doesNotStart() {
        val instance = instance(AppsFlyerSessionMode.APP_MANAGED)
        instance.initialize("key", null)

        instance.stopTracking(false)

        verify(exactly = 1) { appsFlyerLib.stop(false, any()) }
        verify(exactly = 0) { appsFlyerLib.start() }
    }

    @Test
    fun conversionData_dropsNullValues() {
        val listener = slot<AppsFlyerConversionListener>()
        every { appsFlyerLib.init(any(), capture(listener), any()) } returns appsFlyerLib
        instance().initialize("key", null)

        // The SDK documents that values can be null, even though the Kotlin signature says otherwise.
        val conversionData = mutableMapOf<String, Any?>(
            Tracking.GCD_IS_FIRST_LAUNCH to true,
            "media_source" to "email",
            "campaign" to null
        )
        @Suppress("UNCHECKED_CAST")
        listener.captured.onConversionDataSuccess(conversionData as MutableMap<String, Any>)

        verify(exactly = 1) {
            context.track(
                AttributionEvents.CONVERSION_DATA_RECEIVED,
                mapOf(Tracking.GCD_IS_FIRST_LAUNCH to true, "media_source" to "email")
            )
        }
    }
}
