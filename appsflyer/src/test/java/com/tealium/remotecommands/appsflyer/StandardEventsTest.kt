package com.tealium.remotecommands.appsflyer

import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
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
    lateinit var mockTracker: AppsFlyerTrackable

    @InjectMockKs
    var appsFlyerRemoteCommand: AppsFlyerRemoteCommand = AppsFlyerRemoteCommand()

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        appsFlyerRemoteCommand.tracker = mockTracker
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
            mockTracker.trackEvent(any(), any())
        } just Runs

        verify {
            mockTracker.trackEvent(AFInAppEventType.LEVEL_ACHIEVED,
                mapOf(
                    AFInAppEventParameterName.LEVEL to 5,
                    AFInAppEventParameterName.SCORE to 500
                )
            )
        }

        confirmVerified(mockTracker)
    }

    @Test
    fun testStandardEventWithoutOptionalData() {
        val payload = JSONObject()
        payload.put("event", JSONObject())
        appsFlyerRemoteCommand.parseCommands(arrayOf("levelachieved"), payload)

        every {
            mockTracker.trackEvent(any(), any())
        } just runs

        verify {
            mockTracker.trackEvent(AFInAppEventType.LEVEL_ACHIEVED)
        }

        confirmVerified(mockTracker)
    }

}