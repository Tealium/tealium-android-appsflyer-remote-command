package com.tealium.remotecommands.appsflyer

import android.app.Application
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
    fun testTrackLocation() {
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
        payload.put(COMMAND_NAME_KEY, Commands.INITIALIZE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)

        verify {
            mockAppsFlyerInstance.initialize("test_dev_key", emptyMap())
        }
        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testInitializeWithSettings() {
        val payload = JSONObject()
        val settings = JSONObject()
        settings.put(Settings.DEBUG, true)
        settings.put(Settings.ANONYMIZE_USER, false)
        
        payload.put(Config.DEV_KEY, "test_dev_key")
        payload.put(Config.SETTINGS, settings)
        payload.put(COMMAND_NAME_KEY, Commands.INITIALIZE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)

        verify {
            mockAppsFlyerInstance.initialize(
                "test_dev_key",
                mapOf(
                    "debug" to true,
                    "anonymize_user" to false
                )
            )
        }
        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testInitializeWithAdditionalSettings() {
        val payload = JSONObject()
        val settings = JSONObject()
        val deepLinkPath = JSONArray()
        deepLinkPath.put("path1")
        deepLinkPath.put("path2")
        
        settings.put(Settings.TIME_BETWEEN_SESSIONS, 300)
        settings.put(Settings.COLLECT_DEVICE_NAME, true)
        settings.put(Settings.PUSH_NOTIFICATION_DEEP_LINK_PATH, deepLinkPath)
        
        payload.put(Config.DEV_KEY, "test_dev_key")
        payload.put(Config.SETTINGS, settings)
        payload.put(COMMAND_NAME_KEY, Commands.INITIALIZE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)

        verify {
            mockAppsFlyerInstance.initialize(
                "test_dev_key",
                mapOf(
                    "time_between_sessions" to 300,
                    "collect_device_name" to true,
                    "push_notification_deep_link_path" to deepLinkPath
                )
            )
        }
        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetPhoneNumber() {
        val payload = JSONObject()
        payload.put(PhoneNumberParam.PHONE_NUMBER, "+1234567890")
        payload.put(COMMAND_NAME_KEY, Commands.SET_PHONE_NUMBER)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_PHONE_NUMBER), payload)

        verify {
            mockAppsFlyerInstance.setPhoneNumber("+1234567890")
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testLogAdRevenue() {
        val payload = JSONObject()
        val additionalParams = JSONObject()
        additionalParams.put("param1", "value1")
        
        payload.put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
        payload.put(AdRevenueParams.MEDIATION_NETWORK, "googleadmob")
        payload.put(AdRevenueParams.AD_REVENUE_CURRENCY, "USD")
        payload.put(AdRevenueParams.AD_REVENUE_AMOUNT, 1.99)
        payload.put(AdRevenueParams.AD_REVENUE_ADDITIONAL_PARAMS, additionalParams)
        payload.put(COMMAND_NAME_KEY, Commands.LOG_AD_REVENUE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)

        verify {
            mockAppsFlyerInstance.logAdRevenue(any(), mapOf("param1" to "value1"))
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetConsentData() {
        val payload = JSONObject()
        payload.put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, true)
        payload.put(ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE, true)
        payload.put(ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION, false)
        payload.put(ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE, true)
        payload.put(COMMAND_NAME_KEY, Commands.SET_CONSENT_DATA)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)

        verify {
            mockAppsFlyerInstance.setConsentData(true, true, false, true)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetPartnerData() {
        val payload = JSONObject()
        val partnerInfo = JSONObject()
        partnerInfo.put("key1", "value1")
        partnerInfo.put("key2", "value2")
        
        payload.put(PartnerDataParams.PARTNER_ID, "test_partner")
        payload.put(PartnerDataParams.PARTNER_INFO, partnerInfo)
        payload.put(COMMAND_NAME_KEY, Commands.SET_PARTNER_DATA)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_PARTNER_DATA), payload)

        verify {
            mockAppsFlyerInstance.setPartnerData("test_partner", mapOf("key1" to "value1", "key2" to "value2"))
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetSharingFilterForPartners() {
        val payload = JSONObject()
        val sharingFilter = JSONArray()
        sharingFilter.put("partner1")
        sharingFilter.put("partner2")
        
        payload.put(SharingFilterParams.SHARING_FILTER, sharingFilter)
        payload.put(COMMAND_NAME_KEY, Commands.SET_SHARING_FILTER_FOR_PARTNERS)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_SHARING_FILTER_FOR_PARTNERS), payload)

        verify {
            mockAppsFlyerInstance.setSharingFilterForPartners(arrayOf("partner1", "partner2"))
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetSharingFilterForPartnersReset() {
        val payload = JSONObject()
        // No sharing_filter parameter - should reset filter
        payload.put(COMMAND_NAME_KEY, Commands.SET_SHARING_FILTER_FOR_PARTNERS)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_SHARING_FILTER_FOR_PARTNERS), payload)

        verify {
            mockAppsFlyerInstance.setSharingFilterForPartners(null)
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
}