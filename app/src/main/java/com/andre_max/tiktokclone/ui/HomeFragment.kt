package com.andre_max.tiktokclone.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.comments.MainComment
import com.andre_max.tiktokclone.databinding.FragmentHomeBinding
import com.andre_max.tiktokclone.exoplayer.Player
import com.andre_max.tiktokclone.video_reaction.Reaction
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.each_tiktok_video.view.*
import timber.log.Timber

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private val adapter = GroupAdapter<GroupieViewHolder>()
    var currentSimpleExoPlayerView: PlayerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Timber.d("onCreateView called")
        (activity as MainActivity).navView.setBackgroundColor(Color.TRANSPARENT)
        Timber.d("background is ${(activity as MainActivity).navView.background}")

        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        val recyclerView = binding.recyclerView
        (activity as MainActivity).navView.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.tw__solid_white, null))

        recyclerView.adapter = adapter
        getVideos()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val llm = (recyclerView.layoutManager as LinearLayoutManager)
                val visiblePosition = llm.findFirstCompletelyVisibleItemPosition()
                Timber.d("visiblePosition is $visiblePosition")
//                val previousPosition = llm.findLastCompletelyVisibleItemPosition()
//                (adapter.getItem(previousPosition) as EachRemoteVideoGroup).
            }
        })

        return binding.root
    }

    var videoGroupList = ArrayList<EachRemoteVideoGroup>()


    private fun getVideos() {
        val ref = firebaseDatabase.getReference(getAllVideosPath())
            .limitToFirst(36)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val remoteVideo = it.getValue(RemoteUserVideo::class.java)
                    if (remoteVideo == null) {
                        Timber.d("RemoteVideo is null")
                        return
                    }

                    videoGroupList.add(EachRemoteVideoGroup(remoteVideo, this@HomeFragment, (activity as MainActivity).navView.height))
                }

                videoGroupList.sortedByDescending { it.remoteUserVideo.likes }
                adapter.update(videoGroupList)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.message)
            }
        })
    }

    class EachRemoteVideoGroup(val remoteUserVideo: RemoteUserVideo, private val homeFragment : HomeFragment, private val navViewHeight: Int): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            Timber.d("bind method called")

            val shouldReload = ((homeFragment.videoGroupList.size - position) < homeFragment.videoGroupList.size / 4)
            Timber.d("shouldReload is $shouldReload")

            if (shouldReload) {
                homeFragment.getVideos()
            }

        }

        override fun onViewAttachedToWindow(viewHolder: GroupieViewHolder) {
            super.onViewAttachedToWindow(viewHolder)
            Timber.d("onViewAttachedToWindow called.")

            val layout = viewHolder.itemView

            val simpleExoPlayerView = layout.actual_video
            val videoCreatorTag = layout.video_creator_tag
            val videoDescription = layout.video_description
            val likeBtn = layout.like_video_icon
            val shareBtn = layout.share_video_btn
            val commentSectionBtn = layout.comment_section_btn
            val sendCommentBtn = layout.send_comment_btn
            val commentText = layout.comment_text
            val commentLayout = layout.comment_layout
            val playBtn = layout.play_btn
            val totalComments = layout.total_comments
            val adapter = GroupAdapter<GroupieViewHolder>()
            val followVideoCreator = layout.follow_video_creator
            val commentRecyclerview = layout.comment_recyclerview
            val exitCommentSectionBtn = layout.exit_comment_section_btn
            val videoCreatorIcon = layout.video_creator_icon

            val reaction = Reaction(remoteUserVideo, likeBtn)
            val mainComment = MainComment(commentLayout, remoteUserVideo, adapter, totalComments)


            if (firebaseAuth.uid == remoteUserVideo.creatorUid) {
                followVideoCreator.visibility = View.GONE
                Timber.d("firebaseAuth.uid == remoteUserVideo.creatorUid is ${firebaseAuth.uid == remoteUserVideo.creatorUid}")
            }

            val myFollowingRef = firebaseDatabase.getReference(getMyFollowingPath()).child(remoteUserVideo.creatorUid)

            myFollowingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Timber.d("snapshot.exists() is ${snapshot.exists()}")
                    if (snapshot.exists()) {
                        followVideoCreator.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException())
                }
            })

            reaction.checkIfVideoWasLiked()
            Timber.d("videoLiked is ${reaction.wasLiked}")

            followVideoCreator.setOnClickListener {
                @Suppress("NAME_SHADOWING")
                val myFollowingRef = firebaseDatabase.getReference(getMyFollowingPath()).push()
                val otherFollowersRef = firebaseDatabase.getReference("${getOtherFollowerPath(remoteUserVideo.creatorUid)}/${firebaseAuth.uid}")

                myFollowingRef.setValue(remoteUserVideo.creatorUid)
                otherFollowersRef.setValue(firebaseAuth.uid)
                followVideoCreator.visibility = View.GONE
            }


            sendCommentBtn.setOnClickListener {
                if (commentText.text.toString().isEmpty()) {
                    Snackbar.make(layout, "You cannot send an empty comment", Snackbar.LENGTH_SHORT).let {
                        it.setTextColor(ColorStateList.valueOf(ResourcesCompat.getColor(layout.context.resources, android.R.color.white, null)))
                        it.show()
                    }
                    return@setOnClickListener
                }

                mainComment.addComment(commentText.text.toString())
            }

            videoCreatorIcon.setOnClickListener {
                homeFragment.findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileWithAccountFragment(remoteUserVideo.creatorUid))
            }

            commentSectionBtn.setOnClickListener {
                commentRecyclerview.layoutManager = LinearLayoutManager(layout.context)
                mainComment.setUpCommentSection(commentRecyclerview)
            }

            exitCommentSectionBtn.setOnClickListener {
                mainComment.hideCommentSection()
            }

            likeBtn.setOnClickListener {
                reaction.checkIfVideoWasLiked()
                if (reaction.wasLiked) {
                    Timber.d("Is now unliking")
                    reaction.removeLike()
                }
                else {
                    Timber.d("Is now liking")
                    reaction.likeVideo()
                }
            }


            videoDescription.text = remoteUserVideo.description ?: "#NoDescription"

            val ref = firebaseDatabase.getReference(getUserBasicDataPath(remoteUserVideo.creatorUid))
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userBasicData = snapshot.getValue(User::class.java)
                    val username = userBasicData?.username

                    if (username == null) {
                        Timber.d("username is null")
                        return
                    }

                    videoCreatorTag.text ="@$username"

                    userBasicData.profilePictureUrl?.let {
                        Glide.with(layout.context)
                            .load(it)
                            .into(layout.video_creator_icon)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.message)
                }
            })

            val simpleExoPlayer: SimpleExoPlayer? = null
            val player = Player(simpleExoPlayerView, layout.context, simpleExoPlayer, remoteUserVideo.url)

            player.startPlayer()
            homeFragment.currentSimpleExoPlayerView = player.simpleExoplayerView

            simpleExoPlayerView.setOnClickListener {
                player.doPlayerChange(playBtn)
            }
        }

        override fun onViewDetachedFromWindow(viewHolder: GroupieViewHolder) {
            val simpleExoPlayerView = viewHolder.itemView.actual_video
            Timber.d("onViewDetachedFromWindow called.")
            simpleExoPlayerView.player?.release()

            simpleExoPlayerView.player = null

            super.onViewDetachedFromWindow(viewHolder)
        }

        override fun getLayout(): Int = R.layout.each_tiktok_video
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart called")
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume called.")
        val navView = (activity as MainActivity).navView
        navView.visibility = View.VISIBLE
        navView.setBackgroundColor(Color.TRANSPARENT/*Color.parseColor("#00000000")*/)
        navView.itemTextColor = ColorStateList.valueOf(Color.WHITE)
        navView.itemIconTintList = ColorStateList.valueOf(Color.WHITE)

//        val a = Color.TRANSPARENT
    }

    override fun onPause() {
        super.onPause()
        Timber.d("onPause called")
        Timber.d("currentSimpleExoPlayerView is $currentSimpleExoPlayerView")
        if (currentSimpleExoPlayerView != null) {
            currentSimpleExoPlayerView!!.player?.release()
        }
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop called")
        Timber.d("currentSimpleExoPlayerView is $currentSimpleExoPlayerView")
        if (currentSimpleExoPlayerView != null) {
            currentSimpleExoPlayerView!!.player?.release()
        }

        val navView = (activity as MainActivity).navView
        navView.setBackgroundColor(Color.WHITE/*Color.parseColor("#00000000")*/)
        navView.itemTextColor = ColorStateList.valueOf(Color.BLACK)
        navView.itemIconTintList = ColorStateList.valueOf(Color.BLACK)
    }

}