package com.tealium.remotecommands.appsflyer

import com.appsflyer.AppsFlyerProperties

/**
 * Maps integer payload values to [AppsFlyerProperties.EmailsCryptType] enum values,
 * mirroring the iOS EmailCryptType raw values for cross-platform consistency.
 *
 * Accepted values:
 *  0 = NONE
 *  3 = SHA256
 */
internal object EmailCryptTypeMapping {

    private val intMap: Map<Int, AppsFlyerProperties.EmailsCryptType> = mapOf(
        0 to AppsFlyerProperties.EmailsCryptType.NONE,
        3 to AppsFlyerProperties.EmailsCryptType.SHA256
    )

    val validValues: List<Int> get() = intMap.keys.toList()

    fun fromInt(value: Int): AppsFlyerProperties.EmailsCryptType? = intMap[value]
}
