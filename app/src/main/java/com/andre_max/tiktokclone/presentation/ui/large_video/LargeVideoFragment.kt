package com.andre_max.tiktokclone.presentation.ui.large_video

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.LargeVideoLayoutBinding
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.presentation.exoplayer.Player
import com.andre_max.tiktokclone.presentation.ui.components.comment.MainComment
import com.andre_max.tiktokclone.presentation.ui.components.video.MainLargeVideo
import com.andre_max.tiktokclone.repo.network.comment.CommentRepo
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.repo.network.videos.VideosRepo
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import com.andre_max.tiktokclone.utils.ImageUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import timber.log.Timber

/**
 * This is a fragment that displays a fullScreen video. Fragments that navigate to this fragment include
 * the SearchFragment, ProfileWithAccountFragment, and others I'm lazy to add in this KDoc
 */
class LargeVideoFragment : BaseFragment(R.layout.large_video_layout) {

    private lateinit var binding: LargeVideoLayoutBinding
    private lateinit var remoteVideo: RemoteVideo
    private lateinit var player: Player

    private val args by navArgs<LargeVideoFragmentArgs>()

    private val mainLargeVideo by lazy {
        MainLargeVideo(
            scope = lifecycleScope,
            lifecycle = viewLifecycleOwner.lifecycle,
            binding = binding,
            userRepo = UserRepo(),
            videosRepo = VideosRepo(),
            onPersonIconClicked = {
                findNavController().navigate(
                    LargeVideoFragmentDirections
                        .actionLargeVideoFragmentToProfileWithAccountFragment(remoteVideo.authorUid)
                )
            },
            onVideoEnded = {}
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        remoteVideo = args.remoteVideo
        mainLargeVideo.init(remoteVideo)
    }

    override fun setUpLayout() {
        binding = LargeVideoLayoutBinding.bind(requireView()).apply {
            lifecycleOwner = viewLifecycleOwner
            isFollowingAuthor = mainLargeVideo.isFollowingAuthor
            isVideoLiked = mainLargeVideo.isVideoLiked
            liveComment = mainLargeVideo.liveUserComment
        }
    }

    override fun onResume() {
        super.onResume()
        BottomNavViewUtils.changeVisibility(activity, shouldShow = false)
        player.resumePlayer()
    }

    override fun onStop() {
        super.onStop()
        player.stopPlayer()
}
}