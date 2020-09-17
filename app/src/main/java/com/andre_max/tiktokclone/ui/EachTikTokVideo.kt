package com.andre_max.tiktokclone.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.comments.MainComment
import com.andre_max.tiktokclone.databinding.EachTiktokVideoBinding
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
import timber.log.Timber


class EachTikTokVideo: Fragment() {

    private lateinit var binding: EachTiktokVideoBinding
    private lateinit var simpleExoplayerView: PlayerView
    private lateinit var remoteUserVideo: RemoteUserVideo
    private lateinit var reaction: Reaction
    private lateinit var player: Player
    private lateinit var mainComment: MainComment
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        remoteUserVideo = EachTikTokVideoArgs.fromBundle(requireArguments()).remoteVideo
        binding = DataBindingUtil.inflate(inflater, R.layout.each_tiktok_video, container, false)
        simpleExoplayerView = binding.actualVideo
        val videoCreatorTag = binding.videoCreatorTag
        val videoDescription = binding.videoDescription
        val commentText = binding.commentText
        val sendCommentBtn = binding.sendCommentBtn

        val simpleExoPlayer: SimpleExoPlayer? = null
        player = Player(
            simpleExoplayerView,
            this.requireContext(),
            simpleExoPlayer,
            remoteUserVideo.url
        )

        reaction = Reaction(remoteUserVideo, binding.likeVideoIcon)
        mainComment = MainComment(binding.commentLayout, remoteUserVideo, adapter, binding.totalComments)

        player.startPlayer()

        simpleExoplayerView.setOnClickListener {
            player.doPlayerChange(binding.playBtn)
        }

        videoDescription.text = remoteUserVideo.description ?: "#NoDescription"

        if (firebaseAuth.uid == remoteUserVideo.creatorUid) {
            binding.followVideoCreator.visibility = View.GONE
            Timber.d("firebaseAuth.uid == remoteUserVideo.creatorUid is ${firebaseAuth.uid == remoteUserVideo.creatorUid}")
        }

        val myFollowingRef = firebaseDatabase.getReference(getMyFollowingPath()).child(remoteUserVideo.creatorUid)

        myFollowingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Timber.d("snapshot.exists() is ${snapshot.exists()}")
                if (snapshot.exists()) {
                    binding.followVideoCreator.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        })

        reaction.checkIfVideoWasLiked()
        Timber.d("videoLiked is ${reaction.wasLiked}")
        

        binding.followVideoCreator.setOnClickListener {
            @Suppress("NAME_SHADOWING")
            val myFollowingRef = firebaseDatabase.getReference(getMyFollowingPath()).push()
            val otherFollowersRef = firebaseDatabase.getReference("${getOtherFollowerPath(remoteUserVideo.creatorUid)}/${firebaseAuth.uid}")

            myFollowingRef.setValue(remoteUserVideo.creatorUid)
            otherFollowersRef.setValue(firebaseAuth.uid)
            binding.followVideoCreator.visibility = View.GONE
        }


        sendCommentBtn.setOnClickListener {
            if (commentText.text.toString().isEmpty()) {
                Snackbar.make(this.requireView(), "You cannot send an empty comment", Snackbar.LENGTH_SHORT).let {
                    it.setTextColor(ColorStateList.valueOf(ResourcesCompat.getColor(resources, android.R.color.white, null)))
                    it.show()
                }
                return@setOnClickListener
            }

            mainComment.addComment(commentText.text.toString())
        }

        binding.videoCreatorIcon.setOnClickListener {
            findNavController().navigate(EachTikTokVideoDirections.actionEachTikTokVideoToProfileWithAccountFragment(remoteUserVideo.creatorUid))
        }

        binding.commentSectionBtn.setOnClickListener {
            binding.commentRecyclerview.layoutManager = LinearLayoutManager(this.requireContext())
            mainComment.setUpCommentSection(binding.commentRecyclerview)
        }

        binding.exitCommentSectionBtn.setOnClickListener {
            mainComment.hideCommentSection()
        }

        binding.likeVideoIcon.setOnClickListener {
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

        val ref = firebaseDatabase.getReference(getUserBasicDataPath(remoteUserVideo.creatorUid))
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userBasicData = snapshot.getValue(User::class.java)
                val username = userBasicData?.username

                if (username == null) {
                    Timber.d("username is null")
                    return
                }

                videoCreatorTag.text = "@$username"

                userBasicData.profilePictureUrl?.let {
                    Glide.with(this@EachTikTokVideo)
                        .load(it)
                        .into(binding.videoCreatorIcon)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.message)
            }
        })

        return binding.root
    }



    override fun onResume() {
        super.onResume()
        (activity as MainActivity).navView.visibility = View.INVISIBLE
        player.startPlayer()
    }


    override fun onStop() {
        super.onStop()
        val simpleExoPlayerView = binding.actualVideo
        Timber.d("onStop called.")
        simpleExoPlayerView.player?.release()

        simpleExoPlayerView.player = null
    }


}