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

package com.swordfish.lemuroid.common.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextPaint

class TextDrawable(private val text: String, private val color: Int) : Drawable() {
    companion object {
        private const val DEFAULT_COLOR = Color.WHITE
    }

    private val mTextBounds = Rect()
    private val mPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    init {
        mPaint.color = DEFAULT_COLOR
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.typeface = Typeface.MONOSPACE
        mPaint.getTextBounds(text, 0, text.length, mTextBounds)
    }

    override fun draw(canvas: Canvas) {
        mPaint.color = color
        canvas.drawRect(bounds, mPaint)

        mPaint.color = DEFAULT_COLOR
        mPaint.textSize = bounds.height() * 0.3f
        val xPos = bounds.width().toFloat() / 2
        val yPos = (bounds.height().toFloat() / 2 - (mPaint.descent() + mPaint.ascent()) / 2)
        canvas.drawText(text, xPos, yPos, mPaint)
    }

    override fun getOpacity(): Int = mPaint.alpha

    override fun getIntrinsicWidth(): Int = -1

    override fun getIntrinsicHeight(): Int = -1

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(p0: ColorFilter?) {
        mPaint.colorFilter = p0
        invalidateSelf()
    }
}
