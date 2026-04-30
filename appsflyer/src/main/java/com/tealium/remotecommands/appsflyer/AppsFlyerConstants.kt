@file:JvmName("AppsflyerConstants")

package com.tealium.remotecommands.appsflyer

import com.appsflyer.AFInAppEventType
import java.util.Locale

object Commands {
    const val COMMAND_KEY = "command_name"
    const val METHOD_KEY = "method"
    const val SEPARATOR = ","
    const val INITIALIZE = "initialize"
    const val TRACK_LOCATION = "tracklocation"
    const val SET_HOST = "sethost"
    const val SET_USER_EMAILS = "setuseremails"
    const val SET_CURRENCY_CODE = "setcurrencycode"
    const val SET_CUSTOMER_ID = "setcustomerid"
    const val SET_PHONE_NUMBER = "setphonenumber"
    const val LOG_AD_REVENUE = "logadrevenue"
    const val SET_CONSENT_DATA = "setconsentdata"
    const val SET_PARTNER_DATA = "setpartnerdata"
    const val SET_SHARING_FILTER_FOR_PARTNERS = "setsharingfilterforpartners"
    const val ANONYMIZE_USER = "anonymizeuser"
    const val DISABLE_DEVICE_TRACKING = "disabledevicetracking"
    const val RESOLVE_DEEPLINK_URLS = "resolvedeeplinkurls"
    const val START = "start"
    const val STOP_TRACKING = "stoptracking"
    const val DISABLE_TRACKING = "disabletracking"
    const val LOG_SESSION = "logsession"
    const val SET_OAID = "setoaid"
    const val SET_ANDROID_ID = "setandroidid"
    const val SET_IMEI = "setimei"
    const val SET_OUT_OF_STORE = "setoutofstore"
    const val SET_DISABLE_NETWORK_DATA = "setdisablenetworkdata"
    const val SET_APP_INVITE_ONE_LINK = "setappinviteonelink"
    const val SET_PREINSTALL_ATTRIBUTION = "setpreinstallattribution"
    const val SET_IS_UPDATE = "setisupdate"
}

/**
 * Type-safe enum of AppsFlyer remote commands.
 * Wraps the string command names so [AppsFlyerRemoteCommand] can dispatch via
 * exhaustive `when` and catch unknown commands at parse time.
 */
enum class Command(val commandName: String) {
    /**
     * Initializes the AppsFlyer SDK with a dev key and optional settings.
     * @see https://dev.appsflyer.com/hc/docs/integrate-android-sdk
     */
    INITIALIZE("initialize"),

    /**
     * Logs a location event with latitude and longitude.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#tracklocation
     */
    TRACK_LOCATION("tracklocation"),

    /**
     * Sets a custom host for the AppsFlyer SDK.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#sethost
     */
    SET_HOST("sethost"),

    /**
     * Sets user email addresses for cross-platform attribution.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setuseremails
     */
    SET_USER_EMAILS("setuseremails"),

    /**
     * Sets the currency code for in-app purchase events.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setcurrencycode
     */
    SET_CURRENCY_CODE("setcurrencycode"),

    /**
     * Sets a customer user ID for cross-referencing with internal user records.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setcustomeruserid
     */
    SET_CUSTOMER_ID("setcustomerid"),

    /**
     * Sets the user's phone number for attribution.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setphonenumber
     */
    SET_PHONE_NUMBER("setphonenumber"),

    /**
     * Logs ad revenue from a mediation network.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#logadrevenue
     */
    LOG_AD_REVENUE("logadrevenue"),

    /**
     * Sets GDPR and DMA consent data.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerconsent
     */
    SET_CONSENT_DATA("setconsentdata"),

    /**
     * Sets additional data to share with a specific partner.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setpartnerdata
     */
    SET_PARTNER_DATA("setpartnerdata"),

    /**
     * Sets a list of partner IDs to exclude from data sharing.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setsharingfilterforpartners
     */
    SET_SHARING_FILTER_FOR_PARTNERS("setsharingfilterforpartners"),

    /**
     * Anonymizes the user for GDPR compliance.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#anonymizeuser
     */
    ANONYMIZE_USER("anonymizeuser"),

    /**
     * Alias for ANONYMIZE_USER — accepted for backwards compatibility.
     * The original Android command name mapped to anonymizeUser() internally;
     * the name "disabledevicetracking" was misleading so it was standardised to "anonymizeuser".
     */
    DISABLE_DEVICE_TRACKING("disabledevicetracking"),

