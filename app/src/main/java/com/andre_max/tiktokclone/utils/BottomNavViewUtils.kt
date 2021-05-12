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
     * Changes bottom nav color. When useWhiteBar is true, the background should be white,
     * the icons dark and pink(selected). Otherwise, the background should be dark,
     * the icons white and pink(selected)
     *
     * @param systemBarColors whether the navView background color is white or not
     */
    @SuppressLint("ResourceType")
    fun changeNavBarColor(activity: Activity?, systemBarColors: SystemBarColors) {
        val navView = (activity as MainActivity).binding.navView

        val backgroundRes =
            if (systemBarColors == SystemBarColors.WHITE) android.R.color.white
            else R.color.dark_black

        val iconRes =
            if (systemBarColors == SystemBarColors.WHITE) R.drawable.dark_nav_icon_tint
            else R.drawable.white_nav_icon_tint

        val iconTint = ResourcesCompat.getColorStateList(activity.resources, iconRes, null)

        navView.setBackgroundColor(backgroundRes)
        navView.itemIconTintList = iconTint
        navView.itemTextColor = iconTint
    }
}