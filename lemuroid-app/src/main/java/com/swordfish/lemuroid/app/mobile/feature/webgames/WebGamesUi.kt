/*
 * WebGamesUi.kt
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

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.isProVersion
import com.swordfish.lemuroid.app.mobile.feature.webgame.WebGameActivity
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.lib.library.db.entity.Game

/**
 * Access rule for a GameHub web game. Free-tier games are always playable; pro-tier
 * games require the Pro version (Roomcord does NOT unlock them — pro games are
 * Pro-only).
 */
fun canPlayWebGame(context: Context, game: Game): Boolean =
    game.isFreeTier || isProVersion()

/** Launch the offline player for [game]. */
fun launchWebGame(context: Context, game: Game) {
    val slug = game.webGameSlug ?: return
    val zipUrl = game.webZipUrl ?: return
    context.startActivity(
        WebGameActivity.newIntent(
            context = context,
            slug = slug,
            zipUrl = zipUrl,
            zipSha = game.webZipSha256,
            orientation = game.webOrientation,
        ),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WebGameCard(
    modifier: Modifier = Modifier,
    game: Game,
    locked: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    ElevatedCard(
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Column {
            Box {
                LemuroidGameImage(
                    modifier = Modifier,
                    game = game,
                )
                // Locked (pro-tier in a non-Pro build) games are dimmed.
                if (locked) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                    )
                }
                // FREE / PRO label.
                val isFree = game.isFreeTier
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = if (isFree) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                ) {
                    Text(
                        text = stringResource(if (isFree) R.string.web_games_free_badge else R.string.web_games_pro_badge),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFree) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiary,
                    )
                }
                // Favorite marker (toggled via the long-press context menu).
                if (game.isFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(18.dp),
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
