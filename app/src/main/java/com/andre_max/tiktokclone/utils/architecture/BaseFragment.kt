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