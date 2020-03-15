package com.tealium.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    lateinit var setHostButton: Button
    lateinit var setUserEmailsButton :Button
    lateinit var setCustomerIdButton :Button
    lateinit var setCurrecyCodeButton :Button
    lateinit var logPurchaseButton :Button
    lateinit var trackLevelAchievedButton :Button
    lateinit var trackLocationButton :Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TealiumHelper.trackView("home_screen")

        setHostButton = findViewById(R.id.set_host_button)
        setHostButton.setOnClickListener {
            TealiumHelper.trackEvent("sethost")
        }

        setUserEmailsButton = findViewById(R.id.set_user_emails_button)
        setUserEmailsButton.setOnClickListener {
            TealiumHelper.trackEvent("setuseremails")
        }

        setCustomerIdButton = findViewById(R.id.set_customer_id_button)
        setCustomerIdButton.setOnClickListener {
            TealiumHelper.trackEvent("setcustomerid")
        }


        setCurrecyCodeButton = findViewById(R.id.set_currency_code_button)
        setCurrecyCodeButton.setOnClickListener {
            TealiumHelper.trackEvent("setcurrencycode")
        }


        logPurchaseButton = findViewById(R.id.track_purchase_button)
        logPurchaseButton.setOnClickListener {
            TealiumHelper.trackEvent("purchase")
        }


        trackLevelAchievedButton = findViewById(R.id.track_level_achieved_button)
        trackLevelAchievedButton.setOnClickListener {
            TealiumHelper.trackEvent("levelachieved")
        }

        trackLocationButton = findViewById(R.id.track_location_button)
        trackLocationButton.setOnClickListener {
            TealiumHelper.trackEvent("tracklocation")
        }

    }
}
