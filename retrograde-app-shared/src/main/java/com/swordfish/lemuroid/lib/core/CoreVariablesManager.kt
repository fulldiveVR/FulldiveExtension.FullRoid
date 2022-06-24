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

package com.swordfish.lemuroid.lib.core

import android.content.SharedPreferences
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.SystemID
import io.reactivex.Single
import java.security.InvalidParameterException
import dagger.Lazy

class CoreVariablesManager(private val sharedPreferences: Lazy<SharedPreferences>) {

    fun getOptionsForCore(
        systemID: SystemID,
        systemCoreConfig: SystemCoreConfig
    ): Single<List<CoreVariable>> {

        val defaultMap = convertCoreVariablesToMap(systemCoreConfig.defaultSettings)
        return retrieveCustomCoreVariables(systemID, systemCoreConfig)
            .map { convertCoreVariablesToMap(it) }
            .map { defaultMap + it }
            .map { convertMapToCoreVariables(it) }
    }

    private fun convertMapToCoreVariables(variablesMap: Map<String, String>): List<CoreVariable> {
        return variablesMap.entries.map { CoreVariable(it.key, it.value) }
    }

    private fun convertCoreVariablesToMap(coreVariables: List<CoreVariable>): Map<String, String> {
        return coreVariables
            .map { it.key to it.value }
            .toMap()
    }

    private fun retrieveCustomCoreVariables(
        systemID: SystemID,
        systemCoreConfig: SystemCoreConfig
    ) = Single.fromCallable {

        val exposedKeys = systemCoreConfig.exposedSettings
        val exposedAdvancedKeys = systemCoreConfig.exposedAdvancedSettings

        val requestedKeys = (exposedKeys + exposedAdvancedKeys).map { it.key }
            .map { computeSharedPreferenceKey(it, systemID.dbname) }

        sharedPreferences.get().all.filter { it.key in requestedKeys }
            .map { (key, value) ->
                val result = when (value!!) {
                    is Boolean -> if (value as Boolean) "enabled" else "disabled"
                    is String -> value as String
                    else -> throw InvalidParameterException("Invalid setting in SharedPreferences")
                }
                CoreVariable(computeOriginalKey(key, systemID.dbname), result)
            }
    }

    companion object {
        private const val RETRO_OPTION_PREFIX = "cv"

        fun computeSharedPreferenceKey(retroVariableName: String, systemID: String): String {
            return "${computeSharedPreferencesPrefix(systemID)}$retroVariableName"
        }

        fun computeOriginalKey(sharedPreferencesKey: String, systemID: String): String {
            return sharedPreferencesKey.replace(computeSharedPreferencesPrefix(systemID), "")
        }

        private fun computeSharedPreferencesPrefix(systemID: String): String {
            return "${RETRO_OPTION_PREFIX}_${systemID}_"
        }
    }
}
