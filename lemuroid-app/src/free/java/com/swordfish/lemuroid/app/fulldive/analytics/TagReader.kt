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

import com.swordfish.lemuroid.app.appextension.or

@ByteKey(bytes = [0x6e, 0x57, 0x6e, 0x67, 0x70, 0x41, 0x52, 0x68, 0x78, 0x53, 0x73, 0x32, 0x42, 0x46, 0x32, 0x6d])
class TagReader : ITagReader {
    private val map = mutableMapOf<String, String>()
    private val aesUtils by lazy {
        val bytes = this::class.java.getAnnotation(ByteKey::class.java)?.bytes
            ?: byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        AESUtils(bytes)
    }

    override fun read(text: String): String {
        var result = text
        try {
            if (text.startsWith('`')) {
                result = map[text].or {
                    aesUtils.read(text.substring(1)).also { value ->
                        map[text] = value
                    }
                }
            }
        } catch (ex: Exception) {
            FdLog.e(ex)
        }
        return result
    }
}
