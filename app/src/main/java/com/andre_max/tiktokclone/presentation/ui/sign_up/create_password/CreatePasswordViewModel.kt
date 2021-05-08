package com.andre_max.tiktokclone.presentation.ui.sign_up.create_password

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel

class CreatePasswordViewModel : BaseViewModel() {

    val passwordInput = MutableLiveData("")
    val livePasswordStatus = MutableLiveData<Int>()

    val isValid = Transformations.map(livePasswordStatus) { status ->
        status == R.string.valid_password
    }

    private var _navigate = MutableLiveData<Boolean>()
    val navigate: LiveData<Boolean> = _navigate

    val passwordTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            var hasDigit = false
            var hasCharacter = false

            passwordInput.value.toString().toCharArray().forEach {
                if (it.isLetter()) hasCharacter = true
                else if (it.isDigit()) hasDigit = true
            }

            livePasswordStatus.value = when  {
                // Complete password
                passwordInput.value.toString().length >= 8 && hasCharacter && hasDigit -> R.string.valid_password
                // Lacks digit
                hasCharacter && !hasDigit -> R.string.needs_one_digit
                // Lacks character
                !hasCharacter && hasDigit -> R.string.needs_one_character
                // Lacks both
                else -> R.string.needs_both_digit_and_character
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

        if (passwordInput.value.toString().length < 8 || !hasCharacter || !hasDigit)
            showMessage(R.string.invalid_password)
        else
            _navigate.value = true
    }

    fun resetLiveNavigate() {
        _navigate.value = false
    }
}