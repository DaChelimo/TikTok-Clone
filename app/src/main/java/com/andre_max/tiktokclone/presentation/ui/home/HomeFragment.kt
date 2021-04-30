package com.andre_max.tiktokclone.presentation.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentHomeBinding
import com.andre_max.tiktokclone.presentation.exoplayer.Player
import com.andre_max.tiktokclone.presentation.ui.home.each_remote_video_group.LargeVideoGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import timber.log.Timber

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var recyclerView: RecyclerView
    private var currentPlayer: Player? = null

    private val viewModel: HomeViewModel by viewModels()
    private val snapHelper = PagerSnapHelper()
    private val groupAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(HomeLifecycleCallback(requireActivity(), currentPlayer))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView(FragmentHomeBinding.bind(view))
        setLiveData()
    }

    private fun setLiveData() {
        viewModel.listOfRemoteVideo.observe(viewLifecycleOwner) { listOfRemoteVideo ->
            listOfRemoteVideo?.forEach { remoteVideo ->
                val eachRemoteVideoGroup = LargeVideoGroup(
                    userRepo = viewModel.userRepo,
                    remoteVideo = remoteVideo,
                    onClick = { player ->
                        currentPlayer = player
                    },
                    onVideoEnded = {
                        scrollDownToNextVideo(groupAdapter.getAdapterPosition(it))
                    }
                )

                groupAdapter.add(eachRemoteVideoGroup)
            }
        }
    }

    private fun setUpRecyclerView(binding: FragmentHomeBinding) {
        recyclerView = binding.recyclerView.also {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = groupAdapter
            it.addOnScrollListener(homeScrollListener)
            snapHelper.attachToRecyclerView(it)
        }
    }

    private fun scrollDownToNextVideo(currentPosition: Int) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
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
}