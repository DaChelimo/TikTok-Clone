package com.andre_max.tiktokclone.profile_tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.FragmentMyPublicVideosBinding
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.user_video_layout.view.*
import timber.log.Timber


class MyPublicVideosFragment : Fragment() {

    lateinit var binding: FragmentMyPublicVideosBinding
    var adapter = GroupAdapter<GroupieViewHolder>()
    lateinit var ARG_UID: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_public_videos, container, false)
        val recyclerView = binding.publicVideosRecyclerview

        recyclerView.layoutManager = GridLayoutManager(this.requireContext(), 3)
        recyclerView.adapter = adapter

        getPublicVideos()

        return binding.root
    }

    val videoMap = LinkedHashMap<String, RemoteUserVideo>()

    private fun getPublicVideos() {
        val ref = firebaseDatabase.getReference(getUserPublicVideosPath(ARG_UID))

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val userVideo = it.getValue(RemoteUserVideo::class.java) ?: return@forEach
                    videoMap[userVideo.firestoreId] = userVideo
                }

                videoMap.values.forEach {
                    val userVideoGroup = UserVideoGroup(it, this@MyPublicVideosFragment)
                    adapter.add(userVideoGroup)
                }

                Timber.d("Size of adapter is ${adapter.itemCount}")
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.message)
            }
        })
    }

    private class UserVideoGroup(private val remoteUserVideo: RemoteUserVideo, private val fragment: Fragment): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val layout = viewHolder.itemView
            layout.layoutParams = ConstraintLayout.LayoutParams(250, 250)

            Timber.d("layout.layoutParams is ${layout.layoutParams}")
            Glide.with(layout.context)
                .load(remoteUserVideo.url)
                .into(layout.user_video)

            layout.setOnClickListener {
                fragment.findNavController().navigate(MeFragmentDirections.actionMeFragmentToEachTikTokVideo(remoteUserVideo))
            }

            layout.user_video_duration.text = convertTimeToDisplayTime(remoteUserVideo.duration.toString())
        }

        override fun getLayout(): Int = R.layout.user_video_layout
    }


    companion object {
        fun getInstance (uid: String?): MyPublicVideosFragment {
            val myPublicVideosFragment = MyPublicVideosFragment()
            myPublicVideosFragment.ARG_UID = uid ?: firebaseAuth.currentUser?.uid.toString()
            return myPublicVideosFragment
        }
    }
}