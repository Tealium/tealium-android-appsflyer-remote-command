package com.tealium.remotecommands.appsflyer

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class JsonConversionTest {

    private val json = JSONObject()
        .put("text", "value")
        .put("count", 2)
        .put("nested", JSONObject().put("level", 1).put("empty", JSONObject.NULL))
        .put("list", JSONArray().put("a").put(JSONObject.NULL).put(JSONObject().put("k", "v")))
        .put("empty", JSONObject.NULL)

    @Test
    fun toMap_convertsNestedObjectsAndArrays_keepingNulls() {
        assertEquals(
            mapOf(
                "text" to "value",
                "count" to 2,
                "nested" to mapOf("level" to 1, "empty" to JSONObject.NULL),
                "list" to listOf("a", JSONObject.NULL, mapOf("k" to "v")),
                "empty" to JSONObject.NULL
            ),
            json.toMap()
        )
    }

    @Test
    fun toMap_dropNulls_removesNullsAtEveryLevel() {
        assertEquals(
            mapOf(
                "text" to "value",
                "count" to 2,
                "nested" to mapOf("level" to 1),
                "list" to listOf("a", mapOf("k" to "v"))
            ),
            json.toMap(dropNulls = true)
        )
    }

    @Test
    fun toMap_nullReceiver_returnsEmptyMap() {
        assertEquals(emptyMap<String, Any>(), (null as JSONObject?).toMap())
    }
}
