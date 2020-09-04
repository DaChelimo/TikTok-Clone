package com.andre_max.tiktokclone.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.andre_max.tiktokclone.MainActivity
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentHomeBinding
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        (activity as MainActivity).navView.visibility = View.VISIBLE
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        val recyclerView = binding.recyclerView

        recyclerView.adapter = adapter

        return binding.root
    }

}