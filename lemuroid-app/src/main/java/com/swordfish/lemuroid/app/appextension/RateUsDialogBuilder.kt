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

package com.swordfish.lemuroid.app.appextension

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.core.content.ContextCompat
import com.swordfish.lemuroid.R
import kotlin.math.roundToInt

object RateUsDialogBuilder {

    fun show(activity: Activity, onPositiveClicked: (Int) -> Unit) {
        val view = LayoutInflater.from(activity).inflate(R.layout.rate_us_dialog_layout, null)
        val ratingBar = view.findViewById<AppCompatRatingBar>(R.id.ratingBar)
        var ratingValue = 5
        ratingBar.rating = 5F

        ratingBar.setOnRatingBarChangeListener { _, value, fromUser ->
            if (fromUser) {
                ratingValue = value.roundToInt()
            }
        }

        val dialog = AlertDialog.Builder(activity)
            .setView(view)
            .setTitle(R.string.rate_us_title)
            .setPositiveButton(R.string.rate_submit) { _, _ ->
                onPositiveClicked.invoke(ratingValue)
            }
            .setNegativeButton(R.string.rate_cancel) { _, _ -> }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(ContextCompat.getColor(activity, R.color.colorWhite))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(ContextCompat.getColor(activity, R.color.textColorSecondary))
        }
        dialog.show()
    }
}
