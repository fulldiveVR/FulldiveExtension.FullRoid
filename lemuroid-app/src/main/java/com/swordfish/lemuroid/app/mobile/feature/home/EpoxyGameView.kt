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

package com.swordfish.lemuroid.app.mobile.feature.home

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.GameContextMenuListener
import com.swordfish.lemuroid.app.shared.GameInteractor
import com.swordfish.lemuroid.app.shared.covers.CoverLoader
import com.swordfish.lemuroid.app.utils.games.GameUtils
import com.swordfish.lemuroid.lib.library.db.entity.Game

@EpoxyModelClass
abstract class EpoxyGameView : EpoxyModelWithHolder<EpoxyGameView.Holder>() {

    override fun getDefaultLayout(): Int {
        return R.layout.layout_game_recent
    }

    @EpoxyAttribute
    lateinit var game: Game

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var gameInteractor: GameInteractor

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var coverLoader: CoverLoader

    override fun bind(holder: Holder) {
        holder.titleView?.text = game.title
        holder.subtitleView?.let { it.text = GameUtils.getGameSubtitle(it.context, game) }

        coverLoader.loadCover(game, holder.coverView)

        holder.itemView?.setOnClickListener { gameInteractor.onGamePlay(game) }
        holder.itemView?.setOnCreateContextMenuListener(
            GameContextMenuListener(gameInteractor, game)
        )
        holder.itemView?.setOnClickListener { gameInteractor.onGamePlay(game) }
    }

    override fun unbind(holder: Holder) {
        holder.itemView?.setOnClickListener(null)
        holder.coverView?.apply {
            coverLoader.cancelRequest(this)
        }
        holder.itemView?.setOnCreateContextMenuListener(null)
    }

    class Holder : EpoxyHolder() {
        var itemView: View? = null
        var titleView: TextView? = null
        var subtitleView: TextView? = null
        var coverView: ImageView? = null

        override fun bindView(itemView: View) {
            this.itemView = itemView
            this.titleView = itemView.findViewById(R.id.text)
            this.subtitleView = itemView.findViewById(R.id.subtext)
            this.coverView = itemView.findViewById(R.id.image)
        }
    }
}
