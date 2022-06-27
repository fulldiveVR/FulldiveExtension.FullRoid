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

package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.content.Context
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameMenuContract
import com.swordfish.lemuroid.app.shared.gamemenu.GameMenuHelper
import com.swordfish.lemuroid.common.preferences.DummyDataStore
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import dagger.android.support.AndroidSupportInjection

class GameMenuFragment : PreferenceFragmentCompat() {

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = DummyDataStore
        setPreferencesFromResource(R.xml.mobile_game_settings, rootKey)

        val audioEnabled = activity?.intent?.getBooleanExtra(
            GameMenuContract.EXTRA_AUDIO_ENABLED,
            false
        ) ?: false

        GameMenuHelper.setupAudioOption(preferenceScreen, audioEnabled)

        val fastForwardSupported = activity?.intent?.getBooleanExtra(
            GameMenuContract.EXTRA_FAST_FORWARD_SUPPORTED,
            false
        ) ?: false

        val fastForwardEnabled = activity?.intent?.getBooleanExtra(
            GameMenuContract.EXTRA_FAST_FORWARD,
            false
        ) ?: false

        GameMenuHelper.setupFastForwardOption(
            preferenceScreen,
            fastForwardEnabled,
            fastForwardSupported
        )

        val systemCoreConfig = activity?.intent?.getSerializableExtra(
            GameMenuContract.EXTRA_SYSTEM_CORE_CONFIG
        ) as SystemCoreConfig

        GameMenuHelper.setupSaveOption(preferenceScreen, systemCoreConfig)

        val numDisks = activity?.intent?.getIntExtra(GameMenuContract.EXTRA_DISKS, 0) ?: 0
        val currentDisk = activity?.intent?.getIntExtra(GameMenuContract.EXTRA_CURRENT_DISK, 0) ?: 0
        if (numDisks > 1) {
            GameMenuHelper.setupChangeDiskOption(activity, preferenceScreen, currentDisk, numDisks)
        }

        GameMenuHelper.setupSettingsOption(preferenceScreen, systemCoreConfig)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if (GameMenuHelper.onPreferenceTreeClicked(activity, preference))
            return true

        when (preference?.key) {
            "pref_game_section_save" -> {
                findNavController().navigate(R.id.game_menu_save)
            }
            "pref_game_section_load" -> {
                findNavController().navigate(R.id.game_menu_load)
            }
            "pref_game_section_core_options" -> {
                findNavController().navigate(R.id.game_menu_core_options)
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    @dagger.Module
    class Module
}
