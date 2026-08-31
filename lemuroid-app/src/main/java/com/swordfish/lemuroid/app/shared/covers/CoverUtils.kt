package com.swordfish.lemuroid.app.shared.covers

import android.content.Context
import android.widget.ImageView
import coil.ImageLoader
import coil.disk.DiskCache
import coil.imageLoader
import coil.load
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.swordfish.lemuroid.common.drawable.TextDrawable
import com.swordfish.lemuroid.common.graphics.ColorUtils
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.Collections
import java.util.WeakHashMap

object CoverUtils {
    // Games whose custom cover is still being looked up, so a recycled view does not end up
    // displaying the cover of the game it used to hold. Only touched from the main thread.
    private val pendingCovers = Collections.synchronizedMap(WeakHashMap<ImageView, String>())

    @OptIn(DelicateCoroutinesApi::class)
    fun loadCover(
        game: Game,
        imageView: ImageView?,
    ) {
        if (imageView == null) return

        val customCover = CustomCovers.cached(game)

        imageView.load(customCover ?: game.coverFrontUrl, imageView.context.imageLoader) {
            val fallbackDrawable = getFallbackDrawable(game)
            fallback(fallbackDrawable)
            error(fallbackDrawable)
            if (customCover != null) {
                // Covers are replaced in place, so their uri alone is not a stable cache key.
                memoryCacheKey("$customCover#${CustomCovers.revision.value}")
                diskCachePolicy(CachePolicy.DISABLED)
            }
        }

        // The first time we see a game we do not know yet whether the user set a cover for it.
        if (customCover == null && !CustomCovers.isResolved(game)) {
            pendingCovers[imageView] = game.fileUri
            val context = imageView.context.applicationContext
            GlobalScope.launch(Dispatchers.Main) {
                val resolved = CustomCovers.resolve(context, game)
                if (resolved != null && pendingCovers[imageView] == game.fileUri) {
                    loadCover(game, imageView)
                }
            }
        } else {
            pendingCovers.remove(imageView)
        }
    }

    fun buildImageLoader(applicationContext: Context): ImageLoader {
        return ImageLoader.Builder(applicationContext)
            .diskCache(
                DiskCache.Builder()
                    .directory(applicationContext.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.20)
                    .build(),
            )
            .memoryCache {
                MemoryCache.Builder(applicationContext)
                    .maxSizePercent(0.20)
                    .build()
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .addNetworkInterceptor(ThrottleFailedThumbnailsInterceptor)
                    .build()
            }
            .crossfade(true)
            .interceptorDispatcher(Dispatchers.IO)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .build()
    }

    fun getFallbackDrawable(game: Game) = TextDrawable(computeTitle(game), computeColor(game))

    fun getFallbackRemoteUrl(game: Game): String {
        val color = Integer.toHexString(computeColor(game)).substring(2)
        val title = computeTitle(game)
        return "https://fakeimg.pl/512x512/$color/fff/?font=bebas&text=$title"
    }

    private fun computeTitle(game: Game): String {
        val sanitizedName =
            game.title
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
