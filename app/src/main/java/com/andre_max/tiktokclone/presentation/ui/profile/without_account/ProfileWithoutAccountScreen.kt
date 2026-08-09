/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.profile.without_account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.andre_max.tiktokclone.R

/** The signed-out Me tab: just the sign-up prompt, as in the legacy layout. */
@Composable
fun ProfileWithoutAccountScreen(onSignUpClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_perm_identity),
            contentDescription = null,
            modifier = Modifier.size(96.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSignUpClicked,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .testTag(ProfileWithoutAccountTags.SIGN_UP),
        ) {
            Text(
                text = stringResource(R.string.sign_up_for_an_account),
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

object ProfileWithoutAccountTags {
    const val SIGN_UP = "profile_without_account_sign_up"
}
