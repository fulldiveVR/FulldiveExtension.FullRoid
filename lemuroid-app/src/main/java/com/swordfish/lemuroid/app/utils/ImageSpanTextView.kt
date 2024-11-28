package com.swordfish.lemuroid.app.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.R

@Composable
fun ImageSpanTextView(
    formatString: String,
    placeholder: String = ":check_mark:",
    iconResId: Int = R.drawable.ic_check_mark
) {
    val parts = formatString.split(placeholder)

    val disclaimerText = buildAnnotatedString {
        parts.forEachIndexed { index, part ->
            append(part)
            if (index != parts.lastIndex) {
                appendInlineContent("checkMark$index", placeholder)
            }
        }
    }

    val inlineContent = parts.indices.associate { index ->
        "checkMark$index" to InlineTextContent(
            placeholder = Placeholder(
                width = 12.sp,
                height = 12.sp,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            ),
            children = {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        )
    }

    Text(
        text = disclaimerText,
        inlineContent = inlineContent,
        fontSize = dimensionResource(R.dimen.textsize_small).value.sp,
        modifier = Modifier.padding(top = dimensionResource(R.dimen.size_12dp))
    )
}
