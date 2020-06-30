package com.tealium.demo

import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        TealiumHelper.initialize(this)
    }
}