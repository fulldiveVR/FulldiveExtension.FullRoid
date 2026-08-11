/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.swordfish.lemuroid.R

/**
 * Shown when a game stored inside a .7z archive is launched in the free version.
 *
 * The point of the wording is that the *archive format* is the paid feature, not the console:
 * every emulator is available for free, so a plain "buy Pro" screen reads as if the system itself
 * were locked.
 */
@Composable
fun Archive7zLockedDialog(
    onLearnMore: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.archive_7z_locked_title)) },
        text = { Text(stringResource(R.string.archive_7z_locked_message)) },
        confirmButton = {
            TextButton(onClick = {
                onLearnMore()
                onDismiss()
            }) {
                Text(stringResource(R.string.archive_7z_locked_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
