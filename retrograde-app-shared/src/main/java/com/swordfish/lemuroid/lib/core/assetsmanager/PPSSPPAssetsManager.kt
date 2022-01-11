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

package com.swordfish.lemuroid.lib.core.assetsmanager

import android.content.SharedPreferences
import android.net.Uri
import com.swordfish.lemuroid.lib.core.CoreUpdater
import com.swordfish.lemuroid.lib.library.CoreID
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import retrofit2.Response
import timber.log.Timber
import java.io.File
import java.util.zip.ZipInputStream

class PPSSPPAssetsManager : CoreID.AssetsManager {

    override fun clearAssets(directoriesManager: DirectoriesManager) = Completable.fromAction {
        getAssetsDirectory(directoriesManager).deleteRecursively()
    }

    override fun retrieveAssetsIfNeeded(
        coreUpdaterApi: CoreUpdater.CoreManagerApi,
        directoriesManager: DirectoriesManager,
        sharedPreferences: SharedPreferences
    ): Completable {

        return updatedRequested(directoriesManager, sharedPreferences)
            .filter { it }
            .flatMapCompletable {
                coreUpdaterApi.downloadZip(PPSSPP_ASSETS_URL.toString())
                    .doOnSuccess { handleSuccess(directoriesManager, it, sharedPreferences) }
                    .doOnError { getAssetsDirectory(directoriesManager).deleteRecursively() }
                    .ignoreElement()
            }
    }

    private fun handleSuccess(
        directoriesManager: DirectoriesManager,
        response: Response<ZipInputStream>,
        sharedPreferences: SharedPreferences
    ) {
        val coreAssetsDirectory = getAssetsDirectory(directoriesManager)
        coreAssetsDirectory.deleteRecursively()
        coreAssetsDirectory.mkdirs()

        response.body()?.use { zipInputStream ->
            while (true) {
                val entry = zipInputStream.nextEntry ?: break
                Timber.d("Writing file: ${entry.name}")
                val destFile = File(
                    coreAssetsDirectory,
                    entry.name
                )
                if (entry.isDirectory) {
                    destFile.mkdirs()
                } else {
                    zipInputStream.copyTo(destFile.outputStream())
                }
            }
        }

        sharedPreferences.edit()
            .putString(PPSSPP_ASSETS_VERSION_KEY, PPSSPP_ASSETS_VERSION)
            .commit()
    }

    private fun updatedRequested(
        directoriesManager: DirectoriesManager,
        sharedPreferences: SharedPreferences
    ): Single<Boolean> {
        val directoryExists = Single.fromCallable {
            getAssetsDirectory(directoriesManager).exists()
        }

        val hasCurrentVersion = Single
            .fromCallable { sharedPreferences.getString(PPSSPP_ASSETS_VERSION_KEY, "none") }
            .map { it == PPSSPP_ASSETS_VERSION }

        return Singles.zip(directoryExists, hasCurrentVersion) { a, b -> !a || !b }
    }

    private fun getAssetsDirectory(directoriesManager: DirectoriesManager) =
        File(directoriesManager.getSystemDirectory(), PPSSPP_ASSETS_FOLDER_NAME)

    companion object {
        const val PPSSPP_ASSETS_VERSION = "1.11"

        val PPSSPP_ASSETS_URL: Uri = Uri.parse("https://github.com/Swordfish90/LemuroidCores/")
            .buildUpon()
            .appendEncodedPath("raw/$PPSSPP_ASSETS_VERSION/assets/ppsspp.zip")
            .build()

        const val PPSSPP_ASSETS_VERSION_KEY = "ppsspp_assets_version_key"

        const val PPSSPP_ASSETS_FOLDER_NAME = "PPSSPP"
    }
}
