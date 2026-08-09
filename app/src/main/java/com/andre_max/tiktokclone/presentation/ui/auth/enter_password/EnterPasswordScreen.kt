/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_password

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.auth.usecase.PasswordError
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Stateless screen 3 (spec §4.3) — a pure function of [state] plus a single [onAction] sink, so it
 * is `@Preview`-able and testable with no ViewModel and no Hilt.
 *
 * Layout follows `docs/designs/onboarding/03-enter-password.png` and `03b`: the "Sign up" bar, a
 * "Create password" headline, an underlined password field with clear + reveal affordances, the
 * "Your password must have at least:" checklist, a strength bar, and the primary button.
 *
 * The legacy `fragment_create_password.xml` is consulted only for *behavior* — it showed a single
 * red status line ("Include at least 8 characters…") that swapped text as the user typed, which is
 * exactly the shape D6 replaces: every rule is on screen at all times, each ticked or not.
 *
 * The design's own three-line checklist is **not** the rule set. D6 governs which rules exist
 * (digit, special, upper, lower, identity, and a 20-char cap per D9); the design governs how they
 * are laid out. See `docs/designs/onboarding/README.md` §"Two places the design contradicts".
 */
@Composable
fun EnterPasswordScreen(
    state: EnterPasswordUiState,
    onAction: (EnterPasswordAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        EnterPasswordTopBar(onBackClick = { onAction(EnterPasswordAction.BackClicked) })

        Text(
            text = stringResource(R.string.create_password),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.dark_black),
        )

        PasswordField(
            password = state.password,
            isPasswordVisible = state.isPasswordVisible,
            onPasswordChange = { onAction(EnterPasswordAction.PasswordChanged(it)) },
            onToggleVisibility = { onAction(EnterPasswordAction.TogglePasswordVisibility) },
        )

        RuleChecklist(failedRules = state.failedRules, hasBeenEdited = state.hasBeenEdited)

        PasswordStrength(failedRules = state.failedRules, hasBeenEdited = state.hasBeenEdited)

        // The spinner is a sibling of the Button rather than its content: a Button merges its
        // descendants' semantics, so a tag placed inside it is not addressable from the merged
        // tree the screen test queries.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = { onAction(EnterPasswordAction.ContinueClicked) },
                enabled = state.isContinueEnabled && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag(EnterPasswordScreenTags.CONTINUE),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.pinkBtnBackground),
                    contentColor = Color.White,
                    disabledContainerColor = colorResource(R.color.pinkBtnBackground)
                        .copy(alpha = 0.4f),
                    disabledContentColor = Color.White,
                ),
            ) {
                Text(
                    text = stringResource(R.string.next),
                    // Hidden rather than swapped out, so the button does not resize while loading.
                    modifier = Modifier.alpha(if (state.isLoading) 0f else 1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .testTag(EnterPasswordScreenTags.LOADING),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
        // Log-in mode signs in from this screen, so a rejected credential surfaces here rather
        // than on the next one.
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun EnterPasswordTopBar(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .testTag(EnterPasswordScreenTags.BACK),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.back_button),
                tint = colorResource(R.color.dark_black),
            )
        }
        Text(
            text = stringResource(R.string.sign_up),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 12.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.dark_black),
        )
    }
}

/**
 * The design's underlined field: no filled box, no label, with a clear "x" and a reveal eye once
 * there is text. Deliberately duplicated from `BasicEmailScreen`'s `EmailField` rather than
 * extracted — the two differ in keyboard type, visual transformation and trailing content, and
 * `AGENTS.md` §5 puts the extraction trigger at the third occurrence, not the second.
 */
@Composable
private fun PasswordField(
    password: String,
    isPasswordVisible: Boolean,
    onPasswordChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(EnterPasswordScreenTags.PASSWORD_FIELD),
            placeholder = {
                Text(
                    text = stringResource(R.string.enter_password),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(R.color.medium_grey),
                )
            },
            trailingIcon = {
                Row {
                    if (password.isNotEmpty()) {
                        IconButton(onClick = { onPasswordChange("") }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_round_close),
                                contentDescription = stringResource(R.string.clear_password),
                                tint = colorResource(R.color.medium_grey),
                            )
                        }
                    }
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.testTag(EnterPasswordScreenTags.VISIBILITY_TOGGLE),
                    ) {
                        Icon(
                            painter = painterResource(
                                if (isPasswordVisible) {
                                    R.drawable.ic_visibility_off
                                } else {
                                    R.drawable.ic_visibility
                                }
                            ),
                            contentDescription = stringResource(
                                if (isPasswordVisible) R.string.hide_password
                                else R.string.show_password
                            ),
                            tint = colorResource(R.color.medium_grey),
                        )
                    }
                }
            },
            textStyle = MaterialTheme.typography.titleMedium,
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = colorResource(R.color.dark_black),
                unfocusedTextColor = colorResource(R.color.dark_black),
                cursorColor = colorResource(R.color.pinkBtnBackground),
            ),
        )
        HorizontalDivider(color = colorResource(R.color.light_grey))
    }
}

