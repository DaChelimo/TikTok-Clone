package com.andre_max.tiktokclone.presentation.ui.components.video

import android.net.Uri
import android.view.View
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.SmallVideoLayoutBinding
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.utils.ImageUtils.getRequestListener
import com.andre_max.tiktokclone.utils.TimeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.xwray.groupie.viewbinding.BindableItem
import timber.log.Timber

class SmallVideoGroup(
    private val remoteVideo: RemoteVideo,
    private val onClickListener: () -> Unit,
    private val onLoadFailed: (SmallVideoGroup) -> Unit
) : BindableItem<SmallVideoLayoutBinding>() {

    override fun bind(binding: SmallVideoLayoutBinding, position: Int) {
        Timber.d("Cursor url is ${remoteVideo.url} and Uri parser uri is ${Uri.parse(remoteVideo.url)}")
        binding.root.setOnClickListener { onClickListener() }
        binding.smallVideoDuration.text =
            TimeUtils.convertTimeToDisplayTime(remoteVideo.duration)

        val requestOptions = RequestOptions()
        Glide
            .with(binding.root)
            .applyDefaultRequestOptions(requestOptions)
            .load(remoteVideo.url)
            .addListener(getRequestListener(binding.loadingBar) { onLoadFailed(this) })
            .into(binding.smallVideoThumbnail)
    }

    override fun initializeViewBinding(view: View) =
        SmallVideoLayoutBinding.bind(view)

    override fun getLayout(): Int = R.layout.small_video_layout
}