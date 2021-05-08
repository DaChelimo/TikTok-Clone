package com.andre_max.tiktokclone.presentation.ui.sign_up.create_password

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentCreatePasswordBinding
import com.andre_max.tiktokclone.models.sign_up.EmailBody
import com.andre_max.tiktokclone.utils.ResUtils

class CreatePasswordFragment : Fragment(R.layout.fragment_create_password) {

    lateinit var binding: FragmentCreatePasswordBinding

    private val args by navArgs<CreatePasswordFragmentArgs>()
    private val viewModel by viewModels<CreatePasswordViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLayout()
        setUpLiveData()

        binding.passwordInput.addTextChangedListener(viewModel.passwordTextWatcher)
    }

    private fun setUpLiveData() {
        viewModel.navigate.observe(viewLifecycleOwner) {
            if (it == true) {
                val emailBody =
                    EmailBody(args.email, viewModel.passwordInput.value ?: return@observe)
                findNavController().navigate(
                    CreatePasswordFragmentDirections
                        .actionCreatePasswordFragmentToCreateUsernameFragment(
                            null, null, emailBody
                        )
                )
                viewModel.resetLiveNavigate()
            }
        }

        viewModel.isValid.observe(viewLifecycleOwner) { isValid ->
            binding.liveStatusText.setTextColor(
                Color.parseColor(if (isValid == true) "#19FD00" else "#F43636")
            )
        }
    }

    private fun setUpLayout() {
        binding = FragmentCreatePasswordBinding.bind(requireView()).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.viewModel = viewModel
        }
    }
}