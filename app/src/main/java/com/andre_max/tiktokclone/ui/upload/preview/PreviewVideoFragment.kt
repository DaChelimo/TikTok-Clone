package com.andre_max.tiktokclone.ui.upload.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.LocalUserVideo
import com.andre_max.tiktokclone.MainActivity
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentPreviewVideoBinding
import com.andre_max.tiktokclone.exoplayer.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import timber.log.Timber


class PreviewVideoFragment : Fragment() {

    lateinit var binding: FragmentPreviewVideoBinding
    lateinit var localUserVideo: LocalUserVideo
    lateinit var simpleExoplayerView: PlayerView
    lateinit var player: Player

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        localUserVideo = PreviewVideoFragmentArgs.fromBundle(requireArguments()).localUserVideo
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_preview_video, container, false)


        simpleExoplayerView = binding.playerView
        simpleExoplayerView.hideController()

        val simpleExoPlayer: SimpleExoPlayer? = null
        player = Player(
            simpleExoplayerView,
            this.requireContext(),
            simpleExoPlayer,
            localUserVideo.url
        )

        player.startPlayer()

        simpleExoplayerView.setOnClickListener {
            player.doPlayerChange(binding.playBtn)
        }

        binding.nextBtn.setOnClickListener {
            findNavController().navigate(PreviewVideoFragmentDirections.actionPreviewVideoFragmentToPostVideoFragment(localUserVideo))
        }

        binding.playerView.setOnClickListener {
            player.doPlayerChange(binding.playBtn)
        }

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        player.startPlayer()
        Timber.d("onResume called")
        (activity as MainActivity).navView.visibility = View.GONE
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