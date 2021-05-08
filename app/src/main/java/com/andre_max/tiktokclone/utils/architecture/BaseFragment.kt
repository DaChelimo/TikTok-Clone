package com.andre_max.tiktokclone.utils.architecture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.andre_max.tiktokclone.utils.ResUtils

/**
 * A small class providing necessary abstraction over a bunch of functions I use in almost every fragment
 * that uses viewBinding
 */
abstract class BaseFragment(@LayoutRes val resId: Int?): Fragment() {

    open val viewModel by viewModels<BaseViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return resId?.let {
            inflater.inflate(resId, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLayout()
        setUpClickListeners()
        setUpLiveData()
    }

    /**
     * Ensures that we don't forget to set up our binding class
     */
    abstract fun setUpLayout()

    /**
     * Given the fact that a fragment may lack view click listeners, we will not enforce this
     */
    open fun setUpClickListeners() {}

    /**
     * Given the fact that a fragment may lack not observe any LiveData, we will also not enforce this
     */
    open fun setUpLiveData() {
        viewModel.snackBarMessageRes.observe(viewLifecycleOwner) { resId ->
            resId?.let {
                ResUtils.showSnackBar(requireView(), resId)
                viewModel.clearMessage()
            }
        }
    }
}