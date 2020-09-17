package com.andre_max.tiktokclone.ui

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.FragmentPostVideoBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
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
        val uriParserUri = File(localUserVideo.url.toString()).toURI().toString()//Uri.parse(localUserVideo.url).toString().toUri()
        Timber.d("Uri parser uri is $uriParserUri")
        mediaMetadataRetriever.setDataSource(this.requireContext(), Uri.parse(localUserVideo.url))
        val bmp = mediaMetadataRetriever.frameAtTime
        mediaMetadataRetriever.release()

        Glide.with(this)
            .load(bmp)
            .into(binding.videoThumbnail)

        binding.postBtn.setOnClickListener {
            postVideo()
        }

        return binding.root
    }

    fun Fragment.showShortToast(text: String) {
        Toast.makeText(this.requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun postVideo() {
        val storageRef = firebaseStorage.getReference("/videos/${firebaseAuth.uid}/${UUID.randomUUID()}")
        val videoFile = File(localUserVideo.url!!)
        val videoUri = localUserVideo.url!!.toString().toUri()
        Timber.d("videoFile is $videoFile and videoUri is $videoUri")
        showShortToast("Uploading has started.")
        binding.postBtn.visibility = View.INVISIBLE
        storageRef.putFile(videoUri)//localUserVideo.url!!.toString() as Uri)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener {
                        showShortToast("Uploading video to Firebase Storage.")
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

        Timber.d("Start of tags loop")
        tags.forEach {
            val tagForFirebaseDatabase = it.replace("#", "", true)
            val basicDataTagRef = firebaseDatabase.getReference("${getTagsPath()}/$tagForFirebaseDatabase/basic-data")
            val videoTagRef = firebaseDatabase.getReference("${getTagsPath()}/$tagForFirebaseDatabase/tag-videos/${globalVideosRef.key}")

            videoTagRef.setValue(globalVideosRef.key.toString())
                .addOnSuccessListener {
                    Timber.d("Added videoTagRef in database.")
                }

            basicDataTagRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val eachTag = EachTag(it, 1)
                        basicDataTagRef.setValue(eachTag)
                    }
                    else {
                        val eachTag = snapshot.getValue(EachTag::class.java)
                        val tagNumber = eachTag?.tagCount
                        Timber.d("tagNumber is $tagNumber")

                        tagNumber?.let {num ->
                            eachTag.tagCount = num.plus(1)
                            basicDataTagRef.setValue(eachTag)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException())
                }
            })
        }
        Timber.d("End of tags loop")


        databaseRef.setValue(remoteUserVideo)
            .addOnSuccessListener {
                Timber.d("Added video successfully in personal profile.")
                showShortToast("Video has been successfully uploaded.")
                findNavController().navigate(PostVideoFragmentDirections.actionPostVideoFragmentToEachTikTokVideo(remoteUserVideo))
            }
            .addOnFailureListener {
                Timber.e(it)
            }
    }

}