package com.tealium.remotecommands.appsflyer

import com.appsflyer.AFLogger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LogLevelMappingTest {

    @Test
    fun fromString_resolvesLowercaseNames() {
        assertEquals(AFLogger.LogLevel.NONE, LogLevelMapping.fromString("none"))
        assertEquals(AFLogger.LogLevel.ERROR, LogLevelMapping.fromString("error"))
        assertEquals(AFLogger.LogLevel.WARNING, LogLevelMapping.fromString("warning"))
        assertEquals(AFLogger.LogLevel.INFO, LogLevelMapping.fromString("info"))
        assertEquals(AFLogger.LogLevel.DEBUG, LogLevelMapping.fromString("debug"))
        assertEquals(AFLogger.LogLevel.VERBOSE, LogLevelMapping.fromString("verbose"))
    }

    @Test
    fun fromString_isCaseInsensitive() {
        assertEquals(AFLogger.LogLevel.DEBUG, LogLevelMapping.fromString("DEBUG"))
        assertEquals(AFLogger.LogLevel.DEBUG, LogLevelMapping.fromString("Debug"))
        assertEquals(AFLogger.LogLevel.VERBOSE, LogLevelMapping.fromString("Verbose"))
    }

    @Test
    fun fromString_trimsWhitespace() {
        assertEquals(AFLogger.LogLevel.INFO, LogLevelMapping.fromString("  info  "))
        assertEquals(AFLogger.LogLevel.VERBOSE, LogLevelMapping.fromString("\tverbose\n"))
    }

    @Test
    fun fromString_fallsBackToEnumName() {
        // The fallback path matches the AFLogger.LogLevel enum's declared names.
        for (level in AFLogger.LogLevel.entries) {
            assertEquals(level, LogLevelMapping.fromString(level.name))
        }
    }

    @Test
    fun fromString_returnsNullForUnknown() {
        assertNull(LogLevelMapping.fromString("not_a_level"))
        assertNull(LogLevelMapping.fromString("critical"))
        assertNull(LogLevelMapping.fromString(""))
    }

    @Test
    fun validValues_containsAllLogLevels() {
        val expected = listOf("none", "error", "warning", "info", "debug", "verbose")
        assertEquals(expected.sorted(), LogLevelMapping.validValues.sorted())
    }
}
