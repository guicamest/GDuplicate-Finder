package com.sleepcamel.gduplicatefinder.app

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class AboutTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `name of the app is displayed`() {
        compose.setContent {
            About(remember { mutableStateOf(true) }, "test")
        }

        // Then
        compose.onNodeWithText(text = "GDuplicateFinder test").assertExists()
    }

    @Test
    fun `link to Github is displayed`() {
        compose.setContent {
            About(remember { mutableStateOf(true) }, "")
        }

        // Then
        compose.onNodeWithText(text = "GitHub").assertExists()
    }

    @Test
    fun `link to Donate is displayed`() {
        compose.setContent {
            About(remember { mutableStateOf(true) }, "")
        }

        // Then
        compose.onNodeWithText(text = "Donate").assertExists()
    }
}