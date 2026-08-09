/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.post_video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.MainActivity
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.core.asString
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint

/** Compose host for the post step. A finished upload lands on the Home tab, as before. */
@AndroidEntryPoint
class PostVideoFragment : Fragment() {

    private val viewModel by viewModels<PostVideoViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }
                var pendingMessage by remember { mutableStateOf<UiText?>(null) }

                LaunchedEffect(viewModel) {
                    viewModel.events.collect { event ->
                        when (event) {
                            PostVideoEvent.NavigateHome -> navigateHome()
                            is PostVideoEvent.ShowMessage -> pendingMessage = event.message
                        }
                    }
                }

                val message = pendingMessage?.asString()
                LaunchedEffect(message) {
                    message?.let {
                        snackbarHostState.showSnackbar(it)
                        pendingMessage = null
                    }
                }

                PostVideoScreen(
                    state = state,
                    onAction = viewModel::onAction,
                    snackbarHostState = snackbarHostState,
                )
            }
        }
    }

    private fun navigateHome() {
        (activity as? MainActivity)?.binding?.navView?.selectedItemId = R.id.homeFragment
    }

    override fun onResume() {
        super.onResume()
        ViewUtils.changeSystemBars(activity, SystemBarColors.WHITE)
    }
}
