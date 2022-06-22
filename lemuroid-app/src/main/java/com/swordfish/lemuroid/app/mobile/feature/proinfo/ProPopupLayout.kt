package com.swordfish.lemuroid.app.mobile.feature.proinfo

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.swordfish.lemuroid.R

class ProPopupLayout : FrameLayout {

    private val animator = NavigationPanelAnimator()

    constructor(context: Context) : super(context) {
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initLayout()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initLayout()
    }

    private fun initLayout() {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.layout_pro_popup, this, true)

        val closeImageView = findViewById<ImageView>(R.id.closeImageView)
        closeImageView.setOnClickListener { hideSnackbar() }
    }

    fun showSnackbar() {
        Log.d("TestB", "showSnackbar")
        val cardView = findViewById<CardView>(R.id.cardView)
        cardView.isVisible = true
        animator.show(cardView)
    }

    fun hideSnackbar() {
        val cardView = findViewById<CardView>(R.id.cardView)
        animator.hide(
            cardView,
            endAction = {
                cardView.isVisible = false
            },
            hideDirection = NavigationPanelAnimator.HIDE_TO_BOTTOM
        )
    }
}
