package com.tealium.remotecommands.appsflyer

import android.app.Application
import com.appsflyer.AppsFlyerLib
import com.appsflyer.share.deeplink.DeepLink
import com.appsflyer.share.deeplink.DeepLinkResult
import com.tealium.remotecommands.RemoteCommandContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Verifies the app-open attribution events that [AppsFlyerRemoteCommand.onDeepLinking] reports through the real
 * [AppsFlyerInstance], against a mocked [AppsFlyerLib] singleton.
 */
@RunWith(RobolectricTestRunner::class)
class AppsFlyerInstanceDeepLinkTest {

    private val appsFlyerLib = mockk<AppsFlyerLib>(relaxed = true)
    private val application = mockk<Application>(relaxed = true)
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

    private fun command() = AppsFlyerRemoteCommand(application, AppsFlyerSessionMode.AUTOMATIC)
        .also { it.setContext(context) }

    private fun found(clickEvent: JSONObject) = DeepLinkResult(DeepLink.parse(clickEvent), null)

    private fun clickEvent() = JSONObject()
        .put("deep_link_value", "promo")
        .put("media_source", "email")
        .put("count", 2)
        .put("details", JSONObject().put("level", 1).put("missing", JSONObject.NULL))
        .put("tags", JSONArray().put("a").put(JSONObject.NULL).put(JSONObject().put("k", "v")))
        .put("dropped", JSONObject.NULL)

    @Test
    fun setContextAndInitialize_neverSubscribesForDeepLink() {
        val command = command()

        command.appsFlyerInstance.initialize("key", null)

        verify(exactly = 1) { appsFlyerLib.init("key", any(), any()) }
        verify(exactly = 0) { appsFlyerLib.subscribeForDeepLink(any()) }
    }

    @Test
    fun onDeepLinking_beforeSetContext_dropsResult() {
        val command = AppsFlyerRemoteCommand(application, AppsFlyerSessionMode.AUTOMATIC)

        command.onDeepLinking(found(clickEvent()))

        verify(exactly = 0) { context.track(any(), any()) }
    }

    @Test
    fun found_notDeferred_tracksClickEventWithoutNulls() {
        command().onDeepLinking(found(clickEvent()))

        verify(exactly = 1) {
            context.track(
                AttributionEvents.APP_OPEN_ATTRIBUTION,
                mapOf(
                    "deep_link_value" to "promo",
                    "media_source" to "email",
                    "count" to 2,
                    "details" to mapOf("level" to 1),
                    "tags" to listOf("a", mapOf("k" to "v"))
                )
            )
        }
    }

    @Test
    fun found_deferred_doesNotTrack() {
        command().onDeepLinking(found(clickEvent().put("is_deferred", true)))

        verify(exactly = 0) { context.track(any(), any()) }
    }

    @Test
    fun found_isDeferredFalse_tracks() {
        command().onDeepLinking(found(clickEvent().put("is_deferred", false)))

        verify(exactly = 1) { context.track(AttributionEvents.APP_OPEN_ATTRIBUTION, any()) }
    }

    @Test
    fun found_isDeferredAbsent_isTreatedAsNotDeferred() {
        val deepLink = DeepLink.parse(clickEvent())
        assertNull(deepLink.isDeferred())

        command().onDeepLinking(DeepLinkResult(deepLink, null))

        verify(exactly = 1) { context.track(AttributionEvents.APP_OPEN_ATTRIBUTION, any()) }
    }

    @Test
    fun error_tracksAppsFlyerErrorWithErrorName() {
        command().onDeepLinking(DeepLinkResult(null, DeepLinkResult.Error.TIMEOUT))

        verify(exactly = 1) {
            context.track(
                AttributionEvents.APPSFLYER_ERROR,
                mapOf(
                    AttributionEvents.KEY_ERROR_NAME to AttributionEvents.ERROR_APP_OPEN_ATTRIBUTION_FAILURE,
                    AttributionEvents.KEY_ERROR_MESSAGE to "TIMEOUT"
                )
            )
        }
    }

    @Test
    fun error_undocumentedDeveloperError_isTracked() {
        command().onDeepLinking(DeepLinkResult(null, DeepLinkResult.Error.DEVELOPER_ERROR))

        verify(exactly = 1) {
            context.track(
                AttributionEvents.APPSFLYER_ERROR,
                match { it[AttributionEvents.KEY_ERROR_MESSAGE] == "DEVELOPER_ERROR" }
            )
        }
    }

    @Test
    fun notFound_doesNotTrack() {
        command().onDeepLinking(DeepLinkResult(null, null))

        verify(exactly = 0) { context.track(any(), any()) }
    }
}
