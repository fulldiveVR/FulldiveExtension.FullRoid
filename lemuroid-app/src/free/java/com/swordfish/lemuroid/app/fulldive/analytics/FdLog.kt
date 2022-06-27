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

@file:Suppress("unused")

package com.swordfish.lemuroid.app.fulldive.analytics

object FdLog {
    private var logger: ILogger = DefaultLogger
    private var tagReader: (String) -> String = { it }
    private const val DEFAULT_TAG = "FULLDIVE"
    fun i(message: String) {
        i(DEFAULT_TAG, message)
    }

    fun i(tag: String, message: String) {
        logger.i(tagReader(tag), message)
    }

    fun setLogger(value: ILogger?) {
        logger = value ?: DefaultLogger
    }

    fun setTagReader(value: ((String) -> String)?) {
        tagReader = value ?: { it }
    }

    fun isAvailable() = logger != ErrorLogger

    fun setAvailable(isAvailable: Boolean) {
        logger = if (isAvailable) DefaultLogger else ErrorLogger
    }

    fun i(tag: String, message: String, tr: Throwable) {
        logger.i(tagReader(tag), message, tr)
    }

    fun e(tag: String, tr: Throwable) {
        logger.e(tagReader(tag), tr)
    }

    fun e(tr: Throwable) {
        logger.e(DEFAULT_TAG, tr)
    }

    fun e(message: String) {
        logger.e(DEFAULT_TAG, message)
    }

    fun e(tag: String, message: String) {
        logger.e(tagReader(tag), message)
    }

    fun e(tag: String, message: String, tr: Throwable) {
        logger.e(tagReader(tag), message, tr)
    }

    fun d(message: String) {
        logger.d(DEFAULT_TAG, message)
    }

    fun d(tag: String, message: String) {
        logger.d(tagReader(tag), message)
    }

    fun d(tag: String, message: String, tr: Throwable) {
        logger.d(tagReader(tag), message, tr)
    }

    fun v(tag: String, message: String) {
        logger.v(tagReader(tag), message)
    }

    fun v(tag: String, message: String, tr: Throwable) {
        logger.v(tagReader(tag), message, tr)
    }

    fun w(message: String) {
        w(DEFAULT_TAG, message)
    }

    fun w(tag: String, message: String) {
        logger.w(tagReader(tag), message)
    }

    fun w(tag: String, tr: Throwable) {
        logger.w(tagReader(tag), tr)
    }

    fun w(tag: String, message: String, tr: Throwable) {
        logger.w(tagReader(tag), message, tr)
    }
}
