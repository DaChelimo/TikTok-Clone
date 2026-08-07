/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.username

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.andre_max.tiktokclone.presentation.core.UiText
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Screen 4's UI, driven against the stateless composable. */
@RunWith(AndroidJUnit4::class)
class CreateUsernameScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun render(
        state: CreateUsernameUiState,
        onAction: (CreateUsernameAction) -> Unit = {},
    ) = composeRule.setContent {
        CreateUsernameScreen(state = state, onAction = onAction)
    }

    @Test
    fun emptyStateShowsAnEmptyFieldAndADisabledContinueButton() {
        render(CreateUsernameUiState())

        composeRule.onNodeWithTag(CreateUsernameScreenTags.USERNAME_FIELD).assertExists()
        composeRule.onNodeWithTag(CreateUsernameScreenTags.CONTINUE).assertIsNotEnabled()
    }

    @Test
    fun typingFiresUsernameChanged() {
        val actions = mutableListOf<CreateUsernameAction>()
        render(state = CreateUsernameUiState(), onAction = { actions += it })

        composeRule.onNodeWithTag(CreateUsernameScreenTags.USERNAME_FIELD).performTextInput("a")

        assertThat(actions).containsExactly(CreateUsernameAction.UsernameChanged("a"))
    }

    @Test
    fun theCheckingStateShowsAnInFlightIndicatorAndNoVerdictYet() {
        render(
            CreateUsernameUiState(
                username = "andrew",
                isCheckingAvailability = true,
                isAvailable = null,
            )
        )

        composeRule.onNodeWithTag(CreateUsernameScreenTags.CHECKING).assertExists()
        composeRule.onNodeWithTag(CreateUsernameScreenTags.AVAILABLE).assertDoesNotExist()
    }

    @Test
    fun anAvailableUsernameShowsTheAvailableMarkAndEnablesContinue() {
        render(
            CreateUsernameUiState(
                username = "andrew",
                isAvailable = true,
                isContinueEnabled = true,
            )
        )

        composeRule.onNodeWithTag(CreateUsernameScreenTags.AVAILABLE).assertExists()
        composeRule.onNodeWithTag(CreateUsernameScreenTags.CONTINUE).assertIsEnabled()
    }

    @Test
    fun aTakenUsernameShowsTheErrorAndKeepsContinueDisabled() {
        render(
            CreateUsernameUiState(
                username = "andrew",
                isAvailable = false,
                error = UiText.Dynamic("That username is taken"),
                isContinueEnabled = false,
            )
        )

        composeRule.onNodeWithTag(CreateUsernameScreenTags.ERROR).assertExists()
        composeRule.onNodeWithTag(CreateUsernameScreenTags.CONTINUE).assertIsNotEnabled()
    }

    @Test
    fun clickingContinueFiresContinueClicked() {
        val actions = mutableListOf<CreateUsernameAction>()
        render(
            state = CreateUsernameUiState(
                username = "andrew",
                isAvailable = true,
                isContinueEnabled = true,
            ),
            onAction = { actions += it },
        )

        composeRule.onNodeWithTag(CreateUsernameScreenTags.CONTINUE).performClick()

        assertThat(actions).containsExactly(CreateUsernameAction.ContinueClicked)
    }

    @Test
    fun clickingBackFiresBackClicked() {
        val actions = mutableListOf<CreateUsernameAction>()
        render(state = CreateUsernameUiState(), onAction = { actions += it })

        composeRule.onNodeWithTag(CreateUsernameScreenTags.BACK).performClick()

        assertThat(actions).containsExactly(CreateUsernameAction.BackClicked)
    }
}
