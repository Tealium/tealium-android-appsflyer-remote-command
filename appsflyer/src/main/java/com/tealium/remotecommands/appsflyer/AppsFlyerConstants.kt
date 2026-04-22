@file:JvmName("AppsflyerConstants")

package com.tealium.remotecommands.appsflyer

import com.appsflyer.AFInAppEventType

object Commands {
    const val COMMAND_KEY = "command_name"
    const val SEPARATOR = ","
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
     * Registers URLs that should be resolved as deep links.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#resolvedeeplinkurls
     */
    RESOLVE_DEEPLINK_URLS("resolvedeeplinkurls"),

    /**
     * Stops the AppsFlyer SDK from sending attribution data.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#stoptracking
     */
    STOP_TRACKING("stoptracking");

    companion object {
        /**
         * Resolves a command string to a [Command]. Returns null when the string is
         * not a built-in command — callers should fall back to treating it as a
         * custom event name.
         */
        fun fromString(command: String): Command? {
            val normalized = command.lowercase().trim()
            return values().find { it.commandName == normalized }
        }
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
        "customersegment" to AFInAppEventType.CUSTOMER_SEGMENT
    )
}

/**
 * Keys used during SDK initialization.
 * @see https://dev.appsflyer.com/hc/docs/integrate-android-sdk
 */
object Config {
    const val APP_ID = "app_id"
    const val DEV_KEY = "app_dev_key"
    const val SETTINGS = "settings"
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
}

object Customer {
    /** Cross-references internal user records with AppsFlyer attribution data.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setcustomeruserid */
    const val USER_ID = "af_customer_user_id"

    /** List of email addresses for cross-platform attribution.
     * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-appsflyerlib#setuseremails */
    const val EMAILS = "customer_emails"
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
 * Supported mediation network identifiers for ad revenue logging.
 * @see https://dev.appsflyer.com/hc/docs/android-sdk-reference-mediationnetworktype
 */
object MediationNetworks {
    val networkNames: Map<String, String> = mapOf(
        "googleadmob" to "GOOGLE_ADMOB",
        "ironsource" to "IRONSOURCE",
        "applovinmax" to "APPLOVIN_MAX",
        "fyber" to "FYBER",
        "appodeal" to "APPODEAL",
        "admost" to "ADMOST",
        "topon" to "TOPON",
        "tradplus" to "TRADPLUS",
        "yandex" to "YANDEX",
        "chartboost" to "CHARTBOOST",
        "unity" to "UNITY",
        "toponpte" to "TOPON_PTE",
        "custom" to "CUSTOM_MEDIATION",
        "direct" to "DIRECT_MONETIZATION_NETWORK"
    )
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
