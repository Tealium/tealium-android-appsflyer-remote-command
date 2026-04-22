package com.tealium.remotecommands.appsflyer

import android.app.Application
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
    fun debugFlag_togglesLogger() {
        RemoteCommandLogger.debugEnabled = false
        AppsFlyerRemoteCommand(mockApplication, "key", debug = true)
        assertTrue(RemoteCommandLogger.debugEnabled)

        AppsFlyerRemoteCommand(mockApplication, "key", debug = false)
        assertFalse(RemoteCommandLogger.debugEnabled)
    }
}
