@file:JvmName("AppsflyerConstants")

package com.tealium.remotecommands.appsflyer

import com.appsflyer.AFInAppEventType
import com.appsflyer.AFLogger

object Commands {
    const val COMMAND_KEY = "command_name"
    const val SEPARATOR = ","

    const val INITIALIZE = "initialize"
    const val LAUNCH = "launch"
    const val TRACK_LOCATION = "tracklocation"
    const val LOG_SESSION = "logsession"
    const val SET_HOST = "sethost"
    const val SET_USER_EMAILS = "setuseremails"
    const val SET_CURRENCY_CODE = "setcurrencycode"
    const val SET_CUSTOMER_ID = "setcustomerid"
    const val DISABLE_DEVICE_TRACKING = "disabledevicetracking"
    const val RESOLVE_DEEPLINK_URLS = "resolvedeeplinkurls"
    const val STOP_TRACKING = "stoptracking"
    const val ANONYMIZE_USER = "anonymizeuser"
    const val LOG_AD_REVENUE = "logadrevenue"
    const val SET_DMA_CONSENT = "setdmaconsent"
    const val SET_PHONE_NUMBER = "setphonenumber"
    const val ADD_PUSH_NOTIFICATION_DEEP_LINK_PATH = "addpushnotificationdeeplinkpath"
    const val VALIDATE_AND_LOG_PURCHASE = "validateandlogpurchase"
    const val SET_SHARING_FILTER_FOR_PARTNERS = "setsharingfilterforpartners"
    const val APPEND_CUSTOM_DATA = "appendcustomdata"
    const val SET_DEVICE_LANGUAGE = "setcurrentdevicelanguage"
    const val SET_PARTNER_DATA = "setpartnerdata"
    const val DISABLE_TRACKING = "disabletracking"
    const val SET_IS_UPDATE = "setisupdate"
}

object StandardEvents {
    const val EVENT_PARAMETERS = "event_parameters"
    const val EVENT_PARAMETERS_SHORT = "event"
    val eventNames = mapOf(
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
        "spentcredits" to AFInAppEventType.SPENT_CREDIT,
        "achievementunlocked" to AFInAppEventType.ACHIEVEMENT_UNLOCKED,
        "contentview" to AFInAppEventType.CONTENT_VIEW,
        "listview" to AFInAppEventType.LIST_VIEW,
        "adclick" to AFInAppEventType.AD_CLICK,
        "adview" to AFInAppEventType.AD_VIEW,
        "share" to AFInAppEventType.SHARE,
        "invite" to AFInAppEventType.INVITE,
        "login" to AFInAppEventType.LOGIN,
        "reengage" to AFInAppEventType.RE_ENGAGE,
        "openfrompushnotification" to AFInAppEventType.OPENED_FROM_PUSH_NOTIFICATION,
        "update" to AFInAppEventType.UPDATE,
        "search" to AFInAppEventType.SEARCH,
        "travelbooking" to AFInAppEventType.TRAVEL_BOOKING,
        "locationcoordinates" to AFInAppEventType.LOCATION_COORDINATES,
        "customersegment" to AFInAppEventType.CUSTOMER_SEGMENT
    )
}

object Config {
    const val DEBUG = "debug"
    const val MIN_TIME_BETWEEN_SESSIONS = "time_between_sessions"
    const val ANONYMIZE_USER = "anonymize_user"
    const val CUSTOM_DATA = "custom_data"
    const val SETTINGS = "settings"
    const val DEV_KEY = "app_dev_key"
    const val APP_ID = "app_id"
    const val DISABLE_AD_TRACKING = "disable_ad_tracking"
    const val DISABLE_NETWORK_DATA = "disable_network_data"
    const val DISABLE_APPSET_ID = "disable_appset_id"
    const val COLLECT_DEVICE_NAME = "collect_device_name"
    const val ENABLE_TCF_DATA_COLLECTION = "enable_tcf_data_collection"
    const val APP_INVITE_ONELINK_ID = "app_invite_onelink_id"
    const val DEEPLINK_TIMEOUT = "deeplink_timeout"
    const val ONELINK_CUSTOM_DOMAINS = "onelink_custom_domains"
    const val RESOLVE_DEEP_LINKS = "resolve_deep_links"
    const val CUSTOMER_EMAILS = "customer_emails"
    const val EMAIL_HASH_TYPE = "email_hash_type"
    const val HOST = "host"
    const val HOST_PREFIX = "host_prefix"
    const val COLLECT_ANDROID_ID = "collect_android_id"
    const val COLLECT_IMEI = "collect_imei"
    const val COLLECT_OAID = "collect_oaid"
    const val LOG_LEVEL = "log_level"
    const val WAIT_FOR_CUSTOMER_USER_ID = "wait_for_customer_user_id"
    const val ENABLE_FACEBOOK_DEFERRED_APPLINKS = "enable_facebook_deferred_applinks"
    const val OUT_OF_STORE = "out_of_store"
    const val IS_UPDATE = "is_update"
    const val EXTENSION = "extension"
    const val PREINSTALL_ATTRIBUTION = "preinstall_attribution"
    const val APPEND_PARAMETERS_TO_DEEPLINK_URL = "append_parameters_to_deeplink_url"
    const val ANDROID_ID_DATA = "android_id_data"
    const val IMEI_DATA = "imei_data"
    const val OAID_DATA = "oaid_data"
}

