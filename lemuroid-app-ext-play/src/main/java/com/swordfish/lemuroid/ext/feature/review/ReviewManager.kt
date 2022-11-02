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

package com.swordfish.lemuroid.ext.feature.review

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.tasks.await

class ReviewManager {
    private var reviewManager: ReviewManager? = null
    private var reviewInfo: ReviewInfo? = null

    suspend fun initialize(context: Context) {
        reviewManager = ReviewManagerFactory.create(context)

        runCatching {
            reviewInfo = reviewManager?.requestReviewFlow()?.await()
        }
    }

    suspend fun launchReviewFlow(activity: Activity, sessionTimeMillis: Long) {
        // Only sessions which lasted more than 10 minutes considered good sessions
        if (sessionTimeMillis < MIN_GAME_SESSION_LENGTH) {
            return
        }

        reviewInfo?.let {
            reviewManager?.launchReviewFlow(activity, it)?.await()
        }
    }

    companion object {
        private val MIN_GAME_SESSION_LENGTH = TimeUnit.MINUTES.toMillis(5)
    }
}
