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

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import com.flurry.android.FlurryAgent
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.*
import javax.inject.Inject

class FulldiveActionTracker @Inject constructor(
    context: Context,
    private val tagReader: ITagReader
) : IActionTracker {
    private var firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logAction(event: String, bundle: Bundle, screen: String?) {
        val processedEvent = tagReader.read(event)
        if (screen != null) {
            val processedScreen = tagReader.read(screen)
            bundle.putString(TrackerConstants.PARAM_SCREEN_KEY, processedScreen)
        }
        FdLog.d(TAG, "event: $processedEvent => $bundle => ${bundle.toStringMap()}")
        firebaseAnalytics.logEvent(processedEvent.take(40), bundle)
        FlurryAgent.logEvent(processedEvent, bundle.toStringMap())
    }

    override fun logScreenTime(screen: String, startTime: Long, endTime: Long) {
        val processedScreen = tagReader.read(screen)
        val bundle = bundleOf(
            TrackerConstants.PARAM_SCREEN_KEY to processedScreen,
            TrackerConstants.PARAM_START_TIME to startTime,
            TrackerConstants.PARAM_END_TIME to endTime,
            TrackerConstants.PARAM_DURATION to (endTime - startTime)
        )

        val firebaseEvent =
            "${TrackerConstants.FIREBASE_SCREEN_TIME}_${processedScreen.toLowerCase(Locale.getDefault())}".take(
                40
            )
        FdLog.d(TAG, "event firebase: $firebaseEvent: $bundle")
        firebaseAnalytics.logEvent(firebaseEvent, bundle)

        val flurryEvent = "${TrackerConstants.FLURRY_SCREEN_TIME}: $processedScreen"
        val params = bundle.toStringMap()
        FdLog.d(TAG, "event flurry: $flurryEvent: $params")
        FlurryAgent.logEvent(flurryEvent, params)
    }

    private fun Bundle.toStringMap(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        this.keySet().forEach { key ->
            get(key)?.let { value ->
                result[key] = value.toString()
            }
        }
        return result
    }

    companion object {
        private const val TAG = "FulldiveActionTracker"
    }
}
