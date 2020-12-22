package com.andre_max.tiktokclone.ui.sign_up.create_username

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.custom_views.CustomTextWatcher
import com.andre_max.tiktokclone.databinding.FragmentCreateUsernameBinding
import com.andre_max.tiktokclone.utils.FirebaseAuthUtils
import com.andre_max.tiktokclone.utils.FirestoreUtils
import com.andre_max.tiktokclone.utils.ViewUtils
import com.google.firebase.auth.AuthCredential
import timber.log.Timber
import kotlin.random.Random

class CreateUsernameFragment : Fragment() {

    private val viewModel by viewModels<CreateUsernameViewModel>()
    private val args by navArgs<CreateUsernameFragmentArgs>()

    lateinit var binding: FragmentCreateUsernameBinding
    var googleProfilePicture: String? = null
    var emailBody: EmailBody? = null
    var credential: AuthCredential? = null
    var googleUsername: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateUsernameBinding.inflate(inflater)
        credential = CreateUsernameFragmentArgs.fromBundle(requireArguments()).credential
        googleUsername =
            CreateUsernameFragmentArgs.fromBundle(requireArguments()).signUpUsername
        googleProfilePicture = args.signUpProfilePicture
        emailBody = args.emailBody

        val chosenRandomName =
            googleUsername?.let { viewModel.getGoogleUsername(it) } ?: viewModel.getRandomUsername()

        binding.usernameInput.setText(chosenRandomName)

        val username = binding.usernameInput
        val errorText = binding.errorText

        binding.usernameInput.addTextChangedListener(object : CustomTextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                changeErrorTextValue(username, errorText)
            }
        })

        binding.signUpBtn.setOnClickListener {
            if (username.text.toString().length < 2 ||
                username.text.toString().contains(" ") ||
                FirestoreUtils.doesNameExist(username.text.toString()) ||
                username.text.toString().length >= 25
            ) {
                ViewUtils.showLongSnackBar(requireView(), "Username is invalid")
                return@setOnClickListener
            }

            val imm = this.requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(username.windowToken, 0)
            createAccount(credential, username.text.toString())
        }

        binding.skipBtn.setOnClickListener {
            val imm = this.requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(username.windowToken, 0)
            createAccount(credential, chosenRandomName)
        }

        return binding.root
    }

    private fun createAccount(
        credential: AuthCredential?,
        username: String
    ) {
        if (emailBody != null) {
            firebaseAuth.signInWithEmailAndPassword(emailBody!!.email, emailBody!!.password)
                .addOnSuccessListener {
                    Timber.d("Success with email/password authentication")
//                    FirestoreUtils.createAccountInDatabase("", it, "")
//                        .collect { resultUser ->
//                            resultUser.getOrNull()?.let {
//                                registerUserName(username)
//                                findNavController().navigate(CreateUsernameFragmentDirections.actionCreateUsernameFragmentToHomeFragment())
//                            } ?: run {
//                                ViewUtils.showLongToast(
//                                    requireContext(),
//                                    "Error occurred creating account in database. Try again later."
//                                )
//                            }
//                        }
                }
                .addOnFailureListener {
                    Timber.e(it)
                }
        }
        if (credential == null) {
            Timber.d("Credential is null")
            FirebaseAuthUtils.createTwitterAccount(
                username,
                FirebaseAuthUtils.getTwitterAuthResult(requireActivity())
            ) { isSuccess ->
                if (isSuccess)
                    findNavController().navigate(CreateUsernameFragmentDirections.actionCreateUsernameFragmentToHomeFragment())
                else
                    ViewUtils.showLongToast(
                        requireContext(),
                        "Error occurred creating account in database. Try again later."
                    )
            }
            return
        }
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                Timber.d("Success. Account created in Firebase Authentication. Uid is ${it.user?.uid}")
//                FirestoreUtils.createAccountInDatabase(username, it)
            }
            .addOnFailureListener {
                Timber.e(it)
                Toast.makeText(
                    this.requireContext(),
                    "Error occurred creating user. Try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

//    private fun createAccountInDatabase(
//        username: String,
//        it: AuthResult
//    ) {
//        val user = User(username, 0, 0, 0, googleProfilePicture, it.user?.uid.toString())
//        firebaseDatabase.getReference(getUserBasicDataPath()).setValue(user)
//            .addOnSuccessListener {
//                Timber.d("Success adding user to database")
//                (activity as MainActivity).navView.visibility = View.VISIBLE
//                registerUserName(username)
//                findNavController().navigate(CreateUsernameFragmentDirections.actionCreateUsernameFragmentToHomeFragment())
//            }
//            .addOnFailureListener { exception ->
//                Timber.e(exception)
//                Toast.makeText(
//                    this.requireContext(),
//                    "Error occurred creating account in database. Try again later.",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//    }

//    private fun registerUserName(username: String) {
//        firebaseDatabase.getReference("taken-usernames/$username").setValue(username)
//            .addOnSuccessListener {
//                Timber.d("Success. Registered name: $username")
//            }
//            .addOnFailureListener {
//                Timber.e(it)
//            }
//    }

    private fun changeErrorTextValue(
        username: EditText,
        errorText: TextView
    ) {
        val nameExists = FirestoreUtils.doesNameExist(username.text.toString())
        errorText.visibility = View.VISIBLE
        when {
            username.text.toString().length < 2 -> {
                errorText.text = SHORT_USERNAME
            }
            username.text.toString().contains(" ") -> {
                errorText.text = CONTAINS_SPACES
            }
            nameExists -> {
                errorText.text = NAME_UNAVAILABLE
            }
            username.text.toString().length >= 25 -> {
                errorText.text = LONG_USERNAME
            }
            else -> {
                errorText.visibility = View.GONE
            }
        }
    }

}