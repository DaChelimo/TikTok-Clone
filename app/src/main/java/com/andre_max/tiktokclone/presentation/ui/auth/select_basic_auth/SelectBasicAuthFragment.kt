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

package com.andre_max.tiktokclone.presentation.ui.auth.select_basic_auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentSelectBasicSignUpBinding
import com.andre_max.tiktokclone.presentation.ui.auth.email_auth.enter_email.BasicEmailFragment
import com.andre_max.tiktokclone.presentation.ui.auth.phone_auth.enter_phone_number.BasicPhoneFragment
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber

/**
 * This class is in charge of displaying a viewpager that allows the user to sign up with email or phone.
 * We navigate to this fragment when the user clicks on the Sign up with phone or email button.
 * As you may have noticed, the class name isn't that descriptive so kindly drop in a suggestion if you
 * have one and leave a PR
 */
class SelectBasicAuthFragment : Fragment(R.layout.fragment_select_basic_sign_up) {

    lateinit var binding: FragmentSelectBasicSignUpBinding

    private val args by navArgs<SelectBasicAuthFragmentArgs>()

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

        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            Timber.d("tab is $tab and tab position is $position")
            tab.text = getString(if (position == 0) R.string.phone else R.string.email)
        }
        TabLayoutMediator(binding.tabLayout, binding.basicViewpager, tabConfigurationStrategy).attach()
    }

    inner class BasicFragmentStateAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment =
            if (position == 0) BasicPhoneFragment.getInstance(args.isLogIn)
            else BasicEmailFragment.getInstance(args.isLogIn)
    }
}