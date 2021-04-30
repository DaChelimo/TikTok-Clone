package com.andre_max.tiktokclone.presentation.ui.home.each_remote_video_group

import android.view.View
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.LargeVideoLayoutBinding
import com.andre_max.tiktokclone.models.succeeded
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.presentation.exoplayer.Player
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.utils.ImageUtils
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * This is a fullScreen video that is displayed in a recyclerview in the {@link [com.andre_max.tiktokclone.presentation.ui.home.HomeFragment]}
 *
 * @param userRepo - The repo that handles our Firebase Firestore database
 * @param remoteVideo - Provides information necessary to retrieve the video
 * @param onClick - Lambda that abstracts the onClick method to the parent class
 * @param onVideoEnded - Lambda that is invoked when the video being played has ended, allowing us to scroll to the next video
 */
class LargeVideoGroup(
    private val userRepo: UserRepo,
    private val remoteVideo: RemoteVideo,
    private val onClick: (Player) -> Unit,
    private val onVideoEnded: (LargeVideoGroup) -> Unit
) : BindableItem<LargeVideoLayoutBinding>() {

    override fun bind(binding: LargeVideoLayoutBinding, position: Int) {
        binding.videoDescription.text = remoteVideo.description ?: "#NoDescription"

        GlobalScope.launch {
            val result = userRepo.getUserProfile(remoteVideo.authorUid)

            if (result.succeeded) {
                val videoAuthor = result.forceData()
                binding.authorUsername.text = videoAuthor?.username ?: "@..."
                ImageUtils.loadGlideImage(binding.authorIcon, videoAuthor?.profilePictureUrl)
            }
        }
    }

    override fun onViewAttachedToWindow(viewHolder: GroupieViewHolder<LargeVideoLayoutBinding>) {
        super.onViewAttachedToWindow(viewHolder)
        Timber.d("onViewAttachedToWindow called.")

        val binding = viewHolder.binding
        val simpleExoPlayerView = binding.simpleExoPlayerView

        // We are not providing the onVideoEnded directly since it requires EachRemoteVideoGroup
        val player = Player(
            simpleExoplayerView = simpleExoPlayerView,
            context = binding.root.context,
            url = remoteVideo.url,
            onVideoEnded = { onVideoEnded(this) })

        player.setUpPlayer(binding.playBtn)
        simpleExoPlayerView.setOnClickListener {
            onClick(player)
        }
    }

    override fun onViewDetachedFromWindow(viewHolder: GroupieViewHolder<LargeVideoLayoutBinding>) {
        super.onViewDetachedFromWindow(viewHolder)
        Timber.d("onViewDetachedFromWindow called.")
        viewHolder.binding.simpleExoPlayerView.player?.release()
    }

    override fun initializeViewBinding(view: View) =
        LargeVideoLayoutBinding.bind(view)

    override fun getLayout() = R.layout.large_video_layout
}