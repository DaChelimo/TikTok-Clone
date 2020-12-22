package com.andre_max.tiktokclone.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andre_max.tiktokclone.databinding.FragmentHomeBinding
import com.andre_max.tiktokclone.exoplayer.Player
import com.andre_max.tiktokclone.ui.home.each_remote_video_group.EachRemoteVideoGroup
import com.andre_max.tiktokclone.utils.ViewUtils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import timber.log.Timber

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private var currentPlayer: Player? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.also {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = groupAdapter
            it.addOnScrollListener(homeScrollListener)
        }

        viewModel.listOfRemoteVideo.observe(viewLifecycleOwner) {
            it?.forEach { loopRemoteVideo ->
                val eachRemoteVideoGroup = EachRemoteVideoGroup(loopRemoteVideo) { player ->
                    currentPlayer = player
                }
                groupAdapter.add(eachRemoteVideoGroup)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ViewUtils.changeBottomNavView(activity, Color.TRANSPARENT, true)
    }

    override fun onPause() {
        super.onPause()
        currentPlayer?.pausePlayer()
    }

    override fun onStop() {
        super.onStop()
        currentPlayer?.stopPlayer()

        ViewUtils.changeBottomNavView(activity, Color.WHITE, false)
    }

    private val homeScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                val llm = (recyclerView.layoutManager as LinearLayoutManager)
                val firstVisiblePosition = llm.findFirstVisibleItemPosition()
                val lastVisiblePosition = llm.findLastVisibleItemPosition()
                Timber.d("firstVisiblePosition is $firstVisiblePosition")
                Timber.d("lastVisiblePosition is $lastVisiblePosition")

                checkLastVisiblePositionAndGetMoreVideos(lastVisiblePosition)
            }

        }

        fun checkLastVisiblePositionAndGetMoreVideos(lastVisiblePosition: Int) {
            if (groupAdapter.itemCount - lastVisiblePosition <= 5) {
                viewModel.getAllVideos()
                Timber.d("groupAdapter.itemCount is ${groupAdapter.itemCount} and lastVisiblePosition is $lastVisiblePosition")
            }
        }
    }
}