package com.andre_max.tiktokclone

import android.app.Application
import com.facebook.FacebookActivity
import com.facebook.FacebookSdk

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
//        Timber
        FacebookSdk.sdkInitialize(applicationContext)
    }
}