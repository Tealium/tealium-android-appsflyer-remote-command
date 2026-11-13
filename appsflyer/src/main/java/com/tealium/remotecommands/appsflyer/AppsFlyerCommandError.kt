package com.tealium.remotecommands.appsflyer

/**
 * Represents a failure that occurred during AppsFlyer command execution.
 * Caught by parseCommands and forwarded to RemoteCommandLogger.
 */
internal class AppsFlyerCommandError(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    companion object {

        /** A required parameter was absent from the payload. */
        @JvmStatic
        fun missingParameter(key: String) =
            AppsFlyerCommandError("$key is required but missing from payload.")

        /** A parameter was present but its value was not among the allowed values. */
        @JvmStatic
        fun invalidParameterValue(key: String, value: String, allowedValues: List<String>) =
            AppsFlyerCommandError("Invalid value '$value' for '$key'. Supported values: ${allowedValues.joinToString(", ")}.")

        /** A parameter was present but did not match the expected type. */
        @JvmStatic
        fun invalidParameterType(key: String, expectedTypes: String) =
            AppsFlyerCommandError("Unsupported type for '$key'. Supported types: $expectedTypes.")
    }
}
