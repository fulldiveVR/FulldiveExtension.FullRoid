/*
 * WebGamesScreen.kt
 *
 * Copyright (C) 2026 FullDive
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.mobile.feature.webgames

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.FulldiveConfigs
import com.swordfish.lemuroid.app.appextension.openAppInGooglePlay
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.BrandSearchField
import com.swordfish.lemuroid.app.shared.catalog.rememberSmoothCatalogProgress
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.flow.Flow

@Composable
fun WebGamesScreen(
    modifier: Modifier = Modifier,
    webGamesFlow: Flow<List<Game>>,
) {
    val context = LocalContext.current
    val allGames by webGamesFlow.collectAsState(initial = emptyList())
    val syncProgress = rememberSmoothCatalogProgress()
    var query by remember { mutableStateOf("") }

    val games = remember(allGames, query) {
        if (query.isBlank()) {
            allGames
        } else {
            allGames.filter { it.title.contains(query.trim(), ignoreCase = true) }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        BrandSearchField(
            query = query,
            onQueryChange = { query = it },
            placeholder = stringResource(R.string.web_games_search_hint),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        if (games.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // First sync with an empty catalog: show progress instead of a bare
                // "no games" message — determinate once the worker reports, indeterminate
                // while it's still only enqueued. Any other empty case (search miss,
                // offline with a failed sync) keeps the plain empty text.
                if (allGames.isEmpty() && (syncProgress.visible || syncProgress.running)) {
                    if (syncProgress.visible) {
                        CircularProgressIndicator(
                            progress = { syncProgress.progress },
                            strokeCap = StrokeCap.Round,
                        )
                    } else {
                        CircularProgressIndicator(strokeCap = StrokeCap.Round)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.web_games_syncing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.web_games_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            return@Column
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(games, key = { it.id }) { game ->
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
                )
            }
        }
    }
}
