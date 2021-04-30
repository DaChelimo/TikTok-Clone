package com.andre_max.tiktokclone.presentation.ui.custom_views

import android.text.Editable
import android.text.TextWatcher

interface CustomTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {}
}