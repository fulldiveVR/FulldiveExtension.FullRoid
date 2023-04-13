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
import com.swordfish.lemuroid.app.appextension.or

class FirebaseConfigurationFetcher : IRemoteConfigFetcher {

    private var remoteConfig: FirebaseRemoteConfig? = null

    override fun fetch(force: Boolean) {
        try {
            remoteConfig = FirebaseRemoteConfig.getInstance()
                .apply {
                    setDefaultsAsync(R.xml.config_defaults)
                }
            val remoteConfigSettings = FirebaseRemoteConfigSettings
                .Builder()
                .setFetchTimeoutInSeconds(if (force) 0L else 3600L)
                .build()

            remoteConfig?.setConfigSettingsAsync(remoteConfigSettings)

            remoteConfig?.fetchAndActivate()
        } catch (ex: Exception) {
        }
    }

    override fun getRemoteBoolean(value: String) = remoteConfig?.getBoolean(value).or { false }

    override fun getRemoteString(value: String) = remoteConfig?.getString(value).or { "" }

    override fun getRemoteLong(value: String) = remoteConfig?.getLong(value).or { 0L }

    override fun getRemoteDouble(value: String) = remoteConfig?.getDouble(value).or { 0.0 }
}
