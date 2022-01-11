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

package com.swordfish.lemuroid.app.shared.main

import android.app.Activity
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.dao.updateAsync
import com.swordfish.lemuroid.lib.library.db.entity.Game
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class PostGameHandler(
    private val reviewManager: ReviewManager,
    private val retrogradeDb: RetrogradeDatabase
) {

    fun handleAfterGame(
        activity: Activity,
        enableRatingFlow: Boolean,
        game: Game,
        duration: Long
    ): Completable {

        return Single.just(game)
            .flatMapCompletable { game ->
                val comps = mutableListOf<Completable>().apply {
                    add(updateGamePlayedTimestamp(game))

                    if (enableRatingFlow) {
                        add(displayReviewRequest(activity, duration))
                    }
                }
                Completable.concat(comps)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun displayReviewRequest(activity: Activity, durationMillis: Long): Completable {
        return Completable.timer(500, TimeUnit.MILLISECONDS)
            .andThen { reviewManager.startReviewFlow(activity, durationMillis) }
    }

    private fun updateGamePlayedTimestamp(game: Game): Completable {
        return retrogradeDb.gameDao()
            .updateAsync(game.copy(lastPlayedAt = System.currentTimeMillis()))
    }
}
