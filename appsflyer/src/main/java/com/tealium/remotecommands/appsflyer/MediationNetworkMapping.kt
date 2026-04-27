package com.tealium.remotecommands.appsflyer

import com.appsflyer.MediationNetwork

/**
 * Valid mediation network names accepted in the payload. Matches the keys of
 * [MediationNetworks.networkNames] and is surfaced in error messages when
 * an unknown value is received.
 */
internal val mediationNetworkValidValues: List<String>
    get() = MediationNetworks.networkNames.keys.toList()

/**
 * Converts a payload string to the AppsFlyer [MediationNetwork] enum.
 * Case-insensitive with whitespace trimming. Returns null when the value
 * is not recognized — callers should throw [AppsFlyerCommandError.invalidParameterValue].
 */
internal fun String.toMediationNetwork(): MediationNetwork? {
    val normalized = this.lowercase().trim()
    val enumName = MediationNetworks.networkNames[normalized] ?: return null
    return try {
        MediationNetwork.valueOf(enumName)
    } catch (_: IllegalArgumentException) {
        null
    }
}
