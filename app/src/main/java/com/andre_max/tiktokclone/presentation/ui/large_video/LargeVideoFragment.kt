package com.andre_max.tiktokclone.presentation.ui.large_video

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.LargeVideoLayoutBinding
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.presentation.exoplayer.Player
import com.andre_max.tiktokclone.presentation.ui.components.comment.MainComment
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import com.andre_max.tiktokclone.utils.ImageUtils
import com.google.android.exoplayer2.ui.PlayerView
import timber.log.Timber

/**
 * This is a fragment that displays a fullScreen video. Fragments that navigate to this fragment include
 * the SearchFragment, ProfileWithAccountFragment, and others I'm lazy to add in this KDoc
 */
class LargeVideoFragment : Fragment(R.layout.large_video_layout) {

    private lateinit var binding: LargeVideoLayoutBinding
    private lateinit var remoteVideo: RemoteVideo
    private lateinit var player: Player

    private val args by navArgs<LargeVideoFragmentArgs>()
    private val viewModel by viewModels<LargeVideoViewModel>()
    private val mainComment by lazy {
        MainComment(binding, remoteVideo, viewModel.commentRepo, viewModel.userRepo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        remoteVideo = args.remoteVideo
        setUpBindingLayout()
        setUpClickListener()
        setUpLiveData()
        viewModel.setUp(remoteVideo)

        binding.videoDescription.text = remoteVideo.description ?: "#NoDescription"

        player = Player(
            simpleExoplayerView =  binding.simpleExoPlayerView,
            context = requireContext(),
            url = remoteVideo.url,
            onVideoEnded = {}
        )
        player.startOrResumePlayer()
    }

    private fun setUpLiveData() {
        viewModel.author.observe(viewLifecycleOwner) { author ->
            binding.authorUsername.text =
                getString(R.string.author_username, author?.username ?: "...")
            ImageUtils.loadGlideImage(binding.authorIcon, author?.profilePictureUrl)
        }
    }

    private fun setUpClickListener() {
        binding.followAuthor.setOnClickListener { viewModel.followOrUnFollowAuthor() }
        binding.likeVideoIcon.setOnClickListener { viewModel.likeOrUnlikeVideo(remoteVideo) }
        binding.simpleExoPlayerView.setOnClickListener { player.doPlayerChange(binding.playBtn) }

        binding.authorIcon.setOnClickListener {
            findNavController().navigate(
                LargeVideoFragmentDirections
                    .actionLargeVideoFragmentToProfileWithAccountFragment(remoteVideo.authorUid)
            )
        }

        binding.openCommentSectionBtn.setOnClickListener {
            mainComment.setUpCommentSection(binding.commentRecyclerview)
            mainComment.showCommentSection()
        }
        binding.exitCommentSectionBtn.setOnClickListener { mainComment.hideCommentSection() }
    }

    private fun setUpBindingLayout() {
        binding = LargeVideoLayoutBinding.bind(requireView()).apply {
            lifecycleOwner = viewLifecycleOwner
            isFollowingAuthor = viewModel.isFollowingAuthor
            isVideoLiked = viewModel.isVideoLiked
            liveComment = viewModel.liveComment
        }
    }

    override fun onResume() {
        super.onResume()
        BottomNavViewUtils.changeVisibility(activity, shouldShow = false)
        player.startOrResumePlayer()
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop called.")
        binding.simpleExoPlayerView.player?.release()
        binding.simpleExoPlayerView.player = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mainComment.onDestroy()
    }
}