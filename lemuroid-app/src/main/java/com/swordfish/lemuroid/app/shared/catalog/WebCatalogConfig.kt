/*
 * WebCatalogConfig.kt
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

/**
 * Constants for the GameHub web-games catalog hosted on the public `gameshub` GCS
 * bucket. The manifest (`games/catalog.json`) is downloaded and cached by
 * [RemoteCatalogSyncWork]; each game is then downloaded + played offline by
 * `WebGameActivity`.
 */
object WebCatalogConfig {
    const val BASE = "https://storage.googleapis.com/gameshub"
    const val CATALOG_URL = "$BASE/games/catalog.json"

    /** synthetic systemId for web games; not a real GameSystem, so they never appear
     *  in the Systems list, ROM scan, recents or search. */
    const val WEB_SYSTEM_ID = "webgame"

    /** file:// scheme prefix used as the unique key/fileUri for a web game row. */
    const val WEB_URI_SCHEME = "webgame://"

    const val CACHE_TTL_MS = 24L * 60L * 60L * 1000L // 24h

    // SharedPreferences keys (default prefs).
    const val PREF_CATALOG_VERSION = "web_catalog_version"
    const val PREF_CATALOG_FETCHED_AT = "web_catalog_fetched_at"
    const val PREF_CDN_URL = "web_catalog_cdn_url"
    const val PREF_CDN_SHA = "web_catalog_cdn_sha"

    fun slugFromUri(fileUri: String): String = fileUri.removePrefix(WEB_URI_SCHEME)

    fun uriForSlug(slug: String): String = "$WEB_URI_SCHEME$slug"
}
