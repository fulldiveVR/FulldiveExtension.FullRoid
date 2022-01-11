/*
 *  RetrogradeApplicationComponent.kt
 *
 *  Copyright (C) 2017 Retrograde Project
 *
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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.displayErrorDialog
import com.swordfish.lemuroid.lib.game.GameLoaderError
import com.swordfish.lemuroid.lib.library.SystemCoreConfig

fun Activity.displayGameLoaderError(gameError: GameLoaderError, coreConfig: SystemCoreConfig) {

    val messageId = when (gameError) {
        GameLoaderError.GL_INCOMPATIBLE -> getString(R.string.game_loader_error_gl_incompatible)
        GameLoaderError.GENERIC -> getString(R.string.game_loader_error_generic)
        GameLoaderError.LOAD_CORE -> getString(R.string.game_loader_error_load_core)
        GameLoaderError.LOAD_GAME -> getString(R.string.game_loader_error_load_game)
        GameLoaderError.SAVES -> getString(R.string.game_loader_error_save)
        GameLoaderError.MISSING_BIOS -> getString(
            R.string.game_loader_error_missing_bios,
            coreConfig.requiredBIOSFiles.joinToString(", ")
        )
    }

    this.displayErrorDialog(messageId, getString(R.string.ok)) { finish() }
}
