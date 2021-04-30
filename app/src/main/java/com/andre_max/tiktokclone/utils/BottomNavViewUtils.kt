package com.andre_max.tiktokclone.utils

import android.app.Activity
import android.content.res.ColorStateList
import android.view.View
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.MainActivity

object BottomNavViewUtils {

    fun showBottomNavBar(activity: Activity?) = changeVisibility(activity, shouldShow = true)
    fun hideBottomNavBar(activity: Activity?) = changeVisibility(activity, shouldShow = false)

    fun changeVisibility(activity: Activity?, shouldShow: Boolean) {
        val bottomNavigationView = (activity as? MainActivity)?.navView
        bottomNavigationView?.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }

    fun changeBottomNavViewColor(activity: Activity?, colorConstant: Int?, useWhiteIcons: Boolean) {
        val navView = (activity as MainActivity).binding.navView
        if (colorConstant == null)
            navView.visibility = View.GONE
        else {
            val tintDrawableId =
                if (useWhiteIcons) R.drawable.light_bottom_nav_bar
                else R.drawable.dark_bottom_nav_bar

            val tintColor = ColorStateList.valueOf(tintDrawableId)

            navView.visibility = View.VISIBLE
            navView.setBackgroundColor(colorConstant)
            navView.itemIconTintList = tintColor
            navView.itemTextColor = tintColor
        }
    }
}