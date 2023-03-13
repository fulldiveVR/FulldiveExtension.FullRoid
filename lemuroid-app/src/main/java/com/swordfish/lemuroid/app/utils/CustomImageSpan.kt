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

package com.swordfish.lemuroid.app.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import java.lang.ref.WeakReference

class CustomImageSpan(context: Context, value: Int, alignment: Int) :
    ImageSpan(context, value, alignment) {

    private var initialDescent = 0
    private var extraSpace = 0
    private var drawableRef: WeakReference<Drawable>? = null

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        if (mVerticalAlignment != ALIGN_CENTER) {
            return super.getSize(paint, text, start, end, fm)
        }
        cachedDrawable?.let { drawable ->
            val rect = drawable.bounds

            if (fm != null) {
                // Centers the text with the ImageSpan
                if (rect.bottom - (fm.descent - fm.ascent) >= 0) {
                    // Stores the initial descent and computes the margin available
                    initialDescent = fm.descent
                    extraSpace = rect.bottom - (fm.descent - fm.ascent)
                }

                fm.descent = extraSpace / 2 + initialDescent
                fm.bottom = fm.descent

                fm.ascent = fm.descent - rect.bottom
                fm.top = fm.ascent
            }
            return rect.right
        }
        return 0
    }

    private val cachedDrawable: Drawable?
        get() {
            val wr = drawableRef
            var d: Drawable? = null

            if (wr != null)
                d = wr.get()

            if (d == null) {
                d = drawable
                drawableRef = WeakReference<Drawable>(d)
            }

            return d
        }

    companion object {
        const val ALIGN_CENTER = 2
    }
}