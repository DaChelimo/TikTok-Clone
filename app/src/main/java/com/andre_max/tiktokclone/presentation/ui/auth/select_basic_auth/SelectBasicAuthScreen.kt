/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.select_basic_auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.ui.auth.basic_phone.BasicPhoneRoute
import com.andre_max.tiktokclone.presentation.ui.auth.email_auth.enter_email.BasicEmailRoute
import kotlinx.coroutines.launch

/**
 * Stateful screen 6 — the `Phone | Email` tab bar.
 *
 * `ViewPager2` + `TabLayoutMediator` + `FragmentStateAdapter` collapse into `HorizontalPager` +
 * `TabRow`, which is what lets `BasicEmailFragment`/`BasicPhoneFragment` and their
 * `getInstance(isLogin)` + `Delegates.notNull` pattern go away: [isLogin] becomes an ordinary
 * composable parameter.
 *
 * There is no ViewModel of its own here — the pager's page index is the only state this screen
 * owns; phone and email each keep their own ViewModel via [BasicPhoneRoute]/[BasicEmailRoute].
 */
@Composable
fun SelectBasicAuthScreen(
    isLogin: Boolean,
    onBackClick: () -> Unit,
    onNavigateToPassword: (email: String) -> Unit = {},
    onNavigateToVerificationCode: (verificationId: String) -> Unit = {},
    onNavigateToUsername: () -> Unit = {},
) {
    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.back_button),
                modifier = Modifier.testTag(SelectBasicAuthScreenTags.BACK),
                tint = colorResource(R.color.dark_black),
            )
        }

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(SelectBasicAuthScreenTags.TAB_ROW),
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                modifier = Modifier.testTag(SelectBasicAuthScreenTags.PHONE_TAB),
                text = { Text(stringResource(R.string.phone_number)) },
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                modifier = Modifier.testTag(SelectBasicAuthScreenTags.EMAIL_TAB),
                text = { Text(stringResource(R.string.email_address)) },
            )
        }

        Box(modifier = Modifier.testTag(SelectBasicAuthScreenTags.PAGER)) {
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> BasicPhoneRoute(
                        onNavigateToVerificationCode = onNavigateToVerificationCode,
                        onNavigateToUsername = onNavigateToUsername,
                        onNavigateBack = onBackClick,
                    )

                    1 -> BasicEmailRoute(
                        isLogin = isLogin,
                        onNavigateToPassword = onNavigateToPassword,
                        onNavigateBack = onBackClick,
                    )
                }
            }
        }
    }
}

/** Test/preview tags for the nodes this screen must expose. */
object SelectBasicAuthScreenTags {
    const val TAB_ROW = "select_basic_auth_tabs"
    const val PHONE_TAB = "select_basic_auth_phone_tab"
    const val EMAIL_TAB = "select_basic_auth_email_tab"
    const val PAGER = "select_basic_auth_pager"
    const val BACK = "select_basic_auth_back"
}
