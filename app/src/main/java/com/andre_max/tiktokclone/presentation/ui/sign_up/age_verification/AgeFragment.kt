package com.andre_max.tiktokclone.presentation.ui.sign_up.age_verification

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