package com.tealium.remotecommands.appsflyer

import com.appsflyer.AFAdRevenueData
import com.appsflyer.AppsFlyerConsent

/**
 * Fully inspectable test double for [AppsFlyerCommand].
 * Tracks per-method call counts, last-call parameters, and full call histories —
 * lets tests assert exact sequences, not just presence.
 */
class MockAppsFlyerInstance : AppsFlyerCommand {

    var initializeCallCount = 0
    var trackLocationCallCount = 0
    var setHostCallCount = 0
    var trackEventCallCount = 0
    var setUserEmailsCallCount = 0
    var setCurrencyCodeCallCount = 0
    var setCustomerIdCallCount = 0
    var setPhoneNumberCallCount = 0
    var logAdRevenueCallCount = 0
    var setConsentDataCallCount = 0
    var setPartnerDataCallCount = 0
    var setSharingFilterForPartnersCallCount = 0
    var anonymizeUserCallCount = 0
    var resolveDeepLinkUrlsCallCount = 0
    var startCallCount = 0
    var stopTrackingCallCount = 0
    var addPushNotificationDeepLinkPathCallCount = 0
    var logSessionCallCount = 0
    var setOaidCallCount = 0
    var setAndroidIdCallCount = 0
    var setImeiCallCount = 0
    var setOutOfStoreCallCount = 0
    var setDisableNetworkDataCallCount = 0
    var setAppInviteOneLinkCallCount = 0
    var setPreinstallAttributionCallCount = 0
    var setIsUpdateCallCount = 0

    var initializeDevKeyParam: String? = null
    var initializeSettingsParam: Map<String, Any>? = null
    var trackLocationLatitudeParam: Double? = null
    var trackLocationLongitudeParam: Double? = null
    var setHostHostParam: String? = null
    var setHostPrefixParam: String? = null
    var trackEventTypeParam: String? = null
    var trackEventParametersParam: Map<String, Any>? = null
    var setUserEmailsParam: List<String>? = null
    var setUserEmailsCryptTypeParam: Int? = null
    var setCurrencyCodeParam: String? = null
    var setCustomerIdParam: String? = null
    var setPhoneNumberParam: String? = null
    var logAdRevenueDataParam: AFAdRevenueData? = null
    var logAdRevenueAdditionalParamsParam: Map<String, Any>? = null
    var setConsentDataParam: AppsFlyerConsent? = null
    var setPartnerDataIdParam: String? = null
    var setPartnerDataInfoParam: Map<String, Any>? = null
    var setSharingFilterForPartnersParam: Array<String>? = null
    var anonymizeUserParam: Boolean? = null
    var resolveDeepLinkUrlsParam: List<String>? = null
    var stopTrackingParam: Boolean? = null
    var addPushNotificationDeepLinkPathParam: List<String>? = null
    var setOaidParam: String? = null
    var setAndroidIdParam: String? = null
    var setImeiParam: String? = null
    var setOutOfStoreParam: String? = null
    var setDisableNetworkDataParam: Boolean? = null
    var setAppInviteOneLinkParam: String? = null
    var setPreinstallMediaSourceParam: String? = null
    var setPreinstallCampaignParam: String? = null
    var setPreinstallSiteIdParam: String? = null
    var setIsUpdateParam: Boolean? = null

    // Convenience accessors — AppsFlyerConsent's boxed Booleans surface as non-null via the builder
    // path exercised by AppsFlyerRemoteCommand (requireBoolean() unboxes before construction).
    val setConsentGdprParam: Boolean? get() = setConsentDataParam?.isUserSubjectToGDPR
    val setConsentDataUsageParam: Boolean? get() = setConsentDataParam?.hasConsentForDataUsage
    val setConsentAdsPersonalizationParam: Boolean? get() = setConsentDataParam?.hasConsentForAdsPersonalization
    val setConsentAdStorageParam: Boolean? get() = setConsentDataParam?.hasConsentForAdStorage

    data class TrackEventCall(val eventType: String, val eventParameters: Map<String, Any>?)

    val trackEventCalls: MutableList<TrackEventCall> = mutableListOf()

    override fun initialize(devKey: String?, configSettings: Map<String, Any>?) {
        initializeCallCount++
        initializeDevKeyParam = devKey
        initializeSettingsParam = configSettings
    }

    override fun trackLocation(latitude: Double, longitude: Double) {
        trackLocationCallCount++
        trackLocationLatitudeParam = latitude
        trackLocationLongitudeParam = longitude
    }

    override fun setHost(host: String, hostPrefix: String?) {
        setHostCallCount++
        setHostHostParam = host
        setHostPrefixParam = hostPrefix
    }

    override fun trackEvent(eventType: String, eventParameters: Map<String, Any>?) {
        trackEventCallCount++
        trackEventTypeParam = eventType
        trackEventParametersParam = eventParameters
        trackEventCalls.add(TrackEventCall(eventType, eventParameters))
    }

