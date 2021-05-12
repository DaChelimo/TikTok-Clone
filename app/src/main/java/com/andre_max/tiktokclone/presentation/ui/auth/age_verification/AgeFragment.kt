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

package com.andre_max.tiktokclone.presentation.ui.auth.age_verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentAgeBinding
import com.andre_max.tiktokclone.models.CustomDate
import com.andre_max.tiktokclone.utils.ResUtils.showSnackBar
import java.util.*

class AgeFragment : Fragment(R.layout.fragment_age) {

    lateinit var binding: FragmentAgeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAgeBinding.bind(view)

        binding.backBtn.setOnClickListener { findNavController().popBackStack() }

        binding.datePicker2.init(2018, 1, 23, dateChangedLambda)
        binding.datePicker2.maxDate = System.currentTimeMillis()

        val year = Calendar.getInstance(Locale.getDefault()).let {
            it.time = Date()
            it.get(Calendar.YEAR)
        }

        binding.signUpBtn.setOnClickListener {
            when {
                date.year > year - 9 -> showSnackBar(requireView(), R.string.user_too_young)
                date.year < year - 150 -> showSnackBar(requireView(), R.string.enter_valid_year)
                else -> findNavController()
                    .navigate(AgeFragmentDirections.actionAgeFragmentToSelectBasicSignUpFragment())
            }
        }
    }

    private val date = CustomDate(23, 1, 2018)

    private val dateChangedLambda =
        DatePicker.OnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            date.date = dayOfMonth
            date.month = monthOfYear
            date.year = year
        }
}