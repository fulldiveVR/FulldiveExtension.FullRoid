/*
 *  RetrogradeApplicationComponent.kt
 *
 *  Copyright (C) 2017 Retrograde Project
 *
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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.swordfish.lemuroid.app.shared.settings

import android.content.SharedPreferences
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.SystemID
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import dagger.Lazy

class ControllerConfigsManager(private val sharedPreferences: Lazy<SharedPreferences>) {

    fun getControllerConfigs(
        systemId: SystemID,
        systemCoreConfig: SystemCoreConfig
    ): Single<Map<Int, ControllerConfig>> = Single.fromCallable {
        systemCoreConfig.controllerConfigs.entries
            .map { (port, controllers) ->
                val currentName = sharedPreferences.get().getString(
                    getSharedPreferencesId(systemId.dbname, systemCoreConfig.coreID, port),
                    null
                )

                val currentController = controllers
                    .firstOrNull { it.name == currentName } ?: controllers.first()

                port to currentController
            }
            .toMap()
    }.subscribeOn(Schedulers.io())

    companion object {
        fun getSharedPreferencesId(systemId: String, coreID: CoreID, port: Int) =
            "pref_key_controller_type_${systemId}_${coreID.coreName}_$port"
    }
}
