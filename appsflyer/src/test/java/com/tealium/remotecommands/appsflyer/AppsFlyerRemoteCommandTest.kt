package com.tealium.remotecommands.appsflyer

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppsFlyerRemoteCommandTest {

    val COMMAND_NAME_KEY = "command_name"

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
    fun testSplitCommands() {
        val json = JSONObject()
        json.put("command_name", "initialize, log_purchase, add_to_cart")
        val commands = appsFlyerRemoteCommand.splitCommands(json)

        Assert.assertEquals(3, commands.count())
        Assert.assertEquals("initialize", commands[0])
        Assert.assertEquals("log_purchase", commands[1])
        Assert.assertEquals("add_to_cart", commands[2])
    }

    @Test
    fun testTrackLaunch() {
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, Commands.LAUNCH)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.LAUNCH), payload)

        every { mockTracker.trackLaunch() } just Runs

        verify {
            mockTracker.trackLaunch()
        }

        confirmVerified(mockTracker)
    }

    @Test
    fun testInvokeTrackLocation() {
        val payload = JSONObject()
        payload.put(Location.LATITUDE, 10.0)
        payload.put(Location.LONGITUDE, 11.0)
        payload.put(COMMAND_NAME_KEY, Commands.TRACK_LOCATION)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.TRACK_LOCATION), payload)

        every {
            mockTracker.trackLocation(any(), any())
        } just Runs

        verify {
            mockTracker.trackLocation(10.0, 11.0)
        }

        confirmVerified(mockTracker)
    }

    @Test
    fun testSetHost() {
        val payload = JSONObject()
        payload.put(Host.HOST, "www.test123.com")
        payload.put(Host.HOST_PREFIX, "")
        payload.put(COMMAND_NAME_KEY, Commands.SET_HOST)
        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_HOST), payload)

        every {
            mockTracker.setHost(any(), any())
        } just Runs

        verify {
            mockTracker.setHost("www.test123.com")
        }

        confirmVerified(mockTracker)
    }

    @Test
    fun testSetUserEmails() {
        val userEmailProperties = JSONArray()
        userEmailProperties.put("test@testing.com")
        userEmailProperties.put("test2@testing.com")
        userEmailProperties.put("test3@testing.com")

        val payload = JSONObject()
        payload.put(User.USER_EMAILS, userEmailProperties)
        payload.put(COMMAND_NAME_KEY, Commands.SET_USER_EMAILS)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAILS), payload)

        every {
            mockTracker.setUserEmails(any())
        } just Runs

        verify {
            mockTracker.setUserEmails(
                listOf(
                    "test@testing.com",
                    "test2@testing.com",
                    "test3@testing.com"
                )
            )
        }

        confirmVerified(mockTracker)
    }

    @Test
    fun testSetCurrencyCode() {
        val payload = JSONObject()
        payload.put(Currency.CURRENCY_CODE, "USD")
        payload.put(COMMAND_NAME_KEY, Commands.SET_CURRENCY_CODE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_CURRENCY_CODE), payload)

        every {
            mockTracker.setCurrencyCode(any())
        } just Runs

        verify {
            mockTracker.setCurrencyCode("USD")
        }

        confirmVerified(mockTracker)
    }

    @Test
    fun testSetCustomerId() {
        val payload = JSONObject()
        payload.put(User.CUSTOMER_USER_ID, "1234")
        payload.put(COMMAND_NAME_KEY, Commands.SET_CUSTOMER_ID)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_CUSTOMER_ID), payload)

        every {
            mockTracker.setCustomerId(any())
        } just Runs

        verify {
            mockTracker.setCustomerId("1234")
        }

        confirmVerified(mockTracker)
    }

    @Test
    fun testDisableTracking() {
        val payload = JSONObject()
        payload.put(Privacy.DISABLE_TRACKING, true)
        payload.put(COMMAND_NAME_KEY, Commands.DISABLE_TRACKING)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.DISABLE_TRACKING), payload)

        every {
            mockTracker.disableTracking(any())
        } just Runs

        verify {
            mockTracker.disableTracking(true)
        }

        confirmVerified(mockTracker)
    }

    @Test
    fun testResolveDeeplinkUrls() {
        val deeplinkProperties = JSONArray()
        deeplinkProperties.put("val1")
        deeplinkProperties.put("val2")
        deeplinkProperties.put("val3")


        val payload = JSONObject()
        payload.put(Deeplinking.DEEPLINK_URLS, deeplinkProperties)
        payload.put(COMMAND_NAME_KEY, Commands.RESOLVE_DEEPLINK_URLS)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.RESOLVE_DEEPLINK_URLS), payload)

        every {
            mockTracker.resolveDeeplinksUrls(any())
        } just Runs

        verify {
            mockTracker.resolveDeeplinksUrls(listOf("val1", "val2", "val3"))
        }

        confirmVerified(mockTracker)
    }
}