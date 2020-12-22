package com.andre_max.tiktokclone.ui

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.LocalUserVideo
import com.andre_max.tiktokclone.MainActivity
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentPreviewRecordedVideoBinding
import com.andre_max.tiktokclone.exoplayer.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import timber.log.Timber
import java.io.File


class PreviewRecordedVideoFragment : Fragment() {

    lateinit var binding: FragmentPreviewRecordedVideoBinding
    private lateinit var simpleExoplayerView: PlayerView
    private lateinit var player: Player
    private lateinit var localUserVideo: LocalUserVideo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_preview_recorded_video, container, false)
        simpleExoplayerView = binding.playerview
        localUserVideo = PreviewRecordedVideoFragmentArgs.fromBundle(requireArguments()).localUserVideo

        val simpleExoPlayer: SimpleExoPlayer? = null
        val url = localUserVideo.url.toString()

        binding.nextBtn.setOnClickListener {
            val newLocalUserVideo = localUserVideo
            val file = File(localUserVideo.url.toString())
            Timber.d("File is $file and file.exists is ${file.exists()}")
            newLocalUserVideo.url = file.path
            Timber.d("newLocalUserVideo.url is ${newLocalUserVideo.url}")
            findNavController().navigate(PreviewRecordedVideoFragmentDirections.actionPreviewRecordedVideoFragmentToPostVideoFragment(newLocalUserVideo))
        }

        player = Player(
            simpleExoplayerView,
            this.requireContext(),
            simpleExoPlayer,
            url
        )

        player.startPlayer()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).navView.visibility = View.INVISIBLE
        player.startPlayer()
    }

    override fun onStop() {
        super.onStop()
        val simpleExoPlayerView = binding.playerview
        Timber.d("onStop called.")
        simpleExoPlayerView.player?.release()

        simpleExoPlayerView.player = null
    }

}