package com.swordfish.lemuroid.lib.citra

import android.content.Context
import android.net.Uri
import com.swordfish.lemuroid.lib.storage.DirectoriesManager
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages the user-provided 3DS system file.
 *
 * The file is never bundled, generated or fetched by the app: the only way it can arrive on
 * the device is an import the user starts themselves, from a location they pick in the system
 * document picker. It must come from a console the user owns.
 */
class Citra3DSSystemFilesManager(private val directoriesManager: DirectoriesManager) {

    /**
     * Destination path. The file name is dictated by the emulation core, which looks for
     * exactly this name — it is a technical requirement, not a product-facing label.
     */
    fun systemFile(): File =
        File(directoriesManager.getSavesDirectory(), "Fullroid/sysdata/keys.txt")

    fun isPresent(): Boolean = systemFile().let { it.exists() && it.length() > 0 }

    suspend fun importFromUri(
        context: Context,
        uri: Uri,
    ) {
        withContext(Dispatchers.IO) {
            val dest = systemFile()
            dest.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: throw IOException("Cannot open selected file")
            validateSystemFile(dest)
        }
    }

    suspend fun delete() =
        withContext(Dispatchers.IO) {
            systemFile().delete()
        }

    private fun validateSystemFile(file: File) {
        val header = ByteArray(10_240)
        val len = file.inputStream().use { it.read(header) }
        val text = String(header, 0, maxOf(len, 0), Charsets.UTF_8)
        if (!text.contains(":AES")) {
            file.delete()
            throw IOException("This does not appear to be a valid 3DS system file")
        }
    }
}
