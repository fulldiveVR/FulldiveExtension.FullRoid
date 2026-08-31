package com.swordfish.lemuroid.app.shared.covers

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * User provided game covers.
 *
 * A cover is a plain png file sitting next to the rom and named after it, so
 * "Tetris (R).zip" is covered by "Tetris (R).png". Nothing is stored in the database: the file
 * on disk is the only source of truth, which means covers survive a reinstall and can also be
 * dropped in with a file manager without the app knowing about it.
 *
 * Rom folders picked through the storage access framework are often granted read only access, so
 * when the rom folder cannot be written the cover is kept in app storage instead. Both locations
 * are looked up when displaying a game.
 */
object CustomCovers {
    private const val COVER_EXTENSION = "png"
    private const val COVER_MIME_TYPE = "image/png"
    private const val MAX_COVER_SIZE = 512
    private const val APP_COVERS_FOLDER = "custom_covers"

    sealed interface SaveResult {
        /** The cover was stored next to the rom, where the user can see and manage it. */
        data object NextToRom : SaveResult

        /** The rom folder is not writable, so the cover was kept inside the app instead. */
        data object AppStorage : SaveResult

        data object Failed : SaveResult
    }

    private class Resolved(val generation: Long, val uri: Uri?)

    private class FolderListing(
        val namesByDocumentId: Map<String, String>,
        val documentIdsByName: Map<String, String>,
    )

    private class DocumentSibling(
        val parentUri: Uri,
        val coverName: String,
        val coverUri: Uri?,
    )

    private val resolvedCovers = ConcurrentHashMap<String, Resolved>()
    private val folderListings = ConcurrentHashMap<String, FolderListing>()
    private val generation = AtomicLong(0)
    private val revisionState = MutableStateFlow(0L)

    /** Bumped whenever covers might have changed, so the ones on screen get resolved again. */
    val revision: StateFlow<Long> = revisionState.asStateFlow()

    /**
     * Cover of [game] as resolved by a previous lookup, without touching the disk. A stale value
     * is returned on purpose: it lets the ui display the right cover immediately instead of
     * flashing the scraped one while the file system is being queried again.
     */
    fun cached(game: Game): Uri? = resolvedCovers[game.fileUri]?.uri

    fun isResolved(game: Game): Boolean = resolvedCovers[game.fileUri]?.generation == generation.get()

    /** Cover of [game], or null when the user did not set one. */
    suspend fun resolve(
        context: Context,
        game: Game,
    ): Uri? {
        if (!supportsCustomCover(game)) return null

        val currentGeneration = generation.get()
        resolvedCovers[game.fileUri]
            ?.takeIf { it.generation == currentGeneration }
            ?.let { return it.uri }

        val cover =
            withContext(Dispatchers.IO) {
                runCatching { lookup(context, game) }
                    .onFailure { Timber.w(it, "Unable to look up cover of ${game.fileName}") }
                    .getOrNull()
            }

        resolvedCovers[game.fileUri] = Resolved(currentGeneration, cover)
        return cover
    }

    suspend fun save(
        context: Context,
        game: Game,
        source: Uri,
    ): SaveResult =
        withContext(Dispatchers.IO) {
            if (!supportsCustomCover(game)) return@withContext SaveResult.Failed

            val png =
                runCatching { encodeCover(context, source) }
                    .onFailure { Timber.w(it, "Unable to read the picked image") }
                    .getOrNull() ?: return@withContext SaveResult.Failed

            val storedNextToRom =
                runCatching { writeNextToRom(context, game, png) }
                    .onFailure { Timber.w(it, "Unable to store the cover next to the rom") }
                    .getOrDefault(false)

            val result =
                when {
                    storedNextToRom -> SaveResult.NextToRom
                    writeToAppStorage(context, game, png) -> SaveResult.AppStorage
                    else -> SaveResult.Failed
                }

            invalidate(game)
            result
        }

    /**
     * Removes the cover of [game] from both locations. Returns false when a cover file is there
     * but could not be deleted, which happens on read only rom folders.
     */
    suspend fun delete(
        context: Context,
        game: Game,
    ): Boolean =
        withContext(Dispatchers.IO) {
            val sibling = runCatching { siblingCover(context, game) }.getOrNull()

            val siblingDeleted =
                sibling == null ||
                    runCatching { deleteCover(context, sibling) }
                        .onFailure { Timber.w(it, "Unable to delete the cover next to the rom") }
                        .getOrDefault(false)

            runCatching { appStorageCoverFile(context, game).delete() }

            invalidate(game)
            siblingDeleted
        }

    /**
     * Forgets what is known about covers on disk. Covers dropped in with a file manager while the
     * app was in the background show up after this.
     */
    fun invalidateAll() {
        folderListings.clear()
        revisionState.value = generation.incrementAndGet()
    }

