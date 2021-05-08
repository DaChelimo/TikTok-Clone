package com.andre_max.tiktokclone.presentation.ui.upload.select_media.tab.group

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.View
import androidx.core.net.toUri
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.LocalVideoGroupLayoutBinding
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.utils.TimeUtils
import com.bumptech.glide.Glide
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

// TODO: Not sure if passing a coroutine scope is a good practice.
// Reasons I'm not using GlobalScope is because it executes the suspend function even when it isn't needed (no lifecycle)
class LocalVideoGroup(
    private val localVideo: LocalVideo,
    private val scope: CoroutineScope,
    private val onClickListener: () -> Unit
) : BindableItem<LocalVideoGroupLayoutBinding>() {

    override fun bind(binding: LocalVideoGroupLayoutBinding, position: Int) {
        Timber.d("Cursor url is ${localVideo.filePath} and Uri parser uri is ${localVideo.filePath?.toUri()}")

        scope.launch(Dispatchers.IO) {
            val mediaMetadataRetriever = MediaMetadataRetriever().apply {
                setDataSource(binding.root.context, Uri.parse(localVideo.filePath))
            }
            val bitmap = mediaMetadataRetriever.frameAtTime
            mediaMetadataRetriever.release()

            withContext(Dispatchers.Main) {
                Glide.with(binding.root.context).load(bitmap).centerCrop()
                    .into(binding.localVideoThumbnail)
            }
        }

        binding.root.setOnClickListener { onClickListener() }
        binding.localVideoDuration.text =
            TimeUtils.convertTimeToDisplayTime(localVideo.duration ?: return)
    }

    override fun initializeViewBinding(view: View) =
        LocalVideoGroupLayoutBinding.bind(view)

    override fun getLayout(): Int = R.layout.local_video_group_layout
}