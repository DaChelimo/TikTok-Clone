package com.andre_max.tiktokclone.comments

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.R
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.xwray.groupie.*
import kotlinx.android.synthetic.main.each_comment_item.view.*
import timber.log.Timber

class MainComment(
    private val commentLayout: ConstraintLayout,
    private val remoteUserVideo: RemoteUserVideo,
    private val adapter: GroupAdapter<GroupieViewHolder>,
    private val totalCommentsText: TextView
) {

    var wasCommentLiked = false

    fun setUpCommentSection(recyclerView: RecyclerView) {
        commentLayout.visibility = View.VISIBLE
        recyclerView.adapter = adapter
        getComments()
        getCommentSize()
    }

    fun hideCommentSection() {
        commentLayout.visibility = View.GONE
    }

    private fun getComments() {
        val commentsRef = firebaseDatabase.getReference(getCommentsPath(remoteUserVideo.key))
        val totalCommentsMap  = LinkedHashMap<String, EachCommentGroup>()

        commentsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val comment = snapshot.getValue(Comment::class.java)

                if (comment == null) {
                    Timber.d("comment is null")
                    return
                }

                val eachCommentGroup = EachCommentGroup(comment, remoteUserVideo)
                totalCommentsMap[comment.commentKey] = eachCommentGroup
                adapter.add(eachCommentGroup)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val comment = snapshot.getValue(Comment::class.java)

                if (comment == null) {
                    Timber.d("comment is null")
                    return
                }
                val eachCommentGroup = EachCommentGroup(comment, remoteUserVideo)
                totalCommentsMap[comment.commentKey] = eachCommentGroup
//                adapter.updateAsync(totalCommentsMap.values as MutableList<out Group>) { Timber.d("update completed") }
                val totalList = ArrayList<EachCommentGroup>()
                totalCommentsMap.values.forEach {
                    totalList.add(it)
                }
                adapter.updateAsync(totalList)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val comment = snapshot.getValue(Comment::class.java)

                if (comment == null) {
                    Timber.d("comment is null")
                    return
                }
                val eachCommentGroup = EachCommentGroup(comment, remoteUserVideo)
                totalCommentsMap.remove(comment.commentKey)
                adapter.remove(eachCommentGroup)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Timber.d("onChildMoved moved")
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        })


        Timber.d(" end totalCommentsList.size is ${totalCommentsMap.size}")
    }

    private fun getCommentSize() {
        val totalCommentsRef = firebaseDatabase.getReference(getAllVideosPath()).child(remoteUserVideo.key).child("totalCommentsSize")

        totalCommentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val size = snapshot.getValue(Long::class.java)
                totalCommentsText.text = "$size comments"
                Timber.d("comments size changed. size: $size")
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        })
    }

    fun addComment(commentText: String) {
        val myCommentsRef = firebaseDatabase.getReference(getCommentsPath(remoteUserVideo.key)).push()
        val comment = Comment(firebaseAuth.uid.toString(), commentText, 0, arrayListOf(), System.currentTimeMillis(), myCommentsRef.key.toString())
        addCommentToCommentSize(firebaseDatabase.getReference(getAllVideosPath()).child(remoteUserVideo.key).child("totalCommentsSize"))

        myCommentsRef.setValue(comment)
            .addOnSuccessListener {
                Timber.d("Success. Added comment.")
            }
            .addOnFailureListener {
                Timber.e(it)
            }

        getCommentSize()
    }

    fun deleteComment(comment: Comment) {
        val myCommentsRef = firebaseDatabase.getReference(getCommentsPath(remoteUserVideo.key)).child(comment.commentKey)
        removeCommentToCommentSize(firebaseDatabase.getReference(getAllVideosPath()).child(remoteUserVideo.key).child("totalCommentsSize"))

        myCommentsRef.removeValue()
            .addOnSuccessListener {
                Timber.d("Successfully removed comment.")
            }
            .addOnFailureListener {
                Timber.e(it)
            }
        getCommentSize()
    }

    private fun addCommentToCommentSize(ref: DatabaseReference) : ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var totalComments = snapshot.getValue(Long::class.java)!!
            totalComments++

            ref.setValue(totalComments)
                .addOnSuccessListener {
                    Timber.d("addCommentToCommentSize successful")
                }
                .addOnFailureListener {
                    Timber.e(it)
                }
        }

        override fun onCancelled(error: DatabaseError) {
            Timber.e(error.toException())
        }
    }

    private fun removeCommentToCommentSize(ref: DatabaseReference) : ValueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var totalComments = snapshot.getValue(Long::class.java)!!
            totalComments--

            ref.setValue(totalComments)
        }

        override fun onCancelled(error: DatabaseError) {
            Timber.e(error.toException())
        }
    }


    class EachCommentGroup(private val comment: Comment, private val remoteUserVideo: RemoteUserVideo): Item<GroupieViewHolder>() {
        var wasLiked = false
        lateinit var likeCommentBtn: ImageView

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val layout = viewHolder.itemView

            val personImage = layout.person_image
            val personComment = layout.person_comment
            val personUsername = layout.person_username
            val commentLikes = layout.likes_count
            likeCommentBtn = layout.like_icon_image

            personComment.text = comment.commentText
            checkIfCommentWasLiked(comment)

            makeUiChanges()
            getCommentLikes(commentLikes, comment, remoteUserVideo)

            Timber.d("wasLiked is $wasLiked")

            likeCommentBtn.setOnClickListener {
                if (!wasLiked) likeComment(comment, likeCommentBtn) else removeCommentLike(comment, likeCommentBtn)
            }

            val basicProfileRef =
                firebaseDatabase.getReference(getUserBasicDataPath(comment.authorUid))

            basicProfileRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)

                    if (user == null) {
                        Timber.d("user is null")
                        return
                    }

                    Glide.with(layout.context)
                        .load(user.profilePictureUrl)
                        .into(personImage)

                    personUsername.text = user.username
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException())
                }
            })
        }

        override fun getLayout(): Int = R.layout.each_comment_item

        private fun likeComment(comment: Comment, likeCommentBtn: ImageView) {
            val commentRef = firebaseDatabase.getReference(getCommentsPath(remoteUserVideo.key))
                .child(comment.commentKey).child("commentLikes")
            val myLikedCommentsRef =
                firebaseDatabase.getReference("${getMyLikedComments()}/${comment.commentKey}")

            commentRef.addListenerForSingleValueEvent(likeVideoValueEventFun(commentRef))
            myLikedCommentsRef.setValue(true)
//            myLikedCommentsRef.addListenerForSingleValueEvent(likeVideoValueEventFun(myLikedCommentsRef))
            checkIfCommentWasLiked(comment)
            makeUiChanges()
        }

        private fun removeCommentLike(comment: Comment, likeCommentBtn: ImageView) {
            val commentRef = firebaseDatabase.getReference(getCommentsPath(remoteUserVideo.key))
                .child(comment.commentKey).child("commentLikes")
            val myLikedCommentsRef =
                firebaseDatabase.getReference("${getMyLikedComments()}/${comment.commentKey}")

            commentRef.addListenerForSingleValueEvent(removeLikeVideoValueEventFun(commentRef))
//            myLikedCommentsRef.addListenerForSingleValueEvent(removeLikeVideoValueEventFun(myLikedCommentsRef))
            myLikedCommentsRef.removeValue()
                .addOnSuccessListener {
                    Timber.d("myLikedCommentsRef has been removed")
                }
            checkIfCommentWasLiked(comment)
            makeUiChanges()
        }

        private fun checkIfCommentWasLiked(comment: Comment) {
            val myLikedVideos = firebaseDatabase.getReference(getMyLikedComments()).child(comment.commentKey)

            myLikedVideos.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    wasLiked = snapshot.exists()
                    Timber.d("wasLiked is $wasLiked")
                    makeUiChanges()
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException())
                }
            })

        }

        private fun makeUiChanges() {
            Timber.d("makeUiChanges: wasLiked is $wasLiked")
            if (wasLiked) {
                likeCommentBtn.setImageResource(R.drawable.heart_red)
            } else {
                likeCommentBtn.setImageResource(R.drawable.heart_grey_outline)
            }
        }

        private fun getCommentLikes(commentLikes: TextView, comment: Comment, remoteUserVideo: RemoteUserVideo) {
            val commentRef = firebaseDatabase.getReference(getCommentsPath(remoteUserVideo.key)).child(comment.commentKey).child("commentLikes")

            commentRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val likes = snapshot.getValue(Long::class.java) ?: return

                    Timber.d("Change occurred: Likes is $likes")
                    commentLikes.text = likes.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException())
                }
            })
        }
    }

}