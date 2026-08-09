/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.phone

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * Tracks the resumed `Activity` by registering against the `Application`'s lifecycle callbacks.
 *
 * The reference is weak and cleared on pause, so a destroyed Activity cannot be leaked by this
 * provider or, worse, handed to Firebase after its window is gone. `current()` returning null is
 * a normal state (the app is backgrounded), not an error — callers must handle it.
 */
class ResumedActivityProvider(application: Application) : ActivityProvider {

    private var resumed: WeakReference<Activity>? = null

    init {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    resumed = WeakReference(activity)
                }

                override fun onActivityPaused(activity: Activity) {
                    if (resumed?.get() === activity) resumed = null
                }

                override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
                override fun onActivityStarted(activity: Activity) = Unit
                override fun onActivityStopped(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, out: Bundle) = Unit
                override fun onActivityDestroyed(activity: Activity) = Unit
            },
        )
    }

    override fun current(): Activity? = resumed?.get()?.takeUnless { it.isFinishing }
}
