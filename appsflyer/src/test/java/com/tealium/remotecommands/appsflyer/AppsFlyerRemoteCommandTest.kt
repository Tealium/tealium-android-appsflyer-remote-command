package com.tealium.remotecommands.appsflyer

import android.app.Application
import com.appsflyer.share.AFInAppEventParameterName
import com.appsflyer.share.AFInAppEventType
import com.appsflyer.share.MediationNetwork
import com.tealium.remotecommands.RemoteCommandContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.verify
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * End-to-end tests for [AppsFlyerRemoteCommand].
 * Uses [MockAppsFlyerInstance] so tests can assert not only that the right
 * SDK call was made, but also that unrelated calls were NOT made — critical
 * for validating the "skip call on missing parameter" contract.
 */
@RunWith(RobolectricTestRunner::class)
class AppsFlyerRemoteCommandTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    // The command registers Activity lifecycle callbacks on the Application when it is constructed.
    @MockK(relaxUnitFun = true)
    lateinit var mockApplication: Application

    private lateinit var mockInstance: MockAppsFlyerInstance
    private lateinit var remoteCommand: AppsFlyerRemoteCommand

    @Before
    fun setUp() {
        mockInstance = MockAppsFlyerInstance()
        remoteCommand = AppsFlyerRemoteCommand(mockApplication, AppsFlyerSessionMode.AUTOMATIC, "testKey")
        remoteCommand.appsFlyerInstance = mockInstance
    }

    @Test
    fun splitCommands_splitsTrimsAndPreservesCase() {
        val json = JSONObject()
        json.put(Commands.COMMAND_KEY, "initialize, LogPurchase, add_to_cart")

        val commands = remoteCommand.splitCommands(json)

        assertEquals(3, commands.size)
        assertEquals("initialize", commands[0])
        assertEquals("LogPurchase", commands[1])
        assertEquals("add_to_cart", commands[2])
    }

    @Test
    fun splitCommands_emptyPayload_returnsSingleEmptyString() {
        val commands = remoteCommand.splitCommands(JSONObject())
        // An empty command_name results in no commands — trailing-empty dropped.
        assertEquals(0, commands.size)
    }

    @Test
    fun initialize_minimalPayload_dispatchesWithEmptySettings() {
        val payload = JSONObject().put(Config.DEV_KEY, "test_dev_key")

        remoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)

        assertEquals(1, mockInstance.initializeCallCount)
        assertEquals("test_dev_key", mockInstance.initializeDevKeyParam)
        assertEquals(emptyMap<String, Any>(), mockInstance.initializeSettingsParam)
    }

    @Test
    fun initialize_withSettings_forwardsMap() {
        val settings = JSONObject()
            .put(Settings.DEBUG, true)
            .put(Settings.ANONYMIZE_USER, false)
        val payload = JSONObject()
            .put(Config.DEV_KEY, "test_dev_key")
            .put(Config.SETTINGS, settings)

        remoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)

        assertEquals(1, mockInstance.initializeCallCount)
        val forwarded = mockInstance.initializeSettingsParam!!
        assertEquals(true, forwarded[Settings.DEBUG])
        assertEquals(false, forwarded[Settings.ANONYMIZE_USER])
    }

    @Test
    fun initialize_missingDevKey_skipsCall() {
        val commandWithoutKey = AppsFlyerRemoteCommand(mockApplication, AppsFlyerSessionMode.AUTOMATIC, null)
        commandWithoutKey.appsFlyerInstance = mockInstance
        commandWithoutKey.parseCommands(arrayOf(Commands.INITIALIZE), JSONObject())
        assertEquals(0, mockInstance.initializeCallCount)
    }

    @Test
    fun trackLocation_validCoordinates_dispatchesCall() {
        val payload = JSONObject()
            .put(Location.LATITUDE, 10.0)
            .put(Location.LONGITUDE, 20.0)

        remoteCommand.parseCommands(arrayOf(Commands.TRACK_LOCATION), payload)

        assertEquals(1, mockInstance.trackLocationCallCount)
        assertEquals(10.0, mockInstance.trackLocationLatitudeParam!!, 0.0001)
        assertEquals(20.0, mockInstance.trackLocationLongitudeParam!!, 0.0001)
    }

    @Test
    fun trackLocation_missingLatitude_skipsCall() {
        val payload = JSONObject().put(Location.LONGITUDE, 1.0)
        remoteCommand.parseCommands(arrayOf(Commands.TRACK_LOCATION), payload)
        assertEquals(0, mockInstance.trackLocationCallCount)
    }

    @Test
    fun trackLocation_missingLongitude_skipsCall() {
        val payload = JSONObject().put(Location.LATITUDE, 1.0)
        remoteCommand.parseCommands(arrayOf(Commands.TRACK_LOCATION), payload)
        assertEquals(0, mockInstance.trackLocationCallCount)
    }

    @Test
    fun setHost_valid_callsSDK() {
        val payload = JSONObject()
            .put(Host.HOST, "example.com")
            .put(Host.HOST_PREFIX, "my_prefix")

        remoteCommand.parseCommands(arrayOf(Commands.SET_HOST), payload)

        assertEquals(1, mockInstance.setHostCallCount)
        assertEquals("example.com", mockInstance.setHostHostParam)
        assertEquals("my_prefix", mockInstance.setHostPrefixParam)
    }

    @Test
    fun setHost_missingHost_skipsCall() {
        val payload = JSONObject().put(Host.HOST_PREFIX, "my_prefix")
        remoteCommand.parseCommands(arrayOf(Commands.SET_HOST), payload)
        assertEquals(0, mockInstance.setHostCallCount)
    }

    @Test
    fun setHost_missingHostPrefix_skipsCall() {
        val payload = JSONObject().put(Host.HOST, "example.com")
        remoteCommand.parseCommands(arrayOf(Commands.SET_HOST), payload)
        assertEquals(0, mockInstance.setHostCallCount)
    }

    @Test
    fun setUserEmail_valid_callsSDK() {
        val payload = JSONObject().put(Customer.EMAIL, "user@example.com")
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAIL), payload)
        assertEquals(1, mockInstance.setUserEmailCallCount)
        assertEquals("user@example.com", mockInstance.setUserEmailParam)
    }

    @Test
    fun setUserEmail_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAIL), JSONObject())
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserEmail_nonString_skipsCall() {
        val payload = JSONObject().put(Customer.EMAIL, JSONArray().put("user@example.com"))
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAIL), payload)
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserFirstName_valid_callsSDK() {
        val payload = JSONObject().put(Customer.FIRST_NAME, "Jane")
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FIRST_NAME), payload)
        assertEquals(1, mockInstance.setUserFirstNameCallCount)
        assertEquals("Jane", mockInstance.setUserFirstNameParam)
    }

    @Test
    fun setUserFirstName_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FIRST_NAME), JSONObject())
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserFirstName_nonString_skipsCall() {
        val payload = JSONObject().put(Customer.FIRST_NAME, 42)
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FIRST_NAME), payload)
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserLastName_valid_callsSDK() {
        val payload = JSONObject().put(Customer.LAST_NAME, "Doe")
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_LAST_NAME), payload)
        assertEquals(1, mockInstance.setUserLastNameCallCount)
        assertEquals("Doe", mockInstance.setUserLastNameParam)
    }

    @Test
    fun setUserLastName_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_LAST_NAME), JSONObject())
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserLastName_nonString_skipsCall() {
        val payload = JSONObject().put(Customer.LAST_NAME, true)
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_LAST_NAME), payload)
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserFbLoginId_long_callsSDK() {
        val payload = JSONObject().put(Customer.FB_LOGIN_ID, 10_000_000_000L)
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FB_LOGIN_ID), payload)
        assertEquals(1, mockInstance.setUserFbLoginIdCallCount)
        assertEquals(10_000_000_000L, mockInstance.setUserFbLoginIdParam)
    }

    @Test
    fun setUserFbLoginId_int_callsSDK() {
        val payload = JSONObject().put(Customer.FB_LOGIN_ID, 12345)
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FB_LOGIN_ID), payload)
        assertEquals(1, mockInstance.setUserFbLoginIdCallCount)
        assertEquals(12345L, mockInstance.setUserFbLoginIdParam)
    }

    @Test
    fun setUserFbLoginId_numericString_callsSDK() {
        val payload = JSONObject().put(Customer.FB_LOGIN_ID, "10000000000")
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FB_LOGIN_ID), payload)
        assertEquals(1, mockInstance.setUserFbLoginIdCallCount)
        assertEquals(10_000_000_000L, mockInstance.setUserFbLoginIdParam)
    }

    @Test
    fun setUserFbLoginId_wholeDouble_callsSDK() {
        val payload = JSONObject().put(Customer.FB_LOGIN_ID, 12345.0)
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FB_LOGIN_ID), payload)
        assertEquals(1, mockInstance.setUserFbLoginIdCallCount)
        assertEquals(12345L, mockInstance.setUserFbLoginIdParam)
    }

    @Test
    fun setUserFbLoginId_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FB_LOGIN_ID), JSONObject())
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserFbLoginId_wrongType_skipsCall() {
        val payload = JSONObject().put(Customer.FB_LOGIN_ID, true)
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FB_LOGIN_ID), payload)
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserFbLoginId_fractional_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FB_LOGIN_ID), JSONObject().put(Customer.FB_LOGIN_ID, 123.5))
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FB_LOGIN_ID), JSONObject().put(Customer.FB_LOGIN_ID, "123.5"))
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserFbLoginId_nonNumericString_skipsCall() {
        val payload = JSONObject().put(Customer.FB_LOGIN_ID, "abc123")
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FB_LOGIN_ID), payload)
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserFbLoginId_outOfRange_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FB_LOGIN_ID), JSONObject().put(Customer.FB_LOGIN_ID, "9223372036854775808"))
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_FB_LOGIN_ID), JSONObject().put(Customer.FB_LOGIN_ID, 1.0e19))
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun clearUserPii_dispatchesCall() {
        remoteCommand.parseCommands(arrayOf(Commands.CLEAR_USER_PII), JSONObject())
        assertEquals(1, mockInstance.clearUserPiiCallCount)
    }

    @Test
    fun setCurrencyCode_valid_callsSDK() {
        val payload = JSONObject().put(TransactionProperties.CURRENCY, "USD")
        remoteCommand.parseCommands(arrayOf(Commands.SET_CURRENCY_CODE), payload)
        assertEquals(1, mockInstance.setCurrencyCodeCallCount)
        assertEquals("USD", mockInstance.setCurrencyCodeParam)
    }

    @Test
    fun setCurrencyCode_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_CURRENCY_CODE), JSONObject())
        assertEquals(0, mockInstance.setCurrencyCodeCallCount)
    }

    @Test
    fun setCustomerId_valid_callsSDK() {
        val payload = JSONObject().put(Customer.USER_ID, "1234")
        remoteCommand.parseCommands(arrayOf(Commands.SET_CUSTOMER_ID), payload)
        assertEquals(1, mockInstance.setCustomerIdCallCount)
        assertEquals("1234", mockInstance.setCustomerIdParam)
    }

    @Test
    fun setCustomerId_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_CUSTOMER_ID), JSONObject())
        assertEquals(0, mockInstance.setCustomerIdCallCount)
    }

    @Test
    fun setUserPhone_valid_callsSDK() {
        val payload = JSONObject()
            .put(PhoneNumberParam.COUNTRY_CODE, "1")
            .put(PhoneNumberParam.PHONE_NUMBER, "2345678900")
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_PHONE), payload)
        assertEquals(1, mockInstance.setUserPhoneCallCount)
        assertEquals("1", mockInstance.setUserPhoneCountryCodeParam)
        assertEquals("2345678900", mockInstance.setUserPhoneNumberParam)
    }

    @Test
    fun setUserPhone_missingCountryCode_skipsCall() {
        val payload = JSONObject().put(PhoneNumberParam.PHONE_NUMBER, "2345678900")
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_PHONE), payload)
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserPhone_missingPhoneNumber_skipsCall() {
        val payload = JSONObject().put(PhoneNumberParam.COUNTRY_CODE, "1")
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_PHONE), payload)
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserPhone_bothMissing_reportsCountryCodeFirst() {
        val logger = mockk<RemoteCommandLogger>(relaxed = true)
        val command = AppsFlyerRemoteCommand(
            mockApplication,
            AppsFlyerSessionMode.AUTOMATIC,
            "testKey",
            AppsFlyerRemoteCommand.DEFAULT_COMMAND_ID,
            AppsFlyerRemoteCommand.DEFAULT_COMMAND_DESCRIPTION,
            logger
        )
        command.appsFlyerInstance = mockInstance

        command.parseCommands(arrayOf(Commands.SET_USER_PHONE), JSONObject())

        verify(exactly = 1) { logger.error(match { it.contains(PhoneNumberParam.COUNTRY_CODE) }) }
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserPhone_numericPhoneNumber_skipsCall() {
        val payload = JSONObject()
            .put(PhoneNumberParam.COUNTRY_CODE, "1")
            .put(PhoneNumberParam.PHONE_NUMBER, 2345678900L)
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_PHONE), payload)
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun setUserPhone_numericCountryCode_skipsCall() {
        val payload = JSONObject()
            .put(PhoneNumberParam.COUNTRY_CODE, 1)
            .put(PhoneNumberParam.PHONE_NUMBER, "2345678900")
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_PHONE), payload)
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun logAdRevenue_allRequiredParams_dispatchesCall() {
        val payload = JSONObject()
            .put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
            .put(AdRevenueParams.MEDIATION_NETWORK, "googleadmob")
            .put(AdRevenueParams.AD_REVENUE_CURRENCY, "USD")
            .put(AdRevenueParams.AD_REVENUE_AMOUNT, 1.99)

        remoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)

        assertEquals(1, mockInstance.logAdRevenueCallCount)
        assertNotNull(mockInstance.logAdRevenueDataParam)
    }

    @Test
    fun logAdRevenue_withAdditionalParams_forwardsThem() {
        val additional = JSONObject().put("param1", "value1")
        val payload = JSONObject()
            .put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
            .put(AdRevenueParams.MEDIATION_NETWORK, "googleadmob")
            .put(AdRevenueParams.AD_REVENUE_CURRENCY, "USD")
            .put(AdRevenueParams.AD_REVENUE_AMOUNT, 1.99)
            .put(AdRevenueParams.AD_REVENUE_ADDITIONAL_PARAMS, additional)

        remoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)

        assertEquals(mapOf("param1" to "value1"), mockInstance.logAdRevenueAdditionalParamsParam)
    }

    @Test
    fun logAdRevenue_missingMonetizationNetwork_skipsCall() {
        val payload = JSONObject()
            .put(AdRevenueParams.MEDIATION_NETWORK, "googleadmob")
            .put(AdRevenueParams.AD_REVENUE_CURRENCY, "USD")
            .put(AdRevenueParams.AD_REVENUE_AMOUNT, 1.99)
        remoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)
        assertEquals(0, mockInstance.logAdRevenueCallCount)
    }

    @Test
    fun logAdRevenue_missingMediationNetwork_skipsCall() {
        val payload = JSONObject()
            .put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
            .put(AdRevenueParams.AD_REVENUE_CURRENCY, "USD")
            .put(AdRevenueParams.AD_REVENUE_AMOUNT, 1.99)
        remoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)
        assertEquals(0, mockInstance.logAdRevenueCallCount)
    }

    @Test
    fun logAdRevenue_missingCurrency_skipsCall() {
        val payload = JSONObject()
            .put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
            .put(AdRevenueParams.MEDIATION_NETWORK, "googleadmob")
            .put(AdRevenueParams.AD_REVENUE_AMOUNT, 1.99)
        remoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)
        assertEquals(0, mockInstance.logAdRevenueCallCount)
    }

    @Test
    fun logAdRevenue_missingRevenueAmount_skipsCall() {
        val payload = JSONObject()
            .put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
            .put(AdRevenueParams.MEDIATION_NETWORK, "googleadmob")
            .put(AdRevenueParams.AD_REVENUE_CURRENCY, "USD")
        remoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)
        assertEquals(0, mockInstance.logAdRevenueCallCount)
    }

    @Test
    fun logAdRevenue_unknownMediationNetwork_skipsCall() {
        val payload = JSONObject()
            .put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
            .put(AdRevenueParams.MEDIATION_NETWORK, "not_a_network")
            .put(AdRevenueParams.AD_REVENUE_CURRENCY, "USD")
            .put(AdRevenueParams.AD_REVENUE_AMOUNT, 1.99)
        remoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)
        assertEquals(0, mockInstance.logAdRevenueCallCount)
    }

    @Test
    fun setConsentData_allFlagsProvided_dispatchesCall() {
        val payload = JSONObject()
            .put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, true)
            .put(ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE, false)
            .put(ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION, true)
            .put(ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE, false)

        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)

        assertEquals(1, mockInstance.setConsentDataCallCount)
        assertEquals(true, mockInstance.setConsentGdprParam)
        assertEquals(false, mockInstance.setConsentDataUsageParam)
        assertEquals(true, mockInstance.setConsentAdsPersonalizationParam)
        assertEquals(false, mockInstance.setConsentAdStorageParam)
    }

    @Test
    fun setConsentData_missingGdprFlag_skipsCall() {
        val payload = JSONObject()
            .put(ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE, true)
            .put(ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION, true)
            .put(ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE, true)
        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)
        assertEquals(0, mockInstance.setConsentDataCallCount)
    }

    @Test
    fun setConsentData_nonBooleanGdprFlag_skipsCallWithoutThrowing() {
        val payload = JSONObject()
            .put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, "yes")
        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)
        assertEquals(0, mockInstance.setConsentDataCallCount)
    }

    @Test
    fun setConsentData_nonBooleanOptionalFlags_forwardedAsNull() {
        val payload = JSONObject()
            .put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, true)
            .put(ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE, "maybe")
            .put(ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION, 1)
            .put(ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE, JSONObject.NULL)

        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)

        assertEquals(1, mockInstance.setConsentDataCallCount)
        assertEquals(true, mockInstance.setConsentGdprParam)
        assertNull(mockInstance.setConsentDataUsageParam)
        assertNull(mockInstance.setConsentAdsPersonalizationParam)
        assertNull(mockInstance.setConsentAdStorageParam)
    }

    @Test
    fun setConsentData_stringBooleanFlags_areCoerced() {
        val payload = JSONObject()
            .put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, "TRUE")
            .put(ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE, "true")
            .put(ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION, "False")
            .put(ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE, "false")

        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)

        assertEquals(1, mockInstance.setConsentDataCallCount)
        assertEquals(true, mockInstance.setConsentGdprParam)
        assertEquals(true, mockInstance.setConsentDataUsageParam)
        assertEquals(false, mockInstance.setConsentAdsPersonalizationParam)
        assertEquals(false, mockInstance.setConsentAdStorageParam)
    }

    @Test
    fun setConsentData_stringFalseGdprFlag_dispatchesCallWithFalse() {
        val payload = JSONObject()
            .put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, "false")

        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)

        assertEquals(1, mockInstance.setConsentDataCallCount)
        assertEquals(false, mockInstance.setConsentGdprParam)
    }

    @Test
    fun setConsentData_numericGdprFlag_skipsCall() {
        val payload = JSONObject()
            .put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, 1)
        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)
        assertEquals(0, mockInstance.setConsentDataCallCount)
    }

    @Test
    fun parseCommands_unexpectedException_logsAndContinuesWithNextCommand() {
        val logger = mockk<RemoteCommandLogger>(relaxed = true)
        val instance = mockk<AppsFlyerCommand>(relaxed = true)
        every { instance.clearUserPii() } throws IllegalStateException("boom")
        val command = AppsFlyerRemoteCommand(
            mockApplication,
            AppsFlyerSessionMode.AUTOMATIC,
            "testKey",
            AppsFlyerRemoteCommand.DEFAULT_COMMAND_ID,
            AppsFlyerRemoteCommand.DEFAULT_COMMAND_DESCRIPTION,
            logger
        )
        command.appsFlyerInstance = instance

        command.parseCommands(arrayOf(Commands.CLEAR_USER_PII, Commands.LOG_SESSION), JSONObject())

        verify(exactly = 1) { logger.error(match { it.contains("'${Commands.CLEAR_USER_PII}'") }, any<Throwable>()) }
        verify(exactly = 1) { instance.logSession() }
    }

    @Test
    fun setConsentData_onlyGdprFlag_dispatchesCallWithNullDetails() {
        val payload = JSONObject()
            .put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, false)

        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)

        assertEquals(1, mockInstance.setConsentDataCallCount)
        assertEquals(false, mockInstance.setConsentGdprParam)
        assertNull(mockInstance.setConsentDataUsageParam)
        assertNull(mockInstance.setConsentAdsPersonalizationParam)
        assertNull(mockInstance.setConsentAdStorageParam)
    }

    @Test
    fun setPartnerData_valid_callsSDK() {
        val partnerInfo = JSONObject().put("key1", "value1").put("key2", "value2")
        val payload = JSONObject()
            .put(PartnerDataParams.PARTNER_ID, "test_partner")
            .put(PartnerDataParams.PARTNER_INFO, partnerInfo)

        remoteCommand.parseCommands(arrayOf(Commands.SET_PARTNER_DATA), payload)

        assertEquals(1, mockInstance.setPartnerDataCallCount)
        assertEquals("test_partner", mockInstance.setPartnerDataIdParam)
        assertEquals(mapOf("key1" to "value1", "key2" to "value2"), mockInstance.setPartnerDataInfoParam)
    }

    @Test
    fun setPartnerData_missingId_skipsCall() {
        val payload = JSONObject().put(PartnerDataParams.PARTNER_INFO, JSONObject().put("k", "v"))
        remoteCommand.parseCommands(arrayOf(Commands.SET_PARTNER_DATA), payload)
        assertEquals(0, mockInstance.setPartnerDataCallCount)
    }

    @Test
    fun setPartnerData_missingInfo_forwardsEmptyMap() {
        val payload = JSONObject().put(PartnerDataParams.PARTNER_ID, "test_partner")
        remoteCommand.parseCommands(arrayOf(Commands.SET_PARTNER_DATA), payload)
        assertEquals(1, mockInstance.setPartnerDataCallCount)
        assertEquals(emptyMap<String, Any>(), mockInstance.setPartnerDataInfoParam)
    }

    @Test
    fun setSharingFilter_valid_callsSDK() {
        val sharingFilter = JSONArray().put("partner1").put("partner2")
        val payload = JSONObject().put(SharingFilterParams.SHARING_FILTER, sharingFilter)

        remoteCommand.parseCommands(arrayOf(Commands.SET_SHARING_FILTER_FOR_PARTNERS), payload)

        assertEquals(1, mockInstance.setSharingFilterForPartnersCallCount)
        assertArrayEquals(arrayOf("partner1", "partner2"), mockInstance.setSharingFilterForPartnersParam)
    }

    @Test
    fun setSharingFilter_missingArray_resetsFilter() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_SHARING_FILTER_FOR_PARTNERS), JSONObject())
        assertEquals(1, mockInstance.setSharingFilterForPartnersCallCount)
        assertNull(mockInstance.setSharingFilterForPartnersParam)
    }

    @Test
    fun setSharingFilter_jsonNull_resetsFilter() {
        val payload = JSONObject().put(SharingFilterParams.SHARING_FILTER, JSONObject.NULL)
        remoteCommand.parseCommands(arrayOf(Commands.SET_SHARING_FILTER_FOR_PARTNERS), payload)
        assertEquals(1, mockInstance.setSharingFilterForPartnersCallCount)
        assertNull(mockInstance.setSharingFilterForPartnersParam)
    }

    @Test
    fun setSharingFilter_wrongType_doesNotResetFilter() {
        val payload = JSONObject().put(SharingFilterParams.SHARING_FILTER, "all")
        remoteCommand.parseCommands(arrayOf(Commands.SET_SHARING_FILTER_FOR_PARTNERS), payload)
        assertEquals(0, mockInstance.setSharingFilterForPartnersCallCount)
    }

    @Test
    fun anonymizeUser_true_callsSDK() {
        val payload = JSONObject().put(Tracking.ANONYMIZE_USER, true)
        remoteCommand.parseCommands(arrayOf(Commands.ANONYMIZE_USER), payload)
        assertEquals(1, mockInstance.anonymizeUserCallCount)
        assertEquals(true, mockInstance.anonymizeUserParam)
    }

    @Test
    fun anonymizeUser_missingFlag_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.ANONYMIZE_USER), JSONObject())
        assertEquals(0, mockInstance.anonymizeUserCallCount)
    }

    @Test
    fun anonymizeUser_stringTrue_callsSDK() {
        val payload = JSONObject().put(Tracking.ANONYMIZE_USER, "TRUE")
        remoteCommand.parseCommands(arrayOf(Commands.ANONYMIZE_USER), payload)
        assertEquals(1, mockInstance.anonymizeUserCallCount)
        assertEquals(true, mockInstance.anonymizeUserParam)
    }

    @Test
    fun anonymizeUser_wrongType_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.ANONYMIZE_USER), JSONObject().put(Tracking.ANONYMIZE_USER, "yes"))
        remoteCommand.parseCommands(arrayOf(Commands.ANONYMIZE_USER), JSONObject().put(Tracking.ANONYMIZE_USER, 1))
        assertEquals(0, mockInstance.anonymizeUserCallCount)
    }

    @Test
    fun disableDeviceTracking_aliasCallsAnonymizeUser() {
        val payload = JSONObject().put(Tracking.ANONYMIZE_USER, true)
        remoteCommand.parseCommands(arrayOf(Commands.DISABLE_DEVICE_TRACKING), payload)
        assertEquals(1, mockInstance.anonymizeUserCallCount)
        assertEquals(true, mockInstance.anonymizeUserParam)
    }

    @Test
    fun resolveDeepLinkUrls_valid_callsSDK() {
        val urls = JSONArray().put("val1").put("val2").put("val3")
        val payload = JSONObject().put(DeepLink.URLS, urls)

        remoteCommand.parseCommands(arrayOf(Commands.RESOLVE_DEEPLINK_URLS), payload)

        assertEquals(1, mockInstance.resolveDeepLinkUrlsCallCount)
        assertEquals(listOf("val1", "val2", "val3"), mockInstance.resolveDeepLinkUrlsParam)
    }

    @Test
    fun resolveDeepLinkUrls_legacyKey_dispatchesCall() {
        val urls = JSONArray().put("click.example.com").put("email.example.com")
        val payload = JSONObject().put(DeepLink.URLS_LEGACY_TIQ, urls)

        remoteCommand.parseCommands(arrayOf(Commands.RESOLVE_DEEPLINK_URLS), payload)

        assertEquals(1, mockInstance.resolveDeepLinkUrlsCallCount)
        assertEquals(listOf("click.example.com", "email.example.com"), mockInstance.resolveDeepLinkUrlsParam)
    }

    @Test
    fun resolveDeepLinkUrls_missingArray_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.RESOLVE_DEEPLINK_URLS), JSONObject())
        assertEquals(0, mockInstance.resolveDeepLinkUrlsCallCount)
    }

    @Test
    fun setContext_calledTwice_keepsFirstInstanceAndContext() {
        val logger = mockk<RemoteCommandLogger>(relaxed = true)
        val command = AppsFlyerRemoteCommand(
            mockApplication,
            AppsFlyerSessionMode.AUTOMATIC,
            "testKey",
            AppsFlyerRemoteCommand.DEFAULT_COMMAND_ID,
            AppsFlyerRemoteCommand.DEFAULT_COMMAND_DESCRIPTION,
            logger
        )
        val firstContext = mockk<RemoteCommandContext>()
        command.setContext(firstContext)
        val firstInstance = command.appsFlyerInstance

        command.setContext(mockk())

        assertSame(firstInstance, command.appsFlyerInstance)
        assertSame(firstContext, command.context)
        verify(exactly = 1) { logger.warning(match { it.contains("RemoteCommandContext") }) }
    }

    @Test
    fun publicConstructor_defaultLogLevel_isError() {
        val command = AppsFlyerRemoteCommand(mockApplication, AppsFlyerSessionMode.APP_MANAGED)
        assertEquals(RemoteCommandLogLevel.ERROR, command.logger.logLevel)
    }

    @Test
    fun stopTracking_true_callsSDK() {
        val payload = JSONObject().put(Tracking.STOP_TRACKING, true)
        remoteCommand.parseCommands(arrayOf(Commands.STOP_TRACKING), payload)
        assertEquals(1, mockInstance.stopTrackingCallCount)
        assertEquals(true, mockInstance.stopTrackingParam)
    }

    @Test
    fun stopTracking_false_callsSDK() {
        val payload = JSONObject().put(Tracking.STOP_TRACKING, false)
        remoteCommand.parseCommands(arrayOf(Commands.STOP_TRACKING), payload)
        assertEquals(1, mockInstance.stopTrackingCallCount)
        assertEquals(false, mockInstance.stopTrackingParam)
    }

    @Test
    fun stopTracking_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.STOP_TRACKING), JSONObject())
        assertEquals(0, mockInstance.stopTrackingCallCount)
    }

    @Test
    fun stopTracking_wrongType_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.STOP_TRACKING), JSONObject().put(Tracking.STOP_TRACKING, "yes"))
        remoteCommand.parseCommands(arrayOf(Commands.STOP_TRACKING), JSONObject().put(Tracking.STOP_TRACKING, 1))
        assertEquals(0, mockInstance.stopTrackingCallCount)
    }

    @Test
    fun disableTracking_aliasCallsStopTracking() {
        val payload = JSONObject().put(Tracking.STOP_TRACKING, true)
        remoteCommand.parseCommands(arrayOf(Commands.DISABLE_TRACKING), payload)
        assertEquals(1, mockInstance.stopTrackingCallCount)
        assertEquals(true, mockInstance.stopTrackingParam)
    }

    @Test
    fun logSession_dispatchesCall() {
        remoteCommand.parseCommands(arrayOf(Commands.LOG_SESSION), JSONObject())
        assertEquals(1, mockInstance.logSessionCallCount)
    }

    @Test
    fun setOaid_valid_callsSDK() {
        val payload = JSONObject().put(StringCommandParams.OAID, "test-oaid-123")
        remoteCommand.parseCommands(arrayOf(Commands.SET_OAID), payload)
        assertEquals(1, mockInstance.setOaidCallCount)
        assertEquals("test-oaid-123", mockInstance.setOaidParam)
    }

    @Test
    fun setOaid_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_OAID), JSONObject())
        assertEquals(0, mockInstance.setOaidCallCount)
    }

    @Test
    fun setAndroidId_valid_callsSDK() {
        val payload = JSONObject().put(StringCommandParams.ANDROID_ID, "test_android_id")
        remoteCommand.parseCommands(arrayOf(Commands.SET_ANDROID_ID), payload)
        assertEquals(1, mockInstance.setAndroidIdCallCount)
        assertEquals("test_android_id", mockInstance.setAndroidIdParam)
    }

    @Test
    fun setAndroidId_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_ANDROID_ID), JSONObject())
        assertEquals(0, mockInstance.setAndroidIdCallCount)
    }

    @Test
    fun setImei_valid_callsSDK() {
        val payload = JSONObject().put(StringCommandParams.IMEI, "123456789012345")
        remoteCommand.parseCommands(arrayOf(Commands.SET_IMEI), payload)
        assertEquals(1, mockInstance.setImeiCallCount)
        assertEquals("123456789012345", mockInstance.setImeiParam)
    }

    @Test
    fun setImei_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_IMEI), JSONObject())
        assertEquals(0, mockInstance.setImeiCallCount)
    }

    @Test
    fun setOutOfStore_valid_callsSDK() {
        val payload = JSONObject().put(StringCommandParams.STORE_NAME, "amazon")
        remoteCommand.parseCommands(arrayOf(Commands.SET_OUT_OF_STORE), payload)
        assertEquals(1, mockInstance.setOutOfStoreCallCount)
        assertEquals("amazon", mockInstance.setOutOfStoreParam)
    }

    @Test
    fun setOutOfStore_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_OUT_OF_STORE), JSONObject())
        assertEquals(0, mockInstance.setOutOfStoreCallCount)
    }

    @Test
    fun setDisableNetworkData_true_callsSDK() {
        val payload = JSONObject().put(StringCommandParams.DISABLE_NETWORK_DATA, true)
        remoteCommand.parseCommands(arrayOf(Commands.SET_DISABLE_NETWORK_DATA), payload)
        assertEquals(1, mockInstance.setDisableNetworkDataCallCount)
        assertEquals(true, mockInstance.setDisableNetworkDataParam)
    }

    @Test
    fun setDisableNetworkData_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_DISABLE_NETWORK_DATA), JSONObject())
        assertEquals(0, mockInstance.setDisableNetworkDataCallCount)
    }

    @Test
    fun setAppInviteOneLink_valid_callsSDK() {
        val payload = JSONObject().put(StringCommandParams.APP_INVITE_ONE_LINK_ID, "XY1A")
        remoteCommand.parseCommands(arrayOf(Commands.SET_APP_INVITE_ONE_LINK), payload)
        assertEquals(1, mockInstance.setAppInviteOneLinkCallCount)
        assertEquals("XY1A", mockInstance.setAppInviteOneLinkParam)
    }

    @Test
    fun setAppInviteOneLink_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_APP_INVITE_ONE_LINK), JSONObject())
        assertEquals(0, mockInstance.setAppInviteOneLinkCallCount)
    }

    @Test
    fun setIsUpdate_true_callsSDK() {
        val payload = JSONObject().put(StringCommandParams.IS_UPDATE, true)
        remoteCommand.parseCommands(arrayOf(Commands.SET_IS_UPDATE), payload)
        assertEquals(1, mockInstance.setIsUpdateCallCount)
        assertEquals(true, mockInstance.setIsUpdateParam)
    }

    @Test
    fun setIsUpdate_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_IS_UPDATE), JSONObject())
        assertEquals(0, mockInstance.setIsUpdateCallCount)
    }

    @Test
    fun setPreinstallAttribution_valid_callsSDK() {
        val payload = JSONObject()
            .put(PreinstallParams.MEDIA_SOURCE, "email")
            .put(PreinstallParams.CAMPAIGN, "summer_promo")
            .put(PreinstallParams.SITE_ID, "site_001")

        remoteCommand.parseCommands(arrayOf(Commands.SET_PREINSTALL_ATTRIBUTION), payload)

        assertEquals(1, mockInstance.setPreinstallAttributionCallCount)
        assertEquals("email", mockInstance.setPreinstallMediaSourceParam)
        assertEquals("summer_promo", mockInstance.setPreinstallCampaignParam)
        assertEquals("site_001", mockInstance.setPreinstallSiteIdParam)
    }

    @Test
    fun setPreinstallAttribution_missingMediaSource_skipsCall() {
        val payload = JSONObject()
            .put(PreinstallParams.CAMPAIGN, "summer_promo")
            .put(PreinstallParams.SITE_ID, "site_001")
        remoteCommand.parseCommands(arrayOf(Commands.SET_PREINSTALL_ATTRIBUTION), payload)
        assertEquals(0, mockInstance.setPreinstallAttributionCallCount)
    }

    @Test
    fun setPreinstallAttribution_missingCampaign_skipsCall() {
        val payload = JSONObject()
            .put(PreinstallParams.MEDIA_SOURCE, "email")
            .put(PreinstallParams.SITE_ID, "site_001")
        remoteCommand.parseCommands(arrayOf(Commands.SET_PREINSTALL_ATTRIBUTION), payload)
        assertEquals(0, mockInstance.setPreinstallAttributionCallCount)
    }

    @Test
    fun setPreinstallAttribution_missingSiteId_skipsCall() {
        val payload = JSONObject()
            .put(PreinstallParams.MEDIA_SOURCE, "email")
            .put(PreinstallParams.CAMPAIGN, "summer_promo")
        remoteCommand.parseCommands(arrayOf(Commands.SET_PREINSTALL_ATTRIBUTION), payload)
        assertEquals(0, mockInstance.setPreinstallAttributionCallCount)
    }

    @Test
    fun unknownCommand_mappedToStandardEvent() {
        val payload = JSONObject().put(Commands.COMMAND_KEY, "purchase")
        remoteCommand.parseCommands(arrayOf("purchase"), payload)

        assertEquals(1, mockInstance.trackEventCallCount)
        // "purchase" maps to AFInAppEventType.PURCHASE via StandardEvents.eventNames.
        assertEquals(StandardEvents.eventNames["purchase"], mockInstance.trackEventTypeParam)
    }

    @Test
    fun customEventName_preservesCase() {
        val payload = JSONObject().put(Commands.COMMAND_KEY, " MyEvent ")
        remoteCommand.parseCommands(remoteCommand.splitCommands(payload), payload)

        assertEquals(1, mockInstance.trackEventCallCount)
        assertEquals("MyEvent", mockInstance.trackEventTypeParam)
    }

    @Test
    fun cancelPurchase_passesThroughUnchanged() {
        val payload = JSONObject().put(Commands.COMMAND_KEY, "cancel_purchase")
        remoteCommand.parseCommands(remoteCommand.splitCommands(payload), payload)

        assertEquals(1, mockInstance.trackEventCallCount)
        assertEquals("cancel_purchase", mockInstance.trackEventTypeParam)
    }

    @Test
    fun standardEventName_isMatchedCaseInsensitively() {
        val payload = JSONObject().put(Commands.COMMAND_KEY, "AddToCart")
        remoteCommand.parseCommands(remoteCommand.splitCommands(payload), payload)

        assertEquals(1, mockInstance.trackEventCallCount)
        assertEquals(AFInAppEventType.ADD_TO_CART, mockInstance.trackEventTypeParam)
    }

    @Test
    fun builtInCommand_isMatchedCaseInsensitivelyThroughSplit() {
        val payload = JSONObject()
            .put(Commands.COMMAND_KEY, "SetCustomerId")
            .put(Customer.USER_ID, "user-1")
        remoteCommand.parseCommands(remoteCommand.splitCommands(payload), payload)

        assertEquals(1, mockInstance.setCustomerIdCallCount)
        assertEquals("user-1", mockInstance.setCustomerIdParam)
        assertEquals(0, mockInstance.trackEventCallCount)
    }

    @Test
    fun unknownCommand_dispatchedAsCustomEvent() {
        val payload = JSONObject().put(Commands.COMMAND_KEY, "my_custom_event")
        remoteCommand.parseCommands(arrayOf("my_custom_event"), payload)

        assertEquals(1, mockInstance.trackEventCallCount)
        assertEquals("my_custom_event", mockInstance.trackEventTypeParam)
    }

    @Test
    fun trackEvent_eventParametersKey_isForwarded() {
        val params = JSONObject()
            .put(AFInAppEventParameterName.LEVEL, 5)
            .put(AFInAppEventParameterName.SCORE, 500)
        val payload = JSONObject().put(StandardEvents.EVENT_PARAMETERS, params)

        remoteCommand.parseCommands(arrayOf("levelachieved"), payload)

        assertEquals(AFInAppEventType.LEVEL_ACHIEVED, mockInstance.trackEventTypeParam)
        assertEquals(
            mapOf(AFInAppEventParameterName.LEVEL to 5, AFInAppEventParameterName.SCORE to 500),
            mockInstance.trackEventParametersParam
        )
    }

    @Test
    fun trackEvent_shortEventParametersKey_isForwarded() {
        val params = JSONObject().put(AFInAppEventParameterName.LEVEL, 1)
        val payload = JSONObject().put(StandardEvents.EVENT_PARAMETERS_SHORT, params)

        remoteCommand.parseCommands(arrayOf("levelachieved"), payload)

        assertEquals(mapOf(AFInAppEventParameterName.LEVEL to 1), mockInstance.trackEventParametersParam)
    }

    @Test
    fun trackEvent_prefersLongEventParametersKeyOverShort() {
        val longParams = JSONObject().put("event_params_long", "value")
        val shortParams = JSONObject().put("event_params_short", "value")
        val payload = JSONObject()
            .put(StandardEvents.EVENT_PARAMETERS, longParams)
            .put(StandardEvents.EVENT_PARAMETERS_SHORT, shortParams)

        remoteCommand.parseCommands(arrayOf("custom"), payload)

        assertEquals(mapOf("event_params_long" to "value"), mockInstance.trackEventParametersParam)
    }

    @Test
    fun trackEvent_fallsBackToPayloadWhenNoParamsKey() {
        val payload = JSONObject().put("af_data", "12345")
        remoteCommand.parseCommands(arrayOf("levelachieved"), payload)

        assertEquals(mapOf<String, Any>("af_data" to "12345"), mockInstance.trackEventParametersParam)
    }

    @Test
    fun trackEvent_fallback_filtersReservedKeys() {
        val payload = JSONObject()
            .put("method", "12345")
            .put(Settings.DEBUG, true)
            .put(Config.DEV_KEY, "12345")
            .put(Config.SETTINGS, "12345")
            .put(Commands.COMMAND_KEY, "12345")
            .put(Config.APP_ID, "12345")

        remoteCommand.parseCommands(arrayOf("levelachieved"), payload)

        assertEquals(emptyMap<String, Any>(), mockInstance.trackEventParametersParam)
    }

    @Test
    fun blankCommand_isIgnored() {
        remoteCommand.parseCommands(arrayOf(" ", "\t", ""), JSONObject())
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun blankCommand_doesNotAffectOtherCommands() {
        val payload = JSONObject().put(Tracking.STOP_TRACKING, false)
        remoteCommand.parseCommands(arrayOf(" ", Commands.STOP_TRACKING), payload)
        assertEquals(0, mockInstance.trackEventCallCount)
        assertEquals(1, mockInstance.stopTrackingCallCount)
        assertEquals(false, mockInstance.stopTrackingParam)
    }

    @Test
    fun multipleCommandsInSingleBatch_areAllDispatched() {
        val payload = JSONObject()
            .put(TransactionProperties.CURRENCY, "USD")
            .put(Customer.USER_ID, "abc")
            .put(Tracking.STOP_TRACKING, true)

        remoteCommand.parseCommands(
            arrayOf(Commands.SET_CURRENCY_CODE, Commands.SET_CUSTOMER_ID, Commands.STOP_TRACKING),
            payload
        )

        assertEquals(1, mockInstance.setCurrencyCodeCallCount)
        assertEquals(1, mockInstance.setCustomerIdCallCount)
        assertEquals(1, mockInstance.stopTrackingCallCount)
    }

    @Test
    fun failedValidation_doesNotBlockSubsequentCommands() {
        val payload = JSONObject()
            // Missing Customer.USER_ID — SET_CUSTOMER_ID must throw and be swallowed.
            .put(Tracking.STOP_TRACKING, true)

        remoteCommand.parseCommands(
            arrayOf(Commands.SET_CUSTOMER_ID, Commands.STOP_TRACKING),
            payload
        )

        assertEquals(0, mockInstance.setCustomerIdCallCount)
        assertEquals(1, mockInstance.stopTrackingCallCount)
    }

    @Test
    fun mixedCaseCommand_resolvesBuiltInCommand() {
        // The Command.fromString resolver accepts arbitrary casing.
        val payload = JSONObject().put(TransactionProperties.CURRENCY, "EUR")
        remoteCommand.parseCommands(arrayOf("SetCurrencyCode"), payload)
        assertEquals(1, mockInstance.setCurrencyCodeCallCount)
        assertEquals("EUR", mockInstance.setCurrencyCodeParam)
    }

    @Test
    fun standardEvent_knownName_returnsMappedAfEvent() {
        assertEquals(AFInAppEventType.LEVEL_ACHIEVED, remoteCommand.standardEvent("levelachieved"))
    }

    @Test
    fun standardEvent_unknownName_returnsNull() {
        assertNull(remoteCommand.standardEvent("not_a_standard_event"))
    }

    @Test
    fun logLevel_constructor_setsLoggerLevel() {
        val debugLogger = RemoteCommandLogger(RemoteCommandLogLevel.DEBUG)
        val cmd = AppsFlyerRemoteCommand(mockApplication, AppsFlyerSessionMode.AUTOMATIC, "key", AppsFlyerRemoteCommand.DEFAULT_COMMAND_ID, AppsFlyerRemoteCommand.DEFAULT_COMMAND_DESCRIPTION, debugLogger)
        assertEquals(RemoteCommandLogLevel.DEBUG, cmd.logger.logLevel)
    }

    @Test
    fun anonymizeUser_false_callsSDK() {
        val payload = JSONObject().put(Tracking.ANONYMIZE_USER, false)
        remoteCommand.parseCommands(arrayOf(Commands.ANONYMIZE_USER), payload)
        assertEquals(1, mockInstance.anonymizeUserCallCount)
        assertEquals(false, mockInstance.anonymizeUserParam)
    }

    @Test
    fun setDisableNetworkData_false_callsSDK() {
        val payload = JSONObject().put(StringCommandParams.DISABLE_NETWORK_DATA, false)
        remoteCommand.parseCommands(arrayOf(Commands.SET_DISABLE_NETWORK_DATA), payload)
        assertEquals(1, mockInstance.setDisableNetworkDataCallCount)
        assertEquals(false, mockInstance.setDisableNetworkDataParam)
    }

    @Test
    fun setIsUpdate_false_callsSDK() {
        val payload = JSONObject().put(StringCommandParams.IS_UPDATE, false)
        remoteCommand.parseCommands(arrayOf(Commands.SET_IS_UPDATE), payload)
        assertEquals(1, mockInstance.setIsUpdateCallCount)
        assertEquals(false, mockInstance.setIsUpdateParam)
    }

    @Test
    fun logAdRevenue_allRequiredParams_forwardsCorrectFields() {
        val payload = JSONObject()
            .put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
            .put(AdRevenueParams.MEDIATION_NETWORK, "googleadmob")
            .put(AdRevenueParams.AD_REVENUE_CURRENCY, "EUR")
            .put(AdRevenueParams.AD_REVENUE_AMOUNT, 2.50)

        remoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)

        val data = mockInstance.logAdRevenueDataParam!!
        assertEquals("TestNetwork", data.monetizationNetwork)
        assertEquals(MediationNetwork.GOOGLE_ADMOB, data.mediationNetwork)
        assertEquals("EUR", data.currencyIso4217Code)
        assertEquals(2.50, data.revenue, 0.0001)
    }

    @Test
    fun logAdRevenue_missingAdditionalParams_forwardsEmptyMap() {
        val payload = JSONObject()
            .put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
            .put(AdRevenueParams.MEDIATION_NETWORK, "googleadmob")
            .put(AdRevenueParams.AD_REVENUE_CURRENCY, "USD")
            .put(AdRevenueParams.AD_REVENUE_AMOUNT, 1.0)

        remoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)

        assertEquals(1, mockInstance.logAdRevenueCallCount)
        assertEquals(emptyMap<String, Any>(), mockInstance.logAdRevenueAdditionalParamsParam)
    }

    // Small helper for array comparison — Kotlin's Assert.assertEquals doesn't
    // deep-compare arrays (it would use equals() on Array<*>).
    private fun assertArrayEquals(expected: Array<String>, actual: Array<String>?) {
        assertNotNull(actual)
        assertEquals(expected.toList(), actual!!.toList())
    }

    // Values that org.json's optString would have converted to a String in 1.4.0.
    private val nonStringValues: List<Any> =
        listOf(JSONObject.NULL, 123, true, JSONObject().put("k", "v"), JSONArray().put("v"))

    private fun assertNonStringRejected(command: String, key: String, others: JSONObject = JSONObject()) {
        for (value in nonStringValues) {
            val payload = JSONObject(others.toString()).put(key, value)
            remoteCommand.parseCommands(arrayOf(command), payload)
            assertTrue("$command accepted $key = $value", mockInstance.verifyNoCalls())
        }
    }

    @Test
    fun setHost_nonStringValue_skipsCall() {
        assertNonStringRejected(Commands.SET_HOST, Host.HOST, JSONObject().put(Host.HOST_PREFIX, "my_prefix"))
        assertNonStringRejected(Commands.SET_HOST, Host.HOST_PREFIX, JSONObject().put(Host.HOST, "example.com"))
    }

    @Test
    fun setHost_emptyStrings_callsSDK() {
        val payload = JSONObject().put(Host.HOST, "").put(Host.HOST_PREFIX, "")
        remoteCommand.parseCommands(arrayOf(Commands.SET_HOST), payload)
        assertEquals(1, mockInstance.setHostCallCount)
        assertEquals("", mockInstance.setHostHostParam)
        assertEquals("", mockInstance.setHostPrefixParam)
    }

    @Test
    fun setCurrencyCode_nonStringValue_skipsCall() {
        assertNonStringRejected(Commands.SET_CURRENCY_CODE, TransactionProperties.CURRENCY)
    }

    @Test
    fun setCurrencyCode_emptyString_callsSDK() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_CURRENCY_CODE), JSONObject().put(TransactionProperties.CURRENCY, ""))
        assertEquals(1, mockInstance.setCurrencyCodeCallCount)
        assertEquals("", mockInstance.setCurrencyCodeParam)
    }

    @Test
    fun setCustomerId_nonStringValue_skipsCall() {
        assertNonStringRejected(Commands.SET_CUSTOMER_ID, Customer.USER_ID)
    }

    @Test
    fun setCustomerId_emptyString_callsSDK() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_CUSTOMER_ID), JSONObject().put(Customer.USER_ID, ""))
        assertEquals(1, mockInstance.setCustomerIdCallCount)
        assertEquals("", mockInstance.setCustomerIdParam)
    }

    @Test
    fun logAdRevenue_nonStringValue_skipsCall() {
        val valid = JSONObject()
            .put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
            .put(AdRevenueParams.MEDIATION_NETWORK, "googleadmob")
            .put(AdRevenueParams.AD_REVENUE_CURRENCY, "USD")
            .put(AdRevenueParams.AD_REVENUE_AMOUNT, 1.99)
        assertNonStringRejected(Commands.LOG_AD_REVENUE, AdRevenueParams.MONETIZATION_NETWORK, valid)
        assertNonStringRejected(Commands.LOG_AD_REVENUE, AdRevenueParams.MEDIATION_NETWORK, valid)
        assertNonStringRejected(Commands.LOG_AD_REVENUE, AdRevenueParams.AD_REVENUE_CURRENCY, valid)
    }

    @Test
    fun setPartnerData_nonStringId_skipsCall() {
        assertNonStringRejected(Commands.SET_PARTNER_DATA, PartnerDataParams.PARTNER_ID)
    }

    @Test
    fun setOaid_nonStringValue_skipsCall() {
        assertNonStringRejected(Commands.SET_OAID, StringCommandParams.OAID)
    }

    @Test
    fun setAndroidId_nonStringValue_skipsCall() {
        assertNonStringRejected(Commands.SET_ANDROID_ID, StringCommandParams.ANDROID_ID)
    }

    @Test
    fun setImei_nonStringValue_skipsCall() {
        assertNonStringRejected(Commands.SET_IMEI, StringCommandParams.IMEI)
    }

    @Test
    fun setOutOfStore_nonStringValue_skipsCall() {
        assertNonStringRejected(Commands.SET_OUT_OF_STORE, StringCommandParams.STORE_NAME)
    }

    @Test
    fun setAppInviteOneLink_nonStringValue_skipsCall() {
        assertNonStringRejected(Commands.SET_APP_INVITE_ONE_LINK, StringCommandParams.APP_INVITE_ONE_LINK_ID)
    }

    @Test
    fun setPreinstallAttribution_nonStringValue_skipsCall() {
        val valid = JSONObject()
            .put(PreinstallParams.MEDIA_SOURCE, "email")
            .put(PreinstallParams.CAMPAIGN, "summer_promo")
            .put(PreinstallParams.SITE_ID, "site_001")
        assertNonStringRejected(Commands.SET_PREINSTALL_ATTRIBUTION, PreinstallParams.MEDIA_SOURCE, valid)
        assertNonStringRejected(Commands.SET_PREINSTALL_ATTRIBUTION, PreinstallParams.CAMPAIGN, valid)
        assertNonStringRejected(Commands.SET_PREINSTALL_ATTRIBUTION, PreinstallParams.SITE_ID, valid)
    }

    @Test
    fun setSharingFilter_nonStringElement_skipsCall() {
        for (element in listOf(5, JSONObject.NULL, true)) {
            val payload = JSONObject().put(SharingFilterParams.SHARING_FILTER, JSONArray().put("partner1").put(element))
            remoteCommand.parseCommands(arrayOf(Commands.SET_SHARING_FILTER_FOR_PARTNERS), payload)
        }
        assertEquals(0, mockInstance.setSharingFilterForPartnersCallCount)
    }

    @Test
    fun resolveDeepLinkUrls_nonStringElement_skipsCall() {
        for (key in listOf(DeepLink.URLS, DeepLink.URLS_LEGACY_TIQ)) {
            for (element in listOf(5, JSONObject.NULL, true)) {
                val payload = JSONObject().put(key, JSONArray().put("click.example.com").put(element))
                remoteCommand.parseCommands(arrayOf(Commands.RESOLVE_DEEPLINK_URLS), payload)
            }
        }
        assertEquals(0, mockInstance.resolveDeepLinkUrlsCallCount)
    }

    @Test
    fun resolveDeepLinkUrls_nonStringElement_errorNamesTheKeyUsed() {
        val logger = mockk<RemoteCommandLogger>(relaxed = true)
        val command = AppsFlyerRemoteCommand(
            mockApplication,
            AppsFlyerSessionMode.AUTOMATIC,
            "testKey",
            AppsFlyerRemoteCommand.DEFAULT_COMMAND_ID,
            AppsFlyerRemoteCommand.DEFAULT_COMMAND_DESCRIPTION,
            logger
        )
        command.appsFlyerInstance = mockInstance

        command.parseCommands(
            arrayOf(Commands.RESOLVE_DEEPLINK_URLS),
            JSONObject().put(DeepLink.URLS_LEGACY_TIQ, JSONArray().put(5))
        )

        verify(exactly = 1) { logger.error(match { it.contains(DeepLink.URLS_LEGACY_TIQ) }) }
    }

    @Test
    fun initialize_jsonNullDevKey_usesConstructorKey() {
        val payload = JSONObject().put(Config.DEV_KEY, JSONObject.NULL)
        remoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)
        assertEquals(1, mockInstance.initializeCallCount)
        assertEquals("testKey", mockInstance.initializeDevKeyParam)
    }

    @Test
    fun initialize_blankDevKey_usesConstructorKey() {
        val payload = JSONObject().put(Config.DEV_KEY, " ")
        remoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)
        assertEquals("testKey", mockInstance.initializeDevKeyParam)
    }

    @Test
    fun initialize_nonStringDevKey_skipsCall() {
        for (value in listOf(123, true, JSONObject(), JSONArray())) {
            remoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), JSONObject().put(Config.DEV_KEY, value))
        }
        assertEquals(0, mockInstance.initializeCallCount)
    }

    @Test
    fun initialize_jsonNullDevKeyAndNoConstructorKey_skipsCall() {
        val commandWithoutKey = AppsFlyerRemoteCommand(mockApplication, AppsFlyerSessionMode.AUTOMATIC, null)
        commandWithoutKey.appsFlyerInstance = mockInstance
        commandWithoutKey.parseCommands(arrayOf(Commands.INITIALIZE), JSONObject().put(Config.DEV_KEY, JSONObject.NULL))
        assertEquals(0, mockInstance.initializeCallCount)
    }

    @Suppress("unused")
    private fun assertFalseStub(msg: String, value: Boolean) = assertFalse(msg, value)
}
