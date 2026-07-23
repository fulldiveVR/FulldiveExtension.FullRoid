/*
 * WebGameRepository.kt
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

package com.swordfish.lemuroid.app.mobile.feature.webgame

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

/**
 * On-device cache + downloader for GameHub web games. Ported from the `gamehub/android`
 * prototype `Backend` (bucket/prod mode only): given a game's zip URL + sha256, downloads
 * and unpacks it under `filesDir/webgames/<slug>`, verified by a `.sha256` marker so
 * re-runs are skipped and stale copies are refreshed. Also unpacks the shared `/__cdn`
 * loader bundle once. Everything is served offline afterwards by [WebGameActivity].
 */
object WebGameRepository {

    fun gameDir(ctx: Context, slug: String) = File(File(ctx.filesDir, "webgames"), slug)

    fun cdnDir(ctx: Context) = File(File(ctx.filesDir, "webgames"), "__cdn")

    private fun shaMarker(ctx: Context, slug: String) = File(gameDir(ctx, slug), ".sha256")

    private fun cdnMarker(ctx: Context) = File(cdnDir(ctx), ".sha256")

    /** True when the game is unpacked and its `.sha256` marker matches [wantSha]. */
    fun isCached(ctx: Context, slug: String, wantSha: String?): Boolean {
        val entry = File(gameDir(ctx, slug), "index.html")
        if (!entry.exists()) return false
        if (wantSha == null) return true
        val have = runCatching { shaMarker(ctx, slug).readText().trim() }.getOrNull()
        return have == wantSha
    }

    /**
     * Ensure the game is downloaded + unpacked and matches [sha256]. [onProgress] receives
     * bytes-downloaded / total (total may be -1). Returns true when ready to play.
     */
    suspend fun ensureDownloaded(
        ctx: Context,
        slug: String,
        zipUrl: String,
        sha256: String?,
        onProgress: (Long, Long) -> Unit = { _, _ -> },
    ): Boolean = withContext(Dispatchers.IO) {
        if (isCached(ctx, slug, sha256)) return@withContext true
        val ok = downloadAndUnzip(ctx, zipUrl, gameDir(ctx, slug), "$slug.zip.part", onProgress)
        if (ok) sha256?.let { shaMarker(ctx, slug).writeText(it) }
        ok
    }

    /**
     * Ensure the shared `/__cdn` loader bundle is unpacked and matches [cdnSha]. Small
     * (~200 KB), downloaded once. No-op when [cdnUrl] is null (no cdn block in the catalog).
     */
    suspend fun ensureCdn(
        ctx: Context,
        cdnUrl: String?,
        cdnSha: String?,
    ): Boolean = withContext(Dispatchers.IO) {
        if (cdnUrl == null) return@withContext true
        val have = runCatching { cdnMarker(ctx).readText().trim() }.getOrNull()
        if (have != null && have == cdnSha && File(cdnDir(ctx), "loaders").exists()) {
            return@withContext true
        }
        val ok = downloadAndUnzip(ctx, cdnUrl, cdnDir(ctx), "__cdn.zip.part") { _, _ -> }
        if (ok) cdnSha?.let { cdnMarker(ctx).writeText(it) }
        ok
    }

    /** Download the zip from [url] and unpack into [dir] (cleaned first, zip-slip guarded). */
    private fun downloadAndUnzip(
        ctx: Context,
        url: String,
        dir: File,
        tmpName: String,
        onProgress: (Long, Long) -> Unit,
    ): Boolean {
        val tmpZip = File(ctx.cacheDir, tmpName)
        return runCatching {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 60000
            }
            val total = conn.contentLengthLong
            conn.inputStream.use { input ->
                tmpZip.outputStream().use { out ->
                    val buf = ByteArray(64 * 1024)
                    var done = 0L
                    while (true) {
                        val n = input.read(buf); if (n < 0) break
                        out.write(buf, 0, n); done += n
                        onProgress(done, total)
                    }
                }
            }
            conn.disconnect()

            if (dir.exists()) dir.deleteRecursively()
            dir.mkdirs()
            val dirCanon = dir.canonicalPath + File.separator
            ZipInputStream(tmpZip.inputStream().buffered()).use { zin ->
                while (true) {
                    val e = zin.nextEntry ?: break
                    val outFile = File(dir, e.name)
                    if (!outFile.canonicalPath.startsWith(dirCanon)) { zin.closeEntry(); continue }
                    if (e.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        outFile.outputStream().use { zin.copyTo(it) }
                    }
                    zin.closeEntry()
                }
            }
            true
        }.getOrElse {
            runCatching { dir.deleteRecursively() }
            false
        }.also { runCatching { tmpZip.delete() } }
    }
}
