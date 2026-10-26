# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2026-10-26

### Added
- `AppsFlyerSessionMode` (required): `AUTOMATIC` lets the RemoteCommand register the session-ready listener and call `start()`; `APP_MANAGED` leaves both to the app, e.g. to wait for consent. See [Session modes](README.md#session-modes)
- Hashed-PII commands: `setuseremail`, `setuserfirstname`, `setuserlastname`, `setuserphone`, `setuserfbloginid`, `clearuserpii`. The SDK hashes values on-device, except `fb_login_id`. `setuserphone` requires String `country_code` and `phone_number`
- `setconsentdata` command (GDPR/DMA). `is_user_subject_to_gdpr` is required, the other consent keys are optional, and an invalid optional value counts as not set. Map it before `initialize` to include consent in the first session: `"launch": "setconsentdata,initialize"`
- Commands: `anonymizeuser`, `logadrevenue`, `setpartnerdata`, `setsharingfilterforpartners` (omit `sharing_filter` to reset), `logsession`, `setoaid`, `setandroidid`, `setimei`, `setoutofstore`, `setdisablenetworkdata`, `setappinviteonelink`, `setpreinstallattribution`, `setisupdate`
- `logadrevenue` takes a friendly `mediation_network` name such as `googleadmob`, `applovinmax`, `googleadmanager` or `cloudx`, or the SDK enum name
- Initialize settings: `log_level` (`NONE`, `ERROR`, `WARNING`, `INFO`, `DEBUG`, `VERBOSE`), `deep_link_timeout`, `deep_link_parameters`, `push_notification_deep_link_path`, `enable_facebook_deferred_applinks`, `enable_tcf_data_collection`, `one_link_custom_domains`, `disable_advertising_identifiers`, `disable_app_set_id`, `collect_android_id`
- Standard events `search`, `travelbooking`, `locationcoordinates` and `customersegment`
- Aliases: `disabletracking` (iOS name for `stoptracking`), `disable_ad_tracking` (for `disable_advertising_identifiers`), `resolve_deep_links` (for `af_deep_link`), and the iOS event names `achievelevel`, `viewedcontent`, `completetutorial`, `unlockachievement`, `pushnotificationopened`
- `fb_login_id` accepts a whole number or a numeric String
- Validation for `deep_link_timeout` (milliseconds) and `time_between_sessions`: both accept a whole number or a numeric String, and negative or invalid values are ignored with a warning
- `logLevel` constructor argument (`RemoteCommandLogLevel`: `DEBUG`, `INFO`, `WARNING`, `ERROR`, `SILENT`), default `ERROR`. Messages use the tag `TealiumAppsFlyer`

### Fixed
- `debug` and `log_level` are set first in `initialize`, so debug logs cover initialization
- `custom_data` forwards values that are not Strings (numbers, Booleans, lists and nested objects); 1.4.0 silently dropped them
- One failing command no longer aborts the remaining commands in the same payload
- A setting with the wrong type no longer throws during `initialize`; it is ignored. Boolean settings also accept `"true"`/`"false"` and log a warning for any other invalid value
- `setsharingfilterforpartners` rejects a `sharing_filter` that is not an array (e.g. `"all"`) instead of resetting the filter; omit it or send null to reset
- `deep_link_parameters` entries whose `parameters` contain non-String keys or values are skipped with a warning, matching iOS

### Changed
- AppsFlyer Android SDK `[7.0.2,8)` (SDK 6 is no longer supported); Tealium `remotecommands` 1.0.3 (was 1.0.1)
- **Breaking:** consumer apps need Kotlin 2.0+. SDK types moved from `com.appsflyer` to `com.appsflyer.share` (`AFAdRevenueData`, `AppsFlyerConsent`, `AppsFlyerConversionListener`, `AFInAppEventType`, `MediationNetwork`; deep link types to `com.appsflyer.share.deeplink`). Update your imports
- **Breaking:** `AppsFlyerRemoteCommand` requires `sessionMode` as its second argument, ahead of `appsFlyerDevKey`. The `AppsFlyerInstance` constructor is `internal`: use `AppsFlyerRemoteCommand.appsFlyerInstance`
- **Breaking:** `AppsFlyerCommand` interface changed (`setUserEmails` and `disableDeviceTracking` removed, new command methods, `setHost(host, hostPrefix)` takes a non-null prefix); update custom implementations
- **Breaking:** `initialize` no longer starts the session (1.4.0 called `start()` right after `init`). SDK 7 starts inside its single session-ready listener, and `AppsFlyerSessionMode` decides who owns it. In `AUTOMATIC` mode the RemoteCommand registers the listener, replacing any the app registered. In `APP_MANAGED` mode the app must register its own `SessionReadyListener` and call `start()` in it, otherwise no session starts. See [Session modes](README.md#session-modes)
- **Breaking:** SDK 7 does not keep setter values across cold starts. Send `setcustomerid`, `setconsentdata`, `anonymizeuser`, `setcurrencycode` and similar commands on every launch
- **Breaking:** `sethost` requires both `host` and `host_prefix` (the prefix was optional). `stoptracking` requires `stop_tracking` and `disabledevicetracking` requires `anonymize_user` (neither defaults to `false`)
- **Breaking:** every command listed under Added is now built in, not a custom event. If your tag sends a custom event with one of those names, rename it
- **Breaking:** custom event names keep their original case (1.4.0 lowercased them), so check your dashboards for the new names. Standard events are still matched case-insensitively
- **Breaking:** app-open attribution now comes from the SDK's deep link listener (SDK 7 removed `onAppOpenAttribution`). To keep tracking `app_open_attribution`, subscribe the RemoteCommand: `AppsFlyerLib.getInstance().subscribeForDeepLink(command)`. Apps with their own listener forward every result to `command.onDeepLinking(result)`. Deferred links are skipped, because `conversion_data_received` reports them. The payload is the OneLink click event. See [Deep links](README.md#deep-links)
- `app_open_attribution` failures are reported as `appsflyer_error` with `error_name` `app_open_attribution_failure`, and `error_message` is the SDK's deep link error name
- Events, `tracklocation` and `logadrevenue` queue until `initialize` has run with a dev key, then are sent in order
- `stoptracking` with `stop_tracking` `false` calls `start()` after `stop(false)` in `AUTOMATIC` mode, once `initialize` has run. In `APP_MANAGED` mode the app restarts the session
- `conversion_data_received` omits null values
- Nested JSON objects and arrays in event parameters, `custom_data`, `partner_info` and `ad_revenue_additional_params` are converted to Maps and Lists before they reach the SDK
- A missing or invalid required parameter skips that command and logs an error naming the key. Boolean keys (`stop_tracking`, `anonymize_user`, `disable_network_data`, `is_update`, `is_user_subject_to_gdpr`) take a Boolean or the String `"true"`/`"false"`; anything else is invalid
- Internal: built with Kotlin 2.0.21, compileSdk and targetSdk 35 and a Java 11 target; the example app runs in `AUTOMATIC` mode, uses `setuseremail`, maps the new commands and maps location to `af_lat` and `af_long`

### Removed
- **Breaking:** `setuseremails` and its `customer_emails` parameter (removed in SDK 7). Use `setuseremail` with `email`, and unmap the old command, or it is sent as a custom event
- **Breaking:** the `disable_device_tracking` key. `disabledevicetracking` (now an alias of `anonymizeuser`) reads `anonymize_user`
- **Breaking:** the `cancelpurchase` to `cancel_purchase` rename. Send `cancel_purchase` as the command name instead
- **Breaking:** the `com.appsflyer.SingleInstallBroadcastReceiver` manifest entry, because SDK 7 no longer has the class. Remove it from your manifest too if you declared it

## [1.4.0] - 2024-11-27

### Fixed
- Log Event now falls back to full payload when event parameters are not mapped

## [1.3.0] - 2023-02-14

### Added
- AppsFlyer v6.10 `setHost` support
- Custom event support

## [1.2.1] - 2022-10-04

### Changed
- Update AppsFlyer SDK version range constraint

## [1.2.0] - 2022-03-14

### Changed
- Update AppsFlyer SDK version
- Update method calls to reflect API changes in newer SDK versions

## [1.1.0] - 2020-10-22

### Added
- JSON-based remote command implementation
- Remote command context (`RC Context`) support

### Changed
- Align code style with other Tealium remote command libraries
- Update RC to latest Kotlin + RC adapter

## [1.0.1] - 2020-06-30

### Fixed
- TiQ tag integration fixes
- `setHost` fix

## [1.0.0] - 2020-05-07

### Added
- Initial release of the Tealium Android AppsFlyer Remote Command
