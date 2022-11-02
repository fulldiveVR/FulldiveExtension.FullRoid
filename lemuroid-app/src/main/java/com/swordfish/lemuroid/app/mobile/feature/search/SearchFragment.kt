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

package com.swordfish.lemuroid.app.mobile.feature.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.GamesAdapter
import com.swordfish.lemuroid.app.mobile.shared.RecyclerViewFragment
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.common.coroutines.launchOnState
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce

class SearchFragment : RecyclerViewFragment() {

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    @Inject
    lateinit var gameInteractor: GameInteractor

    @Inject
    lateinit var coverLoader: CoverLoader

    private lateinit var searchViewModel: SearchViewModel

    private val searchDebounce = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = SearchViewModel.Factory(retrogradeDb)
        searchViewModel = ViewModelProvider(this, factory)[SearchViewModel::class.java]

        initializeMenuProvider()

        val gamesAdapter = GamesAdapter(R.layout.layout_game_list, gameInteractor, coverLoader)

        launchOnState(Lifecycle.State.RESUMED) {
            searchViewModel.searchResults
                .collect { gamesAdapter.submitData(viewLifecycleOwner.lifecycle, it) }
        }

        launchOnState(Lifecycle.State.RESUMED) {
            searchDebounce.debounce(1000)
                .collect { searchViewModel.queryString.value = it }
        }

        gamesAdapter.addLoadStateListener { loadState ->
            updateEmptyViewVisibility(loadState, gamesAdapter.itemCount)
        }

        recyclerView?.apply {
            adapter = gamesAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun initializeMenuProvider() {
        val menuHost: MenuHost = requireActivity() as MenuHost
        val menuProvider = object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {}

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return true
            }

            override fun onMenuClosed(menu: Menu) {}

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_menu, menu)

                val searchItem = menu.findItem(R.id.action_search)
                setupSearchMenuItem(searchItem)
            }
        }
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupSearchMenuItem(searchItem: MenuItem) {
        val onExpandListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                activity?.onBackPressed()
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem?) = true
        }
        searchItem.setOnActionExpandListener(onExpandListener)

        searchItem.expandActionView()

        val searchView = searchItem.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE

        val onQueryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchDebounce.value = newText
                return true
            }
        }
        searchView.setOnQueryTextListener(onQueryTextListener)
        searchView.setQuery(searchViewModel.queryString.value, false)
    }

    @dagger.Module
    class Module
}
