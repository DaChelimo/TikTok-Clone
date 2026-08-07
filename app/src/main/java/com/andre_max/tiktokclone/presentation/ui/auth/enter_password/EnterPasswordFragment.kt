/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.models.sign_up.EmailBody
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Compose host for screen 3. Navigation callbacks only — all logic lives in
 * [EnterPasswordViewModel].
 */
@AndroidEntryPoint
class EnterPasswordFragment : Fragment() {

    private val args by navArgs<EnterPasswordFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                EnterPasswordRoute(
                    identity = args.email.substringBefore("@"),
                    onNavigateToUsername = ::navigateToCreateUsername,
                    onNavigateBack = { findNavController().popBackStack() },
                )
            }
        }
    }

    private fun navigateToCreateUsername(password: String) {
        findNavController().navigate(
            EnterPasswordFragmentDirections.actionCreatePasswordFragmentToCreateUsernameFragment(
                null, null, EmailBody(args.email, password),
            )
        )
    }
}
