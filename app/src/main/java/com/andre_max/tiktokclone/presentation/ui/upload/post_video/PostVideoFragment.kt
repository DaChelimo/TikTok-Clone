package com.andre_max.tiktokclone.presentation.ui.upload.post_video

import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.FragmentPostVideoBinding
import com.andre_max.tiktokclone.models.local.LocalVideo
import com.bumptech.glide.Glide
import java.util.*


class PostVideoFragment : Fragment(R.layout.fragment_post_video) {

    lateinit var binding: FragmentPostVideoBinding

    private val args by navArgs<PostVideoFragmentArgs>()
    private val localVideo: LocalVideo by lazy { args.localVideo }
    private val viewModel by viewModels<PostVideoViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLayout(view)

        val mediaMetadataRetriever = MediaMetadataRetriever().apply {
            setDataSource(requireContext(), localVideo.url?.toUri())
        }

        val bitmap = mediaMetadataRetriever.frameAtTime
        mediaMetadataRetriever.release()

        Glide.with(this).load(bitmap).into(binding.videoThumbnail)

        binding.postBtn.setOnClickListener {
            viewModel.postVideo(localVideo)
        }

    }

    private fun setUpLayout(view: View) {
        binding = FragmentPostVideoBinding.bind(view).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.viewModel = viewModel
        }
    }
}