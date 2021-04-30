package com.andre_max.tiktokclone.presentation.ui.upload.select_media.tab

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.FragmentEachMediaBinding
import com.andre_max.tiktokclone.repo.local.media.LocalMediaRepo
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.user_image_layout.view.*
import kotlinx.android.synthetic.main.user_video_layout.view.*
import kotlin.properties.Delegates

/**
 * This fragment is a tab in [com.andre_max.tiktokclone.presentation.ui.upload.select_media.SelectMediaFragment] and can either be SelectImageTab or SelectVideoTab.
 */
class EachMediaTab : Fragment(R.layout.fragment_each_media) {

    private var tabPosition by Delegates.notNull<Int>()
    private lateinit var localMediaRepo: LocalMediaRepo
    private lateinit var binding: FragmentEachMediaBinding

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLayout()

        if (tabPosition == 0)
            awaitLocalImages()
        else
            awaitLocalVideos()
    }

    private fun setUpLayout() {
        binding.eachMediaRecyclerview.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = groupAdapter
        }
    }

    private fun awaitLocalImages() {
        localMediaRepo.listOfLocalImage.observe(viewLifecycleOwner) { listOfLocalImage ->
            listOfLocalImage?.forEach { localImage ->
                val localImageGroup = LocalImageGroup(localImage) {

                }
                groupAdapter.add(localImageGroup)
            }
        }
    }


    private fun awaitLocalVideos() {
        localMediaRepo.listOfLocalVideo.observe(viewLifecycleOwner) { listOfLocalVideo ->
            listOfLocalVideo?.forEach { localVideo ->
                val localVideoGroup = LocalVideoGroup(localVideo) {
                    findNavController()
                        .navigate(
                            SelectMediaFragmentDirections
                                .actionSelectMediaFragmentToPreviewVideoFragment(localVideo)
                        )
                }
                groupAdapter.add(localVideoGroup)
            }
        }
    }

    companion object {
        fun getInstance(tabPosition: Int, localMediaRepo: LocalMediaRepo) = EachMediaTab().also {
            it.tabPosition = tabPosition
            it.localMediaRepo = localMediaRepo
        }
    }
}
