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

package com.swordfish.lemuroid.app.mobile.feature.tilt

import com.swordfish.lemuroid.common.math.MathUtils
import com.swordfish.radialgamepad.library.RadialGamePad

class CrossTiltTracker(val id: Int) : TiltTracker {

    override fun updateTracking(
        xTilt: Float,
        yTilt: Float,
        pads: Sequence<RadialGamePad>
    ) {
        if (MathUtils.distance(xTilt, 0.5f, yTilt, 0.5f) > ACTIVATION_THRESHOLD) {
            pads.forEach { it.simulateMotionEvent(id, xTilt, yTilt) }
        } else if (MathUtils.distance(xTilt, 0.5f, yTilt, 0.5f) < DEACTIVATION_THRESHOLD) {
            pads.forEach { it.simulateMotionEvent(id, 0.5f, 0.5f) }
        }
    }

    override fun trackedIds(): Set<Int> = setOf(id)

    override fun stopTracking(pads: Sequence<RadialGamePad>) {
        pads.forEach { it.simulateClearMotionEvent(id) }
    }

    companion object {
        private const val ACTIVATION_THRESHOLD = 0.25f
        private const val DEACTIVATION_THRESHOLD = 0.225f
    }
}
