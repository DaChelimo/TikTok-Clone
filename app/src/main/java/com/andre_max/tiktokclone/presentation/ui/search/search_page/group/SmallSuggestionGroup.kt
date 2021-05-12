/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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