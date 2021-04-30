package com.andre_max.tiktokclone.presentation.ui.profile.with_account.tab

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentMyPublicVideosBinding
import com.andre_max.tiktokclone.models.video.VideoType
import com.andre_max.tiktokclone.presentation.ui.components.video.SmallVideoGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class ProfileVideoTab : Fragment(R.layout.fragment_my_public_videos) {

    lateinit var binding: FragmentMyPublicVideosBinding
    lateinit var profileUid: String
    lateinit var videoType: VideoType

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private val viewModel by viewModels<ProfileVideoViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLayout()
        setUpLiveData()
        viewModel.fetchVideos(profileUid, videoType)
    }

    private fun setUpLayout() {
        binding.publicVideosRecyclerview.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = groupAdapter
        }
    }


    private fun setUpLiveData() {
        viewModel.listOfRemoteVideo.observe(viewLifecycleOwner) { listOfRemoteVideo ->
            listOfRemoteVideo?.forEach { remoteVideo ->
                val smallVideoGroup = SmallVideoGroup(remoteVideo) {
//                   Navigate: ProfileWithAccount
                }
                groupAdapter.add(smallVideoGroup)
            }
        }
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