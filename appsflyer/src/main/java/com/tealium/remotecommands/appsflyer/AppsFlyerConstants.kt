@file:JvmName("AppsflyerConstants")

package com.tealium.remotecommands.appsflyer

import com.appsflyer.AFInAppEventType
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.MediationNetwork
import com.appsflyer.AppsFlyerProperties.EmailsCryptType

object Commands {
    const val COMMAND_KEY = "command_name"
    const val SEPARATOR = ","

    const val INITIALIZE = "initialize"
    const val TRACK_LOCATION = "tracklocation"
    const val SET_HOST = "sethost"
    const val SET_USER_EMAILS = "setuseremails"
    const val SET_CURRENCY_CODE = "setcurrencycode"
    const val SET_CUSTOMER_ID = "setcustomerid"
    const val ANONYMIZE_USER = "anonymizeuser"
    const val RESOLVE_DEEPLINK_URLS = "resolvedeeplinkurls"
    const val STOP_TRACKING = "stoptracking"
    const val LOG_AD_REVENUE = "logadrevenue"
    const val ENABLE_APPSET_ID = "enableappsetid"
    const val SET_DMA_CONSENT = "setdmaconsent"
    const val SET_DISABLE_NETWORK_DATA = "setdisablenetworkdata"
    const val SET_PHONE_NUMBER = "setphonenumber"
    const val SET_OUT_OF_STORE = "setoutofstore"
    const val ADD_PUSH_NOTIFICATION_DEEP_LINK_PATH = "addpushnotificationdeeplinkpath"
    const val SEND_PUSH_NOTIFICATION_DATA = "sendpushnotificationdata"
    const val VALIDATE_AND_LOG_PURCHASE = "validateandlogpurchase"
    const val LOG_SESSION = "logsession"
    const val WAIT_FOR_CUSTOMER_USER_ID = "waitforcustomeruserid"
    const val SET_CUSTOMER_ID_AND_LOG_SESSION = "setcustomeridandlogsession"
    const val SET_MIN_TIME_BETWEEN_SESSIONS = "setmintimebetweensessions"
    const val SET_APP_ID = "setappid"
    const val SET_DISABLE_ADVERTISING_IDENTIFIERS = "setdisableadvertisingidentifiers"
    const val ENABLE_TCF_DATA_COLLECTION = "enabletcfdatacollection"
    const val SET_SHARING_FILTER_FOR_PARTNERS = "setsharingfilterforpartners"
    const val UPDATE_SERVER_UNINSTALL_TOKEN = "updateserveruninstalltoken"
    const val SET_IS_UPDATE = "setisupdate"
    const val SET_ADDITIONAL_DATA = "setadditionaldata"
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
        "customersegment" to AFInAppEventType.CUSTOMER_SEGMENT,
        "locationchanged" to AFInAppEventType.LOCATION_CHANGED,
        "locationcoordinates" to AFInAppEventType.LOCATION_COORDINATES,
        "orderid" to AFInAppEventType.ORDER_ID
    )
}

object Config {
    const val DEBUG = "debug"
    const val MIN_TIME_BETWEEN_SESSIONS = "time_between_sessions"
    const val ANONYMIZE_USER = "anonymize_user"
    const val CUSTOM_DATA = "custom_data"
    const val DEV_KEY = "app_dev_key"
    const val DISABLE_NETWORK_DATA = "disable_network_data"
    const val ENABLE_APPSET_ID = "enable_appset_id"
    const val COLLECT_DEVICE_NAME = "collect_device_name"
    const val DISABLE_AD_TRACKING = "disable_ad_tracking"
    const val DISABLE_APPLE_AD_TRACKING = "disable_apple_ad_tracking"
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
}

object Tracking {
    const val ANONYMIZE_USER = "anonymize_user"
    const val STOP_TRACKING = "stop_tracking"
    const val GCD_IS_FIRST_LAUNCH = "is_first_launch"
}

object DMAConsent {
    const val GDPR_APPLIES = "gdpr_applies"
    const val CONSENT_FOR_DATA_USAGE = "consent_for_data_usage"
    const val CONSENT_FOR_ADS_PERSONALIZATION = "consent_for_ads_personalization"
    const val CONSENT_FOR_AD_STORAGE = "consent_for_ad_storage"
}

