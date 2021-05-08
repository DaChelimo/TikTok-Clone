package com.andre_max.tiktokclone.utils

import android.app.Activity
import android.os.Build
import android.os.Build.VERSION_CODES.*
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import com.andre_max.tiktokclone.utils.ResUtils.getResColor

object ViewUtils {

    @RequiresApi(M)
    private const val darkStatusIcons = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

    @RequiresApi(M)
    private const val whiteStatusIcons = 0

    private fun setUpStatusBarAndNavigationBar(activity: Activity) {
        with(activity) {
            if (Build.VERSION.SDK_INT >= M) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }
        }
    }

    fun changeStatusBarIcons(activity: Activity, isWhite: Boolean) {
        setUpStatusBarAndNavigationBar(activity)
        if (Build.VERSION.SDK_INT >= M) {
            activity.window?.decorView?.systemUiVisibility =
                if (isWhite) whiteStatusIcons else darkStatusIcons
        }
    }

    fun changeStatusBarColor(activity: Activity, @ColorRes colorRes: Int) {
        setUpStatusBarAndNavigationBar(activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window?.statusBarColor = getResColor(activity.applicationContext, colorRes)
        }
    }

    fun changeSystemNavigationBarColor(activity: Activity, @ColorRes colorRes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window?.navigationBarColor = getResColor(activity.applicationContext, colorRes)
        }
    }

    fun showStatusAndNavBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= R) {
            val windowInsetsController = activity.window.insetsController
            windowInsetsController?.show(WindowInsets.Type.statusBars())
            windowInsetsController?.show(WindowInsets.Type.systemBars())
        }
    }

    fun hideStatusAndNavBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= R) {
            val windowInsetsController = activity.window.insetsController
            windowInsetsController?.hide(WindowInsets.Type.statusBars())
            windowInsetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE // Prevents layout resize
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // Initially load things behind the navigation bar. Prevents resize
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Initially load things behind the navigation bars. Prevents resize
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Load things behind the navigation bars
                    or View.SYSTEM_UI_FLAG_FULLSCREEN) // Load things behind the navigation bars
        }
    }
//    // Shows the system bars by removing all the flags
//    // except for the ones that make the content appear under the system bars.
//    @Suppress("DEPRECATION")
//    fun showSystemUI(activity: Activity) {
//        changeSystemNavigationBarColor(activity, android.R.color.white)
//        changeStatusBarColor(activity, android.R.color.white)
//        changeStatusBarIcons(activity, false)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//        // New way of requesting the app to be laid out fullscreen
//        // (for example, when implementing edge-to-edge)
//            activity.window.setDecorFitsSystemWindows(false)
//        } else {
//            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
//        }
//    }
//    fun hideSystemUI(activity: Activity) {
//        changeSystemNavigationBarColor(activity, android.R.color.transparent)
//        changeStatusBarColor(activity, android.R.color.transparent)
//        changeStatusBarIcons(activity, true)
//        activity.window.decorView.systemUiVisibility = (
//                // Set the content to appear under the system bars so that the
//                // content doesn't resize when the system bars hide and show.
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        // Hide the nav bar and status bar
//                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
//    }
}