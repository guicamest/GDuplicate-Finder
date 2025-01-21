package com.sleepcamel.gduplicatefinder.app

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import org.awaitility.Awaitility.await
import org.junit.Rule
import org.junit.Test
import java.net.URI
import java.util.concurrent.TimeUnit

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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `when there is an error opening a link, a snackbar is displayed`() =
        runComposeUiTest {
            setContent {
                About(
                    showAbout = remember { mutableStateOf(true) },
                    version = "",
                    openLink = { uri: URI -> throw RuntimeException("Failed") },
                    height = 200.dp,
                )
            }

            // Then
            runOnIdle {
                onNodeWithText(text = "GitHub").performClick()
            }

            /*
            Too experimental, has a bug still in v1.7.3 even if it seems to be fixed before
            https://github.com/JetBrains/compose-multiplatform-core/pull/1550/files#diff-5c3cb81e6b1158edec59ef47a6ee055863eb1d7e4a8a35952212ba2be1ceab68L299

            waitUntilAtLeastOneExists(
                hasText("Failed"),
                timeoutMillis = 1000L,
            )
             */
            await().atMost(1, TimeUnit.SECONDS).untilAsserted {
                onNodeWithText(text = "Failed").assertExists()
            }
        }
}
