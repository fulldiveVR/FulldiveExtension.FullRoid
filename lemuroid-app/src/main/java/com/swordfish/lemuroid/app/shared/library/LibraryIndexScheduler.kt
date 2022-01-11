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

package com.swordfish.lemuroid.app.shared.library

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object LibraryIndexScheduler {
    val UNIQUE_WORK_ID: String = LibraryIndexScheduler::class.java.simpleName

    fun scheduleFullSync(applicationContext: Context) {
        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.APPEND,
                OneTimeWorkRequestBuilder<LibraryIndexWork>().build()
            )
            .then(OneTimeWorkRequestBuilder<CoreUpdateWork>().build())
            .enqueue()
    }

    fun scheduleCoreUpdate(applicationContext: Context) {
        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(
                UNIQUE_WORK_ID,
                ExistingWorkPolicy.APPEND,
                OneTimeWorkRequestBuilder<CoreUpdateWork>().build()
            )
            .enqueue()
    }
}
