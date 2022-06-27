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
package com.swordfish.lemuroid.app.fulldive.analytics

import android.util.Log

object DefaultLogger : ILogger {

    override fun i(message: String) {
        i(DEFAULT_TAG, message)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun i(tag: String, message: String, tr: Throwable) {
        Log.i(tag, message, tr)
    }

    override fun e(tag: String, tr: Throwable) {
        Log.e(tag, tr.message.orEmpty(), tr)
        tr.printStackTrace()
    }

    override fun e(tr: Throwable) {
        Log.e(DEFAULT_TAG, tr.toString())
        tr.printStackTrace()
    }

    override fun e(message: String) {
        Log.e(DEFAULT_TAG, message)
    }

    override fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    override fun e(tag: String, message: String, tr: Throwable) {
        Log.e(tag, message, tr)
        tr.printStackTrace()
    }

    override fun d(message: String) {
        Log.d(DEFAULT_TAG, message)
    }

    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun d(tag: String, message: String, tr: Throwable) {
        Log.d(tag, message, tr)
        tr.printStackTrace()
    }

    override fun v(tag: String, message: String) {
        Log.v(tag, message)
    }

    override fun v(tag: String, message: String, tr: Throwable) {
        Log.v(tag, message, tr)
        tr.printStackTrace()
    }

    override fun w(message: String) {
        w(DEFAULT_TAG, message)
    }

    override fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun w(tag: String, tr: Throwable) {
        Log.w(tag, tr)
        tr.printStackTrace()
    }

    override fun w(tag: String, message: String, tr: Throwable) {
        Log.w(tag, message, tr)
        tr.printStackTrace()
    }

    private const val DEFAULT_TAG = "FULLDIVE"
}
