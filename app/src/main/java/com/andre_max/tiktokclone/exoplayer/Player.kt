package com.andre_max.tiktokclone.exoplayer

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.andre_max.tiktokclone.R
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber

class Player(val simpleExoplayerView: PlayerView, val context: Context, var player: SimpleExoPlayer?, val url: String?) {

    var currentWindow = 0
    var playbackPosition = 0L
    var isPlaying = false


    fun startPlayer() {
        simpleExoplayerView.setControllerVisibilityListener { visibility ->
            simpleExoplayerView.hideController()
            Timber.d("visibility is $visibility")
            simpleExoplayerView.controllerAutoShow = false
            simpleExoplayerView.controllerShowTimeoutMs = 1
            simpleExoplayerView.controllerHideOnTouch = true
        }

        if(player == null) {
            Timber.d("Player is null")
            Timber.d("current window is $currentWindow and playback position is $playbackPosition")
            isPlaying = true
            player = ExoPlayerFactory.newSimpleInstance(context)
            val mediaDataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.resources.getString(R.string.app_name))
            )

            val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
                .createMediaSource(Uri.parse(url))

            with(player!!) {
                prepare(mediaSource)
                playWhenReady = true
                repeatMode = SimpleExoPlayer.REPEAT_MODE_ALL
            }

//            player!!.seekTo(currentWindow, playbackPosition)

            player!!.addListener(playerListener)

            simpleExoplayerView.setShutterBackgroundColor(Color.TRANSPARENT)
            simpleExoplayerView.player = player
            simpleExoplayerView.requestFocus()
            Timber.d("After simpleExoplayerView.requestFocus called.")
        }
        else {
            Timber.d("player is not null")
            player!!.seekTo(0, playbackPosition)
            isPlaying = true
            player!!.playWhenReady = true
        }
    }


    fun doPlayerChange(playBtn: ImageView) {
        Timber.d("IsPlaying is $isPlaying")
        if (isPlaying) {
            stopPlayer()
            playBtn.visibility = View.VISIBLE
        } else {
            startPlayer()
            playBtn.visibility = View.GONE
        }
        simpleExoplayerView.hideController()
    }


    private val playerListener = object : Player.EventListener {
        override fun onPlayerError(error: ExoPlaybackException) {
            Timber.e(error.localizedMessage)
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            simpleExoplayerView.hideController()
            when (playbackState) {

                Player.STATE_BUFFERING -> {
                    Timber.d("State is buffering")
                }
                Player.STATE_READY -> {
                    Timber.d("State is ready")
                }
                Player.STATE_ENDED -> {
                    Timber.d("State is ended")
                }
                Player.STATE_IDLE -> {
                    Timber.d("state is idle")
                }

            }
        }
    }

    fun stopPlayer() {
        if (player != null ){
            isPlaying = false
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player?.playWhenReady = false
//            player?.release()
//            player = null
        }
    }

}