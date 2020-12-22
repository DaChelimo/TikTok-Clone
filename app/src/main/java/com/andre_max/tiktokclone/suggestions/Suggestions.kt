package com.andre_max.tiktokclone.suggestions

import android.widget.EditText
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.User
import com.andre_max.tiktokclone.firebaseDatabase
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.each_suggestions_profile.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class Suggestions(val searchInput: EditText) {
    val adapter = GroupAdapter<GroupieViewHolder>()
    private val uiThread = CoroutineScope(Dispatchers.Main)

    fun getSuggestions(): ArrayList<User> {
        val peopleSuggestionsRef = firebaseDatabase.getReference("users")
            .orderByChild("followers")
        val mapOfUsers = LinkedHashMap<String, User>()
        Timber.d("Search input is $searchInput")

        mapOfUsers.clear()
        adapter.clear()
        uiThread.launch {
            peopleSuggestionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        if (mapOfUsers.size < 10) {
                            val basicDataRef = it.ref.child("basic-data")

                            basicDataRef.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val basicUserData = snapshot.getValue(User::class.java)
                                    Timber.d("basicUserData is $basicUserData")
                                    basicUserData?.let { user ->
                                        if (user.username.contains(searchInput.text.toString())) {
                                            mapOfUsers[user.uid] = user
                                        }
                                    }
                                    Timber.d("Internal map size is ${mapOfUsers.size}")

                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Timber.e(error.toException())
                                }
                            })
                        }
                        else return
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException())
                }
            })
        }

        mapOfUsers.values.forEach {
            val eachSuggestionsProfileGroup = EachSuggestionsProfileGroup(it, searchInput)
            adapter.add(eachSuggestionsProfileGroup)
        }

        Timber.d("Before function return, arrayOfUsers.values.size is ${mapOfUsers.values.size}")
        return mapOfUsers.values.toList() as ArrayList<User>
    }

    class EachSuggestionsProfileGroup(private val user: User, private val searchInput: EditText) : Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val layout = viewHolder.itemView
            val searchName = layout.search_name
            val searchProfilePic = layout.search_profile_pic
            val addToTextIcon = layout.add_to_search_input_btn

            searchName.text = user.username
            addToTextIcon.setOnClickListener {
                searchInput.setText(user.username)
            }

            Glide.with(layout.context)
                .load(user.profilePictureUrl)
                .into(searchProfilePic)
        }

        override fun getLayout(): Int = R.layout.each_suggestions_profile
    }
}