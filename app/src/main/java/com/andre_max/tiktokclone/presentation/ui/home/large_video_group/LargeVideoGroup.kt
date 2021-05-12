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

package com.andre_max.tiktokclone.presentation.ui.home.large_video_group

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.LargeVideoLayoutBinding
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.presentation.ui.components.video.MainLargeVideo
import com.andre_max.tiktokclone.repo.network.comment.CommentRepo
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.repo.network.videos.VideosRepo
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.coroutines.CoroutineScope

/**
 * This is a fullScreen video that is displayed in a recyclerview in the {@link [com.andre_max.tiktokclone.presentation.ui.home.HomeFragment]}
 *
 * @param scope the coroutine scope to run all suspend functions in. Not sure if it's a good practice so drop an issue on Github
 * @param lifecycleOwner lifecycle owner used to observe live data in the layout
 * @param userRepo the repo that handles our user database
 * @param commentRepo the repo that handles our comments database
 * @param videosRepo the repo that handles our videos database
 * @param remoteVideo - Provides information necessary to retrieve the video
 * @param onPersonIconClicked Lambda that abstracts the onClick method of the author's icon
 * @param onVideoEnded - Lambda that is invoked when the video being played has ended, allowing us to scroll to the next video
 */
class LargeVideoGroup(
    private val scope: CoroutineScope,
    private val lifecycleOwner: LifecycleOwner,
    private val userRepo: UserRepo,
    private val commentRepo: CommentRepo,
    private val videosRepo: VideosRepo,
    private val remoteVideo: RemoteVideo,
    private val onPersonIconClicked: (String) -> Unit,
    private val onVideoEnded: (LargeVideoGroup) -> Unit,
    private val onCommentVisibilityChanged: (Boolean) -> Unit
) : BindableItem<LargeVideoLayoutBinding>() {

    private lateinit var mainLargeVideo: MainLargeVideo

    override fun bind(binding: LargeVideoLayoutBinding, position: Int) {
        mainLargeVideo = MainLargeVideo(
            scope = scope,
            lifecycle = lifecycleOwner.lifecycle,
            binding = binding,
            userRepo = userRepo,
            videosRepo = videosRepo,
            onPersonIconClicked = onPersonIconClicked,
            onVideoEnded = { onVideoEnded(this) },
            onCommentVisibilityChanged = onCommentVisibilityChanged
        )

        binding.lifecycleOwner = lifecycleOwner
        binding.isFollowingAuthor = mainLargeVideo.isFollowingAuthor
        binding.isVideoLiked = mainLargeVideo.isVideoLiked

        binding.liveComment = mainLargeVideo.liveUserComment
    }

    override fun onViewAttachedToWindow(viewHolder: GroupieViewHolder<LargeVideoLayoutBinding>) {
        super.onViewAttachedToWindow(viewHolder)
        mainLargeVideo.init(remoteVideo)
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
    }

    override fun onViewDetachedFromWindow(viewHolder: GroupieViewHolder<LargeVideoLayoutBinding>) {
        super.onViewDetachedFromWindow(viewHolder)
        mainLargeVideo.destroy()
        lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
    }

    override fun initializeViewBinding(view: View) =
        LargeVideoLayoutBinding.bind(view)

    override fun getLayout() = R.layout.large_video_layout

    private val lifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            mainLargeVideo.player?.pausePlayer()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            mainLargeVideo.player?.pausePlayer()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            mainLargeVideo.destroy()
        }
    }
}