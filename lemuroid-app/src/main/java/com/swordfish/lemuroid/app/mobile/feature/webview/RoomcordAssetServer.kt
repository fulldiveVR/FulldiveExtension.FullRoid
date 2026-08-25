/*
 * RoomcordAssetServer.kt
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

package com.swordfish.lemuroid.app.mobile.feature.webview

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.io.IOException

/**
 * Serves the Roomcord web app out of `assets/roomcord/` so the WebView never
 * downloads the shell from Cloudflare (slow or blocked in some countries).
 *
 * The origin stays `https://web.roomcord.com`: requests to that host are
 * answered from the APK, everything else (the API, socket.io, media, sign-in)
 * goes to the network untouched. Keeping the real origin is what preserves
 * cookies / localStorage — a fake host would log every user out exactly once.
 *
 * An asset that is not in the bundle falls back to the live site (return null →
 * WebView fetches the same URL), with two deliberate exceptions:
 *
 *  - [VERSION_JSON]: a live copy would report a newer build than the frozen
 *    bundle and resurrect the in-app "Update available" banner, which cannot be
 *    acted on from inside an embedded build.
 *  - boot-critical files: on a blocked network a fallback does not fail, it
 *    hangs until the WebView timeout. A clean 404 is strictly better — the app
 *    cannot start either way.
 *
 * The bundle is produced by `emulator/update_roomcord_web.sh`; see
 * `docs/plans/roomcord-embedded-webview.md`.
 */
object RoomcordAssetServer {
    const val HOST = "web.roomcord.com"

    private const val ROOT = "roomcord"
    private const val INDEX = "index.html"
    private const val VERSION_JSON = "version.json"
    private const val GSTATIC_HOST = "www.gstatic.com"
    private const val GSTATIC_ROOT = "roomcord/__gstatic"

    private const val TAG_MISS = "RoomcordAssetMiss"
    private const val TAG_FALLBACK = "RoomcordAssetFallback"
    private const val TAG_SERVE = "RoomcordAsset"

    /**
     * @return a response served from the APK, or null to let the WebView fetch
     *   the request from the network as usual.
     */
    fun intercept(
        context: Context,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        val url = request.url
        return when (url.host) {
            HOST -> serve(context, url.path.orEmpty())
            // Only serves anything when the bundle was packed with
            // --vendor-gstatic; otherwise falls through to the network.
            GSTATIC_HOST -> serveGstatic(context, url.path.orEmpty())
            else -> null
        }
    }

    /**
     * True when [path] is one the app cannot boot without.
     *
     * Note `canvaskit/`: the bundle ships only the `chromium/` variant, because
     * Android System WebView is always Chromium and the Flutter loader picks
     * that one whenever `ImageDecoder` + the Intl break iterators exist. A
     * WebView old enough to ask for the full `canvaskit/canvaskit.*` pair
     * (pre-94) cannot run this Flutter build at all, so a logged 404 is the
     * honest outcome — better than hanging on a network fetch it can't use.
     */
    fun isBootCritical(path: String): Boolean =
        path == INDEX ||
            path == "main.dart.js" ||
            path == "flutter.js" ||
            path == "flutter_bootstrap.js" ||
            path.startsWith("canvaskit/") ||
            path.startsWith("assets/AssetManifest") ||
            path == "assets/FontManifest.json"

    private fun serve(
        context: Context,
        rawPath: String,
    ): WebResourceResponse? {
        val path = normalize(rawPath)

        // Pinned local: the update banner must never be able to see prod.
        if (path == VERSION_JSON) {
            open(context, "$ROOT/$path")?.let {
                // Logged so QA can prove the update check never reached the CDN.
                Log.d(TAG_SERVE, "served version.json from the bundle")
                return ok(path, it)
            }
            Log.e(TAG_MISS, "version.json missing from the bundle")
            return notFound()
        }

        open(context, "$ROOT/$path")?.let { return ok(path, it) }

        // An in-app route (/join/i-XXXXXX, /m/<room>/<msg>, ...) — serve the
        // shell and let the Flutter router read the URL. `<base href="/">`
        // keeps asset URLs root-relative whatever the entry path is.
        if (!hasExtension(path)) {
            open(context, "$ROOT/$INDEX")?.let { return ok(INDEX, it) }
        }

        if (isBootCritical(path)) {
            Log.e(TAG_MISS, "boot-critical asset missing: $rawPath")
            return notFound()
        }

        Log.w(TAG_FALLBACK, "not bundled, going online: $rawPath")
        return null
    }

    private fun serveGstatic(
        context: Context,
        rawPath: String,
    ): WebResourceResponse? {
        val path = rawPath.trimStart('/')
        if (!path.startsWith("firebasejs/")) return null
        val asset = open(context, "$GSTATIC_ROOT/$path") ?: return null
        return ok(path, asset)
    }

    /** `/`, `/index.html?x=1` and `/rooms/` all resolve to a bundle-relative path. */
    private fun normalize(rawPath: String): String {
        var path = rawPath.substringBefore('?').substringBefore('#').trimStart('/')
        if (path.isEmpty() || path.endsWith("/")) path += INDEX
        return path
    }

    private fun hasExtension(path: String): Boolean = path.substringAfterLast('/').contains('.')

    private fun open(
        context: Context,
        assetPath: String,
    ): java.io.InputStream? =
        try {
            context.assets.open(assetPath)
        } catch (e: IOException) {
            null
        }

    private fun ok(
        path: String,
        stream: java.io.InputStream,
    ): WebResourceResponse {
        val mime = mimeOf(path)
        // Only text formats get an explicit charset; handing one to binary
        // payloads (wasm, fonts, images) makes WebView mangle them.
        val isText = mime.startsWith("text/") || mime.endsWith("json") || mime.endsWith("javascript")
        val encoding = if (isText) "utf-8" else null
        return WebResourceResponse(mime, encoding, 200, "OK", HashMap(), stream)
    }

    private fun notFound(): WebResourceResponse =
        WebResourceResponse(
            "text/plain",
            "utf-8",
            404,
            "Not Found",
            emptyMap(),
            ByteArrayInputStream(ByteArray(0)),
        )

    private fun mimeOf(path: String): String {
        val ext = path.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "js", "mjs" -> "text/javascript"
            "wasm" -> "application/wasm"
            "json", "map" -> "application/json"
            "html" -> "text/html"
            "css" -> "text/css"
            "bin", "symbols" -> "application/octet-stream"
            "otf" -> "font/otf"
            "ttf" -> "font/ttf"
            "woff" -> "font/woff"
            "woff2" -> "font/woff2"
            "svg" -> "image/svg+xml"
            "frag", "vert" -> "text/plain"
            else ->
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                    ?: "application/octet-stream"
        }
    }
}
