/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_password

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.andre_max.tiktokclone.domain.auth.usecase.PasswordError
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Screen 3's UI, driven against the stateless composable.
 *
 * The distinguishing assertion here is that **every** rule node is present in every state — the
 * screen renders a live checklist, not a single error line (D6).
 */
@RunWith(AndroidJUnit4::class)
class EnterPasswordScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val allRules = listOf(
        PasswordError.TooShort,
        PasswordError.TooLong,
        PasswordError.NoDigit,
        PasswordError.NoSpecialCharacter,
        PasswordError.NoUppercase,
        PasswordError.NoLowercase,
        PasswordError.ContainsIdentity,
    )

    private fun render(
        state: EnterPasswordUiState,
        onAction: (EnterPasswordAction) -> Unit = {},
    ) = composeRule.setContent {
        EnterPasswordScreen(state = state, onAction = onAction)
    }

    @Test
    fun theRuleChecklistIsFullyVisibleBeforeTheFirstKeystroke() {
        render(EnterPasswordUiState())

        allRules.forEach { rule ->
            composeRule.onNodeWithTag(EnterPasswordScreenTags.ruleTag(rule)).assertExists()
        }
    }

    @Test
    fun anUneditedFieldCannotContinue() {
        render(EnterPasswordUiState())

        composeRule.onNodeWithTag(EnterPasswordScreenTags.CONTINUE).assertIsNotEnabled()
    }

    @Test
    fun typingFiresPasswordChanged() {
        val actions = mutableListOf<EnterPasswordAction>()
        render(state = EnterPasswordUiState(), onAction = { actions += it })

        composeRule.onNodeWithTag(EnterPasswordScreenTags.PASSWORD_FIELD).performTextInput("a")

        assertThat(actions).containsExactly(EnterPasswordAction.PasswordChanged("a"))
    }

    @Test
    fun failingRulesAreStillRenderedAlongsideThePassingOnes() {
        render(
            EnterPasswordUiState(
                password = "pw",
                hasBeenEdited = true,
                failedRules = persistentListOf(PasswordError.TooShort, PasswordError.NoDigit),
            )
        )

        allRules.forEach { rule ->
            composeRule.onNodeWithTag(EnterPasswordScreenTags.ruleTag(rule)).assertExists()
        }
    }

    @Test
    fun aPasswordSatisfyingEveryRuleEnablesContinue() {
        render(
            EnterPasswordUiState(
                password = "Passw0rd!",
                hasBeenEdited = true,
                isContinueEnabled = true,
            )
        )

        composeRule.onNodeWithTag(EnterPasswordScreenTags.CONTINUE).assertIsEnabled()
    }

    @Test
    fun clickingTheVisibilityToggleFiresTogglePasswordVisibility() {
        val actions = mutableListOf<EnterPasswordAction>()
        render(
            state = EnterPasswordUiState(password = "Passw0rd!", hasBeenEdited = true),
            onAction = { actions += it },
        )

        composeRule.onNodeWithTag(EnterPasswordScreenTags.VISIBILITY_TOGGLE).performClick()

        assertThat(actions).containsExactly(EnterPasswordAction.TogglePasswordVisibility)
    }

    @Test
    fun theLoadingStateShowsAProgressIndicator() {
        render(
            EnterPasswordUiState(
                password = "Passw0rd!",
                hasBeenEdited = true,
                isContinueEnabled = true,
                isLoading = true,
            )
        )

        composeRule.onNodeWithTag(EnterPasswordScreenTags.LOADING).assertExists()
    }

    @Test
    fun clickingContinueFiresContinueClicked() {
        val actions = mutableListOf<EnterPasswordAction>()
        render(
            state = EnterPasswordUiState(
                password = "Passw0rd!",
                hasBeenEdited = true,
                isContinueEnabled = true,
            ),
            onAction = { actions += it },
        )

        composeRule.onNodeWithTag(EnterPasswordScreenTags.CONTINUE).performClick()

        assertThat(actions).containsExactly(EnterPasswordAction.ContinueClicked)
    }

    @Test
    fun clickingBackFiresBackClicked() {
        val actions = mutableListOf<EnterPasswordAction>()
        render(state = EnterPasswordUiState(), onAction = { actions += it })

        composeRule.onNodeWithTag(EnterPasswordScreenTags.BACK).performClick()

        assertThat(actions).containsExactly(EnterPasswordAction.BackClicked)
    }
}
