package com.andre_max.tiktokclone.presentation.ui.search.group

import android.graphics.Bitmap
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.SmallTagLayoutBinding
import com.andre_max.tiktokclone.models.tag.Tag
import com.andre_max.tiktokclone.models.video.RemoteVideo
import com.andre_max.tiktokclone.utils.NumbersUtils
import com.andre_max.tiktokclone.utils.runAsync
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TagGroup(
    private val tag: Tag,
    private val getVideoThumbnail: suspend (RemoteVideo) -> Bitmap?,
    private val fetchVideos: suspend () -> List<RemoteVideo>
) : BindableItem<SmallTagLayoutBinding>() {

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()

    override fun bind(binding: SmallTagLayoutBinding, position: Int) {
        binding.tagName.text = binding.root.context.getString(R.string.hash_tag_name, tag.name)
        binding.tagCount.text = NumbersUtils.formatTagCount(tag.count)

        binding.tagRecyclerview.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        runAsync {
            val listOfSmallTagImage = fetchVideos().map { remoteVideo ->
                SmallTagImage { getVideoThumbnail(remoteVideo) }
            }
            withContext(Dispatchers.Main) { groupAdapter.addAll(listOfSmallTagImage) }
        }
    }

    override fun initializeViewBinding(view: View) =
        SmallTagLayoutBinding.bind(view)

    override fun getLayout(): Int = R.layout.small_tag_layout
}
