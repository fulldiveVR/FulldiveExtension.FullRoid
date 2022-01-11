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

package com.swordfish.lemuroid.app.shared.savesync

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.swordfish.lemuroid.app.utils.livedata.CombinedLiveData

class SaveSyncMonitor(private val appContext: Context) {

    fun getLiveData(): LiveData<Boolean> {
        return CombinedLiveData(getPeriodicLiveData(), getOneTimeLiveData()) { b1, b2 -> b1 || b2 }
    }

    private fun getPeriodicLiveData(): LiveData<Boolean> {
        val workInfosLiveData = WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWorkLiveData(SaveSyncWork.UNIQUE_PERIODIC_WORK_ID)

        return Transformations.map(workInfosLiveData) { workInfos ->
            val isRunning = workInfos
                .map { it.state }
                .any { it in listOf(WorkInfo.State.RUNNING) }

            isRunning
        }
    }

    private fun getOneTimeLiveData(): LiveData<Boolean> {
        val workInfosLiveData = WorkManager.getInstance(appContext)
            .getWorkInfosForUniqueWorkLiveData(SaveSyncWork.UNIQUE_WORK_ID)

        return Transformations.map(workInfosLiveData) { workInfos ->
            val isRunning = workInfos
                .map { it.state }
                .any { it in listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING) }

            isRunning
        }
    }
}
