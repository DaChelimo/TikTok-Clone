package com.andre_max.tiktokclone.presentation.ui.suggestions.group

import android.view.View
import android.widget.EditText
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.SmallSuggestionLayoutBinding
import com.andre_max.tiktokclone.models.user.User
import com.bumptech.glide.Glide
import com.xwray.groupie.viewbinding.BindableItem

class SmallSuggestionGroup(
    private val user: User, private val insertSearchNameToEditText: (String) -> Unit
) : BindableItem<SmallSuggestionLayoutBinding>() {
    override fun bind(binding: SmallSuggestionLayoutBinding, position: Int) {
        binding.searchName.text = user.username
        binding.insertSearchName.setOnClickListener { insertSearchNameToEditText(user.username) }
        Glide.with(binding.root.context).load(user.profilePictureUrl).into(binding.searchProfilePic)
    }

    override fun getLayout(): Int = R.layout.small_suggestion_layout
    override fun initializeViewBinding(view: View) =
        SmallSuggestionLayoutBinding.bind(view)
}