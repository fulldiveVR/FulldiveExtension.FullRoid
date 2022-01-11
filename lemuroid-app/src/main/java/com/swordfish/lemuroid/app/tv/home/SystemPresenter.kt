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

package com.swordfish.lemuroid.app.tv.home

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo

class SystemPresenter(private val cardSize: Int, private val cardPadding: Int) : Presenter() {

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder?, item: Any) {
        val systemInfo = item as MetaSystemInfo
        val context = (viewHolder as ViewHolder).view.context

        viewHolder.mCardView.titleText = context.resources.getString(systemInfo.metaSystem.titleResId)
        viewHolder.mCardView.contentText = context.getString(R.string.system_grid_details, systemInfo.count.toString())
        viewHolder.mCardView.setMainImageDimensions(cardSize, cardSize)
        viewHolder.mCardView.mainImageView.setImageResource(systemInfo.metaSystem.imageResId)
        viewHolder.mCardView.mainImageView.setPadding(cardPadding, cardPadding, cardPadding, cardPadding)
        viewHolder.mCardView.setMainImageScaleType(ImageView.ScaleType.FIT_CENTER)
    }

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        val cardView = ImageCardView(parent.context)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        (cardView.findViewById<View>(R.id.content_text) as TextView).setTextColor(Color.LTGRAY)
        return ViewHolder(cardView)
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder?) {}

    class ViewHolder(view: ImageCardView) : Presenter.ViewHolder(view) {
        val mCardView: ImageCardView = view
    }
}