object EventParameters {
    const val CONTENT = "af_content"
    const val ACHIEVEMENT_ID = "af_achievement_id"
    const val LEVEL = "af_level"
    const val SCORE = "af_score"
    const val SUCCESS = "af_success"
    const val PRICE = "af_price"
    const val CONTENT_TYPE = "af_content_type"
    const val CONTENT_ID = "af_content_id"
    const val CONTENT_LIST = "af_content_list"
    const val CURRENCY = "af_currency"
    const val QUANTITY = "af_quantity"
    const val REGISTRATION_METHOD = "af_registration_method"
    const val PAYMENT_INFO_AVAILABLE = "af_payment_info_available"
    const val MAX_RATING_VALUE = "af_max_rating_value"
    const val RATING_VALUE = "af_rating_value"
    const val SEARCH_STRING = "af_search_string"
    
    const val LAT = "af_lat"
    const val LONG = "af_long"
    
    const val CUSTOMER_USER_ID = "af_customer_user_id"
    
    const val VALIDATED = "af_validated"
    const val REVENUE = "af_revenue"
    const val PROJECTED_REVENUE = "af_projected_revenue"
    const val RECEIPT_ID = "af_receipt_id"
    const val TUTORIAL_ID = "af_tutorial_id"
    const val VIRTUAL_CURRENCY_NAME = "af_virtual_currency_name"
    
    const val DEEP_LINK = "af_deep_link"
    
    const val OLD_VERSION = "af_old_version"
    const val NEW_VERSION = "af_new_version"
    const val REVIEW_TEXT = "af_review_text"
    const val COUPON_CODE = "af_coupon_code"
    const val ORDER_ID = "af_order_id"
    
    const val DATE_A = "af_date_a"
    const val DATE_B = "af_date_b"
    const val DESTINATION_A = "af_destination_a"
    const val DESTINATION_B = "af_destination_b"
    const val DESCRIPTION = "af_description"
    const val CLASS = "af_class"
    const val EVENT_START = "af_event_start"
    const val EVENT_END = "af_event_end"
    
    const val PARAM_1 = "af_param_1"
    const val PARAM_2 = "af_param_2"
    const val PARAM_3 = "af_param_3"
    const val PARAM_4 = "af_param_4"
    const val PARAM_5 = "af_param_5"
    const val PARAM_6 = "af_param_6"
    const val PARAM_7 = "af_param_7"
    const val PARAM_8 = "af_param_8"
    const val PARAM_9 = "af_param_9"
    const val PARAM_10 = "af_param_10"
    
