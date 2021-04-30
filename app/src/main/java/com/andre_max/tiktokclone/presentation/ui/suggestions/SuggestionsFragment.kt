package com.andre_max.tiktokclone.presentation.ui.suggestions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentSuggestionsBinding
import com.andre_max.tiktokclone.presentation.ui.suggestions.group.SmallSuggestionGroup
import com.andre_max.tiktokclone.utils.KeyboardUtils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder

class SuggestionsFragment : Fragment(R.layout.fragment_suggestions) {

    private lateinit var binding: FragmentSuggestionsBinding

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private val viewModel by viewModels<SuggestionsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLayout()
        setUpLiveData()
        KeyboardUtils.show(binding.searchInput)
    }

    private fun setUpLayout() {
        binding = FragmentSuggestionsBinding.bind(requireView()).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.viewModel = viewModel
        }
        binding.suggestionsRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupAdapter
        }
    }

    private fun setUpLiveData() {
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