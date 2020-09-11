package com.andre_max.tiktokclone.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.databinding.FragmentCreateUsernameBinding
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import timber.log.Timber
import kotlin.random.Random


class CreateUsernameFragment : Fragment() {

    lateinit var binding: FragmentCreateUsernameBinding
    var googleProfilePicture: String? = null
    var emailBody: EmailBody? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_username, container, false)
        val credential = CreateUsernameFragmentArgs.fromBundle(requireArguments()).credential
        val googleUsername = CreateUsernameFragmentArgs.fromBundle(requireArguments()).signUpUsername
        googleProfilePicture = CreateUsernameFragmentArgs.fromBundle(requireArguments()).signUpProfilePicture
        emailBody = CreateUsernameFragmentArgs.fromBundle(requireArguments()).emailBody

        val chosenRandomName = if (googleUsername == null) getRandomUsername() else getGoogleUsername(googleUsername)

        binding.usernameInput.setText(chosenRandomName)

        val username = binding.usernameInput
        val errorText = binding.errorText

        binding.usernameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                changeErrorTextValue(username, errorText)
            }

            override fun afterTextChanged(s: Editable?) { }
        })

        binding.signUpBtn.setOnClickListener {
            if (username.text.toString().length < 2 || username.text.toString().contains(" ") || checkIfUsernameExistsInDatabase(username.text.toString()) || username.text.toString().length >= 25){
                makeToast(this.requireContext(), "Username is invalid")
                return@setOnClickListener
            }

            val imm = this.requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(username.windowToken, 0)
            createAccount(credential, username.text.toString())
        }

        binding.skipBtn.setOnClickListener {
            val imm = this.requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(username.windowToken, 0)
            createAccount(credential, chosenRandomName)
        }

        return binding.root
    }

    private fun makeToast(ctx: Context, text: String) {
        Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show()
    }

    private fun getGoogleUsername(googleUsername: String): String {

        var finalName = googleUsername

        while (checkIfUsernameExistsInDatabase(finalName)) {
            finalName = "$googleUsername${Random.nextInt(50, 100_000_000)}"
            Timber.d("finalName in loop is $finalName")
        }
        Timber.d("finalName below loop is $finalName")

        return finalName.trim()
    }

    private fun createTwitterAccount(username: String) {
        val provider = OAuthProvider.newBuilder("twitter.com")
        val pendingResultTask = firebaseAuth.pendingAuthResult

        if (pendingResultTask != null) {
            pendingResultTask.addOnSuccessListener {
                Timber.d("Pending result successful")
                createAccountInDatabase(username, it)

            }
                .addOnFailureListener {
                    Timber.e(it)
                    makeToast(this.requireContext(), "Error occurred")
                }

        }
        else {
            firebaseAuth.startActivityForSignInWithProvider(this.requireActivity(), provider.build())
                .addOnSuccessListener {
                    Timber.d("Normal twitter result successful")
                    createAccountInDatabase(username, it)
                }
                .addOnFailureListener {
                    Timber.e(it)
                    makeToast(this.requireContext(), "Error occurred")
                }
        }
    }

    private fun createAccount(
        credential: AuthCredential?,
        username: String
    ) {
        if (emailBody != null) {
            firebaseAuth.signInWithEmailAndPassword(emailBody!!.email, emailBody!!.password)
                .addOnSuccessListener {
                    Timber.d("Success with email/password authentication")
                    createAccountInDatabase(username, it)
                }
                .addOnFailureListener {
                    Timber.e(it)
                }
        }
        if (credential == null) {
            Timber.d("Credential is null")
            createTwitterAccount(username)
            return
        }
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                Timber.d("Success. Account created in Firebase Authentication. Uid is ${it.user?.uid}")
                createAccountInDatabase(username, it)
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

    private fun createAccountInDatabase(
        username: String,
        it: AuthResult
    ) {
        val user = User(username, 0, 0, 0, googleProfilePicture, it.user?.uid.toString())
        firebaseDatabase.getReference(getUserBasicDataPath()).setValue(user)
            .addOnSuccessListener {
                Timber.d("Success adding user to database")
                (activity as MainActivity).navView.visibility = View.VISIBLE
                registerUserName(username)
                findNavController().navigate(CreateUsernameFragmentDirections.actionCreateUsernameFragmentToHomeFragment())
            }
            .addOnFailureListener { exception ->
                Timber.e(exception)
                Toast.makeText(
                    this.requireContext(),
                    "Error occurred creating account in database. Try again later.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun registerUserName(username: String) {
        firebaseDatabase.getReference("taken-usernames/$username").setValue(username)
            .addOnSuccessListener {
                Timber.d("Success. Registered name: $username")
            }
            .addOnFailureListener {
                Timber.e(it)
            }
    }

    private fun changeErrorTextValue(
        username: EditText,
        errorText: TextView
    ) {
        val nameExists = checkIfUsernameExistsInDatabase(username.text.toString())
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

    private fun getRandomUsername(): String {
        return if (checkIfUsernameExistsInDatabase(generateRandomName())){
            generateRandomName()
        }
        else generateRandomName()
    }

    private fun generateRandomName() : String {
        val initialNameArray = listOf("user", "account", "person")
        val randomUsername = "${initialNameArray[Random.nextInt(initialNameArray.size - 1)]}${Random.nextInt(0, 100_000_000)}"
        Timber.d("random username is $randomUsername")
        return randomUsername
    }

    private fun checkIfUsernameExistsInDatabase(username: String): Boolean {
        val usernameRef = firebaseDatabase.getReference("/taken-usernames/$username")
        var doesExists = false
        usernameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    doesExists = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.message)
            }
        })

       return doesExists
    }
}