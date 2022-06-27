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
import android.text.format.Formatter
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.storage.cache.CacheCleaner

object AdvancedSettingsPreferences {

    fun updateCachePreferences(preferenceScreen: PreferenceScreen) {
        val cacheKey = preferenceScreen.context.getString(R.string.pref_key_max_cache_size)
        preferenceScreen.findPreference<ListPreference>(cacheKey)?.apply {
            val supportedCacheValues = CacheCleaner.getSupportedCacheLimits()

            entries = supportedCacheValues
                .map { getSizeLabel(preferenceScreen.context, it) }
                .toTypedArray()

            entryValues = supportedCacheValues
                .map { it.toString() }
                .toTypedArray()

            if (value == null) {
                setValueIndex(supportedCacheValues.indexOf(CacheCleaner.getDefaultCacheLimit()))
            }

            summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        }
    }

    private fun getSizeLabel(appContext: Context, size: Long): String {
        return Formatter.formatShortFileSize(appContext, size)
    }
}
