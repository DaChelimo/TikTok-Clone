package com.andre_max.tiktokclone.ui.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andre_max.tiktokclone.EachTag
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentSearchBinding
import com.andre_max.tiktokclone.firebaseDatabase
import com.andre_max.tiktokclone.getTagsPath
import com.andre_max.tiktokclone.tag.MainTag
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.each_tag_item.view.*
import timber.log.Timber


class SearchFragment : Fragment() {

    lateinit var binding: FragmentSearchBinding
    lateinit var searchRecyclerView: RecyclerView
    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false)
        searchRecyclerView = binding.searchRecyclerview

        searchRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        searchRecyclerView.adapter = adapter

        fetchTagData()

        binding.searchTextBackground.setOnClickListener {
            Timber.d("searchTextBackground.setOnClickListener called")
            findNavController().navigate(R.id.action_search_fragment_to_searchSuggestionsFragment)
        }

        return binding.root
    }

    val tagsMap = LinkedHashMap<String, EachTagItemGroup>()

    private fun fetchTagData() {
        val tagsRef = firebaseDatabase.getReference(getTagsPath())
            .orderByChild("tagCount")
            .limitToFirst(40)

        tagsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val eachTag = it.getValue(EachTag::class.java)

                    val eachTagItemGroup = EachTagItemGroup(eachTag, this@SearchFragment.requireContext())
                    tagsMap[eachTag?.tagName.toString()] = eachTagItemGroup
                }
                adapter.updateAsync(tagsMap.values.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
            }
        })
    }

    class EachTagItemGroup(private val eachTag: EachTag?, private val context: Context): Item<GroupieViewHolder>() {
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            if (eachTag == null) {
                Timber.d("eachTag is $eachTag")
                return
            }

            val layout = viewHolder.itemView
            val hashTagRecyclerView = layout.hash_tag_recyclerview
            val hashTagName = layout.hash_tag_name
            val hashTagCount = layout.hash_tag_count

            val mainTag = MainTag()
            hashTagRecyclerView.adapter = mainTag.adapter
            hashTagRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            mainTag.getVideosWithTag(eachTag.tagName)

            hashTagName.text = "#${eachTag.tagName}"
            hashTagCount.text = mainTag.formatHashTagCount(eachTag.tagCount)
        }

        override fun getLayout(): Int = R.layout.each_tag_item
    }
}