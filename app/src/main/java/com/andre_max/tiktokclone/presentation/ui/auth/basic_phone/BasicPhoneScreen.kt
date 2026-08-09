/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.basic_phone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.core.asString
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme

/** Stateless screen 5 — pure function of [state] plus a single [onAction] sink. */
@Composable
fun BasicPhoneScreen(
    state: BasicPhoneUiState,
    onAction: (BasicPhoneAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        IconButton(onClick = { onAction(BasicPhoneAction.BackClicked) }) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.back_button),
                modifier = Modifier.testTag(BasicPhoneScreenTags.BACK),
                tint = colorResource(R.color.dark_black),
            )
        }

        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            TextField(
                value = state.countryCode,
                onValueChange = { onAction(BasicPhoneAction.CountryCodeChanged(it)) },
                modifier = Modifier
                    .width(96.dp)
                    .testTag(BasicPhoneScreenTags.COUNTRY_CODE),
                placeholder = { Text("+254", style = MaterialTheme.typography.titleMedium) },
                textStyle = MaterialTheme.typography.titleMedium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )

            TextField(
                value = state.phoneNumber,
                onValueChange = { onAction(BasicPhoneAction.PhoneNumberChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(BasicPhoneScreenTags.PHONE_FIELD),
                placeholder = {
                    Text(
                        text = stringResource(R.string.phone_number),
                        style = MaterialTheme.typography.titleMedium,
                        color = colorResource(R.color.medium_grey),
                    )
                },
                textStyle = MaterialTheme.typography.titleMedium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done,
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = colorResource(R.color.light_grey),
        )

        if (state.error != null) {
            Text(
                text = state.error.asString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag(BasicPhoneScreenTags.ERROR),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.dark_red),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = { onAction(BasicPhoneAction.ContinueClicked) },
                enabled = state.isContinueEnabled && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag(BasicPhoneScreenTags.CONTINUE),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.pinkBtnBackground),
                    contentColor = Color.White,
                    disabledContainerColor = colorResource(R.color.pinkBtnBackground).copy(alpha = 0.4f),
                    disabledContentColor = Color.White,
                ),
            ) {
                Text(
                    text = stringResource(R.string.send_code),
                    modifier = Modifier.alpha(if (state.isLoading) 0f else 1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .testTag(BasicPhoneScreenTags.LOADING),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Empty — Continue disabled")
@Composable
private fun BasicPhoneScreenEmptyPreview() {
    TikTokTheme { BasicPhoneScreen(state = BasicPhoneUiState(), onAction = {}) }
}

@Preview(showBackground = true, name = "Valid number — Continue enabled")
@Composable
private fun BasicPhoneScreenValidPreview() {
    TikTokTheme {
        BasicPhoneScreen(
            state = BasicPhoneUiState(
                countryCode = "+254",
                phoneNumber = "712345678",
                isContinueEnabled = true,
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun BasicPhoneScreenLoadingPreview() {
    TikTokTheme {
        BasicPhoneScreen(
            state = BasicPhoneUiState(
                countryCode = "+254",
                phoneNumber = "712345678",
                isContinueEnabled = true,
                isLoading = true,
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Invalid number")
@Composable
private fun BasicPhoneScreenErrorPreview() {
    TikTokTheme {
        BasicPhoneScreen(
            state = BasicPhoneUiState(
                countryCode = "+254",
                phoneNumber = "1",
                error = UiText.Resource(R.string.enter_valid_phone_number),
            ),
            onAction = {},
        )
    }
}
