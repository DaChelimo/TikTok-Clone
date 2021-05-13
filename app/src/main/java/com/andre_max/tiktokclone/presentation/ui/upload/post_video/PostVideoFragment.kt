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

package com.andre_max.tiktokclone.presentation.ui.upload.post_video

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentPostVideoBinding
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.andre_max.tiktokclone.models.upload.Progress
import com.andre_max.tiktokclone.presentation.MainActivity
import com.andre_max.tiktokclone.utils.ImageUtils
import com.andre_max.tiktokclone.utils.ResUtils
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PostVideoFragment : BaseFragment(R.layout.fragment_post_video) {

    lateinit var binding: FragmentPostVideoBinding

    private val args by navArgs<PostVideoFragmentArgs>()
    private val localVideo: LocalVideo by lazy { args.localVideo }
    override val viewModel by viewModels<PostVideoViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch { loadVideoThumbnail() }

        binding.postBtn.setOnClickListener {
            viewModel.postVideo(requireContext(), localVideo)
        }
    }

    private suspend fun loadVideoThumbnail(): Unit = withContext(Dispatchers.IO) {
        val mediaMetadataRetriever = MediaMetadataRetriever().apply {
            setDataSource(requireContext(), localVideo.filePath?.toUri())
        }

        val bitmap = mediaMetadataRetriever.frameAtTime
        mediaMetadataRetriever.release()

        withContext(Dispatchers.Main) {
            val requestOptions = RequestOptions()
            Glide.with(requireContext())
                .applyDefaultRequestOptions(requestOptions)
                .addDefaultRequestListener(ImageUtils.getRequestListener(binding.loadingBar) {  })
                .load(bitmap)
                .into(binding.videoThumbnail)
        }
    }

    override fun setUpLayout() {
        binding = FragmentPostVideoBinding.bind(requireView()).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.viewModel = viewModel
            it.loadingLayout.uploadStatus = viewModel.uploadStatus
        }
    }

    override fun setUpLiveData() {
        super.setUpLiveData()
        viewModel.uploadStatus.observe(viewLifecycleOwner) { uploadStatus ->
            if (uploadStatus == Progress.DONE) // Set the navItem to home
                (activity as MainActivity).binding.navView.selectedItemId = R.id.homeFragment
            else if (uploadStatus == Progress.FAILED)
                ResUtils.showSnackBar(requireView(), R.string.error_occurred_during_video_upload)
        }
    }

    override fun onResume() {
        super.onResume()
        ViewUtils.changeSystemBars(activity, SystemBarColors.WHITE)
    }
}