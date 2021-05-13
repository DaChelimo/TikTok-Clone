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

package com.andre_max.tiktokclone.presentation.ui.auth.phone_auth.enter_code

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navOptions
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentEnterCodeBinding
import com.andre_max.tiktokclone.userVerificationId
import com.andre_max.tiktokclone.utils.KeyboardUtils
import com.andre_max.tiktokclone.utils.ResUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import timber.log.Timber

class EnterCodeFragment : BaseFragment(R.layout.fragment_enter_code) {

    private lateinit var binding: FragmentEnterCodeBinding

    private val args by navArgs<EnterCodeFragmentArgs>()
    override val viewModel by viewModels<EnterCodeViewModel>()

    override fun setUpLayout() {
        binding = FragmentEnterCodeBinding.bind(requireView())
    }

    override fun setUpClickListeners() {
        super.setUpClickListeners()
        binding.signUpBtn.setOnClickListener {
            val codeView = binding.inputCode

            if (codeView.code.length != codeView.codeLength) {
                ResUtils.showSnackBar(requireView(), R.string.code_invalid)
                codeView.clearCode()
            } else {
                KeyboardUtils.hide(requireView())
                getUserCredential(codeView.code)
            }
        }
    }

    override fun setUpLiveData() {
        super.setUpLiveData()
        viewModel.navigateToMyProfile.observe(viewLifecycleOwner) { navigateToMyProfile ->
            if (navigateToMyProfile == true) {
                findNavController().navigate(
                    R.id.meFragment,
                    null,
                    navOptions { popUpTo = R.id.homeFragment }
                )
                viewModel.resetNavigateToMyProfile()
            }
        }
    }

    private fun getUserCredential(code: String) {
        Timber.d("verificationId is $userVerificationId and code is $code")
        val credential =
            PhoneAuthProvider.getCredential(userVerificationId.toString(), code) as AuthCredential

        if (args.isLogIn)
            viewModel.logInWithCredential(credential)
        else
            navigateToCreateUsername(credential)

    }

    private fun navigateToCreateUsername(credential: AuthCredential) {
        findNavController()
            .navigate(
                EnterCodeFragmentDirections
                    .actionEnterCodeFragmentToCreateUsernameFragment(credential, null, null)
            )
    }

}