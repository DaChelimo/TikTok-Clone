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

package com.andre_max.tiktokclone.presentation.ui.auth.age_verification

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.andre_max.tiktokclone.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

@RunWith(AndroidJUnit4::class)
class AgeFragmentTest {

    lateinit var ageFragmentScenario: FragmentScenario<AgeFragment>

    @get:Rule
    val mockTestRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockNavController: NavController

    @Before
    fun setUp() {
        ageFragmentScenario = launchFragmentInContainer()
        ageFragmentScenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }
    }

    @Test
    fun continueWithSignUp_withCorrectAge_works() {
        // GIVEN a correct age
        onView(withId(R.id.date_picker2)).perform(PickerActions.setDate(1990, 1, 1))

        //WHEN the sign up button is clicked
        onView(withId(R.id.sign_up_btn)).perform(click())

        // THEN app navigates
        verify(AgeFragmentDirections.actionAgeFragmentToSelectBasicSignUpFragment())
    }

    @Test
    fun continueWithSignUp_withYoungAge_fails() {
        // GIVEN a young age
        onView(withId(R.id.date_picker2)).perform(PickerActions.setDate(2020, 1, 1))

        //WHEN the sign up button is clicked
        onView(withId(R.id.sign_up_btn)).perform(click())

        // THEN a snackbar is shown and the app does not navigate
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.user_too_young)))
        verify(AgeFragmentDirections.actionAgeFragmentToSelectBasicSignUpFragment(), never())
    }

    @Test
    fun continueWithSignUp_withOldAge_fails() {
        // GIVEN a old age
        onView(withId(R.id.date_picker2)).perform(PickerActions.setDate(1800, 1, 1))

        //WHEN the sign up button is clicked
        onView(withId(R.id.sign_up_btn)).perform(click())

        // THEN a snackbar is shown and the app does not navigate
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.enter_valid_year)))
        verify(AgeFragmentDirections.actionAgeFragmentToSelectBasicSignUpFragment(), never())
    }
}