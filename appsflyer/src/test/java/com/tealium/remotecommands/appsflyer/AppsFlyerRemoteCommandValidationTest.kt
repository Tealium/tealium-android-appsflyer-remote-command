package com.tealium.remotecommands.appsflyer

import android.app.Application
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Validates parameter handling and error paths in [AppsFlyerRemoteCommand].
 * Uses the hand-rolled [MockAppsFlyerInstance] (not MockK) so tests can assert
 * that failed validation produces zero SDK calls, not just an absence of a
 * specific call.
 */
@RunWith(RobolectricTestRunner::class)
class AppsFlyerRemoteCommandValidationTest {

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
    fun trackLocation_missingLatitude_skipsCall() {
        val payload = JSONObject()
        payload.put(Location.LONGITUDE, 1.0)

        remoteCommand.parseCommands(arrayOf(Commands.TRACK_LOCATION), payload)

        assertEquals(0, mockInstance.trackLocationCallCount)
    }

    @Test
    fun trackLocation_validCoordinates_dispatchesCall() {
        val payload = JSONObject()
        payload.put(Location.LATITUDE, 10.0)
        payload.put(Location.LONGITUDE, 20.0)

        remoteCommand.parseCommands(arrayOf(Commands.TRACK_LOCATION), payload)

        assertEquals(1, mockInstance.trackLocationCallCount)
        assertEquals(10.0, mockInstance.trackLocationLatitudeParam!!, 0.0001)
        assertEquals(20.0, mockInstance.trackLocationLongitudeParam!!, 0.0001)
    }

    @Test
    fun setCurrencyCode_empty_skipsCall() {
        val payload = JSONObject()
        payload.put(TransactionProperties.CURRENCY, "")

        remoteCommand.parseCommands(arrayOf(Commands.SET_CURRENCY_CODE), payload)

        assertEquals(0, mockInstance.setCurrencyCodeCallCount)
    }

    @Test
    fun setCustomerId_empty_skipsCall() {
        val payload = JSONObject()

        remoteCommand.parseCommands(arrayOf(Commands.SET_CUSTOMER_ID), payload)

        assertEquals(0, mockInstance.setCustomerIdCallCount)
    }

    @Test
    fun logAdRevenue_allRequiredParams_dispatchesCall() {
        val payload = JSONObject()
        payload.put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
        payload.put(AdRevenueParams.MEDIATION_NETWORK, "googleadmob")
        payload.put(AdRevenueParams.AD_REVENUE_CURRENCY, "USD")
        payload.put(AdRevenueParams.AD_REVENUE_AMOUNT, 1.99)

        remoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)

