package com.tealium.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tealium.demo.databinding.ActivityMainBinding 
import com.tealium.remotecommands.appsflyer.Host
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        TealiumHelper.trackView("main_screen")
        TealiumHelper.trackEvent("home_screen", mapOf("af_dev_key" to "YOUR_APPSFLYER_DEV_KEY"))


        binding.buttonSetHost.setOnClickListener {
            TealiumHelper.trackEvent(
                "set_host",
                mapOf(Host.HOST to "YOUR_APPSFLYER_DEV_KEY", Host.HOST_PREFIX to "uokf4p")
            )
        }

        binding.buttonSetUserEmails.setOnClickListener {
            val emails = JSONArray()
            emails.put("test@tester.com")
            emails.put("othertest@tester.com")
            TealiumHelper.trackEvent(
                "user_register",
                mapOf("user_emails" to emails, "customer_user_id" to "userId123")
            )
        }

        binding.buttonSetCurrencyCode.setOnClickListener {
            TealiumHelper.trackEvent("set_currency", mapOf("currency_code" to "USD"))
        }

        binding.buttonLogPurchase.setOnClickListener {
            TealiumHelper.trackEvent("order", mapOf("order_total" to 29.99, "currency" to "USD"))
        }

        binding.buttonTrackLevelAchieved.setOnClickListener {
            TealiumHelper.trackEvent("level_up", mapOf("current_level" to 3))
        }

        binding.buttonTrackLocation.setOnClickListener {
            TealiumHelper.trackEvent("track_location", mapOf("location_latitude" to 0.0, "location_longitude" to 0.0))
        }

        binding.buttonCheckStandardEvents.setOnClickListener {
            TealiumHelper.trackEvent("payment", mapOf("payment_info_available" to true))
            TealiumHelper.trackEvent("cart_add", mapOf("product_name" to "Test Product"))
            TealiumHelper.trackEvent("wishlist_add", mapOf("product_id" to "test_123"))
            TealiumHelper.trackEvent("checkout", mapOf("checkout_step" to 1))
            TealiumHelper.trackEvent("email_signup", mapOf("signup_method" to "email"))
            TealiumHelper.trackEvent("rate", mapOf("rating_value" to 5, "max_rating_value" to 5))
            TealiumHelper.trackEvent("unlock_achievement", mapOf("achievement_id" to "first_level"))
            TealiumHelper.trackEvent("product", mapOf("content_type" to "product"))
            TealiumHelper.trackEvent("category", mapOf("content_type" to "category"))
            TealiumHelper.trackEvent("show_offers", mapOf("content" to "special_offers"))
            TealiumHelper.trackEvent("share", mapOf("content_type" to "article"))
            TealiumHelper.trackEvent("invite", mapOf("content" to "friend_invite"))
            TealiumHelper.trackEvent("user_login", mapOf("signup_method" to "google"))
        }

        binding.buttonCustomEvent.setOnClickListener {
            TealiumHelper.trackEvent("custom_event", mapOf())
        }

        binding.buttonSetDmaConsent.setOnClickListener {
            // Example for GDPR user with consent
            TealiumHelper.trackEvent(
                "set_dma_consent", 
                mapOf(
                    "gdpr_applies" to true,
                    "consent_for_data_usage" to true,
                    "consent_for_ads_personalization" to true,
                    "consent_for_ad_storage" to false
                )
            )
            
            // For testing non-GDPR scenario, uncomment this:
            // TealiumHelper.trackEvent("set_dma_consent", mapOf("gdpr_applies" to false))
        }

        binding.buttonLogAdRevenue.setOnClickListener {
            TealiumHelper.trackEvent(
                "log_ad_revenue",
                mapOf(
                    "ad_monetization_network" to "test_network",
                    "ad_mediation_network" to "googleadmob",
                    "ad_revenue" to 10.50,
                    "currency" to "USD",
                    "ad_additional_parameters" to mapOf(
                        "ad_size" to "banner",
                        "ad_placement_id" to "home_screen"
                    )
                )
            )
        }

        binding.buttonDisableTracking.setOnClickListener {
            TealiumHelper.trackEvent(
                "anonymize_user",
                mapOf("anonymize_user" to true)
            )
        }

        binding.buttonDisableAppsetId.setOnClickListener {
            TealiumHelper.trackEvent(
                "disable_appset_id",
                mapOf("disable_appset_id" to true)
            )
        }

        binding.buttonStopTracking.setOnClickListener {
            TealiumHelper.trackEvent(
                "stop_tracking",
                mapOf("stop_tracking" to true)
            )
        }

        binding.buttonValidatePurchase.setOnClickListener {
            TealiumHelper.trackEvent(
                "validate_and_log_purchase",
                mapOf(
                    "purchase_validation_type" to "one_time_purchase",
                    "purchase_validation_token" to "test_token_123",
                    "purchase_validation_product_id" to "test_product_id",
                    "purchase_validation_price" to "9.99",
                    "purchase_validation_currency" to "USD",
                    "purchase_validation_additional_parameters" to mapOf(
                        "product_name" to "Premium Package",
                        "category" to "premium"
                    )
                )
            )
        }

        binding.buttonSetPhoneNumber.setOnClickListener {
            TealiumHelper.trackEvent(
                "set_phone_number",
                mapOf("user_phone_number" to "+1234567890")
            )
        }

        binding.buttonSetOutOfStore.setOnClickListener {
            TealiumHelper.trackEvent(
                "set_out_of_store",
                mapOf("out_of_store_source" to "samsung_galaxy_store")
            )
        }

        binding.buttonPushDeepLink.setOnClickListener {
            val deepLinkPaths = JSONArray()
            deepLinkPaths.put("notification")
            deepLinkPaths.put("campaigns")
            TealiumHelper.trackEvent(
                "add_push_notification_deep_link_path",
                mapOf("push_deep_link_path" to deepLinkPaths)
            )
        }

        binding.buttonSendPushData.setOnClickListener {
            TealiumHelper.trackEvent("send_push_notification_data", mapOf())
        }

        binding.buttonLogSession.setOnClickListener {
            TealiumHelper.trackEvent("log_session", mapOf())
        }

        binding.buttonWaitForUserId.setOnClickListener {
            TealiumHelper.trackEvent(
                "wait_for_customer_user_id",
                mapOf("wait_for_customer_user_id" to true)
            )
        }

        binding.buttonSetCustomerIdAndLogSession.setOnClickListener {
            TealiumHelper.trackEvent(
                "set_customer_id_and_log_session",
                mapOf("customer_user_id" to "test_user_456")
            )
        }

        binding.buttonDisableAdIdentifiers.setOnClickListener {
            TealiumHelper.trackEvent(
                "set_disable_advertising_identifiers",
                mapOf("disable_advertising_identifiers" to true)
            )
        }

        binding.buttonEnableTcf.setOnClickListener {
            TealiumHelper.trackEvent(
                "enable_tcf_data_collection",
                mapOf("enable_tcf_data_collection" to true)
            )
        }

        binding.buttonResolveDeepLinks.setOnClickListener {
            val urls = JSONArray()
            urls.put("https://example.com/product/123")
            urls.put("https://example.com/campaign/456")
            TealiumHelper.trackEvent(
                "resolve_deeplink_urls",
                mapOf("deep_link" to urls)
            )
        }

        binding.buttonSetAdditionalData.setOnClickListener {
            TealiumHelper.trackEvent(
                "set_additional_data",
                mapOf(
                    "additional_data" to mapOf(
                        "custom_dimension_1" to "value1",
                        "custom_dimension_2" to "value2",
                        "app_version" to "1.0.0",
                        "feature_flag" to true
                    )
                )
            )
        }

        binding.buttonSetSharingFilter.setOnClickListener {
            val partners = JSONArray()
            partners.put("facebook_int")
            partners.put("googleadwords_int")
            partners.put("snapchat_int")
            TealiumHelper.trackEvent(
                "set_sharing_filter_for_partners",
                mapOf("sharing_filter_partners" to partners)
            )
        }

        binding.buttonSetMinTimeBetweenSessions.setOnClickListener {
            TealiumHelper.trackEvent(
                "set_min_time_between_sessions",
                mapOf("min_time_between_sessions" to 10)
            )
        }

        binding.buttonSetAppId.setOnClickListener {
            TealiumHelper.trackEvent(
                "set_app_id",
                mapOf("app_id" to "com.tealium.demo.appsflyer")
            )
        }

        binding.buttonUpdateUninstallToken.setOnClickListener {
            TealiumHelper.trackEvent(
                "update_server_uninstall_token",
                mapOf("uninstall_token" to "test_fcm_token_12345")
            )
        }

        binding.buttonSetIsUpdate.setOnClickListener {
            TealiumHelper.trackEvent(
                "set_is_update",
                mapOf("is_update" to true)
            )
        }
    }
}
