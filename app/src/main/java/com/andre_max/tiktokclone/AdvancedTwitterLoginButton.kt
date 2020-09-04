package com.andre_max.tiktokclone

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.twitter.sdk.android.core.identity.TwitterLoginButton

class AdvancedTwitterLoginButton(context: Context, attrs: AttributeSet): TwitterLoginButton(context, attrs) {

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
