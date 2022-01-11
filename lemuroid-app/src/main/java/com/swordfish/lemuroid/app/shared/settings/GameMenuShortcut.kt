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

package com.swordfish.lemuroid.app.shared.settings

import android.view.InputDevice
import android.view.KeyEvent

data class GameMenuShortcut(val name: String, val keys: Set<Int>) {

    companion object {

        fun getDefault(inputDevice: InputDevice): GameMenuShortcut? {
            return ALL_SHORTCUTS
                .firstOrNull { shortcut ->
                    inputDevice.hasKeys(*(shortcut.keys.toIntArray())).all { it }
                }
        }

        val ALL_SHORTCUTS = listOf(
            GameMenuShortcut(
                "L3 + R3",
                setOf(KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR)
            ),
            GameMenuShortcut(
                "Select + Start",
                setOf(KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_BUTTON_SELECT)
            )
        )

        fun findByName(name: String): GameMenuShortcut {
            return ALL_SHORTCUTS.first { it.name == name }
        }
    }
}
