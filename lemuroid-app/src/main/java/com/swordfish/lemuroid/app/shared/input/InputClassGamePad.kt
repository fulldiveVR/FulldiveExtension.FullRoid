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
package com.swordfish.lemuroid.app.shared.input

import android.content.Context
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.swordfish.lemuroid.app.shared.settings.GameMenuShortcut

object InputClassGamePad : InputClass {

    private val MINIMAL_SUPPORTED_KEYS = intArrayOf(
        KeyEvent.KEYCODE_BUTTON_A,
        KeyEvent.KEYCODE_BUTTON_B,
        KeyEvent.KEYCODE_BUTTON_X,
        KeyEvent.KEYCODE_BUTTON_Y,
    )

    private val MINIMAL_KEYS_DEFAULT_ENABLED = MINIMAL_SUPPORTED_KEYS + intArrayOf(
        KeyEvent.KEYCODE_BUTTON_START,
        KeyEvent.KEYCODE_BUTTON_SELECT,
    )

    private val INPUT_KEYS = listOf(
        KeyEvent.KEYCODE_BUTTON_A,
        KeyEvent.KEYCODE_BUTTON_B,
        KeyEvent.KEYCODE_BUTTON_X,
        KeyEvent.KEYCODE_BUTTON_Y,
        KeyEvent.KEYCODE_BUTTON_START,
        KeyEvent.KEYCODE_BUTTON_SELECT,
        KeyEvent.KEYCODE_BUTTON_L1,
        KeyEvent.KEYCODE_BUTTON_R1,
        KeyEvent.KEYCODE_BUTTON_L2,
        KeyEvent.KEYCODE_BUTTON_R2,
        KeyEvent.KEYCODE_BUTTON_THUMBL,
        KeyEvent.KEYCODE_BUTTON_THUMBR,
        KeyEvent.KEYCODE_BUTTON_C,
        KeyEvent.KEYCODE_BUTTON_Z,
        KeyEvent.KEYCODE_BUTTON_1,
        KeyEvent.KEYCODE_BUTTON_2,
        KeyEvent.KEYCODE_BUTTON_3,
        KeyEvent.KEYCODE_BUTTON_4,
        KeyEvent.KEYCODE_BUTTON_5,
        KeyEvent.KEYCODE_BUTTON_6,
        KeyEvent.KEYCODE_BUTTON_7,
        KeyEvent.KEYCODE_BUTTON_8,
        KeyEvent.KEYCODE_BUTTON_9,
        KeyEvent.KEYCODE_BUTTON_10,
        KeyEvent.KEYCODE_BUTTON_11,
        KeyEvent.KEYCODE_BUTTON_12,
        KeyEvent.KEYCODE_BUTTON_13,
        KeyEvent.KEYCODE_BUTTON_14,
        KeyEvent.KEYCODE_BUTTON_15,
        KeyEvent.KEYCODE_BUTTON_16,
        KeyEvent.KEYCODE_BUTTON_MODE
    )

    private val CUSTOMIZABLE_KEYS = listOf(
        KeyEvent.KEYCODE_BUTTON_A,
        KeyEvent.KEYCODE_BUTTON_B,
        KeyEvent.KEYCODE_BUTTON_X,
        KeyEvent.KEYCODE_BUTTON_Y,
        KeyEvent.KEYCODE_BUTTON_START,
        KeyEvent.KEYCODE_BUTTON_SELECT,
        KeyEvent.KEYCODE_BUTTON_L1,
        KeyEvent.KEYCODE_BUTTON_R1,
        KeyEvent.KEYCODE_BUTTON_L2,
        KeyEvent.KEYCODE_BUTTON_R2,
        KeyEvent.KEYCODE_BUTTON_THUMBL,
        KeyEvent.KEYCODE_BUTTON_THUMBR,
        KeyEvent.KEYCODE_BUTTON_C,
        KeyEvent.KEYCODE_BUTTON_Z,
        KeyEvent.KEYCODE_BUTTON_1,
        KeyEvent.KEYCODE_BUTTON_2,
        KeyEvent.KEYCODE_BUTTON_3,
        KeyEvent.KEYCODE_BUTTON_4,
        KeyEvent.KEYCODE_BUTTON_5,
        KeyEvent.KEYCODE_BUTTON_6,
        KeyEvent.KEYCODE_BUTTON_7,
        KeyEvent.KEYCODE_BUTTON_8,
        KeyEvent.KEYCODE_BUTTON_9,
        KeyEvent.KEYCODE_BUTTON_10,
        KeyEvent.KEYCODE_BUTTON_11,
        KeyEvent.KEYCODE_BUTTON_12,
        KeyEvent.KEYCODE_BUTTON_13,
        KeyEvent.KEYCODE_BUTTON_14,
        KeyEvent.KEYCODE_BUTTON_15,
        KeyEvent.KEYCODE_BUTTON_16,
        KeyEvent.KEYCODE_BUTTON_MODE
    )

    private val AXES_MAP = mapOf(
        MotionEvent.AXIS_BRAKE to KeyEvent.KEYCODE_BUTTON_L2,
        MotionEvent.AXIS_THROTTLE to KeyEvent.KEYCODE_BUTTON_R2,
        MotionEvent.AXIS_LTRIGGER to KeyEvent.KEYCODE_BUTTON_L2,
        MotionEvent.AXIS_RTRIGGER to KeyEvent.KEYCODE_BUTTON_R2
    )

    private val DEFAULT_BINDINGS = mapOf(
        KeyEvent.KEYCODE_BUTTON_A to KeyEvent.KEYCODE_BUTTON_B,
        KeyEvent.KEYCODE_BUTTON_B to KeyEvent.KEYCODE_BUTTON_A,
        KeyEvent.KEYCODE_BUTTON_X to KeyEvent.KEYCODE_BUTTON_Y,
        KeyEvent.KEYCODE_BUTTON_Y to KeyEvent.KEYCODE_BUTTON_X
    ).withDefault { if (it in InputDeviceManager.OUTPUT_KEYS) it else KeyEvent.KEYCODE_UNKNOWN }

    override fun getInputKeys() = INPUT_KEYS

    override fun getAxesMap() = AXES_MAP

    override fun getDefaultBindings() = DEFAULT_BINDINGS

    override fun isEnabledByDefault(appContext: Context, device: InputDevice): Boolean {
        return device.hasKeys(*MINIMAL_KEYS_DEFAULT_ENABLED).all { it }
    }

    override fun getCustomizableKeys(): List<Int> = CUSTOMIZABLE_KEYS

    override fun getSupportedShortcuts(): List<GameMenuShortcut> = listOf(
        GameMenuShortcut(
            "L3 + R3",
            setOf(KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR)
        ),
        GameMenuShortcut(
            "Select + Start",
            setOf(KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_BUTTON_SELECT)
        )
    )

    override fun isSupported(device: InputDevice): Boolean {
        return sequenceOf(
            device.sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD,
            device.hasKeys(*MINIMAL_SUPPORTED_KEYS).all { it },
            device.isVirtual.not(),
            device.controllerNumber > 0
        ).all { it }
    }
}
