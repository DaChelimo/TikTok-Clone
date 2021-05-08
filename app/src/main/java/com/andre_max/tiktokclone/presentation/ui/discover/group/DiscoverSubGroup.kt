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