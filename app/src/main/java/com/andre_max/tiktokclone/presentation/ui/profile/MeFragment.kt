/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andre_max.tiktokclone.presentation.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.auth.AuthRepo
import com.andre_max.tiktokclone.presentation.ui.profile.with_account.ProfileWithAccountFragment
import com.andre_max.tiktokclone.presentation.ui.profile.without_account.ProfileWithoutAccountFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The profile tab's session gate: signed-in users get their profile, everyone else gets the
 * sign-up prompt.
 *
 * The app deliberately has no launch-time gate — the feed is browsable signed-out, so this tab
 * is the only place the session actually decides anything.
 *
 * Re-evaluated in [onResume] rather than only on creation, because signing out happens inside
 * the child fragment this method chose: without the re-check the user would sign out and keep
 * looking at their own profile until the tab was recreated.
 */
@AndroidEntryPoint
class MeFragment : Fragment(R.layout.fragment_me) {

    @Inject
    lateinit var authRepo: AuthRepo

    private var shownUserId: String? = null
    private var hasShownChild = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProfileFor(authRepo.currentUserId())
    }

    override fun onResume() {
        super.onResume()
        val userId = authRepo.currentUserId()
        if (userId != shownUserId) showProfileFor(userId)
    }

    private fun showProfileFor(userId: String?) {
        val child = if (userId == null) {
            ProfileWithoutAccountFragment()
        } else {
            // ProfileWithAccountFragment reads its uid via navArgs(), so it has to arrive in the
            // arguments Bundle rather than through a constructor parameter.
            ProfileWithAccountFragment().apply {
                arguments = Bundle().also { it.putString("uid", userId) }
            }
        }

        childFragmentManager.beginTransaction()
            .also { if (hasShownChild) it.replace(R.id.frag_container, child) else it.add(R.id.frag_container, child) }
            .commit()

        shownUserId = userId
        hasShownChild = true
    }
}
