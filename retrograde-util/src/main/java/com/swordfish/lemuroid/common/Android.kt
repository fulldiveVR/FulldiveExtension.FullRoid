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

package com.swordfish.lemuroid.common

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast

fun Bundle?.dump(): String {
    if (this == null) return "null"

    val builder = StringBuilder("Extras:\n")
    keySet()
        .toSet()
        .forEach { key ->
            builder.append(key).append(": ").append(get(key)).append("\n")
        }
    return builder.toString()
}

fun Activity.displayToast(string: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, string, length).show()
}

fun Activity.displayToast(stringId: Int, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, stringId, length).show()
}

fun Context.animationDuration(): Int {
    return resources.getInteger(android.R.integer.config_mediumAnimTime)
}

fun Context.shortAnimationDuration(): Int {
    return resources.getInteger(android.R.integer.config_shortAnimTime)
}

fun Context.longAnimationDuration(): Int {
    return resources.getInteger(android.R.integer.config_longAnimTime)
}
