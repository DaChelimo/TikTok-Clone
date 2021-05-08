package com.andre_max.tiktokclone.presentation

import android.app.Application
import com.andre_max.tiktokclone.BuildConfig
import com.facebook.FacebookSdk
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import timber.log.Timber

/*
 Notes:

 */
// TODO: Fix jank in BasicPhoneFragment
// TODO: Add forgot password in email authentification
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        FacebookSdk.setApplicationId(BuildConfig.FACEBOOK_APP_ID)
        FacebookSdk.sdkInitialize(applicationContext)

        val mTwitterAuthConfig = TwitterAuthConfig(
            BuildConfig.TWITTER_CONSUMER_KEY,
            BuildConfig.TWITTER_CONSUMER_SECRET
        )
        val twitterConfig = TwitterConfig.Builder(applicationContext)
            .twitterAuthConfig(mTwitterAuthConfig)
            .build()

        Twitter.initialize(twitterConfig)
    }
}