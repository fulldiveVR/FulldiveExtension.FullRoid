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

package com.swordfish.lemuroid.app.tv.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.swordfish.lemuroid.app.shared.library.LibraryIndexMonitor
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.metaSystemID
import io.reactivex.Observable

class TVHomeViewModel(retrogradeDb: RetrogradeDatabase, appContext: Context) : ViewModel() {

    companion object {
        const val CAROUSEL_MAX_ITEMS = 10
    }

    class Factory(
        val retrogradeDb: RetrogradeDatabase,
        val appContext: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TVHomeViewModel(retrogradeDb, appContext) as T
        }
    }

    val indexingInProgress = LibraryIndexMonitor(appContext).getLiveData()

    val recentGames = retrogradeDb.gameDao().rxSelectFirstUnfavoriteRecents(CAROUSEL_MAX_ITEMS)

    val favoritesGames = retrogradeDb.gameDao().rxSelectFirstFavoritesRecents(CAROUSEL_MAX_ITEMS + 1)

    val availableSystems: Observable<List<MetaSystemInfo>> = retrogradeDb.gameDao()
        .selectSystemsWithCount()
        .map { systemCounts ->
            systemCounts.filter { (_, count) -> count > 0 }
                .map { (systemId, count) -> GameSystem.findById(systemId).metaSystemID() to count }
                .groupBy { (metaSystemId, _) -> metaSystemId }
                .map { (metaSystemId, counts) -> MetaSystemInfo(metaSystemId, counts.sumBy { it.second }) }
        }
}
