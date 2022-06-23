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

package com.swordfish.lemuroid.app.utils.games

import android.content.Context
import com.swordfish.lemuroid.app.gamesystem.GameSystemHelper
import com.swordfish.lemuroid.lib.library.db.entity.Game

class GameUtils {

    companion object {
        fun getGameSubtitle(context: Context, game: Game): String {
            val systemName = getSystemNameForGame(context, game)
            val developerName = if (game.developer?.isNotBlank() == true) {
                "- ${game.developer}"
            } else {
                ""
            }
            return "$systemName $developerName"
        }

        private fun getSystemNameForGame(context: Context, game: Game): String {
            val systemTitleResource = GameSystemHelper().findById(game.systemId).shortTitleResId
            return context.getString(systemTitleResource)
        }
    }
}
