package com.andre_max.tiktokclone.presentation.ui.upload.record

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentRecordVideoBinding
import com.andre_max.tiktokclone.utils.BottomNavViewUtils.hideBottomNavBar
import com.andre_max.tiktokclone.utils.PermissionUtils
import com.andre_max.tiktokclone.utils.PermissionUtils.arePermissionsGranted
import com.andre_max.tiktokclone.utils.PermissionUtils.recordVideoPermissions
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.gesture.Gesture
import com.otaliastudios.cameraview.gesture.GestureAction
import timber.log.Timber

class RecordVideoFragment : Fragment(R.layout.fragment_record_video) {

    private lateinit var binding: FragmentRecordVideoBinding
    private lateinit var cameraView: CameraView

    private val viewModel by viewModels<RecordVideoViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecordVideoBinding.bind(view)
        cameraView = binding.cameraView

        initCameraView()
        checkPermissionsGranted()
        setUpLiveData()
        setUpClickListener()
    }

    private fun setUpLiveData() {
        viewModel.localVideo.observe(viewLifecycleOwner) { localVideo ->
            localVideo?.let {
                findNavController().navigate(
                    RecordVideoFragmentDirections
                        .actionRecordVideoFragmentToPreviewRecordedVideoFragment(localVideo)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideBottomNavBar(requireActivity())
        checkPermissionsGranted()
    }

    private fun initCameraView() {
        cameraView.apply {
            facing = Facing.BACK
            audio = Audio.ON
            mode = Mode.VIDEO
            addCameraListener(viewModel.getCameraListener(requireContext()))

            mapGesture(Gesture.PINCH, GestureAction.ZOOM) // Pinch to zoom!
            mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS) // Tap to focus!
        }
    }

    private fun turnOnCameraPreview() {
        Timber.d("turnOnCameraPreview called")
        cameraView.open()
    }


    private fun setUpClickListener() {
        binding.closeBtn.setOnClickListener { findNavController().popBackStack() }

        binding.uploadImageBtn.setOnClickListener {
            findNavController().navigate(R.id.action_recordVideoFragment_to_createVideoFragment)
        }

        binding.flipCameraBtn.setOnClickListener {
            Timber.d("camera facing is ${cameraView.facing}")
            cameraView.facing = if (cameraView.facing == Facing.FRONT) Facing.BACK else Facing.FRONT
        }

        binding.startRecordingBtn.setOnClickListener {
            val fileDescriptor = viewModel.takeVideo(requireContext()) ?: return@setOnClickListener
            cameraView.takeVideo(fileDescriptor)
        }

        binding.stopRecordingBtn.setOnClickListener {
            Timber.d("cameraView.isTakingVideo is ${cameraView.isTakingVideo}")
            if (cameraView.isTakingVideo) {
                cameraView.stopVideo()
                viewModel.stopVideo(requireContext())
            }
        }
    }

    private val dialogMultiplePermissionsListener =
        PermissionUtils.DialogMultiplePermissionsListener(
            context = requireContext(),
            onPermissionsGranted = { turnOnCameraPreview() },
            onPermissionsDenied = { findNavController().popBackStack() }
        )

    private fun checkPermissionsGranted() {
        if (arePermissionsGranted(requireContext(), recordVideoPermissions))
            turnOnCameraPreview()
        else
            PermissionUtils.requestPermissions(
                requireContext(),
                dialogMultiplePermissionsListener,
                recordVideoPermissions
            )
    }


}