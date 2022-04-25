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

package com.swordfish.lemuroid.app.mobile.feature.settings

import android.content.Context
import android.os.Bundle
import android.view.InputDevice
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.settings.GamePadPreferencesHelper
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class GamepadSettingsFragment : PreferenceFragmentCompat() {

    @Inject lateinit var gamePadPreferencesHelper: GamePadPreferencesHelper
    @Inject lateinit var inputDeviceManager: InputDeviceManager

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore =
            SharedPreferencesHelper.getSharedPreferencesDataStore(requireContext())
        setPreferencesFromResource(R.xml.empty_preference_screen, rootKey)
        inputDeviceManager.getGamePadsObservable()
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { refreshGamePads(it) }
    }

    private fun refreshGamePads(gamePads: List<InputDevice>) {
        preferenceScreen.removeAll()
        gamePadPreferencesHelper.addGamePadsPreferencesToScreen(
            requireContext(),
            preferenceScreen,
            gamePads
        )
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            getString(R.string.pref_key_reset_gamepad_bindings) -> handleResetBindings()
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun handleResetBindings() {
        gamePadPreferencesHelper.resetBindingsAndRefresh()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { refreshGamePads(it) }
    }

    @dagger.Module
    class Module
}
