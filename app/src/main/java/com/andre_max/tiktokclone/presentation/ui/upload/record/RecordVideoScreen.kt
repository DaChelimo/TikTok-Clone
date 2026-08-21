/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.upload.record

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.utils.PermissionUtils
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Facing
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.gesture.Gesture
import com.otaliastudios.cameraview.gesture.GestureAction
import kotlinx.coroutines.launch

/**
 * The camera capture screen. The `CameraView` is a classic View driven through `AndroidView`,
 * bound to the composition's lifecycle owner so open/close/destroy track the screen. Pause
 * ends the current take (as the legacy screen's buttons effectively did — CameraView has no
 * true segment pause) and the finished take navigates to preview via the ViewModel.
 */
@Composable
fun RecordVideoScreen(
    state: RecordVideoUiState,
    startRecording: suspend (CameraView) -> Unit,
    onPause: (CameraView) -> Unit,
    onFinish: (CameraView) -> Unit,
    onResume: () -> Unit,
    onVideoTaken: () -> Unit,
    onDismissLittleSpace: () -> Unit,
    onClose: () -> Unit,
    onUploadClicked: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val context = LocalContext.current
    var permissionsGranted by remember {
        mutableStateOf(
            PermissionUtils.arePermissionsGranted(context, PermissionUtils.recordVideoPermissions),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (permissionsGranted) {
            CameraContent(
                state = state,
                startRecording = startRecording,
                onPause = onPause,
                onFinish = onFinish,
                onResume = onResume,
                onVideoTaken = onVideoTaken,
                onClose = onClose,
                onUploadClicked = onUploadClicked,
            )
        } else {
            PermissionsOverlay(onGranted = { permissionsGranted = true })
        }

        if (state.showLittleSpace) {
            LittleSpaceOverlay(onDismiss = onDismissLittleSpace)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun CameraContent(
    state: RecordVideoUiState,
    startRecording: suspend (CameraView) -> Unit,
    onPause: (CameraView) -> Unit,
    onFinish: (CameraView) -> Unit,
    onResume: () -> Unit,
    onVideoTaken: () -> Unit,
    onClose: () -> Unit,
    onUploadClicked: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraView = remember {
        CameraView(context).apply {
            facing = Facing.BACK
            audio = Audio.ON
            mode = Mode.VIDEO
            mapGesture(Gesture.PINCH, GestureAction.ZOOM)
            mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS)
            attachRecordingListener(onVideoTaken)
        }
    }

    DisposableEffect(cameraView, lifecycleOwner) {
        cameraView.setLifecycleOwner(lifecycleOwner)
        onDispose { cameraView.setLifecycleOwner(null) }
    }

    AndroidView(factory = { cameraView }, modifier = Modifier.fillMaxSize())

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onClose, modifier = Modifier.testTag(RecordVideoTags.CLOSE)) {
                Icon(
                    painter = painterResource(R.drawable.ic_round_close),
                    contentDescription = stringResource(R.string.close),
                    tint = Color.White,
                )
            }

            Row {
                var isFlashOn by remember { mutableStateOf(false) }
                IconButton(
                    onClick = {
                        cameraView.facing =
                            if (cameraView.facing == Facing.FRONT) Facing.BACK else Facing.FRONT
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_outline_flip_camera_ios_white),
                        contentDescription = stringResource(R.string.flip),
                        tint = Color.White,
                    )
                }
                IconButton(
                    onClick = {
                        isFlashOn = !isFlashOn
                        cameraView.flash = if (isFlashOn) Flash.ON else Flash.OFF
                    },
                ) {
                    Icon(
                        painter = painterResource(
                            if (isFlashOn) R.drawable.ic_round_flash_on
                            else R.drawable.ic_round_flash_off,
                        ),
                        contentDescription = stringResource(R.string.flash),
                        tint = Color.White,
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.isRecording) {
                IconButton(
                    onClick = { onPause(cameraView) },
                    modifier = Modifier
                        .size(72.dp)
                        .testTag(RecordVideoTags.PAUSE),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.stop_recording_image_pink),
                        contentDescription = stringResource(R.string.pause_recording),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(64.dp),
                    )
                }
                IconButton(
                    onClick = { onFinish(cameraView) },
                    modifier = Modifier.testTag(RecordVideoTags.FINISH),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = stringResource(R.string.finish_recording),
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        if (state.hasRecordingStarted) onResume()
                        else scope.launch { startRecording(cameraView) }
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .testTag(RecordVideoTags.RECORD),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.record_btn_drawable),
                        contentDescription = stringResource(R.string.start_recording),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(64.dp),
                    )
                }
                IconButton(
                    onClick = onUploadClicked,
                    modifier = Modifier.testTag(RecordVideoTags.UPLOAD),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_movie_filter),
                        contentDescription = stringResource(R.string.upload),
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionsOverlay(onGranted: () -> Unit) {
    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants -> if (grants.values.all { it }) onGranted() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.record_permissions_title),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.record_permissions_description),
            modifier = Modifier.padding(vertical = 12.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = {
                permissionsLauncher.launch(PermissionUtils.recordVideoPermissions.toTypedArray())
            },
            modifier = Modifier.testTag(RecordVideoTags.GRANT_PERMISSIONS),
        ) {
            Text(stringResource(R.string.grant_permissions))
        }
    }
}

@Composable
private fun LittleSpaceOverlay(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.little_space_available),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.little_space_description),
            modifier = Modifier.padding(vertical = 12.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onDismiss) {
            Text(stringResource(R.string.close))
        }
    }
}

/** Wires the ViewModel's session to the camera: descriptor in, finished take out. */
fun CameraView.attachRecordingListener(onVideoTaken: () -> Unit) {
    addCameraListener(object : CameraListener() {
        override fun onVideoTaken(result: VideoResult) {
            onVideoTaken()
        }
    })
}
