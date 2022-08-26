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

package com.swordfish.lemuroid.app.mobile.feature.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.PopupManager
import com.swordfish.lemuroid.app.appextension.PopupManager.Companion.DISCORD_INVITATION
import com.swordfish.lemuroid.app.fulldive.analytics.IActionTracker
import com.swordfish.lemuroid.app.fulldive.analytics.TrackerConstants
import com.swordfish.lemuroid.app.mobile.feature.proinfo.DiscordPopupLayout
import com.swordfish.lemuroid.app.mobile.feature.proinfo.ProPopupLayout
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    @Inject
    lateinit var gameInteractor: GameInteractor

    @Inject
    lateinit var coverLoader: CoverLoader

    @Inject
    lateinit var settingsInteractor: SettingsInteractor

    @Inject
    lateinit var actionTracker: IActionTracker

    @Inject
    lateinit var popupManager: PopupManager

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
            ViewModelProvider(
                this,
                HomeViewModel.Factory(requireContext().applicationContext, retrogradeDb)
            ).get(HomeViewModel::class.java)

        // Disable snapping in carousel view
        Carousel.setDefaultGlobalSnapHelperFactory(null)

        val pagingController = EpoxyHomeController(gameInteractor, settingsInteractor, coverLoader)

        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recyclerview)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = pagingController.adapter

        homeViewModel.recentGames.observe(viewLifecycleOwner) {
            pagingController.updateRecents(it)
        }

        homeViewModel.favoriteGames.observe(viewLifecycleOwner) {
            pagingController.updateFavorites(it)
        }

        homeViewModel.discoverGames.observe(viewLifecycleOwner) {
            pagingController.updateDiscover(it)
        }

        homeViewModel.indexingInProgress.observe(viewLifecycleOwner) {
            pagingController.updateLibraryIndexingInProgress(it)
        }

        val isProPopupVisible = popupManager.isProPopupVisible()
        view.findViewById<ProPopupLayout>(R.id.proPopupLayout).apply {
            this.isVisible = isProPopupVisible
            if (isProPopupVisible) {
                popupManager.setProVersionPopupClosed(false)
                actionTracker.logAction(TrackerConstants.EVENT_PRO_POPUP_SHOWN)
            }
            onClickListener = {
                actionTracker.logAction(TrackerConstants.EVENT_PRO_TUTORIAL_OPENED_FROM_PRO_POPUP)
                findNavController().navigate(R.id.navigation_pro_tutorial)
            }
            onCloseClickListener = {
                actionTracker.logAction(TrackerConstants.EVENT_PRO_POPUP_CLOSED)
                popupManager.setProVersionPopupClosed(true)
            }
            showSnackbar()
        }

        val isDiscordPopupVisible = popupManager.isDiscordPopupVisible()
        view.findViewById<DiscordPopupLayout>(R.id.discordPopupLayout).apply {
            this.isVisible = isDiscordPopupVisible
            if (isDiscordPopupVisible) {
                popupManager.setDiscordPopupClosed(false)
                actionTracker.logAction(TrackerConstants.EVENT_DISCORD_POPUP_SHOWN)
            }
            onClickListener = {
                actionTracker.logAction(TrackerConstants.EVENT_DISCORD_POPUP_CLICKED)
                popupManager.setDiscordPopupClosed(true)
                startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(DISCORD_INVITATION) })
            }
            onCloseClickListener = {
                actionTracker.logAction(TrackerConstants.EVENT_DISCORD_POPUP_CLOSED)
                popupManager.setDiscordPopupClosed(true)
            }
            showSnackbar()
        }
    }

    @dagger.Module
    class Module
}
