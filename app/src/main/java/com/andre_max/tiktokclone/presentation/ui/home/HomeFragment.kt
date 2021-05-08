package com.andre_max.tiktokclone.presentation.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentHomeBinding
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.presentation.ui.home.large_video_group.LargeVideoGroup
import com.andre_max.tiktokclone.utils.BottomNavViewUtils
import com.andre_max.tiktokclone.utils.ViewUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import timber.log.Timber

class HomeFragment : BaseFragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding

    override val viewModel by viewModels<HomeViewModel>()
    private val snapHelper = PagerSnapHelper()
    private val groupAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    override fun setUpLayout() {
        binding = FragmentHomeBinding.bind(requireView())
    }

    override fun setUpLiveData() {
        viewModel.listOfRemoteVideo.observe(viewLifecycleOwner) { listOfRemoteVideo ->
            Timber.d("listOfRemoteVideo is List<RemoteVideo> is ${listOfRemoteVideo is List<RemoteVideo>}")
            val listOfGroup = listOfRemoteVideo?.map { remoteVideo ->
                val largeVideoGroup = LargeVideoGroup(
                    scope = lifecycleScope,
                    lifecycleOwner = viewLifecycleOwner,
                    userRepo = viewModel.userRepo,
                    commentRepo = viewModel.commentRepo,
                    videosRepo = viewModel.videosRepo,
                    remoteVideo = remoteVideo,
                    onPersonIconClicked = { uid ->
                        findNavController().navigate(
                            HomeFragmentDirections
                                .actionHomeFragmentToProfileWithAccountFragment(uid)
                        )
                    },
                    onVideoEnded = {
                        scrollDownToNextVideo(groupAdapter.getAdapterPosition(it))
                    }
                )

                largeVideoGroup
            } ?: listOf()

            groupAdapter.addAll(listOfGroup)
            Timber.d("groupAdapter.size is ${groupAdapter.itemCount}")
        }
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.also {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = groupAdapter
            it.addOnScrollListener(homeScrollListener)
            snapHelper.attachToRecyclerView(it)
        }
    }

    private fun scrollDownToNextVideo(currentPosition: Int) {
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        layoutManager.scrollToPosition(currentPosition + 1)
    }

    private val homeScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                val llm = (recyclerView.layoutManager as LinearLayoutManager)
                val lastVisiblePosition = llm.findLastVisibleItemPosition()

                // Checks if we are reaching towards the end of the list
                if (groupAdapter.itemCount - lastVisiblePosition <= 5) {
                    viewModel.fetchVideos()
                    Timber.d("groupAdapter.itemCount is ${groupAdapter.itemCount} and lastVisiblePosition is $lastVisiblePosition")
                }
            }

        }
    }

    override fun onStart() {
        super.onStart()
        Timber.d("Lifecycle Callbacks: onStart() called")
    }
    override fun onResume() {
        super.onResume()
        BottomNavViewUtils.showBottomNavBar(activity)
        ViewUtils.changeStatusBarIcons(requireActivity(), isWhite = true)
        ViewUtils.changeStatusBarColor(requireActivity(), android.R.color.transparent)
        ViewUtils.changeSystemNavigationBarColor(requireActivity(), android.R.color.transparent)
        Timber.d("Lifecycle Callbacks: onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("Lifecycle Callbacks: onPause() called")
    }
    override fun onStop() {
        super.onStop()
        Timber.d("Lifecycle Callbacks: onStop() called")
    }
    override fun onDestroy() {
        super.onDestroy()
        Timber.d("Lifecycle Callbacks: onDestroy() called")
    }
}