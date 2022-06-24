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

package com.swordfish.lemuroid.app.tv.settings

import android.content.Context
import android.os.Bundle
import android.view.InputDevice
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.gamesystem.GameSystemHelper
import com.swordfish.lemuroid.app.shared.settings.AdvancedSettingsPreferences
import com.swordfish.lemuroid.app.shared.settings.BiosPreferences
import com.swordfish.lemuroid.app.shared.settings.CoresSelectionPreferences
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.library.PendingOperationsMonitor
import com.swordfish.lemuroid.app.shared.settings.GamePadPreferencesHelper
import com.swordfish.lemuroid.app.shared.settings.SaveSyncPreferences
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class TVSettingsFragment : LeanbackPreferenceFragmentCompat() {

    @Inject
    lateinit var settingsInteractor: SettingsInteractor
    @Inject
    lateinit var biosPreferences: BiosPreferences
    @Inject
    lateinit var gamePadPreferencesHelper: GamePadPreferencesHelper
    @Inject
    lateinit var inputDeviceManager: InputDeviceManager
    @Inject
    lateinit var coresSelectionPreferences: CoresSelectionPreferences
    @Inject
    lateinit var saveSyncManager: SaveSyncManager

    lateinit var saveSyncPreferences: SaveSyncPreferences

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)

        saveSyncPreferences = SaveSyncPreferences(saveSyncManager)

        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore =
            SharedPreferencesHelper.getSharedPreferencesDataStore(requireContext())
        setPreferencesFromResource(R.xml.tv_settings, rootKey)

        getCoresSelectionScreen()?.let {
            coresSelectionPreferences.addCoresSelectionPreferences(it, GameSystemHelper().all())
        }

        getBiosInfoPreferenceScreen()?.let {
            biosPreferences.addBiosPreferences(it)
        }

        getAdvancedSettingsPreferenceScreen()?.let {
            AdvancedSettingsPreferences.updateCachePreferences(it)
        }

        getSaveSyncScreen()?.let {
            if (saveSyncManager.isSupported()) {
                saveSyncPreferences.addSaveSyncPreferences(it)
            }
            it.isVisible = saveSyncManager.isSupported()
        }
    }

    override fun onResume() {
        super.onResume()
        inputDeviceManager.getGamePadsObservable()
            .distinctUntilChanged()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { refreshGamePadBindingsScreen(it) }

        refreshSaveSyncScreen()

        getSaveSyncScreen()?.let { screen ->
            PendingOperationsMonitor(requireContext())
                .anySaveOperationInProgress()
                .observe(this) { syncInProgress ->
                    saveSyncPreferences.updatePreferences(screen, syncInProgress)
                }
        }
    }

    private fun getGamePadPreferenceScreen(): PreferenceScreen? {
        return findPreference(resources.getString(R.string.pref_key_open_gamepad_settings))
    }

    private fun getSaveSyncScreen(): PreferenceScreen? {
        return findPreference(resources.getString(R.string.pref_key_open_save_sync_settings))
    }

    private fun getCoresSelectionScreen(): PreferenceScreen? {
        return findPreference(resources.getString(R.string.pref_key_open_cores_selection))
    }

    private fun getBiosInfoPreferenceScreen(): PreferenceScreen? {
        return findPreference(resources.getString(R.string.pref_key_display_bios_info))
    }

    private fun getAdvancedSettingsPreferenceScreen(): PreferenceScreen? {
        return findPreference(resources.getString(R.string.pref_key_advanced_settings))
    }

    private fun refreshGamePadBindingsScreen(gamePads: List<InputDevice>) {
        getGamePadPreferenceScreen()?.let {
            it.removeAll()
            gamePadPreferencesHelper.addGamePadsPreferencesToScreen(requireContext(), it, gamePads)
        }
    }

    private fun refreshSaveSyncScreen() {
        getSaveSyncScreen()?.let {
            saveSyncPreferences.updatePreferences(it, false)
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (saveSyncPreferences.onPreferenceTreeClick(activity, preference)) {
            return true
        }

        when (preference.key) {
            getString(R.string.pref_key_reset_gamepad_bindings) -> handleResetGamePadBindings()
            getString(R.string.pref_key_reset_settings) -> handleResetSettings()
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun handleResetGamePadBindings() {
        gamePadPreferencesHelper.resetBindingsAndRefresh()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe { refreshGamePadBindingsScreen(it) }
    }

    private fun handleResetSettings() {
        settingsInteractor.resetAllSettings()
        activity?.finish()
    }

    @dagger.Module
    class Module
}
