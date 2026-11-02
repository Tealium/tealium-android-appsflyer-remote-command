# tealium-android-appsflyer-remote-command

[![License](https://img.shields.io/badge/license-Proprietary-blue.svg?style=flat
            )](https://github.com/Tealium/tealium-java/blob/master/LICENSE.txt)
[![Platform](https://img.shields.io/badge/Platform-Android-lightgrey.svg?style=flat
             )](https://developer.android.com/guide/index.html)
[![Language](https://img.shields.io/badge/Language-Kotlin-orange.svg?style=flat
             )](https://developer.android.com/reference/packages.html)



## Installation

The AppsFlyer Android SDK (`[7.0.2,8)`) and Tealium `remotecommands` (`1.0.3`) come in as `api` dependencies. The app needs Kotlin 2.0+, minSdk 21 and Tealium Kotlin with the RemoteCommands dispatcher. See the [documentation](https://docs.tealium.com/platforms/remote-commands/integrations/appsflyer/) for their setup steps.

```groovy
repositories {
    google()
    mavenCentral()
    maven { url "https://maven.tealiumiq.com/android/releases/" }
}

dependencies {
    implementation "com.tealium.remotecommands:appsflyer:2.0.0"
}
```

If the app declares the AppsFlyer SDK itself, keep it within `[7.0.2,8)`.

## Session modes

AppsFlyer SDK 7 keeps a single session-ready listener, and `AppsFlyerLib.getInstance().start()` has to be called inside it. `AppsFlyerSessionMode` decides who owns that listener. It is a required constructor argument, not a remote setting, because it has to match what the app code does.

| Mode | Who registers the listener | Who calls `start()` |
|------|----------------------------|---------------------|
| `AUTOMATIC` | The RemoteCommand, once the `initialize` command has run. The app must not register its own listener, or it replaces ours. | The RemoteCommand's listener |
| `APP_MANAGED` | The app | The app, inside its listener. Without one, no session starts |

AppsFlyer SDK 7 does not keep setter values across launches. Send `setcustomerid`, `setconsentdata`, `anonymizeuser`, `setcurrencycode`, `setuseremail` and similar commands on every launch, from values your app stores itself, and send `setconsentdata` before `initialize` so consent is in place before the first session: `"launch": "setconsentdata,initialize"`. For a value such as the customer user ID to reach the install event, use `APP_MANAGED` and set it before you call `start()`.

"Initialized" means the `initialize` command ran with a dev key, from `app_dev_key` or the constructor's `appsFlyerDevKey`. It calls `AppsFlyerLib.init` and applies the settings, but no longer starts the session. The RemoteCommand does not detect an `init()` that the app calls itself, so keep `initialize` mapped in both modes. Events, `tracklocation` and `logadrevenue` queue until it has run, then are sent in order.

### Constructing the RemoteCommand

Construct `AppsFlyerRemoteCommand` before the launcher Activity resumes, ideally in `Application.onCreate` before `Tealium.create`, so `init` sees the Activity.

```kotlin
// The `initialize` payload provides the dev key.
val command = AppsFlyerRemoteCommand(application, AppsFlyerSessionMode.AUTOMATIC)
val tealium = Tealium.create("main", config) {
    remoteCommands?.add(command, filename = "appsflyer.json")
}

// A fallback dev key, for payloads without `app_dev_key`, and a log level.
val commandWithKey = AppsFlyerRemoteCommand(application,
                                            AppsFlyerSessionMode.AUTOMATIC,
                                            appsFlyerDevKey = "<your dev key>",
                                            logLevel = RemoteCommandLogLevel.DEBUG)
```

`appsFlyerDevKey`, `commandId`, `description` and `logLevel` are optional. `logLevel` is a `RemoteCommandLogLevel` (`DEBUG`, `INFO`, `WARNING`, `ERROR`, `SILENT`) and defaults to `ERROR`. Messages use the tag `TealiumAppsFlyer`.

### `APP_MANAGED` example

The app registers the listener and calls `start()` in it.

```kotlin
val command = AppsFlyerRemoteCommand(application, AppsFlyerSessionMode.APP_MANAGED)
val tealium = Tealium.create("main", config) {
    remoteCommands?.add(command, filename = "appsflyer.json")
}

AppsFlyerLib.getInstance().registerSessionReadyListener(object : SessionReadyListener {
    override fun onSessionReady() {
        // Runs on an SDK background thread.
        AppsFlyerLib.getInstance().start()
    }
})
```

`SessionReadyListener` is `com.appsflyer.share.SessionReadyListener`. After a `stoptracking` command with `stop_tracking` set to `false`, the RemoteCommand only calls `stop(false)` in this mode, so the app restarts the session itself. In `AUTOMATIC` mode it calls `start()` once `initialize` has run.

### Deep links

App-open attribution is opt-in. `AppsFlyerRemoteCommand` is a `DeepLinkListener` but never subscribes itself. Once subscribed, it reports `app_open_attribution` with the OneLink click event as payload, and `appsflyer_error` with `error_name` `app_open_attribution_failure` when the deep link resolution fails. Deferred deep links are skipped, because `conversion_data_received` reports them.

An app without its own deep link routing subscribes the command in `Application.onCreate`, right after constructing it:

```kotlin
val command = AppsFlyerRemoteCommand(application, AppsFlyerSessionMode.AUTOMATIC)
AppsFlyerLib.getInstance().subscribeForDeepLink(command)
```

An app that routes deep links itself subscribes its own listener there instead, and forwards every result to the command, otherwise `app_open_attribution` is not tracked:

```kotlin
AppsFlyerLib.getInstance().subscribeForDeepLink { result ->
    // The app's own deep link routing.
    command.onDeepLinking(result)
}
```

The SDK holds a single deep link listener, so a second `subscribeForDeepLink` silently replaces the first. It drops results that arrive with no listener, and requests the deferred deep link only if a listener is set by the first foreground, so subscribe before the first Activity resumes. `DeepLinkListener` and `DeepLinkResult` are in `com.appsflyer.share.deeplink`. The `initialize` settings `one_link_custom_domains`, `deep_link_parameters`, `push_notification_deep_link_path` and `deep_link_timeout` (milliseconds), and the `resolvedeeplinkurls` command, configure how the SDK resolves deep links.

In both modes, the app must call `AppsFlyerLib.getInstance().collectDataFromLauncherActivity(activity)` in the launcher Activity's `onCreate`, before `start()`. SDK 7 collects app-open and web referrer data only through this call, and the RemoteCommand does not call it.

## App-side responsibilities

The RemoteCommand does not cover these, the app does:

- Uninstall measurement: call `updateServerUninstallToken` from the app's Firebase messaging service.
- Purchase validation (`validateAndLogInAppPurchase`) and the Purchase Connector are not exposed as commands.
- `af_init_config.json`: if the app ships this AppsFlyer file, the SDK applies it inside `init()`, so its values can override settings the command applied earlier.

## Documentation
For full documentation, please see the Tealium Learning Community: 

[https://docs.tealium.com/platforms/remote-commands/integrations/appsflyer/](https://docs.tealium.com/platforms/remote-commands/integrations/appsflyer/)

## License

Use of this software is subject to the terms and conditions of the license agreement contained in the file titled "LICENSE.txt".  Please read the license before downloading or using any of the files contained in this repository. By downloading or using any of these files, you are agreeing to be bound by and comply with the license agreement.
 
---
Copyright (C) 2012-2026, Tealium Inc.