        assertEquals(1, mockInstance.logAdRevenueCallCount)
        assertNotNull(mockInstance.logAdRevenueDataParam)
    }

    @Test
    fun logAdRevenue_unknownMediationNetwork_skipsCall() {
        val payload = JSONObject()
        payload.put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
        payload.put(AdRevenueParams.MEDIATION_NETWORK, "not_a_network")
        payload.put(AdRevenueParams.AD_REVENUE_CURRENCY, "USD")
        payload.put(AdRevenueParams.AD_REVENUE_AMOUNT, 1.99)

        remoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)

        assertEquals(0, mockInstance.logAdRevenueCallCount)
    }

    @Test
    fun logAdRevenue_missingCurrency_skipsCall() {
        val payload = JSONObject()
        payload.put(AdRevenueParams.MONETIZATION_NETWORK, "TestNetwork")
        payload.put(AdRevenueParams.MEDIATION_NETWORK, "googleadmob")
        payload.put(AdRevenueParams.AD_REVENUE_AMOUNT, 1.99)

        remoteCommand.parseCommands(arrayOf(Commands.LOG_AD_REVENUE), payload)

        assertEquals(0, mockInstance.logAdRevenueCallCount)
    }

    @Test
    fun setConsentData_partiallyProvided_skipsCall() {
        val payload = JSONObject()
        payload.put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, true)
        payload.put(ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE, true)
        // Missing HAS_CONSENT_FOR_ADS_PERSONALIZATION and HAS_CONSENT_FOR_AD_STORAGE

        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)

        assertEquals(0, mockInstance.setConsentDataCallCount)
    }

    @Test
    fun setConsentData_allFlagsProvided_dispatchesCall() {
        val payload = JSONObject()
        payload.put(ConsentDataParams.IS_USER_SUBJECT_TO_GDPR, true)
        payload.put(ConsentDataParams.HAS_CONSENT_FOR_DATA_USAGE, false)
        payload.put(ConsentDataParams.HAS_CONSENT_FOR_ADS_PERSONALIZATION, true)
        payload.put(ConsentDataParams.HAS_CONSENT_FOR_AD_STORAGE, false)

        remoteCommand.parseCommands(arrayOf(Commands.SET_CONSENT_DATA), payload)

        assertEquals(1, mockInstance.setConsentDataCallCount)
        assertEquals(true, mockInstance.setConsentGdprParam)
        assertEquals(false, mockInstance.setConsentDataUsageParam)
        assertEquals(true, mockInstance.setConsentAdsPersonalizationParam)
        assertEquals(false, mockInstance.setConsentAdStorageParam)
    }

    @Test
    fun setPartnerData_missingId_skipsCall() {
        val payload = JSONObject()
        payload.put(PartnerDataParams.PARTNER_INFO, JSONObject().put("k", "v"))

        remoteCommand.parseCommands(arrayOf(Commands.SET_PARTNER_DATA), payload)

        assertEquals(0, mockInstance.setPartnerDataCallCount)
    }

    @Test
    fun setSharingFilter_missingArray_resetsFilter() {
        val payload = JSONObject()

        remoteCommand.parseCommands(arrayOf(Commands.SET_SHARING_FILTER_FOR_PARTNERS), payload)

        assertEquals(1, mockInstance.setSharingFilterForPartnersCallCount)
        assertNull(mockInstance.setSharingFilterForPartnersParam)
    }

    @Test
    fun anonymizeUser_missingFlag_skipsCall() {
        val payload = JSONObject()

        remoteCommand.parseCommands(arrayOf(Commands.ANONYMIZE_USER), payload)

        assertEquals(0, mockInstance.anonymizeUserCallCount)
    }

    @Test
    fun unknownCommand_dispatchedAsStandardEvent() {
        val payload = JSONObject()
        payload.put(Commands.COMMAND_KEY, "purchase")

        remoteCommand.parseCommands(arrayOf("purchase"), payload)

        assertEquals(1, mockInstance.trackEventCallCount)
        // "purchase" is a known AppsFlyer standard event -> mapped via StandardEvents.eventNames.
        assertEquals(StandardEvents.eventNames["purchase"], mockInstance.trackEventTypeParam)
    }

    @Test
    fun unknownCommand_dispatchedAsCustomEventWhenNotInStandardEvents() {
        val payload = JSONObject()
        payload.put(Commands.COMMAND_KEY, "my_custom_event")

        remoteCommand.parseCommands(arrayOf("my_custom_event"), payload)

        assertEquals(1, mockInstance.trackEventCallCount)
        assertEquals("my_custom_event", mockInstance.trackEventTypeParam)
    }

    @Test
    fun multipleCommandsInSingleBatch_areAllDispatched() {
        val payload = JSONObject()
        payload.put(TransactionProperties.CURRENCY, "USD")
        payload.put(Customer.USER_ID, "abc")
        payload.put(Tracking.STOP_TRACKING, true)

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
        // SET_CURRENCY_CODE fails (empty currency) but STOP_TRACKING must still run.
        payload.put(TransactionProperties.CURRENCY, "")
        payload.put(Tracking.STOP_TRACKING, true)

        remoteCommand.parseCommands(
            arrayOf(Commands.SET_CURRENCY_CODE, Commands.STOP_TRACKING),
            payload
        )

        assertEquals(0, mockInstance.setCurrencyCodeCallCount)
        assertEquals(1, mockInstance.stopTrackingCallCount)
    }

    @Test
    fun blankCommand_resultsInNoCalls() {
        remoteCommand.parseCommands(arrayOf(" ", "\t", ""), JSONObject())
        assertTrue(mockInstance.verifyNoCalls())
    }

    @Test
    fun mixedCaseCommand_dispatchesCorrectly() {
        val payload = JSONObject()
        payload.put(TransactionProperties.CURRENCY, "EUR")

        remoteCommand.parseCommands(arrayOf("SetCurrencyCode"), payload)

        assertEquals(1, mockInstance.setCurrencyCodeCallCount)
        assertEquals("EUR", mockInstance.setCurrencyCodeParam)
    }

    @Test
    fun setUserEmails_missingArray_skipsCall() {
        val payload = JSONObject()
        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAILS), payload)
        assertEquals(0, mockInstance.setUserEmailsCallCount)
    }

    @Test
    fun setUserEmails_withArray_dispatchesCall() {
        val payload = JSONObject()
        val emails = JSONArray().put("a@x.com").put("b@x.com")
        payload.put(Customer.EMAILS, emails)

        remoteCommand.parseCommands(arrayOf(Commands.SET_USER_EMAILS), payload)

        assertEquals(1, mockInstance.setUserEmailsCallCount)
        assertEquals(listOf("a@x.com", "b@x.com"), mockInstance.setUserEmailsParam)
    }

    @Test
    fun resolveDeepLinkUrls_missingArray_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.RESOLVE_DEEPLINK_URLS), JSONObject())
        assertEquals(0, mockInstance.resolveDeepLinkUrlsCallCount)
    }

    @Test
    fun logLevel_constructor_setsLoggerLevel() {
        AppsFlyerRemoteCommand(mockApplication, "key", logLevel = RemoteCommandLogLevel.DEBUG)
        assertEquals(RemoteCommandLogLevel.DEBUG, RemoteCommandLogger.logLevel)

        AppsFlyerRemoteCommand(mockApplication, "key", logLevel = RemoteCommandLogLevel.SILENT)
        assertEquals(RemoteCommandLogLevel.SILENT, RemoteCommandLogger.logLevel)
    }

    @Test
    fun stopTracking_present_true_callsSDK() {
        val payload = JSONObject()
        payload.put(Tracking.STOP_TRACKING, true)
        remoteCommand.parseCommands(arrayOf(Commands.STOP_TRACKING), payload)
        assertEquals(1, mockInstance.stopTrackingCallCount)
        assertEquals(true, mockInstance.stopTrackingParam)
    }

    @Test
    fun stopTracking_present_false_callsSDK() {
        val payload = JSONObject()
        payload.put(Tracking.STOP_TRACKING, false)
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
    fun disableTracking_aliasCallsSameAsStopTracking() {
        val payload = JSONObject()
        payload.put(Tracking.STOP_TRACKING, true)
        remoteCommand.parseCommands(arrayOf(Commands.DISABLE_TRACKING), payload)
        assertEquals(1, mockInstance.stopTrackingCallCount)
        assertEquals(true, mockInstance.stopTrackingParam)
    }

    @Test
    fun setHost_valid_callsSDK() {
        val payload = JSONObject()
        payload.put(Host.HOST, "example.com")
        payload.put(Host.HOST_PREFIX, "my_prefix")
        remoteCommand.parseCommands(arrayOf(Commands.SET_HOST), payload)
        assertEquals(1, mockInstance.setHostCallCount)
        assertEquals("example.com", mockInstance.setHostHostParam)
        assertEquals("my_prefix", mockInstance.setHostPrefixParam)
    }

    @Test
    fun setHost_missingHostPrefix_skipsCall() {
        val payload = JSONObject()
        payload.put(Host.HOST, "example.com")
        remoteCommand.parseCommands(arrayOf(Commands.SET_HOST), payload)
        assertEquals(0, mockInstance.setHostCallCount)
    }

    @Test
    fun disableDeviceTracking_aliasCallsAnonymizeUser() {
        val payload = JSONObject()
        payload.put(Tracking.ANONYMIZE_USER, true)
        remoteCommand.parseCommands(arrayOf(Commands.DISABLE_DEVICE_TRACKING), payload)
        assertEquals(1, mockInstance.anonymizeUserCallCount)
        assertEquals(true, mockInstance.anonymizeUserParam)
    }

    @Test
    fun resolveDeepLinkUrls_legacyKey_dispatchesCall() {
        val payload = JSONObject()
        val urls = JSONArray().put("click.example.com").put("email.example.com")
        payload.put(DeepLink.URLS_LEGACY_TIQ, urls)
        remoteCommand.parseCommands(arrayOf(Commands.RESOLVE_DEEPLINK_URLS), payload)
        assertEquals(1, mockInstance.resolveDeepLinkUrlsCallCount)
        assertEquals(listOf("click.example.com", "email.example.com"), mockInstance.resolveDeepLinkUrlsParam)
    }

    @Test
    fun logSession_dispatchesCall() {
        remoteCommand.parseCommands(arrayOf(Commands.LOG_SESSION), JSONObject())
        assertEquals(1, mockInstance.logSessionCallCount)
    }

    @Test
    fun setOaid_valid_callsSDK() {
        val payload = JSONObject()
        payload.put(StringCommandParams.OAID, "test-oaid-123")
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
    fun setOutOfStore_valid_callsSDK() {
        val payload = JSONObject()
        payload.put(StringCommandParams.STORE_NAME, "amazon")
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
        val payload = JSONObject()
        payload.put(StringCommandParams.DISABLE_NETWORK_DATA, true)
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
        val payload = JSONObject()
        payload.put(StringCommandParams.APP_INVITE_ONE_LINK_ID, "XY1A")
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
    fun setPreinstallAttribution_valid_callsSDK() {
        val payload = JSONObject()
        payload.put(PreinstallParams.MEDIA_SOURCE, "email")
        payload.put(PreinstallParams.CAMPAIGN, "summer_promo")
        payload.put(PreinstallParams.SITE_ID, "site_001")
        remoteCommand.parseCommands(arrayOf(Commands.SET_PREINSTALL_ATTRIBUTION), payload)
        assertEquals(1, mockInstance.setPreinstallAttributionCallCount)
        assertEquals("email", mockInstance.setPreinstallMediaSourceParam)
        assertEquals("summer_promo", mockInstance.setPreinstallCampaignParam)
        assertEquals("site_001", mockInstance.setPreinstallSiteIdParam)
    }

    @Test
    fun setPreinstallAttribution_missingMediaSource_skipsCall() {
        val payload = JSONObject()
        payload.put(PreinstallParams.CAMPAIGN, "summer_promo")
        payload.put(PreinstallParams.SITE_ID, "site_001")
        remoteCommand.parseCommands(arrayOf(Commands.SET_PREINSTALL_ATTRIBUTION), payload)
        assertEquals(0, mockInstance.setPreinstallAttributionCallCount)
    }

    @Test
    fun setIsUpdate_true_callsSDK() {
        val payload = JSONObject()
        payload.put(StringCommandParams.IS_UPDATE, true)
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
    fun setLogLevel_valid_callsSDK() {
        val payload = JSONObject()
        payload.put(StringCommandParams.LOG_LEVEL, "VERBOSE")
        remoteCommand.parseCommands(arrayOf(Commands.SET_LOG_LEVEL), payload)
        assertEquals(1, mockInstance.setLogLevelCallCount)
        assertEquals("VERBOSE", mockInstance.setLogLevelParam)
    }

    @Test
    fun setLogLevel_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_LOG_LEVEL), JSONObject())
        assertEquals(0, mockInstance.setLogLevelCallCount)
    }

    @Test
    fun setAndroidId_valid_callsSDK() {
        val payload = JSONObject()
        payload.put(StringCommandParams.ANDROID_ID, "test_android_id")
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
        val payload = JSONObject()
        payload.put(StringCommandParams.IMEI, "123456789012345")
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
    fun initialize_collectAndroidId_passedInSettings() {
        val settings = JSONObject()
        settings.put(Settings.COLLECT_ANDROID_ID, true)
        val payload = JSONObject()
        payload.put(Config.DEV_KEY, "test_dev_key")
        payload.put(Config.SETTINGS, settings)
        remoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)
        assertEquals(1, mockInstance.initializeCallCount)
        assertEquals(true, mockInstance.initializeSettingsParam?.get(Settings.COLLECT_ANDROID_ID))
    }

    @Test
    fun initialize_collectImei_passedInSettings() {
        val settings = JSONObject()
        settings.put(Settings.COLLECT_IMEI, true)
        val payload = JSONObject()
        payload.put(Config.DEV_KEY, "test_dev_key")
        payload.put(Config.SETTINGS, settings)
        remoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)
        assertEquals(1, mockInstance.initializeCallCount)
        assertEquals(true, mockInstance.initializeSettingsParam?.get(Settings.COLLECT_IMEI))
    }

    @Test
    fun initialize_collectOaid_passedInSettings() {
        val settings = JSONObject()
        settings.put(Settings.COLLECT_OAID, true)
        val payload = JSONObject()
        payload.put(Config.DEV_KEY, "test_dev_key")
        payload.put(Config.SETTINGS, settings)

        remoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), payload)

        assertEquals(1, mockInstance.initializeCallCount)
        assertEquals(true, mockInstance.initializeSettingsParam?.get(Settings.COLLECT_OAID))
    }

    @Test
    fun initialize_missingDevKey_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.INITIALIZE), JSONObject())
        assertEquals(0, mockInstance.initializeCallCount)
    }

    @Test
    fun setPhoneNumber_missing_skipsCall() {
        remoteCommand.parseCommands(arrayOf(Commands.SET_PHONE_NUMBER), JSONObject())
        assertEquals(0, mockInstance.setPhoneNumberCallCount)
    }
}
