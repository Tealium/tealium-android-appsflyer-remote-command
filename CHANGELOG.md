# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Support for setting user phone numbers
- Support for logging ad revenue
- Support for managing GDPR/CCPA consent data
- Support for setting partner data
- Support for setting partner sharing filters
- Support for anonymizing users
- New command mappings in `appsflyer.json`
- Enhanced unit test coverage for new functionalities

### Changed
- Update Gradle configurations and dependencies to latest versions
- Upgrade Kotlin, Android SDK, and Tealium libraries
- Upgrade Tealium Kotlin core to 1.9.1
- Upgrade Robolectric to 4.15.1
- Refactor settings access to use the new `Settings` object in `AppsFlyerInstance` and `AppsFlyerRemoteCommand`
- Replace placeholder values in `appsflyer.json`
- Update event names in `MainActivity` and `AppsFlyerConstants`
- Improve compile options and clean task registration in build scripts

### Fixed
- Use safe casting for settings parameters during initialization

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
