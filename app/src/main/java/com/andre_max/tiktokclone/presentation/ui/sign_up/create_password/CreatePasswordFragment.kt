package com.andre_max.tiktokclone.presentation.ui.sign_up.create_password

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.EmailBody
import com.andre_max.tiktokclone.databinding.FragmentCreatePasswordBinding
import com.andre_max.tiktokclone.utils.BottomNavViewUtils


class CreatePasswordFragment : Fragment() {

    lateinit var binding: FragmentCreatePasswordBinding
    private val viewModel by viewModels<CreatePasswordViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCreatePasswordBinding.inflate(inflater).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.viewModel = viewModel
        }

        return binding.root
    }

    // Error red - #F43636
    // Valid green - #19FD00

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val email = CreatePasswordFragmentArgs.fromBundle(requireArguments()).email

        viewModel.snackBarMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                BottomNavViewUtils.showLongSnackBar(requireView(), it)
                viewModel.resetLiveSnackBarMessage()
            }
        }

        viewModel.navigate.observe(viewLifecycleOwner) {
            if (it == true) {
                val emailBody = EmailBody(email, viewModel.passwordInput.value ?: return@observe)
                findNavController().navigate(
                    CreatePasswordFragmentDirections
                        .actionCreatePasswordFragmentToCreateUsernameFragment(
                            null, null, null, emailBody
                        )
                )
                viewModel.resetLiveNavigate()
            }
        }

        binding.passwordInput.addTextChangedListener(viewModel.passwordTextWatcher)

        viewModel.errorText.observe(viewLifecycleOwner) {
            it?.also {
                binding.errorText.setTextColor(
                    Color.parseColor(
                        if (it == "Valid password") "#19FD00"
                        else "#F43636"
                    )
                )
            }
        }
    }
}