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

package com.swordfish.lemuroid.app.mobile.feature.home

import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.carousel
import com.swordfish.lemuroid.BuildConfig
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.withModelsFrom
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.lib.library.db.entity.Game

class EpoxyHomeController(
    private val gameInteractor: GameInteractor,
    private val settingsInteractor: SettingsInteractor
) : AsyncEpoxyController() {

    private var recentGames = listOf<Game>()
    private var favoriteGames = listOf<Game>()
    private var discoverGames = listOf<Game>()

    private var libraryIndexingInProgress = false

    fun updateRecents(games: List<Game>) {
        recentGames = games
        requestDelayedModelBuild(UPDATE_DELAY_TIME)
    }

    fun updateFavorites(games: List<Game>) {
        favoriteGames = games
        requestDelayedModelBuild(UPDATE_DELAY_TIME)
    }

    fun updateDiscover(games: List<Game>) {
        discoverGames = games
        requestDelayedModelBuild(UPDATE_DELAY_TIME)
    }

    fun updateLibraryIndexingInProgress(indexingInProgress: Boolean) {
        libraryIndexingInProgress = indexingInProgress
        requestDelayedModelBuild(UPDATE_DELAY_TIME)
    }

    override fun buildModels() {
        if (favoriteGames.isNotEmpty()) {
            addCarousel("favorites", R.string.favorites, favoriteGames)
        }

        if (recentGames.isNotEmpty()) {
            addCarousel("recent", R.string.recent, recentGames)
        }

        if (discoverGames.isNotEmpty()) {
            addCarousel("discover", R.string.discover, discoverGames)
        }

        if (recentGames.isEmpty() && favoriteGames.isEmpty() && discoverGames.isEmpty()) {
            addEmptyView()
        }
    }

    private fun addCarousel(id: String, titleId: Int, games: List<Game>) {
        epoxyHomeSection {
            id("section_$id")
            title(titleId)
        }
        carousel {
            id("carousel_$id")
            paddingRes(R.dimen.grid_spacing)
            withModelsFrom(games) { item ->
                EpoxyGameView_()
                    .id(item.id)
                    .game(item)
                    .gameInteractor(gameInteractor)
            }
        }
    }

    private fun addEmptyView() {
        epoxyEmptyViewAction {
            id("empty_home")
                .title(R.string.home_empty_title)
                .message(R.string.home_empty_message)
                .action(R.string.home_empty_action)
                .actionEnabled(!libraryIndexingInProgress)
                .onClick { settingsInteractor.changeLocalStorageFolder() }
        }
    }

    init {
        if (BuildConfig.DEBUG) {
            isDebugLoggingEnabled = true
        }
    }

    override fun onExceptionSwallowed(exception: RuntimeException) {
        throw exception
    }

    companion object {
        const val UPDATE_DELAY_TIME = 160
    }
}
