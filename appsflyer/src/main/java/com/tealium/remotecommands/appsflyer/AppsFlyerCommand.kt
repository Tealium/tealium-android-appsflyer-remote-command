package com.tealium.remotecommands.appsflyer

import com.appsflyer.MediationNetwork

interface AppsFlyerCommand {
    fun initialize(devKey: String? = null, configSettings: Map<String, Any>? = null)
    fun trackLocation(latitude: Double, longitude: Double)
    fun setHost(host: String, hostPrefix: String? = "")
    fun trackEvent(eventType: String, eventParameters: Map<String, Any>? = null)
    fun setUserEmails(emails: List<String>, hashType: EmailHashType? = null)
    fun setCurrencyCode(currency: String)
    fun setCustomerId(id: String)
    fun anonymizeUser(anonymize: Boolean)
    fun resolveDeepLinkUrls(links: List<String>)
    fun stopTracking(isTrackingStopped: Boolean)
}