package com.andre_max.tiktokclone.presentation.ui.upload.select_media

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentSelectMediaBinding
import com.andre_max.tiktokclone.presentation.ui.upload.select_media.tab.EachMediaTab
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import com.andre_max.tiktokclone.utils.architecture.BaseViewModel
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber

class SelectMediaFragment : BaseFragment(R.layout.fragment_select_media) {

    private lateinit var binding: FragmentSelectMediaBinding

    override val viewModel by viewModels<SelectMediaViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            Timber.d("tab is $tab and tab position is $position")
            tab.text = getString(if (position == 0) R.string.images else R.string.videos)
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager, tabConfigurationStrategy).attach()
    }

    override fun setUpLayout() {
        binding = DataBindingUtil.bind(requireView())!!
        viewModel.initViewModel(requireContext())

        binding.viewPager.apply {
            adapter = MediaFragmentStateAdapter()
        }
    }

    override fun setUpClickListeners() {
        super.setUpClickListeners()
        binding.closeBtn.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onResume() {
        super.onResume()
        BottomNavViewUtils.hideBottomNavBar(activity)
    }

    inner class MediaFragmentStateAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int) =
            EachMediaTab.getInstance(position, viewModel.localMediaRepo)
    }

}