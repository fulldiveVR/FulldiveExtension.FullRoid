/*
 *  RetrogradeApplicationComponent.kt
 *
 *  Copyright (C) 2017 Retrograde Project
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.swordfish.lemuroid.metadata.libretrodb

import com.swordfish.lemuroid.lib.library.GameSystem
import com.swordfish.lemuroid.lib.library.metadata.GameMetadataProvider
import com.swordfish.lemuroid.lib.storage.StorageFile
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDBManager
import com.swordfish.lemuroid.metadata.libretrodb.db.LibretroDatabase
import com.swordfish.lemuroid.metadata.libretrodb.db.entity.LibretroRom
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.swordfish.lemuroid.common.rx.toSingleAsOptional
import com.swordfish.lemuroid.lib.library.SystemID
import com.swordfish.lemuroid.lib.library.metadata.GameMetadata
import io.reactivex.Maybe
import io.reactivex.Single
import timber.log.Timber
import java.util.Locale

class LibretroDBMetadataProvider(private val ovgdbManager: LibretroDBManager) :
    GameMetadataProvider {

    private val sortedSystemIds: List<String> by lazy {
        SystemID.values()
            .map { it.dbname }
            .sortedByDescending { it.length }
    }

    override fun retrieveMetadata(storageFile: StorageFile): Single<Optional<GameMetadata>> {
        return ovgdbManager.dbReady
            .flatMap { db: LibretroDatabase ->
                Single.just(storageFile)
                    .doOnSuccess { Timber.d("Looking metadata for file: $it") }
                    .flatMap { file ->
                        findByCRC(file, db)
                            .switchIfEmpty(findBySerial(file, db))
                            .switchIfEmpty(findByFilename(db, file))
                            .switchIfEmpty(findByPathAndFilename(db, file))
                            .switchIfEmpty(findByUniqueExtension(file))
                            .switchIfEmpty(findByKnownSystem(file))
                            .switchIfEmpty(findByPathAndSupportedExtension(file))
                            .doOnSuccess { Timber.d("Metadata retrieved for item: $it") }
                            .toSingleAsOptional()
                            .doOnError { Timber.e("Error in retrieving $file metadata: $it... Skipping.") }
                            .onErrorReturn { None }
                    }
            }
    }

    private fun convertToGameMetadata(rom: LibretroRom): GameMetadata {
        val system = GameSystem.findById(rom.system!!)
        return GameMetadata(
            name = rom.name,
            romName = rom.romName,
            thumbnail = computeCoverUrl(system, rom.name),
            system = rom.system,
            developer = rom.developer
        )
    }

    private fun findByFilename(db: LibretroDatabase, file: StorageFile): Maybe<GameMetadata> {
        return db.gameDao().findByFileName(file.name)
            .filter { extractGameSystem(it).scanOptions.scanByFilename }
            .map { convertToGameMetadata(it) }
    }

    private fun findByPathAndFilename(
        db: LibretroDatabase,
        file: StorageFile
    ): Maybe<GameMetadata> {
        return db.gameDao().findByFileName(file.name)
            .filter { extractGameSystem(it).scanOptions.scanByPathAndFilename }
            .filter { parentContainsSystem(file.path, extractGameSystem(it).id.dbname) }
            .map { convertToGameMetadata(it) }
    }

    private fun findByPathAndSupportedExtension(file: StorageFile) = Maybe.fromCallable {
        val system = sortedSystemIds
            .filter { parentContainsSystem(file.path, it) }
            .map { GameSystem.findById(it) }
            .filter { it.scanOptions.scanByPathAndSupportedExtensions }
            .firstOrNull { it.supportedExtensions.contains(file.extension) }

        system?.let {
            GameMetadata(
                name = file.extensionlessName,
                romName = file.name,
                thumbnail = null,
                system = it.id.dbname,
                developer = null
            )
        }
    }

    private fun parentContainsSystem(parent: String?, dbname: String): Boolean {
        return parent?.toLowerCase(Locale.getDefault())?.contains(dbname) == true
    }

    private fun findByCRC(file: StorageFile, db: LibretroDatabase): Maybe<GameMetadata> {
        if (file.crc == null || file.crc == "0") return Maybe.empty()
        return file.crc?.let { crc32 ->
            db.gameDao().findByCRC(crc32).map {
                convertToGameMetadata(it)
            }
        } ?: Maybe.empty()
    }

    private fun findBySerial(file: StorageFile, db: LibretroDatabase): Maybe<GameMetadata> {
        if (file.serial == null) return Maybe.empty()
        return db.gameDao().findBySerial(file.serial!!)
            .map { convertToGameMetadata(it) }
    }

    private fun findByKnownSystem(file: StorageFile) = Maybe.fromCallable<GameMetadata> {
        if (file.systemID == null) return@fromCallable null

        GameMetadata(
            name = file.extensionlessName,
            romName = file.name,
            thumbnail = null,
            system = file.systemID!!.dbname,
            developer = null,
        )
    }

    private fun findByUniqueExtension(file: StorageFile) = Maybe.fromCallable {
        val system = GameSystem.findByUniqueFileExtension(file.extension)

        if (system?.scanOptions?.scanByUniqueExtension == false) {
            return@fromCallable null
        }

        val result = system?.let {
            GameMetadata(
                name = file.extensionlessName,
                romName = file.name,
                thumbnail = null,
                system = it.id.dbname,
                developer = null
            )
        }

        result
    }

    private fun extractGameSystem(rom: LibretroRom): GameSystem {
        return GameSystem.findById(rom.system!!)
    }

    private fun computeCoverUrl(system: GameSystem, name: String?): String? {
        var systemName = system.libretroFullName

        // Specific mame version don't have any thumbnails in Libretro database
        if (system.id == SystemID.MAME2003PLUS) {
            systemName = "MAME"
        }

        if (name == null) {
            return null
        }

        val imageType = "Named_Boxarts"

        val thumbGameName = name.replace("&", "_")

        return "http://thumbnails.libretro.com/$systemName/$imageType/$thumbGameName.png"
    }
}
