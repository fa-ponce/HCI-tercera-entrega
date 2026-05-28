package com.example.smarthome

import android.app.Application

class SmarthomeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
