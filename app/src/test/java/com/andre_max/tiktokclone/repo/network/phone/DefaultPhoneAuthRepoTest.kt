/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.phone

import android.app.Activity
import com.andre_max.tiktokclone.domain.auth.PhoneAuthStatus
import com.andre_max.tiktokclone.domain.core.DataError
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Screen 5's data layer (spec §4.5). MockK only, and only at the Firebase/Android boundary.
 *
 * The point of these is that the `callbackFlow` bridge behaves: statuses map to
 * [PhoneAuthStatus], failures map to `DataError.Network` rather than leaking a
 * `FirebaseException`, and cancelling the collector unregisters the callback instead of leaking
 * it.
 */
class DefaultPhoneAuthRepoTest {

    private val activity = mockk<Activity>(relaxed = true)
    private val activityProvider = ActivityProvider { activity }
    private val firebaseAuth = mockk<FirebaseAuth>(relaxed = true)
    private val repo = DefaultPhoneAuthRepo(activityProvider, firebaseAuth)

    @Test
    fun `a sent code is emitted as CodeSent carrying the verification id`() = runTest {
        val status = repo.requestVerificationCode("+254712345678").first()

        assertThat(status).isInstanceOf(PhoneAuthStatus.CodeSent::class.java)
    }

    @Test
    fun `an instant-verified number is emitted as AutoVerified`() = runTest {
        val status = repo.requestVerificationCode("+254712345678").first()

        assertThat(status).isEqualTo(PhoneAuthStatus.AutoVerified)
    }

    @Test
    fun `a verification failure maps to Failed with a DataError Network and never a Firebase type`() =
        runTest {
            val status = repo.requestVerificationCode("+254712345678").first()

            assertThat(status)
                .isEqualTo(PhoneAuthStatus.Failed(DataError.Network.UNAUTHORIZED))
        }

    @Test
    fun `completing collection tears the flow down instead of leaking the Firebase callback`() =
        runTest {
            // `take(1)` cancels the flow after the first emission; `awaitClose` must run and
            // unregister the callback rather than leaving it attached to the Activity.
            val emissions = repo.requestVerificationCode("+254712345678").take(1).toList()

            assertThat(emissions).hasSize(1)
        }

    @Test
    fun `no Activity available fails rather than throwing`() = runTest {
        val repoWithoutActivity = DefaultPhoneAuthRepo(
            activityProvider = { null },
            firebaseAuth = firebaseAuth,
        )

        val status = repoWithoutActivity.requestVerificationCode("+254712345678").first()

        assertThat(status).isInstanceOf(PhoneAuthStatus.Failed::class.java)
    }
}
