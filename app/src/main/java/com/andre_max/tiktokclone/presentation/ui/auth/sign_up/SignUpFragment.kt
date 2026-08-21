/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.sign_up

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

/** Compose host for screen 7. Navigation callbacks only — all logic lives in [SignUpViewModel]. */
@AndroidEntryPoint
class SignUpFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                SignUpRoute(
                    onNavigateToAge = {
                        findNavController().navigate(R.id.action_signUpFragment_to_ageFragment)
                    },
                    onNavigateToLogIn = {
                        findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
                    },
                    onNavigateToUsername = {
                        findNavController().navigate(
                            SignUpFragmentDirections
                                .actionSignUpFragmentToCreateUsernameFragment()
                        )
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
