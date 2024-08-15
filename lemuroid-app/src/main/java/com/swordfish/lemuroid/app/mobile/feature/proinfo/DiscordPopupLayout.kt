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

package com.swordfish.lemuroid.app.mobile.feature.proinfo

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.swordfish.lemuroid.R

class DiscordPopupLayout : FrameLayout {

    var onClickListener: (() -> Unit)? = null
    var onCloseClickListener: (() -> Unit)? = null

    private val animator = NavigationPanelAnimator()

    constructor(context: Context) : super(context) {
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initLayout()
    }

    private fun initLayout() {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout. layout_discord_popup, this, true)

        val closeImageView = findViewById<ImageView>(R.id.closeImageView)
        closeImageView.setOnClickListener { hideSnackbar() }
        val containerLayout = findViewById<ConstraintLayout>(R.id.containerLayout)
        containerLayout.setOnClickListener { onClickListener?.invoke() }
    }

    fun showSnackbar() {
        val cardView = findViewById<CardView>(R.id.cardView)
        cardView.isVisible = true
        animator.show(cardView)
    }

    private fun hideSnackbar() {
        onCloseClickListener?.invoke()
        val cardView = findViewById<CardView>(R.id.cardView)
        animator.hide(
            cardView,
            endAction = {
                cardView.isVisible = false
            },
            hideDirection = NavigationPanelAnimator.HIDE_TO_BOTTOM
        )
    }
}
