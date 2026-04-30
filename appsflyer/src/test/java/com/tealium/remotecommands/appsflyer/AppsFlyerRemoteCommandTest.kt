package com.tealium.remotecommands.appsflyer

import android.app.Application
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.MediationNetwork
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @MockK
    lateinit var mockApplication: Application

    private lateinit var mockInstance: MockAppsFlyerInstance
    private lateinit var remoteCommand: AppsFlyerRemoteCommand

    @Before
    fun setUp() {
        mockInstance = MockAppsFlyerInstance()
        remoteCommand = AppsFlyerRemoteCommand(mockApplication, "testKey")
        remoteCommand.appsFlyerInstance = mockInstance
    }

    @Test
    fun splitCommands_splitsAndNormalises() {
        val json = JSONObject()
        json.put(Commands.COMMAND_KEY, "initialize, LogPurchase, add_to_cart")

        val commands = remoteCommand.splitCommands(json)

        assertEquals(3, commands.size)
        assertEquals("initialize", commands[0])
        assertEquals("logpurchase", commands[1])
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
        val commandWithoutKey = AppsFlyerRemoteCommand(mockApplication, null)
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
    fun setUserEmails_validCryptTypeNone_dispatchesCall() {
        val emails = JSONArray().put("a@x.com").put("b@x.com")
        val payload = JSONObject()
            .put(Customer.EMAILS, emails)
            .put(Customer.EMAIL_HASH_TYPE, 0)

        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAILS), payload)

        assertEquals(1, mockInstance.setUserEmailsCallCount)
        assertEquals(listOf("a@x.com", "b@x.com"), mockInstance.setUserEmailsParam)
        assertEquals(0, mockInstance.setUserEmailsCryptTypeParam)
    }

    @Test
    fun setUserEmails_validCryptTypeSha256_dispatchesCall() {
        val emails = JSONArray().put("a@x.com")
        val payload = JSONObject()
            .put(Customer.EMAILS, emails)
            .put(Customer.EMAIL_HASH_TYPE, 3)

        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAILS), payload)

        assertEquals(1, mockInstance.setUserEmailsCallCount)
        assertEquals(3, mockInstance.setUserEmailsCryptTypeParam)
    }

    @Test
    fun setUserEmails_missingArray_skipsCall() {
        val payload = JSONObject().put(Customer.EMAIL_HASH_TYPE, 0)
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAILS), payload)
        assertEquals(0, mockInstance.setUserEmailsCallCount)
    }

    @Test
    fun setUserEmails_missingCryptType_skipsCall() {
        val emails = JSONArray().put("a@x.com")
        val payload = JSONObject().put(Customer.EMAILS, emails)
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAILS), payload)
        assertEquals(0, mockInstance.setUserEmailsCallCount)
    }

    @Test
    fun setUserEmails_invalidCryptType_skipsCall() {
        val emails = JSONArray().put("a@x.com")
        val payload = JSONObject()
            .put(Customer.EMAILS, emails)
            .put(Customer.EMAIL_HASH_TYPE, 99)
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAILS), payload)
        assertEquals(0, mockInstance.setUserEmailsCallCount)
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
    fun setPhoneNumber_valid_callsSDK() {
        val payload = JSONObject().put(PhoneNumberParam.PHONE_NUMBER, "+1234567890")
        remoteCommand.parseCommands(arrayOf(Commands.SET_PHONE_NUMBER), payload)
        assertEquals(1, mockInstance.setPhoneNumberCallCount)
        assertEquals("+1234567890", mockInstance.setPhoneNumberParam)
    }

    @Test
    fun setPhoneNumber_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_PHONE_NUMBER), JSONObject())
        assertEquals(0, mockInstance.setPhoneNumberCallCount)
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
    fun setConsentData_missingDataUsage_skipsCall() {
        val payload = JSONObject()
            .put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, true)
            .put(ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION, true)
            .put(ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE, true)
        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)
        assertEquals(0, mockInstance.setConsentDataCallCount)
    }

    @Test
    fun setConsentData_missingAdsPersonalization_skipsCall() {
        val payload = JSONObject()
            .put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, true)
            .put(ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE, true)
            .put(ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE, true)
        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)
        assertEquals(0, mockInstance.setConsentDataCallCount)
    }

    @Test
    fun setConsentData_missingAdStorage_skipsCall() {
        val payload = JSONObject()
            .put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, true)
            .put(ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE, true)
            .put(ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION, true)
        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)
        assertEquals(0, mockInstance.setConsentDataCallCount)
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
    fun start_dispatchesCall() {
        remoteCommand.parseCommands(arrayOf(Commands.START), JSONObject())
        assertEquals(1, mockInstance.startCallCount)
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
    fun mixedCaseCommand_isNormalisedViaSplit() {
        // When clients call parseCommands directly they must pre-normalise, but the
        // Command.fromString resolver accepts arbitrary casing.
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
        AppsFlyerRemoteCommand(mockApplication, "key", logLevel = RemoteCommandLogLevel.DEBUG)
        assertEquals(RemoteCommandLogLevel.DEBUG, RemoteCommandLogger.logLevel)

        AppsFlyerRemoteCommand(mockApplication, "key", logLevel = RemoteCommandLogLevel.SILENT)
        assertEquals(RemoteCommandLogLevel.SILENT, RemoteCommandLogger.logLevel)
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

    @Suppress("unused")
    private fun assertFalseStub(msg: String, value: Boolean) = assertFalse(msg, value)
}
