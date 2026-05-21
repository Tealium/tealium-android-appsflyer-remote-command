package com.tealium.remotecommands.appsflyer

import com.appsflyer.AFLogger
import java.util.Locale

/**
 * Maps payload string values to [AFLogger.LogLevel] enum values.
 * Case-insensitive. Returns null for unknown values — callers should throw
 * [AppsFlyerCommandError.invalidParameterValue].
 */
internal object LogLevelMapping {

    private val stringMap: Map<String, AFLogger.LogLevel> = mapOf(
        "none" to AFLogger.LogLevel.NONE,
        "error" to AFLogger.LogLevel.ERROR,
        "warning" to AFLogger.LogLevel.WARNING,
        "info" to AFLogger.LogLevel.INFO,
        "debug" to AFLogger.LogLevel.DEBUG,
        "verbose" to AFLogger.LogLevel.VERBOSE
    )

    val validValues: List<String> get() = stringMap.keys.toList()

    fun fromString(value: String): AFLogger.LogLevel? {
        val trimmed = value.trim()
        return stringMap[trimmed.lowercase(Locale.ROOT)]
            ?: AFLogger.LogLevel.entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
    }
}
