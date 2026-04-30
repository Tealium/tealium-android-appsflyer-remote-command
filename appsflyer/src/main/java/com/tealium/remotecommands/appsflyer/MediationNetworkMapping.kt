package com.tealium.remotecommands.appsflyer

import com.appsflyer.MediationNetwork
import java.util.Locale

/**
 * Maps lowercase payload strings to SDK enum values.
 * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-mediationnetworktype
 */
private val networkNames: Map<String, MediationNetwork> = mapOf(
    "googleadmob" to MediationNetwork.GOOGLE_ADMOB,
    "ironsource" to MediationNetwork.IRONSOURCE,
    "applovinmax" to MediationNetwork.APPLOVIN_MAX,
    "fyber" to MediationNetwork.FYBER,
    "appodeal" to MediationNetwork.APPODEAL,
    "admost" to MediationNetwork.ADMOST,
    "topon" to MediationNetwork.TOPON,
    "tradplus" to MediationNetwork.TRADPLUS,
    "yandex" to MediationNetwork.YANDEX,
    "chartboost" to MediationNetwork.CHARTBOOST,
    "unity" to MediationNetwork.UNITY,
    "toponpte" to MediationNetwork.TOPON_PTE,
    "custom" to MediationNetwork.CUSTOM_MEDIATION,
    "direct" to MediationNetwork.DIRECT_MONETIZATION_NETWORK
)

/**
 * Valid mediation network names accepted in the payload, surfaced in error messages when
 * an unknown value is received. Also accepts enum names (e.g. "GOOGLE_ADMOB").
 */
internal val mediationNetworkValidValues: List<String>
    get() = networkNames.keys.toList()

/**
 * Converts a payload string to the AppsFlyer [MediationNetwork] enum.
 * Case-insensitive with whitespace trimming. Accepts friendly names (e.g. "googleadmob")
 * and enum names (e.g. "GOOGLE_ADMOB"). Returns null when the value is not recognized —
 * callers should throw [AppsFlyerCommandError.invalidParameterValue].
 */
internal fun String.toMediationNetwork(): MediationNetwork? {
    val trimmed = this.trim()
    return networkNames[trimmed.lowercase(Locale.ROOT)]
        ?: MediationNetwork.entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
}
