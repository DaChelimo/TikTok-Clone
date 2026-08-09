/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint

/** Compose host for settings — reached from the own-profile menu. */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme { SettingsRoute() }
        }
    }

    override fun onResume() {
        super.onResume()
        ViewUtils.changeSystemBars(activity, SystemBarColors.WHITE)
    }
}

@Composable
fun SettingsRoute(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.settings),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.show_liked_videos),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.show_liked_videos_description),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Switch(
                checked = state.showLikedVideos,
                onCheckedChange = { onAction(SettingsAction.ShowLikedVideosToggled) },
                enabled = !state.isLoading,
                modifier = Modifier.testTag(SettingsScreenTags.SHOW_LIKED_VIDEOS),
            )
        }
    }
}

object SettingsScreenTags {
    const val SHOW_LIKED_VIDEOS = "settings_show_liked_videos"
}
