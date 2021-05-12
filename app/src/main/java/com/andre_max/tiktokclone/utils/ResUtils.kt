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

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.andre_max.tiktokclone.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

object ResUtils {

    fun getResColor(context: Context, @ColorRes colorRes: Int) =
        ResourcesCompat.getColor(context.resources, colorRes, null)

    fun showToast(context: Context, message: String, @ToastDuration duration: Int = Toast.LENGTH_SHORT) =
        Toast.makeText(context, message, duration).show()

    fun showToast(context: Context, @StringRes messageRes: Int, @ToastDuration duration: Int = Toast.LENGTH_SHORT) =
        showToast(context, context.getString(messageRes), duration)

    fun showSnackBar(view: View, @StringRes messageRes: Int, @SnackBarDuration duration: Int = Snackbar.LENGTH_SHORT) =
        showSnackBar(view, view.resources.getString(messageRes), duration)

    fun showSnackBar(view: View, message: String, @SnackBarDuration duration: Int = Snackbar.LENGTH_SHORT) =
        Snackbar.make(view, message, duration)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
            .setTextColor(getResColor(view.context, android.R.color.white))
            .setBackgroundTint(getResColor(view.context, R.color.pinkBtnBackground))
            .show()
}

@IntDef(Snackbar.LENGTH_SHORT, Snackbar.LENGTH_LONG, Snackbar.LENGTH_INDEFINITE)
annotation class SnackBarDuration

@IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
annotation class ToastDuration