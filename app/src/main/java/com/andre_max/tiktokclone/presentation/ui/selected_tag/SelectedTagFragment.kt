package com.andre_max.tiktokclone.presentation.ui.selected_tag

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentSelectedTagBinding


class SelectedTagFragment: Fragment(R.layout.fragment_selected_tag) {

    private lateinit var binding: FragmentSelectedTagBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLayout()
    }

    private fun setUpLayout() {
        binding = FragmentSelectedTagBinding.bind(requireView())
    }

}