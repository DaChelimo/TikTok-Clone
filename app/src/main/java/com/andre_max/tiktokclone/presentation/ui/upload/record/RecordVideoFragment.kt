/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.core.asString
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint

/** Compose host for the camera capture screen. */
@AndroidEntryPoint
class RecordVideoFragment : Fragment() {

    private val viewModel by viewModels<RecordVideoViewModel>()

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
                            is RecordVideoEvent.NavigateToPreview ->
                                findNavController().navigate(
                                    RecordVideoFragmentDirections
                                        .actionRecordVideoFragmentToPreviewVideoFragment(
                                            event.localVideo,
                                        ),
                                )

                            is RecordVideoEvent.ShowMessage -> pendingMessage = event.message
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

                RecordVideoScreen(
                    state = state,
                    startRecording = { cameraView ->
                        viewModel.startVideo()?.let { cameraView.takeVideo(it) }
                    },
                    onPause = { cameraView ->
                        cameraView.stopVideo()
                        viewModel.pauseVideo()
                    },
                    onFinish = { cameraView ->
                        cameraView.stopVideo()
                        viewModel.stopVideo()
                    },
                    onResume = viewModel::resumeVideo,
                    onVideoTaken = viewModel::onVideoTaken,
                    onDismissLittleSpace = viewModel::resetShowLittleLayout,
                    onClose = { findNavController().popBackStack() },
                    onUploadClicked = {
                        findNavController()
                            .navigate(R.id.action_recordVideoFragment_to_createVideoFragment)
                    },
                    snackbarHostState = snackbarHostState,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        BottomNavViewUtils.hideBottomNavBar(activity)
        ViewUtils.changeSystemBars(activity, SystemBarColors.DARK)
    }

    override fun onStop() {
        super.onStop()
        // Leaving mid-take publishes the pending MediaStore entry, as the legacy screen did.
        viewModel.stopVideo()
    }
}
