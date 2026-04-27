package com.tealium.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tealium.demo.databinding.ActivityMainBinding
import com.tealium.remotecommands.appsflyer.Host
import com.tealium.remotecommands.appsflyer.StringCommandParams
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        TealiumHelper.trackView("main_screen")
        TealiumHelper.trackEvent("home_screen", mapOf("af_dev_key" to "<your_key_here>"))


        binding.buttonSetHost.setOnClickListener {
            TealiumHelper.trackEvent(
                "set_host",
                mapOf(Host.HOST to "abc123", Host.HOST_PREFIX to "test_prefix")
            )
        }

        binding.buttonSetUserEmails.setOnClickListener {
            val emails = JSONArray()
            emails.put("test@tester.com")
            emails.put("othertest@tester.com")
            TealiumHelper.trackEvent(
                "user_register",
                mapOf("customer_email" to emails, "customer_id" to "userId123")
            )
        }

        binding.buttonSetCurrencyCode.setOnClickListener {
            TealiumHelper.trackEvent("set_currency", mapOf("currency_type" to "USD"))
        }

        binding.buttonLogPurchase.setOnClickListener {
            TealiumHelper.trackEvent("order")
        }

        binding.buttonTrackLevelAchieved.setOnClickListener {
            TealiumHelper.trackEvent("level_up", mapOf("current_level" to 3))
        }

        binding.buttonTrackLocation.setOnClickListener {
            TealiumHelper.trackEvent("track_location", mapOf("latitude" to 0.0, "longitude" to 0.0))
        }

        binding.buttonCheckStandardEvents.setOnClickListener {
            TealiumHelper.trackEvent("payment", mapOf("payment" to true))
            TealiumHelper.trackEvent("cart_add", mapOf())
            TealiumHelper.trackEvent("wishlist_add", mapOf())
            TealiumHelper.trackEvent("checkout", mapOf())
            TealiumHelper.trackEvent("email_signup", mapOf())
            TealiumHelper.trackEvent("rate", mapOf())
            TealiumHelper.trackEvent("unlock_achievement", mapOf())
            TealiumHelper.trackEvent("product", mapOf())
            TealiumHelper.trackEvent("listview", mapOf())
            TealiumHelper.trackEvent("show_offers", mapOf())
            TealiumHelper.trackEvent("share", mapOf())
            TealiumHelper.trackEvent("invite", mapOf())
            TealiumHelper.trackEvent("user_login", mapOf())
        }

        binding.buttonCustomEvent.setOnClickListener {
            TealiumHelper.trackEvent("custom_event", mapOf())
        }

        binding.buttonSetPhoneNumber.setOnClickListener {
            TealiumHelper.trackEvent("set_phone_number", mapOf("phone_number" to "+1234567890"))
        }

        binding.buttonLogAdRevenue.setOnClickListener {
            TealiumHelper.trackEvent("ad_revenue", mapOf(
                "monetization_network" to "TestNetwork",
                "mediation_network" to "googleadmob", 
                "ad_revenue_currency" to "USD",
                "ad_revenue_amount" to 1.99,
                "ad_revenue_ad_unit_id" to "test_ad_unit",
                "ad_revenue_ad_format" to "banner"
            ))
        }

        binding.buttonSetConsentData.setOnClickListener {
            TealiumHelper.trackEvent("set_consent", mapOf(
                "is_user_subject_to_gdpr" to true,
                "has_consent_for_data_usage" to true,
                "has_consent_for_ads_personalization" to false,
                "has_consent_for_ad_storage" to true
            ))
        }

        binding.buttonSetPartnerData.setOnClickListener {
            TealiumHelper.trackEvent("set_partner_data", mapOf(
                "partner_id" to "test_partner_123",
                "partner_puid" to "user123",
                "partner_user_segment" to "premium",
                "partner_ltv" to 150.0
            ))
        }

        binding.buttonAnonymizeUser.setOnClickListener {
            TealiumHelper.trackEvent("anonymize_user", mapOf("anonymize_user" to true))
        }

        binding.buttonSetSharingFilter.setOnClickListener {
            val filterArray = JSONArray()
            filterArray.put("partner1")
            filterArray.put("partner2")
            TealiumHelper.trackEvent("set_sharing_filter", mapOf("sharing_filter" to filterArray))
        }

        binding.buttonLogSession.setOnClickListener {
            TealiumHelper.trackEvent("log_session", mapOf())
        }

        binding.buttonSetOaid.setOnClickListener {
            TealiumHelper.trackEvent("set_oaid", mapOf(StringCommandParams.OAID to "test-oaid-value"))
        }

        binding.buttonSetOutOfStore.setOnClickListener {
            TealiumHelper.trackEvent("set_out_of_store", mapOf(StringCommandParams.STORE_NAME to "samsung_galaxy_store"))
        }

        binding.buttonSetDisableNetworkData.setOnClickListener {
            TealiumHelper.trackEvent("set_disable_network_data", mapOf(StringCommandParams.DISABLE_NETWORK_DATA to "true"))
        }

        binding.buttonSetAppInviteOnelink.setOnClickListener {
            TealiumHelper.trackEvent("set_app_invite_onelink", mapOf(StringCommandParams.APP_INVITE_ONE_LINK_ID to "abc123"))
        }

        binding.buttonSetPreinstallAttribution.setOnClickListener {
            TealiumHelper.trackEvent("set_preinstall_attribution", mapOf(
                "preinstall_media_source" to "oem_partner",
                "preinstall_campaign" to "default_campaign",
                "preinstall_site_id" to "site_001"
            ))
        }

        binding.buttonSetIsUpdate.setOnClickListener {
            TealiumHelper.trackEvent("set_is_update", mapOf(StringCommandParams.IS_UPDATE to "true"))
        }

        binding.buttonSetLogLevel.setOnClickListener {
            TealiumHelper.trackEvent("set_log_level", mapOf(StringCommandParams.LOG_LEVEL to "VERBOSE"))
        }
    }
}
