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
