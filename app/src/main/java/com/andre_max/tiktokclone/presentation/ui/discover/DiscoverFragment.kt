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

package com.andre_max.tiktokclone.presentation.ui.discover

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentDiscoverBinding
import com.andre_max.tiktokclone.presentation.ui.discover.group.DiscoverGroup
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import timber.log.Timber

class DiscoverFragment : BaseFragment(R.layout.fragment_discover) {

    private lateinit var binding: FragmentDiscoverBinding

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    override val viewModel by viewModels<DiscoverViewModel>()

    override fun setUpLayout() {
        binding = FragmentDiscoverBinding.bind(requireView())

        binding.searchRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupAdapter
        }
    }

    override fun setUpLiveData() {
        viewModel.listOfPopularTags.observe(viewLifecycleOwner) { listOfPopularTags ->
            listOfPopularTags?.forEach { popularTag ->
                val tagGroup = DiscoverGroup(
                    coroutineScope = lifecycleScope,
                    tag = popularTag,
                    getVideoThumbnail = { viewModel.getVideoThumbnail(requireContext(), it) },
                    fetchVideos = { viewModel.fetchVideos(popularTag) }
                )

                groupAdapter.add(tagGroup)
            }
        }
    }

    override fun setUpClickListeners() {
        binding.searchInput.setOnClickListener {
            Timber.d("searchInput.setOnClickListener called")
            findNavController().navigate(
                DiscoverFragmentDirections.actionDiscoverFragmentToSearchPageFragment()
            )
        }
    }

    override fun onStart() {
        super.onStart()
        ViewUtils.changeSystemBars(activity, SystemBarColors.WHITE)
    }
}