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

import android.util.Log
import timber.log.Timber
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord

class TimberLoggingHandler : Handler() {
    @Throws(SecurityException::class)
    override fun close() { }

    override fun flush() {}

    override fun publish(record: LogRecord) {
        val tag = loggerNameToTag(record.loggerName)
        val level = getAndroidLevel(record.level)
        Timber.tag(tag).log(level, record.message)
    }

    // https://android.googlesource.com/platform/frameworks/base.git/+/master/core/java/com/android/internal/logging/AndroidHandler.java
    private fun getAndroidLevel(level: Level): Int {
        val value = level.intValue()
        return when {
            value >= 1000 -> Log.ERROR // SEVERE
            value >= 900 -> Log.WARN // WARNING
            value >= 800 -> Log.INFO // INFO
            else -> Log.DEBUG
        }
    }

    // https://android.googlesource.com/platform/libcore/+/master/dalvik/src/main/java/dalvik/system/DalvikLogging.java
    private fun loggerNameToTag(loggerName: String?): String {
        if (loggerName == null) {
            return "null"
        }
        val length = loggerName.length
        if (length <= 23) {
            return loggerName
        }
        val lastPeriod = loggerName.lastIndexOf(".")
        return if (length - (lastPeriod + 1) <= 23) {
            loggerName.substring(lastPeriod + 1)
        } else {
            loggerName.substring(loggerName.length - 23)
        }
    }
}
