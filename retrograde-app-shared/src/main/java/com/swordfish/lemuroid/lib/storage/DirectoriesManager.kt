/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package com.swordfish.lemuroid.lib.storage

import android.content.Context
import java.io.File

class DirectoriesManager(private val appContext: Context) {

    @Deprecated("Use the external states directory")
    fun getInternalStatesDirectory(): File = File(appContext.filesDir, "states").apply {
        mkdirs()
    }

    fun getCoresDirectory(): File = File(appContext.filesDir, "cores").apply {
        mkdirs()
    }

    fun getSystemDirectory(): File = File(appContext.filesDir, "system").apply {
        mkdirs()
    }

    fun getStatesDirectory(): File = File(appContext.getExternalFilesDir(null), "states").apply {
        mkdirs()
    }

    fun getStatesPreviewDirectory(): File = File(appContext.getExternalFilesDir(null), "state-previews").apply {
        mkdirs()
    }

    fun getSavesDirectory(): File = File(appContext.getExternalFilesDir(null), "saves").apply {
        mkdirs()
    }

    fun getInternalRomsDirectory(): File = File(appContext.getExternalFilesDir(null), "roms").apply {
        mkdirs()
    }
}
