package com.example.smarthome

import android.app.Application
import com.example.smarthome.ui.notifications.NotificationHelper

class SmarthomeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        NotificationHelper.createChannel(this)
    }
}
