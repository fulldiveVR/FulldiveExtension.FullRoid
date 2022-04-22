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

package com.swordfish.lemuroid.app.tv.search

import android.content.Context
import android.os.Bundle
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.paging.PagingDataAdapter
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.ObjectAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.paging.cachedIn
import com.jakewharton.rxrelay2.PublishRelay
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.tv.shared.GamePresenter
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.util.subscribeBy
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.android.support.AndroidSupportInjection
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TVSearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor
    @Inject lateinit var coverLoader: CoverLoader

    private val searchRelay: PublishRelay<String> = PublishRelay.create()

    private lateinit var rowsAdapter: ArrayObjectAdapter
    private lateinit var searchViewModel: TVSearchViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setOnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Game -> gameInteractor.onGamePlay(item)
            }
        }

        rowsAdapter = createAdapter()

        val factory = TVSearchViewModel.Factory(retrogradeDb)
        searchViewModel = ViewModelProvider(this, factory).get(TVSearchViewModel::class.java)

        searchViewModel.searchResults.cachedIn(lifecycle).observe(this) {
            val gamesAdapter = (rowsAdapter.get(0) as ListRow).adapter as PagingDataAdapter<Game>
            gamesAdapter.submitData(lifecycle, it)
        }

        searchRelay
            .debounce(1, TimeUnit.SECONDS)
            .distinctUntilChanged()
            .autoDispose(scope())
            .subscribeBy { searchViewModel.queryString.postValue(it) }

        setSearchResultProvider(this)
    }

    private fun createAdapter(): ArrayObjectAdapter {
        val searchAdapter = ArrayObjectAdapter(ListRowPresenter())

        val gamePresenter = GamePresenter(
            resources.getDimensionPixelSize(R.dimen.card_size),
            gameInteractor,
            coverLoader
        )

        val gamesAdapter = PagingDataAdapter(gamePresenter, Game.DIFF_CALLBACK)
        searchAdapter.add(
            ListRow(
                HeaderItem(resources.getString(R.string.tv_search_results)),
                gamesAdapter
            )
        )

        return searchAdapter
    }

    override fun getResultsAdapter(): ObjectAdapter {
        return rowsAdapter
    }

    override fun onQueryTextChange(query: String): Boolean {
        searchRelay.accept(query)
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        searchRelay.accept(query)
        return true
    }

    @dagger.Module
    class Module
}
