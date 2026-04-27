package com.tealium.remotecommands.appsflyer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CommandEnumTest {

    @Test
    fun fromString_resolvesKnownCommand() {
        assertEquals(Command.INITIALIZE, Command.fromString("initialize"))
        assertEquals(Command.TRACK_LOCATION, Command.fromString("tracklocation"))
    }

    @Test
    fun fromString_isCaseInsensitive() {
        assertEquals(Command.INITIALIZE, Command.fromString("Initialize"))
        assertEquals(Command.INITIALIZE, Command.fromString("INITIALIZE"))
    }

    @Test
    fun fromString_trimsWhitespace() {
        assertEquals(Command.INITIALIZE, Command.fromString("  initialize  "))
        assertEquals(Command.TRACK_LOCATION, Command.fromString("\ttracklocation\n"))
    }

    @Test
    fun fromString_returnsNullForUnknown() {
        assertNull(Command.fromString("purchase"))
        assertNull(Command.fromString(""))
        assertNull(Command.fromString("not_a_command"))
    }

    @Test
    fun commandValues_areUnique() {
        val names = Command.entries.map { it.commandName }
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun commandValues_matchCommandsConstants() {
        // Enum values are a nominal mapping of Commands.* constants.
        assertEquals(Commands.INITIALIZE, Command.INITIALIZE.commandName)
        assertEquals(Commands.TRACK_LOCATION, Command.TRACK_LOCATION.commandName)
        assertEquals(Commands.LOG_AD_REVENUE, Command.LOG_AD_REVENUE.commandName)
        assertEquals(Commands.STOP_TRACKING, Command.STOP_TRACKING.commandName)
    }

    @Test
    fun mediationNetwork_resolvesKnownValue() {
        assertNotNull("googleadmob".toMediationNetwork())
        assertNotNull("GoogleAdMob".toMediationNetwork())
        assertNotNull("  ironsource  ".toMediationNetwork())
    }

    @Test
    fun mediationNetwork_returnsNullForUnknown() {
        assertNull("not_a_network".toMediationNetwork())
        assertNull("".toMediationNetwork())
    }

    @Test
    fun mediationNetworkValidValues_matchesNetworkNamesKeys() {
        assertEquals(
            MediationNetworks.networkNames.keys.toList(),
            mediationNetworkValidValues
        )
    }
}
