# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.5.0] - 2026-04-30

### Added
- New remote commands:
  - User data: `setphonenumber`, `anonymizeuser` (`disabledevicetracking` as backwards-compatible alias)
  - Consent & ad revenue: `setconsentdata` (GDPR/DMA flags), `logadrevenue`
  - Partner management: `setpartnerdata`, `setsharingfilterforpartners`
  - Tracking control: `start`, `stoptracking` (`disabletracking` as iOS cross-platform alias), `logsession`
  - Deep links: `resolvedeeplinkurls` (with legacy TiQ alias `resolve_deep_links`)
  - Device identifiers: `setoaid`, `setandroidid`, `setimei`
  - Attribution: `setoutofstore`, `setpreinstallattribution`, `setisupdate`
  - Misc: `setdisablenetworkdata`, `setappinviteonelink`
- New initialize settings:
  - `log_level` — granular SDK log level (NONE/ERROR/WARNING/INFO/DEBUG/VERBOSE)
  - `deep_link_parameters` — calls `appendParametersToDeepLinkingURL` before `start()`
  - `enable_facebook_deferred_applinks`, `enable_tcf_data_collection`, `one_link_custom_domains`
  - `disable_advertising_identifiers` (`disable_ad_tracking` as iOS alias), `disable_app_set_id`
  - `collect_android_id`, `collect_imei`
- `AppsFlyerCommandError` for structured per-command error handling
- `RemoteCommandLogger` and `RemoteCommandLogLevel` for controllable internal logging
- Comprehensive unit test coverage for all new commands and settings

### Changed
- Refactoring:
  - Introduce type-safe `Command` enum with exhaustive `when` dispatch in `AppsFlyerRemoteCommand`
  - Extract `EmailCryptTypeMapping`, `LogLevelMapping`, and `MediationNetworkMapping` into dedicated files
  - Refactor settings access to use the new `Settings` object in `AppsFlyerInstance` and `AppsFlyerRemoteCommand`
- Dependencies:
  - Upgrade Tealium Kotlin core to 1.9.1
  - Upgrade Robolectric to 4.15.1
  - Update Gradle configurations, Kotlin, and Android SDK to latest versions

### Fixed
- Use safe casting for settings parameters during initialization
- One failing command no longer blocks execution of subsequent commands in the same payload

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
