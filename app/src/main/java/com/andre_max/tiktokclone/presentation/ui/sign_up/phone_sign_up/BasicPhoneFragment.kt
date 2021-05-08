package com.andre_max.tiktokclone.presentation.ui.sign_up.phone_sign_up

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentBasicPhoneBinding
import com.andre_max.tiktokclone.presentation.ui.sign_up.select_basic_sign_up.SelectBasicSignUpFragmentDirections
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import timber.log.Timber

class BasicPhoneFragment : BaseFragment(R.layout.fragment_basic_phone) {

    lateinit var binding: FragmentBasicPhoneBinding

    override val viewModel by viewModels<BasicPhoneViewModel>()

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
}