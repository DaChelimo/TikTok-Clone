package com.andre_max.tiktokclone.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentAgeBinding
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

class AgeFragment : Fragment() {

    lateinit var binding: FragmentAgeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_age, null, false)

        val date = CustomDate(23, 1, 2018)
        binding.datePicker2.init(2018, 1, 23, object : DatePicker.OnDateChangedListener {
            override fun onDateChanged(
                view: DatePicker?,
                year: Int,
                monthOfYear: Int,
                dayOfMonth: Int
            ) {
                date.date = dayOfMonth
                date.month = monthOfYear
                date.year = year
            }
        })
        binding.datePicker2.maxDate = System.currentTimeMillis()

        val currentDate = Date()
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.time = currentDate
        val year = calendar.get(Calendar.YEAR)

        binding.signUpBtn.setOnClickListener {
            if (date.year > year - 9){
                Snackbar.make(it, "You are too young to have an account. Thanks for trying.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if (date.year < year - 150) {
                Snackbar.make(it, "Enter valid year.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            findNavController().navigate(AgeFragmentDirections.actionAgeFragmentToBasicSignUpFragment())
        }

        Timber.d("binding.datePicker2 is ${binding.datePicker2} and binding.datePicker2.visibility is ${binding.datePicker2.visibility}")

        return binding.root
    }

    class CustomDate(var date: Int, var month: Int, var year: Int)
}