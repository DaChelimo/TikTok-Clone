/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andre_max.tiktokclone.presentation

import android.app.Application
import com.andre_max.tiktokclone.BuildConfig
import com.facebook.FacebookSdk
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
        // Init Timber for logging
        Timber.plant(Timber.DebugTree())

        // Enable Firebase Crashlytics only on release build
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        // Init Facebook for auth
        FacebookSdk.setApplicationId(BuildConfig.FACEBOOK_APP_ID)
        FacebookSdk.sdkInitialize(applicationContext)

        initTwitterAuth()
    }

    // Init Twitter for auth
    private fun initTwitterAuth() {
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