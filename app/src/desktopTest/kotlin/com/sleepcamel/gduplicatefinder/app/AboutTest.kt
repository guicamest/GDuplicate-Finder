package com.sleepcamel.gduplicatefinder.app

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.net.URI

class AboutTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `name of the app is displayed`() {
        compose.setContent {
            About(remember { mutableStateOf(true) }, "test") // , rememberDialogState(),)
        }

        // Then
        compose.onNodeWithText(text = "GDuplicateFinder test").assertExists()
    }

    @Test
    fun `link to Github is displayed`() {
        compose.setContent {
            About(remember { mutableStateOf(true) }, "") // , rememberDialogState(),)
        }

        // Then
        compose.onNodeWithText(text = "GitHub").assertExists()
    }

    @Test
    fun `link to Donate is displayed`() {
        compose.setContent {
            About(remember { mutableStateOf(true) }, "") // , rememberDialogState(),)
        }

        // Then
        compose.onNodeWithText(text = "Donate").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `when there is an error opening a link, a snackbar is displayed`() =
        runComposeUiTest {
            setContent { }
            setContent {
                About(
                    showAbout = remember { mutableStateOf(true) },
                    version = "",
//                state = rememberDialogState(),
                    openLink = { uri: URI -> throw RuntimeException("Failed") },
                )
            }
            runBlocking { awaitIdle() }
            // Then
            onNodeWithText(text = "GitHub").performClick()
            waitUntilAtLeastOneExists(
                hasText("Failed"),
                timeoutMillis = 1000L,
            )
        }
}
