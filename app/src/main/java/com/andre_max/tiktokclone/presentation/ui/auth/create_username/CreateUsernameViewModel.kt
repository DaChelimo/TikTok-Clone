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

package com.andre_max.tiktokclone.presentation.ui.auth.create_username

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.models.upload.Progress
import com.andre_max.tiktokclone.repo.network.auth.AuthRepo
import com.andre_max.tiktokclone.repo.network.name.NameRepo
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.repo.network.name.DefaultNameRepo
import com.andre_max.tiktokclone.repo.network.user.DefaultUserRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateUsernameViewModel(
    private val nameRepo: NameRepo = DefaultNameRepo(),
    private val authRepo: AuthRepo = AuthRepo(),
    private val userRepo: UserRepo = DefaultUserRepo()
) : BaseViewModel(), NameRepo by nameRepo {

    lateinit var args: CreateUsernameFragmentArgs

    val liveUsername = MutableLiveData("")

    private val _errorTextRes = MutableLiveData<Int?>()
    val errorTextRes: LiveData<Int?> = _errorTextRes

    private val _progress = MutableLiveData(Progress.IDLE)
    val progress: LiveData<Progress> = _progress

    private val liveUsernameObserver: (String) -> Unit = {
        viewModelScope.launch { checkUsernameIsValid() }
    }

    init {
        liveUsername.observeForever(liveUsernameObserver)
    }

    fun setUp(navArgs: CreateUsernameFragmentArgs) {
        viewModelScope.launch {
            args = navArgs
            args.googleBody?.userName?.let { googleName ->
                getUsernameFromGoogleUsername(googleName)
            }
        }
    }

    fun completeSignIn() {
        _progress.value = Progress.ACTIVE
        viewModelScope.launch {
            // Username will not be null since we've already done our check. The safety check is because I dislike the non-null assertion mark
            val username = liveUsername.value ?: ""
            val authResult = getAuthResult()

            // To avoid if...else statements everywhere, I preferred using Uncle Bob's method of throwing exceptions
            try {
                val profilePicture = args.googleBody?.profilePicture
                val isSuccess = userRepo
                    .addUserToDatabase(username, authResult.forceData(), profilePicture)
                    .forceData()

                if (isSuccess) {
                    registerUserName(username)
                    _progress.value = Progress.DONE
                }
            } catch (e: Exception) {
                Timber.e(e, "Caught exception")
                showMessage(R.string.error_during_account_creation)
                _progress.value = Progress.FAILED
            }
        }
    }

    /**
     * Gets an authResult based on the type of sign up used. In all scenarios except using email sign up,
     * we have a credential while in email sign up, we have the email info we need.
     * We create the user at the last stage to prevent creating a void user
     * in case he/she leaves the app mid-way during the sign-up process.
     */
    private suspend fun getAuthResult() = when {
        args.credential != null -> {
            authRepo.signInWithCredential(args.credential!!)
        }
        args.emailBody != null -> {
            authRepo.signUpWithEmailBody(args.emailBody!!)
        }
        else -> throw UnknownError("There is no credential or emailBody. Call the 911, a bug has occurred(pun intended)")
    }

    suspend fun checkUsernameIsValid(): Boolean {
        val errorRes = getErrorFromUsername(liveUsername.value ?: return false)
        _errorTextRes.value = errorRes
        return errorRes == null
    }

    override fun onCleared() {
        super.onCleared()
        liveUsername.removeObserver(liveUsernameObserver)
    }
}