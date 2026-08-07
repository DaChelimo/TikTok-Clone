/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.select_basic_auth

import androidx.compose.runtime.Composable

/**
 * Stateless screen 6 — the `Phone | Email` tab bar.
 *
 * `ViewPager2` + `TabLayoutMediator` + `FragmentStateAdapter` collapse into `HorizontalPager` +
 * `TabRow`, which is what lets `BasicEmailFragment`/`BasicPhoneFragment` and their
 * `getInstance(isLogin)` + `Delegates.notNull` pattern go away: [isLogin] becomes an ordinary
 * composable parameter.
 *
 * There is no ViewModel — this screen holds no state beyond the pager's own.
 */
@Composable
fun SelectBasicAuthScreen(
    isLogin: Boolean,
    onBackClick: () -> Unit,
) {
    TODO("Pass 2")
}

/** Test/preview tags for the nodes this screen must expose. */
object SelectBasicAuthScreenTags {
    const val TAB_ROW = "select_basic_auth_tabs"
    const val PHONE_TAB = "select_basic_auth_phone_tab"
    const val EMAIL_TAB = "select_basic_auth_email_tab"
    const val PAGER = "select_basic_auth_pager"
    const val BACK = "select_basic_auth_back"
}
