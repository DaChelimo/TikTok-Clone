package com.andre_max.tiktokclone.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.andre_max.tiktokclone.MainActivity
import com.andre_max.tiktokclone.POSITION
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentCreateVideoBinding
import com.andre_max.tiktokclone.longToast
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener
import timber.log.Timber


class UploadMediaFragment: Fragment() {

    val PERMISSIONS_REQUEST_CODE = 1234

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
        (activity as MainActivity).navView.visibility = View.INVISIBLE

        viewPager2 = binding.eachMediaViewPager
        tabLayout = binding.tabLayout

        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        setUpStateAdapter()

        viewPager2PageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Timber.d("Callback position is $position")
            }
        }


        return binding.root
    }



    private fun setUpStateAdapter() {
        viewPager2.visibility = View.VISIBLE
        Timber.d("setUpStateAdapter() called")
        binding.permissionsLayout.visibility = View.GONE
        val mediaFragmentStateAdapter = MediaFragmentStateAdapter(this)
        viewPager2.adapter = mediaFragmentStateAdapter

        viewPager2.registerOnPageChangeCallback(viewPager2PageChangeCallback)

        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position -> Timber.d(
            "tab is $tab and tab position is $position"
        ) }

        TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy).attach()
    }



    override fun onResume() {
        super.onResume()
        (activity as MainActivity).navView.visibility = View.INVISIBLE
        viewPager2.registerOnPageChangeCallback(viewPager2PageChangeCallback)
    }

    override fun onStop() {
        super.onStop()
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