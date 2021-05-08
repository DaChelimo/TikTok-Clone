package com.andre_max.tiktokclone.presentation.ui.sign_up.email_sign_up

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentBasicEmailBinding
import com.andre_max.tiktokclone.presentation.ui.sign_up.select_basic_sign_up.SelectBasicSignUpFragmentDirections
import com.andre_max.tiktokclone.utils.architecture.BaseFragment

class BasicEmailFragment : BaseFragment(R.layout.fragment_basic_email) {

    private lateinit var binding: FragmentBasicEmailBinding
    override val viewModel by viewModels<BasicEmailViewModel>()

    override fun setUpLayout() {
        binding = FragmentBasicEmailBinding.bind(requireView()).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.viewModel = viewModel
        }
    }

    override fun setUpLiveData() {
        viewModel.shouldNavigate.observe(viewLifecycleOwner) { shouldNavigate ->
            val email = viewModel.liveEmail.value ?: return@observe
            if (shouldNavigate) {
                findNavController()
                    .navigate(
                        SelectBasicSignUpFragmentDirections
                            .actionSelectBasicSignUpFragmentToCreatePasswordFragment(email)
                    )
                viewModel.resetShouldNavigate()
            }
        }


        // Change the background tint in code because the textColor is automatically changed in the layout through DataBinding.
        viewModel.isValid.observe(viewLifecycleOwner) { isValid ->
            binding.signUpBtn.backgroundTintList = ResourcesCompat.getColorStateList(
                resources,
                if (isValid) R.color.pinkBtnBackground else R.color.grey_button_background,
                null
            )
        }
    }

    override fun setUpClickListeners() {
        binding.addGmailBtn.setOnClickListener { viewModel.appendEmailExtension(getString(R.string.gmail_com)) }
        binding.addHotmailBtn.setOnClickListener { viewModel.appendEmailExtension(getString(R.string.hotmail_com)) }
        binding.addOutlookBtn.setOnClickListener { viewModel.appendEmailExtension(getString(R.string.outlook_com)) }
    }
}