package com.swordfish.lemuroid.app.mobile.feature.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.isProVersion
import com.swordfish.lemuroid.app.shared.library.PendingOperationsMonitor
import com.swordfish.lemuroid.app.shared.settings.StorageFrameworkPickerLauncher
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo
import com.swordfish.lemuroid.common.coroutines.combine
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metaSystemID
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(
    appContext: Context,
    retrogradeDb: RetrogradeDatabase,
    private val coresSelection: CoresSelection,
) : ViewModel() {
    companion object {
        const val CAROUSEL_MAX_ITEMS = 10
        const val RECENT_MAX_ITEMS = 50
        const val NEW_MAX_ITEMS = 20
        const val DEBOUNCE_TIME = 100L
    }

    val localRomsDirectory: String =
        DirectoriesManager(appContext).getInternalRomsDirectory().absolutePath

    class Factory(
        val appContext: Context,
        val retrogradeDb: RetrogradeDatabase,
        val coresSelection: CoresSelection,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(appContext, retrogradeDb, coresSelection) as T
        }
    }

    data class UIState(
        val favoritesGames: List<Game> = emptyList(),
        val recentGames: List<Game> = emptyList(),
        val discoveryGames: List<Game> = emptyList(),
        val catalogGames: List<Game> = emptyList(),
        val indexInProgress: Boolean = true,
        val showNoNotificationPermissionCard: Boolean = false,
        val showNoMicrophonePermissionCard: Boolean = false,
        val showNoGamesCard: Boolean = false,
        val showDesmumeDeprecatedCard: Boolean = false,
    )

    private val microphonePermissionEnabledState = MutableStateFlow(true)
    private val notificationsPermissionEnabledState = MutableStateFlow(true)
    private val uiStates = MutableStateFlow(UIState())

    // "Show catalog" setting (Settings → ROMs). When off, bundled and web catalog games are
    // hidden from Home only — the Catalog button and Favorites keep showing them.
    val showCatalog: Flow<Boolean> =
        FlowSharedPreferences(SharedPreferencesHelper.getSharedPreferences(appContext))
            .getBoolean(appContext.getString(R.string.pref_key_show_catalog), true)
            .asFlow()
            .distinctUntilChanged()

    // Flat Home grid sources (filtered by the Home chips). No horizontal category rows.
    val installedGames: Flow<List<Game>> = retrogradeDb.gameDao().selectVisibleGames()
    val catalogGames: Flow<List<Game>> =
        showCatalog.flatMapLatest { show ->
            if (show) retrogradeDb.gameDao().selectCatalogGames() else flowOf(emptyList())
        }

    // "Recent" chip: any game the user launched (ROM or web), newest play first.
    val playedGames: Flow<List<Game>> =
        combine(
            retrogradeDb.gameDao().selectRecentGames(RECENT_MAX_ITEMS),
            showCatalog,
        ) { games, show -> games.hideCatalogUnless(show) }

    // "New" chip: the most recently added user ROMs.
    val newlyAddedGames: Flow<List<Game>> = retrogradeDb.gameDao().selectNewGames(NEW_MAX_ITEMS)

    // Platform chips on Home (replaces the removed Systems tab). With the catalog hidden the
    // counts come from scanned games only, so no chip appears for a catalog-only platform.
    val availableMetaSystems: Flow<List<MetaSystemInfo>> =
        showCatalog.flatMapLatest { show ->
            if (show) {
                retrogradeDb.gameDao().selectSystemsWithCount()
            } else {
                retrogradeDb.gameDao().selectScannedSystemsWithCount()
            }
        }
            .map { systemCounts ->
                systemCounts.asSequence()
                    .filter { (_, count) -> count > 0 }
                    .mapNotNull { (systemId, count) ->
                        GameSystem.findByIdOrNull(systemId, isProVersion = true)
                            ?.metaSystemID()
                            ?.let { it to count }
                    }
                    .groupBy { (metaSystemId, _) -> metaSystemId }
                    .map { (metaSystemId, counts) -> MetaSystemInfo(metaSystemId, counts.sumOf { it.second }) }
                    .sortedBy { it.getName(appContext) }
                    .toList()
            }

    fun getViewStates(): Flow<UIState> {
        return uiStates
    }

    fun changeLocalStorageFolder(context: Context) {
        StorageFrameworkPickerLauncher.pickFolder(context)
    }

    fun updatePermissions(context: Context) {
        notificationsPermissionEnabledState.value = isNotificationsPermissionGranted(context)
        microphonePermissionEnabledState.value = isMicrophonePermissionGranted(context)
    }

    private fun isNotificationsPermissionGranted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        val permissionResult =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            )

        return permissionResult == PackageManager.PERMISSION_GRANTED
    }

    private fun isMicrophonePermissionGranted(context: Context): Boolean {
        val permissionResult =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            )

        return permissionResult == PackageManager.PERMISSION_GRANTED
    }

    private fun buildViewState(
        favoritesGames: List<Game>,
        recentGames: List<Game>,
        discoveryGames: List<Game>,
        indexInProgress: Boolean,
        notificationsPermissionEnabled: Boolean,
        showMicrophoneCard: Boolean,
        showDesmumeWarning: Boolean,
        catalogGames: List<Game>,
    ): UIState {
        val noGames = recentGames.isEmpty() && favoritesGames.isEmpty() && discoveryGames.isEmpty()

        return UIState(
            favoritesGames = favoritesGames,
            recentGames = recentGames,
            discoveryGames = discoveryGames,
            catalogGames = catalogGames,
            indexInProgress = indexInProgress,
            showNoNotificationPermissionCard = !notificationsPermissionEnabled,
            showNoMicrophonePermissionCard = showMicrophoneCard,
            showNoGamesCard = noGames,
            showDesmumeDeprecatedCard = showDesmumeWarning,
        )
    }

    init {
        viewModelScope.launch {
            val uiStatesFlow =
                combine(
                    favoritesGames(retrogradeDb),
                    recentGames(retrogradeDb),
                    discoveryGames(retrogradeDb),
                    indexingInProgress(appContext),
                    notificationsPermissionEnabledState,
                    microphoneNotification(retrogradeDb),
                    desmumeWarningNotification(),
                    catalogGames,
                    ::buildViewState,
                )

            uiStatesFlow
                .debounce(DEBOUNCE_TIME)
                .flowOn(Dispatchers.IO)
                .collect { uiStates.value = it }
        }
    }

    private fun indexingInProgress(appContext: Context) =
        PendingOperationsMonitor(appContext).anyLibraryOperationInProgress()

    private fun discoveryGames(retrogradeDb: RetrogradeDatabase) =
        retrogradeDb.gameDao().selectFirstNotPlayed(CAROUSEL_MAX_ITEMS)

    // These two only feed the "no games" card, so they follow Home visibility: with the catalog
    // hidden, a favorited or previously played catalog game must not suppress the empty state.
    private fun recentGames(retrogradeDb: RetrogradeDatabase) =
        combine(
            retrogradeDb.gameDao().selectFirstUnfavoriteRecents(CAROUSEL_MAX_ITEMS),
            showCatalog,
        ) { games, show -> games.hideCatalogUnless(show) }

    private fun favoritesGames(retrogradeDb: RetrogradeDatabase) =
        combine(
            retrogradeDb.gameDao().selectFirstFavorites(CAROUSEL_MAX_ITEMS),
            showCatalog,
        ) { games, show -> games.hideCatalogUnless(show) }

    private fun dsGamesCount(retrogradeDb: RetrogradeDatabase): Flow<Int> {
        return retrogradeDb.gameDao().selectSystemsWithCount()
            .map { systems ->
                systems
                    .firstOrNull { it.systemId == SystemID.NDS.dbname }
                    ?.count
                    ?: 0
            }
            .distinctUntilChanged()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun microphoneNotification(db: RetrogradeDatabase): Flow<Boolean> {
        return microphonePermissionEnabledState
            .flatMapLatest { isMicrophoneEnabled ->
                if (isMicrophoneEnabled) {
                    flowOf(false)
                } else {
                    combine(
                        coresSelection.getSelectedCores(isProVersion()),
                        dsGamesCount(db),
                    ) { cores, dsCount ->
                        cores.any { it.coreConfig.supportsMicrophone } &&
                            dsCount > 0
                    }
                }
                    .distinctUntilChanged()
            }
    }

    private fun desmumeWarningNotification(): Flow<Boolean> {
        return coresSelection.getSelectedCores(isProVersion())
            .map { cores -> cores.any { it.coreConfig.coreID == CoreID.DESMUME } }
            .distinctUntilChanged()
    }
}

private fun List<Game>.hideCatalogUnless(showCatalog: Boolean) =
    if (showCatalog) this else filter { !it.isCatalogGame }
