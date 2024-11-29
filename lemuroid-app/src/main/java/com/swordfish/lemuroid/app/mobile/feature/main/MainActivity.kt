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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.FIN_WIZE_APP
import com.swordfish.lemuroid.app.appextension.FulldiveConfigs
import com.swordfish.lemuroid.app.appextension.PopupManager
import com.swordfish.lemuroid.app.appextension.discord.DiscordManager
import com.swordfish.lemuroid.app.appextension.discord.ShareDiscordTextGenerator
import com.swordfish.lemuroid.app.appextension.discord.ShowShareDialog
import com.swordfish.lemuroid.app.appextension.isProVersion
import com.swordfish.lemuroid.app.appextension.openAppInGooglePlay
import com.swordfish.lemuroid.app.fulldive.analytics.IActionTracker
import com.swordfish.lemuroid.app.fulldive.analytics.TrackerConstants
import com.swordfish.lemuroid.app.mobile.feature.favorites.FavoritesScreen
import com.swordfish.lemuroid.app.mobile.feature.favorites.FavoritesViewModel
import com.swordfish.lemuroid.app.mobile.feature.games.GamesScreen
import com.swordfish.lemuroid.app.mobile.feature.games.GamesViewModel
import com.swordfish.lemuroid.app.mobile.feature.home.HomeScreen
import com.swordfish.lemuroid.app.mobile.feature.home.HomeViewModel
import com.swordfish.lemuroid.app.mobile.feature.proinfo.DiscordPopupLayout
import com.swordfish.lemuroid.app.mobile.feature.proinfo.FinWizeLayout
import com.swordfish.lemuroid.app.mobile.feature.proinfo.ProPopupLayout
import com.swordfish.lemuroid.app.mobile.feature.proinfo.tutorial.ProTutorialScreen
import com.swordfish.lemuroid.app.mobile.feature.search.SearchScreen
import com.swordfish.lemuroid.app.mobile.feature.search.SearchViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.advanced.AdvancedSettingsScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.advanced.AdvancedSettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.bios.BiosScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.bios.BiosSettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.coreselection.CoresSelectionScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.coreselection.CoresSelectionViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.general.SettingsScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.general.SettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.inputdevices.InputDevicesSettingsScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.inputdevices.InputDevicesSettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.settings.savesync.SaveSyncSettingsScreen
import com.swordfish.lemuroid.app.mobile.feature.settings.savesync.SaveSyncSettingsViewModel
import com.swordfish.lemuroid.app.mobile.feature.shortcuts.ShortcutsGenerator
import com.swordfish.lemuroid.app.mobile.feature.systems.MetaSystemsScreen
import com.swordfish.lemuroid.app.mobile.feature.systems.MetaSystemsViewModel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.AppTheme
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.game.BaseGameActivity
import com.swordfish.lemuroid.app.shared.game.GameLauncher
import com.swordfish.lemuroid.app.shared.input.InputDeviceManager
import com.swordfish.lemuroid.app.shared.main.BusyActivity
import com.swordfish.lemuroid.app.shared.main.GameLaunchTaskHandler
import com.swordfish.lemuroid.app.shared.settings.SettingsInteractor
import com.swordfish.lemuroid.common.coroutines.safeLaunch
import com.swordfish.lemuroid.ext.feature.review.ReviewManager
import com.swordfish.lemuroid.lib.android.RetrogradeComponentActivity
import com.swordfish.lemuroid.lib.bios.BiosManager
import com.swordfish.lemuroid.lib.core.CoresSelection
import com.swordfish.lemuroid.lib.injection.PerActivity
import com.swordfish.lemuroid.lib.library.GameSystemHelperImpl
import com.swordfish.lemuroid.lib.library.MetaSystemID
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import com.swordfish.lemuroid.lib.savesync.SaveSyncManager
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import dagger.Provides
import de.charlex.compose.material3.HtmlText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : RetrogradeComponentActivity(), BusyActivity {
    @Inject
    lateinit var gameLaunchTaskHandler: GameLaunchTaskHandler

    @Inject
    lateinit var saveSyncManager: SaveSyncManager

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    @Inject
    lateinit var gameInteractor: GameInteractor

    @Inject
    lateinit var biosManager: BiosManager

    @Inject
    lateinit var coresSelection: CoresSelection

    @Inject
    lateinit var settingsInteractor: SettingsInteractor

    @Inject
    lateinit var inputDeviceManager: InputDeviceManager

    @Inject
    lateinit var actionTracker: IActionTracker

    @Inject
    lateinit var shareDiscordTextGenerator: ShareDiscordTextGenerator

    @Inject
    lateinit var popupManager: PopupManager

    private val reviewManager = ReviewManager()

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory(applicationContext, saveSyncManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalScope.safeLaunch {
            reviewManager.initialize(applicationContext)
        }

        setContent {
            val navController = rememberNavController()
            MainScreen(navController)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen(navController: NavHostController) {
        AppTheme {
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry.value?.destination
            val currentRoute =
                currentDestination?.route
                    ?.let { MainRoute.findByRoute(it) }
                    ?: MainRoute.HOME

            val isFinWizeVisible = remember {
                mutableStateOf(false)
            }
            val isProPopupVisible = remember {
                mutableStateOf(false)
            }
            val isDiscordPopupVisible = remember {
                mutableStateOf(false)
            }
            if (currentRoute == MainRoute.HOME) {
                popupManager.onAppStarted(this@MainActivity)
                isFinWizeVisible.value = popupManager.isFinWizeVisible()
                isProPopupVisible.value = popupManager.isProPopupVisible()
                isDiscordPopupVisible.value = popupManager.isDiscordPopupVisible()
            } else {
                isFinWizeVisible.value = false
                isProPopupVisible.value = false
                isDiscordPopupVisible.value = false
            }

            val infoDialogDisplayed =
                remember {
                    mutableStateOf(false)
                }

            val shareDiscordDialogDisplayed =
                remember {
                    mutableStateOf<Game?>(null)
                }

            LaunchedEffect(currentRoute) {
                mainViewModel.changeRoute(currentRoute)
            }

            val selectedGameState =
                remember {
                    mutableStateOf<Game?>(null)
                }

            val onGameLongClick = { game: Game ->
                selectedGameState.value = game
            }

            val onGameClick = { game: Game ->
                gameInteractor.onGamePlay(game)
            }

            val onGameFavoriteToggle = { game: Game, isFavorite: Boolean ->
                gameInteractor.onFavoriteToggle(game, isFavorite)
            }

            val onHelpPressed = {
                infoDialogDisplayed.value = true
            }

            val mainUIState =
                mainViewModel.state
                    .collectAsState(MainViewModel.UiState())
                    .value

            Scaffold(
                topBar = {
                    MainTopBar(
                        currentRoute = currentRoute,
                        navController = navController,
                        onHelpPressed = onHelpPressed,
                        mainUIState = mainUIState,
                        onUpdateQueryString = { mainViewModel.changeQueryString(it) },
                        isProTutorialNavigationVisible = currentRoute != MainRoute.PRO_TUTORIAL && !isProVersion()
                    )
                },
                bottomBar = { MainNavigationBar(currentRoute, navController) },
            ) { padding ->
                NavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    startDestination = MainRoute.HOME.route,
                ) {
                    composable(MainRoute.HOME) {
                        HomeScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                            viewModel(
                                factory =
                                HomeViewModel.Factory(
                                    applicationContext,
                                    retrogradeDb,
                                ),
                            ),
                            onGameClick = onGameClick,
                            onGameLongClick = onGameLongClick,
                        )
                        when {
                            isFinWizeVisible.value -> {
                                actionTracker.logAction(TrackerConstants.EVENT_PRO_POPUP_SHOWN)
                                FinWizeLayout(
                                    onClick = {
                                        actionTracker.logAction(TrackerConstants.EVENT_PRO_TUTORIAL_OPENED_FROM_PRO_POPUP)
                                        this@MainActivity.openAppInGooglePlay(FIN_WIZE_APP)
                                        isFinWizeVisible.value = false
                                    },
                                    onCloseClick = {
                                        actionTracker.logAction(TrackerConstants.EVENT_PRO_POPUP_CLOSED)
                                        isFinWizeVisible.value = false
                                    }
                                )
                            }

                            isProPopupVisible.value && !isFinWizeVisible.value -> {
                                actionTracker.logAction(TrackerConstants.EVENT_PRO_POPUP_SHOWN)
                                ProPopupLayout(
                                    onClick = {
                                        actionTracker.logAction(TrackerConstants.EVENT_PRO_TUTORIAL_OPENED_FROM_PRO_POPUP)
                                        navController.navigateToRoute(MainRoute.PRO_TUTORIAL)
                                        isProPopupVisible.value = false
                                    },
                                    onCloseClick = {
                                        actionTracker.logAction(TrackerConstants.EVENT_PRO_POPUP_CLOSED)
                                        isProPopupVisible.value = false
                                    }
                                )
                            }

                            isDiscordPopupVisible.value && !isFinWizeVisible.value && !isProPopupVisible.value -> {
                                actionTracker.logAction(TrackerConstants.EVENT_DISCORD_POPUP_SHOWN)
                                DiscordPopupLayout(
                                    onClick = {
                                        actionTracker.logAction(TrackerConstants.EVENT_DISCORD_POPUP_CLICKED)
                                        startActivity(Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(PopupManager.DISCORD_INVITATION)
                                        })
                                        isDiscordPopupVisible.value = false
                                    },
                                    onCloseClick = {
                                        actionTracker.logAction(TrackerConstants.EVENT_DISCORD_POPUP_CLOSED)
                                        isDiscordPopupVisible.value = false
                                    }
                                )
                            }

                            else -> Unit
                        }
                    }

                    composable(MainRoute.FAVORITES) {
                        FavoritesScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                            viewModel(
                                factory = FavoritesViewModel.Factory(retrogradeDb),
                            ),
                            onGameClick = onGameClick,
                            onGameLongClick = onGameLongClick,
                        )
                    }
                    composable(MainRoute.SEARCH) {
                        SearchScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                            viewModel(
                                factory = SearchViewModel.Factory(retrogradeDb),
                            ),
                            searchQuery = mainUIState.searchQuery,
                            onGameClick = onGameClick,
                            onGameLongClick = onGameLongClick,
                            onGameFavoriteToggle = onGameFavoriteToggle,
                            onResetSearchQuery = { mainViewModel.changeQueryString("") },
                        )
                    }
                    composable(MainRoute.SYSTEMS) {
                        MetaSystemsScreen(
                            modifier = Modifier.padding(padding),
                            navController = navController,
                            viewModel =
                            viewModel(
                                factory =
                                MetaSystemsViewModel.Factory(
                                    retrogradeDb,
                                    applicationContext,
                                ),
                            ),
                        )
                    }
                    composable(MainRoute.SYSTEM_GAMES) { entry ->
                        val metaSystemId = entry.arguments?.getString("metaSystemId")
                        GamesScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                            viewModel(
                                factory =
                                GamesViewModel.Factory(
                                    retrogradeDb,
                                    MetaSystemID.valueOf(metaSystemId!!),
                                ),
                            ),
                            onGameClick = onGameClick,
                            onGameLongClick = onGameLongClick,
                            onGameFavoriteToggle = onGameFavoriteToggle,
                        )
                    }
                    composable(MainRoute.SETTINGS) {
                        SettingsScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                            viewModel(
                                factory =
                                SettingsViewModel.Factory(
                                    applicationContext,
                                    settingsInteractor,
                                    saveSyncManager,
                                    FlowSharedPreferences(
                                        SharedPreferencesHelper.getLegacySharedPreferences(
                                            applicationContext,
                                        ),
                                    ),
                                ),
                            ),
                            navController = navController,
                        )
                    }

                    composable(MainRoute.PRO_TUTORIAL) {
                        actionTracker.logAction(TrackerConstants.EVENT_PRO_TUTORIAL_OPENED_FROM_TOOLBAR)
                        ProTutorialScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                            viewModel(
                                factory =
                                AdvancedSettingsViewModel.Factory(
                                    applicationContext,
                                    settingsInteractor,
                                ),
                            ),
                            navController = navController,
                            onBuyProClick = {
                                openAppInGooglePlay(FulldiveConfigs.FULLROID_PRO_PACKAGE_NAME)
                            }
                        )
                    }
                    composable(MainRoute.SETTINGS_ADVANCED) {
                        AdvancedSettingsScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                            viewModel(
                                factory =
                                AdvancedSettingsViewModel.Factory(
                                    applicationContext,
                                    settingsInteractor,
                                ),
                            ),
                            navController = navController,
                        )
                    }
                    composable(MainRoute.SETTINGS_BIOS) {
                        BiosScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                            viewModel(
                                factory = BiosSettingsViewModel.Factory(biosManager),
                            ),
                        )
                    }
                    composable(MainRoute.SETTINGS_CORES_SELECTION) {
                        CoresSelectionScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                            viewModel(
                                factory =
                                CoresSelectionViewModel.Factory(
                                    applicationContext,
                                    coresSelection,
                                ),
                            ),
                        )
                    }
                    composable(MainRoute.SETTINGS_INPUT_DEVICES) {
                        InputDevicesSettingsScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                            viewModel(
                                factory =
                                InputDevicesSettingsViewModel.Factory(
                                    applicationContext,
                                    inputDeviceManager,
                                ),
                            ),
                        )
                    }
                    composable(MainRoute.SETTINGS_SAVE_SYNC) {
                        SaveSyncSettingsScreen(
                            modifier = Modifier.padding(padding),
                            viewModel =
                            viewModel(
                                factory =
                                SaveSyncSettingsViewModel.Factory(
                                    application,
                                    saveSyncManager,
                                ),
                            ),
                        )
                    }
                }

                MainGameContextActions(
                    selectedGameState = selectedGameState,
                    shortcutSupported = gameInteractor.supportShortcuts(),
                    onGamePlay = { gameInteractor.onGamePlay(it) },
                    onGameRestart = { gameInteractor.onGameRestart(it) },
                    onFavoriteToggle = { game: Game, isFavorite: Boolean ->
                        gameInteractor.onFavoriteToggle(game, isFavorite)
                    },
                    onCreateShortcut = { gameInteractor.onCreateShortcut(it) },
                    onShareDiscord = { shareDiscordDialogDisplayed.value = it }
                )

                val game = shareDiscordDialogDisplayed.value
                if (game != null) {
                    ShowShareDialog(
                        game = game,
                        onPositiveClicked = { content, imageUrl ->
                            shareDiscordTextGenerator.shareGame(
                                content,
                                imageUrl,
                                onSuccess = {
                                    Toast.makeText(this, "Feedback is successfully shared!", Toast.LENGTH_SHORT)
                                        .show()
                                },
                                onError = {
                                    Toast.makeText(this, "Error while share  feedback!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onDismissRequest = { shareDiscordDialogDisplayed.value = null }
                    )
                }

                if (infoDialogDisplayed.value) {
                    val message = remember {
                        val systemFolders =
                            SystemID.values()
                                .joinToString(", ") { "<i>${it.dbname}</i>" }

                        String
                            .format(
                                getString(R.string.lemuroid_help_content),
                                getString(R.string.lemuroid_name)
                            )
                            .replace("\$SYSTEMS", systemFolders)
                    }

                    AlertDialog(
                        text = { HtmlText(text = message) },
                        onDismissRequest = { infoDialogDisplayed.value = false },
                        confirmButton = { },
                    )
                }
            }
        }
    }

    override fun activity(): Activity = this

    override fun isBusy(): Boolean = mainViewModel.state.value.operationInProgress

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            BaseGameActivity.REQUEST_PLAY_GAME -> {
                GlobalScope.safeLaunch {
                    gameLaunchTaskHandler.handleGameFinish(
                        true,
                        this@MainActivity,
                        resultCode,
                        data,
                    )
                }
            }
        }
    }

    @dagger.Module
    abstract class Module {
        @dagger.Module
        companion object {
            @Provides
            @PerActivity
            @JvmStatic
            fun settingsInteractor(
                activity: MainActivity,
                directoriesManager: DirectoriesManager,
            ) = SettingsInteractor(activity, directoriesManager)

            @Provides
            @PerActivity
            @JvmStatic
            fun gameInteractor(
                activity: MainActivity,
                retrogradeDb: RetrogradeDatabase,
                shortcutsGenerator: ShortcutsGenerator,
                gameLauncher: GameLauncher,
                gameSystemHelper: GameSystemHelperImpl,
            ) = GameInteractor(
                activity,
                retrogradeDb,
                false,
                shortcutsGenerator,
                gameLauncher,
                gameSystemHelper
            )
        }
    }
}
