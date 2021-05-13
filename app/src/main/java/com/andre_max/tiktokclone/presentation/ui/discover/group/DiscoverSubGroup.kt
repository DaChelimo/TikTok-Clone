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

package com.andre_max.tiktokclone.presentation.ui.discover.group

import android.graphics.Bitmap
import android.view.View
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.DiscoverSubItemBinding
import com.bumptech.glide.Glide
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiscoverSubGroup(
    private val scope: CoroutineScope,
    private val getVideoThumbnail: suspend () -> Bitmap?
): BindableItem<DiscoverSubItemBinding>() {
    override fun bind(binding: DiscoverSubItemBinding, position: Int) {
        scope.launch {
            val videoThumbnail = getVideoThumbnail()

            withContext(Dispatchers.Main) {
                binding.loadingSpinner.visibility = View.GONE
                Glide.with(binding.root).load(videoThumbnail).into(binding.videoThumbnail)
            }
        }
    }

    override fun getLayout() = R.layout.discover_sub_item
    override fun initializeViewBinding(view: View) =
        DiscoverSubItemBinding.bind(view)
}