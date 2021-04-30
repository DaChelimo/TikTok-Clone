package com.andre_max.tiktokclone.presentation.ui.sign_up.create_username

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.*
import com.andre_max.tiktokclone.models.succeeded
import com.andre_max.tiktokclone.repo.network.auth.AuthRepo
import com.andre_max.tiktokclone.repo.network.user.INameRepo
import com.andre_max.tiktokclone.repo.network.user.NameRepo
import com.andre_max.tiktokclone.repo.network.user.UserRepo
import com.andre_max.tiktokclone.utils.viewModel.BaseViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class CreateUsernameViewModel(private val nameRepo: INameRepo = NameRepo()) : BaseViewModel() {
    private val authRepo = AuthRepo()
    private val userRepo = UserRepo()

    lateinit var args: CreateUsernameFragmentArgs

    val liveUsername = MutableLiveData("")

    private val _errorTextRes = MutableLiveData<Int?>()
    val errorTextRes: LiveData<Int?> = _errorTextRes


    fun setUp(navArgs: CreateUsernameFragmentArgs) {
        viewModelScope.launch {
            args = navArgs
            args.googleBody?.userName?.let {
                getUsernameFromGoogleName(it)
            }
        }
    }

    suspend fun completeSignIn() {
        viewModelScope.launch {
            // Username will not be null since we've already done our check. The safety check is because I dislike the non-null assertion mark
            val username = liveUsername.value ?: ""
            val authResult = getAuthResult() ?: return@launch
            userRepo.addUserToDatabase(username, authResult, args.googleBody?.profilePicture)
        }
    }

    /**
     * Gets an authResult based on the type of sign up used. In all scenarios except using email sign up,
     * we have a credential while in email sign up, we have the email info we need.
     * We create the user at the last stage to prevent creating a void user
     * in case he/she leaves the app mid-way during the sign-up process.
     */
    private suspend fun getAuthResult() = when {
        args.credential != null -> {
            val authResult = authRepo.signInWithCredential(args.credential!!)
            if (!authResult.succeeded)
                showMessage(R.string.error_during_account_creation)
            authResult.getData()
        }
        args.emailBody != null -> {
            val emailBody = args.emailBody!!
            Firebase.auth.createUserWithEmailAndPassword(emailBody.email, emailBody.password).await()
        }
        else -> throw UnknownError("There is no credential or emailBody. Call the 911, a bug has occurred(pun intended)")
    }


    /**
     * Generates a name from the user's google account name
     * @return A name derived from the user's google account name
     */
    private suspend fun getUsernameFromGoogleName(googleName: String) {
        var userName = googleName

        while (nameRepo.doesNameExist(userName)) {
            val randomNumber = Random.nextInt(100_000_000).toString()
            userName = googleName + randomNumber
        }

        liveUsername.value = userName
    }

    /**
     * Generates a random name that is not in the database
     * @return A random name to display to the user as the initial option
     */
    suspend fun generateRandomName() {
        var randomUserName = ""
        val initialNameArray = listOf("user", "account", "person")

        while (nameRepo.doesNameExist(randomUserName) || randomUserName == "") {
            val randomNumber = Random.nextInt(100_000_000).toString()
            randomUserName = initialNameArray.random() + randomNumber
        }

        liveUsername.value = randomUserName
    }

    /**
     * Checks if the username meets the needed requirements
     * @return True when the username is valid
     */
    suspend fun checkUsernameIsValid(): Boolean {
        val username = liveUsername.value ?: ""
        val errorRes = when {
            username.length < 4 -> R.string.short_username
            username.contains(" ") -> R.string.name_unavailable
            username.length >= 25 -> R.string.name_unavailable
            nameRepo.doesNameExist(username) -> R.string.name_unavailable
            else -> null
        }

        _errorTextRes.value = errorRes
        return errorRes == null
    }
}