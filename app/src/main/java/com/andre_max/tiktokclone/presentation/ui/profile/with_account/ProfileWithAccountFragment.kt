/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.profile.with_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.presentation.ui.profile.ProfileRoute
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Compose host for another user's profile (reached with a `uid` nav argument, which the
 * ViewModel reads via SavedStateHandle). Navigation callbacks and system-bar chrome only.
 */
@AndroidEntryPoint
class ProfileWithAccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                ProfileRoute(
                    onNavigateToVideo = { video ->
                        findNavController().navigate(
                            ProfileWithAccountFragmentDirections
                                .actionProfileWithAccountFragmentToLargeVideoFragment(video),
                        )
                    },
                    // Unreachable here: the menu only renders on the own profile (Me tab).
                    onNavigateToSettings = {},
                    onSignedOut = { findNavController().popBackStack() },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ViewUtils.changeSystemBars(activity, SystemBarColors.WHITE)
    }

    override fun onResume() {
        super.onResume()
        BottomNavViewUtils.showBottomNavBar(activity)
    }
}
