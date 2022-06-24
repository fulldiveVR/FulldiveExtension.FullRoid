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

package com.swordfish.lemuroid.app.mobile.feature.home

import android.view.View
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.swordfish.lemuroid.R

@EpoxyModelClass(layout = R.layout.layout_home_section)
abstract class EpoxyHomeSection : EpoxyModelWithHolder<EpoxyHomeSection.Holder>() {

    @EpoxyAttribute
    var title: Int? = null

    override fun bind(holder: Holder) {
        title?.let {
            holder.titleView.setText(it)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var titleView: TextView

        override fun bindView(itemView: View) {
            titleView = itemView.findViewById(R.id.text)
        }
    }
}
