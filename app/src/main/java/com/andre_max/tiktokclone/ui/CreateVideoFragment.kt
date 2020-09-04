package com.andre_max.tiktokclone.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.andre_max.tiktokclone.POSITION
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentCreateVideoBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber


class CreateVideoFragment: Fragment() {

    lateinit var binding: FragmentCreateVideoBinding
    private lateinit var viewPager2PageChangeCallback: ViewPager2.OnPageChangeCallback
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_video, null, false)

        val mediaFragmentStateAdapter = MediaFragmentStateAdapter(this)
        viewPager2 = binding.eachMediaViewPager
        tabLayout = binding.tabLayout

        viewPager2.adapter = mediaFragmentStateAdapter
        viewPager2PageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Timber.d("Callback position is $position")
            }
        }

        viewPager2.registerOnPageChangeCallback(viewPager2PageChangeCallback)

        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position -> Timber.d("tab is $tab and tab position is $position") }

        TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy).attach()


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager2.unregisterOnPageChangeCallback(viewPager2PageChangeCallback)
    }


    class MediaFragmentStateAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            val eachMediaFragment = EachMediaFragment()
            Timber.d("position is $position")
            val bundle = Bundle().apply {
                this.putInt(POSITION, position)
            }
            eachMediaFragment.arguments = bundle

            return eachMediaFragment
        }
    }

}