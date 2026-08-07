/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.network.username

import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Screen 4's data layer. MockK is used **only here**, at the Firebase boundary (spec §4.4,
 * docs/SPEC.md §8) — the use-case and ViewModel tests use `FakeUsernameRepo` instead.
 *
 * Every assertion is on the mapped `Result`/`DataError`. A leaked `FirebaseException` reaching
 * a caller is the bug these tests exist to prevent.
 */
class DefaultUsernameRepoTest {

    private val reference = mockk<DatabaseReference>(relaxed = true)
    private val firebaseDatabase = mockk<FirebaseDatabase>(relaxed = true) {
        every { getReference(any()) } returns reference
    }
    private val repo = DefaultUsernameRepo(firebaseDatabase)

    /**
     * `FirebaseNetworkException`'s constructor calls `TextUtils.isEmpty`, which is not available
     * in a plain JVM unit test, so the exception is mocked rather than constructed.
     */
    private fun networkException(): FirebaseNetworkException = mockk(relaxed = true)

    private fun snapshotTask(exists: Boolean): Task<DataSnapshot> {
        val snapshot = mockk<DataSnapshot>(relaxed = true) {
            every { this@mockk.exists() } returns exists
        }
        return Tasks.forResult(snapshot)
    }

    @Test
    fun `a username with no entry in the taken-names node is available`() = runTest {
        every { reference.get() } returns snapshotTask(exists = false)

        val result = repo.isUsernameAvailable("andrew_max")

        assertThat(result).isEqualTo(Result.Success(true))
    }

    @Test
    fun `a username already present in the taken-names node is unavailable`() = runTest {
        every { reference.get() } returns snapshotTask(exists = true)

        val result = repo.isUsernameAvailable("andrew_max")

        assertThat(result).isEqualTo(Result.Success(false))
    }

    @Test
    fun `a Firebase network failure maps to DataError Network NO_INTERNET`() = runTest {
        every { reference.get() } returns Tasks.forException(networkException())

        val result = repo.isUsernameAvailable("andrew_max")

        assertThat(result).isEqualTo(Result.Error(DataError.Network.NO_INTERNET))
    }

    @Test
    fun `an unrecognised failure maps to DataError Network UNKNOWN and not a Firebase type`() =
        runTest {
            every { reference.get() } returns Tasks.forException(IllegalStateException("boom"))

            val result = repo.isUsernameAvailable("andrew_max")

            assertThat(result).isEqualTo(Result.Error(DataError.Network.UNKNOWN))
        }

    @Test
    fun `claiming a username succeeds when the write completes`() = runTest {
        every { reference.setValue(any()) } returns Tasks.forResult(null)

        val result = repo.claimUsername("andrew_max")

        assertThat(result).isEqualTo(Result.Success(Unit))
    }

    @Test
    fun `claiming a username maps a failed write to DataError Network`() = runTest {
        every { reference.setValue(any()) } returns Tasks.forException(networkException())

        val result = repo.claimUsername("andrew_max")

        assertThat(result).isEqualTo(Result.Error(DataError.Network.NO_INTERNET))
    }
}
