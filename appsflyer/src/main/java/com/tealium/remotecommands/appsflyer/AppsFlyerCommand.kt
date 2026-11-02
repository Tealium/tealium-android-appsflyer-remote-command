package com.tealium.remotecommands.appsflyer

import com.appsflyer.share.AFAdRevenueData
import com.appsflyer.share.AppsFlyerConsent
import com.appsflyer.share.deeplink.DeepLinkResult

interface AppsFlyerCommand {
    fun initialize(devKey: String? = null, configSettings: Map<String, Any>? = null)
    fun trackLocation(latitude: Double, longitude: Double)
    fun setHost(host: String, hostPrefix: String = "")
    fun trackEvent(eventType: String, eventParameters: Map<String, Any>? = null)
    fun setUserEmail(email: String)
    fun setUserFirstName(firstName: String)
    fun setUserLastName(lastName: String)
    fun setUserFbLoginId(fbLoginId: Long)
    fun clearUserPii()
    fun setCurrencyCode(currency: String)
    fun setCustomerId(id: String)
    fun setUserPhone(countryCode: String, phoneNumber: String)
    fun logAdRevenue(adRevenueData: AFAdRevenueData, additionalParameters: Map<String, Any>?)
    fun setConsentData(consent: AppsFlyerConsent)
    fun setPartnerData(partnerId: String, partnerInfo: Map<String, Any>?)
    fun setSharingFilterForPartners(partners: Array<String>?)
    fun anonymizeUser(anonymize: Boolean)
    fun resolveDeepLinkUrls(links: List<String>)
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
    fun onDeepLinking(result: DeepLinkResult)
}
