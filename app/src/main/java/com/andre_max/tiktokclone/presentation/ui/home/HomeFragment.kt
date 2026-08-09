/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.home

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

/** Compose host for the feed. Navigation callbacks and system-bar chrome only. */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                HomeRoute(
                    onNavigateToProfile = { uid ->
                        findNavController().navigate(
                            HomeFragmentDirections
                                .actionHomeFragmentToProfileWithAccountFragment(uid),
                        )
                    },
                    onCommentSheetVisibilityChanged = { isVisible ->
                        if (isVisible) BottomNavViewUtils.hideBottomNavBar(activity)
                        else BottomNavViewUtils.showBottomNavBar(activity)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        BottomNavViewUtils.showBottomNavBar(activity)
        BottomNavViewUtils.changeNavBarColor(activity, SystemBarColors.DARK)
        ViewUtils.changeSystemBars(activity, SystemBarColors.DARK)
        ViewUtils.hideStatusBar(requireActivity())
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }
}
