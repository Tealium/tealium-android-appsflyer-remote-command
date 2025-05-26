package com.tealium.remotecommands.appsflyer

import android.app.Application
import com.appsflyer.MediationNetwork
import io.mockk.*
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
    lateinit var mockApplication: Application

    @MockK(relaxed = true)
    lateinit var mockAppsFlyerInstance: AppsFlyerCommand

    lateinit var appsFlyerRemoteCommand: AppsFlyerRemoteCommand

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        appsFlyerRemoteCommand = AppsFlyerRemoteCommand(
            mockApplication,
            "testKey")

        appsFlyerRemoteCommand.appsFlyerInstance = mockAppsFlyerInstance
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
    fun testInvokeTrackLocation() {
        val payload = JSONObject()
        payload.put(Location.LATITUDE, 10.0)
        payload.put(Location.LONGITUDE, 11.0)
        payload.put(COMMAND_NAME_KEY, Commands.TRACK_LOCATION)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.TRACK_LOCATION), payload)

        verify {
            mockAppsFlyerInstance.trackLocation(10.0, 11.0)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetHost() {
        val payload = JSONObject()
        payload.put(Host.HOST, "www.test123.com")
        payload.put(Host.HOST_PREFIX, "")
        payload.put(COMMAND_NAME_KEY, Commands.SET_HOST)
        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_HOST), payload)

        verify {
            mockAppsFlyerInstance.setHost("www.test123.com")
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetUserEmails() {
        val userEmailProperties = JSONArray()
        userEmailProperties.put("test@testing.com")
        userEmailProperties.put("test2@testing.com")
        userEmailProperties.put("test3@testing.com")

        val payload = JSONObject()
        payload.put(Customer.EMAILS, userEmailProperties)
        payload.put(COMMAND_NAME_KEY, Commands.SET_USER_EMAILS)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAILS), payload)

        verify {
            mockAppsFlyerInstance.setUserEmails(
                listOf(
                    "test@testing.com",
                    "test2@testing.com",
                    "test3@testing.com"
                )
            )
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetCurrencyCode() {
        val payload = JSONObject()
        payload.put(TransactionProperties.CURRENCY, "USD")
        payload.put(COMMAND_NAME_KEY, Commands.SET_CURRENCY_CODE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_CURRENCY_CODE), payload)

        verify {
            mockAppsFlyerInstance.setCurrencyCode("USD")
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetCustomerId() {
        val payload = JSONObject()
        payload.put(Customer.USER_ID, "1234")
        payload.put(COMMAND_NAME_KEY, Commands.SET_CUSTOMER_ID)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_CUSTOMER_ID), payload)

        verify {
            mockAppsFlyerInstance.setCustomerId("1234")
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testAnonymizeUser() {
        val payload = JSONObject()
        payload.put(Tracking.ANONYMIZE_USER, true)
        payload.put(COMMAND_NAME_KEY, Commands.ANONYMIZE_USER)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.ANONYMIZE_USER), payload)

        verify {
            mockAppsFlyerInstance.anonymizeUser(true)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testResolveDeepLinkUrls() {
        val deepLinkProperties = JSONArray()
        deepLinkProperties.put("val1")
        deepLinkProperties.put("val2")
        deepLinkProperties.put("val3")


        val payload = JSONObject()
        payload.put(DeepLink.URLS, deepLinkProperties)
        payload.put(COMMAND_NAME_KEY, Commands.RESOLVE_DEEPLINK_URLS)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.RESOLVE_DEEPLINK_URLS), payload)

        verify {
            mockAppsFlyerInstance.resolveDeepLinkUrls(listOf("val1", "val2", "val3"))
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testStopTracking() {
        val payload = JSONObject()
        payload.put(Tracking.STOP_TRACKING, true)
        payload.put(COMMAND_NAME_KEY, Commands.STOP_TRACKING)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.STOP_TRACKING), payload)

        verify {
            mockAppsFlyerInstance.stopTracking(true)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetDisableNetworkData() {
        val payload = JSONObject()
        payload.put(Config.DISABLE_NETWORK_DATA, true)
        payload.put(COMMAND_NAME_KEY, Commands.SET_DISABLE_NETWORK_DATA)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_DISABLE_NETWORK_DATA), payload)

        verify {
            mockAppsFlyerInstance.setDisableNetworkData(true)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetDisableNetworkDataInInitialize() {
        val payload = JSONObject()
        payload.put(Config.DISABLE_NETWORK_DATA, true)
        payload.put(COMMAND_NAME_KEY, Commands.INITIALIZE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)

        verify {
            mockAppsFlyerInstance.setDisableNetworkData(true)
            mockAppsFlyerInstance.initialize(any(), any())
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testLogAdRevenue() {
        val payload = JSONObject()
        payload.put(AdRevenue.MONETIZATION_NETWORK, "test_network")
        payload.put(AdRevenue.MEDIATION_NETWORK, "googleadmob")
        payload.put(AdRevenue.REVENUE, 10.50)
        payload.put(AdRevenue.CURRENCY, "USD")
        
        val additionalParams = JSONObject()
        additionalParams.put("country", "US")
        additionalParams.put("ad_unit", "banner_1")
        payload.put(AdRevenue.ADDITIONAL_PARAMETERS, additionalParams)
        
        payload.put(COMMAND_NAME_KEY, Commands.LOG_AD_REVENUE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)

        verify {
            mockAppsFlyerInstance.logAdRevenue(
                "test_network",
                MediationNetwork.GOOGLE_ADMOB,
                10.50,
                "USD",
                mapOf(
                    "country" to "US",
                    "ad_unit" to "banner_1"
                )
            )
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testLogAdRevenueWithInvalidMediationNetwork() {
        val payload = JSONObject()
        payload.put(AdRevenue.MONETIZATION_NETWORK, "test_network")
        payload.put(AdRevenue.MEDIATION_NETWORK, "invalid_network")
        payload.put(AdRevenue.REVENUE, 10.50)
        payload.put(AdRevenue.CURRENCY, "USD")
        
        payload.put(COMMAND_NAME_KEY, Commands.LOG_AD_REVENUE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)

        verify(exactly = 0) {
            mockAppsFlyerInstance.logAdRevenue(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun testEnableAppSetIdCollection() {
        val payload = JSONObject()
        payload.put(Config.ENABLE_APPSET_ID, true)
        payload.put(COMMAND_NAME_KEY, Commands.ENABLE_APPSET_ID)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.ENABLE_APPSET_ID), payload)

        verify {
            mockAppsFlyerInstance.enableAppSetIdCollection(true)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetDMAConsentData() {
        val payload = JSONObject()
        payload.put(DMAConsent.GDPR_APPLIES, true)
        payload.put(DMAConsent.CONSENT_FOR_DATA_USAGE, true)
        payload.put(DMAConsent.CONSENT_FOR_ADS_PERSONALIZATION, false)
        payload.put(DMAConsent.CONSENT_FOR_AD_STORAGE, true)
        payload.put(COMMAND_NAME_KEY, Commands.SET_DMA_CONSENT)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_DMA_CONSENT), payload)

        val expectedConsentData = mapOf(
            DMAConsent.GDPR_APPLIES to true,
            DMAConsent.CONSENT_FOR_DATA_USAGE to true,
            DMAConsent.CONSENT_FOR_ADS_PERSONALIZATION to false,
            DMAConsent.CONSENT_FOR_AD_STORAGE to true
        )

        verify {
            mockAppsFlyerInstance.setDMAConsentData(expectedConsentData)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetDMAConsentDataForNonGDPRUser() {
        val payload = JSONObject()
        payload.put(DMAConsent.GDPR_APPLIES, false)
        payload.put(COMMAND_NAME_KEY, Commands.SET_DMA_CONSENT)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_DMA_CONSENT), payload)

        val expectedConsentData = mapOf(
            DMAConsent.GDPR_APPLIES to false
        )

        verify {
            mockAppsFlyerInstance.setDMAConsentData(expectedConsentData)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testBlankCommandDoesNothing() {
        val payload = JSONObject()
        val relaxedMockInstance: AppsFlyerInstance = mockk(relaxed = true)
        appsFlyerRemoteCommand.appsFlyerInstance = relaxedMockInstance

        appsFlyerRemoteCommand.parseCommands(arrayOf(" "), payload)

        verify {
            relaxedMockInstance wasNot Called
        }

        confirmVerified(relaxedMockInstance)
    }

    @Test
    fun testBlankCommandDoesntAffectOtherCommands() {
        val payload = JSONObject()
        val relaxedMockInstance: AppsFlyerInstance = mockk(relaxed = true)
        appsFlyerRemoteCommand.appsFlyerInstance = relaxedMockInstance

        appsFlyerRemoteCommand.parseCommands(arrayOf(" ", Commands.STOP_TRACKING), payload)

        verify(exactly = 0) {
            relaxedMockInstance.trackEvent(" ")
        }
        verify(exactly = 1) {
            relaxedMockInstance.stopTracking(false)
        }

        confirmVerified(relaxedMockInstance)
    }

    @Test
    fun testInitialize() {
        val payload = JSONObject()
        payload.put(Config.DEV_KEY, "test_dev_key")
        payload.put(Config.DEBUG, true)
        payload.put(Config.ANONYMIZE_USER, false)
        payload.put(Config.MIN_TIME_BETWEEN_SESSIONS, 30)
        payload.put(COMMAND_NAME_KEY, Commands.INITIALIZE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)

        verify {
            mockAppsFlyerInstance.initialize("test_dev_key", any())
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testInitializeWithoutDevKey() {
        val payload = JSONObject()
        payload.put(Config.DEBUG, true)
        payload.put(COMMAND_NAME_KEY, Commands.INITIALIZE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)

        verify {
            mockAppsFlyerInstance.initialize("", any())
        }

        confirmVerified(mockAppsFlyerInstance)
    }
}