package com.andre_max.tiktokclone.presentation.ui.search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentSearchBinding
import com.andre_max.tiktokclone.presentation.ui.search.group.TagGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import timber.log.Timber

class SearchFragment : Fragment(R.layout.fragment_search) {

    private lateinit var binding: FragmentSearchBinding

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private val viewModel by viewModels<SearchViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)
        setUpLayout()
        setClickListeners()
        setUpLiveData()
    }

    private fun setUpLayout() {
        binding.searchRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }
    }

    private fun setUpLiveData() {
        viewModel.listOfPopularTags.observe(viewLifecycleOwner) { listOfPopularTags ->
            listOfPopularTags?.forEach { popularTag ->
                val tagGroup = TagGroup(
                    tag = popularTag,
                    getVideoThumbnail = { viewModel.getVideoThumbnail(requireContext(), it) },
                    fetchVideos = { viewModel.fetchVideos(popularTag) }
                )

                groupAdapter.add(tagGroup)
            }
        }
    }

    private fun setClickListeners() {
        binding.searchTextBackground.setOnClickListener {
            Timber.d("searchTextBackground.setOnClickListener called")
            findNavController().navigate(R.id.action_search_fragment_to_searchSuggestionsFragment)
        }
    }

}