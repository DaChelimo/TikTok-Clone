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
