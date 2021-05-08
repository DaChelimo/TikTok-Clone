package com.andre_max.tiktokclone.presentation.exoplayer

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.andre_max.tiktokclone.R
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber

class Player(
    private val simpleExoplayerView: PlayerView,
    private val playBtn: ImageView,
    private val context: Context,
    private val url: String?,
    private val onVideoEnded: (com.andre_max.tiktokclone.presentation.exoplayer.Player) -> Unit
): LifecycleObserver {
    private var playbackPosition = 0L
    private var simpleExoPlayer: SimpleExoPlayer? = null

    private var isCreated = false
    private var isPlaying = false

    fun init() {
        Timber.d("Player.init has been called")
        createPlayer()

        simpleExoplayerView.setOnClickListener {
            Timber.d("isCreated is $isCreated and isPlaying is $isPlaying")
            if (isCreated) {
                if (isPlaying) {
                    pausePlayer()
                    playBtn.visibility = View.VISIBLE
                } else {
                    resumePlayer()
                    playBtn.visibility = View.GONE
                }
            }
        }

        playBtn.setOnClickListener {
            resumePlayer()
        }
    }

    private fun createPlayer() {
        Timber.d("Creating player")
        isCreated = true

        simpleExoPlayer = SimpleExoPlayer.Builder(context)
            .setUseLazyPreparation(true)
            .build()

        simpleExoPlayer?.addListener(playerListener)
        simpleExoPlayer?.setMediaItem(MediaItem.fromUri(Uri.parse(url)))

        simpleExoplayerView.player = simpleExoPlayer
        simpleExoplayerView.setShutterBackgroundColor(Color.TRANSPARENT)
        simpleExoplayerView.requestFocus()

        simpleExoPlayer?.prepare()
        resumePlayer()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resumePlayer() {
        if (isCreated && !isPlaying) {
            Timber.d("Resuming player")
            isPlaying = true
            playBtn.visibility = View.GONE
            simpleExoPlayer?.seekTo(playbackPosition)
            simpleExoPlayer?.play()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pausePlayer() {
        if (isCreated && isPlaying) {
            isPlaying = false
            playbackPosition = simpleExoPlayer!!.currentPosition
            simpleExoPlayer?.pause()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun stopPlayer() {
        if (isCreated) {
            pausePlayer()
            simpleExoPlayer?.release()
            simpleExoPlayer = null
            isCreated = false
        }
    }

    fun restartPlayer() {
        if (isCreated) {
            playbackPosition = 0
            isPlaying = true
            simpleExoPlayer?.seekTo(0, playbackPosition)
//            simpleExoPlayer?.playWhenReady = true
            simpleExoPlayer?.play()
        }
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
                    onVideoEnded(this@Player)
                }
                Player.STATE_IDLE -> {
                    Timber.d("State is idle")
                }
            }
        }
    }
}