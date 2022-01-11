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

package com.swordfish.lemuroid.lib.library.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "datafiles",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("gameId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("id", unique = true),
        Index("fileUri"),
        Index("gameId"),
        Index("lastIndexedAt")
    ]
)
data class DataFile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gameId: Int,
    val fileName: String,
    val fileUri: String,
    val lastIndexedAt: Long,
    val path: String?
) : Serializable
