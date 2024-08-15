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

package com.swordfish.lemuroid.app.tv.shared

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.leanback.preference.LeanbackSettingsFragmentCompat
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.swordfish.lemuroid.app.shared.ImmersiveActivity

abstract class TVBaseSettingsActivity : ImmersiveActivity() {
    abstract class BaseSettingsFragmentWrapper : LeanbackSettingsFragmentCompat() {

        override fun onPreferenceStartInitialScreen() {
            startPreferenceFragment(createFragment())
        }

        override fun onPreferenceStartFragment(
            caller: PreferenceFragmentCompat,
            pref: Preference
        ): Boolean {
            val args = pref.extras
            val f = pref.fragment?.let {
                childFragmentManager.fragmentFactory.instantiate(
                    requireActivity().classLoader,
                    it
                )
            }
            f?.arguments = args
            f?.setTargetFragment(caller, 0)
            if (f is PreferenceFragmentCompat || f is PreferenceDialogFragmentCompat) {
                startPreferenceFragment(f)
            } else {
                f?.let { startImmersiveFragment(it) }
            }
            return true
        }

        override fun onPreferenceStartScreen(
            caller: PreferenceFragmentCompat,
            pref: PreferenceScreen
        ): Boolean {
            val fragment = createFragment()
            val args = Bundle(1)
            args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.key)
            fragment.arguments = args
            startPreferenceFragment(fragment)
            return true
        }

        abstract fun createFragment(): Fragment
    }
}
