/*
 * GameDao.kt
 *
 * Copyright (C) 2017 Retrograde Project
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

package com.swordfish.lemuroid.lib.library.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun selectById(id: Int): Game?

    @Query("SELECT * FROM games WHERE fileUri = :fileUri")
    fun selectByFileUri(fileUri: String): Game?

    @Query("SELECT * FROM games WHERE lastIndexedAt < :lastIndexedAt AND isCatalogGame = 0")
    fun selectByLastIndexedAtLessThan(lastIndexedAt: Long): List<Game>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY title ASC")
    fun selectFavorites(): PagingSource<Int, Game>

    @Query(
        """
        SELECT * FROM games WHERE lastPlayedAt IS NOT NULL AND isFavorite = 0 ORDER BY lastPlayedAt DESC LIMIT :limit
        """,
    )
    fun selectFirstUnfavoriteRecents(limit: Int): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun selectFirstFavoritesRecents(limit: Int): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT :limit")
    suspend fun asyncSelectFirstRecents(limit: Int): List<Game>

    @Query("SELECT * FROM games WHERE isFavorite = 1 ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun selectFirstFavorites(limit: Int): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE lastPlayedAt IS NULL AND isCatalogGame = 0 ORDER BY lastIndexedAt DESC LIMIT :limit")
    fun selectFirstNotPlayed(limit: Int): Flow<List<Game>>

    // All installed (scanned) games — used by the flat Home grid ("Games" filter),
    // recents first then alphabetical.
    @Query("SELECT * FROM games WHERE isCatalogGame = 0 ORDER BY lastPlayedAt IS NULL, lastPlayedAt DESC, title ASC")
    fun selectVisibleGames(): Flow<List<Game>>

    // Home "Recent" chip: every game the user has launched (ROM or web), newest first.
    @Query("SELECT * FROM games WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun selectRecentGames(limit: Int): Flow<List<Game>>

    // Home "New" chip: the most recently added user ROMs (scanned games), newest first.
    @Query("SELECT * FROM games WHERE isCatalogGame = 0 ORDER BY lastIndexedAt DESC LIMIT :limit")
    fun selectNewGames(limit: Int): Flow<List<Game>>

    @Query("UPDATE games SET lastPlayedAt = :timestamp WHERE webGameSlug = :slug")
    suspend fun touchWebGamePlayed(slug: String, timestamp: Long)

    @Query("SELECT * FROM games WHERE systemId = :systemId ORDER BY title ASC, id DESC")
    fun selectBySystem(systemId: String): PagingSource<Int, Game>

    @Query("SELECT * FROM games WHERE systemId IN (:systemIds) ORDER BY title ASC, id DESC")
    fun selectBySystems(systemIds: List<String>): PagingSource<Int, Game>

    @Query("SELECT DISTINCT systemId FROM games ORDER BY systemId ASC")
    suspend fun selectSystems(): List<String>

    @Query("SELECT count(*) FROM games WHERE isCatalogGame = 0")
    suspend fun countScannedGames(): Int

    @Query("SELECT count(*) count, systemId systemId FROM games GROUP BY systemId")
    fun selectSystemsWithCount(): Flow<List<SystemCount>>

    // Bundled console catalog games first (3ds/nds sort before the synthetic 'webgame'),
    // then GameHub web games (free tier first).
    @Query("SELECT * FROM games WHERE isCatalogGame = 1 ORDER BY (webGameSlug IS NOT NULL) ASC, isFreeTier DESC, systemId ASC, title ASC")
    fun selectCatalogGames(): Flow<List<Game>>

    // Legacy cleanup for the BUNDLED console catalog only. Must exclude GameHub web
    // games (webGameSlug != null, fileUri = "webgame://…") — they legitimately don't use
    // file:// URIs, and without this guard the bundled CatalogSyncWork would wipe the
    // entire web catalog on every launch.
    @Query("DELETE FROM games WHERE isCatalogGame = 1 AND webGameSlug IS NULL AND fileUri NOT LIKE 'file://%'")
    suspend fun deleteLegacyCatalogGames()

    // ---- GameHub web games (webGameSlug != null). Free tier first, then alphabetical. ----

    @Query(
        """
        SELECT * FROM games WHERE isCatalogGame = 1 AND webGameSlug IS NOT NULL
        ORDER BY isFreeTier DESC, title ASC
        """,
    )
    fun selectWebCatalogGames(): Flow<List<Game>>

    @Query("SELECT * FROM games WHERE webGameSlug = :slug LIMIT 1")
    suspend fun selectWebGameBySlug(slug: String): Game?

    @Query("SELECT * FROM games WHERE isCatalogGame = 1 AND webGameSlug IS NOT NULL")
    suspend fun selectAllWebGames(): List<Game>

    @Query("DELETE FROM games WHERE webGameSlug IS NOT NULL AND webGameSlug NOT IN (:slugs)")
    suspend fun deleteWebGamesNotIn(slugs: List<String>)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsertWebGames(games: List<Game>)

    @Query("DELETE FROM games WHERE webGameSlug IS NOT NULL AND lastIndexedAt < :stamp")
    suspend fun deleteWebGamesOlderThan(stamp: Long)

    /**
     * Atomically replace the whole web-games set in one transaction: upsert all rows
     * (content-addressed by fileUri) then evict any web game not touched this sync
     * (its lastIndexedAt stays below [stamp]). All-or-nothing — an interrupted sync
     * rolls back instead of leaving a partial catalog, and Room emits a single change.
     */
    @Transaction
    suspend fun replaceWebCatalog(games: List<Game>, stamp: Long) {
        upsertWebGames(games)
        deleteWebGamesOlderThan(stamp)
    }

    @Insert
    fun insert(games: List<Game>): List<Long>

    @Insert
    suspend fun insert(game: Game): Long

    @Delete
    fun delete(games: List<Game>)

    @Update
    suspend fun update(game: Game)

    @Update
    fun update(games: List<Game>)
}

data class SystemCount(val systemId: String, val count: Int)
