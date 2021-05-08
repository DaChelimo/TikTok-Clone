package com.andre_max.tiktokclone.presentation.ui.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentInboxBinding
import com.andre_max.tiktokclone.utils.ViewUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment

class InboxFragment : BaseFragment(R.layout.fragment_inbox) {

    private lateinit var binding: FragmentInboxBinding

    override fun setUpLayout() {
        binding = FragmentInboxBinding.bind(requireView())
    }

    override fun onStart() {
        super.onStart()
        ViewUtils.changeStatusBarIcons(requireActivity(), isWhite = false)
        ViewUtils.changeStatusBarColor(requireActivity(), android.R.color.white)
        ViewUtils.changeSystemNavigationBarColor(requireActivity(), android.R.color.white)
    }
}