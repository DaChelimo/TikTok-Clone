/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andre_max.tiktokclone.presentation.ui.components.video

import android.annotation.SuppressLint
import android.content.Intent
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager
import androidx.lifecycle.*
import com.andre_max.tiktokclone.databinding.LargeVideoLayoutBinding
import com.andre_max.tiktokclone.models.succeeded
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.presentation.exoplayer.Player
import com.andre_max.tiktokclone.presentation.ui.components.comment.MainComment
import com.andre_max.tiktokclone.repo.network.comment.DefaultCommentRepo
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.repo.network.videos.VideosRepo
import com.andre_max.tiktokclone.utils.NumbersUtils
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MainLargeVideo(
    private val scope: CoroutineScope,
    private val lifecycleOwner: LifecycleOwner,
    private val binding: LargeVideoLayoutBinding,
    private val userRepo: UserRepo,
    private val videosRepo: VideosRepo,
    private val onPersonIconClicked: (String) -> Unit,
    private val onVideoEnded: (Player) -> Unit,
    private val onCommentVisibilityChanged: (Boolean) -> Unit
) {
    var author: User? = null
    var player: Player? = null

    private var likeCount = 0

    private lateinit var mainComment: MainComment

    /**
     * This is livedata instance holds what the user is currently typing and is passed on to MainComment
     */
    val liveUserComment = MutableLiveData("")

    private val _isVideoLiked = MutableLiveData(false)
    val isVideoLiked: LiveData<Boolean> = _isVideoLiked

    private val _isFollowingAuthor = MutableLiveData(true)
    val isFollowingAuthor: LiveData<Boolean> = _isFollowingAuthor

    fun init(remoteVideo: RemoteVideo) {
        setUpBinding()
        scope.launch {
            createProfile(remoteVideo)
            createPlayer(remoteVideo)
            createMainComment(remoteVideo)
            createVideoInfo(remoteVideo)
            setOnClickListeners(remoteVideo)
            enableDoubleTap(remoteVideo)

            isVideoLiked(remoteVideo)
            isFollowingAuthor(remoteVideo.authorUid)
        }
    }

    private fun setUpBinding() {
        binding.also {
            it.lifecycleOwner = lifecycleOwner
            it.isFollowingAuthor = isFollowingAuthor
            it.isVideoLiked = isVideoLiked
            it.liveComment = liveUserComment
        }
    }

    private fun createVideoInfo(remoteVideo: RemoteVideo) {
        likeCount = remoteVideo.likes.toInt()
        binding.totalVideoLikes.text = NumbersUtils.formatCount(likeCount)
    }

    private fun createMainComment(remoteVideo: RemoteVideo) {
        val commentRepo = DefaultCommentRepo()
        mainComment =
            MainComment(binding, commentRepo, remoteVideo, userRepo, scope, liveUserComment, onCommentVisibilityChanged)
        mainComment.init()
    }

    private suspend fun createProfile(remoteVideo: RemoteVideo) {
        author = userRepo.getUserProfile(remoteVideo.authorUid).tryData()
        Timber.d("author is $author")

        binding.authorUsername.text = author?.username?.let { "@${it}" } ?: "@..."
        Glide.with(binding.root).load(author?.profilePictureUrl).into(binding.authorIcon)
        binding.videoDescription.text = remoteVideo.description ?: "#NoDescription"
    }

    private fun createPlayer(remoteVideo: RemoteVideo) {
        player = Player(
            simpleExoplayerView = binding.simpleExoPlayerView,
            playBtn = binding.playBtn,
            context = binding.root.context,
            url = remoteVideo.url,
            onVideoEnded = { player -> onVideoEnded(player) }
        )
        lifecycleOwner.lifecycle.addObserver(player!!)
        player?.init()
    }

    private fun setOnClickListeners(remoteVideo: RemoteVideo) {
        // TODO: Once the button is clicked, let's show a small pop-up layout that tells him/her to sign up or login
        if (userRepo.doesDeviceHaveAnAccount()) {
            binding.followAuthor.setOnClickListener { followOrUnFollowAuthor() }
            binding.likeVideoIcon.setOnClickListener { likeOrUnlikeVideo(remoteVideo) }
        }

        binding.authorIcon.setOnClickListener { onPersonIconClicked(remoteVideo.authorUid) }
        binding.shareVideoBtn.setOnClickListener {
            // TODO: Create a website that takes in a remote video id and displays them. The website will also check if the
            // device has the app and make an intent to the app. For now, just create a link to the firebase video
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, remoteVideo.url)
            binding.root.context.startActivity(intent)
        }

        binding.bottomAddCommentBtn.setOnClickListener { mainComment.showCommentSection() }
        binding.openCommentSectionBtn.setOnClickListener { mainComment.showCommentSection() }
        binding.exitCommentSectionBtn.setOnClickListener { mainComment.hideCommentSection() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableDoubleTap(remoteVideo: RemoteVideo) {
        val gd = GestureDetector(binding.root.context, object: GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                player?.changePlayerState()
                Timber.d("onSingleTapConfirmed called")
                return true
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                likeOrUnlikeVideo(remoteVideo)
                Timber.d("onDoubleTap called")
                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent?) = true
        })
        binding.materialCardView.setOnTouchListener { view, event ->
            view.performClick()
            return@setOnTouchListener gd.onTouchEvent(event)
        }
    }

    private fun likeOrUnlikeVideo(remoteVideo: RemoteVideo) {
        scope.launch {
            // Change the heart icon
            val shouldLike =
                isVideoLiked.value != true // Simply put if the user already likes the video, dislike it otherwise like it
            _isVideoLiked.value = shouldLike

            // Change like count
            changeLikeCount(shouldLike)

            videosRepo.likeOrUnlikeVideo(
                videoId = remoteVideo.videoId,
                authorId = remoteVideo.authorUid,
                shouldLike = shouldLike
            )
        }
    }

    private fun changeLikeCount(shouldLike: Boolean) {
        if (shouldLike) likeCount++ else likeCount--
        binding.totalVideoLikes.text = NumbersUtils.formatCount(likeCount)
    }

    /**
     * Sets isVideoLiked to true if the user likes the video
     *
     * @param remoteVideo the video currently being displayed to the user
     */
    private suspend fun isVideoLiked(remoteVideo: RemoteVideo) {
        val isLiked = videosRepo.isVideoLiked(remoteVideo.videoId)
        _isVideoLiked.value = isLiked.succeeded && isLiked.forceData()
    }


    /**
     * Sets isFollowingAuthor to true hence removing the icon if we are the video author or we are following the author already
     *
     * @param authorUid the uid of the video author
     */
    private suspend fun isFollowingAuthor(authorUid: String?) {
        _isFollowingAuthor.value =
            (authorUid == Firebase.auth.uid) || (userRepo.isFollowingAuthor(authorUid))
    }

    /**
     * This function adds the author to our following module and adds me to the author's followers module.
     */
    private fun followOrUnFollowAuthor() {
        if (author?.uid != Firebase.auth.uid) { // Author cannot follow or unfollow himself
            scope.launch {
                if (isFollowingAuthor.value == false) {
                    _isFollowingAuthor.value = true
                    userRepo.followAuthor(author?.uid)
                } else {
                    _isFollowingAuthor.value = false
                    userRepo.unFollowAuthor(author?.uid)
                }
            }
        }
    }

    fun destroy() {
        mainComment.destroy()

        player?.let {
            it.stopPlayer()
            lifecycleOwner.lifecycle.removeObserver(it)
            player = null
        }
    }
}