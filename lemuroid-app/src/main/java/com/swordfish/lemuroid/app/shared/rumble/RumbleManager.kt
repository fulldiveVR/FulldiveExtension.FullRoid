/*
 * Copyright (C) 2017 Retrograde Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.swordfish.lemuroid.app.shared.rumble

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.InputDevice
import com.swordfish.lemuroid.app.mobile.feature.settings.RxSettingsManager
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.lib.library.SystemCoreConfig
import com.swordfish.libretrodroid.RumbleEvent
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class RumbleManager(
    applicationContext: Context,
    private val rxSettingsManager: RxSettingsManager,
    private val inputDeviceManager: InputDeviceManager
) {
    private val deviceVibrator = applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    fun processRumbleEvents(
        systemCoreConfig: SystemCoreConfig,
        rumbleEventsObservable: Observable<RumbleEvent>
    ): Completable {
        return rxSettingsManager.enableRumble
            .filter { it && systemCoreConfig.rumbleSupported }
            .flatMapObservable { inputDeviceManager.getEnabledInputsObservable() }
            .flatMapSingle { getVibrators(it) }
            .switchMapCompletable { vibrators ->
                rumbleEventsObservable
                    .subscribeOn(Schedulers.from(singleThreadExecutor))
                    .doOnNext {
                        kotlin.runCatching { vibrate(vibrators[it.port], it) }
                    }
                    .doOnSubscribe { stopAllVibrators(vibrators) }
                    .doAfterTerminate { stopAllVibrators(vibrators) }
                    .ignoreElements()
                    .onErrorComplete()
            }
    }

    private fun stopAllVibrators(vibrators: List<Vibrator>) {
        vibrators.forEach {
            kotlin.runCatching { it.cancel() }
        }
    }

    private fun getVibrators(gamePads: List<InputDevice>): Single<List<Vibrator>> {
        return rxSettingsManager.enableDeviceRumble
            .map { enableDeviceRumble ->
                if (gamePads.isEmpty() && enableDeviceRumble) {
                    listOf(deviceVibrator)
                } else {
                    gamePads.map { it.vibrator }
                }
            }
    }

    private fun vibrate(vibrator: Vibrator?, rumbleEvent: RumbleEvent) {
        if (vibrator == null) return

        vibrator.cancel()

        val amplitude = computeAmplitude(rumbleEvent)

        if (amplitude == 0) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && vibrator.hasAmplitudeControl()) {
            vibrator.vibrate(VibrationEffect.createOneShot(MAX_RUMBLE_DURATION_MS, amplitude))
        } else if (amplitude > LEGACY_MIN_RUMBLE_STRENGTH) {
            vibrator.vibrate(MAX_RUMBLE_DURATION_MS)
        }
    }

    private fun computeAmplitude(rumbleEvent: RumbleEvent): Int {
        val strength = rumbleEvent.strengthStrong * 0.66f + rumbleEvent.strengthWeak * 0.33f
        return (DEFAULT_RUMBLE_STRENGTH * (strength) * 255).roundToInt()
    }

    companion object {
        const val MAX_RUMBLE_DURATION_MS = 1000L
        const val DEFAULT_RUMBLE_STRENGTH = 0.5f
        const val LEGACY_MIN_RUMBLE_STRENGTH = 100
    }
}
