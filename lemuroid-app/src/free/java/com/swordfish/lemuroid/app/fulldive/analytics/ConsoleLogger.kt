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
object ConsoleLogger : ILogger {

    override fun i(message: String) {
        i("i: FULLDIVE", message)
    }


    override fun i(tag: String, message: String) {
        println("i: $tag: $message")
    }


    override fun i(tag: String, message: String, tr: Throwable) {
        println("i: $tag: $message, $tr")
    }


    override fun e(tag: String, tr: Throwable) {
        println("e: $tag: $tr")
        tr.printStackTrace()
    }


    override fun e(tr: Throwable) {
        println("e: FULLDIVE: $tr")
        tr.printStackTrace()
    }


    override fun e(message: String) {
        println("e: FULLDIVE: $message")
    }


    override fun e(tag: String, message: String) {
        println("e: $tag: $message")
    }


    override fun e(tag: String, message: String, tr: Throwable) {
        println("e: $tag: $message, $tr")
        tr.printStackTrace()
    }


    override fun d(message: String) {
        println("d: FULLDIVE: $message")
    }


    override fun d(tag: String, message: String) {
        println("d: $tag: $message")
    }


    override fun d(tag: String, message: String, tr: Throwable) {
        println("d: $tag: $message, $tr")
        tr.printStackTrace()
    }


    override fun v(tag: String, message: String) {
        println("v: $tag: $message")
    }


    override fun v(tag: String, message: String, tr: Throwable) {
        println("v: $tag: $message, $tr")
        tr.printStackTrace()
    }


    override fun w(message: String) {
        w("FULLDIVE", message)
    }


    override fun w(tag: String, message: String) {
        println("w: $tag: $message")
    }


    override fun w(tag: String, tr: Throwable) {
        println("w: $tag: $tr")
        tr.printStackTrace()
    }


    override fun w(tag: String, message: String, tr: Throwable) {
        println("w: $tag: $message, $tr")
        tr.printStackTrace()
    }
}
