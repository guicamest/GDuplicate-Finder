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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.net.URI
import java.util.Locale

private const val DONATE_URL =
    "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=fezuqqg9t6j6y"

@Composable
fun About(
    showAbout: MutableState<Boolean>,
    version: String,
    openLink: (URI) -> Unit = ::openInBrowser,
) {
    DialogWindow(
        onCloseRequest = { showAbout.value = false },
        visible = showAbout.value,
        title = "About",
        state = rememberDialogState(width = 384.dp, height = 160.dp),
    ) {
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
        ) {
            aboutContent(version, openLink, scope.handleError(snackbarHostState))
        }
    }
}

@Composable
private fun aboutContent(
    version: String,
    openFn: (URI) -> Unit,
    onOpenLinkError: (e: Exception) -> Unit,
) {
    Column(
        Modifier.fillMaxSize().then(Modifier.padding(14.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row {
            Text(
                text = "GDuplicateFinder $version",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Row {
            FilledTonalButton(onClick = {
                tryOpenInBrowser(
                    uri = URI.create("https://github.com/guicamest/GDuplicate-Finder"),
                    openFn = openFn,
                    onOpenLinkError = onOpenLinkError,
                )
            }) {
                Text("GitHub")
            }
            Button(onClick = {
                tryOpenInBrowser(
                    uri = URI.create(DONATE_URL),
                    openFn = openFn,
                    onOpenLinkError = onOpenLinkError,
                )
            }, Modifier.padding(start = 20.dp)) {
                Text("Donate")
            }
        }
    }
}

private val osName by lazy(LazyThreadSafetyMode.NONE) {
    System.getProperty("os.name").lowercase(Locale.getDefault())
}

private fun CoroutineScope.handleError(snackbarHostState: SnackbarHostState): (e: Exception) -> Unit =
    {
        launch {
            snackbarHostState.showSnackbar(
                message = it.message ?: "Error happened",
                duration = SnackbarDuration.Short,
            )
        }
    }

private fun tryOpenInBrowser(
    uri: URI,
    openFn: (URI) -> Unit,
    onOpenLinkError: (e: Exception) -> Unit,
) = try {
    // Try catch IOException in case `exec` fails
    openFn(uri)
} catch (e: Exception) {
    onOpenLinkError(e)
}

private fun openInBrowser(uri: URI) {
    when {
        Desktop.isDesktopSupported() &&
            Desktop.getDesktop().isSupported(
                Desktop.Action.BROWSE,
            )
        -> Desktop.getDesktop().browse(uri)
        "mac" in osName -> Runtime.getRuntime().exec(arrayOf("open", uri.toString()))
        "nix" in osName || "nux" in osName -> Runtime.getRuntime().exec(arrayOf("xdg-open", uri.toString()))
        else -> { } // Show toast
    }
}

@Preview
@Composable
fun AboutPreview() {
    About(remember { mutableStateOf(true) }, "v3.0.0-snap")
}
