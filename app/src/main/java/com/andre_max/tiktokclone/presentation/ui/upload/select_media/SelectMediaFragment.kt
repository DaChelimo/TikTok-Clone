/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
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
        ViewUtils.changeSystemBars(activity, SystemBarColors.WHITE)
    }

    inner class MediaFragmentStateAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int) =
            EachMediaTab.getInstance(position, viewModel.localMediaRepo)
    }

}