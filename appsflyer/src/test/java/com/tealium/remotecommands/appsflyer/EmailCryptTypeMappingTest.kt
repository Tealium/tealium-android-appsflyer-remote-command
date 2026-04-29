package com.tealium.remotecommands.appsflyer

import com.appsflyer.AppsFlyerProperties
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EmailCryptTypeMappingTest {

    @Test
    fun fromInt_resolvesNone() {
        assertEquals(AppsFlyerProperties.EmailsCryptType.NONE, EmailCryptTypeMapping.fromInt(0))
    }

    @Test
    fun fromInt_resolvesSha256() {
        assertEquals(AppsFlyerProperties.EmailsCryptType.SHA256, EmailCryptTypeMapping.fromInt(3))
    }

    @Test
    fun fromInt_returnsNullForUnknown() {
        assertNull(EmailCryptTypeMapping.fromInt(1))
        assertNull(EmailCryptTypeMapping.fromInt(2))
        assertNull(EmailCryptTypeMapping.fromInt(99))
        assertNull(EmailCryptTypeMapping.fromInt(-1))
    }

    @Test
    fun validValues_containsAllKnownValues() {
        assertEquals(listOf(0, 3).sorted(), EmailCryptTypeMapping.validValues.sorted())
    }
}
