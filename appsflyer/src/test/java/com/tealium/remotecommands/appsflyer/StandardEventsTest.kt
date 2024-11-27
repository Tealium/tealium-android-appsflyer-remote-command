package com.tealium.remotecommands.appsflyer

import android.app.Application
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StandardEventsTest {

    @MockK
    lateinit var mockApplication: Application

    @MockK(relaxed = true)
    lateinit var mockAppsFlyerInstance: AppsFlyerCommand

    lateinit var appsFlyerRemoteCommand: AppsFlyerRemoteCommand

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        appsFlyerRemoteCommand = AppsFlyerRemoteCommand(
            mockApplication,
            "testKey"
        )

        appsFlyerRemoteCommand.appsFlyerInstance = mockAppsFlyerInstance
    }

    @Test
    fun standardEvent_Returns_Mapped_AF_Event() {
        val isStandardEvent = appsFlyerRemoteCommand.standardEvent("levelachieved")
        assertNotNull(isStandardEvent)
        assertEquals(AFInAppEventType.LEVEL_ACHIEVED, isStandardEvent)
    }

    @Test
    fun standardEvent_Returns_Null_When_Invalid_AF_Event() {
        val isStandardEvent = appsFlyerRemoteCommand.standardEvent("testEvent")
        assertNull(isStandardEvent)
    }

    @Test
    fun parseCommands_Logs_Event_With_EventParameters_When_Present() {
        val payload = JSONObject()
        val eventParams = JSONObject()
        eventParams.put(AFInAppEventParameterName.LEVEL, 5)
        eventParams.put(AFInAppEventParameterName.SCORE, 500)

        payload.put(StandardEvents.EVENT_PARAMETERS, eventParams)

        appsFlyerRemoteCommand.parseCommands(arrayOf("levelachieved"), payload)

        verify {
            mockAppsFlyerInstance.trackEvent(
                AFInAppEventType.LEVEL_ACHIEVED,
                mapOf(
                    AFInAppEventParameterName.LEVEL to 5,
                    AFInAppEventParameterName.SCORE to 500
                )
            )
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun parseCommands_Logs_Event_Using_Payload_When_EventParameters_Not_Present() {
        val payload = JSONObject()
        payload.put("af_data", "12345")
        appsFlyerRemoteCommand.parseCommands(arrayOf("levelachieved"), payload)

        verify {
            mockAppsFlyerInstance.trackEvent(AFInAppEventType.LEVEL_ACHIEVED, mapOf(
                "af_data" to "12345"
            ))
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun parseCommands_Logs_Event_With_ShortEventParameters_When_Present() {
        val payload = JSONObject()
        val eventParams = JSONObject()
        eventParams.put(AFInAppEventParameterName.LEVEL, 5)
        eventParams.put(AFInAppEventParameterName.SCORE, 500)

        payload.put(StandardEvents.EVENT_PARAMETERS, eventParams)

        appsFlyerRemoteCommand.parseCommands(arrayOf("levelachieved"), payload)

        verify {
            mockAppsFlyerInstance.trackEvent(
                AFInAppEventType.LEVEL_ACHIEVED,
                mapOf(
                    AFInAppEventParameterName.LEVEL to 5,
                    AFInAppEventParameterName.SCORE to 500
                )
            )
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun parseCommands_Logs_Event_Using_Payload_When_EventParametersShort_Not_Present() {
        val payload = JSONObject()
        payload.put("af_data", "12345")
        appsFlyerRemoteCommand.parseCommands(arrayOf("levelachieved"), payload)

        verify {
            mockAppsFlyerInstance.trackEvent(AFInAppEventType.LEVEL_ACHIEVED, mapOf(
                "af_data" to "12345"
            ))
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun parseCommands_Logs_Custom_Event_With_EventParameters_When_Present() {
        val customEventName = "mycustomevent"
        val payload = JSONObject()
        val eventParams = JSONObject()
        eventParams.put("some_param_1", 5)
        eventParams.put("some_param_2", 500)

        payload.put(StandardEvents.EVENT_PARAMETERS, eventParams)

        appsFlyerRemoteCommand.parseCommands(arrayOf(customEventName), payload)

        verify {
            mockAppsFlyerInstance.trackEvent(
                customEventName,
                mapOf(
                    "some_param_1" to 5,
                    "some_param_2" to 500
                )
            )
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun parseCommands_Logs_Custom_Event_Without_ShortEventParameters_When_Not_Present() {
        val customEventName = "mycustomevent"
        val payload = JSONObject()
        payload.put("af_data", "12345")
        appsFlyerRemoteCommand.parseCommands(arrayOf(customEventName), payload)

        verify {
            mockAppsFlyerInstance.trackEvent(customEventName, mapOf(
                "af_data" to "12345"
            ))
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun parseCommands_Prefers_EventParameters_Over_Short_When_Both_Present() {
        val payload = JSONObject()
        val eventParameters = JSONObject().apply {
            put("event_params_long", "value")
        }
        val eventParametersShort = JSONObject().apply {
            put("event_params_short", "value")
        }
        payload.put(StandardEvents.EVENT_PARAMETERS, eventParameters)
        payload.put(StandardEvents.EVENT_PARAMETERS_SHORT, eventParametersShort)
        appsFlyerRemoteCommand.parseCommands(arrayOf("custom"), payload)

        verify {
            mockAppsFlyerInstance.trackEvent(
                "custom", mapOf(
                    "event_params_long" to "value"
                )
            )
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun parseCommands_Logs_Event_Without_Filtered_Keys() {
        val payload = JSONObject()
        payload.put("method", "12345")
        payload.put(Config.DEBUG, true)
        payload.put(Config.DEV_KEY, "12345")
        payload.put(Config.SETTINGS, "12345")
        payload.put(Commands.COMMAND_KEY, "12345")
        payload.put("app_id", "12345")

        appsFlyerRemoteCommand.parseCommands(arrayOf("levelachieved"), payload)

        verify {
            mockAppsFlyerInstance.trackEvent(
                AFInAppEventType.LEVEL_ACHIEVED,
                mapOf()
            )
        }

        confirmVerified(mockAppsFlyerInstance)
    }
}