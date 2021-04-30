package com.andre_max.tiktokclone.presentation.ui.sign_up.phone_sign_up

import android.os.Bundle
import android.view.View
import androidx.core.text.trimmedLength
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentBasicPhoneBinding
import com.andre_max.tiktokclone.presentation.ui.sign_up.select_basic_sign_up.SelectBasicSignUpFragmentDirections
import com.andre_max.tiktokclone.utils.ResUtils
import timber.log.Timber

class BasicPhoneFragment : Fragment(R.layout.fragment_basic_phone) {

    lateinit var binding: FragmentBasicPhoneBinding

    private val viewModel by viewModels<BasicPhoneViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBasicPhoneBinding.bind(view)
        setUpLiveData()
        setUpClickListeners()
    }

    private val onComplete = { phoneNumber: String? ->
        phoneNumber?.let {
            try {
                findNavController().navigate(
                    SelectBasicSignUpFragmentDirections
                        .actionSelectBasicSignUpFragmentToEnterCodeFragment(it)
                )
            } catch (e: Exception) {
                Timber.e(e)
            }
        } ?: run {
            Timber.d("phoneNumber is null")
        }
    }

    private fun setUpClickListeners() {
        binding.sendCodeBtn.setOnClickListener {
            viewModel.sendCode(
                binding.countryCodePicker.selectedCountryCodeWithPlus,
                requireActivity(),
                onComplete
            )
        }
    }

    private fun setUpLiveData() {
        viewModel.liveCredential.observe(viewLifecycleOwner) { liveCredential ->
            liveCredential?.let {
                findNavController().navigate(
                    SelectBasicSignUpFragmentDirections
                        .actionSelectBasicSignUpFragmentToCreateUsernameFragment(
                            liveCredential,
                            null,
                            null
                        )
                )

                viewModel.resetLiveCredential()
            }
        }

        viewModel.livePhoneNumber.observe(viewLifecycleOwner) { livePhoneNumber ->
            val isValid = livePhoneNumber.trimmedLength() == 9
            val sendCodeBtn = binding.sendCodeBtn
            sendCodeBtn.setBackgroundColor(
                ResUtils.getResColor(
                    context = requireContext(),
                    colorRes = if (isValid) R.color.pinkBtnBackground else R.color.grey_button_background
                )
            )
            sendCodeBtn.setTextColor(
                ResUtils.getResColor(
                    context = requireContext(),
                    colorRes = if (isValid) android.R.color.white else R.color.dark_black
                )
            )
        }
    }
}