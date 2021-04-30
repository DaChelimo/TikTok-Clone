package com.andre_max.tiktokclone.presentation.ui.upload.select_media.tab

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.View
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.LocalVideoGroupLayoutBinding
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.utils.TimeUtils
import com.bumptech.glide.Glide
import com.xwray.groupie.viewbinding.BindableItem
import timber.log.Timber

class LocalVideoGroup(
    private val localVideo: LocalVideo,
    private val onClickListener: () -> Unit
) : BindableItem<LocalVideoGroupLayoutBinding>() {

    override fun bind(binding: LocalVideoGroupLayoutBinding, position: Int) {
        Timber.d("Cursor url is ${localVideo.url} and Uri parser uri is ${Uri.parse(localVideo.url)}")

        val mediaMetadataRetriever = MediaMetadataRetriever().apply {
            setDataSource(binding.root.context, Uri.parse(localVideo.url))
        }
        val bitmap = mediaMetadataRetriever.frameAtTime
        mediaMetadataRetriever.release()

        binding.root.setOnClickListener { onClickListener() }
        Glide.with(binding.root.context).load(bitmap).into(binding.localVideoThumbnail)
        binding.localVideoDuration.text =
            TimeUtils.convertTimeToDisplayTime(localVideo.duration ?: return)
    }

    override fun initializeViewBinding(view: View) =
        LocalVideoGroupLayoutBinding.bind(view)

    override fun getLayout(): Int = R.layout.local_video_group_layout
}