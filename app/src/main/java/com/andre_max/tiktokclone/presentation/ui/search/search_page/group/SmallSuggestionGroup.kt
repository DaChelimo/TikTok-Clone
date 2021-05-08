package com.andre_max.tiktokclone.presentation.ui.search.search_page.group

import android.view.View
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.SearchSuggestionBinding
import com.andre_max.tiktokclone.models.user.User
import com.bumptech.glide.Glide
import com.xwray.groupie.viewbinding.BindableItem

class SmallSuggestionGroup(
    private val user: User, private val insertSearchNameToEditText: (String) -> Unit
) : BindableItem<SearchSuggestionBinding>() {
    override fun bind(binding: SearchSuggestionBinding, position: Int) {
        binding.searchSuggestionName.text = user.username
        binding.addSearchSuggestion.setOnClickListener { insertSearchNameToEditText(user.username) }
        Glide.with(binding.root.context).load(user.profilePictureUrl).into(binding.searchSuggestionIcon)
    }

    override fun getLayout(): Int = R.layout.search_suggestion
    override fun initializeViewBinding(view: View) =
        SearchSuggestionBinding.bind(view)
}