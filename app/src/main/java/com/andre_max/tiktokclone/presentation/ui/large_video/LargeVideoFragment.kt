/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.large_video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Compose host for the single-video detail screen. Navigation callbacks and system-bar chrome
 * only; the `remoteVideo` nav argument reaches the ViewModel via SavedStateHandle.
 *
 * The bottom nav bar stays hidden for the whole screen (the legacy comment-close handler
 * re-showed it, contradicting its own `onResume` — kept hidden, matching the resume intent).
 */
@AndroidEntryPoint
class LargeVideoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                LargeVideoRoute(
                    onNavigateToProfile = { uid ->
                        findNavController().navigate(
                            LargeVideoFragmentDirections
                                .actionLargeVideoFragmentToProfileWithAccountFragment(uid),
                        )
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        BottomNavViewUtils.hideBottomNavBar(activity)
        ViewUtils.changeSystemBars(activity, SystemBarColors.DARK)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }
}
