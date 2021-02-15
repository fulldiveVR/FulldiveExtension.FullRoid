package com.swordfish.lemuroid.app.shared.settings

import android.content.Context
import android.content.SharedPreferences
import com.swordfish.lemuroid.lib.controller.ControllerConfig
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class ControllerConfigsManager(context: Context) {
    private val sharedPreferences = getDefaultSharedPreferences(context)

    private fun getDefaultSharedPreferences(context: Context): SharedPreferences {
        return SharedPreferencesHelper.getSharedPreferences(context)
    }

    fun getControllerConfigs(
        systemId: SystemID,
        systemCoreConfig: SystemCoreConfig
    ): Single<Map<Int, ControllerConfig>> = Single.fromCallable {
        systemCoreConfig.controllerConfigs.entries
            .map { (port, controllers) ->
                val currentName = sharedPreferences.getString(
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