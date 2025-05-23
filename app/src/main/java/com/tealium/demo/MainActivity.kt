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
                mapOf("customer_email" to emails, "customer_id" to "userId123")
            )
        }

        binding.buttonSetCurrencyCode.setOnClickListener {
            TealiumHelper.trackEvent("set_currency", mapOf("currency_type" to "USD"))
        }

        binding.buttonLogPurchase.setOnClickListener {
            TealiumHelper.trackEvent("purchase")
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
                    "monetization_network" to "test_network",
                    "mediation_network" to "googleadmob",
                    "ad_revenue" to 10.50,
                    "ad_currency" to "USD",
                    "ad_revenue_parameters" to mapOf(
                        "ad_size" to "banner",
                        "placement" to "home_screen"
                    )
                )
            )
        }

        binding.buttonDisableTracking.setOnClickListener {
            TealiumHelper.trackEvent(
                "disabledevicetracking",
                mapOf("disable_device_tracking" to true)
            )
        }

        binding.buttonEnableAppsetId.setOnClickListener {
            TealiumHelper.trackEvent(
                "enable_appset_id",
                mapOf("enable_appset_id" to true)
            )
        }

        binding.buttonStopTracking.setOnClickListener {
            TealiumHelper.trackEvent(
                "stoptracking",
                mapOf("stop_tracking" to true)
            )
        }
    }
}
