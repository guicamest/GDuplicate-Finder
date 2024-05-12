package com.sleepcamel.gduplicatefinder.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "GDuplicateFinder",
            // From https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Window_API_new#adaptive-window-size
            // Does not work as expected :( MenuBar causes issues, with just App it is not working as expected
//         state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
        ) {
            MaterialTheme {
                val aboutDialogVisible = remember { mutableStateOf(false) }
//            val aboutDialogState = rememberDialogState(width = 384.dp, height = 160.dp)

                MenuBar {
                    Menu("Help", 'H', true) {
                        Item("About", mnemonic = 'A', onClick = { aboutDialogVisible.value = true })
                    }
                }
                App()
                // When there is an error -> user closes the dialog while visible, then opens it again, the error is shown, dialog cannot be closed :(
                About(aboutDialogVisible, "v3.0.0-snap") // , state = aboutDialogState)
            }
        }
    }

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}
