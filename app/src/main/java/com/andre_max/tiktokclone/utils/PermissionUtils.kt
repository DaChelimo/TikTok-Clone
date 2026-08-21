/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Permission checks. Requesting happens through the Compose
 * `rememberLauncherForActivityResult` on the screen that needs it — the Dexter-based request
 * flow left with the last legacy screen.
 */
object PermissionUtils {

    val recordVideoPermissions = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO,
    )

    fun isPermissionGranted(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun arePermissionsGranted(context: Context, permissionList: Collection<String>) =
        permissionList.all { permission -> isPermissionGranted(context, permission) }
}
