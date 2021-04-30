package com.andre_max.tiktokclone.presentation.ui.sign_up.create_username

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.FragmentCreateUsernameBinding
import com.andre_max.tiktokclone.models.sign_up.EmailBody
import com.andre_max.tiktokclone.utils.KeyboardUtils
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.launch

class CreateUsernameFragment : Fragment(R.layout.fragment_create_username) {

    private val viewModel by viewModels<CreateUsernameViewModel>()
    private val args by navArgs<CreateUsernameFragmentArgs>()

    private lateinit var binding: FragmentCreateUsernameBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setUp(args)
        setUpLayout()
        setUpLiveData()
    }

    private fun setUpLayout() {
        binding = FragmentCreateUsernameBinding.bind(requireView()).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.viewModel = viewModel
        }
    }

    private fun setOnClickListeners() {
        binding.signUpBtn.setOnClickListener {
            lifecycleScope.launch {
                KeyboardUtils.hide(binding.usernameInput)
                viewModel.completeSignIn()
            }
        }

        binding.skipBtn.setOnClickListener {
            lifecycleScope.launch {
                KeyboardUtils.hide(binding.usernameInput)
                viewModel.completeSignIn()
            }
        }
    }

    private fun setUpLiveData() {
        viewModel.liveUsername.observe(viewLifecycleOwner) { liveUsername ->
            liveUsername?.let { lifecycleScope.launch{viewModel.checkUsernameIsValid()} }
        }

        viewModel.errorTextRes.observe(viewLifecycleOwner) { errorTextRes ->
            errorTextRes?.let {
                binding.errorText.text = getString(errorTextRes)
            }
        }
    }

}