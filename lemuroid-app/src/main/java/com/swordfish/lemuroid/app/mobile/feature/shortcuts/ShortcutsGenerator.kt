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

package com.swordfish.lemuroid.app.mobile.feature.shortcuts

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.shared.deeplink.DeepLink
import com.swordfish.lemuroid.common.bitmap.cropToSquare
import com.swordfish.lemuroid.common.bitmap.toBitmap
import com.swordfish.lemuroid.lib.library.db.entity.Game
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.io.InputStream

class ShortcutsGenerator(
    private val appContext: Context,
    retrofit: Retrofit
) {

    private val thumbnailsApi = retrofit.create(ThumbnailsApi::class.java)

    fun pinShortcutForGame(game: Game): Completable {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return Completable.complete()

        val shortcutManager = appContext.getSystemService(ShortcutManager::class.java)!!

        return Single.fromCallable { game.coverFrontUrl }
            .flatMap { thumbnailsApi.downloadThumbnail(it) }
            .map { BitmapFactory.decodeStream(it.body()).cropToSquare() }
            .onErrorReturn {
                val desiredIconSize = getDesiredIconSize()
                CoverLoader.getFallbackDrawable(game).toBitmap(desiredIconSize, desiredIconSize)
            }
            .map { bitmap ->
                val builder = ShortcutInfo.Builder(appContext, "game_${game.id}")
                    .setShortLabel(game.title)
                    .setLongLabel(game.title)
                    .setIntent(DeepLink.launchIntentForGame(appContext, game))
                    .setIcon(Icon.createWithBitmap(bitmap))

                builder.build()
            }
            .doOnSuccess { shortcutManager.requestPinShortcut(it, null) }
            .ignoreElement()
    }

    private fun getDesiredIconSize(): Int {
        val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        return am?.launcherLargeIconSize ?: 256
    }

    fun supportShortcuts(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return false

        val shortcutManager = appContext.getSystemService(ShortcutManager::class.java)!!
        return shortcutManager.isRequestPinShortcutSupported
    }

    interface ThumbnailsApi {
        @GET
        @Streaming
        fun downloadThumbnail(@Url url: String): Single<Response<InputStream>>
    }
}
