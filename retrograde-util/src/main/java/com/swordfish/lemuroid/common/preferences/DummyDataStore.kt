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

import androidx.preference.PreferenceDataStore

object DummyDataStore : PreferenceDataStore() {
    override fun putString(key: String?, value: String?) {}

    override fun putStringSet(key: String?, values: MutableSet<String>?) {}

    override fun putInt(key: String?, value: Int) {}

    override fun putLong(key: String?, value: Long) {}

    override fun putFloat(key: String?, value: Float) {}

    override fun putBoolean(key: String?, value: Boolean) {}

    override fun getString(key: String?, defValue: String?): String? = defValue

    override fun getStringSet(
        key: String?,
        defValues: MutableSet<String>?
    ): MutableSet<String> = mutableSetOf()

    override fun getInt(key: String?, defValue: Int): Int = defValue

    override fun getLong(key: String?, defValue: Long): Long = defValue

    override fun getFloat(key: String?, defValue: Float): Float = defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue
}
