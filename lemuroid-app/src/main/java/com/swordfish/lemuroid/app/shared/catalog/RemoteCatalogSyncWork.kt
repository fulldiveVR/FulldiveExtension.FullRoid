/*
 * RemoteCatalogSyncWork.kt
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

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.swordfish.lemuroid.lib.injection.AndroidWorkerInjection
import com.swordfish.lemuroid.lib.injection.WorkerKey
import com.swordfish.lemuroid.lib.library.db.RetrogradeDatabase
import com.swordfish.lemuroid.lib.library.db.entity.Game
import dagger.Binds
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Downloads the GameHub web-games manifest (`catalog.json`) from the GCS bucket,
 * caches it, and upserts one [Game] row per game (isCatalogGame = 1, webGameSlug set).
 * Refreshed daily via a periodic request; a one-off catch-up is enqueued on app
 * start when the cache is older than [WebCatalogConfig.CACHE_TTL_MS] or missing.
 *
 * Offline / any failure: the cached DB rows are kept and the work is retried.
 */
class RemoteCatalogSyncWork(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var retrogradeDb: RetrogradeDatabase

    override suspend fun doWork(): Result {
        AndroidWorkerInjection.inject(this)

        return withContext(Dispatchers.IO) {
            runCatching { sync() }
                .fold(
                    onSuccess = { Result.success() },
                    onFailure = {
                        Timber.e(it, "RemoteCatalogSyncWork failed")
                        Result.retry()
                    },
                )
        }
    }

    private suspend fun sync() {
        val body = httpGet("${WebCatalogConfig.CATALOG_URL}?t=${System.currentTimeMillis()}")
        val root = JSONObject(body)
        val gamesArray = root.optJSONArray("games") ?: return

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val dao = retrogradeDb.gameDao()

        // Skip when the catalog version is unchanged. The version pref is written only
        // AFTER a full replace succeeds (see below), so a version match guarantees the
        // previous sync completed and the DB already holds this exact catalog — no need
        // to touch the DB again (avoids re-running the transaction / re-rendering the grid
        // on every launch). When the version differs we do one atomic replace.
        val fetchedVersion = root.optString("version")
        val storedVersion = prefs.getString(WebCatalogConfig.PREF_CATALOG_VERSION, null)
        val dbHasGames = runCatching { dao.selectAllWebGames().isNotEmpty() }.getOrDefault(false)
        // Skip only if the version matches AND the DB still holds games. The dbHasGames
        // guard re-syncs if anything wiped the web catalog (e.g. a bundled-catalog cleanup)
        // even when the version is unchanged.
        if (fetchedVersion.isNotEmpty() && fetchedVersion == storedVersion && dbHasGames) {
            prefs.edit().putLong(WebCatalogConfig.PREF_CATALOG_FETCHED_AT, System.currentTimeMillis()).apply()
            return
        }

        // Shared loader bundle (Unity master-loader chain). Optional.
        root.optJSONObject("cdn")?.let { cdn ->
            val path = cdn.optString("path")
            if (path.isNotEmpty()) {
                prefs.edit()
                    .putString(WebCatalogConfig.PREF_CDN_URL, "${WebCatalogConfig.BASE}/$path")
                    .putString(WebCatalogConfig.PREF_CDN_SHA, cdn.optString("sha256"))
                    .apply()
            }
        }

        val now = System.currentTimeMillis()
        val games = ArrayList<Game>(gamesArray.length())

        for (i in 0 until gamesArray.length()) {
            val o = gamesArray.getJSONObject(i)
            val slug = o.optString("slug").takeIf { it.isNotEmpty() } ?: continue
            val zip = o.optJSONObject("zip") ?: continue
            val zipPath = zip.optString("path").takeIf { it.isNotEmpty() } ?: continue
            val sha = zip.optString("sha256").takeIf { it.isNotEmpty() }

            val title = o.optString("title", slug)
            val orientation = o.optString("orientation", "any")
            val isFree = o.optString("tier", "pro") == "free"
            val zipUrl = "${WebCatalogConfig.BASE}/$zipPath"
            val coverUrl = o.optJSONObject("cover")
                ?.optString("path")
                ?.takeIf { it.isNotEmpty() }
                ?.let { "${WebCatalogConfig.BASE}/$it" }

            games.add(
                Game(
                    fileName = slug,
                    fileUri = WebCatalogConfig.uriForSlug(slug),
                    title = title,
                    systemId = WebCatalogConfig.WEB_SYSTEM_ID,
                    developer = null,
                    coverFrontUrl = coverUrl,
                    lastIndexedAt = now,
                    isCatalogGame = true,
                    webGameSlug = slug,
                    webZipUrl = zipUrl,
                    webZipSha256 = sha,
                    webOrientation = orientation,
                    isFreeTier = isFree,
                ),
            )
        }

        // Never wipe on an empty/failed fetch: replaceWebCatalog would delete every game
        // (deleteWebGamesOlderThan with nothing re-inserted). Keep the existing catalog.
        if (games.isEmpty()) {
            Timber.w("RemoteCatalogSync: fetched 0 games — keeping existing catalog")
            return
        }

        // One atomic transaction: replace the whole web set + evict stale. No partial
        // state, no huge IN() bind list, correct tiers, single Room emission.
        dao.replaceWebCatalog(games, now)

        // Store the version ONLY after a successful replace, so a version match on the
        // next launch reliably means the DB is complete and the sync can be skipped.
        prefs.edit()
            .putString(WebCatalogConfig.PREF_CATALOG_VERSION, fetchedVersion)
            .putLong(WebCatalogConfig.PREF_CATALOG_FETCHED_AT, System.currentTimeMillis())
            .apply()
        Timber.i("RemoteCatalogSync: replaced catalog=${games.size} free=${games.count { it.isFreeTier }}")
    }

    private fun httpGet(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 30000
            setRequestProperty("Cache-Control", "no-cache")
        }
        try {
            return conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }

    @dagger.Module(subcomponents = [Subcomponent::class])
    abstract class Module {
        @Binds
        @IntoMap
        @WorkerKey(RemoteCatalogSyncWork::class)
        abstract fun bindWorkerFactory(builder: Subcomponent.Builder): AndroidInjector.Factory<out ListenableWorker>
    }

    @dagger.Subcomponent
    interface Subcomponent : AndroidInjector<RemoteCatalogSyncWork> {
        @dagger.Subcomponent.Builder
        abstract class Builder : AndroidInjector.Builder<RemoteCatalogSyncWork>()
    }

    companion object {
        private const val PERIODIC_WORK_NAME = "web_catalog_sync_periodic"
        private const val ONESHOT_WORK_NAME = "web_catalog_sync_oneshot"

        fun schedule(context: Context) {
            // Single sync on launch. Deliberately NOT a periodic + oneshot pair: two
            // separate unique works both fire RemoteCatalogSyncWork and can run
            // concurrently, racing on the upsert/deleteWebGamesNotIn pass and briefly
            // wiping games (catalog flickers empty then repopulates). One unique oneshot
            // with KEEP guarantees at most one running sync; the version-check in sync()
            // makes it a no-op when the catalog is unchanged. Launch-based refresh is
            // enough (the app is opened regularly).
            // REPLACE (not KEEP): a finished unique oneshot won't re-run under KEEP, so the
            // catalog would never refresh after the first sync. REPLACE cancels any prior
            // run and starts fresh each launch — still a single unique work (no concurrent
            // race), and the atomic replaceWebCatalog transaction rolls back cleanly if a
            // launch cancels a sync mid-flight. The version-check in sync() keeps it cheap.
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    ONESHOT_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<RemoteCatalogSyncWork>()
                        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                        .build(),
                )

            // Cancel any legacy periodic from older builds (it caused the concurrent race).
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
        }
    }
}