    /**
     * Registers URLs that should be resolved as deep links.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#resolvedeeplinkurls
     */
    RESOLVE_DEEPLINK_URLS("resolvedeeplinkurls"),

    /**
     * Explicitly starts the SDK — useful after stoptracking to resume attribution.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#start
     */
    START("start"),

    /**
     * Stops the AppsFlyer SDK from sending attribution data.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#stoptracking
     */
    STOP_TRACKING("stoptracking"),

    /**
     * iOS command name for the same stop-tracking intent — accepted here so that
     * cross-platform payloads work on both platforms without separate TiQ tags.
     */
    DISABLE_TRACKING("disabletracking"),

    /**
     * Manually logs an af_app_opened session event.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#logsession
     */
    LOG_SESSION("logsession"),

    /**
     * Manually sets the OAID when auto-collection is unavailable.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setoaiddata
     */
    SET_OAID("setoaid"),

    /**
     * Manually sets the Android ID when auto-collection is unavailable (Android > 4.4 without Google Play Services).
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setandroididdata
     */
    SET_ANDROID_ID("setandroidid"),

    /**
     * Manually sets the device IMEI when auto-collection is unavailable (Android > 4.4 without Google Play Services).
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setimeidata
     */
    SET_IMEI("setimei"),

    /**
     * Sets the alternative app store name for attribution.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setoutofstore
     */
    SET_OUT_OF_STORE("setoutofstore"),

    /**
     * Opts out of collecting carrier/SIM operator name.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setdisablenetworkdata
     */
    SET_DISABLE_NETWORK_DATA("setdisablenetworkdata"),

    /**
     * Sets the OneLink ID used for user-invite links.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setappinviteonelink
     */
    SET_APP_INVITE_ONE_LINK("setappinviteonelink"),

    /**
     * Attributes a preinstalled app to a media source and campaign.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setpreinstallattribution
     */
    SET_PREINSTALL_ATTRIBUTION("setpreinstallattribution"),

    /**
     * Manually flags that the app was updated when auto-detection fails.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setisupdate
     */
    SET_IS_UPDATE("setisupdate");

    companion object {
        // Cached to avoid re-allocating an array on every call to fromString.
        private val BY_NAME: Map<String, Command> = Command.entries.associateBy { it.commandName }

        /**
         * Resolves a command string to a [Command]. Returns null when the string is
         * not a built-in command — callers should fall back to treating it as a
         * custom event name.
         */
        fun fromString(command: String): Command? = BY_NAME[command.lowercase(Locale.ROOT).trim()]
    }
}

/**
 * Standard AppsFlyer in-app event names.
 * @see https://dev.appsflyer.com/hc/docs/in-app-events-android
 */
object StandardEvents {
    const val EVENT_PARAMETERS = "event_parameters"
    const val EVENT_PARAMETERS_SHORT = "event"

    val eventNames: Map<String, String> = mapOf(
        "levelachieved" to AFInAppEventType.LEVEL_ACHIEVED,
        "addpaymentinfo" to AFInAppEventType.ADD_PAYMENT_INFO,
        "addtocart" to AFInAppEventType.ADD_TO_CART,
        "addtowishlist" to AFInAppEventType.ADD_TO_WISH_LIST,
        "completeregistration" to AFInAppEventType.COMPLETE_REGISTRATION,
        "tutorialcompletion" to AFInAppEventType.TUTORIAL_COMPLETION,
        "initiatecheckout" to AFInAppEventType.INITIATED_CHECKOUT,
        "purchase" to AFInAppEventType.PURCHASE,
        "subscribe" to AFInAppEventType.SUBSCRIBE,
        "starttrial" to AFInAppEventType.START_TRIAL,
        "rate" to AFInAppEventType.RATE,
        "search" to AFInAppEventType.SEARCH,
        "spentcredits" to AFInAppEventType.SPENT_CREDIT,
        "achievementunlocked" to AFInAppEventType.ACHIEVEMENT_UNLOCKED,
        "contentview" to AFInAppEventType.CONTENT_VIEW,
        "listview" to AFInAppEventType.LIST_VIEW,
        "adclick" to AFInAppEventType.AD_CLICK,
        "adview" to AFInAppEventType.AD_VIEW,
        "travelbooking" to AFInAppEventType.TRAVEL_BOOKING,
        "share" to AFInAppEventType.SHARE,
        "invite" to AFInAppEventType.INVITE,
        "login" to AFInAppEventType.LOGIN,
        "reengage" to AFInAppEventType.RE_ENGAGE,
        "openfrompushnotification" to AFInAppEventType.OPENED_FROM_PUSH_NOTIFICATION,
        "update" to AFInAppEventType.UPDATE,
        "locationcoordinates" to AFInAppEventType.LOCATION_COORDINATES,
        "customersegment" to AFInAppEventType.CUSTOMER_SEGMENT,
        // iOS naming aliases — both variants accepted on all platforms.
        "achievelevel" to AFInAppEventType.LEVEL_ACHIEVED,
        "viewedcontent" to AFInAppEventType.CONTENT_VIEW,
        "completetutorial" to AFInAppEventType.TUTORIAL_COMPLETION,
        "unlockachievement" to AFInAppEventType.ACHIEVEMENT_UNLOCKED,
        "pushnotificationopened" to AFInAppEventType.OPENED_FROM_PUSH_NOTIFICATION
    )
}

