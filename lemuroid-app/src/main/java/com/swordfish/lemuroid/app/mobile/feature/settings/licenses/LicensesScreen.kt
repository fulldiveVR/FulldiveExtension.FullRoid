package com.swordfish.lemuroid.app.mobile.feature.settings.licenses

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidCardSettingsGroup
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsMenuLink
import com.swordfish.lemuroid.app.utils.android.settings.LemuroidSettingsPage
import com.swordfish.lemuroid.lib.library.CoreLicense
import com.swordfish.lemuroid.lib.library.CoreNotice
import com.swordfish.lemuroid.lib.library.CoreNotices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Open-source notices for the bundled emulation cores.
 *
 * Everything is read from `assets/licenses/` — the screen must keep working with no network, which
 * is why it replaced the previous CDN-backed HTML page.
 */
@Composable
fun LicensesScreen(modifier: Modifier = Modifier) {
    var shownLicense by remember { mutableStateOf<LicenseTextRequest?>(null) }

    LemuroidSettingsPage(modifier = modifier.fillMaxSize()) {
        AboutGroup(onShowLicense = { shownLicense = it })
        CoresGroup(onShowLicense = { shownLicense = it })
        LibrariesGroup(onShowLicense = { shownLicense = it })
    }

    shownLicense?.let { request ->
        LicenseTextDialog(request = request, onDismiss = { shownLicense = null })
    }
}

private data class LicenseTextRequest(
    val title: String,
    val assetPath: String,
)

@Composable
private fun AboutGroup(onShowLicense: (LicenseTextRequest) -> Unit) {
    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.licenses_category_application)) },
    ) {
        LemuroidSettingsMenuLink(
            title = { Text(text = stringResource(id = R.string.lemuroid_name)) },
            subtitle = { Text(text = CoreLicense.APPLICATION.displayName) },
            onClick = {
                onShowLicense(
                    LicenseTextRequest(
                        title = CoreLicense.APPLICATION.displayName,
                        assetPath = CoreLicense.APPLICATION.assetPath,
                    ),
                )
            },
        )
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            text = stringResource(id = R.string.licenses_source_offer),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CoresGroup(onShowLicense: (LicenseTextRequest) -> Unit) {
    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.licenses_category_cores)) },
    ) {
        CoreNotices.all().forEach { notice ->
            CoreNoticeRow(notice = notice, onShowLicense = onShowLicense)
        }
    }
}

@Composable
private fun CoreNoticeRow(
    notice: CoreNotice,
    onShowLicense: (LicenseTextRequest) -> Unit,
) {
    LemuroidSettingsMenuLink(
        title = { Text(text = notice.coreID.coreDisplayName) },
        subtitle = {
            Column {
                Text(text = notice.license.displayName)
                notice.copyright?.let { copyright ->
                    Text(
                        text = copyright,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = notice.upstreamUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        onClick = {
            onShowLicense(
                LicenseTextRequest(
                    title = notice.coreID.coreDisplayName,
                    assetPath = notice.license.assetPath,
                ),
            )
        },
    )
}

@Composable
private fun LibrariesGroup(onShowLicense: (LicenseTextRequest) -> Unit) {
    // Read outside onClick: stringResource needs a Composable context.
    val apacheTitle = stringResource(id = R.string.licenses_libraries_apache_title)

    LemuroidCardSettingsGroup(
        title = { Text(text = stringResource(id = R.string.licenses_category_libraries)) },
    ) {
        LemuroidSettingsMenuLink(
            title = { Text(text = apacheTitle) },
            subtitle = { Text(text = stringResource(id = R.string.licenses_libraries_apache_subtitle)) },
            onClick = {
                onShowLicense(
                    LicenseTextRequest(
                        title = apacheTitle,
                        assetPath = CoreLicense.APACHE_2_0_ASSET,
                    ),
                )
            },
        )
    }
}

@Composable
private fun LicenseTextDialog(
    request: LicenseTextRequest,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val loading = stringResource(id = R.string.licenses_loading)
    val failed = stringResource(id = R.string.licenses_read_failed)

    val text by produceState(initialValue = loading, request.assetPath) {
        value =
            withContext(Dispatchers.IO) {
                runCatching {
                    context.assets.open(request.assetPath).bufferedReader().use { it.readText() }
                }.getOrElse { failed }
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = request.title) },
        text = {
            // Licence texts are pre-wrapped plain text: keep them monospaced and scrollable in
            // both directions rather than reflowing them.
            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState()),
                text = text,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
    )
}
