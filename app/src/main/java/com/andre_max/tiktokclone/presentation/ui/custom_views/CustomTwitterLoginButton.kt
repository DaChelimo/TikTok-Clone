package com.andre_max.tiktokclone.presentation.ui.custom_views

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.andre_max.tiktokclone.R
import com.google.android.material.button.MaterialButton
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import com.twitter.sdk.android.core.internal.CommonUtils
import java.lang.ref.WeakReference

class CustomTwitterLoginButton(context: Context, attrs: AttributeSet): MaterialButton(context, attrs) {

    val activityRef: WeakReference<Activity?>? = null

    @Volatile
    var authClient: TwitterAuthClient? = null
    var callback: Callback<TwitterSession>? = null
    var theClickListener: OnClickListener? = null

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setupButton() {
        val res = resources
        super.setCompoundDrawablesWithIntrinsicBounds(
            ResourcesCompat.getDrawable(res, R.drawable.tw__ic_logo_default, null), null, null, null
        )
        super.setCompoundDrawablePadding(
            res.getDimensionPixelSize(R.dimen.tw__login_btn_drawable_padding)
        )
        super.setText(R.string.tw__login_btn_txt)
        super.setTextColor(ResourcesCompat.getColor(res, R.color.tw__solid_white, null))
        setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            res.getDimensionPixelSize(R.dimen.tw__login_btn_text_size).toFloat()
        )
        super.setTypeface(Typeface.DEFAULT_BOLD)
        super.setPadding(
            res.getDimensionPixelSize(R.dimen.tw__login_btn_left_padding), 0,
            res.getDimensionPixelSize(R.dimen.tw__login_btn_right_padding), 0
        )
        super.setBackgroundResource(R.drawable.tw__login_btn)
        super.setOnClickListener(LoginClickListener())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.setAllCaps(false)
        }
    }

    /**
     * Call this method when [android.app.Activity.onActivityResult]
     * is called to complete the authorization flow.
     *
     * @param requestCode the request code used for SSO
     * @param resultCode the result code returned by the SSO activity
     * @param data the result data returned by the SSO activity
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == getTwitterAuthClient()!!.requestCode) {
            getTwitterAuthClient()!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Gets the activity. Override this method if this button was created with a non-Activity
     * context.
     */
    fun getActivity(): Activity? {
        return when {
            context is Activity -> {
                context as Activity
            }
            isInEditMode -> {
                null
            }
            else -> {
                throw IllegalStateException(ERROR_MSG_NO_ACTIVITY)
            }
        }
    }

    /**
     * Couldn't find a way to invoke the click listener of the material button
     */
    fun setTheClickListener(onClick: (View) -> Unit) {
        theClickListener = OnClickListener { onClick(it) }
    }

    inner class LoginClickListener : OnClickListener {
        override fun onClick(view: View) {
            checkCallback(callback)
            checkActivity(activityRef?.get())
            getTwitterAuthClient()?.authorize(activityRef?.get(), callback)
            theClickListener?.onClick(view)
        }

        private fun checkCallback(callback: Callback<*>?) {
            if (callback == null) {
                CommonUtils.logOrThrowIllegalStateException(
                    TwitterCore.TAG,
                    "Callback must not be null, did you call setCallback?"
                )
            }
        }

        private fun checkActivity(activity: Activity?) {
            if (activity == null || activity.isFinishing) {
                CommonUtils.logOrThrowIllegalStateException(
                    TwitterCore.TAG,
                    ERROR_MSG_NO_ACTIVITY
                )
            }
        }
    }

    fun getTwitterAuthClient(): TwitterAuthClient? {
        if (authClient == null) {
            synchronized(TwitterLoginButton::class.java) {
                if (authClient == null) {
                    authClient = TwitterAuthClient()
                }
            }
        }
        return authClient
    }

    private fun checkTwitterCoreAndEnable() {
        //Default (Enabled) in edit mode
        if (isInEditMode) return
        try {
            TwitterCore.getInstance()
        } catch (ex: IllegalStateException) {
            //Disable if TwitterCore hasn't started
            Twitter.getLogger().e(TAG, ex.message)
            isEnabled = false
        }
    }

    companion object {
        val TAG = TwitterCore.TAG
        val ERROR_MSG_NO_ACTIVITY = ("TwitterLoginButton requires an activity."
                + " Override getActivity to provide the activity for this button.")

    }
}
