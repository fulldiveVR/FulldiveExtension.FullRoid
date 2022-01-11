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

package com.swordfish.lemuroid.lib.saves

import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Maybe
import java.io.File

class SavesManager(private val directoriesManager: DirectoriesManager) {
    fun getSaveRAM(game: Game): Maybe<ByteArray> {
        val sramMaybe: Maybe<ByteArray> = Maybe.fromCallable {
            val saveFile = getSaveFile(getSaveRAMFileName(game))
            if (saveFile.exists() && saveFile.length() > 0) {
                saveFile.readBytes()
            } else {
                null
            }
        }
        return sramMaybe.retry(FILE_ACCESS_RETRIES)
    }

    fun setSaveRAM(game: Game, data: ByteArray): Completable {
        val saveCompletable = Completable.fromAction {
            if (data.isEmpty())
                return@fromAction

            val saveFile = getSaveFile(getSaveRAMFileName(game))
            saveFile.writeBytes(data)
        }
        return saveCompletable.retry(FILE_ACCESS_RETRIES)
    }

    fun getSaveRAMInfo(game: Game): SaveInfo {
        val saveFile = getSaveFile(getSaveRAMFileName(game))
        val fileExists = saveFile.exists() && saveFile.length() > 0
        return SaveInfo(fileExists, saveFile.lastModified())
    }

    private fun getSaveFile(fileName: String): File {
        val savesDirectory = directoriesManager.getSavesDirectory()
        return File(savesDirectory, fileName)
    }

    /** This name should make it compatible with RetroArch so that users can freely sync saves across the two application. */
    private fun getSaveRAMFileName(game: Game) = "${game.fileName.substringBeforeLast(".")}.srm"

    companion object {
        private const val FILE_ACCESS_RETRIES = 3L
    }
}
