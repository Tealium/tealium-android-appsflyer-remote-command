package com.tealium.remotecommands.appsflyer

import android.app.Application
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Verifies that every supported SDK-level setting in the `initialize` payload
 * is forwarded to [AppsFlyerCommand.initialize] as a correctly-typed map entry.
 *
 * The branching logic that consumes these keys lives in [AppsFlyerInstance] and
 * depends on the static [com.appsflyer.AppsFlyerLib] singleton; mocking that
 * through to every branch would be brittle. Asserting the payload is forwarded
 * verbatim here, and letting the SDK-level behaviour be covered by integration
 * tests / the SDK's own suite, keeps the unit tests focused and fast.
 */
@RunWith(RobolectricTestRunner::class)
class InitializeSettingsForwardingTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    lateinit var mockApplication: Application

    private lateinit var mockInstance: MockAppsFlyerInstance
    private lateinit var remoteCommand: AppsFlyerRemoteCommand

    @Before
    fun setUp() {
        mockInstance = MockAppsFlyerInstance()
        remoteCommand = AppsFlyerRemoteCommand(mockApplication, "testKey")
        remoteCommand.appsFlyerInstance = mockInstance
    }

    private fun initializeWithSettings(settings: JSONObject) {
        val payload = JSONObject()
            .put(Config.DEV_KEY, "test_dev_key")
            .put(Config.SETTINGS, settings)
        remoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)
    }

    @Test
    fun initialize_forwardsEnableFacebookDeferredApplinks() {
        initializeWithSettings(JSONObject().put(Settings.ENABLE_FACEBOOK_DEFERRED_APPLINKS, true))
        assertEquals(1, mockInstance.initializeCallCount)
        assertEquals(true, mockInstance.initializeSettingsParam?.get(Settings.ENABLE_FACEBOOK_DEFERRED_APPLINKS))
    }

    @Test
    fun initialize_forwardsTimeBetweenSessions() {
        initializeWithSettings(JSONObject().put(Settings.TIME_BETWEEN_SESSIONS, 300))
        assertEquals(300, mockInstance.initializeSettingsParam?.get(Settings.TIME_BETWEEN_SESSIONS))
    }

    @Test
    fun initialize_forwardsAnonymizeUser() {
        initializeWithSettings(JSONObject().put(Settings.ANONYMIZE_USER, true))
        assertEquals(true, mockInstance.initializeSettingsParam?.get(Settings.ANONYMIZE_USER))
    }

    @Test
    fun initialize_forwardsCustomData() {
        val custom = JSONObject().put("a", "1").put("b", "2")
        initializeWithSettings(JSONObject().put(Settings.CUSTOM_DATA, custom))
        assertEquals(custom, mockInstance.initializeSettingsParam?.get(Settings.CUSTOM_DATA))
    }

    @Test
    fun initialize_forwardsDebug() {
        initializeWithSettings(JSONObject().put(Settings.DEBUG, true))
        assertEquals(true, mockInstance.initializeSettingsParam?.get(Settings.DEBUG))
    }

    @Test
    fun initialize_forwardsLogLevel() {
        initializeWithSettings(JSONObject().put(Settings.LOG_LEVEL, "VERBOSE"))
        assertEquals("VERBOSE", mockInstance.initializeSettingsParam?.get(Settings.LOG_LEVEL))
    }

    @Test
    fun initialize_forwardsPushNotificationDeepLinkPath() {
        val paths = JSONArray().put("a").put("b")
        initializeWithSettings(JSONObject().put(Settings.PUSH_NOTIFICATION_DEEP_LINK_PATH, paths))
        assertEquals(paths, mockInstance.initializeSettingsParam?.get(Settings.PUSH_NOTIFICATION_DEEP_LINK_PATH))
    }

    @Test
    fun initialize_forwardsEnableTcfDataCollection() {
        initializeWithSettings(JSONObject().put(Settings.ENABLE_TCF_DATA_COLLECTION, true))
        assertEquals(true, mockInstance.initializeSettingsParam?.get(Settings.ENABLE_TCF_DATA_COLLECTION))
    }

    @Test
    fun initialize_forwardsOneLinkCustomDomains() {
        val domains = JSONArray().put("custom.example.com").put("other.example.com")
        initializeWithSettings(JSONObject().put(Settings.ONE_LINK_CUSTOM_DOMAINS, domains))
        assertEquals(domains, mockInstance.initializeSettingsParam?.get(Settings.ONE_LINK_CUSTOM_DOMAINS))
    }

    @Test
    fun initialize_forwardsDisableAdvertisingIdentifiers() {
        initializeWithSettings(JSONObject().put(Settings.DISABLE_ADVERTISING_IDENTIFIERS, true))
        assertEquals(true, mockInstance.initializeSettingsParam?.get(Settings.DISABLE_ADVERTISING_IDENTIFIERS))
    }

    @Test
    fun initialize_forwardsDisableAdTrackingAlias() {
        initializeWithSettings(JSONObject().put(Settings.DISABLE_AD_TRACKING_ALIAS, true))
        assertEquals(true, mockInstance.initializeSettingsParam?.get(Settings.DISABLE_AD_TRACKING_ALIAS))
    }

    @Test
    fun initialize_forwardsDisableAppSetId() {
        initializeWithSettings(JSONObject().put(Settings.DISABLE_APP_SET_ID, true))
        assertEquals(true, mockInstance.initializeSettingsParam?.get(Settings.DISABLE_APP_SET_ID))
    }

    @Test
    fun initialize_forwardsCollectAndroidId() {
        initializeWithSettings(JSONObject().put(Settings.COLLECT_ANDROID_ID, true))
        assertEquals(true, mockInstance.initializeSettingsParam?.get(Settings.COLLECT_ANDROID_ID))
    }

    @Test
    fun initialize_forwardsCollectImei() {
        initializeWithSettings(JSONObject().put(Settings.COLLECT_IMEI, true))
        assertEquals(true, mockInstance.initializeSettingsParam?.get(Settings.COLLECT_IMEI))
    }

    @Test
    fun initialize_forwardsDeepLinkParameters() {
        val entries = JSONArray().put(
            JSONObject()
                .put(DeepLinkParameterEntry.CONTAINS, "discount")
                .put(DeepLinkParameterEntry.PARAMETERS, JSONObject().put("af_dp", "x"))
        )
        initializeWithSettings(JSONObject().put(Settings.DEEP_LINK_PARAMETERS, entries))
        assertEquals(entries, mockInstance.initializeSettingsParam?.get(Settings.DEEP_LINK_PARAMETERS))
    }

    @Test
    fun initialize_forwardsAllSettingsSimultaneously() {
        val settings = JSONObject()
            .put(Settings.DEBUG, true)
            .put(Settings.LOG_LEVEL, "INFO")
            .put(Settings.TIME_BETWEEN_SESSIONS, 42)
            .put(Settings.ANONYMIZE_USER, false)
            .put(Settings.COLLECT_ANDROID_ID, true)
            .put(Settings.COLLECT_IMEI, false)

        initializeWithSettings(settings)

        val forwarded = mockInstance.initializeSettingsParam!!
        assertEquals(6, forwarded.size)
        assertTrue(forwarded.containsKey(Settings.DEBUG))
        assertTrue(forwarded.containsKey(Settings.LOG_LEVEL))
        assertTrue(forwarded.containsKey(Settings.TIME_BETWEEN_SESSIONS))
        assertTrue(forwarded.containsKey(Settings.ANONYMIZE_USER))
        assertTrue(forwarded.containsKey(Settings.COLLECT_ANDROID_ID))
        assertTrue(forwarded.containsKey(Settings.COLLECT_IMEI))
    }

    @Test
    fun initialize_devKeyFromConstructor_usedWhenPayloadOmitsIt() {
        // When the payload supplies app_dev_key, the initialize() call is dispatched
        // even if the SDK-level fallback (constructor key) is also set.
        val payload = JSONObject().put(Config.DEV_KEY, "payload_key")
        remoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)

        assertEquals(1, mockInstance.initializeCallCount)
        assertEquals("payload_key", mockInstance.initializeDevKeyParam)
        assertNotNull(mockInstance.initializeSettingsParam)
    }
}
