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

package com.swordfish.lemuroid.lib.controller

import android.content.Context
import android.content.SharedPreferences
import com.swordfish.touchinput.controller.R
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class TouchControllerSettingsManager(
    private val context: Context,
    private val controllerID: TouchControllerID,
    private val sharedPreferences: Lazy<SharedPreferences>,
    private val orientation: Orientation,
) {
    enum class Orientation {
        PORTRAIT,
        LANDSCAPE,
    }

    data class Settings(
        val scale: Float = DEFAULT_SCALE,
        val rotation: Float = DEFAULT_ROTATION,
        val marginX: Float = DEFAULT_MARGIN_X,
        val marginY: Float = DEFAULT_MARGIN_Y,
    )

    suspend fun retrieveSettings(): Settings =
        withContext(Dispatchers.IO) {
            val sharedPreferences = sharedPreferences.get()
            Settings(
                scale =
                    indexToFloat(
                        sharedPreferences.getInt(
                            getPreferenceString(R.string.pref_key_virtual_pad_scale, orientation),
                            floatToIndex(DEFAULT_SCALE),
                        ),
                    ),
                rotation =
                    indexToFloat(
                        sharedPreferences.getInt(
                            getPreferenceString(R.string.pref_key_virtual_pad_rotation, orientation),
                            floatToIndex(DEFAULT_ROTATION),
                        ),
                    ),
                marginX =
                    indexToFloat(
                        sharedPreferences.getInt(
                            getPreferenceString(R.string.pref_key_virtual_pad_margin_x, orientation),
                            floatToIndex(DEFAULT_MARGIN_X),
                        ),
                    ),
                marginY =
                    indexToFloat(
                        sharedPreferences.getInt(
                            getPreferenceString(R.string.pref_key_virtual_pad_margin_y, orientation),
                            floatToIndex(DEFAULT_MARGIN_Y),
                        ),
                    ),
            )
        }

    suspend fun storeSettings(settings: Settings): Unit =
        withContext(Dispatchers.IO) {
            sharedPreferences.get().edit().apply {
                putInt(
                    getPreferenceString(R.string.pref_key_virtual_pad_scale, orientation),
                    floatToIndex(settings.scale),
                ).apply()
                putInt(
                    getPreferenceString(R.string.pref_key_virtual_pad_rotation, orientation),
                    floatToIndex(settings.rotation),
                ).apply()
                putInt(
                    getPreferenceString(R.string.pref_key_virtual_pad_margin_x, orientation),
                    floatToIndex(settings.marginX),
                ).apply()
                putInt(
                    getPreferenceString(R.string.pref_key_virtual_pad_margin_y, orientation),
                    floatToIndex(settings.marginY),
                ).apply()
            }
        }

    private fun indexToFloat(index: Int): Float = index / 100f

    private fun floatToIndex(value: Float): Int = (value * 100).roundToInt()

    companion object {
        const val DEFAULT_SCALE = 0.5f
        const val DEFAULT_ROTATION = 0.0f
        const val DEFAULT_MARGIN_X = 0.0f
        const val DEFAULT_MARGIN_Y = 0.0f

        const val MAX_ROTATION = 45f
        const val MIN_SCALE = 0.75f
        const val MAX_SCALE = 1.5f

        const val MAX_MARGINS = 96f
    }

    private fun getPreferenceString(
        preferenceStringId: Int,
        orientation: Orientation,
    ): String {
        return "${context.getString(preferenceStringId)}_${controllerID}_${orientation.ordinal}"
    }
}
