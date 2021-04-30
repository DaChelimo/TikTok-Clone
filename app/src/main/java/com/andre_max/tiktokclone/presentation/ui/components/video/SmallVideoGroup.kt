package com.andre_max.tiktokclone.presentation.ui.components.video

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.View
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.SmallVideoLayoutBinding
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.utils.TimeUtils
import com.bumptech.glide.Glide
import com.xwray.groupie.viewbinding.BindableItem
import timber.log.Timber

class SmallVideoGroup(
    private val remoteVideo: RemoteVideo,
    private val onClickListener: () -> Unit
) : BindableItem<SmallVideoLayoutBinding>() {

    override fun bind(binding: SmallVideoLayoutBinding, position: Int) {
        Timber.d("Cursor url is ${remoteVideo.url} and Uri parser uri is ${Uri.parse(remoteVideo.url)}")

        val mediaMetadataRetriever = MediaMetadataRetriever().apply {
            setDataSource(binding.root.context, Uri.parse(remoteVideo.url))
        }
        val bitmap = mediaMetadataRetriever.frameAtTime
        mediaMetadataRetriever.release()

        binding.root.setOnClickListener { onClickListener() }
        Glide.with(binding.root.context).load(bitmap).into(binding.smallVideoThumbnail)
        binding.smallVideoDuration.text =
            TimeUtils.convertTimeToDisplayTime(remoteVideo.duration.toString())
    }

    override fun initializeViewBinding(view: View) =
        SmallVideoLayoutBinding.bind(view)

    override fun getLayout(): Int = R.layout.small_video_layout
}