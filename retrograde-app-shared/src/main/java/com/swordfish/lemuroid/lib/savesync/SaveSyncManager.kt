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

package com.swordfish.lemuroid.lib.savesync

import android.app.Activity
import android.content.Context
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.GameSystem

abstract class SaveSyncManager {
    abstract fun getProvider(): String

    abstract fun getSettingsActivity(): Class<out Activity>?

    abstract fun isSupported(): Boolean

    abstract fun isConfigured(): Boolean

    abstract fun getLastSyncInfo(): String

    abstract fun getConfigInfo(): String

    abstract suspend fun sync(cores: Set<CoreID>)

    abstract fun computeSavesSpace(): String

    abstract fun computeStatesSpace(core: CoreID): String

    fun getDisplayNameForCore(
        context: Context,
        coreID: CoreID,
        isProVersion: Boolean
    ): String {
        val systems = GameSystem.findSystemForCore(coreID, isProVersion)
        val systemHasMultipleCores = systems.any { it.systemCoreConfigs.size > 1 }

        val chunks =
            mutableListOf<String>().apply {
                add(systems.joinToString(", ") { context.getString(it.shortTitleResId) })

                if (systemHasMultipleCores) {
                    add(coreID.coreDisplayName)
                }

                add(computeStatesSpace(coreID))
            }

        return chunks.joinToString(" - ")
    }
}
