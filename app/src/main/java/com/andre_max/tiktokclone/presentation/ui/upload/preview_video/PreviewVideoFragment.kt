package com.andre_max.tiktokclone.presentation.ui.upload.preview_video

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.presentation.MainActivity
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentPreviewVideoBinding
import com.andre_max.tiktokclone.presentation.exoplayer.Player
import com.andre_max.tiktokclone.utils.BottomNavViewUtils.hideBottomNavBar
import com.google.android.exoplayer2.SimpleExoPlayer
import timber.log.Timber


class PreviewVideoFragment : Fragment(R.layout.fragment_preview_video) {

    lateinit var binding: FragmentPreviewVideoBinding
    lateinit var player: Player

    private val args by navArgs<PreviewVideoFragmentArgs>()
    private val localVideo: LocalVideo by lazy { args.localVideo }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListeners()

        binding.playerView.hideController()

        player = Player(
            simpleExoplayerView = binding.playerView,
            context = requireContext(),
            url = localVideo.url,
            onVideoEnded = {
                player.restartPlayer()
            }
        )

        player.startOrResumePlayer()
    }

    private fun setOnClickListeners() {
        binding.playerView.setOnClickListener {
            player.doPlayerChange(binding.playBtn)
        }

        binding.nextBtn.setOnClickListener {
            findNavController().navigate(PreviewVideoFragmentDirections.actionPreviewVideoFragmentToPostVideoFragment(localVideo))
        }

        binding.playerView.setOnClickListener {
            player.doPlayerChange(binding.playBtn)
        }
    }

    override fun onResume() {
        super.onResume()
        player.startOrResumePlayer()
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