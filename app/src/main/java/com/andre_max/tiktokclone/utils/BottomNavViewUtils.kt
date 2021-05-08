package com.andre_max.tiktokclone.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.MainActivity

object BottomNavViewUtils {

    fun showBottomNavBar(activity: Activity?) = changeVisibility(activity, shouldShow = true)
    fun hideBottomNavBar(activity: Activity?) = changeVisibility(activity, shouldShow = false)

    fun changeVisibility(activity: Activity?, shouldShow: Boolean) {
        val bottomNavigationView = (activity as? MainActivity)?.binding?.navView
        bottomNavigationView?.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }

    /**
     * Changes bottom nav color. When useWhiteIcons is true, the background should be transparent,
     * the icons white and pink(selected). Otherwise, the background should be white,
     * the icons dark and pink(selected)
     *
     * @param useWhiteBar changes navigation icon and background color
     */
    @SuppressLint("ResourceType")
    fun changeNavBarColor(activity: Activity?, useWhiteBar: Boolean) {
        val navView = (activity as MainActivity).binding.navView
        showBottomNavBar(activity)

        val backgroundRes =
            if (useWhiteBar) R.drawable.light_bottom_nav_bar
            else R.drawable.transparent_bottom_nav_bar

        val iconRes =
            if (useWhiteBar) R.drawable.dark_nav_icon_tint
            else R.drawable.white_nav_icon_tint

        val iconTint = ResourcesCompat.getColorStateList(activity.resources, iconRes, null)

        navView.setBackgroundResource(backgroundRes)
        navView.itemIconTintList = iconTint
        navView.itemTextColor = iconTint
    }
}