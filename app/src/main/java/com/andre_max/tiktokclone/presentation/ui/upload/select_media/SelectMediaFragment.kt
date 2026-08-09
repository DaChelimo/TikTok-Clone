/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.select_media

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
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint

/** Compose host for the gallery picker. */
@AndroidEntryPoint
class SelectMediaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                SelectMediaRoute(
                    onNavigateToPreview = { localVideo ->
                        findNavController().navigate(
                            SelectMediaFragmentDirections
                                .actionSelectMediaFragmentToPreviewVideoFragment(localVideo),
                        )
                    },
                    onClose = { findNavController().popBackStack() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        BottomNavViewUtils.hideBottomNavBar(activity)
        ViewUtils.changeSystemBars(activity, SystemBarColors.WHITE)
    }
}
