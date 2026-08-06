/*
 * Brand.kt
 *
 * Copyright (C) 2026 FullDive
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.mobile.shared.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * Fixed brand identity for the modern UI: a blue→purple→pink gradient plus a neutral
 * near-black dark palette. Used explicitly by the redesigned header, bottom nav,
 * filter chips and accents so the look is consistent across devices (dynamic color
 * is disabled in [AppTheme]).
 */
object Brand {
    val Start = Color(0xFF6D8BFF) // blue
    val Mid = Color(0xFF9B6BFF) // purple
    val End = Color(0xFFE86BC6) // pink

    val Accent = Mid

    // Neutral dark surfaces (reference is near-black, not the legacy green-gray).
    val Background = Color(0xFF0B0B10)
    val Surface = Color(0xFF16161E)
    val SurfaceElevated = Color(0xFF20202B)
    val OnSurface = Color(0xFFECECF2)
    val OnSurfaceVariant = Color(0xFF9A9AAA)
    val Outline = Color(0xFF2E2E3A)

    val gradient: Brush
        get() = Brush.linearGradient(listOf(Start, Mid, End))

    /** Vertical variant for round/tall surfaces (e.g. the center nav button). */
    val gradientVertical: Brush
        get() = Brush.linearGradient(listOf(Start, End))
}

/** Text painted with the brand gradient — used for the wordmark logo. */
@Composable
fun brandTextStyle(base: TextStyle): TextStyle = base.copy(brush = Brand.gradient, fontWeight = FontWeight.Black)

/** Convenience box with the brand gradient background. */
@Composable
fun BrandGradientBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) { content() }
}
