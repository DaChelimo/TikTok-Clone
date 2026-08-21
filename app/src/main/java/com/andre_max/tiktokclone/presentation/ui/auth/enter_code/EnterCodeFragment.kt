/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.enter_code

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

/** Compose host for the SMS-code screen. Navigation callbacks only. */
@AndroidEntryPoint
class EnterCodeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                EnterCodeRoute(
                    onNavigateToUsername = {
                        findNavController().navigate(
                            EnterCodeFragmentDirections
                                .actionEnterCodeFragmentToCreateUsernameFragment()
                        )
                    },
                    onNavigateToHome = {
                        findNavController().navigate(
                            R.id.meFragment,
                            null,
                            navOptions { popUpTo = R.id.homeFragment },
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
