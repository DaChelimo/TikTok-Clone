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

package com.andre_max.tiktokclone.presentation.ui.upload.preview_video

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentPreviewVideoBinding
import com.andre_max.tiktokclone.presentation.exoplayer.Player
import com.andre_max.tiktokclone.utils.BottomNavViewUtils.hideBottomNavBar
import com.andre_max.tiktokclone.utils.SystemBarColors
import com.andre_max.tiktokclone.utils.ViewUtils
import com.andre_max.tiktokclone.utils.architecture.BaseFragment


class PreviewVideoFragment : BaseFragment(R.layout.fragment_preview_video) {

    lateinit var binding: FragmentPreviewVideoBinding

    private val args by navArgs<PreviewVideoFragmentArgs>()
    private val localVideo by lazy { args.localVideo }
    private val player by lazy {
        Player(
            simpleExoplayerView = binding.playerView,
            playBtn = binding.playBtn,
            context = requireContext(),
            url = localVideo.filePath,
            onVideoEnded = {
                it.restartPlayer()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.addObserver(player)
        player.init()
    }

    override fun setUpLayout() {
        binding = FragmentPreviewVideoBinding.bind(requireView())
    }

    override fun setUpClickListeners() {
        binding.nextBtn.setOnClickListener {
            findNavController().navigate(
                PreviewVideoFragmentDirections
                    .actionPreviewVideoFragmentToPostVideoFragment(localVideo)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        player.resumePlayer()
        ViewUtils.changeSystemBars(activity, SystemBarColors.DARK)
        ViewUtils.changeSystemNavigationBarColor(requireActivity(), R.color.dark_black)
        ViewUtils.hideStatusBar(requireActivity())
        hideBottomNavBar(activity)
    }

    override fun onPause() {
        super.onPause()
        player.pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stopPlayer()
    }

}