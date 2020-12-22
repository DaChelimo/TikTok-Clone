package com.andre_max.tiktokclone

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andre_max.tiktokclone.databinding.FragmentSearchSuggestionsBinding
import com.andre_max.tiktokclone.suggestions.Suggestions
import timber.log.Timber

class SearchSuggestionsFragment : Fragment() {

    private lateinit var binding: FragmentSearchSuggestionsBinding
    private lateinit var searchInput: EditText
    private lateinit var suggestionsRecyclerView: RecyclerView
    private lateinit var suggestions: Suggestions

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_suggestions, container, false)
        searchInput = binding.searchInput
        val imm = this.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        searchInput.requestFocus()
        imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
        suggestionsRecyclerView = binding.suggestionsRecyclerview
        suggestions = Suggestions(searchInput)
        suggestionsRecyclerView.adapter = suggestions.adapter
        suggestionsRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setUpSuggestions()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun setUpSuggestions() {
        Timber.d("searchInput.text.toString().isNotEmpty() is ${searchInput.text.toString().isNotEmpty()}")
        if (searchInput.text.toString().isNotEmpty()) {
            suggestions.getSuggestions()
        }
    }
}