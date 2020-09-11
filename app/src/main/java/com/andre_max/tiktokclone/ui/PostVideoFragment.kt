package com.andre_max.tiktokclone.ui

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.FragmentPostVideoBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import timber.log.Timber
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class PostVideoFragment : Fragment() {

    lateinit var binding: FragmentPostVideoBinding
    lateinit var localUserVideo: LocalUserVideo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        localUserVideo = PostVideoFragmentArgs.fromBundle(requireArguments()).localUserVideo
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_post_video, container, false)

        val mediaMetadataRetriever = MediaMetadataRetriever()
        Timber.d("Uri parser uri is ${Uri.parse(localUserVideo.url)}")
        mediaMetadataRetriever.setDataSource(this.requireContext(), Uri.parse(localUserVideo.url))

        val bmp = mediaMetadataRetriever.frameAtTime

        Glide.with(this)
            .load(bmp)
            .into(binding.videoThumbnail)

        binding.postBtn.setOnClickListener {
            postVideo()
        }

        return binding.root
    }

    private fun postVideo() {
        val storageRef = firebaseStorage.getReference("/videos/${firebaseAuth.uid}/${UUID.randomUUID()}")
        val videoFile = File(localUserVideo.url!!)
        Timber.d("videoFile is $videoFile")

        storageRef.putFile(Uri.fromFile(videoFile))
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener {
                        addVideosToDatabase(it.toString())
                        Timber.d("Internal VideoUrl is $it")
                    }
                    .addOnFailureListener {
                        Timber.e(it)
                    }
            }
            .addOnFailureListener {
                Timber.e(it)
            }

    }

    private fun addVideosToDatabase(videoUrl: String) {
        val descriptionText = if (binding.descriptionText.text.toString().isEmpty()) null else binding.descriptionText.text.toString()
        val stringArray = descriptionText?.split(" ") ?: listOf()
        val tags = ArrayList<String>()

        stringArray.forEach {
            if (it.startsWith("#")){
                tags.add(it)
            }
        }

        Timber.d("String array size is ${stringArray.size} and tags size is ${tags.size}")
        Timber.d("videoUrl is $videoUrl")


        val globalVideosRef = firebaseDatabase.getReference(getAllVideosPath()).push()
        val databaseRef = firebaseDatabase.getReference("${getUserPublicVideosPath()}/${globalVideosRef.key}")

        val remoteUserVideo = RemoteUserVideo(
            url = videoUrl,
            description = descriptionText,
            tags = tags,
            duration = localUserVideo.duration?.toLong() ?: return,
            key = globalVideosRef.key.toString(),
            dateCreated = System.currentTimeMillis(),
            likes = 0,
            views = 0,
            creatorUid = firebaseAuth.uid ?: return
        )

        globalVideosRef.setValue(remoteUserVideo)
            .addOnSuccessListener {
                Timber.d("Success adding video to global videos reference.")
            }
            .addOnFailureListener {
                Timber.e(it)
            }

        databaseRef.setValue(remoteUserVideo)
            .addOnSuccessListener {
                Timber.d("Added video successfully in personal profile.")
                findNavController().navigate(PostVideoFragmentDirections.actionPostVideoFragmentToEachTikTokVideo(remoteUserVideo))
            }
            .addOnFailureListener {
                Timber.e(it)
            }
    }

}