    private fun invalidate(game: Game) {
        resolvedCovers.remove(game.fileUri)
        invalidateAll()
    }

    /** Web and catalog games are not files on disk, so there is nothing to put a cover next to. */
    private fun supportsCustomCover(game: Game): Boolean {
        if (game.webGameSlug != null || game.isCatalogGame) return false
        return Uri.parse(game.fileUri).scheme in setOf(
            ContentResolver.SCHEME_FILE,
            ContentResolver.SCHEME_CONTENT,
        )
    }

    private fun lookup(
        context: Context,
        game: Game,
    ): Uri? {
        siblingCover(context, game)?.let { return it }
        return appStorageCoverFile(context, game)
            .takeIf { it.exists() }
            ?.let { Uri.fromFile(it) }
    }

    private fun siblingCover(
        context: Context,
        game: Game,
    ): Uri? {
        val romUri = Uri.parse(game.fileUri)
        return when (romUri.scheme) {
            ContentResolver.SCHEME_FILE ->
                localSiblingFile(romUri)
                    ?.takeIf { it.exists() }
                    ?.let { Uri.fromFile(it) }

            ContentResolver.SCHEME_CONTENT -> documentSibling(context, romUri)?.coverUri
            else -> null
        }
    }

    private fun localSiblingFile(romUri: Uri): File? {
        val romFile = romUri.path?.let { File(it) } ?: return null
        val parent = romFile.parentFile ?: return null
        return File(parent, coverNameFor(romFile.name))
    }

    private fun documentSibling(
        context: Context,
        romUri: Uri,
    ): DocumentSibling? {
        val documentId = runCatching { DocumentsContract.getDocumentId(romUri) }.getOrNull() ?: return null

        // Document ids of the providers we support are path like ("primary:Roms/Tetris.zip").
        // Anything else gives us no way to address the containing folder, so we give up here and
        // let the caller fall back to app storage.
        if (!documentId.contains('/')) return null
        val parentDocumentId = documentId.substringBeforeLast('/')

        val listing = folderListing(context, romUri, parentDocumentId) ?: return null

        val romName = listing.namesByDocumentId[documentId] ?: documentId.substringAfterLast('/')
        val coverName = coverNameFor(romName)

        return DocumentSibling(
            parentUri = DocumentsContract.buildDocumentUriUsingTree(romUri, parentDocumentId),
            coverName = coverName,
            coverUri =
                listing.documentIdsByName[coverName.lowercase()]
                    ?.let { DocumentsContract.buildDocumentUriUsingTree(romUri, it) },
        )
    }

    /**
     * Names of the files in a rom folder. This is cached because a rom folder holds many games and
     * we would otherwise query the same folder once per displayed cover.
     */
    private fun folderListing(
        context: Context,
        treeUri: Uri,
        parentDocumentId: String,
    ): FolderListing? {
        val cacheKey = "${treeUri.authority}:$parentDocumentId"
        folderListings[cacheKey]?.let { return it }

        val childrenUri =
            runCatching {
                DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocumentId)
            }.getOrNull() ?: return null

