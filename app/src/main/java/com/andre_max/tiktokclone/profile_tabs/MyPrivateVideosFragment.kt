package com.andre_max.tiktokclone.profile_tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentMyPrivateVideosBinding

class MyPrivateVideosFragment : Fragment() {

    lateinit var binding: FragmentMyPrivateVideosBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_private_videos, container, false)



        return binding.root
    }

}