    override fun setUserEmails(emails: List<String>, cryptType: Int) {
        setUserEmailsCallCount++
        setUserEmailsParam = emails
        setUserEmailsCryptTypeParam = cryptType
    }

    override fun setCurrencyCode(currency: String) {
        setCurrencyCodeCallCount++
        setCurrencyCodeParam = currency
    }

    override fun setCustomerId(id: String) {
        setCustomerIdCallCount++
        setCustomerIdParam = id
    }

    override fun setPhoneNumber(phoneNumber: String) {
        setPhoneNumberCallCount++
        setPhoneNumberParam = phoneNumber
    }

    override fun logAdRevenue(adRevenueData: AFAdRevenueData, additionalParameters: Map<String, Any>?) {
        logAdRevenueCallCount++
        logAdRevenueDataParam = adRevenueData
        logAdRevenueAdditionalParamsParam = additionalParameters
    }

    override fun setConsentData(consent: AppsFlyerConsent) {
        setConsentDataCallCount++
        setConsentDataParam = consent
    }

    override fun setPartnerData(partnerId: String, partnerInfo: Map<String, Any>?) {
        setPartnerDataCallCount++
        setPartnerDataIdParam = partnerId
        setPartnerDataInfoParam = partnerInfo
    }

    override fun setSharingFilterForPartners(partners: Array<String>?) {
        setSharingFilterForPartnersCallCount++
        setSharingFilterForPartnersParam = partners
    }

    override fun anonymizeUser(anonymize: Boolean) {
        anonymizeUserCallCount++
        anonymizeUserParam = anonymize
    }

    override fun resolveDeepLinkUrls(links: List<String>) {
        resolveDeepLinkUrlsCallCount++
        resolveDeepLinkUrlsParam = links
    }

    override fun start() {
        startCallCount++
    }

    override fun stopTracking(isTrackingStopped: Boolean) {
        stopTrackingCallCount++
        stopTrackingParam = isTrackingStopped
    }

    override fun addPushNotificationDeepLinkPath(deepLinkPath: List<String>) {
        addPushNotificationDeepLinkPathCallCount++
        addPushNotificationDeepLinkPathParam = deepLinkPath
    }

    override fun logSession() {
        logSessionCallCount++
    }

    override fun setOaid(oaid: String) {
        setOaidCallCount++
        setOaidParam = oaid
    }

    override fun setAndroidId(androidId: String) {
        setAndroidIdCallCount++
        setAndroidIdParam = androidId
    }

    override fun setImei(imei: String) {
        setImeiCallCount++
        setImeiParam = imei
    }

    override fun setOutOfStore(storeName: String) {
        setOutOfStoreCallCount++
        setOutOfStoreParam = storeName
    }

    override fun setDisableNetworkData(disable: Boolean) {
        setDisableNetworkDataCallCount++
        setDisableNetworkDataParam = disable
    }

    override fun setAppInviteOneLink(oneLinkId: String) {
        setAppInviteOneLinkCallCount++
        setAppInviteOneLinkParam = oneLinkId
    }

    override fun setPreinstallAttribution(mediaSource: String, campaign: String, siteId: String) {
        setPreinstallAttributionCallCount++
        setPreinstallMediaSourceParam = mediaSource
        setPreinstallCampaignParam = campaign
        setPreinstallSiteIdParam = siteId
    }

    override fun setIsUpdate(isUpdate: Boolean) {
        setIsUpdateCallCount++
        setIsUpdateParam = isUpdate
    }

    fun verifyNoCalls(): Boolean = initializeCallCount == 0 &&
            trackLocationCallCount == 0 &&
            setHostCallCount == 0 &&
            trackEventCallCount == 0 &&
            setUserEmailsCallCount == 0 &&
            setCurrencyCodeCallCount == 0 &&
            setCustomerIdCallCount == 0 &&
            setPhoneNumberCallCount == 0 &&
            logAdRevenueCallCount == 0 &&
            setConsentDataCallCount == 0 &&
            setPartnerDataCallCount == 0 &&
            setSharingFilterForPartnersCallCount == 0 &&
            anonymizeUserCallCount == 0 &&
            resolveDeepLinkUrlsCallCount == 0 &&
            startCallCount == 0 &&
            stopTrackingCallCount == 0 &&
            addPushNotificationDeepLinkPathCallCount == 0 &&
            logSessionCallCount == 0 &&
            setOaidCallCount == 0 &&
            setAndroidIdCallCount == 0 &&
            setImeiCallCount == 0 &&
            setOutOfStoreCallCount == 0 &&
            setDisableNetworkDataCallCount == 0 &&
            setAppInviteOneLinkCallCount == 0 &&
            setPreinstallAttributionCallCount == 0 &&
            setIsUpdateCallCount == 0
}
