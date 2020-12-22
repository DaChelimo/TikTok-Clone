package com.andre_max.tiktokclone.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.FragmentProfileWithAccountBinding
import com.andre_max.tiktokclone.profile_tabs.MyLikedVideosFragment
import com.andre_max.tiktokclone.profile_tabs.MyPrivateVideosFragment
import com.andre_max.tiktokclone.profile_tabs.MyPublicVideosFragment
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber


class ProfileWithAccountFragment : Fragment() {

    lateinit var binding: FragmentProfileWithAccountBinding
    private lateinit var viewPager2: ViewPager2
    private lateinit var viewPager2PageChangeCallback: ViewPager2.OnPageChangeCallback
    private lateinit var tabLayout: TabLayout
    private var user: User? = null
    var uid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_with_account, container, false)
        uid = ProfileWithAccountFragmentArgs.fromBundle(requireArguments()).uid

        val ref = firebaseDatabase.getReference(getUserBasicDataPath(uid ?: firebaseAuth.currentUser?.uid.toString()))
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)
                setUpUserData(user)

                Timber.d("user is $user")
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.message)
            }
        })


        val mediaFragmentStateAdapter = MyFragmentStateAdapter(this)
        viewPager2 = binding.viewpager
        tabLayout = binding.profileTabLayout

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

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).navView.visibility = View.VISIBLE
    }

    private fun setUpUserData(user: User?) {
        val userImage = binding.userPhoto
        val userTag = binding.userTag
        val followerCount = binding.followersCountNumber
        val followingCount = binding.followingCountNumber
        val totalLikesCount = binding.likesCountNumber

        followerCount.text = user?.followers.toString()
        followingCount.text = user?.following.toString()
        totalLikesCount.text = user?.totalLikes.toString()
        userTag.text = "@${user?.username}"

        if (user?.profilePictureUrl != null) {
            Glide.with(this)
                .load(user.profilePictureUrl)
                .into(userImage)
        }
    }

    class MyFragmentStateAdapter(val fragment: Fragment): FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when(position) {
                0 -> MyPublicVideosFragment.getInstance((fragment as ProfileWithAccountFragment).uid)
                1 -> MyPrivateVideosFragment()
                2 -> MyLikedVideosFragment()
                else -> throw ArrayIndexOutOfBoundsException(3)
            }
        }
    }

}