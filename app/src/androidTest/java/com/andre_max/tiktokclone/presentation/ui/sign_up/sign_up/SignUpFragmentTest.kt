package com.andre_max.tiktokclone.presentation.ui.sign_up.sign_up

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.andre_max.tiktokclone.R
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class SignUpFragmentTest {

    val mockNavController = mock(NavController::class.java)

    @Test
    fun whenClickOnPhoneSignInBtn_fragment_navigates() {
        val signUpFragmentScenario = launchFragmentInContainer<SignUpFragment>()

        //
        Espresso.onView(withId(R.id.sign_up_use_phone_btn)).perform(click())
        verify(mockNavController).navigate(SignUpFragmentDirections.actionSignUpFragmentToAgeFragment())
    }

}