/**
 * Keys used during SDK initialization.
 * @see https://dev.appsflyer.com/hc/docs/integrate-android-sdk
 */
object Config {
    const val DEV_KEY = "app_dev_key"
    const val SETTINGS = "settings"
    const val APP_ID = "app_id"
}

/**
 * SDK-level settings applied during initialization.
 * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib
 */
object Settings {
    const val ANONYMIZE_USER = "anonymize_user"
    const val CUSTOM_DATA = "custom_data"
    const val DEBUG = "debug"
    const val TIME_BETWEEN_SESSIONS = "time_between_sessions"
    const val PUSH_NOTIFICATION_DEEP_LINK_PATH = "push_notification_deep_link_path"

    // Must be called before init() — placed first in the initialize() processing order.
    const val ENABLE_FACEBOOK_DEFERRED_APPLINKS = "enable_facebook_deferred_applinks"

    // Must be called before start().
    const val ENABLE_TCF_DATA_COLLECTION = "enable_tcf_data_collection"
    const val ONE_LINK_CUSTOM_DOMAINS = "one_link_custom_domains"
    const val DISABLE_ADVERTISING_IDENTIFIERS = "disable_advertising_identifiers"
    // iOS cross-platform alias — accepted alongside the canonical Android key.
    const val DISABLE_AD_TRACKING_ALIAS = "disable_ad_tracking"
    const val DISABLE_APP_SET_ID = "disable_app_set_id"
    const val COLLECT_ANDROID_ID = "collect_android_id"
    const val COLLECT_IMEI = "collect_imei"
    const val DEEP_LINK_PARAMETERS = "deep_link_parameters"

    // More granular than the boolean `debug` flag — one of NONE, ERROR, WARNING, INFO, DEBUG, VERBOSE.
    const val LOG_LEVEL = "log_level"
}

object Customer {
    /** Cross-references internal user records with AppsFlyer attribution data.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setcustomeruserid */
    const val USER_ID = "af_customer_user_id"

    /** List of email addresses for cross-platform attribution.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setuseremails */
    const val EMAILS = "customer_emails"

    /** Encryption type for user emails. Integer matching iOS EmailCryptType raw values (0 = NONE, 3 = SHA256).
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setuseremails */
    const val EMAIL_HASH_TYPE = "email_hash_type"
}

object Location {
    /** Latitude coordinate for tracklocation event. */
    const val LATITUDE = "af_lat"

    /** Longitude coordinate for tracklocation event. */
    const val LONGITUDE = "af_long"
}

object Host {
    /** Custom host domain.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#sethost */
    const val HOST = "host"

    /** Custom host prefix. */
    const val HOST_PREFIX = "host_prefix"
}

object TransactionProperties {
    /** ISO 4217 currency code for in-app purchase events.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setcurrencycode */
    const val CURRENCY = "af_currency"
}

object DeepLink {
    /** List of URL schemes to register as deep links.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#resolvedeeplinkurls */
    const val URLS = "af_deep_link"
    // TiQ UI labels this key as "resolve_deep_links" — accepted as a fallback to avoid silent failures.
    const val URLS_LEGACY_TIQ = "resolve_deep_links"
}

