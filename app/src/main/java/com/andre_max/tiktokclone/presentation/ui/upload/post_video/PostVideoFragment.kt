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
import com.andre_max.tiktokclone.utils.ResUtils
import com.andre_max.tiktokclone.utils.ViewUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import com.bumptech.glide.Glide
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
            viewModel.postVideo(localVideo)
        }
    }

    private suspend fun loadVideoThumbnail(): Unit = withContext(Dispatchers.IO) {
        val mediaMetadataRetriever = MediaMetadataRetriever().apply {
            setDataSource(requireContext(), localVideo.filePath?.toUri())
        }

        val bitmap = mediaMetadataRetriever.frameAtTime
        mediaMetadataRetriever.release()

        withContext(Dispatchers.Main) {
            Glide.with(requireContext()).load(bitmap).into(binding.videoThumbnail)
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
        ViewUtils.changeStatusBarColor(requireActivity(), android.R.color.white)
        ViewUtils.changeStatusBarIcons(requireActivity(), isWhite = false)
        ViewUtils.changeSystemNavigationBarColor(requireActivity(), android.R.color.white)
    }
}