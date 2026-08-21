/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.phone

import android.app.Activity

/**
 * Supplies the currently-resumed `Activity` to the data layer.
 *
 * Exists solely because Firebase's `PhoneAuthOptions.setActivity` demands one. Keeping it here
 * is what lets `PhoneAuthRepo` in `domain` stay free of Android types (non-negotiable #6).
 */
fun interface ActivityProvider {
    /** @return the current `Activity`, or null if none is resumed. */
    fun current(): Activity?
}