    const val DEPARTING_DEPARTURE_DATE = "af_departing_departure_date"
    const val RETURNING_DEPARTURE_DATE = "af_returning_departure_date"
    const val DESTINATION_LIST = "af_destination_list"
    const val CITY = "af_city"
    const val REGION = "af_region"
    const val COUNTRY = "af_country"
    const val DEPARTING_ARRIVAL_DATE = "af_departing_arrival_date"
    const val RETURNING_ARRIVAL_DATE = "af_returning_arrival_date"
    const val SUGGESTED_DESTINATIONS = "af_suggested_destinations"
    const val TRAVEL_START = "af_travel_start"
    const val TRAVEL_END = "af_travel_end"
    const val NUM_ADULTS = "af_num_adults"
    const val NUM_CHILDREN = "af_num_children"
    const val NUM_INFANTS = "af_num_infants"
    const val SUGGESTED_HOTELS = "af_suggested_hotels"
    const val USER_SCORE = "af_user_score"
    const val HOTEL_SCORE = "af_hotel_score"
    const val PURCHASE_CURRENCY = "af_purchase_currency"
    const val PREFERRED_NEIGHBORHOODS = "af_preferred_neighborhoods"
    const val PREFERRED_NUM_STOPS = "af_preferred_num_stops"
    const val PREFERRED_PRICE_RANGE = "af_preferred_price_range"
    const val PREFERRED_STAR_RATINGS = "af_preferred_star_ratings"
    
    const val AD_REVENUE_AD_TYPE = "af_adrev_ad_type"
    const val AD_REVENUE_NETWORK_NAME = "af_adrev_network_name"
    const val AD_REVENUE_PLACEMENT_ID = "af_adrev_placement_id"
    const val AD_REVENUE_AD_SIZE = "af_adrev_ad_size"
    const val AD_REVENUE_MEDIATED_NETWORK_NAME = "af_adrev_mediated_network_name"
}

object Customer {
    const val USER_ID = "af_customer_user_id"
    const val EMAILS = "customer_emails"
    const val EMAIL_HASH_TYPE = "email_hash_type"
}

object Location {
    const val LATITUDE = "af_lat"
    const val LONGITUDE = "af_long"
}

object Host {
    const val HOST = "host"
    const val HOST_PREFIX = "host_prefix"
}

object TransactionProperties {
    const val CURRENCY = "af_currency"
}

object DeepLink {
    const val URLS = "af_deep_link"
    const val RESOLVE_DEEP_LINKS = "resolve_deep_links"
}

object Tracking {
    const val DISABLE_DEVICE_TRACKING = "disable_device_tracking"
    const val STOP_TRACKING = "stop_tracking"
    const val GCD_IS_FIRST_LAUNCH = "is_first_launch"
}

object AdRevenue {
    const val AD_MONETIZATION_NETWORK = "monetization_network"
    const val AD_MEDIATION_NETWORK = "mediation_network"
    const val AD_REVENUE = "revenue"
    const val AD_ADDITIONAL_PARAMETERS = "additional_parameters"
}

object DMAConsent {
    const val GDPR_APPLIES = "gdpr_applies"
    const val CONSENT_FOR_DATA_USAGE = "consent_for_data_usage"
    const val CONSENT_FOR_ADS_PERSONALIZATION = "consent_for_ads_personalization"
    const val CONSENT_FOR_AD_STORAGE = "consent_for_ad_storage"
}

object PushNotification {
    const val DEEP_LINK_PATH = "push_notification_deep_link_path"
    const val PAYLOAD = "push_payload"
}

object PurchaseValidation {
    const val PURCHASE_TYPE = "purchase_type"
    const val TRANSACTION_ID = "transaction_id"
    const val PRODUCT_ID = "product_id"
    const val PRICE = "price"
    const val CURRENCY = "currency"
    const val ADDITIONAL_PARAMETERS = "purchase_additional_parameters"
}

object Partner {
    const val SHARING_FILTER_PARTNERS = "sharing_filter_partners"
    const val CUSTOM_DATA_TO_APPEND = "custom_data_to_append"
    const val DEVICE_LANGUAGE = "device_language"
    const val PARTNER_ID = "partner_id"
    const val PARTNER_INFO = "partner_info"
}

object DeepLinkParameters {
    const val URL_CONTAINS = "url_contains"
    const val URL_PARAMETERS = "url_parameters"
}

object Attribution {
    const val APP_OPEN = "app_open_attribution"
    const val APP_OPEN_FAILURE = "app_open_attribution_failure"
    const val FIRST_LAUNCH = "is_first_launch"
    const val CONVERSION_RECEIVED = "conversion_data_received"
    const val CONVERSION_FAILURE = "conversion_data_failure"
    const val ERROR_NAME = "error_name"
    const val ERROR_DESCRIPTION = "error_description"
    const val STATUS = "af_status"
    const val SOURCE = "source"
    const val CAMPAIGN = "campaign"
    const val ERROR = "appsflyer_error"
}

