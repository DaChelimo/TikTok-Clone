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