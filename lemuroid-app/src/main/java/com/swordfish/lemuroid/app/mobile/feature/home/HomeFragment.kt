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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject lateinit var retrogradeDb: RetrogradeDatabase
    @Inject lateinit var gameInteractor: GameInteractor
    @Inject lateinit var settingsInteractor: SettingsInteractor

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeViewModel =
            ViewModelProviders.of(
                this,
                HomeViewModel.Factory(context!!.applicationContext, retrogradeDb)
            ).get(HomeViewModel::class.java)

        // Disable snapping in carousel view
        Carousel.setDefaultGlobalSnapHelperFactory(null)

        val pagingController = EpoxyHomeController(gameInteractor, settingsInteractor)

        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recyclerview)
        val layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.VERTICAL, false)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = pagingController.adapter

        homeViewModel.recentGames.observe(this) {
            pagingController.updateRecents(it)
        }

        homeViewModel.favoriteGames.observe(this) {
            pagingController.updateFavorites(it)
        }

        homeViewModel.discoverGames.observe(this) {
            pagingController.updateDiscover(it)
        }

        homeViewModel.indexingInProgress.observe(this) {
            pagingController.updateLibraryIndexingInProgress(it)
        }
    }

    @dagger.Module
    class Module
}
