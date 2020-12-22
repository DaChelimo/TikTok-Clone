package com.andre_max.tiktokclone.ui.sign_up.create_username

import androidx.lifecycle.ViewModel
import com.andre_max.tiktokclone.utils.FirestoreUtils
import timber.log.Timber
import kotlin.random.Random

class CreateUsernameViewModel: ViewModel() {

    fun getGoogleUsername(googleUsername: String): String {
        var finalName = googleUsername

        while (FirestoreUtils.doesNameExist(finalName)) {
            finalName = "$googleUsername${Random.nextInt(50, 100_000_000)}"
            Timber.d("finalName in loop is $finalName")
        }
        Timber.d("finalName below loop is $finalName")

        return finalName.trim()
    }

    fun getRandomUsername(): String {
        val randomName = generateRandomName()
        return if (FirestoreUtils.doesNameExist(randomName)) {
            randomName
        } else generateRandomName()
    }

    private fun generateRandomName(): String {
        val initialNameArray = listOf("user", "account", "person")
        val randomUsername = "${initialNameArray[Random.nextInt(initialNameArray.size - 1)]}${
            Random.nextInt(
                0,
                100_000_000
            )
        }"
        Timber.d("random username is $randomUsername")
        return randomUsername
    }

}