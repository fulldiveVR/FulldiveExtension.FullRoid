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

package com.swordfish.lemuroid.app.shared.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.ImmersiveActivity
import com.swordfish.lemuroid.app.shared.library.LibraryIndexMonitor
import com.swordfish.lemuroid.app.shared.main.PostGameHandler
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncMonitor
import com.swordfish.lemuroid.app.tv.channel.ChannelUpdateWork
import com.swordfish.lemuroid.app.tv.shared.TVHelper
import com.swordfish.lemuroid.app.utils.android.displayErrorDialog
import com.swordfish.lemuroid.app.utils.livedata.CombinedLiveData
import com.swordfish.lemuroid.common.animationDuration
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * This activity is used as an entry point when launching games from external shortcuts. This activity
 * still runs in the main process so it can peek into background job status and wait for them to
 * complete.
 */
class ExternalGameLauncherActivity : ImmersiveActivity() {

    @Inject lateinit var retrogradeDatabase: RetrogradeDatabase
    @Inject lateinit var postGameHandler: PostGameHandler
    @Inject lateinit var coresSelection: CoresSelection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_loading)
        if (savedInstanceState == null) {

            val gameId = intent.data?.pathSegments?.let { it[it.size - 1].toInt() }!!

            val publisher = LiveDataReactiveStreams.toPublisher(this, getLoadingLiveData())

            val loadingSubject = BehaviorSubject.createDefault(true)

            Observable.fromPublisher(publisher)
                .filter { !it }
                .firstElement()
                .flatMapSingle {
                    retrogradeDatabase.gameDao()
                        .selectById(gameId).subscribeOn(Schedulers.io())
                        .flatMapSingle { game ->
                            coresSelection.getCoreConfigForSystem(GameSystem.findById(game.systemId))
                                .map { game to it }
                        }
                }
                .subscribeOn(Schedulers.io())
                .delay(animationDuration().toLong(), TimeUnit.MILLISECONDS)
                .doOnSubscribe { loadingSubject.onNext(true) }
                .doAfterTerminate { loadingSubject.onNext(false) }
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope())
                .subscribeBy(
                    { displayErrorMessage() },
                    { (game, systemCoreConfig) ->
                        BaseGameActivity.launchGame(
                            this,
                            systemCoreConfig,
                            game,
                            true,
                            TVHelper.isTV(applicationContext)
                        )
                    }
                )

            loadingSubject
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope())
                .subscribeBy {
                    findViewById<View>(R.id.progressBar).setVisibleOrGone(it)
                }
        }
    }

    private fun displayErrorMessage() {
        displayErrorDialog(R.string.game_loader_error_load_game, R.string.ok) { finish() }
    }

    private fun getLoadingLiveData(): LiveData<Boolean> {
        return CombinedLiveData(
            LibraryIndexMonitor(applicationContext).getLiveData(),
            SaveSyncMonitor(applicationContext).getLiveData()
        ) { libraryIndex, saveSync ->
            libraryIndex || saveSync
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            BaseGameActivity.REQUEST_PLAY_GAME -> {
                val duration = data?.extras?.getLong(BaseGameActivity.PLAY_GAME_RESULT_SESSION_DURATION)
                val game = data?.extras?.getSerializable(BaseGameActivity.PLAY_GAME_RESULT_GAME) as Game
                postGameHandler.handleAfterGame(this, false, game, duration!!)
                    .doOnTerminate { finish() }
                    .subscribeBy { }

                val leanback = data?.extras?.getBoolean(BaseGameActivity.PLAY_GAME_RESULT_LEANBACK)
                if (leanback == true) {
                    ChannelUpdateWork.enqueue(applicationContext)
                }
            }
        }
    }
}
