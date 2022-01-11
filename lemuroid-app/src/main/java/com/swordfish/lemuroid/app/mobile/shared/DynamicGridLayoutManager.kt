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

package com.swordfish.lemuroid.app.mobile.shared

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.roundToInt

class DynamicGridLayoutManager(
    context: Context,
    private val scaling: Int = 2
) : GridLayoutManager(context, 1, VERTICAL, false) {

    private val density: Float = context.resources.displayMetrics.density

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        updateSpanCount(width)
        super.onLayoutChildren(recycler, state)
    }

    private fun columnsForWidth(widthPx: Int): Int {
        val widthDp = dpFromPx(widthPx.toFloat())
        val columns = when {
            widthDp >= 840 -> 12
            widthDp >= 600 -> 8
            else -> 4
        }
        return max(columns / scaling, 1)
    }

    private fun dpFromPx(px: Float): Int {
        return (px / density).roundToInt()
    }

    private fun updateSpanCount(width: Int) {
        spanCount = columnsForWidth(width)
    }
}
