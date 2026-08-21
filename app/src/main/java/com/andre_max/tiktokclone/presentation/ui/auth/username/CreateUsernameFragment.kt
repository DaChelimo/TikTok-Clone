/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.username

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Compose host for screen 4. Navigation callbacks only — all logic lives in
 * [CreateUsernameViewModel].
 *
 * Replaces `auth.create_username.CreateUsernameFragment`, which is where account creation used
 * to live. The `credential`/`googleBody`/`emailBody` nav arguments on this destination are
 * deliberately left in place and unread: the Google and phone paths still navigate here with
 * them, and they are removed once those two are migrated (non-negotiable #2).
 */
@AndroidEntryPoint
class CreateUsernameFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                CreateUsernameRoute(
                    onNavigateToHome = {
                        findNavController()
                            .navigate(R.id.action_createUsernameFragment_to_homeFragment)
                    },
                    onNavigateBack = { findNavController().popBackStack() },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        BottomNavViewUtils.hideBottomNavBar(activity)
    }
}
