package com.andre_max.tiktokclone

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert
import org.junit.Test

class SuggestionsUnitTests {
    lateinit var mockSuggestions: Suggestions

    @Test
    fun testSuggestions() {
        mockSuggestions = mock()

        val suggestions = mockSuggestions.getSuggestions()

        verify(mockSuggestions).getSuggestions()

        Assert.assertArrayEquals(arrayOf<User>(), suggestions.toArray())
//        Assert.assertTrue("List is empty", suggestions.isNotEmpty())
    }
}