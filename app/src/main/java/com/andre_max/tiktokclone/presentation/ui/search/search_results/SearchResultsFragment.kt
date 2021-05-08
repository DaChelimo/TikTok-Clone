package com.andre_max.tiktokclone.presentation.ui.search.search_results

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentSearchResultsBinding

//TODO: Handle this later when you've viewed the real app.
class SearchResultsFragment : Fragment(R.layout.fragment_search_results) {

    private lateinit var binding: FragmentSearchResultsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchResultsBinding.bind(view)


    }

}