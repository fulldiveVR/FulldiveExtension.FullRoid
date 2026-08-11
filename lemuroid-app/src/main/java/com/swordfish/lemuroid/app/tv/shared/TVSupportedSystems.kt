/*
 * Copyright (c) 2026 FullDive
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

package com.swordfish.lemuroid.app.tv.shared

import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.entity.Game

/**
 * A 3DS title only loads once the user imports a system file dumped from their own console, and
 * the only importer is on the phone settings screen — the leanback UI has no equivalent, and many
 * TV devices cannot resolve a document picker at all. Listing 3DS games here would offer nothing
 * but a game that refuses to load, so they stay hidden until an importer exists on TV.
 *
 * The games remain in the database: this hides them from the leanback surfaces, it does not
 * unscan them, so the same library shows 3DS normally on a phone.
 */
object TVSupportedSystems {
    private val UNSUPPORTED_SYSTEM_IDS = setOf(SystemID.NINTENDO_3DS.dbname)

    fun isSupported(systemId: String) = systemId !in UNSUPPORTED_SYSTEM_IDS

    fun isSupported(game: Game) = isSupported(game.systemId)
}
