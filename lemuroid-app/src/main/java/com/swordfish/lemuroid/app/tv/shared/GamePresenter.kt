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

package com.swordfish.lemuroid.app.tv.shared

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameContextMenuListener
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.utils.games.GameUtils
import com.swordfish.lemuroid.lib.library.db.entity.Game

class GamePresenter(private val cardSize: Int, private val gameInteractor: GameInteractor) : Presenter() {

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder?, item: Any?) {
        if (item == null || viewHolder !is ViewHolder) return
        val game = item as Game
        viewHolder.mCardView.titleText = game.title
        viewHolder.mCardView.contentText = GameUtils.getGameSubtitle(viewHolder.mCardView.context, game)
        viewHolder.mCardView.setMainImageDimensions(cardSize, cardSize)
        viewHolder.updateCardViewImage(game)
        viewHolder.view.setOnCreateContextMenuListener(GameContextMenuListener(gameInteractor, game))
    }

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        (cardView.findViewById<View>(R.id.content_text) as TextView).setTextColor(Color.LTGRAY)
        return ViewHolder(cardView)
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder?) {
        val viewHolder = viewHolder as ViewHolder
        viewHolder.mCardView.mainImage = null
        CoverLoader.cancelRequest(viewHolder.mCardView.mainImageView)
        viewHolder.view.setOnCreateContextMenuListener(null)
    }

    class ViewHolder(view: ImageCardView) : Presenter.ViewHolder(view) {
        val mCardView: ImageCardView = view

        fun updateCardViewImage(game: Game) {
            CoverLoader.loadCover(game, mCardView.mainImageView)
        }
    }
}
