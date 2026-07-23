/*
 * CatalogSyncState.kt
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

package com.swordfish.lemuroid.app.shared.catalog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.delay

/**
 * Live status of the web-games catalog sync ([RemoteCatalogSyncWork]).
 *
 * @param running true while the sync work is enqueued or running.
 * @param progress 0..100 download/replace progress, or null when indeterminate
 *   (enqueued but not yet reporting).
 */
data class CatalogSyncState(
    val running: Boolean,
    val progress: Int?,
) {
    /** Progress as a 0f..1f fraction, or null for an indeterminate bar. */
    val fraction: Float? get() = progress?.let { it / 100f }
}

/**
 * Observes the unique catalog-sync work and returns its live [CatalogSyncState].
 * Recomposes as the worker moves ENQUEUED → RUNNING → SUCCESS and as it publishes
 * progress.
 */
@Composable
fun rememberCatalogSyncState(): CatalogSyncState {
    val context = LocalContext.current
    val flow = remember(context) {
        WorkManager.getInstance(context.applicationContext)
            .getWorkInfosForUniqueWorkFlow(RemoteCatalogSyncWork.ONESHOT_WORK_NAME)
    }
    val infos by flow.collectAsState(initial = emptyList())

    return remember(infos) {
        val running = infos.firstOrNull { it.state == WorkInfo.State.RUNNING }
        val active = running ?: infos.firstOrNull { it.state == WorkInfo.State.ENQUEUED }
        CatalogSyncState(
            running = active != null,
            progress = running
                ?.progress
                ?.getInt(RemoteCatalogSyncWork.PROGRESS_KEY, -1)
                ?.takeIf { it in 0..100 },
        )
    }
}

/** A smoothly-animated catalog-sync progress ready to bind to a progress indicator. */
data class SyncProgressUi(
    /** True once the worker reports real progress (a genuine populate/replace), animating. */
    val visible: Boolean,
    /** 0f..1f, monotonically increasing, always animates to 1f before hiding. */
    val progress: Float,
    /** True while the sync work is enqueued or running (regardless of progress reporting). */
    val running: Boolean,
)

/**
 * Turns the raw [CatalogSyncState] into a buttery progress value:
 * - interpolates smoothly between the sparse progress reports (no snapping),
 * - never runs backwards,
 * - and, crucially, always finishes the ring to 100% when the sync ends before
 *   fading out — so the animation is never left half-drawn.
 *
 * The ring becomes visible only once the worker publishes real progress (a genuine
 * populate/replace). A routine version-check that reports nothing never shows a ring —
 * so a slow background run can't look "stuck" on an ordinary launch.
 *
 * The fill is an OPTIMISTIC TRICKLE, not the worker's raw byte count: the manifest is
 * small (~440 KB) so a fast download finishes in well under a second and WorkManager
 * coalesces away the intermediate setProgress reports — the raw value would just sit at
 * 0 and then jump. Instead we ease steadily toward 90% on a timer (jumping ahead if the
 * worker ever reports something higher) and always drive home to 100% on completion, so
 * the ring is visibly moving from the first frame and never looks stuck.
 */
@Composable
fun rememberSmoothCatalogProgress(): SyncProgressUi {
    val state = rememberCatalogSyncState()
    val anim = remember { Animatable(0f) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(state.running, state.progress) {
        val hasProgress = state.progress != null
        when {
            // Show + trickle while running AND either progress is being reported now or the
            // ring is already on screen. Target = max(reported, 90%) so it always creeps
            // forward; coerceAtLeast prevents any backward step.
            state.running && (hasProgress || visible) -> {
                visible = true
                val reported = state.progress?.let { it / 100f } ?: 0f
                val target = maxOf(reported, TRICKLE_CAP).coerceAtLeast(anim.value)
                // Steady linear creep from the current value up to the cap over ~8s, so it
                // reads like real progress (rather than rushing to the cap and sitting).
                // A fast sync cuts it short with the finish animation below; the remaining
                // distance is animated proportionally so it never snaps.
                val remaining = (target - anim.value).coerceAtLeast(0f)
                val durationMs = (remaining / TRICKLE_CAP * 8000f).toInt().coerceAtLeast(1)
                anim.animateTo(target, animationSpec = tween(durationMillis = durationMs, easing = LinearEasing))
            }
            // Sync finished after we'd shown the ring: always drive it home to 100%,
            // hold briefly, then hide and reset for the next run.
            !state.running && visible -> {
                anim.animateTo(1f, animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing))
                delay(300)
                visible = false
                anim.snapTo(0f)
            }
            // running but nothing reported yet and not visible → silent check; render nothing.
        }
    }

    return SyncProgressUi(visible = visible, progress = anim.value, running = state.running)
}

/** Trickle ceiling: the optimistic bar eases up to here, then waits for real completion. */
private const val TRICKLE_CAP = 0.9f
