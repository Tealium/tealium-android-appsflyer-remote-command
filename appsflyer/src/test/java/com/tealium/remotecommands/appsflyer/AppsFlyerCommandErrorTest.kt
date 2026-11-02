package com.tealium.remotecommands.appsflyer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppsFlyerCommandErrorTest {

    @Test
    fun missingParameter_includesKeyName() {
        val error = AppsFlyerCommandError.missingParameter("app_dev_key")
        assertNotNull(error.message)
        assertTrue(
            "Message should mention the missing key",
            error.message!!.contains("app_dev_key")
        )
    }

    @Test
    fun invalidParameterValue_includesKeyValueAndAllowedValues() {
        val error = AppsFlyerCommandError.invalidParameterValue(
            key = "email_hash_type",
            value = "99",
            allowedValues = listOf("0", "3")
        )
        val msg = error.message!!
        assertTrue(msg.contains("email_hash_type"))
        assertTrue(msg.contains("99"))
        assertTrue(msg.contains("0"))
        assertTrue(msg.contains("3"))
    }

    @Test
    fun invalidParameterType_includesKeyAndExpectedTypes() {
        val error = AppsFlyerCommandError.invalidParameterType("amount", "Number, String")
        val msg = error.message!!
        assertTrue(msg.contains("amount"))
        assertTrue(msg.contains("Number"))
        assertTrue(msg.contains("String"))
    }

    @Test
    fun isInstanceOfException() {
        val error = AppsFlyerCommandError.missingParameter("x")
        assertEquals(Exception::class.java, error.javaClass.superclass)
    }
}
