package com.sayam.qrcode

import android.app.Application
import com.google.android.material.color.DynamicColors


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // This is all you need.
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}