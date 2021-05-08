package com.andre_max.tiktokclone.presentation.ui.upload.preview_video

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.databinding.FragmentPreviewVideoBinding
import com.andre_max.tiktokclone.presentation.exoplayer.Player
import com.andre_max.tiktokclone.utils.BottomNavViewUtils.hideBottomNavBar
import com.andre_max.tiktokclone.utils.ViewUtils.changeStatusBarColor
import com.andre_max.tiktokclone.utils.ViewUtils.changeSystemNavigationBarColor
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import timber.log.Timber


class PreviewVideoFragment : BaseFragment(R.layout.fragment_preview_video) {

    lateinit var binding: FragmentPreviewVideoBinding

    private val args by navArgs<PreviewVideoFragmentArgs>()
    private val localVideo by lazy { args.localVideo }
    private val player by lazy {
        Player(
            simpleExoplayerView = binding.playerView,
            playBtn = binding.playBtn,
            context = requireContext(),
            url = localVideo.filePath,
            onVideoEnded = {
                it.restartPlayer()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player.init()
    }

    override fun setUpLayout() {
        binding = FragmentPreviewVideoBinding.bind(requireView())
    }

    override fun setUpClickListeners() {
        binding.nextBtn.setOnClickListener {
            findNavController().navigate(
                PreviewVideoFragmentDirections
                    .actionPreviewVideoFragmentToPostVideoFragment(localVideo)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        player.resumePlayer()
        changeStatusBarColor(requireActivity(), R.color.dark_black)
        changeSystemNavigationBarColor(requireActivity(), R.color.dark_black)
        hideBottomNavBar(activity)
    }

    override fun onPause() {
        super.onPause()
        player.pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stopPlayer()
    }

}