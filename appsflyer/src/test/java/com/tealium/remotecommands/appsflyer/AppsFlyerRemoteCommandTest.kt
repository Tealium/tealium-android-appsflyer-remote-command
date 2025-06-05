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
                ),
                null
            )
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetUserEmailsWithHashType() {
        val userEmailProperties = JSONArray()
        userEmailProperties.put("test@testing.com")
        userEmailProperties.put("test2@testing.com")

        val payload = JSONObject()
        payload.put(Customer.EMAILS, userEmailProperties)
        payload.put(Customer.EMAIL_HASH_TYPE, "sha256")
        payload.put(COMMAND_NAME_KEY, Commands.SET_USER_EMAILS)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAILS), payload)

        verify {
            mockAppsFlyerInstance.setUserEmails(
                listOf(
                    "test@testing.com",
                    "test2@testing.com"
                ),
                EmailHashType.SHA256
            )
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetUserEmailsWithInvalidHashType() {
        val userEmailProperties = JSONArray()
        userEmailProperties.put("test@testing.com")

        val payload = JSONObject()
        payload.put(Customer.EMAILS, userEmailProperties)
        payload.put(Customer.EMAIL_HASH_TYPE, "invalid_hash")
        payload.put(COMMAND_NAME_KEY, Commands.SET_USER_EMAILS)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAILS), payload)

        verify {
            mockAppsFlyerInstance.setUserEmails(
                listOf("test@testing.com"),
                null 
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
    fun testDisableAppSetId() {
        val payload = JSONObject() 
        payload.put(COMMAND_NAME_KEY, Commands.DISABLE_APPSET_ID)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.DISABLE_APPSET_ID), payload)

        verify {
            mockAppsFlyerInstance.disableAppSetId()
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

    @Test
    fun testUpdateServerUninstallToken() {
        val payload = JSONObject()
        payload.put(Analytics.UNINSTALL_TOKEN, "test_uninstall_token_123")
        payload.put(COMMAND_NAME_KEY, Commands.UPDATE_SERVER_UNINSTALL_TOKEN)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.UPDATE_SERVER_UNINSTALL_TOKEN), payload)

        verify {
            mockAppsFlyerInstance.updateServerUninstallToken("test_uninstall_token_123")
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetIsUpdate() {
        val payload = JSONObject()
        payload.put(Analytics.IS_UPDATE, true)
        payload.put(COMMAND_NAME_KEY, Commands.SET_IS_UPDATE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_IS_UPDATE), payload)

        verify {
            mockAppsFlyerInstance.setIsUpdate(true)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetAdditionalData() {
        val additionalDataObj = JSONObject()
        additionalDataObj.put("custom_param_1", "value1")
        additionalDataObj.put("custom_param_2", "value2")
        additionalDataObj.put("app_version", "1.2.3")

        val payload = JSONObject()
        payload.put(Analytics.ADDITIONAL_DATA, additionalDataObj)
        payload.put(COMMAND_NAME_KEY, Commands.SET_ADDITIONAL_DATA)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_ADDITIONAL_DATA), payload)

        val expectedAdditionalData = mapOf(
            "custom_param_1" to "value1",
            "custom_param_2" to "value2",
            "app_version" to "1.2.3"
        )

        verify {
            mockAppsFlyerInstance.setAdditionalData(expectedAdditionalData)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetPhoneNumber() {
        val payload = JSONObject()
        payload.put(PhoneNumber.PHONE_NUMBER, "+1234567890")
        payload.put(COMMAND_NAME_KEY, Commands.SET_PHONE_NUMBER)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_PHONE_NUMBER), payload)

        verify {
            mockAppsFlyerInstance.setPhoneNumber("+1234567890")
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetOutOfStore() {
        val payload = JSONObject()
        payload.put(OutOfStore.SOURCE_NAME, "samsung_galaxy_store")
        payload.put(COMMAND_NAME_KEY, Commands.SET_OUT_OF_STORE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_OUT_OF_STORE), payload)

        verify {
            mockAppsFlyerInstance.setOutOfStore("samsung_galaxy_store")
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testAddPushNotificationDeepLinkPath() {
        val deepLinkPathArray = JSONArray()
        deepLinkPathArray.put("notification")
        deepLinkPathArray.put("campaigns")
        deepLinkPathArray.put("promotions")

        val payload = JSONObject()
        payload.put(PushNotification.DEEP_LINK_PATH, deepLinkPathArray)
        payload.put(COMMAND_NAME_KEY, Commands.ADD_PUSH_NOTIFICATION_DEEP_LINK_PATH)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.ADD_PUSH_NOTIFICATION_DEEP_LINK_PATH), payload)

        verify {
            mockAppsFlyerInstance.addPushNotificationDeepLinkPath(
                listOf("notification", "campaigns", "promotions")
            )
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSendPushNotificationData() {
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, Commands.SEND_PUSH_NOTIFICATION_DATA)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SEND_PUSH_NOTIFICATION_DATA), payload)

        verify {
            mockAppsFlyerInstance.sendPushNotificationData()
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testValidateAndLogInAppPurchase() {
        val payload = JSONObject()
        payload.put(InAppPurchase.PURCHASE_TYPE, "one_time_purchase")
        payload.put(InAppPurchase.PURCHASE_TOKEN, "test_token_123")
        payload.put(InAppPurchase.PRODUCT_ID, "premium_package")
        payload.put(InAppPurchase.PRICE, "9.99")
        payload.put(InAppPurchase.CURRENCY, "USD")
        
        val additionalParams = JSONObject()
        additionalParams.put("product_name", "Premium Package")
        additionalParams.put("category", "premium")
        payload.put(InAppPurchase.ADDITIONAL_PARAMETERS, additionalParams)
        
        payload.put(COMMAND_NAME_KEY, Commands.VALIDATE_AND_LOG_PURCHASE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.VALIDATE_AND_LOG_PURCHASE), payload)

        verify {
            mockAppsFlyerInstance.validateAndLogInAppPurchase(
                PurchaseType.ONE_TIME_PURCHASE,
                "test_token_123",
                "premium_package",
                "9.99",
                "USD",
                mapOf(
                    "product_name" to "Premium Package",
                    "category" to "premium"
                )
            )
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testValidateAndLogInAppPurchaseWithInvalidType() {
        val payload = JSONObject()
        payload.put(InAppPurchase.PURCHASE_TYPE, "invalid_type")
        payload.put(InAppPurchase.PURCHASE_TOKEN, "test_token_123")
        payload.put(InAppPurchase.PRODUCT_ID, "premium_package")
        payload.put(InAppPurchase.PRICE, "9.99")
        payload.put(InAppPurchase.CURRENCY, "USD")
        payload.put(COMMAND_NAME_KEY, Commands.VALIDATE_AND_LOG_PURCHASE)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.VALIDATE_AND_LOG_PURCHASE), payload)

        verify(exactly = 0) {
            mockAppsFlyerInstance.validateAndLogInAppPurchase(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun testLogSession() {
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, Commands.LOG_SESSION)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.LOG_SESSION), payload)

        verify {
            mockAppsFlyerInstance.logSession()
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testWaitForCustomerUserId() {
        val payload = JSONObject()
        payload.put(CustomerUserID.WAIT_FOR_CUSTOMER_USER_ID, true)
        payload.put(COMMAND_NAME_KEY, Commands.WAIT_FOR_CUSTOMER_USER_ID)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.WAIT_FOR_CUSTOMER_USER_ID), payload)

        verify {
            mockAppsFlyerInstance.waitForCustomerUserId(true)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetCustomerIdAndLogSession() {
        val payload = JSONObject()
        payload.put(Customer.USER_ID, "test_user_456")
        payload.put(COMMAND_NAME_KEY, Commands.SET_CUSTOMER_ID_AND_LOG_SESSION)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_CUSTOMER_ID_AND_LOG_SESSION), payload)

        verify {
            mockAppsFlyerInstance.setCustomerIdAndLogSession("test_user_456")
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetMinTimeBetweenSessions() {
        val payload = JSONObject()
        payload.put(AppConfig.MIN_TIME_BETWEEN_SESSIONS, 15)
        payload.put(COMMAND_NAME_KEY, Commands.SET_MIN_TIME_BETWEEN_SESSIONS)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_MIN_TIME_BETWEEN_SESSIONS), payload)

        verify {
            mockAppsFlyerInstance.setMinTimeBetweenSessions(15)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetAppId() {
        val payload = JSONObject()
        payload.put(AppConfig.APP_ID, "com.example.testapp")
        payload.put(COMMAND_NAME_KEY, Commands.SET_APP_ID)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_APP_ID), payload)

        verify {
            mockAppsFlyerInstance.setAppId("com.example.testapp")
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetDisableAdvertisingIdentifiers() {
        val payload = JSONObject()
        payload.put(Privacy.DISABLE_ADVERTISING_IDENTIFIERS, true)
        payload.put(COMMAND_NAME_KEY, Commands.SET_DISABLE_ADVERTISING_IDENTIFIERS)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_DISABLE_ADVERTISING_IDENTIFIERS), payload)

        verify {
            mockAppsFlyerInstance.setDisableAdvertisingIdentifiers(true)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testEnableTCFDataCollection() {
        val payload = JSONObject()
        payload.put(Privacy.ENABLE_TCF_DATA_COLLECTION, true)
        payload.put(COMMAND_NAME_KEY, Commands.ENABLE_TCF_DATA_COLLECTION)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.ENABLE_TCF_DATA_COLLECTION), payload)

        verify {
            mockAppsFlyerInstance.enableTCFDataCollection(true)
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetSharingFilterForPartners() {
        val partnersArray = JSONArray()
        partnersArray.put("facebook_int")
        partnersArray.put("googleadwords_int")
        partnersArray.put("snapchat_int")

        val payload = JSONObject()
        payload.put(Privacy.SHARING_FILTER_PARTNERS, partnersArray)
        payload.put(COMMAND_NAME_KEY, Commands.SET_SHARING_FILTER_FOR_PARTNERS)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_SHARING_FILTER_FOR_PARTNERS), payload)

        verify {
            mockAppsFlyerInstance.setSharingFilterForPartners(
                listOf("facebook_int", "googleadwords_int", "snapchat_int")
            )
        }

        confirmVerified(mockAppsFlyerInstance)
    }

    @Test
    fun testSetSharingFilterForPartnersWithSingleString() {
        val payload = JSONObject()
        payload.put(Privacy.SHARING_FILTER_PARTNERS, "facebook_int")
        payload.put(COMMAND_NAME_KEY, Commands.SET_SHARING_FILTER_FOR_PARTNERS)

        appsFlyerRemoteCommand.parseCommands(arrayOf(Commands.SET_SHARING_FILTER_FOR_PARTNERS), payload)

        verify {
            mockAppsFlyerInstance.setSharingFilterForPartners(listOf("facebook_int"))
        }

        confirmVerified(mockAppsFlyerInstance)
    }
}