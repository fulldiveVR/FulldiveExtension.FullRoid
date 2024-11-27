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

package com.swordfish.lemuroid.app.mobile.feature.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.swordfish.lemuroid.app.shared.library.PendingOperationsMonitor
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel(appContext: Context, private val saveSyncManager: SaveSyncManager) : ViewModel() {
    class Factory(
        private val appContext: Context,
        private val saveSyncManager: SaveSyncManager,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(appContext, saveSyncManager) as T
        }
    }

    data class UiState(
        val operationInProgress: Boolean = false,
        val saveSyncEnabled: Boolean = false,
        val displaySearch: Boolean = false,
        val searchQuery: String = "",
    )

    private val currentRouteFlow = MutableStateFlow(MainRoute.HOME)
    private val saveSyncEnabledFlow = MutableStateFlow(false)
    private val operationInProgressFlow = PendingOperationsMonitor(appContext).anyOperationInProgress()
    private val searchQueryFlow = MutableStateFlow("")

    val state = buildStateFlow()

    private fun buildStateFlow(): StateFlow<UiState> {
        val combinedFlows =
            combine(
                currentRouteFlow,
                saveSyncEnabledFlow,
                operationInProgressFlow,
                searchQueryFlow,
            ) { currentRoute, saveSyncEnabled, operationInProgress, searchQuery ->
                UiState(
                    operationInProgress = operationInProgress,
                    saveSyncEnabled = saveSyncEnabled,
                    displaySearch = currentRoute == MainRoute.SEARCH,
                    searchQuery = searchQuery,
                )
            }

        return combinedFlows
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = UiState(),
            )
    }

    fun changeRoute(currentRoute: MainRoute) {
        val current = saveSyncManager.isSupported() && saveSyncManager.isConfigured()
        saveSyncEnabledFlow.value = current

        currentRouteFlow.value = currentRoute
    }

    fun changeQueryString(newSearchQuery: String) {
        searchQueryFlow.value = newSearchQuery
    }
}
