package com.tealium.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TealiumHelper.trackView("home_screen")

        setHostButton.setOnClickListener {
            TealiumHelper.trackEvent("sethost")
        }

        setUserEmailsButton.setOnClickListener {
            TealiumHelper.trackEvent("setuseremails")
        }

        setUserIdButton.setOnClickListener {
            TealiumHelper.trackEvent("setcustomerid")
        }

        setCurrencyCodeButton.setOnClickListener {
            TealiumHelper.trackEvent("setcurrencycode")
        }

        logPurchaseButton.setOnClickListener {
            TealiumHelper.trackEvent("purchase")
        }

        trackLevelAchievedButton.setOnClickListener {
            TealiumHelper.trackEvent("levelachieved")
        }

        trackLocationButton.setOnClickListener {
            TealiumHelper.trackEvent("tracklocation")
        }

    }
}
