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

package com.swordfish.lemuroid.common.view

import android.animation.ObjectAnimator
import android.view.View
import android.widget.ProgressBar
import androidx.core.animation.addListener
import androidx.core.view.isVisible

fun View.animateVisibleOrGone(visible: Boolean, durationInMs: Long) {
    val alpha = if (visible) 1.0f else 0.0f
    ObjectAnimator.ofFloat(this, "alpha", alpha).apply {
        duration = durationInMs
        setAutoCancel(true)
        addListener(
            onStart = {
                if (visible) isVisible = true
            },
            onEnd = {
                if (!visible) isVisible = false
            }
        )
        start()
    }
}

fun ProgressBar.animateProgress(progress: Int, durationInMs: Long) {
    ObjectAnimator.ofInt(this, "progress", progress).apply {
        duration = durationInMs
        setAutoCancel(true)
        start()
    }
}
