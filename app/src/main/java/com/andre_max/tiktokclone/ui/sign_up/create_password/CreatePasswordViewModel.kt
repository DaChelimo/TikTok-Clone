package com.andre_max.tiktokclone.ui.sign_up.create_password

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreatePasswordViewModel : ViewModel() {

    val passwordInput = MutableLiveData("")
    val errorText = MutableLiveData("")

    private var _navigate = MutableLiveData<Boolean>()
    val navigate: LiveData<Boolean> = _navigate

    private var _snackBarMessage = MutableLiveData<String>()
    val snackBarMessage: LiveData<String> = _snackBarMessage

    val passwordTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            var hasDigit = false
            var hasCharacter = false

            passwordInput.value.toString().toCharArray().forEach {
                if (it.isLetter()) hasCharacter = true
                else if (it.isDigit()) hasDigit = true
            }

            if (passwordInput.value.toString().length >= 8 && hasCharacter && hasDigit) {
                errorText.value = "Valid password"
            } else {
                errorText.value = if (hasCharacter && !hasDigit) {
                    "Password must contain at least one digit"
                } else if (!hasCharacter && hasDigit) {
                    "Password must contain at least one character"
                } else {
                    "Password must contain at least one character and one digit"
                }
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    fun verifyPassword() {
        var hasDigit = false
        var hasCharacter = false

        passwordInput.value.toString().toCharArray().forEach {
            if (it.isLetter()) hasCharacter = true
            else if (it.isDigit()) hasDigit = true
        }

        if (passwordInput.value.toString().length < 8 || !hasCharacter || !hasDigit) {
            _snackBarMessage.value = "Invalid password"
            return
        }
    }

    fun resetLiveNavigate() {
        _navigate.value = false
    }

    fun resetLiveSnackBarMessage() {
        _snackBarMessage.value = ""
    }

}