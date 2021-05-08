package com.andre_max.tiktokclone.presentation.ui.discover

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentDiscoverBinding
import com.andre_max.tiktokclone.presentation.ui.discover.group.DiscoverGroup
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
        ViewUtils.changeStatusBarIcons(requireActivity(), isWhite = false)
        ViewUtils.changeStatusBarColor(requireActivity(), android.R.color.white)
        ViewUtils.changeSystemNavigationBarColor(requireActivity(), android.R.color.white)
    }
}