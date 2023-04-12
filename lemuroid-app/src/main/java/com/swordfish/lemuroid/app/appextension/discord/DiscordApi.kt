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

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.swordfish.lemuroid.app.appextension.remoteconfig.IRemoteConfigFetcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface DiscordApi {
    @POST("api/channels/1093485251004735498/messages")
    suspend fun shareGame(@Body shareGameData: ShareGameData): okhttp3.ResponseBody
}

class DiscordApiImpl @Inject constructor(private val remoteConfig: IRemoteConfigFetcher) {
    private var discordRetrofit: Retrofit? = null

    init {
        val gson = Gson()

        discordRetrofit = Retrofit.Builder()
            .baseUrl("https://discord.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(createOkHttpClientBuilder().build())
            .build()
    }

    val api = discordRetrofit?.create(DiscordApi::class.java)

    private fun createOkHttpClientBuilder() = OkHttpClient.Builder().apply {
        readTimeout(30, TimeUnit.SECONDS)
        connectTimeout(30, TimeUnit.SECONDS)
        writeTimeout(60, TimeUnit.SECONDS)
        addInterceptor(DiscordBotInterceptor(remoteConfig))
        addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }
}

@Keep
data class ShareGameData(
    @SerializedName("content")
    val content: String,
    @SerializedName("embeds")
    val embeds: List<ShareEmbeds>? = null
)

@Keep
data class ShareEmbeds(
    @SerializedName("image")
    val image: ShareImage
)

@Keep
data class ShareImage(
    @SerializedName("url")
    val url: String
)
