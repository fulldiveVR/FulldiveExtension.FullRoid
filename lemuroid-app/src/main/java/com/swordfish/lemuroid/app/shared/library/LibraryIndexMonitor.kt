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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.swordfish.lemuroid.app.utils.livedata.ThrottledLiveData

class LibraryIndexMonitor(private val appContext: Context) {

    fun getLiveData(): LiveData<Boolean> {
        val workInfosLiveData = WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWorkLiveData(LibraryIndexScheduler.UNIQUE_WORK_ID)

        val result = Transformations.map(workInfosLiveData) { workInfos ->
            val isRunning = workInfos
                .map { it.state }
                .any { it in listOf(WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED) }

            isRunning
        }

        return ThrottledLiveData(result, 200)
    }
}
