/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.select_basic_auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Compose host for screen 6.
 *
 * Was the structural payoff of this slice: a `ViewPager2` + `TabLayoutMediator` +
 * `FragmentStateAdapter` whose only purpose was to host two child Fragments, which in turn only
 * existed because `FragmentStateAdapter` needs Fragments. `HorizontalPager` hosts composables
 * directly, so the adapter, both child Fragments, and the `getInstance(isLogin)` +
 * `Delegates.notNull` handshake they needed all go away — `isLogin` is now an ordinary parameter.
 */
@AndroidEntryPoint
class SelectBasicAuthFragment : Fragment() {

    private val args by navArgs<SelectBasicAuthFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                SelectBasicAuthScreen(
                    isLogin = args.isLogIn,
                    onBackClick = { findNavController().popBackStack() },
                    onNavigateToPassword = ::navigateToPassword,
                    onNavigateToVerificationCode = ::navigateToVerificationCode,
                    onNavigateToUsername = ::navigateToUsername,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        BottomNavViewUtils.hideBottomNavBar(activity)
    }

    private fun navigateToPassword(email: String) {
        findNavController().navigate(
            SelectBasicAuthFragmentDirections
                .actionSelectBasicSignUpFragmentToCreatePasswordFragment(email, args.isLogIn)
        )
    }

    private fun navigateToVerificationCode(verificationId: String) {
        findNavController().navigate(
            SelectBasicAuthFragmentDirections
                .actionSelectBasicSignUpFragmentToEnterCodeFragment(verificationId, args.isLogIn)
        )
    }

    private fun navigateToUsername() {
        findNavController().navigate(
            SelectBasicAuthFragmentDirections
                .actionSelectBasicSignUpFragmentToCreateUsernameFragment()
        )
    }
}
