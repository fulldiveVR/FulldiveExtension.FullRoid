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

package com.swordfish.lemuroid.app.utils.livedata

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * LiveData throttling value emissions so they don't happen more often than [delayMs].
 */
class ThrottledLiveData<T>(source: LiveData<T>, delayMs: Long) : MediatorLiveData<T>() {
    private val handler = Handler(Looper.getMainLooper())
    var delayMs = delayMs
        private set

    private var isValueDelayed = false
    private var delayedValue: T? = null
    private var delayRunnable: Runnable? = null
        set(value) {
            field?.let { handler.removeCallbacks(it) }
            value?.let { handler.postDelayed(it, delayMs) }
            field = value
        }
    private val objDelayRunnable = Runnable { if (consumeDelayedValue()) startDelay() }

    init {
        addSource(source) { newValue ->
            if (delayRunnable == null) {
                value = newValue
                startDelay()
            } else {
                isValueDelayed = true
                delayedValue = newValue
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        consumeDelayedValue()
    }

    // start counting the delay or clear it if conditions are not met
    private fun startDelay() {
        delayRunnable = if (delayMs > 0 && hasActiveObservers()) objDelayRunnable else null
    }

    private fun consumeDelayedValue(): Boolean {
        delayRunnable = null
        return if (isValueDelayed) {
            value = delayedValue
            delayedValue = null
            isValueDelayed = false
            true
        } else false
    }
}
