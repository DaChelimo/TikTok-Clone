package com.andre_max.tiktokclone.presentation.ui.sign_up.create_username

import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.fakes.repo.network.user.FakeNameRepo
import com.andre_max.tiktokclone.test_utils.CoroutineTestRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateUsernameViewModelTest {

    lateinit var viewModel: CreateUsernameViewModel
    lateinit var fakeNameRepo: FakeNameRepo
    private val alreadyExistingNames = mutableListOf("Andrew@2021", "Bob@1234", "Steve")
    private val testExistingName = alreadyExistingNames.first()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Before
    fun setUpViewModel() {
        fakeNameRepo = FakeNameRepo(alreadyExistingNames)
        viewModel = CreateUsernameViewModel(fakeNameRepo)
    }

    @Test
    fun checkUsernameIsValid_withShortName_returnsShortStringRes() = runBlockingTest {
        viewModel.liveUsername.value = "abc"
        assertThat(viewModel.checkUsernameIsValid()).isEqualTo(R.string.short_username)
    }

    @Test
    fun checkUsernameIsValid_withSpace_returnsSpaceRes() = runBlockingTest {
        viewModel.liveUsername.value = "Andrew Chelix"
        assertThat(viewModel.checkUsernameIsValid()).isEqualTo(R.string.name_contains_spaces)
    }

    @Test
    fun checkUsernameIsValid_withLongName_returnsLongStringRes() = runBlockingTest {
        viewModel.liveUsername.value = "somerandomandlongnamethatIdontexpectyouread"
        assertThat(viewModel.checkUsernameIsValid()).isEqualTo(R.string.long_username)
    }

    @Test
    fun checkUsernameIsValid_withExistingName_returnsUnavailableRes() = runBlockingTest {
        viewModel.liveUsername.value = testExistingName
        assertThat(viewModel.checkUsernameIsValid()).isEqualTo(R.string.name_unavailable)
    }

    @Test
    fun checkUsernameIsValid_withValidName_returnsNull() = runBlockingTest {
        viewModel.liveUsername.value = "Jake_Wharton"
        assertThat(viewModel.checkUsernameIsValid()).isNull()
    }
}