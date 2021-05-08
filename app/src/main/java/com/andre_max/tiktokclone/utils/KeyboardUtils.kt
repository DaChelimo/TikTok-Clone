package com.andre_max.tiktokclone.utils

import android.content.Context
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import com.andre_max.tiktokclone.utils.KeyboardUtils.hide
import com.andre_max.tiktokclone.utils.KeyboardUtils.show

/**
 * Helper object that abstracts the keyboard functionality
 * @see show Forcefully shows the keyboard
 * @see hide Forcefully hides the keyboard
 */
object KeyboardUtils {
    fun show(view: View) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            view.windowInsetsController?.show(WindowInsets.Type.ime())
        } else {
            val imm =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, 0)
        }
    }

    fun hide(view: View) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            view.windowInsetsController?.hide(WindowInsets.Type.ime())
        } else {
            val imm =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.applicationWindowToken, 0)
        }
    }
}