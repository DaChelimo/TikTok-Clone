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
import androidx.recyclerview.widget.LinearLayoutManager
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.DiscoverItemBinding
import com.andre_max.tiktokclone.models.tag.Tag
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.utils.NumbersUtils
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiscoverGroup(
    private val coroutineScope: CoroutineScope,
    private val tag: Tag,
    private val getVideoThumbnail: suspend (RemoteVideo) -> Bitmap?,
    private val fetchVideos: suspend () -> List<RemoteVideo>
) : BindableItem<DiscoverItemBinding>() {

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()

    override fun bind(binding: DiscoverItemBinding, position: Int) {
        binding.tagName.text = binding.root.context.getString(R.string.hash_tag_name, tag.name)
        binding.tagCount.text = NumbersUtils.formatCount(tag.count)

        binding.tagRecyclerview.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        coroutineScope.launch {
            val listOfSmallTagImage = fetchVideos().map { remoteVideo ->
                DiscoverSubGroup(coroutineScope) { getVideoThumbnail(remoteVideo) }
            }
            withContext(Dispatchers.Main) { groupAdapter.addAll(listOfSmallTagImage) }
        }
    }

    override fun initializeViewBinding(view: View) =
        DiscoverItemBinding.bind(view)

    override fun getLayout(): Int = R.layout.discover_item
}
