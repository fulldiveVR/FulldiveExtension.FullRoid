package com.swordfish.lemuroid.app.mobile.feature.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.FulldiveConfigs
import com.swordfish.lemuroid.app.appextension.openAppInGooglePlay
import com.swordfish.lemuroid.app.mobile.feature.webgames.WebGameCard
import com.swordfish.lemuroid.app.mobile.feature.webgames.canPlayWebGame
import com.swordfish.lemuroid.app.mobile.feature.webgames.launchWebGame
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.Brand
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.BrandSearchField
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameCard
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo
import com.swordfish.lemuroid.app.utils.android.ComposableLifecycle
import com.swordfish.lemuroid.common.displayDetailsSettingsScreen
import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.library.metaSystemID

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onOpenCoreSelection: () -> Unit,
    onCatalogGameClicked: (Game) -> Unit,
) {
    val context = LocalContext.current
    val applicationContext = context.applicationContext

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> viewModel.updatePermissions(applicationContext)
            else -> { }
        }
    }

    val permissionsLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (!isGranted) {
                context.displayDetailsSettingsScreen()
            }
        }

    val state = viewModel.getViewStates().collectAsState(HomeViewModel.UIState()).value
    val installedGames = viewModel.installedGames.collectAsState(emptyList()).value
    val catalogGames = viewModel.catalogGames.collectAsState(emptyList()).value
    val playedGames = viewModel.playedGames.collectAsState(emptyList()).value
    val newlyAddedGames = viewModel.newlyAddedGames.collectAsState(emptyList()).value
    val systems = viewModel.availableMetaSystems.collectAsState(emptyList()).value

    HomeContent(
        modifier = modifier,
        state = state,
        localRomsDirectory = viewModel.localRomsDirectory,
        installedGames = installedGames,
        catalogGames = catalogGames,
        playedGames = playedGames,
        newlyAddedGames = newlyAddedGames,
        systems = systems,
        onGameClicked = onGameClick,
        onGameLongClick = onGameLongClick,
        onOpenCoreSelection = onOpenCoreSelection,
        onCatalogGameClicked = onCatalogGameClicked,
        onEnableNotificationsClicked = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        onSetDirectoryClicked = { viewModel.changeLocalStorageFolder(context) },
    )
}

