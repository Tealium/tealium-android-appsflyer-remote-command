package com.tealium.remotecommands.appsflyer

import android.app.Application
import com.appsflyer.AppsFlyerLib
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Verifies the real [AppsFlyerInstance] settings path for `custom_data`, which
 * [InitializeSettingsForwardingTest] cannot cover because it uses a mock instance.
 */
@RunWith(RobolectricTestRunner::class)
class AppsFlyerInstanceCustomDataTest {

    private val appsFlyerLib = mockk<AppsFlyerLib>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(AppsFlyerLib::class)
        every { AppsFlyerLib.getInstance() } returns appsFlyerLib
    }

    @After
    fun tearDown() {
        unmockkStatic(AppsFlyerLib::class)
    }

    @Test
    fun initialize_customData_forwardsNonStringValuesToSetAdditionalData() {
        val application = mockk<Application>(relaxed = true)
        val remoteCommand = AppsFlyerRemoteCommand(application, AppsFlyerSessionMode.AUTOMATIC, "testKey")
        remoteCommand.appsFlyerInstance =
            AppsFlyerInstance(application, AppsFlyerSessionMode.AUTOMATIC, "testKey", mockk(relaxed = true), RemoteCommandLogger())
        val customData = JSONObject()
            .put("name", "value")
            .put("count", 2)
            .put("enabled", true)
            .put("nested", JSONObject().put("level", 1))
        val payload = JSONObject()
            .put(Config.DEV_KEY, "test_dev_key")
            .put(Config.SETTINGS, JSONObject().put(Settings.CUSTOM_DATA, customData))

        remoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)

        val captured = slot<Map<String, Any>>()
        verify(exactly = 1) { appsFlyerLib.setAdditionalData(capture(captured)) }
        assertEquals(
            mapOf("name" to "value", "count" to 2, "enabled" to true, "nested" to mapOf("level" to 1)),
            captured.captured
        )
    }
}
