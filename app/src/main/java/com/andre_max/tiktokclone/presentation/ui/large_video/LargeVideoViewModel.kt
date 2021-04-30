package com.andre_max.tiktokclone.presentation.ui.large_video

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.models.succeeded
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.repo.network.comment.CommentRepo
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.repo.network.videos.VideosRepo
import com.andre_max.tiktokclone.utils.ResUtils
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class LargeVideoViewModel : ViewModel() {
    val commentRepo = CommentRepo()
    val userRepo = UserRepo()
    private val videosRepo = VideosRepo()

    val liveComment = MutableLiveData("")

    private val _author = MutableLiveData<User>()
    val author: LiveData<User> = _author

    private val _isVideoLiked = MutableLiveData(false)
    val isVideoLiked: LiveData<Boolean> = _isVideoLiked

    private val _isFollowingAuthor = MutableLiveData(true)
    val isFollowingAuthor: LiveData<Boolean> = _isFollowingAuthor

    fun setUp(remoteVideo: RemoteVideo) {
        viewModelScope.launch {
            val result = userRepo.getUserProfile(remoteVideo.authorUid)
            _author.value = result.getData()

            isVideoLiked(remoteVideo)
            isFollowingAuthor(remoteVideo.authorUid)
        }
    }

    fun likeOrUnlikeVideo(remoteVideo: RemoteVideo) {
        viewModelScope.launch {
            val shouldLike =
                isVideoLiked.value != true // Simply put if the user already likes the video, dislike it otherwise like it

            videosRepo.likeOrUnlikeVideo(
                videoId = remoteVideo.videoId,
                authorId = remoteVideo.authorUid,
                shouldLike = shouldLike
            )
            _isVideoLiked.value = shouldLike
        }
    }

    /**
     * Sets isVideoLiked to true if the user is not the video author and liked the video
     * since the author shouldn't be able to like his/her own video
     *
     * @param remoteVideo the video currently being displayed to the user
     */
    private suspend fun isVideoLiked(remoteVideo: RemoteVideo) {
        _isVideoLiked.value =
            if (remoteVideo.authorUid == Firebase.auth.uid) false
            else {
                val isLiked = videosRepo.isVideoLiked(remoteVideo.videoId)
                isLiked.succeeded && isLiked.forceData()
            }
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
    fun followOrUnFollowAuthor() {
        viewModelScope.launch {
            if (isFollowingAuthor.value == false) {
                followAuthor()
            } else {
                unFollowAuthor()
            }
        }
    }

    private suspend fun unFollowAuthor() {
        userRepo.changeAuthorInMyFollowing(
            authorUid = author.value?.uid,
            shouldAddAuthor = false
        )
        userRepo.changeMeInAuthorFollowers(
            authorUid = author.value?.uid,
            shouldAddMe = false
        )
        _isFollowingAuthor.value = false
    }

    private suspend fun followAuthor() {
        userRepo.changeAuthorInMyFollowing(
            authorUid = author.value?.uid,
            shouldAddAuthor = true
        )
        userRepo.changeMeInAuthorFollowers(
            authorUid = author.value?.uid,
            shouldAddMe = true
        )
        _isFollowingAuthor.value = true
    }
}