/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.domain.auth.AuthRepo
import com.andre_max.tiktokclone.domain.core.fold
import com.andre_max.tiktokclone.domain.user.UserRepo
import com.andre_max.tiktokclone.domain.user.usecase.GetVisibleVideoTypesUseCase
import com.andre_max.tiktokclone.models.video.VideoType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * A user profile. With a `uid` nav argument it shows that user; without one (the Me tab) it
 * shows the signed-in user — same ViewModel either way, since the only difference the screen
 * cares about is [ProfileUiState.isOwnProfile].
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepo,
    private val authRepo: AuthRepo,
    private val getVisibleVideoTypes: GetVisibleVideoTypesUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    init {
        val profileUid = savedStateHandle.get<String>(NAV_ARG_UID) ?: userRepo.currentUid()
        if (profileUid != null) load(profileUid)
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            is ProfileAction.VideoClicked ->
                _events.trySend(ProfileEvent.NavigateToVideo(action.video))

            ProfileAction.SettingsClicked ->
                _events.trySend(ProfileEvent.NavigateToSettings)

            ProfileAction.SignOutClicked ->
                _uiState.update { it.copy(showSignOutDialog = true) }

            ProfileAction.SignOutDismissed ->
                _uiState.update { it.copy(showSignOutDialog = false) }

            ProfileAction.SignOutConfirmed -> onSignOutConfirmed()
        }
    }

    private fun load(profileUid: String) {
        val isOwnProfile = profileUid == userRepo.currentUid()
        _uiState.update { it.copy(isOwnProfile = isOwnProfile) }

        viewModelScope.launch {
            val user = userRepo.getUserProfile(profileUid).fold({ it }, { null })

            val visibleTypes = getVisibleVideoTypes(
                isOwnProfile = isOwnProfile,
                showLikedVideos = user?.showLikedVideos == true,
            )
            _uiState.update { state ->
                state.copy(
                    user = user,
                    tabs = visibleTypes.map { ProfileTabUi(videoType = it) }.toImmutableList(),
                )
            }

            visibleTypes.forEach { videoType ->
                launch { loadTab(profileUid, videoType) }
            }
        }
    }

    private suspend fun loadTab(profileUid: String, videoType: VideoType) {
        val videos = userRepo.getUserVideos(profileUid, videoType).fold({ it }, { emptyList() })
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.videoType == videoType) {
                        tab.copy(videos = videos.toImmutableList(), isLoading = false)
                    } else {
                        tab
                    }
                }.toImmutableList(),
            )
        }
    }

    private fun onSignOutConfirmed() {
        _uiState.update { it.copy(showSignOutDialog = false) }
        authRepo.signOut()
        _events.trySend(ProfileEvent.SignedOut)
    }

    companion object {
        /** Must match the argument name in mobile_navigation.xml; absent on the Me tab. */
        const val NAV_ARG_UID = "uid"
    }
}
