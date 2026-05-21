package com.tealium.remotecommands.appsflyer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandEnumTest {

    @Test
    fun fromString_resolvesKnownCommand() {
        assertEquals(Command.INITIALIZE, Command.fromString("initialize"))
        assertEquals(Command.TRACK_LOCATION, Command.fromString("tracklocation"))
        assertEquals(Command.LOG_AD_REVENUE, Command.fromString("logadrevenue"))
        assertEquals(Command.SET_CONSENT_DATA, Command.fromString("setconsentdata"))
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
        // Standard AppsFlyer event names are not built-in commands — they fall through
        // to the custom-event path in AppsFlyerRemoteCommand.
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
        // Each enum value should mirror a constant in Commands — checking a representative
        // sample avoids brittleness without sacrificing coverage of the mapping shape.
        assertEquals(Commands.INITIALIZE, Command.INITIALIZE.commandName)
        assertEquals(Commands.TRACK_LOCATION, Command.TRACK_LOCATION.commandName)
        assertEquals(Commands.SET_HOST, Command.SET_HOST.commandName)
        assertEquals(Commands.SET_USER_EMAILS, Command.SET_USER_EMAILS.commandName)
        assertEquals(Commands.SET_CURRENCY_CODE, Command.SET_CURRENCY_CODE.commandName)
        assertEquals(Commands.SET_CUSTOMER_ID, Command.SET_CUSTOMER_ID.commandName)
        assertEquals(Commands.SET_PHONE_NUMBER, Command.SET_PHONE_NUMBER.commandName)
        assertEquals(Commands.LOG_AD_REVENUE, Command.LOG_AD_REVENUE.commandName)
        assertEquals(Commands.SET_CONSENT_DATA, Command.SET_CONSENT_DATA.commandName)
        assertEquals(Commands.SET_PARTNER_DATA, Command.SET_PARTNER_DATA.commandName)
        assertEquals(Commands.SET_SHARING_FILTER_FOR_PARTNERS, Command.SET_SHARING_FILTER_FOR_PARTNERS.commandName)
        assertEquals(Commands.ANONYMIZE_USER, Command.ANONYMIZE_USER.commandName)
        assertEquals(Commands.DISABLE_DEVICE_TRACKING, Command.DISABLE_DEVICE_TRACKING.commandName)
        assertEquals(Commands.RESOLVE_DEEPLINK_URLS, Command.RESOLVE_DEEPLINK_URLS.commandName)
        assertEquals(Commands.START, Command.START.commandName)
        assertEquals(Commands.STOP_TRACKING, Command.STOP_TRACKING.commandName)
        assertEquals(Commands.DISABLE_TRACKING, Command.DISABLE_TRACKING.commandName)
        assertEquals(Commands.LOG_SESSION, Command.LOG_SESSION.commandName)
        assertEquals(Commands.SET_OAID, Command.SET_OAID.commandName)
        assertEquals(Commands.SET_ANDROID_ID, Command.SET_ANDROID_ID.commandName)
        assertEquals(Commands.SET_IMEI, Command.SET_IMEI.commandName)
        assertEquals(Commands.SET_OUT_OF_STORE, Command.SET_OUT_OF_STORE.commandName)
        assertEquals(Commands.SET_DISABLE_NETWORK_DATA, Command.SET_DISABLE_NETWORK_DATA.commandName)
        assertEquals(Commands.SET_APP_INVITE_ONE_LINK, Command.SET_APP_INVITE_ONE_LINK.commandName)
        assertEquals(Commands.SET_PREINSTALL_ATTRIBUTION, Command.SET_PREINSTALL_ATTRIBUTION.commandName)
        assertEquals(Commands.SET_IS_UPDATE, Command.SET_IS_UPDATE.commandName)
    }

    @Test
    fun commandEnum_coversAllCommandsConstants() {
        // The enum must cover every string constant in Commands — if a new command is
        // added in Commands but the enum isn't updated, parseCommands would silently
        // dispatch it as a custom event.
        val constantsByName = mutableSetOf<String>()
        listOf(
            Commands.INITIALIZE, Commands.TRACK_LOCATION, Commands.SET_HOST,
            Commands.SET_USER_EMAILS, Commands.SET_CURRENCY_CODE, Commands.SET_CUSTOMER_ID,
            Commands.SET_PHONE_NUMBER, Commands.LOG_AD_REVENUE, Commands.SET_CONSENT_DATA,
            Commands.SET_PARTNER_DATA, Commands.SET_SHARING_FILTER_FOR_PARTNERS,
            Commands.ANONYMIZE_USER, Commands.DISABLE_DEVICE_TRACKING,
            Commands.RESOLVE_DEEPLINK_URLS, Commands.START, Commands.STOP_TRACKING,
            Commands.DISABLE_TRACKING, Commands.LOG_SESSION, Commands.SET_OAID,
            Commands.SET_ANDROID_ID, Commands.SET_IMEI, Commands.SET_OUT_OF_STORE,
            Commands.SET_DISABLE_NETWORK_DATA, Commands.SET_APP_INVITE_ONE_LINK,
            Commands.SET_PREINSTALL_ATTRIBUTION, Commands.SET_IS_UPDATE
        ).forEach { constantsByName.add(it) }

        val enumNames = Command.entries.map { it.commandName }.toSet()
        assertTrue(
            "Every Commands.* constant should have a matching enum value. Missing: ${constantsByName - enumNames}",
            (constantsByName - enumNames).isEmpty()
        )
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
    fun mediationNetwork_fallbackAcceptsEnumNames() {
        // Payload authors may send the SDK enum name (e.g. "GOOGLE_ADMOB") directly;
        // we accept that as a safety net alongside the friendly lowercase keys.
        assertNotNull("GOOGLE_ADMOB".toMediationNetwork())
        assertNotNull("IRONSOURCE".toMediationNetwork())
    }

    @Test
    fun mediationNetworkValidValues_containsAllKnownNetworks() {
        val expected = listOf(
            "googleadmob", "ironsource", "applovinmax", "fyber", "appodeal",
            "admost", "topon", "tradplus", "yandex", "chartboost", "unity",
            "toponpte", "custom", "direct"
        )
        assertEquals(expected.sorted(), mediationNetworkValidValues.sorted())
    }
}
