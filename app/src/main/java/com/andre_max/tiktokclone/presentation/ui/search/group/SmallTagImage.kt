package com.andre_max.tiktokclone.presentation.ui.search.group

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.SmallTagImageBinding
import com.andre_max.tiktokclone.utils.runAsync
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmallTagImage(private val getVideoThumbnail: suspend () -> Bitmap?): BindableItem<SmallTagImageBinding>() {
    override fun bind(binding: SmallTagImageBinding, position: Int) {
        runAsync {
            val videoThumbnail = getVideoThumbnail()

            withContext(Dispatchers.Main) {
                binding.loadingSpinner.visibility = View.GONE
                Glide.with(binding.root).load(videoThumbnail)
                    .into(binding.smallTagImage)
            }
        }
    }

    override fun getLayout() = R.layout.small_tag_layout
    override fun initializeViewBinding(view: View) =
        SmallTagImageBinding.bind(view)
}