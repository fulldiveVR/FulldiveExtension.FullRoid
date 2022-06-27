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
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.bios.Bios
import com.swordfish.lemuroid.lib.bios.BiosManager

class BiosPreferences(private val biosManager: BiosManager) {

    fun addBiosPreferences(preferenceScreen: PreferenceScreen) {
        val context = preferenceScreen.context
        val (installedBios, notInstalledBios) = biosManager.getBiosInfo()

        val detectedBios = createCategory(context, context.getString(R.string.settings_bios_category_detected))
        preferenceScreen.addPreference(detectedBios)
        detectedBios.isVisible = installedBios.isNotEmpty()

        val notDetectedBios = createCategory(context, context.getString(R.string.settings_bios_category_not_detected))
        preferenceScreen.addPreference(notDetectedBios)
        notDetectedBios.isVisible = notInstalledBios.isNotEmpty()

        installedBios.forEach {
            val preference = createBiosPreference(preferenceScreen.context, it)
            preference.isEnabled = true
            detectedBios.addPreference(preference)
        }

        notInstalledBios.forEach {
            val preference = createBiosPreference(preferenceScreen.context, it)
            preference.isEnabled = false
            notDetectedBios.addPreference(preference)
        }
    }

    private fun createBiosPreference(context: Context, bios: Bios): Preference {
        val preference = Preference(context)
        preference.title = bios.description
        preference.summary = bios.displayName()
        preference.isIconSpaceReserved = false
        return preference
    }

    private fun createCategory(context: Context, title: String): PreferenceCategory {
        val category = PreferenceCategory(context)
        category.title = title
        category.isIconSpaceReserved = false
        return category
    }
}
