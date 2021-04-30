package com.andre_max.tiktokclone.presentation

import android.app.Application
import com.andre_max.tiktokclone.BuildConfig
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import timber.log.Timber

// TODO: Add forgot password in email authentification
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
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