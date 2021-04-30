package com.andre_max.tiktokclone.presentation.ui.upload.select_media.tab

import android.view.View
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.LocalImageGroupLayoutBinding
import com.andre_max.tiktokclone.models.local.LocalImage
import com.bumptech.glide.Glide
import com.xwray.groupie.viewbinding.BindableItem

class LocalImageGroup(
    private val localImage: LocalImage,
    private val onClickListener: () -> Unit
) : BindableItem<LocalImageGroupLayoutBinding>() {

    override fun bind(binding: LocalImageGroupLayoutBinding, position: Int) {
        binding.root.setOnClickListener { onClickListener() }
        Glide.with(binding.localImage).load(localImage.url).into(binding.localImage)
    }

    override fun getLayout(): Int = R.layout.local_image_group_layout
    override fun initializeViewBinding(view: View) =
        LocalImageGroupLayoutBinding.bind(view)
}