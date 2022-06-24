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

package com.swordfish.lemuroid.lib.logging

import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.Observable
import timber.log.Timber

class RxTimberTree : Timber.DebugTree() {

    private val relay = ReplayRelay.createWithSize<LogEntry>(500)

    val observable: Observable<LogEntry> = relay.hide()

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        relay.accept(LogEntry(System.currentTimeMillis(), priority, tag, message, t))
    }

    data class LogEntry(
        val timestamp: Long,
        val priority: Int,
        val tag: String?,
        val message: String?,
        val error: Throwable?
    )
}
