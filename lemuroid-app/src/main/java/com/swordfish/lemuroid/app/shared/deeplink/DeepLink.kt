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

package com.swordfish.lemuroid.app.shared.deeplink

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.swordfish.lemuroid.lib.library.db.entity.Game

object DeepLink {

    fun openLeanbackUri(appContext: Context): Uri {
        return Uri.parse("lemuroid://${appContext.packageName}/open-leanback")
    }

    private fun uriForGame(appContext: Context, game: Game): Uri {
        return Uri.parse("lemuroid://${appContext.packageName}/play-game/id/${game.id}")
    }

    fun launchIntentForGame(appContext: Context, game: Game) =
        Intent(Intent.ACTION_VIEW, uriForGame(appContext, game))
}
