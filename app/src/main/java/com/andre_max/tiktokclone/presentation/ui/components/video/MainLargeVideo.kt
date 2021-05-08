package com.andre_max.tiktokclone.presentation.ui.components.video

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.andre_max.tiktokclone.databinding.LargeVideoLayoutBinding
import com.andre_max.tiktokclone.models.succeeded
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.presentation.exoplayer.Player
import com.andre_max.tiktokclone.presentation.ui.components.comment.MainComment
import com.andre_max.tiktokclone.repo.network.comment.CommentRepo
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
    private val lifecycle: Lifecycle,
    private val binding: LargeVideoLayoutBinding,
    private val userRepo: UserRepo,
    private val videosRepo: VideosRepo,
    private val onPersonIconClicked: (String) -> Unit,
    private val onVideoEnded: () -> Unit
) {
    var author: User? = null
    var player: Player? = null

    private lateinit var mainComment: MainComment

    /**
     * This is livedata instance holds what the user is currently typing and is passed on to MainComment
     */
    val liveUserComment = MutableLiveData("")

    private var likeCount = 0

    private val _isVideoLiked = MutableLiveData(false)
    val isVideoLiked: LiveData<Boolean> = _isVideoLiked

    private val _isFollowingAuthor = MutableLiveData(true)
    val isFollowingAuthor: LiveData<Boolean> = _isFollowingAuthor

    fun init(remoteVideo: RemoteVideo) {
        scope.launch {
            createProfile(remoteVideo)
            createPlayer(remoteVideo)
            createMainComment(remoteVideo)
            createVideoInfo(remoteVideo)
            setOnClickListeners(remoteVideo)

            isVideoLiked(remoteVideo)
            isFollowingAuthor(remoteVideo.authorUid)
        }
    }

    private fun createVideoInfo(remoteVideo: RemoteVideo) {
        likeCount = remoteVideo.likes.toInt()
        binding.totalVideoLikes.text = NumbersUtils.formatCount(likeCount)
    }

    private fun createMainComment(remoteVideo: RemoteVideo) {
        val commentRepo = CommentRepo()
        mainComment =
            MainComment(binding, commentRepo, remoteVideo, userRepo, scope, liveUserComment)
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
            onVideoEnded = { onVideoEnded() }
        )
        lifecycle.addObserver(player!!)
        player?.init()
    }

    private fun setOnClickListeners(remoteVideo: RemoteVideo) {
        binding.followAuthor.setOnClickListener { followOrUnFollowAuthor() }
        binding.likeVideoIcon.setOnClickListener { likeOrUnlikeVideo(remoteVideo) }
        binding.authorIcon.setOnClickListener { onPersonIconClicked(remoteVideo.authorUid) }
        binding.shareVideoBtn.setOnClickListener {
            // TODO: Create a website that takes in a remote video id and displays them. The website will also check if the
            // device has the app and make an intent to the app. For now, just create a link to the firebase video
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_TEXT, remoteVideo.url)
            binding.root.context.startActivity(intent)
        }

        binding.openCommentSectionBtn.setOnClickListener { mainComment.showCommentSection() }
        binding.exitCommentSectionBtn.setOnClickListener { mainComment.hideCommentSection() }
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
                    followAuthor()
                } else {
                    unFollowAuthor()
                }
            }
        }
    }

    private suspend fun unFollowAuthor() {
        _isFollowingAuthor.value = false
        userRepo.changeAuthorInMyFollowing(
            authorUid = author?.uid,
            shouldAddAuthor = false
        )
        userRepo.changeMeInAuthorFollowers(
            authorUid = author?.uid,
            shouldAddMe = false
        )
    }

    private suspend fun followAuthor() {
        _isFollowingAuthor.value = true
        userRepo.changeAuthorInMyFollowing(
            authorUid = author?.uid,
            shouldAddAuthor = true
        )
        userRepo.changeMeInAuthorFollowers(
            authorUid = author?.uid,
            shouldAddMe = true
        )
    }

    fun destroy() {
        mainComment.destroy()

        player?.let {
            it.stopPlayer()
            lifecycle.removeObserver(it)
            player = null
        }
    }
}