/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.auth.AuthRepo
import com.andre_max.tiktokclone.presentation.ui.profile.without_account.ProfileWithoutAccountScreen
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The Me tab: the signed-in user's own profile, or the sign-up prompt without a session.
 *
 * The uid is Compose state refreshed in `onResume` (the legacy child-fragment swap, minus the
 * fragments): signing out or switching accounts recomposes to the right content, and the
 * ViewModel is keyed on the uid so a new user never sees a stale profile.
 */
@AndroidEntryPoint
class MeFragment : Fragment() {

    @Inject
    lateinit var authRepo: AuthRepo

    private var currentUid by mutableStateOf<String?>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                val uid = currentUid
                if (uid == null) {
                    ProfileWithoutAccountScreen(
                        onSignUpClicked = {
                            findNavController().navigate(R.id.action_meFragment_to_signUpFragment)
                        },
                    )
                } else {
                    ProfileRoute(
                        onNavigateToVideo = { video ->
                            findNavController().navigate(
                                MeFragmentDirections.actionMeFragmentToLargeVideoFragment(video),
                            )
                        },
                        onSignedOut = { currentUid = null },
                        viewModel = hiltViewModel(key = "profile-$uid"),
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currentUid = authRepo.currentUserId()
        ViewUtils.changeSystemBars(activity, SystemBarColors.WHITE)
    }
}
