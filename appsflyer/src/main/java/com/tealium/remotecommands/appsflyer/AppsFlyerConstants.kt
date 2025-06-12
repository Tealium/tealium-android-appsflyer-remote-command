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