object Tracking {
    /** When true, anonymizes the user for GDPR compliance.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#anonymizeuser */
    const val ANONYMIZE_USER = "anonymize_user"

    /** When true, stops the SDK from sending attribution data.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#stoptracking */
    const val STOP_TRACKING = "stop_tracking"

    const val GCD_IS_FIRST_LAUNCH = "is_first_launch"
}

/**
 * Event names surfaced to Tealium when the AppsFlyer SDK reports attribution data
 * or errors through its conversion listener.
 */
object AttributionEvents {
    const val CONVERSION_DATA_RECEIVED = "conversion_data_received"
    const val APP_OPEN_ATTRIBUTION = "app_open_attribution"
    const val APPSFLYER_ERROR = "appsflyer_error"

    const val KEY_ERROR_NAME = "error_name"
    const val KEY_ERROR_MESSAGE = "error_message"
    const val ERROR_CONVERSION_DATA_REQUEST_FAILURE = "conversion_data_request_failure"
    const val ERROR_APP_OPEN_ATTRIBUTION_FAILURE = "app_open_attribution_failure"
}

object PhoneNumberParam {
    /** E.164-formatted phone number for attribution.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setphonenumber */
    const val PHONE_NUMBER = "phone_number"
}

/**
 * Parameters for the logAdRevenue command.
 * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#logadrevenue
 */
object AdRevenueParams {
    /** Name of the monetization network (e.g. admob, ironsource). */
    const val MONETIZATION_NETWORK = "monetization_network"

    /** Mediation network used to serve the ad. */
    const val MEDIATION_NETWORK = "mediation_network"

    /** ISO 4217 currency code for the ad revenue amount. */
    const val AD_REVENUE_CURRENCY = "ad_revenue_currency"

    /** Revenue amount earned from the ad impression. */
    const val AD_REVENUE_AMOUNT = "ad_revenue_amount"

    /** Optional map of extra parameters to attach to the ad revenue event. */
    const val AD_REVENUE_ADDITIONAL_PARAMS = "ad_revenue_additional_params"
}

/**
 * Parameters for the setConsentData command.
 * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerconsent
 */
object ConsentDataParams {
    /** Whether GDPR applies to this user. */
    const val IS_USER_SUBJECT_TO_GDPR = "is_user_subject_to_gdpr"

    /** Whether the user has consented to data collection and usage. */
    const val HAS_CONSENT_FOR_DATA_USAGE = "has_consent_for_data_usage"

    /** Whether the user has consented to personalized ads. */
    const val HAS_CONSENT_FOR_ADS_PERSONALIZATION = "has_consent_for_ads_personalization"

    /** Whether the user has consented to ad storage (DMA). */
    const val HAS_CONSENT_FOR_AD_STORAGE = "has_consent_for_ad_storage"
}

/**
 * Parameters for the setPartnerData command.
 * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setpartnerdata
 */
object PartnerDataParams {
    /** Identifier of the partner to receive additional data. */
    const val PARTNER_ID = "partner_id"

    /** Map of key-value pairs to share with the partner. */
    const val PARTNER_INFO = "partner_info"
}

/**
 * Parameters for the setSharingFilterForPartners command.
 * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setsharingfilterforpartners
 */
object SharingFilterParams {
    /** List of partner IDs to exclude from data sharing. */
    const val SHARING_FILTER = "sharing_filter"
}

/**
 * Sub-parameters for each entry in the deep_link_parameters settings array.
 * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#appendparameterstodeeplinkingurl
 */
object DeepLinkParameterEntry {
    const val CONTAINS = "contains"
    const val PARAMETERS = "parameters"
}

/**
 * Parameters for the setPreinstallAttribution command.
 * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setpreinstallattribution
 */
object PreinstallParams {
    const val MEDIA_SOURCE = "preinstall_media_source"
    const val CAMPAIGN = "preinstall_campaign"
    const val SITE_ID = "preinstall_site_id"
}

/**
 * Parameters for commands that take a single string value.
 */
object StringCommandParams {
    const val OAID = "oaid"
    const val ANDROID_ID = "android_id"
    const val IMEI = "imei"
    const val STORE_NAME = "store_name"
    const val APP_INVITE_ONE_LINK_ID = "app_invite_onelink_id"
    const val DISABLE_NETWORK_DATA = "disable_network_data"
    const val IS_UPDATE = "is_update"
    const val LOG_LEVEL = "log_level"
}
