package com.andre_max.tiktokclone.ui.sign_up.email_sign_up

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentBasicEmailBinding

class BasicEmailFragment : Fragment() {

    lateinit var binding: FragmentBasicEmailBinding
    lateinit var inputEmail: EditText

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_basic_email, container, false)
        inputEmail = binding.emailInput
        val signUpBtn = binding.signUpBtn

        inputEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (inputEmail.text.toString().isNotEmpty()){
                    signUpBtn.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.pinkBtnBackground, null))
                    signUpBtn.setTextColor(ResourcesCompat.getColor(resources, R.color.tw__solid_white, null))
                }
                else {
                    signUpBtn.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.grey_button_background, null))
                    signUpBtn.setTextColor(ResourcesCompat.getColor(resources, R.color.textColor, null))
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


        binding.addGmailBtn.setOnClickListener {
            inputEmail.setText("${inputEmail.text}@gmail.com")
        }
        binding.addHotmailBtn.setOnClickListener {
            inputEmail.setText("${inputEmail.text}@hotmail.com")
        }
        binding.addOutlookBtn.setOnClickListener {
            inputEmail.setText("${inputEmail.text}@outlook.com")
        }

        binding.signUpBtn.setOnClickListener {
            if (!inputEmail.text.toString().endsWith(".com")) {
                Toast.makeText(this.requireContext(), "Enter a valid email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            findNavController().navigate(BasicSignUpFragmentDirections.actionBasicSignUpFragmentToCreatePasswordFragment(inputEmail.text.toString()))
        }

        return binding.root
    }

}