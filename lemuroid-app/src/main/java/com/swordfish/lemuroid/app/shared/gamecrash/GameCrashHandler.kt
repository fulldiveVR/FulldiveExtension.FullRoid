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

package com.swordfish.lemuroid.app.shared.gamecrash

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.swordfish.lemuroid.BuildConfig

class GameCrashHandler(
    private val activity: Activity,
    private val systemHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        val message = if (BuildConfig.DEBUG) {
            Log.getStackTraceString(throwable)
        } else {
            throwable.message
        }

        activity.startActivity(
            Intent(activity, GameCrashActivity::class.java).apply {
                putExtra(GameCrashActivity.EXTRA_MESSAGE, message)
            }
        )
        activity.finish()
        systemHandler?.uncaughtException(thread, throwable)
    }
}
