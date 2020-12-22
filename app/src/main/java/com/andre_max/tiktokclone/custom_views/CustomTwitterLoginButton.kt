package com.andre_max.tiktokclone.custom_views

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.res.ResourcesCompat
import com.andre_max.tiktokclone.R
import com.twitter.sdk.android.core.identity.TwitterLoginButton

class CustomTwitterLoginButton(context: Context, attrs: AttributeSet): TwitterLoginButton(context, attrs) {

    init {
        textSize = 14F
        text = context.getString(R.string.continue_with_twitter)
        setCompoundDrawablesWithIntrinsicBounds(
            ResourcesCompat.getDrawable(
                resources,
                R.drawable.tw__ic_logo_default
            , null), null, null, null
        )

    }

}
