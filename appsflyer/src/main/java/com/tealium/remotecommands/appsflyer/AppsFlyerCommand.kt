package com.tealium.remotecommands.appsflyer

import com.appsflyer.AFAdRevenueData
import com.appsflyer.AppsFlyerConsent

interface AppsFlyerCommand {
    fun initialize(devKey: String? = null, configSettings: Map<String, Any>? = null)
    fun trackLocation(latitude: Double, longitude: Double)
    fun setHost(host: String, hostPrefix: String = "")
    fun trackEvent(eventType: String, eventParameters: Map<String, Any>? = null)
    fun setUserEmails(emails: List<String>, cryptType: Int)
    fun setCurrencyCode(currency: String)
    fun setCustomerId(id: String)
    fun setPhoneNumber(phoneNumber: String)
    fun logAdRevenue(adRevenueData: AFAdRevenueData, additionalParameters: Map<String, Any>?)
    fun setConsentData(consent: AppsFlyerConsent)
    fun setPartnerData(partnerId: String, partnerInfo: Map<String, Any>?)
    fun setSharingFilterForPartners(partners: Array<String>?)
    fun anonymizeUser(anonymize: Boolean)
    fun resolveDeepLinkUrls(links: List<String>)
    fun start()
    fun stopTracking(isTrackingStopped: Boolean)
    fun addPushNotificationDeepLinkPath(deepLinkPath: List<String>)
    fun logSession()
    fun setOaid(oaid: String)
    fun setAndroidId(androidId: String)
    fun setImei(imei: String)
    fun setOutOfStore(storeName: String)
    fun setDisableNetworkData(disable: Boolean)
    fun setAppInviteOneLink(oneLinkId: String)
    fun setPreinstallAttribution(mediaSource: String, campaign: String, siteId: String)
    fun setIsUpdate(isUpdate: Boolean)
}