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
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy

class GameLauncher(private val coresSelection: CoresSelection) {

    fun launchGameAsync(activity: Activity, game: Game, loadSave: Boolean, leanback: Boolean) {
        val system = GameSystem.findById(game.systemId)
        coresSelection.getCoreConfigForSystem(system)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy {
                BaseGameActivity.launchGame(activity, it, game, loadSave, leanback)
            }
    }
}
