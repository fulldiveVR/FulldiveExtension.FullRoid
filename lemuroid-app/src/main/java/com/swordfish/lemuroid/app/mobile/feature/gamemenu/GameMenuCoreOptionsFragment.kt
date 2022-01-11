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

package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.Context
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.coreoptions.CoreOptionsPreferenceHelper
import com.swordfish.lemuroid.app.shared.coreoptions.LemuroidCoreOption
import com.swordfish.lemuroid.app.shared.settings.GamePadManager
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import java.security.InvalidParameterException
import javax.inject.Inject

class GameMenuCoreOptionsFragment : PreferenceFragmentCompat() {

    @Inject lateinit var gamePadManager: GamePadManager

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore =
            SharedPreferencesHelper.getSharedPreferencesDataStore(requireContext())
        addPreferencesFromResource(R.xml.empty_preference_screen)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gamePadManager
            .getGamePadsObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribeBy { updateScreen(it.size) }
    }

    fun updateScreen(connectedGamePads: Int) {
        preferenceScreen.removeAll()

        val extras = activity?.intent?.extras

        val coreOptions = extras?.getSerializable(GameMenuContract.EXTRA_CORE_OPTIONS) as Array<LemuroidCoreOption>?
            ?: throw InvalidParameterException("Missing EXTRA_CORE_OPTIONS")

        val advancedCoreOptions = extras?.getSerializable(
            GameMenuContract.EXTRA_ADVANCED_CORE_OPTIONS
        ) as Array<LemuroidCoreOption>? ?: throw InvalidParameterException("Missing EXTRA_ADVANCED_CORE_OPTIONS")

        val game = extras?.getSerializable(GameMenuContract.EXTRA_GAME) as Game?
            ?: throw InvalidParameterException("Missing EXTRA_GAME")

        val coreConfig = extras?.getSerializable(GameMenuContract.EXTRA_SYSTEM_CORE_CONFIG) as SystemCoreConfig?
            ?: throw InvalidParameterException("Missing EXTRA_SYSTEM_CORE_CONFIG")

        CoreOptionsPreferenceHelper.addPreferences(
            preferenceScreen,
            game.systemId,
            coreOptions.toList(),
            advancedCoreOptions.toList()
        )

        CoreOptionsPreferenceHelper.addControllers(
            preferenceScreen,
            game.systemId,
            coreConfig.coreID,
            maxOf(1, connectedGamePads),
            coreConfig.controllerConfigs
        )
    }

    @dagger.Module
    class Module
}
