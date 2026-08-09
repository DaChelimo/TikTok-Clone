/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_code

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.core.asString
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme

/** Stateless SMS-code screen — pure function of [state] plus a single [onAction] sink. */
@Composable
fun EnterCodeScreen(
    state: EnterCodeUiState,
    onAction: (EnterCodeAction) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        IconButton(onClick = { onAction(EnterCodeAction.BackClicked) }) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.back_button),
                modifier = Modifier.testTag(EnterCodeScreenTags.BACK),
                tint = colorResource(R.color.dark_black),
            )
        }

        Text(
            text = stringResource(R.string.enter_6_digit_code),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        CodeBoxes(
            code = state.code,
            focusRequester = focusRequester,
            onCodeChange = { onAction(EnterCodeAction.CodeChanged(it)) },
        )

        if (state.error != null) {
            Text(
                text = state.error.asString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag(EnterCodeScreenTags.ERROR),
                style = MaterialTheme.typography.bodyMedium,
                color = colorResource(R.color.dark_red),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onAction(EnterCodeAction.ContinueClicked) },
            enabled = state.isContinueEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(52.dp)
                .testTag(EnterCodeScreenTags.CONTINUE),
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
                modifier = Modifier.alpha(if (state.isSubmitting) 0f else 1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Six boxes driven by one invisible [BasicTextField].
 *
 * A single field rather than six: six real fields each need their own focus and backspace
 * handling, and they defeat SMS autofill, which targets one field holding the whole code. The
 * boxes here are pure rendering — the field is the only thing that holds state or takes input.
 */
@Composable
private fun CodeBoxes(
    code: String,
    focusRequester: FocusRequester,
    onCodeChange: (String) -> Unit,
) {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        BasicTextField(
            value = code,
            onValueChange = onCodeChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag(EnterCodeScreenTags.CODE_FIELD),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            // The field itself is never drawn; decorationBox replaces it with the boxes, so the
            // caret and the raw text stay invisible while the field keeps the focus and the IME.
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    repeat(EnterCodeUiState.CODE_LENGTH) { index ->
                        CodeBox(digit = code.getOrNull(index), index = index)
                    }
                }
            },
        )
    }
}

@Composable
private fun CodeBox(digit: Char?, index: Int) {
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 52.dp)
            .border(
                width = 1.dp,
                color = colorResource(
                    if (digit != null) R.color.dark_black else R.color.light_grey,
                ),
                shape = RoundedCornerShape(4.dp),
            )
            .testTag(EnterCodeScreenTags.BOX_PREFIX + index),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = digit?.toString().orEmpty(),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = colorResource(R.color.dark_black),
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun EnterCodeScreenEmptyPreview() {
    TikTokTheme { EnterCodeScreen(state = EnterCodeUiState(), onAction = {}) }
}

@Preview(showBackground = true, name = "Partly typed")
@Composable
private fun EnterCodeScreenPartialPreview() {
    TikTokTheme { EnterCodeScreen(state = EnterCodeUiState(code = "123"), onAction = {}) }
}

@Preview(showBackground = true, name = "Rejected")
@Composable
private fun EnterCodeScreenErrorPreview() {
    TikTokTheme {
        EnterCodeScreen(
            state = EnterCodeUiState(
                code = "123456",
                error = UiText.Resource(R.string.code_invalid),
            ),
            onAction = {},
        )
    }
}
