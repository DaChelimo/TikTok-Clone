package com.andre_max.tiktokclone.presentation.ui.sign_up.sign_up

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

    // ActivityResultContracts for Google Sign Up
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
        binding.signUpUsePhoneBtn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_ageFragment)
        }
        binding.signUpFacebookBtn.setOnClickListener {
            viewModel.facebookAuthRepo.doFacebookLogin(requireActivity())
        }
        binding.signUpGoogleBtn.setOnClickListener {
            viewModel.googleAuthRepo.doGoogleSignUp(requireContext(), launcher)
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