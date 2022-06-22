package com.swordfish.lemuroid.app.mobile.feature.proinfo

import android.view.View
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat

open class BaseViewAnimator {
    private val interpolator: Interpolator = OvershootInterpolator(MENU_ANIMATION_OVERSHOT)

    protected fun animate(
        view: View,
        forced: Boolean,
        duration: Long,
        targetAngle: Float,
        targetScale: Float,
        targetAlpha: Float = 1f,
        targetX: Float = 0f,
        targetY: Float = 0f,
        startAction: ((view: View) -> Unit)? = null,
        endAction: ((view: View) -> Unit)? = null,
        animationInterpolator: Interpolator = interpolator
    ) {
        if (forced) {
            startAction?.invoke(view)
            with(view) {
                rotation = targetAngle
                alpha = targetAlpha
                scaleX = targetScale
                scaleY = targetScale
                translationX = targetX
                translationY = targetY
            }
            endAction?.invoke(view)
        } else {
            ViewCompat.animate(view)
                .rotation(targetAngle)
                .scaleX(targetScale)
                .scaleY(targetScale)
                .alpha(targetAlpha)
                .translationX(targetX)
                .translationY(targetY)
                .withStartAction { startAction?.invoke(view) }
                .withEndAction { endAction?.invoke(view) }
                .withLayer()
                .setDuration(duration)
                .setInterpolator(animationInterpolator)
                .start()
        }
    }

    companion object {
        private const val MENU_ANIMATION_OVERSHOT = 3f
    }
}
