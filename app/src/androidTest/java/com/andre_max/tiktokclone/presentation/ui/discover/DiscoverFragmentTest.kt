package com.andre_max.tiktokclone.presentation.ui.discover

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.andre_max.tiktokclone.R
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


class DiscoverFragmentTest {

    private val mockNavController = mock(NavController::class.java)

    @Test
    fun whenSearchInputBtnClick_navigateCalled() {
        // GIVEN a discoverFragment
        val discoverFragmentScenario = launchFragmentInContainer<DiscoverFragment>()
        discoverFragmentScenario.onFragment { fragment ->
            Navigation.setViewNavController(fragment.requireView(), mockNavController)
        }

        // WHEN the searchInput button is clicked
        Espresso.onView(withId(R.id.search_input)).perform(click())

        // THEN the app navigates to SearchPageFragment
        verify(mockNavController)
            .navigate(
                DiscoverFragmentDirections.actionSearchFragmentToSearchPageFragment()
            )
    }

}