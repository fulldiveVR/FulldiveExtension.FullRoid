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

package com.swordfish.lemuroid.common.preferences

import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore

class SharedPreferencesDataStore(
    private val sharedPreferences: SharedPreferences,
) : PreferenceDataStore() {
    override fun putString(
        key: String?,
        value: String?,
    ) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun putStringSet(
        key: String?,
        values: MutableSet<String>?,
    ) {
        sharedPreferences.edit().putStringSet(key, values).apply()
    }

    override fun putInt(
        key: String?,
        value: Int,
    ) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun putLong(
        key: String?,
        value: Long,
    ) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    override fun putFloat(
        key: String?,
        value: Float,
    ) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    override fun putBoolean(
        key: String?,
        value: Boolean,
    ) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    override fun getString(
        key: String?,
        defValue: String?,
    ): String? {
        return sharedPreferences.getString(key, defValue) ?: defValue
    }

    override fun getStringSet(
        key: String?,
        defValues: MutableSet<String>?,
    ): MutableSet<String> {
        return sharedPreferences.getStringSet(key, defValues) ?: mutableSetOf()
    }

    override fun getInt(
        key: String?,
        defValue: Int,
    ): Int {
        return sharedPreferences.getInt(key, defValue)
    }

    override fun getLong(
        key: String?,
        defValue: Long,
    ): Long {
        return sharedPreferences.getLong(key, defValue)
    }

    override fun getFloat(
        key: String?,
        defValue: Float,
    ): Float {
        return sharedPreferences.getFloat(key, defValue)
    }

    override fun getBoolean(
        key: String?,
        defValue: Boolean,
    ): Boolean {
        return sharedPreferences.getBoolean(key, defValue)
    }
}
