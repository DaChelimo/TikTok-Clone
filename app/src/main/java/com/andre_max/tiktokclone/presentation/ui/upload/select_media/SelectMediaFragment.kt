package com.andre_max.tiktokclone.presentation.ui.upload.select_media

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentSelectMediaBinding
import com.andre_max.tiktokclone.presentation.ui.upload.select_media.tab.EachMediaTab
import com.andre_max.tiktokclone.repo.local.media.LocalMediaRepo
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber


class SelectMediaFragment : Fragment(R.layout.fragment_select_media) {

    private lateinit var binding: FragmentSelectMediaBinding

    private val localMediaRepo by lazy { LocalMediaRepo() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        BottomNavViewUtils.hideBottomNavBar(activity)
        setUpLayout()

        localMediaRepo.getAllImages(requireContext())
        localMediaRepo.getAllVideos(requireContext())

        binding.closeBtn.setOnClickListener { findNavController().popBackStack() }


        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            Timber.d("tab is $tab and tab position is $position")
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager, tabConfigurationStrategy).attach()
    }

    private fun setUpLayout() {
        binding.viewPager.apply {
            adapter = MediaFragmentStateAdapter()
        }
    }

    override fun onResume() {
        super.onResume()
        BottomNavViewUtils.hideBottomNavBar(activity)
    }

    inner class MediaFragmentStateAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int) =
            EachMediaTab.getInstance(position, localMediaRepo)
    }

}