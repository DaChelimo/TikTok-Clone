package com.andre_max.tiktokclone.utils

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide

object ImageUtils {

    fun loadGlideImage(imageView: ImageView, profilePictureUrl: String?) {
        Glide.with(imageView)
            .load(profilePictureUrl)
            .into(imageView)
    }

    fun loadGlideImage(imageView: ImageView, @DrawableRes drawableRes: Int) {
        Glide.with(imageView)
            .load(drawableRes)
            .into(imageView)
    }

    fun loadGlideImage(imageView: ImageView, bitmap: Bitmap) {
        Glide.with(imageView)
            .load(bitmap)
            .into(imageView)
    }

}