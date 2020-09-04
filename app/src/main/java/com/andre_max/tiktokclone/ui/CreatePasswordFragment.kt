package com.andre_max.tiktokclone.ui

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.EmailBody
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentCreatePasswordBinding
import com.andre_max.tiktokclone.firebaseAuth
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider


class CreatePasswordFragment : Fragment() {

    lateinit var binding: FragmentCreatePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_password, container, false)
        val email = CreatePasswordFragmentArgs.fromBundle(requireArguments()).email
        val passwordInput = binding.passwordInput
        val signUpBtn = binding.signUpBtn
        val errorText = binding.errorText

        passwordInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var hasDigit = false
                var hasCharacter = false

                passwordInput.text.toString().toCharArray().forEach {
                    if (it.isLetter()) hasCharacter = true
                    else if (it.isDigit()) hasDigit = true
                }

                if (passwordInput.text.toString().length >= 8 && hasCharacter && hasDigit) {
                    errorText.text = getString(R.string.valid_password)
                    errorText.setTextColor(Color.parseColor("#19FD00"))
                }
                else{
                    errorText.setTextColor(Color.parseColor("#F43636"))

                    errorText.text = if (hasCharacter && !hasDigit) {
                        "Password must contain at least one digit"
                    }
                    else if (!hasCharacter && hasDigit) {
                        "Password must contain at least one character"
                    }
                    else {
                        "Password must contain at least one character and one digit"
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) { }
        })

        // Error red - #F43636
        // Valid green - #19FD00

        signUpBtn.setOnClickListener { view ->
            var hasDigit = false
            var hasCharacter = false

            passwordInput.text.toString().toCharArray().forEach {
                if (it.isLetter()) hasCharacter = true
                else if (it.isDigit()) hasDigit = true
            }

            if (passwordInput.text.toString().length < 8 || !hasCharacter || !hasDigit) {
                Snackbar.make(view, "Invalid password", Snackbar.LENGTH_LONG).setTextColor(Color.parseColor("#FFF")).show()
                return@setOnClickListener
            }

            val emailBody = EmailBody(email, passwordInput.text.toString())
            findNavController().navigate(CreatePasswordFragmentDirections.actionCreatePasswordFragmentToCreateUsernameFragment(null, null, null, emailBody))
//            val credential = EmailAuthProvider
//            firebaseAuth.signInWithEmailAndPassword()


        }

        return binding.root
    }
}