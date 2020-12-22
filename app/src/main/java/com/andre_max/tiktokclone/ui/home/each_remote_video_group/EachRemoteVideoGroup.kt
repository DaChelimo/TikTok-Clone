package com.andre_max.tiktokclone.ui.home.each_remote_video_group

import android.view.View
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.EachTiktokVideoLayoutBinding
import com.andre_max.tiktokclone.exoplayer.Player
import com.andre_max.tiktokclone.utils.FirestoreUtils
import com.andre_max.tiktokclone.utils.ViewUtils
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class EachRemoteVideoGroup(
    private val remoteUserVideo: RemoteUserVideo,
    private val setHomeFragmentPlayer: (Player) -> Unit
) : BindableItem<EachTiktokVideoLayoutBinding>() {

    override fun bind(binding: EachTiktokVideoLayoutBinding, position: Int) {
        binding.videoDescription.text = remoteUserVideo.description ?: "#NoDescription"

        GlobalScope.launch {
            FirestoreUtils.getUserProfile(remoteUserVideo.creatorUid).collect { user ->
                binding.videoCreatorTag.text = user?.username ?: "@..."
                ViewUtils.loadGlideImage(binding.videoCreatorIcon, user?.profilePictureUrl)
            }
        }

    }

    override fun onViewAttachedToWindow(viewHolder: GroupieViewHolder<EachTiktokVideoLayoutBinding>) {
        super.onViewAttachedToWindow(viewHolder)
        Timber.d("onViewAttachedToWindow called.")

        val binding = viewHolder.binding
        val simpleExoPlayerView = binding.simpleExoPlayerView

        val player = Player(simpleExoPlayerView, binding.root.context, remoteUserVideo.url)
        player.setUpPlayer(binding.playBtn)

        setHomeFragmentPlayer(player)
    }

    override fun onViewDetachedFromWindow(viewHolder: GroupieViewHolder<EachTiktokVideoLayoutBinding>) {
        super.onViewDetachedFromWindow(viewHolder)
        Timber.d("onViewDetachedFromWindow called.")

        viewHolder.binding.simpleExoPlayerView.player?.release()
    }

    override fun initializeViewBinding(view: View) =
        EachTiktokVideoLayoutBinding.bind(view)

    override fun getLayout() = R.layout.each_tiktok_video_layout
}