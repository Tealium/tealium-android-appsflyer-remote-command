package com.tealium.remotecommands.appsflyer

/** Controls verbosity of [RemoteCommandLogger] output. */
enum class RemoteCommandLogLevel {
    /** Debug, info, warning, and error messages. */
    DEBUG,

    /** Info, warning, and error messages. */
    INFO,

    /** Warning and error messages only. */
    WARNING,

    /** Error messages only. */
    ERROR,

    /** No output. Default. */
    SILENT
}
