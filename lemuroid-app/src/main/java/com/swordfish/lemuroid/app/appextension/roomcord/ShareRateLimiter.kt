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

package com.swordfish.lemuroid.app.appextension.roomcord

import android.content.Context
import android.text.format.DateUtils
import com.swordfish.lemuroid.R

/**
 * Throttles "Share to Roomcord" so the room cannot be flooded from a single install:
 * at most one share per [MIN_INTERVAL_MILLIS], at most [MAX_PER_WINDOW] per rolling
 * [WINDOW_MILLIS], and never the same message twice inside that window.
 *
 * State lives in [SharedPreferences] rather than in memory on purpose — an in-memory counter is
 * bypassed by simply killing the app.
 *
 * Every instance reads and writes the same preferences file, so it is safe to construct one
 * wherever it is needed instead of sharing a single object.
 */
class ShareRateLimiter(private val context: Context) {

    sealed interface Decision {
        data object Allowed : Decision

        data class TooSoon(val retryInMillis: Long) : Decision

        data class DailyLimitReached(val retryInMillis: Long) : Decision

        data object Duplicate : Decision
    }

    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    fun check(
        content: String,
        now: Long = System.currentTimeMillis(),
    ): Decision {
        val entries = loadEntries(now)
        val oldest = entries.minByOrNull { it.timestamp }
        val newest = entries.maxByOrNull { it.timestamp }

        // Most binding constraint first: being out of daily quota is not fixable by editing
        // the text or by waiting five minutes, so reporting anything else would mislead.
        return when {
            entries.size >= MAX_PER_WINDOW && oldest != null ->
                Decision.DailyLimitReached((oldest.timestamp + WINDOW_MILLIS - now).coerceAtLeast(0L))

            newest != null && now - newest.timestamp < MIN_INTERVAL_MILLIS ->
                Decision.TooSoon((newest.timestamp + MIN_INTERVAL_MILLIS - now).coerceAtLeast(0L))

            entries.any { it.hash == hashOf(content) } -> Decision.Duplicate

            else -> Decision.Allowed
        }
    }

    fun recordSent(
        content: String,
        now: Long = System.currentTimeMillis(),
    ) {
        val entries = loadEntries(now) + Entry(now, hashOf(content))
        prefs.edit()
            .putString(KEY_SHARE_LOG, entries.joinToString(ENTRY_SEPARATOR) { "${it.timestamp}$FIELD_SEPARATOR${it.hash}" })
            .apply()
    }

    fun describe(decision: Decision): String =
        when (decision) {
            is Decision.Allowed -> ""
            is Decision.TooSoon ->
                context.getString(R.string.roomcord_share_limit_too_soon, relativeRetry(decision.retryInMillis))
            is Decision.DailyLimitReached ->
                context.getString(R.string.roomcord_share_limit_daily, relativeRetry(decision.retryInMillis))
            is Decision.Duplicate -> context.getString(R.string.roomcord_share_limit_duplicate)
        }

    /** Locale-aware "in 3 minutes" so no per-unit strings have to be translated by hand. */
    private fun relativeRetry(retryInMillis: Long): CharSequence {
        val now = System.currentTimeMillis()
        return DateUtils.getRelativeTimeSpanString(
            now + retryInMillis.coerceAtLeast(DateUtils.MINUTE_IN_MILLIS),
            now,
            DateUtils.MINUTE_IN_MILLIS,
        )
    }

    private fun loadEntries(now: Long): List<Entry> =
        prefs.getString(KEY_SHARE_LOG, "")
            .orEmpty()
            .split(ENTRY_SEPARATOR)
            .mapNotNull { Entry.parse(it) }
            // A negative age means the device clock moved backwards. Keeping the entry is the
            // safe direction: it stays counted instead of silently clearing the quota.
            .filter { now - it.timestamp < WINDOW_MILLIS }
            .sortedBy { it.timestamp }

    private fun hashOf(content: String): Int = normalize(content).hashCode()

    private fun normalize(content: String): String =
        content.trim()
            .replace(WHITESPACE, " ")
            .lowercase()

    private data class Entry(val timestamp: Long, val hash: Int) {
        companion object {
            fun parse(raw: String): Entry? {
                val parts = raw.split(FIELD_SEPARATOR)
                if (parts.size != 2) return null
                val timestamp = parts[0].toLongOrNull() ?: return null
                val hash = parts[1].toIntOrNull() ?: return null
                return Entry(timestamp, hash)
            }
        }
    }

    companion object {
        const val MIN_INTERVAL_MILLIS = 5 * 60 * 1000L
        const val MAX_PER_WINDOW = 5
        const val WINDOW_MILLIS = 24 * 60 * 60 * 1000L

        private const val PREFS_NAME = "roomcord_share_prefs"
        private const val KEY_SHARE_LOG = "share_log"
        private const val ENTRY_SEPARATOR = ";"
        private const val FIELD_SEPARATOR = ":"
        private val WHITESPACE = Regex("\\s+")
    }
}
