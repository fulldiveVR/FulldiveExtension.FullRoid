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

package com.swordfish.lemuroid.app.shared

import android.view.ContextMenu
import android.view.View
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.library.db.entity.Game

class GameContextMenuListener(
    private val gameInteractor: GameInteractor,
    private val game: Game
) : View.OnCreateContextMenuListener {

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        menu.add(R.string.game_context_menu_resume).setOnMenuItemClickListener {
            gameInteractor.onGamePlay(game)
            true
        }

        menu.add(R.string.game_context_menu_restart).setOnMenuItemClickListener {
            gameInteractor.onGameRestart(game)
            true
        }

        if (game.isFavorite) {
            menu.add(R.string.game_context_menu_remove_from_favorites).setOnMenuItemClickListener {
                gameInteractor.onFavoriteToggle(game, false)
                true
            }
        } else {
            menu.add(R.string.game_context_menu_add_to_favorites).setOnMenuItemClickListener {
                gameInteractor.onFavoriteToggle(game, true)
                true
            }
        }

        if (gameInteractor.supportShortcuts()) {
            menu.add(R.string.game_context_menu_create_shortcut).setOnMenuItemClickListener {
                gameInteractor.onCreateShortcut(game)
                true
            }
        }
    }
}
