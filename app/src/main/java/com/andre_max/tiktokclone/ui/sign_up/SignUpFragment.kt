package com.andre_max.tiktokclone.ui.sign_up

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.SignUpPageBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import timber.log.Timber

class SignUpFragment: Fragment() {

    lateinit var binding: SignUpPageBinding
    lateinit var callbackManager: CallbackManager
    lateinit var mTwitterBtn: TwitterLoginButton
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var fbLogin: LoginManager
    private val mAuthListener = FirebaseAuth.AuthStateListener { p0 -> if (p0.currentUser != null) Timber.i("Sign in succeeded") else Timber.i("Sign in failed") }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val mTwitterAuthConfig = TwitterAuthConfig(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret))
        val twitterConfig = TwitterConfig.Builder(this.requireContext())
            .twitterAuthConfig(mTwitterAuthConfig)
            .build()

        Twitter.initialize(twitterConfig)
        Timber.d("Test Log")


        binding = DataBindingUtil.inflate(layoutInflater, R.layout.sign_up_page, null, false)

        (activity as MainActivity).navView.visibility = View.GONE

        callbackManager = CallbackManager.Factory.create()
        firebaseAuth.addAuthStateListener(mAuthListener)

        mTwitterBtn = binding.signUpTwitterBtn

        val twitterCallback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {
                Toast.makeText(
                    this@SignUpFragment.requireContext(),
                    "Successful sign in to Twitter",
                    Toast.LENGTH_SHORT
                ).show()
                Timber.d("Twitter sign in successful")
                signInToFirebaseWithTwitterSession(result?.data)
            }

            override fun failure(exception: TwitterException?) {
                Timber.e(exception)

                findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToCreateUsernameFragment(null, null, null))
                Toast.makeText(
                    this@SignUpFragment.requireContext(),
                    "Failure occurred. No!!!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        mTwitterBtn.callback = twitterCallback

        fbLogin = LoginManager.getInstance()


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(CLIENT_ID_TYPE_3)
            .requestProfile()
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this.requireContext(), gso)

        Timber.d("binding.signUpPage.root.signUpGoogleBtn is ${binding.signUpGoogleBtn}")

        binding.signUpUsePhoneBtn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_ageFragment)
        }

        binding.signUpCancelBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.signUpFacebookBtn.setOnClickListener {
            doFacebookLogin()
        }


        binding.signUpGoogleBtn.setOnClickListener {
            doGoogleLogin()
        }

        return binding.root
    }

        private fun doGoogleLogin() {
            Timber.d("Do google login")
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_RC_SIGN_IN)
        }

        private fun signInToFirebaseWithTwitterSession(twitterSession: TwitterSession?) {
            if (twitterSession?.authToken?.token == null || twitterSession.authToken?.secret == null){
                Timber.i("twitterSession?.authToken?.token is ${twitterSession?.authToken?.token} and twitterSession?.authToken?.secret is ${twitterSession?.authToken?.secret}")
            }
            val credential = TwitterAuthProvider.getCredential(twitterSession?.authToken?.token!!, twitterSession.authToken?.secret!!)

            findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToCreateUsernameFragment(credential, null, null))
//            firebaseAuth.signInWithCredential(credential)
//                .addOnSuccessListener {
//                    Toast.makeText(this.requireContext(), "Pure twitter success", Toast.LENGTH_SHORT).show()
//                    Timber.d("Pure twitter success")
//                }
//                .addOnFailureListener {
//                    Toast.makeText(this.requireContext(), it.message, Toast.LENGTH_SHORT).show()
//                    Timber.e(it)
//                }
        }

        private fun doFacebookLogin() {//binding.signUpFacebookBtn
            //            fbLogin.setPermissions("email")
            fbLogin.logInWithReadPermissions(this, listOf("email"))
            //            fbLogin.logIn()
            //            fbLogin.fragment = this

            fbLogin.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Timber.d("facebook:onSuccess:$loginResult")
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    // App code
                }

                override fun onError(exception: FacebookException) {
                    // App code
                }
            })
        }

        private fun handleFacebookAccessToken(token: AccessToken){
            val credential = FacebookAuthProvider.getCredential(token.token)

            findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToCreateUsernameFragment(credential, null, null))
//            firebaseAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this.requireActivity()) { task ->
//                    if (task.isSuccessful) {
//                        // Sign in success, update UI with the signed-in user's information
//                        Timber.d("signInWithCredential:success with ${firebaseAuth.currentUser}")
////                        val user = firebaseAuth.currentUser
//                    } else {
//                        // If sign in fails, display ViewHolder message to the user.
//                        Timber.e(task.exception)
//                        Toast.makeText(
//                            this.requireContext(), "Authentication failed.",
//                            Toast.LENGTH_SHORT
//                        ).show()
////                    updateUI(null)
//                    }
//                }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            callbackManager.onActivityResult(requestCode, resultCode, data)
            mTwitterBtn.onActivityResult(requestCode, resultCode, data)

            if (requestCode == GOOGLE_RC_SIGN_IN) {
                try{

                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)

                    val account = task.getResult(ApiException::class.java)

                    val credential = GoogleAuthProvider.getCredential(account?.idToken, null)

                    val googleUsername = account?.displayName
                    val googleProfilePicture = account?.photoUrl

                    findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToCreateUsernameFragment(credential, googleUsername, googleProfilePicture.toString()))
//                    authFirebaseWithGoogle(account)

                }
                catch (e : ApiException){
                    Timber.e(e)
                }
            }
        }

        private fun authFirebaseWithGoogle(account: GoogleSignInAccount?) {

//            firebaseAuth.signInWithCredential(credential)
//                .addOnSuccessListener {
//                    Timber.d("Success with Google")
//                }
//                .addOnFailureListener {
//                    Timber.e(it)
//                }
        }


    override fun onDestroy() {
        (activity as MainActivity).navView.visibility = View.VISIBLE
        super.onDestroy()
        firebaseAuth.removeAuthStateListener(mAuthListener)
    }

}

// SHA1: 1B:B6:A5:88:E1:E7:83:61:D9:36:E8:C7:AC:12:DE:50:64:6E:3C:0B
// FIREBASE SHA1: dc:93:70:9f:74:9a:31:ea:75:3c:c7:99:70:8c:3d:17:93:a7:3f:90