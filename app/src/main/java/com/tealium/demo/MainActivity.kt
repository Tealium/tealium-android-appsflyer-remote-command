package com.tealium.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tealium.demo.databinding.ActivityMainBinding
import com.tealium.remotecommands.appsflyer.Host
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
    }
}
