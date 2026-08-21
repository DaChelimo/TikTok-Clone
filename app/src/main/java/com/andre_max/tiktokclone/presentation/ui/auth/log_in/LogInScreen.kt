/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.log_in

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme

/** Stateless log-in landing — pure function of [state] plus a single [onAction] sink. */
@Composable
fun LogInScreen(
    state: LogInUiState,
    onAction: (LogInAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = { onAction(LogInAction.CloseClicked) },
                    modifier = Modifier.testTag(LogInScreenTags.CLOSE),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_close),
                        contentDescription = stringResource(R.string.close_page),
                        tint = colorResource(R.color.dark_black),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.log_in_for_tiktok),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = stringResource(R.string.log_in_description_text),
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.medium_grey),
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProviderButton(
                icon = R.drawable.ic_sign_up_dude,
                label = R.string.use_phone_or_email,
                testTag = LogInScreenTags.USE_PHONE_OR_EMAIL,
                enabled = !state.isSigningIn,
                onClick = { onAction(LogInAction.UsePhoneOrEmailClicked) },
            )

            ProviderButton(
                icon = R.drawable.googleg_standard_color_18,
                label = R.string.continue_with_google,
                testTag = LogInScreenTags.GOOGLE,
                enabled = state.isGoogleEnabled && !state.isSigningIn,
                unavailableLabel = R.string.provider_not_configured,
                isUnavailable = !state.isGoogleEnabled,
                onClick = { onAction(LogInAction.GoogleClicked) },
            )

            ProviderButton(
                icon = null,
                label = R.string.continue_with_facebook,
                testTag = LogInScreenTags.FACEBOOK,
                enabled = state.isFacebookEnabled && !state.isSigningIn,
                unavailableLabel = R.string.provider_not_configured,
                isUnavailable = !state.isFacebookEnabled,
                onClick = { /* No action: Facebook is not configured. */ },
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.log_in_tos),
                modifier = Modifier.padding(horizontal = 32.dp),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.medium_grey),
            )

            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.dont_have_an_account),
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(
                    onClick = { onAction(LogInAction.SignUpClicked) },
                    modifier = Modifier.testTag(LogInScreenTags.SIGN_UP),
                ) {
                    Text(
                        text = stringResource(R.string.sign_up),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.pinkBtnBackground),
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/**
 * One provider row.
 *
 * [isUnavailable] is distinct from `!enabled`: a button disabled because a sign-in is already
 * running needs no explanation, but one disabled because the provider was never configured does
 * — otherwise it reads as a bug rather than as a feature that has not shipped.
 */
@Composable
private fun ProviderButton(
    @DrawableRes icon: Int?,
    @StringRes label: Int,
    testTag: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isUnavailable: Boolean = false,
    @StringRes unavailableLabel: Int? = null,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 6.dp)) {
        TextButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, colorResource(R.color.light_grey), RoundedCornerShape(2.dp))
                .testTag(testTag),
            shape = RoundedCornerShape(2.dp),
        ) {
            // Facebook passes null: SDK 13 stopped shipping com_facebook_button_login_logo,
            // and its references were stripped from the XML layouts during the AGP 8 fix-up.
            if (icon != null) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).alpha(if (enabled) 1f else 0.4f),
                    tint = Color.Unspecified,
                )
            }
            Text(
                text = stringResource(label),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (enabled) 1f else 0.4f),
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.dark_black),
            )
        }

        if (isUnavailable && unavailableLabel != null) {
            Text(
                text = stringResource(unavailableLabel),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.medium_grey),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LogInScreenNoProvidersPreview() {
    TikTokTheme { LogInScreen(state = LogInUiState(), onAction = {}) }
}

@Preview(showBackground = true)
@Composable
private fun LogInScreenGoogleEnabledPreview() {
    TikTokTheme {
        LogInScreen(state = LogInUiState(isGoogleEnabled = true), onAction = {})
    }
}
