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

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator

class NavigationPanelAnimator : BaseViewAnimator() {
    private val decelerateInterpolator = DecelerateInterpolator()
    private val accelerateInterpolator = AccelerateDecelerateInterpolator()

    private var animationDuration = TAB_LAYOUT_ANIMATION_DURATION

    fun show(
        view: View,
        forced: Boolean = false,
        startAction: ((view: View) -> Unit)? = null,
        endAction: ((view: View) -> Unit)? = null
    ) {
        animate(
            view,
            forced,
            animationDuration,
            TAB_LAYOUT_TARGET_ANGLE,
            TAB_LAYOUT_TARGET_SCALE,
            TAB_LAYOUT_NORMAL_ALPHA,
            TAB_LAYOUT_NORMAL_POSITION,
            TAB_LAYOUT_NORMAL_POSITION,
            animationInterpolator = decelerateInterpolator,
            startAction = startAction,
            endAction = endAction
        )
    }

    fun hide(
        view: View,
        forced: Boolean = false,
        hideDirection: Int,
        viewTranslationY: Int = view.height,
        startAction: ((view: View) -> Unit)? = null,
        endAction: ((view: View) -> Unit)? = null
    ) {
        val translationY = when (hideDirection) {
            HIDE_TO_TOP -> -viewTranslationY
            HIDE_TO_BOTTOM -> viewTranslationY
            else -> 0
        }.toFloat()

        animate(
            view,
            forced,
            animationDuration,
            TAB_LAYOUT_TARGET_ANGLE,
            TAB_LAYOUT_TARGET_SCALE,
            TAB_LAYOUT_HIDDEN_ALPHA,
            TAB_LAYOUT_NORMAL_POSITION,
            translationY,
            animationInterpolator = accelerateInterpolator,
            startAction = startAction,
            endAction = endAction
        )
    }

    companion object {
        const val HIDE_TO_TOP = 1
        const val HIDE_TO_BOTTOM = 2

        private const val TAB_LAYOUT_NORMAL_POSITION = 0.0f
        private const val TAB_LAYOUT_HIDDEN_ALPHA = 1.0f
        private const val TAB_LAYOUT_NORMAL_ALPHA = 1.0f
        private const val TAB_LAYOUT_TARGET_ANGLE = 0.0f
        private const val TAB_LAYOUT_TARGET_SCALE = 1.0f
        private const val TAB_LAYOUT_ANIMATION_DURATION = 170L
    }
}
