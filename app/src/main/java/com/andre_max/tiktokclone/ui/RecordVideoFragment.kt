package com.andre_max.tiktokclone.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.LocalUserVideo
import com.andre_max.tiktokclone.MainActivity
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentRecordVideoBinding
import com.andre_max.tiktokclone.longToast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.gesture.Gesture
import com.otaliastudios.cameraview.gesture.GestureAction
import timber.log.Timber
import java.io.File
import kotlin.properties.Delegates

class RecordVideoFragment : Fragment() {

    private lateinit var binding: FragmentRecordVideoBinding
    private lateinit var cameraView: CameraView
    private lateinit var file: File
    private lateinit var localUserVideo: LocalUserVideo
    private var timeCreated by Delegates.notNull<Long>()

    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )
    private var hasRecordingStarted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_record_video, container, false)
        cameraView = binding.cameraView

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        initCameraView()
        doPermissionRequest(permissions)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).navView.visibility = View.GONE
        doPermissionRequest(permissions)
    }

    private fun initCameraView() {
        cameraView.facing = Facing.BACK
        cameraView.audio = Audio.ON
        cameraView.mode = Mode.VIDEO
        cameraView.addCameraListener(cameraListener)

        cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM) // Pinch to zoom!
        cameraView.mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS) // Tap to focus!
//        cameraView.mapGesture(Gesture.LONG_TAP, GestureAction.NONE) // Long tap to shoot!
    }

    private fun turnOnCameraPreview() {
        Timber.d("turnOnCameraPreview called")
        cameraView.open()

        setOnClickListeners()
    }


    private fun setOnClickListeners() {
        binding.uploadImageBtn.setOnClickListener {
            findNavController().navigate(R.id.action_recordVideoFragment_to_createVideoFragment)
        }

        binding.flipCameraBtn.setOnClickListener {
            Timber.d("camera facing is ${cameraView.facing}")
            if(cameraView.facing == Facing.FRONT) cameraView.facing = Facing.BACK else cameraView.facing = Facing.FRONT
        }

        binding.startRecordingBtn.setOnClickListener {
            hasRecordingStarted = true
            changeVisibilityOfLayout()

            timeCreated = System.currentTimeMillis()
            val childPath = "${Environment.DIRECTORY_DCIM}/TikTokClone/MyRecordedVideos"
            val dir = File(Environment.getExternalStorageDirectory().path, childPath).mkdirs()
            Timber.d("dir is ${File(Environment.getExternalStorageDirectory().path, childPath)} and dir.created is $dir")
            file = File(Environment.getExternalStorageDirectory().path, "$childPath/$timeCreated.mp4")
            Timber.d("File is $file and file path is ${file.path}")
            cameraView.takeVideo(file)
        }

        binding.stopRecordingBtn.setOnClickListener {
            Timber.d("cameraView.isTakingVideo is ${cameraView.isTakingVideo}")
            if (cameraView.isTakingVideo) {
                cameraView.stopVideo()
            }
        }
    }

    private fun changeVisibilityOfLayout() {
        if (hasRecordingStarted) {
            binding.startRecordingBtn.visibility = View.GONE
            binding.uploadImageBtn.visibility = View.GONE
            binding.uploadPlainText.visibility = View.GONE
            binding.linearLayout.visibility = View.GONE

            binding.stopRecordingBtn.visibility = View.VISIBLE
            binding.stopRecordingDisplayIcon.visibility = View.VISIBLE
            binding.finishRecordingBtn.visibility = View.VISIBLE
        }
        else {
            binding.startRecordingBtn.visibility = View.VISIBLE
            binding.uploadImageBtn.visibility = View.VISIBLE
            binding.uploadPlainText.visibility = View.VISIBLE
            binding.linearLayout.visibility = View.VISIBLE

            binding.stopRecordingBtn.visibility = View.GONE
            binding.stopRecordingDisplayIcon.visibility = View.GONE
            binding.finishRecordingBtn.visibility = View.GONE
        }
    }

    private val cameraListener = object : CameraListener() {
        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
            Timber.d("Video recording has started")
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
            Timber.d("Video recording has ended")
        }

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            Timber.d("Video has been taken and result.size is ${result.size}")
            val mp = MediaPlayer.create(this@RecordVideoFragment.requireContext(), result.file.toURI().toString().toUri())
            val duration = mp.duration
            Timber.d("result.duration is $duration")
            localUserVideo = LocalUserVideo(result.file.toURI().toString(), duration.toString(), timeCreated.toString())
            findNavController().navigate(RecordVideoFragmentDirections.actionRecordVideoFragmentToPreviewRecordedVideoFragment(localUserVideo))
        }
    }

    private val dialogMultiplePermissionsListener = DialogMultiplePermissionsListener(this)

    class DialogMultiplePermissionsListener(val fragment: RecordVideoFragment) :
        BaseMultiplePermissionsListener() {
        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
            Timber.d("it.areAllPermissionsGranted() is ${report?.areAllPermissionsGranted()} and permissions denied is ${report?.deniedPermissionResponses}")


            report?.let {
                if (it.areAllPermissionsGranted()) {
                    fragment.longToast("All permissions granted.")
                    fragment.turnOnCameraPreview()
                } else {
                    val alertDialog = AlertDialog.Builder(fragment.requireContext())
                        .setTitle("Camera, Storage & Audio permission")
                        .setMessage("Both camera, storage and audio permission are needed to create a video.")
                        .setPositiveButton("Accept") { _, _ ->
                            Dexter.withContext(fragment.requireContext())
                                .withPermissions(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO
                                )
                                .withListener(fragment.dialogMultiplePermissionsListener)
                                .withErrorListener { error ->
                                    Timber.e(error.toString())
                                }
                                .check()
                        }
                        .setNegativeButton("Cancel") { _, _ ->
                            Timber.d("it is $it and permission is denied")
                            fragment.longToast("Permissions denied.")
                            fragment.findNavController().popBackStack()
                        }
                        .create()
                    alertDialog.show()
                }
            }
        }
        override fun onPermissionRationaleShouldBeShown(
            permissions: MutableList<PermissionRequest>?,
            token: PermissionToken?
        ) {
            Timber.d("onPermissionRationaleShouldBeShown called.")
            token?.continuePermissionRequest()
        }
    }

    private fun doPermissionRequest(permissions: Array<String>) {
        val context = this.requireContext()
        if (ContextCompat.checkSelfPermission(
                context,
                permissions[0]
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                permissions[1]
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                permissions[2]
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                permissions[3]
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            turnOnCameraPreview()
        } else {
            Dexter.withContext(this.requireContext())
                .withPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
                )
                .withListener(dialogMultiplePermissionsListener)
                .withErrorListener {
                    Timber.e(it.toString())
                }
                .check()
        }
    }


}