private const val KEY_ALL = "all"
private const val KEY_RECENT = "recent"
private const val KEY_NEW = "new"
private const val KEY_CATALOG = "catalog"

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    state: HomeViewModel.UIState,
    localRomsDirectory: String,
    installedGames: List<Game>,
    catalogGames: List<Game>,
    playedGames: List<Game>,
    newlyAddedGames: List<Game>,
    systems: List<MetaSystemInfo>,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onOpenCoreSelection: () -> Unit,
    onCatalogGameClicked: (Game) -> Unit,
    onEnableNotificationsClicked: () -> Unit,
    onSetDirectoryClicked: () -> Unit,
) {
    val context = LocalContext.current
    val selectedKey = rememberSaveable { mutableStateOf(KEY_ALL) }
    val searchQuery = rememberSaveable { mutableStateOf("") }

    // "Recent" and "New" chips only exist when they have content.
    val hasRecent = playedGames.isNotEmpty()
    val hasNew = newlyAddedGames.isNotEmpty()

    // Keep selection valid if the available chips change (e.g. Recent/New emptied).
    val validKeys = remember(systems, hasRecent, hasNew) {
        buildSet {
            add(KEY_ALL)
            if (hasRecent) add(KEY_RECENT)
            if (hasNew) add(KEY_NEW)
            add(KEY_CATALOG)
            addAll(systems.map { it.metaSystem.name })
        }
    }
    if (selectedKey.value !in validKeys) selectedKey.value = KEY_ALL

    // "All": the user's own games first, then the catalog. Recent/New/Catalog use their
    // own pre-sorted source flows (played = newest first, new = latest added first,
    // catalog = bundled-then-free-web first). No global re-sort — ordering is intrinsic.
    val games =
        when (val key = selectedKey.value) {
            KEY_ALL -> installedGames + catalogGames
            KEY_RECENT -> playedGames
            KEY_NEW -> newlyAddedGames
            KEY_CATALOG -> catalogGames
            else -> {
                val meta = systems.firstOrNull { it.metaSystem.name == key }?.metaSystem
                (installedGames + catalogGames).filter { g ->
                    meta != null &&
                        GameSystem.findByIdOrNull(g.systemId, isProVersion = true)?.metaSystemID() == meta
                }
            }
        }.let { base ->
            val query = searchQuery.value.trim()
            if (query.isEmpty()) base
            else base.filter { it.title.contains(query, ignoreCase = true) }
        }

    val fullSpan: androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope.() -> GridItemSpan =
        { GridItemSpan(maxLineSpan) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 108.dp),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = fullSpan) {
            BrandSearchField(
                query = searchQuery.value,
                onQueryChange = { searchQuery.value = it },
                placeholder = stringResource(R.string.web_games_search_hint),
            )
        }
        item(span = fullSpan) {
            HomeFilterChips(
                systems = systems,
                hasRecent = hasRecent,
                hasNew = hasNew,
                selectedKey = selectedKey.value,
                onSelected = { selectedKey.value = it },
            )
        }
        if (state.showNoNotificationPermissionCard) {
            item(span = fullSpan) {
                HomeNotification(
                    titleId = R.string.home_notification_title,
                    messageId = R.string.home_notification_message,
                    actionId = R.string.home_notification_action,
                    onAction = onEnableNotificationsClicked,
                )
            }
        }
        if (state.showNoGamesCard && selectedKey.value == KEY_ALL) {
            item(span = fullSpan) {
                HomeNotification(
                    titleId = R.string.home_empty_title,
                    messageId = R.string.home_empty_message,
                    actionId = R.string.home_empty_action,
                    onAction = onSetDirectoryClicked,
                    enabled = !state.indexInProgress,
                    extraText = stringResource(R.string.home_empty_local_path, localRomsDirectory),
                )
            }
        }
        if (state.showDesmumeDeprecatedCard) {
            item(span = fullSpan) {
                HomeNotification(
                    titleId = R.string.home_notification_desmume_deprecated_title,
                    messageId = R.string.home_notification_desmume_deprecated_message,
                    actionId = R.string.home_notification_desmume_deprecated_action,
                    onAction = onOpenCoreSelection,
                )
            }
        }

        items(games, key = { "${it.id}_${it.isCatalogGame}" }) { game ->
            GameGridCard(
                game = game,
                context = context,
                onGameClicked = onGameClicked,
                onGameLongClick = onGameLongClick,
                onCatalogGameClicked = onCatalogGameClicked,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameGridCard(
    game: Game,
    context: android.content.Context,
    onGameClicked: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onCatalogGameClicked: (Game) -> Unit,
) {
    when {
        game.webGameSlug != null -> {
            val locked = !canPlayWebGame(context, game)
            WebGameCard(
                game = game,
                locked = locked,
                onClick = {
                    if (locked) {
                        context.openAppInGooglePlay(FulldiveConfigs.FULLROID_PRO_PACKAGE_NAME)
                    } else {
                        launchWebGame(context, game)
                    }
                },
                onLongClick = { onGameLongClick(game) },
            )
        }
        game.isCatalogGame -> {
            CatalogGameCard(
                game = game,
                onClick = { onCatalogGameClicked(game) },
                onLongClick = { onGameLongClick(game) },
            )
        }
        else -> {
            LemuroidGameCard(
                game = game,
                onClick = { onGameClicked(game) },
                onLongClick = { onGameLongClick(game) },
            )
        }
    }
}

@Composable
private fun HomeFilterChips(
    systems: List<MetaSystemInfo>,
    hasRecent: Boolean,
    hasNew: Boolean,
    selectedKey: String,
    onSelected: (String) -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FilterChip(stringResource(R.string.home_filter_all), selectedKey == KEY_ALL) { onSelected(KEY_ALL) }
        if (hasRecent) {
            FilterChip(stringResource(R.string.home_filter_recent), selectedKey == KEY_RECENT) { onSelected(KEY_RECENT) }
        }
        if (hasNew) {
            FilterChip(stringResource(R.string.home_filter_new), selectedKey == KEY_NEW) { onSelected(KEY_NEW) }
        }
        FilterChip(stringResource(R.string.home_filter_catalog), selectedKey == KEY_CATALOG) { onSelected(KEY_CATALOG) }
        systems.forEach { system ->
            val key = system.metaSystem.name
            FilterChip(system.getName(context), selectedKey == key) { onSelected(key) }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .then(
                if (active) {
                    Modifier.background(Brand.gradient)
                } else {
                    Modifier
                        .background(Brand.SurfaceElevated)
                        .border(1.dp, Brand.Outline, RoundedCornerShape(50))
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (active) Color.White else Brand.OnSurface,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CatalogGameCard(
    modifier: Modifier = Modifier,
    game: Game,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    ElevatedCard(
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Column {
            Box {
                LemuroidGameImage(modifier = Modifier.fillMaxWidth(), game = game)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = game.systemId.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun HomeNotification(
    titleId: Int,
    messageId: Int,
    actionId: Int,
    enabled: Boolean = true,
    extraText: String? = null,
    onAction: () -> Unit = { },
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = stringResource(titleId), style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(messageId), style = MaterialTheme.typography.bodyMedium)
            if (extraText != null) {
                Text(
                    text = extraText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onAction,
                enabled = enabled,
            ) {
                Text(stringResource(id = actionId))
            }
        }
    }
}
