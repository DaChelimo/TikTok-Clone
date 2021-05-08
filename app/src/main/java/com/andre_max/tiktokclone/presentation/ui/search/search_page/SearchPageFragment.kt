package com.andre_max.tiktokclone.presentation.ui.search.search_page

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentSearchPageBinding
import com.andre_max.tiktokclone.presentation.ui.search.search_page.group.SmallSuggestionGroup
import com.andre_max.tiktokclone.utils.KeyboardUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class SearchPageFragment : BaseFragment(R.layout.fragment_search_page) {

    private lateinit var binding: FragmentSearchPageBinding

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    override val viewModel by viewModels<SearchPageViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        KeyboardUtils.show(binding.searchInput)
    }

    override fun setUpLayout() {
        binding = FragmentSearchPageBinding.bind(requireView()).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.viewModel = viewModel
        }
        binding.suggestionsRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupAdapter
        }
    }

    override fun setUpLiveData() {
        viewModel.liveSuggestions.observe(viewLifecycleOwner) { liveSuggestions ->
            groupAdapter.clear()

            liveSuggestions?.forEach { suggestion ->
                val smallSuggestionGroup = SmallSuggestionGroup(suggestion) { searchNameToInsert ->
                    binding.searchInput.setText(searchNameToInsert)
                }
                groupAdapter.add(smallSuggestionGroup)
            }
        }
    }
}