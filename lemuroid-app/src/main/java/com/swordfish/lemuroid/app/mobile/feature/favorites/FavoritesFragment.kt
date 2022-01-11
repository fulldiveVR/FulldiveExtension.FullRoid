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

package com.swordfish.lemuroid.app.mobile.feature.favorites

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.paging.cachedIn
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.DynamicGridLayoutManager
import com.swordfish.lemuroid.app.mobile.shared.GamesAdapter
import com.swordfish.lemuroid.app.mobile.shared.GridSpaceDecoration
import com.swordfish.lemuroid.app.mobile.shared.RecyclerViewFragment
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.ui.setVisibleOrGone
import javax.inject.Inject

class FavoritesFragment : RecyclerViewFragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor

    private lateinit var favoritesViewModel: FavoritesViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesViewModel = ViewModelProviders.of(this, FavoritesViewModel.Factory(retrogradeDb))
            .get(FavoritesViewModel::class.java)

        val gamesAdapter = GamesAdapter(R.layout.layout_game_grid, gameInteractor)
        favoritesViewModel.favorites.cachedIn(lifecycle).observe(this) {
            gamesAdapter.submitData(lifecycle, it)
        }

        gamesAdapter.addLoadStateListener {
            emptyView?.setVisibleOrGone(gamesAdapter.itemCount == 0)
        }

        recyclerView?.apply {
            this.adapter = gamesAdapter
            this.layoutManager = DynamicGridLayoutManager(context)

            val spacingInPixels = resources.getDimensionPixelSize(R.dimen.grid_spacing)
            GridSpaceDecoration.setSingleGridSpaceDecoration(this, spacingInPixels)
        }
    }

    @dagger.Module
    class Module
}
