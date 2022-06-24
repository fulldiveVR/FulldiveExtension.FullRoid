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

package com.swordfish.lemuroid.app.shared.settings

import android.content.Context
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.app.shared.library.LibraryIndexScheduler
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.library.GameSystem

class CoresSelectionPreferences {

    fun addCoresSelectionPreferences(preferenceScreen: PreferenceScreen, gameSystems: List<GameSystem>) {
        val context = preferenceScreen.context
        gameSystems
            .filter { it.systemCoreConfigs.size > 1 }
            .forEach {
                preferenceScreen.addPreference(createPreference(context, it))
            }
    }

    private fun createPreference(context: Context, system: GameSystem): Preference {
        val preference = ListPreference(context)
        preference.key = CoresSelection.computeSystemPreferenceKey(system.id)
        preference.title = context.getString(system.titleResId)
        preference.isIconSpaceReserved = false
        preference.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        preference.setDefaultValue(system.systemCoreConfigs.map { it.coreID.coreName }.first())
        preference.isEnabled = system.systemCoreConfigs.size > 1

        preference.entries = system.systemCoreConfigs
            .map { it.coreID.coreDisplayName }
            .toTypedArray()

        preference.entryValues = system.systemCoreConfigs
            .map { it.coreID.coreName }
            .toTypedArray()

        preference.setOnPreferenceChangeListener { _, _ ->
            LibraryIndexScheduler.scheduleCoreUpdate(context.applicationContext)
            true
        }

        return preference
    }
}
