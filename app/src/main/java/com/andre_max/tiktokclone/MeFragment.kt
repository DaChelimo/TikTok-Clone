package com.andre_max.tiktokclone

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.andre_max.tiktokclone.databinding.FragmentMeBinding


class MeFragment : Fragment() {

    lateinit var binding: FragmentMeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_me, container, false)

        val fragmentContainer = binding.fragContainer

        fragmentContainer.

        return binding.root
    }

}