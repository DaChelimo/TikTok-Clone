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

package com.andre_max.tiktokclone.presentation.ui.auth.phone_auth.enter_phone_number

import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentBasicPhoneBinding
import com.andre_max.tiktokclone.presentation.ui.auth.select_basic_auth.SelectBasicAuthFragmentDirections
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import timber.log.Timber
import kotlin.properties.Delegates

class BasicPhoneFragment : BaseFragment(R.layout.fragment_basic_phone) {

    lateinit var binding: FragmentBasicPhoneBinding

    override val viewModel by viewModels<BasicPhoneViewModel>()

    var isLogin by Delegates.notNull<Boolean>()

    override fun setUpLayout() {
        binding = FragmentBasicPhoneBinding.bind(requireView()).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.viewModel = viewModel
        }
    }

    override fun setUpClickListeners() {
        binding.sendCodeBtn.setOnClickListener {
            viewModel.sendCode(
                binding.countryCodePicker.selectedCountryCodeWithPlus,
                requireActivity(),
                onComplete
            )
        }
    }

    override fun setUpLiveData() {
        viewModel.liveCredential.observe(viewLifecycleOwner) { liveCredential ->
            liveCredential?.let {
                findNavController().navigate(
                    SelectBasicAuthFragmentDirections
                        .actionSelectBasicSignUpFragmentToCreateUsernameFragment(
                            liveCredential,
                            null,
                            null
                        )
                )

                viewModel.resetLiveCredential()
            }
        }

        // Change the background tint in code because the textColor is automatically changed in the layout through DataBinding.
        viewModel.isValid.observe(viewLifecycleOwner) { isValid ->
            binding.sendCodeBtn.backgroundTintList = ResourcesCompat.getColorStateList(
                resources,
                if (isValid) R.color.pinkBtnBackground else R.color.grey_button_background,
                null
            )
        }
    }


    private val onComplete = { phoneNumber: String? ->
        phoneNumber?.let {
            try {
                findNavController().navigate(
                    SelectBasicAuthFragmentDirections
                        .actionSelectBasicSignUpFragmentToEnterCodeFragment(it, isLogin)
                )
            } catch (e: Exception) {
                Timber.e(e)
            }
        } ?: run {
            Timber.d("phoneNumber is null")
        }
    }

    companion object {
        fun getInstance(isLogin: Boolean) = BasicPhoneFragment().also {
            it.isLogin = isLogin
        }
    }
}