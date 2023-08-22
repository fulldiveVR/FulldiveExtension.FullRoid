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
import android.widget.Button
import android.widget.TextView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.swordfish.lemuroid.R

@EpoxyModelClass(layout = R.layout.layout_home_notification)
abstract class EpoxyHomeNotification : EpoxyModelWithHolder<EpoxyHomeNotification.Holder>() {

    @EpoxyAttribute
    var title: Int? = null

    @EpoxyAttribute
    var message: Int? = null

    @EpoxyAttribute
    var action: Int? = null

    @EpoxyAttribute
    var actionEnabled: Boolean? = null

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var onClick: (() -> Unit)? = null

    override fun bind(holder: Holder) {
        title?.let { holder.titleView?.setText(it) }
        message?.let { holder.messageView?.setText(it) }
        action?.let { holder.buttonView?.setText(it) }
        actionEnabled?.let { holder.buttonView?.isEnabled = it }

        holder.buttonView?.setOnClickListener { onClick?.invoke() }
    }

    override fun unbind(holder: Holder) {
        super.unbind(holder)
        holder.buttonView?.setOnClickListener(null)
    }

    class Holder : EpoxyHolder() {
        var itemView: View? = null
        var messageView: TextView? = null
        var titleView: TextView? = null
        var buttonView: Button? = null

        override fun bindView(itemView: View) {
            this.itemView = itemView
            this.messageView = itemView.findViewById(R.id.message)
            this.titleView = itemView.findViewById(R.id.title)
            this.buttonView = itemView.findViewById(R.id.action)
        }
    }
}
