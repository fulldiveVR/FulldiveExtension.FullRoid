package com.swordfish.lemuroid.app.mobile.feature.systems

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.swordfish.lemuroid.app.mobile.feature.games.GamesScreen
import com.swordfish.lemuroid.app.mobile.feature.games.GamesViewModel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.Brand
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun MetaSystemsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MetaSystemsViewModel,
    retrogradeDb: RetrogradeDatabase,
    onGameClick: (Game) -> Unit,
    onGameLongClick: (Game) -> Unit,
    onGameFavoriteToggle: (Game, Boolean) -> Unit,
) {
    val metaSystems = viewModel.availableMetaSystems.collectAsState(emptyList()).value

    if (metaSystems.isEmpty()) {
        LemuroidEmptyView(modifier = modifier)
        return
    }

    val selectedIndex = rememberSaveable { mutableStateOf(0) }
    val safeIndex = selectedIndex.value.coerceIn(0, metaSystems.size - 1)
    val selectedSystem = metaSystems[safeIndex]

    Column(modifier = modifier.fillMaxSize()) {
        SystemChips(
            systems = metaSystems,
            selectedIndex = safeIndex,
            onSelected = { selectedIndex.value = it },
        )
        GamesScreen(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            viewModel = viewModel(
                key = "games_${selectedSystem.metaSystem.name}",
                factory = GamesViewModel.Factory(retrogradeDb, selectedSystem.metaSystem),
            ),
            onGameClick = onGameClick,
            onGameLongClick = onGameLongClick,
            onGameFavoriteToggle = onGameFavoriteToggle,
        )
    }
}

@Composable
private fun SystemChips(
    systems: List<MetaSystemInfo>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    val context = LocalContext.current
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(systems.size, key = { systems[it].metaSystem }) { index ->
            val active = index == selectedIndex
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
                    .clickable { onSelected(index) }
                    .padding(horizontal = 18.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = systems[index].getName(context),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) Color.White else Brand.OnSurface,
                )
            }
        }
    }
}
