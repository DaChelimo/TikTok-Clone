package com.andre_max.tiktokclone

import android.app.Application
import com.facebook.FacebookActivity
import com.facebook.FacebookSdk
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import timber.log.Timber

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        FacebookSdk.sdkInitialize(applicationContext)

    }
}