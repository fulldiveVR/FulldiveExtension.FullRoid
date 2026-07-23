package com.swordfish.lemuroid.app.mobile.feature.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.Brand
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.brandTextStyle
import com.swordfish.lemuroid.app.shared.savesync.SaveSyncWork

@Composable
fun MainTopBar(
    currentRoute: MainRoute,
    navController: NavHostController,
    onHelpPressed: () -> Unit,
    onUpdateQueryString: (String) -> Unit,
    mainUIState: MainViewModel.UiState,
    isProTutorialNavigationVisible: Boolean,
    onProButtonClick: () -> Unit = {},
) {
    Column {
        if (currentRoute.parent == null) {
            BrandHeader(
                saveSyncEnabled = mainUIState.saveSyncEnabled,
                operationsInProgress = mainUIState.operationInProgress,
                onHelpPressed = onHelpPressed,
                isProTutorialNavigationVisible = isProTutorialNavigationVisible,
                onProButtonClick = onProButtonClick,
            )
        } else {
            ChildTopAppBar(currentRoute, navController)
        }

        AnimatedVisibility(mainUIState.operationInProgress) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

/** Modern top-level header: gradient wordmark + rounded action pills + avatar/settings. */
@Composable
private fun BrandHeader(
    saveSyncEnabled: Boolean,
    operationsInProgress: Boolean,
    onHelpPressed: () -> Unit,
    isProTutorialNavigationVisible: Boolean,
    onProButtonClick: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brand.Background)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.lemuroid_name),
            style = brandTextStyle(MaterialTheme.typography.titleLarge).copy(letterSpacing = 1.sp),
        )
        Spacer(Modifier.weight(1f))

        if (isProTutorialNavigationVisible) {
            ProPill(onProButtonClick)
        }
        RoundIconButton(onClick = onHelpPressed) {
            Icon(
                Icons.Outlined.Info,
                stringResource(R.string.mobile_settings_help),
                tint = Brand.OnSurface,
                modifier = Modifier.size(20.dp),
            )
        }
        if (saveSyncEnabled) {
            RoundIconButton(
                enabled = !operationsInProgress,
                onClick = { SaveSyncWork.enqueueManualWork(context.applicationContext) },
            ) {
                Icon(
                    Icons.Outlined.CloudSync,
                    stringResource(R.string.save_sync),
                    tint = Brand.OnSurface,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun ProPill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(end = 6.dp)
            .height(34.dp)
            .background(Brand.SurfaceElevated, RoundedCornerShape(50))
            .border(1.dp, Brand.Outline, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(painterResource(R.drawable.ic_pro), null, modifier = Modifier.size(16.dp))
        Text("PRO", style = MaterialTheme.typography.labelMedium, color = Brand.OnSurface, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RoundIconButton(
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .size(38.dp)
            .background(Brand.SurfaceElevated, CircleShape)
            .border(1.dp, Brand.Outline, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChildTopAppBar(
    route: MainRoute,
    navController: NavHostController,
) {
    TopAppBar(
        title = { Text(text = stringResource(route.titleId)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Brand.Background,
            scrolledContainerColor = Brand.Background,
        ),
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(id = R.string.back))
            }
        },
    )
}
