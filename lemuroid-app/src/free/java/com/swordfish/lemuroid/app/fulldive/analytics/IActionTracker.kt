/*
 * Copyright (c) 2022 FullDive
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
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.fulldive.analytics

import android.os.Bundle

interface IActionTracker {
    fun logAction(event: String, bundle: Bundle = Bundle(), screen: String? = null) = Unit
    fun logScreenTime(screen: String, startTime: Long, endTime: Long) = Unit
    fun logScreenScroll(screen: String, scrollUpCount: Int, scrollDownCount: Int) = Unit
}

object GlobalActionTracker : IActionTracker {
    var instance: IActionTracker? = null

    override fun logAction(event: String, bundle: Bundle, screen: String?) {
        try {
            instance?.logAction(event, bundle, screen)
        } catch (ignore: Exception) {
        }
    }

    override fun logScreenTime(screen: String, startTime: Long, endTime: Long) {
        try {
            instance?.logScreenTime(screen, startTime, endTime)
        } catch (ignore: Exception) {
        }
    }

    override fun logScreenScroll(screen: String, scrollUpCount: Int, scrollDownCount: Int) {
        try {
            instance?.logScreenScroll(screen, scrollUpCount, scrollDownCount)
        } catch (ignore: Exception) {
        }
    }
}