        val projection =
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            )

        val namesByDocumentId = mutableMapOf<String, String>()
        val documentIdsByName = mutableMapOf<String, String>()

        val listed =
            runCatching {
                context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getString(0) ?: continue
                        val name = cursor.getString(1) ?: continue
                        namesByDocumentId[id] = name
                        documentIdsByName[name.lowercase()] = id
                    }
                    true
                }
            }.onFailure {
                Timber.w(it, "Unable to list the folder of $parentDocumentId")
            }.getOrNull()

        if (listed != true) return null

        return FolderListing(namesByDocumentId, documentIdsByName)
            .also { folderListings[cacheKey] = it }
    }

    /** "Tetris (R).zip" and "Tetris (R).nes" are both covered by "Tetris (R).png". */
    private fun coverNameFor(fileName: String): String {
        return "${fileName.substringBeforeLast('.', fileName)}.$COVER_EXTENSION"
    }

    private fun writeNextToRom(
        context: Context,
        game: Game,
        png: ByteArray,
    ): Boolean {
        val romUri = Uri.parse(game.fileUri)
        return when (romUri.scheme) {
            ContentResolver.SCHEME_FILE -> {
                val target = localSiblingFile(romUri) ?: return false
                target.writeBytes(png)
                true
            }

            ContentResolver.SCHEME_CONTENT -> writeDocumentNextToRom(context, romUri, png)
            else -> false
        }
    }

    private fun writeDocumentNextToRom(
        context: Context,
        romUri: Uri,
        png: ByteArray,
    ): Boolean {
        val sibling = documentSibling(context, romUri) ?: return false
        val resolver = context.contentResolver

        // Left in place, createDocument would happily add a "Tetris (1).png" next to the old file.
        sibling.coverUri?.let { DocumentsContract.deleteDocument(resolver, it) }

        val created =
            DocumentsContract.createDocument(
                resolver,
                sibling.parentUri,
                COVER_MIME_TYPE,
                sibling.coverName,
            ) ?: return false

        // Some providers rename what they create. A cover we cannot find again is worse than no
        // cover at all, so drop it and let the caller fall back to app storage.
        val written =
            documentDisplayName(context, created) == sibling.coverName &&
                resolver.openOutputStream(created, "wt")?.use { output ->
                    output.write(png)
                    true
                } == true

        if (!written) {
            runCatching { DocumentsContract.deleteDocument(resolver, created) }
        }

        return written
    }

    private fun documentDisplayName(
        context: Context,
        documentUri: Uri,
    ): String? {
        val projection = arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
        return context.contentResolver.query(documentUri, projection, null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        }
    }

    private fun writeToAppStorage(
        context: Context,
        game: Game,
        png: ByteArray,
    ): Boolean {
        return runCatching {
            val target = appStorageCoverFile(context, game)
            target.parentFile?.mkdirs()
            target.writeBytes(png)
            true
        }.onFailure {
            Timber.e(it, "Unable to store the cover in app storage")
        }.getOrDefault(false)
    }

    private fun appStorageCoverFile(
        context: Context,
        game: Game,
    ): File {
        val folder = File(context.filesDir, APP_COVERS_FOLDER)
        return File(folder, "${hashOf(game.fileUri)}.$COVER_EXTENSION")
    }

    private fun hashOf(value: String): String {
        return MessageDigest.getInstance("SHA-1")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun deleteCover(
        context: Context,
        coverUri: Uri,
    ): Boolean {
        return when (coverUri.scheme) {
            ContentResolver.SCHEME_FILE -> coverUri.path?.let { File(it).delete() } ?: false
            ContentResolver.SCHEME_CONTENT ->
                DocumentsContract.deleteDocument(context.contentResolver, coverUri)

            else -> false
        }
    }

    /**
     * Covers are always stored as png, whatever the user picked. They are displayed in small
     * squares, so a long side of [MAX_COVER_SIZE] is plenty and keeps rom folders tidy.
     */
    private fun encodeCover(
        context: Context,
        source: Uri,
    ): ByteArray? {
        val bitmap = decodeScaled(context, source) ?: return null
        return ByteArrayOutputStream().use { output ->
            val compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            bitmap.recycle()
            if (compressed) output.toByteArray() else null
        }
    }

    private fun decodeScaled(
        context: Context,
        source: Uri,
    ): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder applies the exif rotation, which matters for gallery pictures. We are
            // about to re-encode as png and would otherwise bake in a sideways cover.
            val imageSource = ImageDecoder.createSource(context.contentResolver, source)
            return ImageDecoder.decodeBitmap(imageSource) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val scale = max(info.size.width, info.size.height).toFloat() / MAX_COVER_SIZE
                if (scale > 1f) {
                    decoder.setTargetSize(
                        (info.size.width / scale).roundToInt().coerceAtLeast(1),
                        (info.size.height / scale).roundToInt().coerceAtLeast(1),
                    )
                }
            }
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }

        val options =
            BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(max(bounds.outWidth, bounds.outHeight))
            }

        return context.contentResolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }

    private fun sampleSizeFor(longestSide: Int): Int {
        var sampleSize = 1
        while (longestSide / (sampleSize * 2) >= MAX_COVER_SIZE) {
            sampleSize *= 2
        }
        return sampleSize
    }
}

/** Cover the user set for [game], or null when there is none. */
@Composable
fun rememberCustomCover(game: Game): Uri? {
    val context = LocalContext.current
    val revision by CustomCovers.revision.collectAsState()

    val cover by
        produceState(CustomCovers.cached(game), game, revision) {
            value = CustomCovers.resolve(context, game)
        }

    return cover
}

/** Cover of [game]: the one set by the user when there is one, the scraped one otherwise. */
@Composable
fun rememberGameCoverRequest(game: Game): ImageRequest {
    val context = LocalContext.current
    val revision by CustomCovers.revision.collectAsState()
    val customCover = rememberCustomCover(game)

    return remember(game, customCover, revision) {
        ImageRequest.Builder(context)
            .apply {
                if (customCover != null) {
                    data(customCover)
                    // Covers are replaced in place, so their uri alone is not a stable cache key.
                    memoryCacheKey("$customCover#$revision")
                    diskCachePolicy(CachePolicy.DISABLED)
                } else {
                    data(game.coverFrontUrl)
                }
            }
            .build()
    }
}
