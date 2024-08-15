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

interface ILogger {
    fun i(message: String)
    fun i(tag: String, message: String)
    fun i(tag: String, message: String, tr: Throwable)
    fun e(tag: String, tr: Throwable)
    fun e(tr: Throwable)
    fun e(message: String)
    fun e(tag: String, message: String)
    fun e(tag: String, message: String, tr: Throwable)
    fun d(message: String)
    fun d(tag: String, message: String)
    fun d(tag: String, message: String, tr: Throwable)
    fun v(tag: String, message: String)
    fun v(tag: String, message: String, tr: Throwable)
    fun w(message: String)
    fun w(tag: String, message: String)
    fun w(tag: String, tr: Throwable)
    fun w(tag: String, message: String, tr: Throwable)
}