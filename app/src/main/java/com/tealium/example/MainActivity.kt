package com.tealium.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tealium.remotecommands.appsflyer.Currency
import com.tealium.remotecommands.appsflyer.Customer
import com.tealium.remotecommands.appsflyer.Host
import com.tealium.remotecommands.appsflyer.Location
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TealiumHelper.trackView("main_screen")
        TealiumHelper.trackEvent("home_screen", mapOf("af_dev_key" to "Y5WQPfwiANqCdVbZSEvpkX"))

        button_setHost.setOnClickListener {
            TealiumHelper.trackEvent("sethost", mapOf(Host.HOST to "abc123", Host.HOST_PREFIX to "test_prefix"))
        }

        button_setUserEmails.setOnClickListener {
            val emails = JSONArray()
            emails.put("test@tester.com")
            emails.put("othertest@tester.com")
            TealiumHelper.trackEvent("setuseremails", mapOf(Customer.EMAILS to emails))
        }

        button_setUserId.setOnClickListener {
            TealiumHelper.trackEvent("setcustomerid", mapOf(Customer.USER_ID to "userId123"))
        }

        button_setCurrencyCode.setOnClickListener {
            TealiumHelper.trackEvent("setcurrencycode", mapOf(Currency.CODE to "USD"))
        }

        button_logPurchase.setOnClickListener {
            TealiumHelper.trackEvent("purchase")
        }

        button_trackLevelAchieved.setOnClickListener {
            TealiumHelper.trackEvent("levelachieved", mapOf("af_level" to 3))
        }

        button_trackLocation.setOnClickListener {
            TealiumHelper.trackEvent("tracklocation", mapOf(Location.LATITUDE to 0.0, Location.LONGITUDE to 0.0))
        }

        button_checkStandardEvents.setOnClickListener {
            TealiumHelper.trackEvent("addpaymentinfo", mapOf("af_success" to true))
            TealiumHelper.trackEvent("addtocart", mapOf())
            TealiumHelper.trackEvent("addtowishlist", mapOf())
            TealiumHelper.trackEvent("completeregistration", mapOf())
            TealiumHelper.trackEvent("tutorialcompletion", mapOf())
            TealiumHelper.trackEvent("initiatecheckout", mapOf())
            TealiumHelper.trackEvent("purchase", mapOf())
            TealiumHelper.trackEvent("cancelpurchase", mapOf())
            TealiumHelper.trackEvent("subscribe", mapOf())
            TealiumHelper.trackEvent("starttrial", mapOf())
            TealiumHelper.trackEvent("rate", mapOf())
            TealiumHelper.trackEvent("spentcredits", mapOf())
            TealiumHelper.trackEvent("achievementunlocked", mapOf())
            TealiumHelper.trackEvent("contentview", mapOf())
            TealiumHelper.trackEvent("listview", mapOf())
            TealiumHelper.trackEvent("adclick", mapOf())
            TealiumHelper.trackEvent("adview", mapOf())
            TealiumHelper.trackEvent("share", mapOf())
            TealiumHelper.trackEvent("invite", mapOf())
            TealiumHelper.trackEvent("login", mapOf())
            TealiumHelper.trackEvent("reengage", mapOf())
            TealiumHelper.trackEvent("update", mapOf())
        }
    }
}
