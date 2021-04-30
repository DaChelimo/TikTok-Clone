package com.andre_max.tiktokclone.presentation.ui.home

import android.graphics.Color
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.andre_max.tiktokclone.presentation.exoplayer.Player
import com.andre_max.tiktokclone.utils.BottomNavViewUtils


class HomeLifecycleCallback(private val activity: FragmentActivity, private val player: Player?): LifecycleObserver {

    @OnLifecycleEvent(value = Lifecycle.Event.ON_STOP)
    fun onStart() {
        BottomNavViewUtils.changeBottomNavViewColor(activity, Color.TRANSPARENT, true)
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        player?.pausePlayer()
    }

    @OnLifecycleEvent(value = Lifecycle.Event.ON_STOP)
    fun onStop() {
        player?.stopPlayer()
        BottomNavViewUtils.changeBottomNavViewColor(activity, Color.WHITE, false)
    }

}