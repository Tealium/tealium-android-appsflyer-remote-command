package com.tealium.remotecommands.appsflyer

import org.json.JSONArray
import org.json.JSONObject

/**
 * Recursively converts this object to a [Map]: nested [JSONObject]s become maps and [JSONArray]s become lists.
 * A null receiver converts to an empty map.
 *
 * @param dropNulls Whether to drop [JSONObject.NULL] values, including list entries. Remote command payloads
 * keep them, while SDK attribution data must not carry them into a tracking call.
 */
internal fun JSONObject?.toMap(dropNulls: Boolean = false): Map<String, Any> {
    val map = HashMap<String, Any>()
    this?.keys()?.forEach { key ->
        convertJsonValue(opt(key), dropNulls)?.let { map[key] = it }
    }
    return map
}

private fun convertJsonValue(value: Any?, dropNulls: Boolean): Any? = when {
    value == null -> null
    dropNulls && value == JSONObject.NULL -> null
    value is JSONObject -> value.toMap(dropNulls)
    value is JSONArray -> (0 until value.length()).mapNotNull { convertJsonValue(value.opt(it), dropNulls) }
    else -> value
}

/**
 * Converts a payload or settings value to a [Boolean].
 *
 * Data-layer values often arrive as Strings, so "true"/"false" are accepted case-insensitively, like org.json's
 * own coercion does. Anything else (including null) yields null instead of silently becoming false, which could
 * otherwise re-enable tracking.
 */
internal fun toBooleanValue(value: Any?): Boolean? = when (value) {
    is Boolean -> value
    is String -> when {
        value.equals("true", ignoreCase = true) -> true
        value.equals("false", ignoreCase = true) -> false
        else -> null
    }
    else -> null
}