object AdRevenue {
    const val MONETIZATION_NETWORK = "monetization_network"
    const val MEDIATION_NETWORK = "mediation_network"
    const val REVENUE = "revenue"
    const val CURRENCY = "currency"
    const val ADDITIONAL_PARAMETERS = "ad_revenue_parameters"
}

enum class MediationNetworkType(val value: String) {
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
    DIRECT_MONETIZATION_NETWORK("directmonetizationnetwork");
    
    companion object {
        fun fromString(value: String): MediationNetworkType? {
            return entries.find { it.value == value.lowercase() }
        }
    }

    fun toMediationNetwork(): MediationNetwork {
        return when (this) {
            IRONSOURCE -> MediationNetwork.IRONSOURCE
            APPLOVIN_MAX -> MediationNetwork.APPLOVIN_MAX
            GOOGLE_ADMOB -> MediationNetwork.GOOGLE_ADMOB
            FYBER -> MediationNetwork.FYBER
            APPODEAL -> MediationNetwork.APPODEAL
            ADMOST -> MediationNetwork.ADMOST
            TOPON -> MediationNetwork.TOPON
            TRADPLUS -> MediationNetwork.TRADPLUS
            YANDEX -> MediationNetwork.YANDEX
            CHARTBOOST -> MediationNetwork.CHARTBOOST
            UNITY -> MediationNetwork.UNITY
            TOPON_PTE -> MediationNetwork.TOPON_PTE
            CUSTOM_MEDIATION -> MediationNetwork.CUSTOM_MEDIATION
            DIRECT_MONETIZATION_NETWORK -> MediationNetwork.DIRECT_MONETIZATION_NETWORK
        }
    }
}

enum class EmailHashType(val value: String) {
    NONE("none"),
    SHA256("sha256");
    
    companion object {
        fun fromString(value: String?): EmailHashType? {
            return when (value?.lowercase()) {
                "none" -> NONE
                "sha256" -> SHA256
                else -> null
            }
        }
    }
    
    fun toAppsFlyerCryptType(): EmailsCryptType {
        return when (this) {
            NONE -> EmailsCryptType.NONE
            SHA256 -> EmailsCryptType.SHA256
        }
    }
}

object PushNotification {
    const val DEEP_LINK_PATH = "push_deep_link_path"
}

object OutOfStore {
    const val SOURCE_NAME = "out_of_store_source"
}

object PhoneNumber {
    const val PHONE_NUMBER = "phone_number"
}

object InAppPurchase {
    const val PURCHASE_TYPE = "purchase_type"
    const val PURCHASE_TOKEN = "purchase_token"
    const val PRODUCT_ID = "product_id"
    const val PRICE = "price"
    const val CURRENCY = "currency"
    const val ADDITIONAL_PARAMETERS = "additional_parameters"
}

enum class PurchaseType(val value: String) {
    ONE_TIME_PURCHASE("one_time_purchase"),
    SUBSCRIPTION("subscription");
    
    companion object {
        fun fromString(value: String?): PurchaseType? {
            return when (value?.lowercase()) {
                "one_time_purchase", "onetime", "one_time" -> ONE_TIME_PURCHASE
                "subscription", "sub" -> SUBSCRIPTION
                else -> null
            }
        }
    }
}

object CustomerUserID {
    const val WAIT_FOR_CUSTOMER_USER_ID = "wait_for_customer_user_id"
}

object AppConfig {
    const val APP_ID = "app_id"
    const val MIN_TIME_BETWEEN_SESSIONS = "min_time_between_sessions"
}

object Privacy {
    const val DISABLE_ADVERTISING_IDENTIFIERS = "disable_advertising_identifiers"
    const val ENABLE_TCF_DATA_COLLECTION = "enable_tcf_data_collection"
    const val SHARING_FILTER_PARTNERS = "sharing_filter_partners"
}

object Analytics {
    const val UNINSTALL_TOKEN = "uninstall_token"
    const val IS_UPDATE = "is_update"
    const val ADDITIONAL_DATA = "additional_data"
}

