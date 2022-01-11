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

package com.swordfish.lemuroid.lib.storage.cache

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.system.Os
import android.text.format.Formatter
import com.swordfish.lemuroid.lib.storage.local.LocalStorageProvider
import com.swordfish.lemuroid.lib.storage.local.StorageAccessFrameworkProvider
import io.reactivex.Completable
import timber.log.Timber
import java.io.File

object CacheCleaner {

    fun getOptimalCacheSize(): Long {
        // We are capping cache size to be 1/20th of total internal memory...
        return getInternalMemorySize() / 20
    }

    private fun getInternalMemorySize(): Long {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.absolutePath)
        return stat.blockSizeLong * stat.blockCountLong
    }

    fun cleanAll(appContext: Context) = Completable.fromAction {
        Timber.i("Running cache cleanup everything task")
        appContext.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
    }

    fun clean(appContext: Context, maxByteSize: Long) = Completable.fromAction {
        Timber.i("Running cache cleanup lru task")

        val cacheFoldersSequence = sequenceOf(
            File(appContext.cacheDir, StorageAccessFrameworkProvider.SAF_CACHE_SUBFOLDER).walkBottomUp(),
            File(appContext.cacheDir, LocalStorageProvider.LOCAL_STORAGE_CACHE_SUBFOLDER).walkBottomUp()
        )

        val cacheFiles = cacheFoldersSequence.flatten()
            .filter { it.isFile }
            .sortedBy { retrieveLastAccess(it) }
            .toMutableList()

        val cacheSize = cacheFiles
            .map { it.length() }
            .sum()

        Timber.i("Space used by cache: ${printSize(appContext, cacheSize)} / ${printSize(appContext, maxByteSize)}")

        var spaceToBeDeleted = maxOf(cacheSize - maxByteSize, 0)

        Timber.i("Freeing cache space: ${printSize(appContext, spaceToBeDeleted)}")

        while (spaceToBeDeleted > 0) {
            val deletedFile = cacheFiles.removeAt(0)
            val size = deletedFile.length()

            if (deletedFile.delete()) {
                spaceToBeDeleted -= size
                Timber.i("Cache file deleted ${deletedFile.name}, size: ${printSize(appContext, size)}")
            }
        }
    }

    private fun printSize(appContext: Context, size: Long): String {
        return Formatter.formatFileSize(appContext, size)
    }

    private fun retrieveLastAccess(file: File) = Os.lstat(file.absolutePath).st_atime
}
