package com.andre_max.tiktokclone.presentation.ui.profile.with_account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.models.user.User
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.utils.viewModel.BaseViewModel
import kotlinx.coroutines.launch

class ProfileWithAccountViewModel: BaseViewModel() {
    private val userRepo = UserRepo()

    private val _profileUser = MutableLiveData<User>()
    val profileUser: LiveData<User> = _profileUser

    fun fetchUser(profileUid: String) {
        viewModelScope.launch {
            val user = userRepo.getUserProfile(profileUid).getData() ?: return@launch
            _profileUser.value = user
        }
    }

}