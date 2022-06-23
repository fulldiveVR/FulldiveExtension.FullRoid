package com.swordfish.lemuroid.app.mobile.feature.proinfo.tutorial

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
import com.swordfish.lemuroid.app.utils.EmotionsTool

class ProTutorialFragment : Fragment() {

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
            activity?.openAppInGooglePlay(FulldiveConfigs.FULLROID_PRO_PACKAGE_NAME)
        }
    }

    @dagger.Module
    class Module
}
