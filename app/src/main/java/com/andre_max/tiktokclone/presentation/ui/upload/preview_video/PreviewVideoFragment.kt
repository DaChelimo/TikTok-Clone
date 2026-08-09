/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.preview_video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.ui.components.video.VideoPlayer
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Looping preview of the just-recorded/selected local video with a Next button into the post
 * step. No ViewModel: the screen has no state beyond the nav argument.
 */
@AndroidEntryPoint
class PreviewVideoFragment : Fragment() {

    private val args by navArgs<PreviewVideoFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                ) {
                    args.localVideo.filePath?.let { filePath ->
                        VideoPlayer(
                            url = filePath,
                            playWhenReady = true,
                            repeatOnEnd = true,
                            onEnded = {},
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    Button(
                        onClick = {
                            findNavController().navigate(
                                PreviewVideoFragmentDirections
                                    .actionPreviewVideoFragmentToPostVideoFragment(args.localVideo),
                            )
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .testTag(PreviewVideoTags.NEXT),
                    ) {
                        Text(stringResource(R.string.next))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ViewUtils.changeSystemBars(activity, SystemBarColors.DARK)
        ViewUtils.changeSystemNavigationBarColor(requireActivity(), R.color.dark_black)
        ViewUtils.hideStatusBar(requireActivity())
        BottomNavViewUtils.hideBottomNavBar(activity)
    }
}

object PreviewVideoTags {
    const val NEXT = "preview_video_next"
}
