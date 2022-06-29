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

package com.swordfish.lemuroid.app.mobile.feature.proinfo.tutorial

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.appextension.FulldiveConfigs
import com.swordfish.lemuroid.app.appextension.fromHtmlToSpanned
import com.swordfish.lemuroid.app.appextension.getHexColor
import com.swordfish.lemuroid.app.appextension.openAppInGooglePlay
import com.swordfish.lemuroid.app.fulldive.analytics.IActionTracker
import com.swordfish.lemuroid.app.fulldive.analytics.TrackerConstants
import com.swordfish.lemuroid.app.utils.EmotionsTool
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ProTutorialFragment : Fragment() {

    @Inject
    lateinit var actionTracker: IActionTracker

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pro_tutorial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.titleTextView).text = fromHtmlToSpanned(
            getString(R.string.string_pro_tutorial_title)
                .replace(
                    "%tutorialTextColor%",
                    requireActivity().getHexColor(R.color.main_color)
                )
        )

        view.findViewById<TextView>(R.id.disclamerTextView).text = SpannableString
            .valueOf(requireContext().getString(R.string.string_pro_tutorial_disclamer))
            .apply { EmotionsTool.processCheckMark(requireContext(), this) }

        view.findViewById<TextView>(R.id.buyProButton).setOnClickListener {
            actionTracker.logAction(TrackerConstants.EVENT_BUY_PRO_CLICKED)
            activity?.openAppInGooglePlay(FulldiveConfigs.FULLROID_PRO_PACKAGE_NAME)
        }
    }

    @dagger.Module
    class Module
}
