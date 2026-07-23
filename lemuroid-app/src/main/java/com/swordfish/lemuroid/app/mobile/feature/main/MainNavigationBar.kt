package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.Brand

@Composable
fun MainNavigationBar(
    currentRoute: MainRoute?,
    navController: NavHostController,
    onChatClick: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = currentRoute?.showBottomNavigation != false,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        ModernNavigationBar(currentRoute, navController, onChatClick)
    }
}

@Composable
private fun ModernNavigationBar(
    currentRoute: MainRoute?,
    navController: NavHostController,
    onChatClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(84.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .align(Alignment.BottomCenter),
            color = Brand.Surface,
            shadowElevation = 12.dp,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavBarItem(MainNavigationRoutes.HOME, currentRoute, navController, Modifier.weight(1f))
                NavBarItem(MainNavigationRoutes.FAVORITES, currentRoute, navController, Modifier.weight(1f))
                Spacer(Modifier.weight(1.3f)) // gap for the center button
                // Chat (Roomcord) — an action, not a tab: opens the Roomcord screen.
                ActionNavItem(
                    icon = Icons.Filled.Forum,
                    labelRes = R.string.nav_chat,
                    onClick = onChatClick,
                    modifier = Modifier.weight(1f),
                )
                NavBarItem(MainNavigationRoutes.SETTINGS, currentRoute, navController, Modifier.weight(1f))
            }
        }

        // Raised gradient gamepad button — the built-in games Catalog.
        CenterGamepadButton(
            modifier = Modifier.align(Alignment.TopCenter),
            onClick = { navController.navigateTab(MainRoute.WEB_GAMES) },
        )
    }
}

@Composable
private fun ActionNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    labelRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 26.dp),
                onClick = onClick,
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = stringResource(labelRes), tint = tint, modifier = Modifier.size(24.dp))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun NavBarItem(
    destination: MainNavigationRoutes,
    currentRoute: MainRoute?,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val isSelected = currentRoute?.root == destination.route
    val tint = if (isSelected) Brand.Accent else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 26.dp),
            ) { navController.navigateTab(destination.route) }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
            contentDescription = stringResource(destination.titleId),
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = stringResource(destination.titleId),
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun CenterGamepadButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(58.dp)
            .shadow(10.dp, CircleShape)
            .clip(CircleShape)
            .background(Brand.gradient)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.SportsEsports,
            contentDescription = stringResource(R.string.web_games_title),
            tint = Color.White,
            modifier = Modifier.size(28.dp),
        )
    }
}

private fun NavHostController.navigateTab(route: MainRoute) {
    navigate(route.route) {
        popUpTo(graph.findStartDestination().id) { saveState = false }
        launchSingleTop = true
        restoreState = false
    }
}
