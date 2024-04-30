package com.sleepcamel.gduplicatefinder.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import java.awt.Desktop
import java.net.URI
import java.util.Locale

@Composable
fun About(showAbout: MutableState<Boolean>, version: String) {
    DialogWindow(
            onCloseRequest = { showAbout.value = false },
            visible = showAbout.value,
            title = "About",
            state = rememberDialogState(width = 384.dp, height = 160.dp)
        ) {
        Column(
            Modifier.fillMaxSize().then(Modifier.padding(14.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row {
                Text(
                    text = "GDuplicateFinder $version",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Row {
                FilledTonalButton(onClick = {
                    openInBrowser(URI.create("https://github.com/guicamest/GDuplicate-Finder"))
                }) {
                    Text("GitHub")
                }
                Button(onClick = {
                    openInBrowser(URI.create("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=fezuqqg9t6j6y"))
                }, Modifier.padding(start = 20.dp)) {
                    Text("Donate")
                }
            }
        }
    }
}

private val osName by lazy(LazyThreadSafetyMode.NONE) { System.getProperty("os.name").lowercase(Locale.getDefault()) }
private fun openInBrowser(uri: URI) {
    // Try catch IOException in case `exec` fails
    when {
        Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) -> Desktop.getDesktop().browse(uri)
        "mac" in osName -> Runtime.getRuntime().exec("open $uri")
        "nix" in osName || "nux" in osName -> Runtime.getRuntime().exec("xdg-open $uri")
        else -> { } // Show toast
    }
}

@Preview
@Composable
fun AboutPreview() {
    About(remember { mutableStateOf(true) }, "v3.0.0-snap")
}
