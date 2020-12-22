package com.andre_max.tiktokclone.ui.sign_up

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentBasicSignUpBinding
import com.andre_max.tiktokclone.ui.sign_up.email_sign_up.BasicEmailFragment
import com.andre_max.tiktokclone.ui.sign_up.phone_sign_up.BasicPhoneFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber


class BasicSignUpFragment : Fragment() {

    lateinit var binding: FragmentBasicSignUpBinding
    private lateinit var viewPager2PageChangeCallback: ViewPager2.OnPageChangeCallback
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_basic_sign_up, container, false)

        val basicFragmentStateAdapter = BasicFragmentStateAdapter(this)
        viewPager2 = binding.basicViewpager
        tabLayout = binding.tabLayout

        viewPager2.adapter = basicFragmentStateAdapter
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

    class BasicFragmentStateAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {

            Timber.d("position is $position")

            return if (position == 0) BasicPhoneFragment() else BasicEmailFragment()
        }
    }
}