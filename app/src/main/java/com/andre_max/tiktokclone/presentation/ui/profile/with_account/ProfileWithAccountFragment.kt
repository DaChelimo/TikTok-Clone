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

package com.andre_max.tiktokclone.presentation.ui.profile.with_account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.FragmentProfileWithAccountBinding
import com.andre_max.tiktokclone.models.video.VideoType
import com.andre_max.tiktokclone.presentation.ui.profile.with_account.tab.ProfileVideoTab
import com.andre_max.tiktokclone.utils.BottomNavViewUtils.showBottomNavBar
import com.andre_max.tiktokclone.utils.ImageUtils.loadGlideImage
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class ProfileWithAccountFragment : BaseFragment(R.layout.fragment_profile_with_account) {

    private lateinit var binding: FragmentProfileWithAccountBinding

    private val args by navArgs<ProfileWithAccountFragmentArgs>()
    override val viewModel by viewModels<ProfileWithAccountViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchUser(args.uid)

        val tabConfigurationStrategy =
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                Timber.d("tab is $tab and tab position is $position")
                tab.text = getString(
                    when (position) {
                        0 -> R.string.my_videos
                        1 -> R.string.my_private_videos
                        2 -> R.string.my_liked_videos
                        else -> throw IndexOutOfBoundsException("position was $position")
                    }
                )
            }
        TabLayoutMediator(binding.profileTabLayout, binding.viewpager, tabConfigurationStrategy)
            .attach()
    }

    private val onPageChangedCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Timber.d("Callback position is $position")
        }
    }


    override fun setUpLayout() {
        binding = FragmentProfileWithAccountBinding.bind(requireView())
        binding.viewpager.apply {
            adapter = MyFragmentStateAdapter(this@ProfileWithAccountFragment)
            registerOnPageChangeCallback(onPageChangedCallback)
        }
    }

    override fun setUpLiveData() {
        viewModel.profileUser.observe(viewLifecycleOwner) { profileUser ->
            profileUser?.let {
                binding.followersCountNumber.text = profileUser.followers.toString()
                binding.followingCountNumber.text = profileUser.following.toString()
                binding.likesCountNumber.text = profileUser.totalLikes.toString()
                binding.userTag.text = getString(R.string.author_username, profileUser.username)

                // Since the user can chose to stay without a profile picture, lets use the person icon
                // as a default.
                if (profileUser.profilePictureUrl == null)
                    loadGlideImage(binding.userPhoto, R.drawable.white_person_icon)
                else
                    loadGlideImage(binding.userPhoto, profileUser.profilePictureUrl)
            }
        }
    }

    override fun setUpClickListeners() {
        super.setUpClickListeners()
        if (args.uid == Firebase.auth.uid) {
            binding.editProfileBtn.setOnClickListener {
                // TODO: Navigate to EditeProfileFragment
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ViewUtils.changeSystemBars(activity, SystemBarColors.WHITE)
    }

    override fun onResume() {
        super.onResume()
        showBottomNavBar(activity)
    }


    class MyFragmentStateAdapter(
        private val profileWithAccountFragment: ProfileWithAccountFragment
    ) : FragmentStateAdapter(profileWithAccountFragment) {

        /*
        If the profile Uid represents the current user, show him everything
        If the profile user wants to show their liked videos, show the current user the first two: public and liked videos
        Otherwise, show only the public videos
         */
        override fun getItemCount(): Int = when {
            profileWithAccountFragment.args.uid == Firebase.auth.uid -> 3
            profileWithAccountFragment.viewModel.profileUser.value?.showLikedVideos == true -> 2
            else -> 1
        }

        override fun createFragment(position: Int): Fragment {
            val uid = profileWithAccountFragment.args.uid
            return when (position) {
                0 -> ProfileVideoTab.getInstance(uid, VideoType.PUBLIC)
                1 -> ProfileVideoTab.getInstance(uid, VideoType.PRIVATE)
                2 -> ProfileVideoTab.getInstance(uid, VideoType.LIKED)
                else -> throw ArrayIndexOutOfBoundsException(position)
            }
        }
    }
}