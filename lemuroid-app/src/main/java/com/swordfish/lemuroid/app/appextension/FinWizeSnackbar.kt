package com.swordfish.lemuroid.app.appextension

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.swordfish.lemuroid.R

class FinWizeSnackbar {

    private var snackbar: Snackbar? = null

    fun showSnackBar(
        view: View,
        onOpenFinWizeClicked: () -> Unit,
        onCloseClicked: () -> Unit,
        bottomMargin: Int
    ) {
        val snackView = View.inflate(view.context, R.layout.layout_fin_wize, null)
        snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE)
        snackbar?.view?.let { rootView ->
            (rootView as? ViewGroup)?.removeAllViews()
            (rootView as? ViewGroup)?.addView(snackView)
            rootView.setPadding(0, 0, 0, 0)

            val params = rootView.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(
                params.leftMargin,
                params.topMargin,
                params.rightMargin,
                bottomMargin + params.bottomMargin
            )
            rootView.layoutParams = params
            rootView.elevation = 0f

            snackbar?.setBackgroundTint(
                ContextCompat.getColor(
                    view.context,
                    android.R.color.transparent
                )
            )
            val cardView = snackView
                .findViewById<CardView>(R.id.cardView)

            cardView.setOnClickListener {
                onOpenFinWizeClicked.invoke()
            }
            val crossButton = snackView.findViewById<ImageView>(R.id.closePopupButton)
            crossButton.setOnClickListener {
                onCloseClicked.invoke()
                dismiss()
            }
        }

        snackbar?.show()
    }

    fun dismiss() {
        snackbar?.dismiss()
    }
}
