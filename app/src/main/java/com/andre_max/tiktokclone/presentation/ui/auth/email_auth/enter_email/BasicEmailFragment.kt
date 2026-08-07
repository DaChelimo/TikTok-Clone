/* MIT License — Copyright (c) 2021 Andre-max */

package com.andre_max.tiktokclone.presentation.ui.auth.email_auth.enter_email

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.presentation.ui.auth.select_basic_auth.SelectBasicAuthFragmentDirections
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import dagger.hilt.android.AndroidEntryPoint

/** Compose shell for screen 2. Nav actions hang off the *host* graph entry, because this is a
 * `ViewPager2` page, not a destination; both it and the pager go away with screen 6 (spec §4.6). */
@AndroidEntryPoint
class BasicEmailFragment : Fragment() {

    // In `arguments`, not a property: after process death FragmentStateAdapter restores the page
    // via the no-arg constructor and never calls createFragment(), so a property set only by
    // getInstance() comes back unset.
    private val isLogin: Boolean get() = requireArguments().getBoolean(ARG_IS_LOGIN)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TikTokTheme {
                BasicEmailRoute(
                    isLogin = isLogin,
                    onNavigateToPassword = ::navigateToPassword,
                    onNavigateBack = { findNavController().popBackStack() },
                )
            }
        }
    }

    // MainActivity is adjustNothing for the video feed's sake, which leaves the bottom-pinned
    // domain chips behind the IME. Set per-screen and restored on pause, so no legacy screen's
    // keyboard behavior changes, and so WindowInsets.ime (imePadding) reports below API 30.
    override fun onResume() {
        super.onResume()
        softInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onPause() {
        super.onPause()
        softInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    private fun softInputMode(mode: Int) = activity?.window?.setSoftInputMode(mode)

    // Second line of defence behind BasicEmailViewModel's isNavigating guard: any navigate()
    // that arrives once the transaction has already left selectBasicSignUpFragment would throw
    // ("destination ... cannot be found from the current destination"), so resolve the action
    // against the *current* destination first and drop the call when it no longer applies.
    private fun navigateToPassword(email: String) {
        val directions = SelectBasicAuthFragmentDirections
            .actionSelectBasicSignUpFragmentToCreatePasswordFragment(email, isLogin)
        val navController = findNavController()

        if (navController.currentDestination?.getAction(directions.actionId) == null) return
        navController.navigate(directions)
    }

    companion object {
        private const val ARG_IS_LOGIN = "is_login"

        fun getInstance(isLogin: Boolean) = BasicEmailFragment().apply {
            arguments = bundleOf(ARG_IS_LOGIN to isLogin)
        }
    }
}
