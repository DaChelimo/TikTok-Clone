package com.andre_max.tiktokclone.ui.sign_up.age_verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.databinding.FragmentAgeBinding
import com.andre_max.tiktokclone.models.CustomDate
import com.google.android.material.snackbar.Snackbar
import java.util.*

class AgeFragment : Fragment() {

    lateinit var binding: FragmentAgeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAgeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.datePicker2.init(2018, 1, 23, dateChangedLambda)
        binding.datePicker2.maxDate = System.currentTimeMillis()

        val year = Calendar.getInstance(Locale.getDefault()).let {
            it.time = Date()
            it.get(Calendar.YEAR)
        }

        binding.signUpBtn.setOnClickListener {
            if (date.year > year - 9) {
                Snackbar.make(
                    it,
                    "You are too young to have an account. Thanks for trying.",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (date.year < year - 150) {
                Snackbar.make(it, "Enter valid year.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            findNavController().navigate(AgeFragmentDirections.actionAgeFragmentToBasicSignUpFragment())
        }
    }

    private val date = CustomDate(23, 1, 2018)

    val dateChangedLambda =
        DatePicker.OnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            date.date = dayOfMonth
            date.month = monthOfYear
            date.year = year
        }
}