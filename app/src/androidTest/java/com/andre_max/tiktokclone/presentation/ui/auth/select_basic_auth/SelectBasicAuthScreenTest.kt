/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.select_basic_auth

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Screen 6's UI (spec §4.6) — the `Phone | Email` tab bar.
 *
 * This screen has no ViewModel and no `UiState`, so it has no JVM unit-test counterpart: its
 * entire behavior is the pager/tab wiring, which only exists on a device.
 */
@RunWith(AndroidJUnit4::class)
class SelectBasicAuthScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun render(
        isLogin: Boolean = false,
        onBackClick: () -> Unit = {},
    ) = composeRule.setContent {
        SelectBasicAuthScreen(isLogin = isLogin, onBackClick = onBackClick)
    }

    @Test
    fun bothTabsAndThePagerAreRendered() {
        render()

        composeRule.onNodeWithTag(SelectBasicAuthScreenTags.TAB_ROW).assertExists()
        composeRule.onNodeWithTag(SelectBasicAuthScreenTags.PHONE_TAB).assertExists()
        composeRule.onNodeWithTag(SelectBasicAuthScreenTags.EMAIL_TAB).assertExists()
        composeRule.onNodeWithTag(SelectBasicAuthScreenTags.PAGER).assertExists()
    }

    @Test
    fun thePhoneTabIsSelectedFirst() {
        // Preserves the legacy FragmentStateAdapter order: position 0 is phone, 1 is email.
        render()

        composeRule.onNodeWithTag(SelectBasicAuthScreenTags.PHONE_TAB).assertIsSelected()
    }

    @Test
    fun tappingTheEmailTabSelectsIt() {
        render()

        composeRule.onNodeWithTag(SelectBasicAuthScreenTags.EMAIL_TAB).performClick()

        composeRule.onNodeWithTag(SelectBasicAuthScreenTags.EMAIL_TAB).assertIsSelected()
    }

    @Test
    fun theScreenRendersInLoginModeToo() {
        // `isLogin` is an ordinary composable parameter now — no `Delegates.notNull`, no
        // `getInstance(isLogin)` factory to serve a FragmentStateAdapter.
        render(isLogin = true)

        composeRule.onNodeWithTag(SelectBasicAuthScreenTags.TAB_ROW).assertExists()
    }

    @Test
    fun clickingBackInvokesTheBackCallback() {
        var backClicks = 0
        render(onBackClick = { backClicks++ })

        composeRule.onNodeWithTag(SelectBasicAuthScreenTags.BACK).performClick()

        assertThat(backClicks).isEqualTo(1)
    }
}
