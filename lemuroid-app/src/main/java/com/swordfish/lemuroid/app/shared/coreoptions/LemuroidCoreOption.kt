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

package com.swordfish.lemuroid.app.shared.coreoptions

import android.content.Context
import com.swordfish.lemuroid.lib.library.ExposedSetting
import java.io.Serializable

data class LemuroidCoreOption(
    private val exposedSetting: ExposedSetting,
    private val coreOption: CoreOption,
) : Serializable {
    fun getKey(): String {
        return exposedSetting.key
    }

    fun getDisplayName(context: Context): String {
        return context.getString(exposedSetting.titleId)
    }

    fun getEntries(context: Context): List<String> {
        if (exposedSetting.values.isEmpty()) {
            return coreOption.optionValues.map { it.capitalize() }
        }

        return getCorrectExposedSettings().map { context.getString(it.titleId) }
    }

    fun getEntriesValues(): List<String> {
        if (exposedSetting.values.isEmpty()) {
            return coreOption.optionValues.map { it }
        }

        return getCorrectExposedSettings().map { it.key }
    }

    fun getCurrentValue(): String {
        return coreOption.variable.value
    }

    fun getCurrentIndex(): Int {
        return maxOf(getEntriesValues().indexOf(getCurrentValue()), 0)
    }

    private fun getCorrectExposedSettings(): List<ExposedSetting.Value> {
        return exposedSetting.values
            .filter { it.key in coreOption.optionValues }
    }
}
