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

package com.swordfish.lemuroid.common.graphics

import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.view.PixelCopy
import io.reactivex.Maybe
import java.lang.RuntimeException
import kotlin.math.roundToInt

fun GLSurfaceView.takeScreenshot(maxResolution: Int): Maybe<Bitmap> = Maybe.create { emitter ->
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
        emitter.onComplete()
        return@create
    }

    queueEvent {
        try {
            val outputScaling = maxResolution / maxOf(width, height).toFloat()
            val inputScaling = outputScaling * 2

            val inputBitmap = Bitmap.createBitmap(
                (width * inputScaling).roundToInt(),
                (height * inputScaling).roundToInt(),
                Bitmap.Config.ARGB_8888
            )

            val onCompleted = { result: Int ->
                if (result == PixelCopy.SUCCESS) {

                    // This rescaling limits the artifacts introduced by shaders.
                    val outputBitmap = Bitmap.createScaledBitmap(
                        inputBitmap,
                        (width * outputScaling).roundToInt(),
                        (height * outputScaling).roundToInt(),
                        true
                    )

                    emitter.onSuccess(outputBitmap)
                } else {
                    emitter.onError(RuntimeException("Cannot take screenshot. Error code: $result"))
                }
            }
            PixelCopy.request(this, inputBitmap, onCompleted, handler)
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }
}
