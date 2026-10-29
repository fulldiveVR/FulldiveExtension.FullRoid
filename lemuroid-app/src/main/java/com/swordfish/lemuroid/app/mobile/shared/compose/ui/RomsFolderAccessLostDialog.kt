package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.settings.RomsFolderAccess
import com.swordfish.lemuroid.app.utils.android.ComposableLifecycle

/**
 * Shown when Android no longer lets us read the ROMs folder (typically after a reinstall or a
 * backup restore). Selecting the folder again restores the grant and rescans it.
 */
@Composable
fun RomsFolderAccessLostDialog(
    folderName: String,
    onSelectFolder: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.roms_folder_lost_title)) },
        text = { Text(stringResource(R.string.roms_folder_lost_message, folderName)) },
        confirmButton = {
            TextButton(onClick = {
                onSelectFolder()
                onDismiss()
            }) {
                Text(stringResource(R.string.roms_folder_lost_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.roms_folder_lost_later))
            }
        },
    )
}

// Re-checked on every resume, so coming back from the folder picker updates the screen.
@Composable
fun rememberRomsFolderLost(): State<Boolean> {
    val context = LocalContext.current.applicationContext
    val lost = remember { mutableStateOf(false) }
    val resumeCount = remember { mutableIntStateOf(0) }
    ComposableLifecycle { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            resumeCount.intValue++
        }
    }
    LaunchedEffect(resumeCount.intValue) {
        lost.value = RomsFolderAccess.isLostInBackground(context)
    }
    return lost
}
