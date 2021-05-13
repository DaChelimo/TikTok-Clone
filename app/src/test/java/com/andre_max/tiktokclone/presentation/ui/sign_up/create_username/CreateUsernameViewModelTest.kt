/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andre_max.tiktokclone.presentation.ui.sign_up.create_username

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.ui.auth.create_username.CreateUsernameViewModel
import com.andre_max.tiktokclone.repo.network.name.DefaultNameRepo
import com.andre_max.tiktokclone.test_utils.CoroutineTestRule
import com.google.common.truth.Truth.assertThat
import com.google.firebase.database.FirebaseDatabase
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class CreateUsernameViewModelTest {

    private lateinit var viewModel: CreateUsernameViewModel

    private lateinit var nameRepo: DefaultNameRepo

//    @Mock private lateinit var userRepo: UserRepo
//    @Mock private lateinit var authRepo: AuthRepo
    @Mock private lateinit var realFire: FirebaseDatabase

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Before
    fun setUpViewModel() = runBlockingTest {
        MockitoAnnotations.initMocks(this)
        nameRepo = spy(DefaultNameRepo(realFire))

        viewModel = CreateUsernameViewModel(nameRepo, mock(), mock())
        viewModel.liveUsername.observeForever {}
        viewModel.errorTextRes.observeForever {}

        doAnswer {
            val inputUsername = it.arguments[0] as String
            return@doAnswer alreadyExistingNames.contains(inputUsername)
        }.whenever(nameRepo.doesNameExist(anyString()))
    }

    @Test
    fun registerUsername_withValidName_worksCorrectly() = runBlockingTest {
        // WHEN registering a new username
        viewModel.registerUserName(testNewName)

        // THEN the nameRepo.registerUsername() is called
        verify(realFire.getReference(anyString()).setValue(testNewName))
    }

    @Test
    fun registerUsername_withInvalidName_fails() = runBlockingTest {
        // WHEN registering a new username
        viewModel.registerUserName(testExistingName)

        // THEN the nameRepo.registerUsername() is never called
        verify(realFire.getReference(anyString()).setValue(testNewName), never())
    }

    @Test
    fun generateRandomName_isReallyRandom() = runBlockingTest {
        viewModel.generateRandomName()
        assertThat(viewModel.liveUsername.value).isNotEmpty()
    }

    @Test
    fun checkUsernameIsValid_withShortName_returnsShortStringRes() = runBlockingTest {
        viewModel.liveUsername.value = "abc"
        assertThat(viewModel.errorTextRes.value).isEqualTo(R.string.short_username)
    }

    @Test
    fun checkUsernameIsValid_withSpace_returnsSpaceRes() = runBlockingTest {
        viewModel.liveUsername.value = "Andrew Chelix"
        assertThat(viewModel.errorTextRes.value).isEqualTo(R.string.name_contains_spaces)
    }

    @Test
    fun checkUsernameIsValid_withLongName_returnsLongStringRes() = runBlockingTest {
        viewModel.liveUsername.value = "somerandomandlongnamethatIdontexpectyouread"
        assertThat(viewModel.errorTextRes.value).isEqualTo(R.string.long_username)
    }

    @Test
    fun checkUsernameIsValid_withExistingName_returnsUnavailableRes() = runBlockingTest {
        viewModel.liveUsername.value = testExistingName
        assertThat(viewModel.errorTextRes.value).isEqualTo(R.string.name_unavailable)
    }

    @Test
    fun checkUsernameIsValid_withValidName_returnsNull() = runBlockingTest {
        viewModel.liveUsername.value = "Jake_Wharton"
        assertThat(viewModel.errorTextRes.value).isNull()
    }

    companion object {
        private val alreadyExistingNames = mutableListOf("Andrew@2021", "Bob@1234", "Steve")
        private val testExistingName = alreadyExistingNames.first()
        private const val testNewName = "Jake001"
    }
}