/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.email_auth.enter_email

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.core.UiText
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Screen 2's UI, driven against the stateless composable — no ViewModel, no Hilt. */
@RunWith(AndroidJUnit4::class)
class BasicEmailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun render(
        state: BasicEmailUiState,
        onAction: (BasicEmailAction) -> Unit = {},
    ) = composeRule.setContent {
        BasicEmailScreen(state = state, onAction = onAction, isLogin = false)
    }

    @Test
    fun emptyStateShowsAnEmptyFieldAndADisabledContinueButton() {
        render(BasicEmailUiState())

        composeRule.onNodeWithTag(BasicEmailScreenTags.EMAIL_FIELD).assertExists()
        composeRule.onNodeWithTag(BasicEmailScreenTags.CONTINUE).assertIsNotEnabled()
        composeRule.onNodeWithTag(BasicEmailScreenTags.ERROR).assertDoesNotExist()
    }

    @Test
    fun typingFiresEmailChanged() {
        val actions = mutableListOf<BasicEmailAction>()
        render(state = BasicEmailUiState(), onAction = { actions += it })

        composeRule.onNodeWithTag(BasicEmailScreenTags.EMAIL_FIELD).performTextInput("a")

        assertThat(actions).containsExactly(BasicEmailAction.EmailChanged("a"))
    }

    @Test
    fun anErrorStateRendersTheErrorAndKeepsContinueDisabled() {
        render(
            BasicEmailUiState(
                email = "nope",
                emailError = UiText.Dynamic("Enter a valid email"),
                isContinueEnabled = false,
            )
        )

        composeRule.onNodeWithTag(BasicEmailScreenTags.ERROR).assertExists()
        composeRule.onNodeWithTag(BasicEmailScreenTags.CONTINUE).assertIsNotEnabled()
    }

    @Test
    fun aValidEmailEnablesContinue() {
        render(
            BasicEmailUiState(email = "andrew@example.com", isContinueEnabled = true)
        )

        composeRule.onNodeWithTag(BasicEmailScreenTags.CONTINUE).assertIsEnabled()
    }

    @Test
    fun theLoadingStateShowsAProgressIndicator() {
        render(
            BasicEmailUiState(
                email = "andrew@example.com",
                isContinueEnabled = true,
                isLoading = true,
            )
        )

        composeRule.onNodeWithTag(BasicEmailScreenTags.LOADING).assertExists()
    }

    @Test
    fun clickingContinueFiresContinueClicked() {
        val actions = mutableListOf<BasicEmailAction>()
        render(
            state = BasicEmailUiState(email = "andrew@example.com", isContinueEnabled = true),
            onAction = { actions += it },
        )

        composeRule.onNodeWithTag(BasicEmailScreenTags.CONTINUE).performClick()

        assertThat(actions).containsExactly(BasicEmailAction.ContinueClicked)
    }

    /**
     * The back arrow is the *host's* (`fragment_select_basic_sign_up.xml`), not this screen's —
     * drawing one here duplicated the header and gave TalkBack two identical "Back Button" nodes.
     * So there is deliberately no back node to assert on until screen 6 (spec §4.6); what this
     * screen must guarantee instead is that it renders no second header.
     */
    @Test
    fun theScreenDrawsNoHeaderOfItsOwn() {
        render(BasicEmailUiState())

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule.onNodeWithText(context.getString(R.string.sign_up)).assertDoesNotExist()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.back_button))
            .assertDoesNotExist()
    }
}
