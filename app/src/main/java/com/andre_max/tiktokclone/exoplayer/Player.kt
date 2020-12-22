package com.andre_max.tiktokclone.exoplayer

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.andre_max.tiktokclone.R
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber

class Player(
    val simpleExoplayerView: PlayerView,
    val context: Context,
    private val url: String?
) {

    private var currentWindow = 0
    private var playbackPosition = 0L
    private var isPlaying = false
    private var simpleExoPlayer: SimpleExoPlayer? = null


    fun startPlayer() {
        simpleExoplayerView.setControllerVisibilityListener { visibility ->
            simpleExoplayerView.hideController()
            Timber.d("visibility is $visibility")
            simpleExoplayerView.controllerAutoShow = false
            simpleExoplayerView.controllerShowTimeoutMs = 1
            simpleExoplayerView.controllerHideOnTouch = true
        }

        if (simpleExoPlayer == null) {
            Timber.d("Player is null")
            Timber.d("current window is $currentWindow and playback position is $playbackPosition")
            isPlaying = true
            simpleExoPlayer = SimpleExoPlayer.Builder(context)
                .setUseLazyPreparation(true)
                .build()
            val mediaDataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.resources.getString(R.string.app_name))
            )

            val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
                .createMediaSource(Uri.parse(url))

            simpleExoplayerView.player = simpleExoPlayer

            with(simpleExoPlayer ?: return) {
                prepare(mediaSource)
                playWhenReady = true
                repeatMode = SimpleExoPlayer.REPEAT_MODE_ALL
                addListener(playerListener)
                seekTo(currentWindow, playbackPosition)
            }

            simpleExoplayerView.also {
                it.setShutterBackgroundColor(Color.TRANSPARENT)
                it.requestFocus()
                it.player = simpleExoPlayer
            }
            Timber.d("After simpleExoplayerView.requestFocus called.")
        } else {
            Timber.d("player is not null")
            isPlaying = true
            simpleExoPlayer?.seekTo(0, playbackPosition)
            simpleExoPlayer?.playWhenReady = true
        }
    }

    fun setUpPlayer(playBtn: ImageView) {
        startPlayer()

        simpleExoplayerView.setOnClickListener {
            doPlayerChange(playBtn)
        }
    }

    fun doPlayerChange(playBtn: ImageView) {
        Timber.d("IsPlaying is $isPlaying")
        if (isPlaying) {
            pausePlayer()
            playBtn.visibility = View.VISIBLE
        } else {
            startPlayer()
            playBtn.visibility = View.GONE
        }
        simpleExoplayerView.hideController()
    }


    private val playerListener = object : Player.EventListener {
        override fun onPlayerError(error: ExoPlaybackException) {
            Timber.e(error)
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

    fun pausePlayer() {
        if (simpleExoPlayer != null) {
            isPlaying = false
            playbackPosition = simpleExoPlayer!!.currentPosition
            currentWindow = simpleExoPlayer!!.currentWindowIndex
            simpleExoPlayer?.playWhenReady = false
        }
    }

    fun stopPlayer() {
        pausePlayer()
        simpleExoPlayer?.release()
        simpleExoPlayer = null
    }

}