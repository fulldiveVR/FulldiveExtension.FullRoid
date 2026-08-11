/*
 * Copyright (c) 2022 FullDive
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
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.appextension.roomcord

import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShareRoomcordTextGenerator @Inject constructor(
    private val roomcordManager: RoomcordManager,
    private val roomcordImageUploader: RoomcordImageUploader,
    private val rateLimiter: ShareRateLimiter
) {

    fun shareGame(
        game: Game,
        content: String,
        screenshotPath: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        // Authoritative gate. The share screens disable their button ahead of time, but that state
        // can be stale, so the limit is enforced here too — before uploading anything, otherwise a
        // rejected share still costs a screenshot upload.
        val decision = rateLimiter.check(content)
        if (decision !is ShareRateLimiter.Decision.Allowed) {
            onError(rateLimiter.describe(decision))
            return
        }

        GlobalScope.launch {
            try {
                val urls = buildAttachmentUrls(game, screenshotPath)
                val request = if (urls.isNotEmpty()) {
                    RoomcordMessageRequest(
                        content = content,
                        type = "image",
                        attachmentUrl = urls.first(),
                        attachmentUrls = urls,
                    )
                } else {
                    RoomcordMessageRequest(content = content, type = "text")
                }
                roomcordManager.sendMessage(request)
                // Only a message that actually reached the room consumes quota, so a failed send
                // does not lock the user out for five minutes.
                rateLimiter.recordSent(content)
                withContext(Dispatchers.Main) { onSuccess.invoke() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError.invoke(e.message ?: "Unknown error") }
            }
        }
    }

    private suspend fun buildAttachmentUrls(game: Game, screenshotPath: String?): List<String> {
        val (screenshotUrl, coverUrl) = roomcordImageUploader.uploadForShare(game, screenshotPath)
        return when {
            screenshotUrl != null && coverUrl != null -> listOf(coverUrl, screenshotUrl)
            screenshotUrl != null -> listOf(screenshotUrl)
            coverUrl != null -> listOf(coverUrl)
            else -> listOfNotNull(roomcordImageUploader.uploadGameImage(game))
        }
    }
}
