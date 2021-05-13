/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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