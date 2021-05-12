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

package com.andre_max.tiktokclone.presentation.ui.profile.with_account.tab

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.ProfileVideoTabBinding
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.models.video.VideoType
import com.andre_max.tiktokclone.presentation.ui.components.video.SmallVideoGroup
import com.andre_max.tiktokclone.presentation.ui.profile.MeFragmentDirections
import com.andre_max.tiktokclone.presentation.ui.profile.with_account.ProfileWithAccountFragmentDirections
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class ProfileVideoTab : BaseFragment(R.layout.profile_video_tab) {

    lateinit var binding: ProfileVideoTabBinding
    lateinit var profileUid: String
    lateinit var videoType: VideoType

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    override val viewModel by viewModels<ProfileVideoViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchVideos(profileUid, videoType)
    }

    override fun setUpLayout() {
        binding = ProfileVideoTabBinding.bind(requireView())
        binding.publicVideosRecyclerview.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = groupAdapter
        }
    }

    override fun setUpLiveData() {
        viewModel.listOfRemoteVideo.observe(viewLifecycleOwner) { listOfRemoteVideo ->
            listOfRemoteVideo?.forEach { remoteVideo ->
                val smallVideoGroup = SmallVideoGroup(
                    remoteVideo = remoteVideo,
                    onClickListener = { navigateToLargeVideo(remoteVideo) },
                    { removeGroup(it) }
                )
                groupAdapter.add(smallVideoGroup)
            }
        }
    }

    // If the user clicked on the Me icon in the Bottom Nav Bar, we need to use the MeFragmentDirections otherwise,
    // use the ProfileWithAccountFragmentDirections.
    private fun navigateToLargeVideo(remoteVideo: RemoteVideo) {
        findNavController().navigate(
            if (findNavController().currentDestination?.id == R.id.meFragment)
                MeFragmentDirections
                    .actionMeFragmentToLargeVideoFragment(remoteVideo)
            else
                ProfileWithAccountFragmentDirections
                    .actionProfileWithAccountFragmentToLargeVideoFragment(remoteVideo)
        )
    }

    private fun removeGroup(smallVideoGroup: SmallVideoGroup) {
        groupAdapter.remove(smallVideoGroup)
    }

    companion object {
        fun getInstance(uid: String, videoType: VideoType): ProfileVideoTab {
            val profileVideosFragment = ProfileVideoTab()
            profileVideosFragment.profileUid = uid
            profileVideosFragment.videoType = videoType
            return profileVideosFragment
        }
    }
}