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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import androidx.core.content.ContextCompat
import com.andre_max.tiktokclone.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import timber.log.Timber

object PermissionUtils {

    val recordVideoPermissions = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    fun isPermissionGranted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun arePermissionsGranted(context: Context, permissionList: Collection<String>) =
        permissionList.all { permission -> isPermissionGranted(context, permission) }

    fun requestPermissions(
        context: Context,
        listener: MultiplePermissionsListener,
        permissionList: Collection<String>
    ) {
        Dexter.withContext(context)
            .withPermissions(permissionList)
            .withListener(listener)
            .withErrorListener {
                Timber.e(it.toString())
            }
            .check()
    }

    class DialogMultiplePermissionsListener(
        val view: View,
        val onPermissionsGranted: () -> Unit,
        val onPermissionsDenied: () -> Unit
    ) : BaseMultiplePermissionsListener() {
        val context: Context = view.context

        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
            Timber.d("it.areAllPermissionsGranted() is ${report?.areAllPermissionsGranted()} and permissions denied is ${report?.deniedPermissionResponses}")

            if (report?.areAllPermissionsGranted() == true) {
                ResUtils.showSnackBar(view, R.string.all_permissions_granted)
                onPermissionsGranted()
            } else {
                ResUtils.showSnackBar(view, R.string.permissions_denied)
                onPermissionsDenied()
            }
        }
    }

}