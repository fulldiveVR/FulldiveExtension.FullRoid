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

package com.swordfish.lemuroid.lib.library.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.swordfish.lemuroid.lib.library.db.entity.DataFile

@Dao
interface DataFileDao {

    @Query("SELECT * FROM datafiles where gameId = :gameId")
    fun selectDataFilesForGame(gameId: Int): List<DataFile>

    @Query("SELECT * FROM datafiles WHERE lastIndexedAt < :lastIndexedAt")
    fun selectByLastIndexedAtLessThan(lastIndexedAt: Long): List<DataFile>

    @Insert
    fun insert(dataFile: DataFile)

    @Insert
    fun insert(dataFiles: List<DataFile>)

    @Delete
    fun delete(dataFiles: List<DataFile>)
}
