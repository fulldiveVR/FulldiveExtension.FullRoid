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

package com.swordfish.lemuroid.app.appextension.discord

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShareDiscordTextGenerator @Inject constructor(
    private val discordManager: DiscordManager
) {

    fun shareGame(content: String, imageUrl: String, onSuccess: () -> Unit, onError: () -> Unit) {
        GlobalScope.launch {
            try {
                val shareData = if (imageUrl.isNotEmpty()) {
                    ShareGameData(content, listOf(ShareEmbeds(ShareImage(imageUrl))))
                } else {
                    ShareGameData(content)
                }

                discordManager.sendMessage(shareData)
                withContext(Dispatchers.Main) {
                    onSuccess.invoke()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError.invoke()
                }
            }
        }
    }
}
