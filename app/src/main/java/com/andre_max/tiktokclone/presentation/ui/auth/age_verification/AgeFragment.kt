/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.age_verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import dagger.hilt.android.AndroidEntryPoint

/** Compose host for screen 1. Navigation callbacks only — all logic lives in [AgeViewModel]. */
@AndroidEntryPoint
class AgeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                AgeRoute(
                    onNavigateToSelectAuth = {
                        findNavController().navigate(
                            AgeFragmentDirections
                                .actionAgeFragmentToSelectBasicSignUpFragment()
                        )
                    },
                    onNavigateBack = { findNavController().popBackStack() },
                )
            }
        }
    }

    // SignUpFragment hid the nav bar before navigating here, but an activity recreation
    // re-inflates activity_main with navView VISIBLE, overlaying the wheel picker.
    //
    // This must NOT run in onViewCreated: activity_main hosts the NavHost via the legacy
    // <fragment> tag, so on process-death restore this fragment's view is created *inside*
    // ActivityMainBinding.inflate(), i.e. before MainActivity.binding is assigned — reading
    // the lateinit there throws UninitializedPropertyAccessException and crash-loops launch.
    // onStart is driven by the host activity's own lifecycle, so binding always exists by then.
    override fun onStart() {
        super.onStart()
        BottomNavViewUtils.hideBottomNavBar(activity)
    }
}
