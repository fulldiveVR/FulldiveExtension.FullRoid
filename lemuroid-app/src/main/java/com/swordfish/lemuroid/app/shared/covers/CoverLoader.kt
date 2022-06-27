/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package com.swordfish.lemuroid.app.shared.covers

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.load
import coil.util.CoilUtils
import com.swordfish.lemuroid.common.drawable.TextDrawable
import com.swordfish.lemuroid.common.graphics.ColorUtils
import com.swordfish.lemuroid.lib.library.db.entity.Game
import okhttp3.OkHttpClient

class CoverLoader(applicationContext: Context) {

    private val imageLoader = ImageLoader.Builder(applicationContext)
        .crossfade(true)
        .okHttpClient {
            OkHttpClient.Builder()
                .cache(CoilUtils.createDefaultCache(applicationContext))
                .addNetworkInterceptor(ThrottleFailedThumbnailsInterceptor)
                .build()
        }
        .build()

    fun loadCover(game: Game, imageView: ImageView?) {
        if (imageView == null) return

        imageView.load(game.coverFrontUrl, imageLoader) {
            val fallbackDrawable = getFallbackDrawable(game)
            fallback(fallbackDrawable)
            error(fallbackDrawable)
        }
    }

    fun cancelRequest(imageView: ImageView) {
        // coil-kt automatically does that for us.
    }

    companion object {
        fun getFallbackDrawable(game: Game) =
            TextDrawable(computeTitle(game), computeColor(game))

        fun getFallbackRemoteUrl(game: Game): String {
            val color = Integer.toHexString(computeColor(game)).substring(2)
            val title = computeTitle(game)
            return "https://fakeimg.pl/512x512/$color/fff/?font=bebas&text=$title"
        }

        private fun computeTitle(game: Game): String {
            val sanitizedName = game.title
                .replace(Regex("\\(.*\\)"), "")

            return sanitizedName.asSequence()
                .filter { it.isDigit() or it.isUpperCase() or (it == '&') }
                .take(3)
                .joinToString("")
                .ifBlank { game.title.first().toString() }
                .capitalize()
        }

        private fun computeColor(game: Game): Int {
            return ColorUtils.randomColor(game.title)
        }
    }
}
