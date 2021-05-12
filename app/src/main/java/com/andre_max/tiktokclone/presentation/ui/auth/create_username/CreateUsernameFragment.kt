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

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navOptions
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.FragmentCreateUsernameBinding
import com.andre_max.tiktokclone.models.upload.Progress
import com.andre_max.tiktokclone.utils.KeyboardUtils
import com.andre_max.tiktokclone.utils.ResUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import kotlinx.coroutines.launch

class CreateUsernameFragment : BaseFragment(R.layout.fragment_create_username) {

    override val viewModel by viewModels<CreateUsernameViewModel>()
    private val args by navArgs<CreateUsernameFragmentArgs>()

    private lateinit var binding: FragmentCreateUsernameBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setUp(args)
    }

    override fun setUpLayout() {
        binding = FragmentCreateUsernameBinding.bind(requireView()).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.viewModel = viewModel
        }
    }

    override fun setUpClickListeners() {
        binding.signUpBtn.setOnClickListener {
            KeyboardUtils.hide(binding.usernameInput)
            viewModel.completeSignIn()
        }

        // Since the user hasn't actually given us a username, let's generate one
        binding.skipBtn.setOnClickListener {
            KeyboardUtils.hide(binding.usernameInput)
            lifecycleScope.launch { viewModel.generateRandomName() }
            viewModel.completeSignIn()
        }
    }

    override fun setUpLiveData() {
        viewModel.errorTextRes.observe(viewLifecycleOwner) { errorTextRes ->
            binding.liveStatusText.text = getString(errorTextRes ?: return@observe)
        }
        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            when (progress) {
                Progress.IDLE -> changeButtonStatus(makeActive = true)
                Progress.ACTIVE -> changeButtonStatus(makeActive = false)
                Progress.DONE -> navigateHome()
                Progress.FAILED -> { ResUtils.showSnackBar(requireView(), R.string.error_occurred_during_sign_up) }
                null -> throw Exception("The progress was null")
            }
        }
    }

    // Whether to make the button pink and active or grey and disabled
    private fun changeButtonStatus(makeActive: Boolean) {
        binding.signUpBtn.isClickable = makeActive
        binding.signUpBtn.setTextColor(
            if (makeActive) Color.WHITE
            else ResUtils.getResColor(requireContext(), R.color.light_grey)
        )
        binding.signUpBtn.backgroundTintList =
            ResourcesCompat.getColorStateList(
                resources,
                if(makeActive) R.color.pinkBtnBackground else R.color.light_grey,
                null
            )
    }

    // Navigate home and remove every fragment on the way since sign up is complete
    private fun navigateHome() {
        findNavController().navigate(
            CreateUsernameFragmentDirections
                .actionCreateUsernameFragmentToHomeFragment(),
            navOptions {
                popUpTo = R.id.homeFragment
            }
        )
    }
}