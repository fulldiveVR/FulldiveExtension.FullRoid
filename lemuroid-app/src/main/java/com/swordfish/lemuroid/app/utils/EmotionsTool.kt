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
import android.text.Spannable
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.util.ArrayMap
import com.swordfish.lemuroid.R
import java.util.regex.Pattern

object EmotionsTool {
    private val checkMarkMap = ArrayMap<Pattern, Int>()
    private const val CHECK_MARK_TAG = ":check_mark:"
    private val CHECK_MARK_RESOURCES = mapOf(CHECK_MARK_TAG to R.drawable.ic_check_mark)

    init {
        CHECK_MARK_RESOURCES[CHECK_MARK_TAG]?.let {
            addPattern(checkMarkMap, CHECK_MARK_TAG, it)
        }
    }

    fun processCheckMark(
        context: Context,
        spannable: Spannable,
        alignment: Int = DynamicDrawableSpan.ALIGN_BASELINE
    ) = process(checkMarkMap, context, spannable, alignment)

    fun process(
        emoticons: ArrayMap<Pattern, Int>,
        context: Context,
        spannable: Spannable,
        alignment: Int
    ): Boolean {
        var hasChanges = false
        for ((key, value) in emoticons) {
            val matcher = key.matcher(spannable)
            while (matcher.find()) {
                var changed = true
                for (span in spannable.getSpans(
                    matcher.start(),
                    matcher.end(),
                    ImageSpan::class.java
                )) {
                    if (spannable.getSpanStart(span) >= matcher.start() &&
                        spannable.getSpanEnd(span) <= matcher.end()
                    )
                        spannable.removeSpan(span)
                    else {
                        changed = false
                        break
                    }
                }
                if (changed) {
                    hasChanges = true
                    spannable.setSpan(
                        CustomImageSpan(context, value, alignment),
                        matcher.start(), matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        return hasChanges
    }

    private fun addPattern(map: ArrayMap<Pattern, Int>, tag: String, resource: Int) {
        map[Pattern.compile(Pattern.quote(tag))] = resource
    }
}
