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
import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.swordfish.lemuroid.R
import dagger.Lazy
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlin.math.roundToInt

class RxSettingsManager(
    private val context: Context,
    sharedPreferences: Lazy<SharedPreferences>
) {

    private val rxSharedPreferences = Single.fromCallable {
        RxSharedPreferences.create(sharedPreferences.get())
    }

    private fun getString(resId: Int) = context.getString(resId)

    val autoSave = booleanPreference(R.string.pref_key_autosave, true)

    val vibrateOnTouch = booleanPreference(R.string.pref_key_vibrate_on_touch, true)

    val screenFilter = stringPreference(
        R.string.pref_key_shader_filter,
        context.resources.getStringArray(R.array.pref_key_shader_filter_values).first()
    )

    val tiltSensitivity = floatPreference(R.string.pref_key_tilt_sensitivity_index, 10, 0.6f)

    val autoSaveSync = booleanPreference(R.string.pref_key_save_sync_auto, false)

    val syncSaves = booleanPreference(R.string.pref_key_save_sync_enable, true)

    val syncStatesCores = stringSetPreference(R.string.pref_key_save_sync_cores, setOf())

    private fun booleanPreference(keyId: Int, default: Boolean): Single<Boolean> {
        return rxSharedPreferences.flatMap {
            it.getBoolean(getString(keyId), default)
                .asObservable()
                .subscribeOn(Schedulers.io())
                .first(default)
        }
    }

    private fun stringPreference(keyId: Int, default: String): Single<String> {
        return rxSharedPreferences.flatMap {
            it.getString(getString(keyId), default)
                .asObservable()
                .subscribeOn(Schedulers.io())
                .first(default)
        }
    }

    private fun stringSetPreference(keyId: Int, default: Set<String>): Single<Set<String>> {
        return rxSharedPreferences.flatMap {
            it.getStringSet(getString(keyId), default)
                .asObservable()
                .subscribeOn(Schedulers.io())
                .first(default)
        }
    }

    private fun floatPreference(keyId: Int, ticks: Int, default: Float): Single<Float> {
        return rxSharedPreferences.flatMap { sharedPreferences ->
            sharedPreferences.getInteger(getString(keyId), floatToIndex(default, ticks))
                .asObservable()
                .subscribeOn(Schedulers.io())
                .first(floatToIndex(default, ticks))
                .map { indexToFloat(it, ticks) }
        }
    }

    private fun indexToFloat(index: Int, ticks: Int): Float = index.toFloat() / ticks.toFloat()

    private fun floatToIndex(value: Float, ticks: Int): Int = (value * ticks).roundToInt()
}
