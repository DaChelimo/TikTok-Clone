/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.username

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.core.asString
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme

/** Stateless screen 4 — pure function of [state] plus a single [onAction] sink. */
@Composable
fun CreateUsernameScreen(
    state: CreateUsernameUiState,
    onAction: (CreateUsernameAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        IconButton(onClick = { onAction(CreateUsernameAction.BackClicked) }) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.back_button),
                modifier = Modifier.testTag(CreateUsernameScreenTags.BACK),
                tint = colorResource(R.color.dark_black),
            )
        }

        Text(
            text = stringResource(R.string.create_username),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        UsernameField(
            username = state.username,
            isCheckingAvailability = state.isCheckingAvailability,
            isAvailable = state.isAvailable,
            onUsernameChange = { onAction(CreateUsernameAction.UsernameChanged(it)) },
        )

        if (state.error != null) {
            Text(
                text = state.error.asString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag(CreateUsernameScreenTags.ERROR),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.dark_red),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onAction(CreateUsernameAction.ContinueClicked) },
            enabled = state.isContinueEnabled && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp)
                .testTag(CreateUsernameScreenTags.CONTINUE),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.pinkBtnBackground),
                contentColor = Color.White,
                disabledContainerColor = colorResource(R.color.pinkBtnBackground).copy(alpha = 0.4f),
                disabledContentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.continue_action),
                modifier = Modifier.alpha(if (state.isLoading) 0f else 1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun UsernameField(
    username: String,
    isCheckingAvailability: Boolean,
    isAvailable: Boolean?,
    onUsernameChange: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        TextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(CreateUsernameScreenTags.USERNAME_FIELD),
            placeholder = {
                Text(
                    text = stringResource(R.string.username),
                    style = MaterialTheme.typography.titleMedium,
                    color = colorResource(R.color.medium_grey),
                )
            },
            // The spinner/checkmark is a sibling `Box`, not a `trailingIcon`: `TextField` merges
            // its trailing-icon slot's semantics into the field's own node, which would make the
            // checking/available tags unaddressable from the merged tree the screen test queries.
            trailingIcon = {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                    when {
                        isCheckingAvailability -> CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .testTag(CreateUsernameScreenTags.CHECKING),
                            strokeWidth = 2.dp,
                        )

                        isAvailable == true -> Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            modifier = Modifier.testTag(CreateUsernameScreenTags.AVAILABLE),
                            tint = colorResource(R.color.dark_black),
                        )
                    }
                }
            },
            textStyle = MaterialTheme.typography.titleMedium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
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

@Preview(showBackground = true, name = "Empty — Continue disabled")
@Composable
private fun CreateUsernameScreenEmptyPreview() {
    TikTokTheme { CreateUsernameScreen(state = CreateUsernameUiState(), onAction = {}) }
}

@Preview(showBackground = true, name = "Checking availability")
@Composable
private fun CreateUsernameScreenCheckingPreview() {
    TikTokTheme {
        CreateUsernameScreen(
            state = CreateUsernameUiState(username = "andrew", isCheckingAvailability = true),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Available — Continue enabled")
@Composable
private fun CreateUsernameScreenAvailablePreview() {
    TikTokTheme {
        CreateUsernameScreen(
            state = CreateUsernameUiState(
                username = "andrew",
                isAvailable = true,
                isContinueEnabled = true,
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Taken — error shown")
@Composable
private fun CreateUsernameScreenTakenPreview() {
    TikTokTheme {
        CreateUsernameScreen(
            state = CreateUsernameUiState(
                username = "andrew",
                isAvailable = false,
                error = UiText.Resource(R.string.name_unavailable),
            ),
            onAction = {},
        )
    }
}
