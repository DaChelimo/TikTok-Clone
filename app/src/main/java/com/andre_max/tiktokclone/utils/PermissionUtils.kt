package com.andre_max.tiktokclone.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
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
        val context: Context,
        val onPermissionsGranted: () -> Unit,
        val onPermissionsDenied: () -> Unit
    ) : BaseMultiplePermissionsListener() {

        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
            Timber.d("it.areAllPermissionsGranted() is ${report?.areAllPermissionsGranted()} and permissions denied is ${report?.deniedPermissionResponses}")

            report?.let {
                if (it.areAllPermissionsGranted()) {
                    ResUtils.showToast(context, R.string.all_permissions_granted)
                    onPermissionsGranted()
                } else {
                    val alertDialog = AlertDialog.Builder(context)
                        .setTitle(context.getString(R.string.record_permissions_title))
                        .setMessage(context.getString(R.string.record_permissions_description))
                        .setPositiveButton(context.getString(R.string.accept)) { _, _ ->
                            requestPermissions(context, this, recordVideoPermissions)
                        }
                        .setNegativeButton(context.getString(R.string.cancel)) { _, _ ->
                            ResUtils.showToast(context, R.string.permissions_denied)
                            onPermissionsDenied()
                        }
                        .create()

                    alertDialog.show()
                }
            }
        }
    }
}