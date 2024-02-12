
package com.sleepcamel.gduplicatefinder.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppKtTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun buttonIsDisplayed() {
        val dispatcher = StandardTestDispatcher()
        val scope = TestScope(dispatcher)
        scope.runTest {
            // Given

            // When
            compose.setContent {
                App()
            }
            compose.awaitIdle()
            // Then
            compose.onNodeWithText(text = "Click", substring = true)
                .assertExists()
            compose.onNodeWithText(text = "Some Content")
                .assertDoesNotExist()
        }
    }

    @Test
    fun `when button is clicked, content is displayed`() {
        val dispatcher = StandardTestDispatcher()
        val scope = TestScope(dispatcher)
        scope.runTest {
            // Given

            // When
            compose.setContent {
                App()
            }
            compose.awaitIdle()
            // Then

            compose.onNodeWithText(text = "Click", substring = true)
                .performClick()
            compose.onNodeWithText(text = "Some Content")
                .assertExists()
        }
    }
}