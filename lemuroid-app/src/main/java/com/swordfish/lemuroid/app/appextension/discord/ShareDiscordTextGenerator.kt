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

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.or
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShareDiscordTextGenerator @Inject constructor(
    private val discordManager: DiscordManager
) {

    fun shareGame(activityContext: Context, game: Game) {
        showShareDialog(activityContext, game) { content, imageUrl ->
            GlobalScope.launch {
                try {
                    val shareData = if (imageUrl.isNotEmpty()) {
                        ShareGameData(content, listOf(ShareEmbeds(ShareImage(imageUrl))))
                    } else {
                        ShareGameData(content)
                    }

                    discordManager.sendMessage(shareData)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activityContext, "Feedback is successfully shared!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activityContext, "Error while share  feedback!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showShareDialog(context: Context, game: Game, onPositiveClicked: (String, String) -> Unit) {
        val view = LayoutInflater.from(context).inflate(R.layout.share_discord_dialog_layout, null)
        val nameEditText = view.findViewById<EditText>(R.id.nameEditText)
        val feedbackEditText = view.findViewById<EditText>(R.id.feedbackEditText)

        val shareButton = view.findViewById<TextView>(R.id.shareButton)

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setTitle(R.string.game_context_menu_share)
            .create()

        shareButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val feedback = feedbackEditText.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(context, "Enter your name!", Toast.LENGTH_SHORT).show()
            } else if (feedback.isEmpty()) {
                Toast.makeText(context, "Enter your feedback!", Toast.LENGTH_SHORT).show()
            } else {
                val shareTextPart1 = String.format(
                    context.getString(R.string.share_discord_text_title_part_1),
                    name,
                    game.title,
                )

                val shareText = "$shareTextPart1 ${feedbackEditText.text}"
                onPositiveClicked.invoke(
                    shareText,
                    game.coverFrontUrl?.replace(" ", "%20").or { "" }
                )
                dialog.dismiss()
            }
        }
        dialog.show()
    }
}
