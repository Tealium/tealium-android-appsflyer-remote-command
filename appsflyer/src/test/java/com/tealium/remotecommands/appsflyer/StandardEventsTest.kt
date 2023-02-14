package com.tealium.remotecommands.appsflyer

import android.app.Application
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StandardEventsTest {

    @MockK
    lateinit var mockApplication: Application

    @MockK
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
    fun testStandardEventSuccess() {
        val isStandardEvent = appsFlyerRemoteCommand.standardEvent("levelachieved")
        Assert.assertNotNull(isStandardEvent)
    }

    @Test
    fun testStandardEventFailure() {
        val isStandardEvent = appsFlyerRemoteCommand.standardEvent("testEvent")
        Assert.assertNull(isStandardEvent)
    }

    @Test
    fun testStandardEventWithOptionalData() {
        val payload = JSONObject()
        val eventParams = JSONObject()
        eventParams.put(AFInAppEventParameterName.LEVEL, 5)
        eventParams.put(AFInAppEventParameterName.SCORE, 500)

        payload.put(StandardEvents.EVENT_PARAMETERS, eventParams)

        appsFlyerRemoteCommand.parseCommands(arrayOf("levelachieved"), payload)

        every {
            mockAppsFlyerInstance.trackEvent(any(), any())
        } just Runs

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
    fun testStandardEventWithoutOptionalData() {
        val payload = JSONObject()
        payload.put("event", JSONObject())
        appsFlyerRemoteCommand.parseCommands(arrayOf("levelachieved"), payload)

        every {
            mockAppsFlyerInstance.trackEvent(any(), any())
        } just runs

        verify {
            mockAppsFlyerInstance.trackEvent(AFInAppEventType.LEVEL_ACHIEVED)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testCustomEventWithOptionalData() {
        val customEventName = "mycustomevent"
        val payload = JSONObject()
        val eventParams = JSONObject()
        eventParams.put("some_param_1", 5)
        eventParams.put("some_param_2", 500)

        payload.put(StandardEvents.EVENT_PARAMETERS, eventParams)

        appsFlyerRemoteCommand.parseCommands(arrayOf(customEventName), payload)

        every {
            mockAppsFlyerInstance.trackEvent(any(), any())
        } just Runs

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
    fun testCustomEventWithoutOptionalData() {
        val customEventName = "mycustomevent"
        val payload = JSONObject()
        payload.put("event", JSONObject())
        appsFlyerRemoteCommand.parseCommands(arrayOf(customEventName), payload)

        every {
            mockAppsFlyerInstance.trackEvent(any(), any())
        } just runs

        verify {
            mockAppsFlyerInstance.trackEvent(customEventName)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

}