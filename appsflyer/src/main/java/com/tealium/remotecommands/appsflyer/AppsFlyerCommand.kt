package com.tealium.remotecommands.appsflyer

import com.appsflyer.MediationNetwork

interface AppsFlyerCommand {
    fun initialize(devKey: String? = null, configSettings: Map<String, Any>? = null)
    fun trackLocation(latitude: Double, longitude: Double)
    fun setHost(host: String, hostPrefix: String? = "")
    fun trackEvent(eventType: String, eventParameters: Map<String, Any>? = null)
    fun setUserEmails(emails: List<String>)
    fun setCurrencyCode(currency: String)
    fun setCustomerId(id: String)
    fun disableDeviceTracking(disable: Boolean)
    fun resolveDeepLinkUrls(links: List<String>)
    fun stopTracking(isTrackingStopped: Boolean)
    fun setDisableNetworkData(disable: Boolean)
    fun enableAppSetIdCollection(enable: Boolean)
    fun setDMAConsentData(consentData: Map<String, Any>)
    fun logAdRevenue(monetizationNetwork: String, mediationNetwork: MediationNetwork, revenue: Double, currency: String, additionalParameters: Map<String, Any>? = null)
}