package com.andre_max.tiktokclone.presentation.ui.profile.without_account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentProfileWithoutAccountBinding
import com.andre_max.tiktokclone.utils.ViewUtils
import timber.log.Timber


class ProfileWithoutAccountFragment : Fragment(R.layout.fragment_profile_without_account) {

    lateinit var binding: FragmentProfileWithoutAccountBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileWithoutAccountBinding.bind(requireView())
        binding.signUpBtn.setOnClickListener { findNavController().navigate(R.id.action_meFragment_to_signUpFragment) }
    }

    override fun onResume() {
        super.onResume()
        ViewUtils.changeStatusBarIcons(requireActivity(), isWhite = false)
        ViewUtils.changeStatusBarColor(requireActivity(), android.R.color.white)
        ViewUtils.changeSystemNavigationBarColor(requireActivity(), android.R.color.white)
    }
}