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

package com.andre_max.tiktokclone.presentation.ui.auth.sign_up

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.SignUpPageBinding
import com.andre_max.tiktokclone.models.sign_up.GoogleBody
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import com.google.firebase.auth.*
import com.twitter.sdk.android.core.*
import kotlinx.coroutines.launch

class SignUpFragment : BaseFragment(R.layout.sign_up_page) {

    lateinit var binding: SignUpPageBinding

    override val viewModel by viewModels<SignUpViewModel>()

    // ActivityResultLauncher for Google Sign Up
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.googleAuthRepo.handleGoogleOnResult(result?.data)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BottomNavViewUtils.hideBottomNavBar(activity)

        binding.signUpTwitterBtn.callback = viewModel.twitterCallback
    }

    override fun setUpLayout() {
        binding = SignUpPageBinding.bind(requireView())
    }

    override fun setUpClickListeners() {
        binding.signUpCancelBtn.setOnClickListener { findNavController().popBackStack() }
        binding.useLogInBtn.setOnClickListener { findNavController().navigate(R.id.action_signUpFragment_to_loginFragment) }

        binding.signUpUsePhoneBtn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_ageFragment)
        }
        binding.signUpFacebookBtn.setOnClickListener {
            viewModel.facebookAuthRepo.doFacebookLogin(requireActivity())
        }
        binding.signUpGoogleBtn.setOnClickListener {
            viewModel.googleAuthRepo.doGoogleAuth(requireContext(), launcher)
        }
        binding.signUpTwitterBtn.setTheClickListener {
            lifecycleScope.launch { viewModel.twitterAuthRepo.doTwitterSignUp(requireActivity()) }
        }
    }

    override fun setUpLiveData() {
        viewModel.liveCredential.observe(viewLifecycleOwner) { liveCredential ->
            if (liveCredential == null) return@observe

            // Since the body is used in providing a name and a profile picture, we should not be afraid if the values are null
            // since we are performing a null check in CreateUserNameViewModel
            val googleAccount = viewModel.googleAccount
            val googleBody =
                GoogleBody(googleAccount?.displayName, googleAccount?.photoUrl?.toString())

            findNavController().navigate(
                SignUpFragmentDirections.actionSignUpFragmentToCreateUsernameFragment(
                    liveCredential,
                    googleBody,
                    null
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.signUpTwitterBtn.onActivityResult(requestCode, resultCode, data)
        viewModel.facebookAuthRepo.handleFacebookOnResult(requestCode, resultCode, data)
    }
}