object PhoneNumber {
    const val PHONE_NUMBER = "phone_number"
}

object AnonymizeUser {
    const val SHOULD_ANONYMIZE = "anonymize_user"
}

// New objects for additional Android SDK parameters
object DeviceData {
    const val ANDROID_ID_DATA = "android_id_data"
    const val IMEI_DATA = "imei_data"
    const val OAID_DATA = "oaid_data"
}

object SharingFilter {
    const val SHARING_FILTER_TYPE = "sharing_filter_type"
    const val SHARING_FILTER_VALUES = "sharing_filter_values"
}

object PreinstallAttribution {
    const val MEDIA_SOURCE = "preinstall_media_source"
    const val CAMPAIGN = "preinstall_campaign"
    const val SITE_ID = "preinstall_site_id"
}

object AppUpdate {
    const val IS_UPDATE = "is_update"
}

object OutOfStore {
    const val SOURCE = "out_of_store"
}

object Extension {
    const val EXTENSION = "extension"
}

enum class LogLevel(val value: String) {
    NONE("NONE"),
    ERROR("ERROR"),
    WARNING("WARNING"),
    INFO("INFO"),
    DEBUG("DEBUG"),
    VERBOSE("VERBOSE");

    fun toAppsFlyerLogLevel(): AFLogger.LogLevel {
        return when (this) {
            NONE -> AFLogger.LogLevel.NONE
            ERROR -> AFLogger.LogLevel.ERROR
            WARNING -> AFLogger.LogLevel.WARNING
            INFO -> AFLogger.LogLevel.INFO
            DEBUG -> AFLogger.LogLevel.DEBUG
            VERBOSE -> AFLogger.LogLevel.VERBOSE
        }
    }

    companion object {
        fun fromString(value: String): LogLevel? {
            return entries.find { it.value.equals(value, ignoreCase = true) }
        }
    }
}

enum class MediationNetwork(val value: String) {
    IRONSOURCE("ironsource"),
    APPLOVIN_MAX("applovinmax"),
    GOOGLE_ADMOB("googleadmob"),
    FYBER("fyber"),
    APPODEAL("appodeal"),
    ADMOST("admost"),
    TOPON("topon"),
    TRADPLUS("tradplus"),
    YANDEX("yandex"),
    CHARTBOOST("chartboost"),
    UNITY("unity"),
    TOPON_PTE("toponpte"),
    CUSTOM_MEDIATION("custommediation"),
    DIRECT_MONETIZATION("directmonetizationnetwork");

    fun toAppsFlyerEnum(): com.appsflyer.MediationNetwork {
        return when (this) {
            IRONSOURCE -> com.appsflyer.MediationNetwork.IRONSOURCE
            APPLOVIN_MAX -> com.appsflyer.MediationNetwork.APPLOVIN_MAX
            GOOGLE_ADMOB -> com.appsflyer.MediationNetwork.GOOGLE_ADMOB
            FYBER -> com.appsflyer.MediationNetwork.FYBER
            APPODEAL -> com.appsflyer.MediationNetwork.APPODEAL
            ADMOST -> com.appsflyer.MediationNetwork.ADMOST
            TOPON -> com.appsflyer.MediationNetwork.TOPON
            TRADPLUS -> com.appsflyer.MediationNetwork.TRADPLUS
            YANDEX -> com.appsflyer.MediationNetwork.YANDEX
            CHARTBOOST -> com.appsflyer.MediationNetwork.CHARTBOOST
            UNITY -> com.appsflyer.MediationNetwork.UNITY
            TOPON_PTE -> com.appsflyer.MediationNetwork.TOPON_PTE
            CUSTOM_MEDIATION -> com.appsflyer.MediationNetwork.CUSTOM_MEDIATION
            DIRECT_MONETIZATION -> com.appsflyer.MediationNetwork.DIRECT_MONETIZATION_NETWORK
        }
    }

    companion object {
        fun fromString(value: String): MediationNetwork? {
            return entries.find { it.value.equals(value, ignoreCase = true) }
        }
    }
}

enum class EmailHashType(val value: Int) {
    NONE(0),
    SHA256(3);

    companion object {
        fun fromInt(value: Int): EmailHashType? {
            return entries.find { it.value == value }
        }
    }
}
