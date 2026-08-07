/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.age_verification

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.andre_max.tiktokclone.presentation.core.UiText
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Screen 1's UI, driven against the **stateless** composable — no ViewModel, no Hilt.
 *
 * These live in `androidTest` rather than `test` because `ui-test-junit4` is an
 * `androidTestImplementation` dependency (spec §3c); `./gradlew testDebugUnitTest` therefore
 * only compiles the JVM suites, and these run with `connectedAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class AgeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun render(
        state: AgeUiState,
        onAction: (AgeAction) -> Unit = {},
    ) = composeRule.setContent { AgeScreen(state = state, onAction = onAction) }

    @Test
    fun emptyStateShowsADatePickerAndADisabledContinueButton() {
        render(AgeUiState())

        composeRule.onNodeWithTag(AgeScreenTags.DATE_PICKER).assertExists()
        composeRule.onNodeWithTag(AgeScreenTags.CONTINUE).assertIsNotEnabled()
    }

    @Test
    fun aValidDateEnablesContinueAndShowsNoError() {
        render(
            AgeUiState(
                dateOfBirth = LocalDate.of(2000, 1, 1),
                isContinueEnabled = true,
            )
        )

        composeRule.onNodeWithTag(AgeScreenTags.CONTINUE).assertIsEnabled()
        composeRule.onNodeWithTag(AgeScreenTags.ERROR).assertDoesNotExist()
    }

    @Test
    fun anErrorStateRendersTheErrorAndKeepsContinueDisabled() {
        render(
            AgeUiState(
                dateOfBirth = LocalDate.of(2020, 1, 1),
                error = UiText.Dynamic("You must be at least 13"),
                isContinueEnabled = false,
            )
        )

        composeRule.onNodeWithTag(AgeScreenTags.ERROR).assertExists()
        composeRule.onNodeWithTag(AgeScreenTags.CONTINUE).assertIsNotEnabled()
    }

    @Test
    fun clickingContinueFiresContinueClicked() {
        val actions = mutableListOf<AgeAction>()
        render(
            state = AgeUiState(
                dateOfBirth = LocalDate.of(2000, 1, 1),
                isContinueEnabled = true,
            ),
            onAction = { actions += it },
        )

        composeRule.onNodeWithTag(AgeScreenTags.CONTINUE).performClick()

        assertThat(actions).containsExactly(AgeAction.ContinueClicked)
    }

    @Test
    fun clickingBackFiresBackClicked() {
        val actions = mutableListOf<AgeAction>()
        render(state = AgeUiState(), onAction = { actions += it })

        composeRule.onNodeWithTag(AgeScreenTags.BACK).performClick()

        assertThat(actions).containsExactly(AgeAction.BackClicked)
    }
}
