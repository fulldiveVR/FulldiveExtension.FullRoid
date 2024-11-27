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

package com.swordfish.lemuroid.lib.saves

import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.db.entity.Game

/*
   Why does this class exist? Because shit happens and we want to make sure we are prepared.
   This is the issue:

   User enables auto-save, plays, disables auto-save, plays for 10h, saves in game, re-enables
   auto-save and loses 10h worth of game.

   If we detect a more recent SRAM file, we basically avoid loading the state. This is also handy,
   if different cores share the same SRAM file. */
class SavesCoherencyEngine(val savesManager: SavesManager, val statesManager: StatesManager) {
    suspend fun shouldDiscardAutoSaveState(
        game: Game,
        coreID: CoreID,
    ): Boolean {
        val autoSRAM = savesManager.getSaveRAMInfo(game)
        val autoSave = statesManager.getAutoSaveInfo(game, coreID)
        return autoSRAM.exists && autoSave.exists && autoSRAM.date > autoSave.date + TOLERANCE
    }

    companion object {
        private const val TOLERANCE = 30L * 1000L
    }
}
