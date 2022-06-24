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

package com.swordfish.lemuroid.lib.game

class GameLoaderException(val error: GameLoaderError) : RuntimeException("Game Loader error: $error")

sealed class GameLoaderError {
    object GLIncompatible : GameLoaderError()
    object Generic : GameLoaderError()
    object LoadCore : GameLoaderError()
    object LoadGame : GameLoaderError()
    object Saves : GameLoaderError()
    data class MissingBiosFiles(val missingFiles: List<String>) : GameLoaderError()
}
