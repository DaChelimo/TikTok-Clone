package com.andre_max.tiktokclone.presentation.ui.sign_up.sign_up

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.SignUpPageBinding
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import com.andre_max.tiktokclone.utils.ResUtils
import com.google.firebase.auth.*
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import kotlinx.coroutines.launch

class SignUpFragment : Fragment(R.layout.sign_up_page) {

    lateinit var binding: SignUpPageBinding
    lateinit var mTwitterBtn: TwitterLoginButton

    private val viewModel by viewModels<SignUpViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BottomNavViewUtils.hideBottomNavBar(activity)
        setUpLiveData()
        binding.signUpTwitterBtn.callback = viewModel.twitterCallback



        binding.signUpCancelBtn.setOnClickListener { findNavController().popBackStack() }
        binding.signUpUsePhoneBtn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_ageFragment)
        }
        binding.signUpFacebookBtn.setOnClickListener {
            viewModel.facebookAuthRepo.doFacebookLogin(requireActivity())
        }
        binding.signUpGoogleBtn.setOnClickListener {
            viewModel.googleAuthRepo.doGoogleSignUp(requireContext())
        }
        binding.signUpTwitterBtn.setOnClickListener {
            lifecycleScope.launch { viewModel.twitterAuthRepo.doTwitterSignUp(requireActivity()) }
        }
    }

    private fun setUpLiveData() {
        viewModel.snackBarMessageRes.observe(viewLifecycleOwner) { resId ->
            if (resId == null) return@observe
            ResUtils.showSnackBar(requireView(), getString(resId))
            viewModel.clearMessage()
        }

        viewModel.liveCredential.observe(viewLifecycleOwner) { liveCredential ->
            if (liveCredential == null) return@observe

            findNavController().navigate(
                SignUpFragmentDirections.actionSignUpFragmentToCreateUsernameFragment(
                    liveCredential,
                    viewModel.googleAccount?.displayName,
                    viewModel.googleAccount?.photoUrl?.toString()
                )
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.signUpTwitterBtn.onActivityResult(requestCode, resultCode, data)
        viewModel.googleAuthRepo.handleGoogleOnResult(data)
        viewModel.facebookAuthRepo.handleFacebookOnResult(requestCode, resultCode, data)
    }

}