/**
 * Every rule the app enforces, always on screen, each ticked or not — the live checklist D6 exists
 * to make possible. A rule the user has not yet had a chance to satisfy is grey rather than red,
 * which is what [EnterPasswordUiState.hasBeenEdited] gates.
 */
@Composable
private fun RuleChecklist(
    failedRules: ImmutableList<PasswordError>,
    hasBeenEdited: Boolean,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.password_requirements_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.dark_black),
        )
        DisplayedRules.forEach { rule ->
            RuleRow(
                rule = rule.error,
                labelRes = rule.labelRes,
                isMet = rule.error !in failedRules,
                hasBeenEdited = hasBeenEdited,
            )
        }
    }
}

@Composable
private fun RuleRow(
    rule: PasswordError,
    @StringRes labelRes: Int,
    isMet: Boolean,
    hasBeenEdited: Boolean,
) {
    val colorRes = when {
        !hasBeenEdited -> R.color.medium_grey
        isMet -> R.color.check_green
        else -> R.color.dark_red
    }

    Row(
        modifier = Modifier.testTag(EnterPasswordScreenTags.ruleTag(rule)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            // The tick is redundant with the status appended to the label below, and TalkBack
            // reading "check" before every line is noise.
            modifier = Modifier
                .size(16.dp)
                .clearAndSetSemantics { },
            contentDescription = null,
            tint = colorResource(colorRes),
        )
        Text(
            // Colour alone must not carry the met/unmet distinction (WCAG 1.4.1), and a screen
            // reader cannot see the tint at all, so the status is in the text for everyone.
            text = if (hasBeenEdited) {
                stringResource(labelRes) + " — " + stringResource(
                    if (isMet) R.string.password_rule_met else R.string.password_rule_unmet
                )
            } else {
                stringResource(labelRes)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = colorResource(colorRes),
        )
    }
}

/**
 * The design's strength bar. Derived from [failedRules] rather than stored: it is a restatement of
 * the checklist, so putting it in `UiState` would be a second source of truth for the same fact.
 */
@Composable
private fun PasswordStrength(
    failedRules: ImmutableList<PasswordError>,
    hasBeenEdited: Boolean,
) {
    val satisfied = DisplayedRules.size - failedRules.size
    val fraction = if (hasBeenEdited) satisfied.toFloat() / DisplayedRules.size else 0f
    val strengthRes = when {
        !hasBeenEdited -> null
        failedRules.isEmpty() -> R.string.password_strength_strong
        satisfied >= DisplayedRules.size - 2 -> R.string.password_strength_medium
        else -> R.string.password_strength_weak
    }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = colorResource(R.color.check_green),
            trackColor = colorResource(R.color.light_grey),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.password_strength_label),
                style = MaterialTheme.typography.bodyLarge,
                color = colorResource(R.color.dark_black),
            )
            if (strengthRes != null) {
                Text(
                    text = stringResource(strengthRes),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.dark_black),
                )
            }
        }
    }
}

/**
 * The checklist rows, in [PasswordError] declaration order — the same order
 * `ValidatePasswordUseCase` reports failures in, so the list never appears to reshuffle.
 */
private data class DisplayedRule(val error: PasswordError, @StringRes val labelRes: Int)

private val DisplayedRules = listOf(
    DisplayedRule(PasswordError.TooShort, R.string.password_rule_min_length),
    DisplayedRule(PasswordError.TooLong, R.string.password_rule_max_length),
    DisplayedRule(PasswordError.NoDigit, R.string.password_rule_digit),
    DisplayedRule(PasswordError.NoSpecialCharacter, R.string.password_rule_special_character),
    DisplayedRule(PasswordError.NoUppercase, R.string.password_rule_uppercase),
    DisplayedRule(PasswordError.NoLowercase, R.string.password_rule_lowercase),
    DisplayedRule(PasswordError.ContainsIdentity, R.string.password_rule_no_identity),
)

@Preview(showBackground = true, name = "Empty — Next disabled")
@Composable
private fun EnterPasswordScreenEmptyPreview() {
    TikTokTheme { EnterPasswordScreen(state = EnterPasswordUiState(), onAction = {}) }
}

@Preview(showBackground = true, name = "Partially valid — rules failing")
@Composable
private fun EnterPasswordScreenFailingPreview() {
    TikTokTheme {
        EnterPasswordScreen(
            state = EnterPasswordUiState(
                password = "password",
                hasBeenEdited = true,
                failedRules = persistentListOf(
                    PasswordError.NoDigit,
                    PasswordError.NoSpecialCharacter,
                    PasswordError.NoUppercase,
                ),
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Valid — Next enabled")
@Composable
private fun EnterPasswordScreenValidPreview() {
    TikTokTheme {
        EnterPasswordScreen(
            state = EnterPasswordUiState(
                password = "Passw0rd!",
                isPasswordVisible = true,
                hasBeenEdited = true,
                isContinueEnabled = true,
            ),
            onAction = {},
        )
    }
}
