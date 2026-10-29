package com.swordfish.lemuroid.app.shared.settings

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.swordfish.lemuroid.lib.library.db.entity.Game
import com.swordfish.lemuroid.lib.preferences.SharedPreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The ROMs folder picked through the Storage Access Framework is saved as a preference, and that
 * preference survives a reinstall or a backup restore while the permission to read it does not.
 * Games scanned from it then stay in the library but can no longer be read.
 */
object RomsFolderAccess {
    fun savedFolder(context: Context): Uri? {
        val key = context.getString(com.swordfish.lemuroid.lib.R.string.pref_key_extenral_folder)
        return SharedPreferencesHelper.getLegacySharedPreferences(context)
            .getString(key, null)
            ?.let { Uri.parse(it) }
            ?.takeIf { it.scheme == ContentResolver.SCHEME_CONTENT }
    }

    // A missing persisted grant alone is not enough: it would block perfectly playable games if a
    // device ever reported the grant under a differently spelled uri. The folder must also be
    // unreadable right now.
    fun isLost(context: Context): Boolean {
        val folder = savedFolder(context) ?: return false
        val hasGrant =
            context.contentResolver.persistedUriPermissions
                .any { it.uri == folder && it.isReadPermission }
        return !hasGrant && !canRead(context, folder)
    }

    private fun canRead(
        context: Context,
        folder: Uri,
    ): Boolean {
        return runCatching {
            val children =
                DocumentsContract.buildChildDocumentsUriUsingTree(
                    folder,
                    DocumentsContract.getTreeDocumentId(folder),
                )
            val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            context.contentResolver.query(children, projection, null, null, null)?.use { true } ?: false
        }.getOrDefault(false)
    }

    // isLost asks the storage provider, which is too slow for the main thread.
    suspend fun isLostInBackground(context: Context): Boolean {
        return withContext(Dispatchers.IO) { isLost(context) }
    }

    fun isFromFolder(game: Game): Boolean {
        return !game.isCatalogGame && game.fileUri.startsWith("${ContentResolver.SCHEME_CONTENT}://")
    }

    fun isGameUnavailable(
        context: Context,
        game: Game,
    ): Boolean {
        return isFromFolder(game) && isLost(context)
    }

    // "primary:Documents/Roms" -> "Documents/Roms", which is what the user saw in the picker.
    fun displayName(folder: Uri): String {
        val documentId = runCatching { DocumentsContract.getTreeDocumentId(folder) }.getOrNull()
            ?: return folder.toString()
        return documentId.substringAfter(':').ifEmpty { documentId }
    }

    // Lets the picker open right at the folder the user has to select again.
    fun initialPickerUri(folder: Uri): Uri? {
        return runCatching {
            DocumentsContract.buildDocumentUri(folder.authority, DocumentsContract.getTreeDocumentId(folder))
        }.getOrNull()
    }
}
