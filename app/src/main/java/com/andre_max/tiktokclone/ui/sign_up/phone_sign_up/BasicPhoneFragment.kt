package com.andre_max.tiktokclone.ui.sign_up.phone_sign_up

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.isDigitsOnly
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentBasicPhoneBinding
import com.andre_max.tiktokclone.firebaseAuth
import com.andre_max.tiktokclone.userToken
import com.andre_max.tiktokclone.userVerificationId
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.hbb20.CountryCodePicker
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BasicPhoneFragment : Fragment() {

    lateinit var binding: FragmentBasicPhoneBinding
    private lateinit var ccp: CountryCodePicker
    private lateinit var phoneNumberInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_basic_phone, container, false)
        ccp = binding.countryCodePicker
        phoneNumberInput = binding.phoneNumberInput
        val sendCodeBtn = binding.signUpBtn

        phoneNumberInput.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (phoneNumberInput.text.toString().isNotEmpty()){
                    sendCodeBtn.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.pinkBtnBackground, null))
                    sendCodeBtn.setTextColor(ResourcesCompat.getColor(resources, R.color.tw__solid_white, null))
                }
                else {
                    sendCodeBtn.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.grey_button_background, null))
                    sendCodeBtn.setTextColor(ResourcesCompat.getColor(resources, R.color.textColor, null))
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        sendCodeBtn.setOnClickListener {
            if (phoneNumberInput.text.toString().isEmpty() || !phoneNumberInput.text.toString().isDigitsOnly()) return@setOnClickListener

            sendCode()
        }

        return binding.root
    }

    val timer = object : CountDownTimer(3000, 1000) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            if (firebaseAuth.currentUser == null) {
                findNavController().navigate(BasicSignUpFragmentDirections.actionBasicSignUpFragmentToEnterCodeFragment())
            }
            else{
                Timber.d("Sign in successfully")
            }
        }
    }

    private fun sendCode() {
        val countryCode = ccp.selectedCountryCodeWithPlus
        val actualNumber = countryCode + phoneNumberInput.text.toString().trim()

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            actualNumber,
            60,
            TimeUnit.SECONDS,
            this.requireActivity(),
            callback
        )
    }


    val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            findNavController().navigate(BasicSignUpFragmentDirections.actionBasicSignUpFragmentToCreateUsernameFragment(credential, null, null))
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Timber.e(p0)
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {

            userVerificationId = verificationId
            userToken = token

            timer.start()
        }
    }
}