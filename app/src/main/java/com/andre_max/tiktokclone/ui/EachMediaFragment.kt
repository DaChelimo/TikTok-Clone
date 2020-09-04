package com.andre_max.tiktokclone.ui

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.FragmentEachMediaBinding
import com.bumptech.glide.Glide
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.user_image_layout.view.*
import kotlinx.android.synthetic.main.user_video_layout.view.*
import timber.log.Timber

class EachMediaFragment: Fragment() {

    lateinit var binding: FragmentEachMediaBinding
    private lateinit var adapter: GroupAdapter<GroupieViewHolder>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_each_media, null, false)
        Timber.d("Internal position is ${arguments?.getInt(POSITION)}")

        val position = requireArguments().getInt(POSITION)
        Timber.d("position is $position")
        adapter = GroupAdapter()
        val eachMediaRecyclerView = binding.eachMediaRecyclerview

        eachMediaRecyclerView.layoutManager = GridLayoutManager(this.requireContext(), 4)
        eachMediaRecyclerView.adapter = adapter

        if (position == 0) getAllImages() else if (position == 1) getAllVideos() else throw UnknownError("position is $position")

        return binding.root
    }

    private fun getAllImages(){
        val contentResolver = context?.contentResolver
        val mediaStoreUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver?.query(mediaStoreUri, null, null, null, null)
        if (cursor == null){
            Timber.d("Cursor is null")
            return
        }

        val imageList = ArrayList<UserImage>()

        while (cursor.moveToNext()){
            val url = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            val dateCreated = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))
            val userImage = UserImage(url, dateCreated)
            Timber.d("url is $url")
            imageList.add(userImage)
        }

        addImagesToGroupieAdapter(imageList)

        cursor.close()
    }

    private fun getAllVideos(){
        val contentResolver = context?.contentResolver
        val mediaStoreUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver?.query(mediaStoreUri, null, null, null, null)
        if (cursor == null){
            Timber.d("Cursor is null")
            return
        }

        val videoList = ArrayList<UserVideo>()
        while (cursor.moveToNext()){
            val url = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
            val duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
            val dateCreated = cursor.getColumnName(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))
            val userVideo = UserVideo(url, duration, dateCreated)
            Timber.d("url is $url")
            videoList.add(userVideo)
        }

        addVideosToGroupieAdapter(videoList)

        cursor.close()
    }

    private fun addImagesToGroupieAdapter(imageList: ArrayList<UserImage>) {
        imageList.forEach{
            val userImageGroup = UserImageGroup(it)
            adapter.add(userImageGroup)
        }
    }

    private fun addVideosToGroupieAdapter(videoList: ArrayList<UserVideo>) {
        videoList.forEach {
            val userVideoGroup = UserVideoGroup(it)
            adapter.add(userVideoGroup)
        }
    }

    class UserImageGroup(private val userImage: UserImage): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val image = viewHolder.itemView.user_image

            Glide.with(viewHolder.itemView.context)
                .load(userImage.url)
                .into(image)
        }

        override fun getLayout(): Int = R.layout.user_image_layout
    }

    class UserVideoGroup(private val userVideo: UserVideo): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val layout = viewHolder.itemView

            val mediaMetadataRetriever = MediaMetadataRetriever()
            Timber.d("Cursor url is ${userVideo.url} and Uri parser uri is ${Uri.parse(userVideo.url)}")
            mediaMetadataRetriever.setDataSource(layout.context, Uri.parse(userVideo.url))

            val bmp = mediaMetadataRetriever.frameAtTime

            Timber.d("bitmap is $bmp")
            Glide.with(layout.context)
                .load(bmp)
                .into(layout.user_video)

            layout.user_video_duration.text = convertTimeToDisplayTime(userVideo.duration ?: return)
        }

        override fun getLayout(): Int = R.layout.user_video_layout
    }
}

/*
arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EachMediaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
 */