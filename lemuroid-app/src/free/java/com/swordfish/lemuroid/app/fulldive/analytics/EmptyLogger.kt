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

@Suppress("UNUSED")
object EmptyLogger : ILogger {

    override fun i(message: String) {}
    override fun i(tag: String, message: String) {}
    override fun i(tag: String, message: String, tr: Throwable) {}
    override fun e(tag: String, tr: Throwable) {}
    override fun e(tr: Throwable) {}
    override fun e(message: String) {}
    override fun e(tag: String, message: String) {}
    override fun e(tag: String, message: String, tr: Throwable) {}
    override fun d(message: String) {}
    override fun d(tag: String, message: String) {}
    override fun d(tag: String, message: String, tr: Throwable) {}
    override fun v(tag: String, message: String) {}
    override fun v(tag: String, message: String, tr: Throwable) {}
    override fun w(message: String) {}
    override fun w(tag: String, message: String) {}
    override fun w(tag: String, tr: Throwable) {}
    override fun w(tag: String, message: String, tr: Throwable) {}
}
