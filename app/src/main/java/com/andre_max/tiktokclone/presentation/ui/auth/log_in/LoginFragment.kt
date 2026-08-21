/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.log_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import dagger.hilt.android.AndroidEntryPoint

/** Compose host for the log-in landing. Navigation callbacks only. */
@AndroidEntryPoint
class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                LogInRoute(
                    onNavigateToSelectAuth = {
                        findNavController().navigate(
                            LoginFragmentDirections
                                .actionLoginFragmentToSelectBasicSignUpFragment(isLogIn = true)
                        )
                    },
                    onNavigateToHome = ::navigateToProfile,
                    onNavigateBack = { findNavController().popBackStack() },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        BottomNavViewUtils.hideBottomNavBar(activity)
    }

    /**
     * Lands on the profile rather than the feed, matching what the legacy screen did: a user who
     * just logged in is checking that it worked, and the profile is where that is visible.
     */
    private fun navigateToProfile() {
        findNavController().navigate(
            R.id.meFragment,
            null,
            navOptions { popUpTo = R.id.homeFragment },
        )
    }
}
