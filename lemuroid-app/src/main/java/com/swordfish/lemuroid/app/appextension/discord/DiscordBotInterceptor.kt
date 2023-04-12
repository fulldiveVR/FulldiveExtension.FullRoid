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

package com.swordfish.lemuroid.app.appextension.discord

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class DiscordBotInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithHeaders = originalRequest.newBuilder()
            .header("Authorization", BOT_TOKEN)
            .build()
        return chain.proceed(requestWithHeaders)
    }

    companion object {
        private const val BOT_TOKEN = "Bot MTA5MzQ4MjkyODY1NjM1OTUwNA.GiGiv_.dR6IrgMkhdj6OwYjKypENVFN3EGwVW-QkaN6S0"
    }
}
