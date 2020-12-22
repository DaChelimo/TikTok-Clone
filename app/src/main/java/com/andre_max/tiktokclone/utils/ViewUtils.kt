package com.andre_max.tiktokclone.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import com.andre_max.tiktokclone.MainActivity
import com.andre_max.tiktokclone.R
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

object ViewUtils {
    fun loadGlideImage(imageView: ImageView, profilePictureUrl: String?) {
        Glide.with(imageView)
            .load(profilePictureUrl)
            .into(imageView)
    }

    fun showLongToast(context: Context, message: String) =
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    fun showLongSnackBar(view: View, message: String) =
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setTextColor(getResColor(view.context, android.R.color.white))
            .setBackgroundTint(getResColor(view.context, R.color.pinkBtnBackground))
            .show()

    fun getResColor(context: Context, @ColorRes colorRes: Int) =
        ResourcesCompat.getColor(context.resources, colorRes, null)

    fun changeBottomNavView(activity: Activity?, colorConstant: Int?, useWhiteIcons: Boolean) {
        val navView = (activity as MainActivity).binding.navView
        if (colorConstant.isNull())
            navView.visibility = View.GONE
        else {
            val tintDrawableId =
                if (useWhiteIcons) R.drawable.light_bottom_nav_bar
                else R.drawable.dark_bottom_nav_bar

            val tintColor = ColorStateList.valueOf(tintDrawableId)

            navView.visibility = View.VISIBLE
            navView.setBackgroundColor(colorConstant!!)
            navView.itemIconTintList = tintColor
            navView.itemTextColor = tintColor
        }
    }
}