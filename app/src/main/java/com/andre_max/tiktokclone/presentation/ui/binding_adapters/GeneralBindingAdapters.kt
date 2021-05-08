package com.andre_max.tiktokclone.presentation.ui.binding_adapters

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("changeVisibility")
fun View.changeVisibility(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}