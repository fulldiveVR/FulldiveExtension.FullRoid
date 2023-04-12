/*
 * Copyright (c) 2022 FullDive
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

package com.swordfish.lemuroid.app.appextension.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.swordfish.lemuroid.R

class FirebaseConfigurationFetcher : IRemoteConfigFetcher {
    private val remoteConfig = FirebaseRemoteConfig
        .getInstance()
        .apply {
            setDefaultsAsync(R.xml.config_defaults)
        }

    override fun fetch(force: Boolean) {
        try {
            val remoteConfigSettings = FirebaseRemoteConfigSettings
                .Builder()
                .setFetchTimeoutInSeconds(if (force) 0L else 3600L)
                .build()

            remoteConfig.setConfigSettingsAsync(remoteConfigSettings)

            remoteConfig
                .fetchAndActivate()
                .addOnSuccessListener {
                }
                .addOnFailureListener {
                }
                .addOnCanceledListener {
                }
        } catch (ex: Exception) {
        }
    }

    override fun getRemoteBoolean(value: String) = remoteConfig.getBoolean(value)

    override fun getRemoteString(value: String) = remoteConfig.getString(value)

    override fun getRemoteLong(value: String) = remoteConfig.getLong(value)

    override fun getRemoteDouble(value: String) = remoteConfig.getDouble(value)

    override fun getAll(): Map<String, String> {
        val result = HashMap<String, String>()
        val pairs = remoteConfig.all
        pairs.keys.forEach { key ->
            result[key] = pairs[key]?.asString().orEmpty()
        }
        val info = remoteConfig.info
        result["lastFetchStatus"] = info.lastFetchStatus.toString()
        result["fetchTimeMillis"] = info.fetchTimeMillis.toString()
        return result
    }
}
