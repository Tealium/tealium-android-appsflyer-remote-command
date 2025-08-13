@file:JvmName("AppsflyerConstants")

package com.tealium.remotecommands.appsflyer

import com.appsflyer.AFInAppEventType

object Commands {
    const val COMMAND_KEY = "command_name"
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
    const val RESOLVE_DEEPLINK_URLS = "resolvedeeplinkurls"
    const val STOP_TRACKING = "stoptracking"
}

object StandardEvents {
    const val EVENT_PARAMETERS = "event_parameters"
    const val EVENT_PARAMETERS_SHORT = "event"
    
    // Official AppsFlyer Standard Events from documentation
    // https://dev.appsflyer.com/hc/docs/in-app-events-android
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

object Config {
    const val APP_ID = "app_id"
    const val DEV_KEY = "app_dev_key"
    const val SETTINGS = "settings"
}

object Settings {
    const val ANONYMIZE_USER = "anonymize_user"
    const val COLLECT_DEVICE_NAME = "collect_device_name"
    const val CUSTOM_DATA = "custom_data"
    const val DEBUG = "debug"
    const val DISABLE_AD_TRACKING = "disable_ad_tracking"
    const val DISABLE_APPLE_AD_TRACKING = "disable_apple_ad_tracking"
    const val TIME_BETWEEN_SESSIONS = "time_between_sessions"
    const val PUSH_NOTIFICATION_DEEP_LINK_PATH = "push_notification_deep_link_path"
}

object Customer {
    const val USER_ID = "af_customer_user_id"
    const val EMAILS = "customer_emails"
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
}

object Tracking {
    const val ANONYMIZE_USER = "anonymize_user"
    const val STOP_TRACKING = "stop_tracking"
    const val GCD_IS_FIRST_LAUNCH = "is_first_launch"
}

object PhoneNumberParam {
    const val PHONE_NUMBER = "phone_number"
}

object AdRevenueParams {
    const val MONETIZATION_NETWORK = "monetization_network"
    const val MEDIATION_NETWORK = "mediation_network"
    const val AD_REVENUE_CURRENCY = "ad_revenue_currency"
    const val AD_REVENUE_AMOUNT = "ad_revenue_amount"
    const val AD_REVENUE_ADDITIONAL_PARAMS = "ad_revenue_additional_params"
}

object MediationNetworks { 
    val networkNames = mapOf(
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

object ConsentDataParams {
    const val IS_USER_SUBJECT_TO_GDPR = "is_user_subject_to_gdpr"
    const val HAS_CONSENT_FOR_DATA_USAGE = "has_consent_for_data_usage"
    const val HAS_CONSENT_FOR_ADS_PERSONALIZATION = "has_consent_for_ads_personalization"
    const val HAS_CONSENT_FOR_AD_STORAGE = "has_consent_for_ad_storage"
}

object PartnerDataParams {
    const val PARTNER_ID = "partner_id"
    const val PARTNER_INFO = "partner_info"
}

object SharingFilterParams {
    const val SHARING_FILTER = "sharing_filter"
}
