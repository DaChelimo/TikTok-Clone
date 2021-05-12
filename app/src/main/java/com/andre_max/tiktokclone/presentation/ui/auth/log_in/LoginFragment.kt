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

package com.andre_max.tiktokclone.presentation.ui.auth.log_in

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentLogInBinding
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import kotlinx.coroutines.launch

class LoginFragment : BaseFragment(R.layout.fragment_log_in) {

    private lateinit var binding: FragmentLogInBinding
    override val viewModel by viewModels<LogInViewModel>()

    // ActivityResultLauncher for Google Sign Up
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.googleAuthRepo.handleGoogleOnResult(result?.data)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BottomNavViewUtils.hideBottomNavBar(activity)

        binding.logInTwitterBtn.callback = viewModel.twitterCallback
    }

    override fun setUpLayout() {
        binding = FragmentLogInBinding.bind(requireView())
    }

    override fun setUpClickListeners() {
        binding.logInCancelBtn.setOnClickListener { findNavController().popBackStack() }
        binding.useSignUpBtn.setOnClickListener { findNavController().popBackStack() } // Return to SignUpFragment

        binding.logInUsePhoneBtn.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToSelectBasicSignUpFragment(isLogIn = true)
            )
        }
        binding.logInFacebookBtn.setOnClickListener {
            viewModel.facebookAuthRepo.doFacebookLogin(requireActivity())
        }
        binding.logInGoogleBtn.setOnClickListener {
            viewModel.googleAuthRepo.doGoogleAuth(requireContext(), launcher)
        }
        binding.logInTwitterBtn.setTheClickListener {
            lifecycleScope.launch { viewModel.twitterAuthRepo.doTwitterSignUp(requireActivity()) }
        }
    }

    override fun setUpLiveData() {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.logInTwitterBtn.onActivityResult(requestCode, resultCode, data)
        viewModel.facebookAuthRepo.handleFacebookOnResult(requestCode, resultCode, data)
    }
}