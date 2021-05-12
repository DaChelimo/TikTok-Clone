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

package com.andre_max.tiktokclone.presentation.ui.auth.email_auth.enter_password

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.models.sign_up.EmailBody
import com.andre_max.tiktokclone.repo.network.auth.AuthRepo
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import kotlinx.coroutines.launch

class EnterPasswordViewModel : BaseViewModel() {

    private val authRepo = AuthRepo()

    val passwordInput = MutableLiveData("")
    val livePasswordStatus = MutableLiveData<Int>()

    val isValid = Transformations.map(livePasswordStatus) { status ->
        status == R.string.valid_password
    }

    private var _navigate = MutableLiveData<Boolean>()
    val navigate: LiveData<Boolean> = _navigate

    private val passwordInputObserver = { password: String ->
        val hasDigit = password.any { it.isDigit() }
        val hasCharacter = password.any { it.isLetter() }

        livePasswordStatus.value = getErrorFromPassword(password, hasCharacter, hasDigit)
    }

    init {
        passwordInput.observeForever(passwordInputObserver)
    }

    /**
     * Verify that the password the user is creating during sign up is valid
     */
    fun verifyPassword() {
        val password = passwordInput.value ?: ""

        val hasDigit = password.any { it.isDigit() }
        val hasCharacter = password.any { it.isLetter() }

        if (password.length > 8 && hasCharacter && hasDigit) // Password meets our requirements
            _navigate.value = true
        else
            showMessage(R.string.invalid_password)
    }

    fun logInWithEmailBody(emailBody: EmailBody) {
        viewModelScope.launch {
            val authResult = authRepo.logInWithEmailBody(emailBody)

            if (authResult.tryData() != null) // Log in successful
                _navigate.value = true
            else
                showMessage(R.string.error_occurred_during_log_in)
        }
    }

    private fun getErrorFromPassword(
        password: String,
        hasCharacter: Boolean,
        hasDigit: Boolean
    ) = when {
        // Complete password
        password.length >= 8 && hasCharacter && hasDigit -> R.string.valid_password
        // Lacks digit
        hasCharacter && !hasDigit -> R.string.needs_one_digit
        // Lacks character
        !hasCharacter && hasDigit -> R.string.needs_one_character
        // Lacks both
        else -> R.string.needs_both_digit_and_character
    }

    fun resetLiveNavigate() {
        _navigate.value = false
    }

    override fun onCleared() {
        super.onCleared()
        passwordInput.removeObserver(passwordInputObserver)
    }
}