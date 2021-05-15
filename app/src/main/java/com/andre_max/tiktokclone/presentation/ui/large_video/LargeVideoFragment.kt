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

package com.andre_max.tiktokclone.presentation.ui.large_video

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.LargeVideoLayoutBinding
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.presentation.ui.components.video.MainLargeVideo
import com.andre_max.tiktokclone.repo.network.user.DefaultUserRepo
import com.andre_max.tiktokclone.repo.network.videos.DefaultVideosRepo
import com.andre_max.tiktokclone.utils.BottomNavViewUtils.changeNavBarColor
import com.andre_max.tiktokclone.utils.BottomNavViewUtils.hideBottomNavBar
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment

/**
 * This is a fragment that displays a fullScreen video. Fragments that navigate to this fragment include
 * the SearchFragment, ProfileWithAccountFragment, and others I'm lazy to add in this KDoc
 */
class LargeVideoFragment : BaseFragment(R.layout.large_video_layout) {

    private lateinit var binding: LargeVideoLayoutBinding
    private lateinit var remoteVideo: RemoteVideo
    private val args by navArgs<LargeVideoFragmentArgs>()

    private val mainLargeVideo by lazy {
        MainLargeVideo(
            scope = lifecycleScope,
            lifecycleOwner = viewLifecycleOwner,
            binding = binding,
            userRepo = DefaultUserRepo(),
            videosRepo = DefaultVideosRepo(),
            onPersonIconClicked = {
                findNavController().navigate(
                    LargeVideoFragmentDirections
                        .actionLargeVideoFragmentToProfileWithAccountFragment(remoteVideo.authorUid)
                )
            },
            onVideoEnded = { player ->
                // TODO: Change this to scroll to the next video. Thinking of using a LinkedList(the little DSA I know)
                player.restartPlayer()
            },
            onCommentVisibilityChanged = { isVisible ->
                changeNavBarColor(
                    activity = activity,
                    systemBarColors = if (isVisible) SystemBarColors.WHITE else SystemBarColors.DARK
                )
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        remoteVideo = args.remoteVideo
        mainLargeVideo.init(remoteVideo)
    }

    override fun setUpLayout() {
        binding = LargeVideoLayoutBinding.bind(requireView())
    }

    // TODO: Confirm this
    override fun onResume() {
        super.onResume()
        binding.bottomAddCommentBtn.visibility = View.VISIBLE
        hideBottomNavBar(activity)
        ViewUtils.changeSystemBars(activity, SystemBarColors.DARK)

        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                    or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
    }


}