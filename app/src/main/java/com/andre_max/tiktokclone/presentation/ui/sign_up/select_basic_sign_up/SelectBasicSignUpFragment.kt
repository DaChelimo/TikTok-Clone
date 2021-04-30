package com.andre_max.tiktokclone.presentation.ui.sign_up.select_basic_sign_up

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentSelectBasicSignUpBinding
import com.andre_max.tiktokclone.presentation.ui.sign_up.email_sign_up.BasicEmailFragment
import com.andre_max.tiktokclone.presentation.ui.sign_up.phone_sign_up.BasicPhoneFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber

/**
 * This class is in charge of displaying a viewpager that allows the user to sign up with email or phone.
 * We navigate to this fragment when the user clicks on the Sign up with phone or email button.
 * As you may have noticed, the class name isn't that descriptive so kindly drop in a suggestion if you
 * have one and leave a PR
 */
class SelectBasicSignUpFragment : Fragment(R.layout.fragment_select_basic_sign_up) {

    lateinit var binding: FragmentSelectBasicSignUpBinding

    private val onPageChangedCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Timber.d("Callback position is $position")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectBasicSignUpBinding.bind(view)

        binding.basicViewpager.also {
            it.adapter = BasicFragmentStateAdapter(this)
            it.registerOnPageChangeCallback(onPageChangedCallback)
        }

        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position -> Timber.d("tab is $tab and tab position is $position") }
        TabLayoutMediator(binding.tabLayout, binding.basicViewpager, tabConfigurationStrategy).attach()
    }

    class BasicFragmentStateAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment =
            if (position == 0) BasicPhoneFragment() else BasicEmailFragment()
    }
}