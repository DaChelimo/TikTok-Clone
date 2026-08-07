/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.email_auth.enter_email

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.core.asString
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme

/**
 * Stateless screen 2 (spec §4.2) — a pure function of [state] plus a single [onAction] sink, so it
 * is `@Preview`-able and testable with no ViewModel and no Hilt.
 *
 * Layout follows `docs/designs/onboarding/02-basic-email.png`: an underlined email field with a
 * clear affordance, the privacy notice, the primary **Continue** button, and a row of
 * domain-suggestion chips pinned above the keyboard. The "Sign up" title and the back arrow are
 * deliberately *not* drawn here — this composable is hosted inside a `ViewPager2` page of
 * `fragment_select_basic_sign_up.xml`, which already renders both above the Phone/Email tabs;
 * drawing them again produced a doubled header and two identical "Back Button" nodes for TalkBack.
 * The host's own back arrow is the single back affordance until screen 6 folds the pager into
 * Compose (spec §4.6). The legacy `fragment_basic_email.xml` is consulted
 * only for *behavior* — it labelled the button "Next", put the suggestion buttons directly under
 * the button as three equal-width filled buttons, and showed the terms-of-use blurb rather than the
 * design's privacy notice.
 *
 * [isLogin] is carried through unused by the screen body: it selects the downstream destination,
 * which is the host's business, and it disappears entirely once screen 6 lands (spec §4.6).
 */
@Composable
fun BasicEmailScreen(
    state: BasicEmailUiState,
    onAction: (BasicEmailAction) -> Unit,
    isLogin: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            // The chips are pinned to the bottom, so without this the IME covers them exactly when
            // they are useful. BasicEmailFragment puts the window in ADJUST_RESIZE for this screen,
            // which is what makes WindowInsets.ime non-zero below API 30.
            .imePadding(),
    ) {
        EmailField(
            email = state.email,
            onEmailChange = { onAction(BasicEmailAction.EmailChanged(it)) },
        )

        EmailErrorText(error = state.emailError)

        Text(
            text = stringResource(R.string.email_privacy_notice),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(R.color.medium_grey),
        )

        // The spinner is a sibling of the Button rather than its content: a Button merges its
        // descendants' semantics, so a tag placed inside it is not addressable from the merged
        // tree the screen test queries.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = { onAction(BasicEmailAction.ContinueClicked) },
                enabled = state.isContinueEnabled && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag(BasicEmailScreenTags.CONTINUE),
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
                    text = stringResource(R.string.continue_action),
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
                        .testTag(BasicEmailScreenTags.LOADING),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        DomainSuggestions(
            email = state.email,
            onSuggestionClick = { onAction(BasicEmailAction.EmailChanged(it)) },
        )
    }
}

/** The design's underlined field: no filled box, no label, a clear "x" once there is text. */
@Composable
private fun EmailField(
    email: String,
    onEmailChange: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
        TextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(BasicEmailScreenTags.EMAIL_FIELD),
            placeholder = {
                Text(
                    text = stringResource(R.string.email_address),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(R.color.medium_grey),
                )
            },
            trailingIcon = {
                if (email.isNotEmpty()) {
                    IconButton(onClick = { onEmailChange("") }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_round_close),
                            contentDescription = stringResource(R.string.clear_email_address),
                            tint = colorResource(R.color.medium_grey),
                        )
                    }
                }
            },
            textStyle = MaterialTheme.typography.titleMedium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
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

@Composable
private fun EmailErrorText(error: UiText?) {
    if (error == null) return

    Text(
        text = error.asString(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag(BasicEmailScreenTags.ERROR),
        style = MaterialTheme.typography.bodyMedium,
        // Not MaterialTheme.colorScheme.error: this screen paints on a hardcoded white surface, so
        // under a dark system theme the theme's error colour (#FFB4AB) would land on white at
        // 1.7:1 and vanish. A static resource keeps it legible whatever the system theme is.
        color = colorResource(R.color.dark_red),
    )
}

/**
 * The design's outlined domain chips, which scroll horizontally past the screen edge.
 *
 * Behavior copied from the legacy `BasicEmailViewModel.appendEmailExtension`: the chip replaces
 * everything from the first `@` onwards, so tapping twice swaps the domain instead of appending a
 * second one. `@yahoo.com` is the design's third chip; the old XML offered `@hotmail.com`.
 */
@Composable
private fun DomainSuggestions(
    email: String,
    onSuggestionClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf(R.string.gmail_com, R.string.yahoo_com, R.string.outlook_com).forEach { domainRes ->
            val domain = stringResource(domainRes)
            OutlinedButton(
                onClick = { onSuggestionClick(email.substringBefore("@") + domain) },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = colorResource(R.color.dark_black),
                ),
            ) {
                Text(
                    text = domain,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Empty — Continue disabled")
@Composable
private fun BasicEmailScreenEmptyPreview() {
    TikTokTheme { BasicEmailScreen(state = BasicEmailUiState(), onAction = {}) }
}

@Preview(showBackground = true, name = "Valid email — Continue enabled")
@Composable
private fun BasicEmailScreenValidPreview() {
    TikTokTheme {
        BasicEmailScreen(
            state = BasicEmailUiState(
                email = "samlee.mobbin@gmail.com",
                isContinueEnabled = true,
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Invalid email")
@Composable
private fun BasicEmailScreenErrorPreview() {
    TikTokTheme {
        BasicEmailScreen(
            state = BasicEmailUiState(
                email = "samlee.mobbin",
                emailError = UiText.Resource(R.string.enter_valid_email),
            ),
            onAction = {},
        )
    }
}
