/*
 *  RetrogradeApplicationComponent.kt
 *
 *  Copyright (C) 2017 Retrograde Project
 *
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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.swordfish.lemuroid.metadata.libretrodb.db

import android.content.Context
import androidx.room.Room
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Single
import java.util.concurrent.ExecutorService

class LibretroDBManager(context: Context, executorService: ExecutorService) {

    companion object {
        private const val DB_NAME = "libretro-db"
    }

    private val dbRelay = BehaviorRelay.create<LibretroDatabase>()

    val dbReady: Single<LibretroDatabase> = dbRelay.take(1).singleOrError()

    init {
        executorService.execute {
            val db = Room.databaseBuilder(context, LibretroDatabase::class.java, DB_NAME)
                .createFromAsset("libretro-db.sqlite")
                .fallbackToDestructiveMigration()
                .build()
            dbRelay.accept(db)
        }
    }
}
