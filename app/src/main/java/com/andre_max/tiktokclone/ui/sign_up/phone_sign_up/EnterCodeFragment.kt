package com.andre_max.tiktokclone.ui.sign_up.phone_sign_up

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentEnterCodeBinding
import com.andre_max.tiktokclone.userVerificationId
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.PhoneAuthProvider


class EnterCodeFragment : Fragment() {

    lateinit var binding: FragmentEnterCodeBinding
//    private val mAuthListener = FirebaseAuth.AuthStateListener { p0 -> if (p0.currentUser != null) Timber.i("Sign in succeeded") else Timber.i("Sign in failed") }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_enter_code, container, false)

        val codeView = binding.inputCode

        binding.signUpBtn.setOnClickListener {
            if (codeView.code.length != codeView.codeLength) {
                Toast.makeText(this.requireContext(), "Code is invalid. Enter valid code", Toast.LENGTH_SHORT).show()
                codeView.clearCode()
                return@setOnClickListener
            }

            getUserCredential(codeView.code)
        }

        return binding.root
    }

    private fun getUserCredential(code: String) {
        val credential = PhoneAuthProvider.getCredential(userVerificationId.toString(), code) as AuthCredential

        findNavController().navigate(EnterCodeFragmentDirections.actionEnterCodeFragmentToCreateUsernameFragment(credential, null, null))
    }

}