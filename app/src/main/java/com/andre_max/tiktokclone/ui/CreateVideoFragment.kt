package com.andre_max.tiktokclone.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber


class CreateVideoFragment: Fragment() {

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
        
        viewPager2PageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Timber.d("Callback position is $position")
            }
        }

        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )

        doPermissionRequest(permissions)

        binding.grantPermissionsBtn.setOnClickListener {
            doPermissionRequest(permissions)
        }

        return binding.root
    }

    private fun doPermissionRequest(permissions: Array<String>) {
        val context = this.requireContext()
        if (ContextCompat.checkSelfPermission(
                context,
                permissions[0]
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                permissions[1]
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                permissions[2]
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                permissions[3]
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setUpStateAdapter()
        } else {
            permissions.forEach {
                if (shouldShowRequestPermissionRationale(it)) {
                    Snackbar.make(this.requireView(), "Permission is needed to post a video.", Snackbar.LENGTH_LONG).show()
                    return
                }
            }
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            var result = 0
            if (grantResults.isNotEmpty()) {
                grantResults.forEach {
                    result += it
                }
            }

            if (result == PackageManager.PERMISSION_GRANTED) {
                setUpStateAdapter()
                Snackbar.make(this.requireView(), "Permissions granted.", Snackbar.LENGTH_SHORT).show()
            }
            else {
                binding.permissionsLayout.visibility = View.VISIBLE
                Snackbar.make(this.requireView(), "Permissions denied.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setUpStateAdapter() {
        viewPager2.visibility = View.VISIBLE
        binding.permissionsLayout.visibility = View.GONE
        val mediaFragmentStateAdapter = MediaFragmentStateAdapter(this)
        viewPager2.adapter = mediaFragmentStateAdapter

        viewPager2.registerOnPageChangeCallback(viewPager2PageChangeCallback)

        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab, position -> Timber.d("tab is $tab and tab position is $position") }

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