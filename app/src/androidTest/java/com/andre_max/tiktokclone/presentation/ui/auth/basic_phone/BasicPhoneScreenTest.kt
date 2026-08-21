/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.basic_phone

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

/** Screen 5's UI, driven against the stateless composable. */
@RunWith(AndroidJUnit4::class)
class BasicPhoneScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun render(
        state: BasicPhoneUiState,
        onAction: (BasicPhoneAction) -> Unit = {},
    ) = composeRule.setContent {
        BasicPhoneScreen(state = state, onAction = onAction)
    }

    @Test
    fun emptyStateShowsTheCountryCodePickerAndADisabledContinueButton() {
        render(BasicPhoneUiState())

        composeRule.onNodeWithTag(BasicPhoneScreenTags.COUNTRY_CODE).assertExists()
        composeRule.onNodeWithTag(BasicPhoneScreenTags.PHONE_FIELD).assertExists()
        composeRule.onNodeWithTag(BasicPhoneScreenTags.CONTINUE).assertIsNotEnabled()
    }

    @Test
    fun typingFiresPhoneNumberChanged() {
        val actions = mutableListOf<BasicPhoneAction>()
        render(state = BasicPhoneUiState(countryCode = "+254"), onAction = { actions += it })

        composeRule.onNodeWithTag(BasicPhoneScreenTags.PHONE_FIELD).performTextInput("7")

        assertThat(actions).containsExactly(BasicPhoneAction.PhoneNumberChanged("7"))
    }

    @Test
    fun anErrorStateRendersTheErrorAndKeepsContinueDisabled() {
        render(
            BasicPhoneUiState(
                countryCode = "+254",
                phoneNumber = "1",
                error = UiText.Dynamic("Enter a valid phone number"),
                isContinueEnabled = false,
            )
        )

        composeRule.onNodeWithTag(BasicPhoneScreenTags.ERROR).assertExists()
        composeRule.onNodeWithTag(BasicPhoneScreenTags.CONTINUE).assertIsNotEnabled()
    }

    @Test
    fun aValidNumberEnablesContinue() {
        render(
            BasicPhoneUiState(
                countryCode = "+254",
                phoneNumber = "712345678",
                isContinueEnabled = true,
            )
        )

        composeRule.onNodeWithTag(BasicPhoneScreenTags.CONTINUE).assertIsEnabled()
    }

    @Test
    fun theLoadingStateShowsAProgressIndicatorWhileTheCodeIsRequested() {
        render(
            BasicPhoneUiState(
                countryCode = "+254",
                phoneNumber = "712345678",
                isContinueEnabled = true,
                isLoading = true,
            )
        )

        composeRule.onNodeWithTag(BasicPhoneScreenTags.LOADING).assertExists()
    }

    @Test
    fun clickingContinueFiresContinueClicked() {
        val actions = mutableListOf<BasicPhoneAction>()
        render(
            state = BasicPhoneUiState(
                countryCode = "+254",
                phoneNumber = "712345678",
                isContinueEnabled = true,
            ),
            onAction = { actions += it },
        )

        composeRule.onNodeWithTag(BasicPhoneScreenTags.CONTINUE).performClick()

        assertThat(actions).containsExactly(BasicPhoneAction.ContinueClicked)
    }

    @Test
    fun clickingBackFiresBackClicked() {
        val actions = mutableListOf<BasicPhoneAction>()
        render(state = BasicPhoneUiState(), onAction = { actions += it })

        composeRule.onNodeWithTag(BasicPhoneScreenTags.BACK).performClick()

        assertThat(actions).containsExactly(BasicPhoneAction.BackClicked)
    }
}
