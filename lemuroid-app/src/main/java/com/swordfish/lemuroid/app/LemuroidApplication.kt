/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package com.swordfish.lemuroid.app

import android.annotation.SuppressLint
import android.content.Context
import androidx.startup.AppInitializer
import androidx.work.Configuration
import androidx.work.ListenableWorker
import com.google.android.material.color.DynamicColors
import com.swordfish.lemuroid.app.appextension.remoteconfig.IRemoteConfigFetcher
import com.swordfish.lemuroid.app.shared.startup.GameProcessInitializer
import com.swordfish.lemuroid.app.shared.startup.MainProcessInitializer
import com.swordfish.lemuroid.app.utils.android.isMainProcess
import com.swordfish.lemuroid.ext.feature.context.ContextHandler
import com.swordfish.lemuroid.lib.injection.HasWorkerInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.DaggerApplication
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LemuroidApplication : DaggerApplication(), HasWorkerInjector {

    @Inject
    lateinit var workerInjector: DispatchingAndroidInjector<ListenableWorker>

    @Inject
    lateinit var remoteConfig: IRemoteConfigFetcher

    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()

        val initializeComponent = if (isMainProcess()) {
            MainProcessInitializer::class.java
        } else {
            GameProcessInitializer::class.java
        }

        AppInitializer.getInstance(this).initializeComponent(initializeComponent)

        DynamicColors.applyToActivitiesIfAvailable(this)

        GlobalScope.launch {
            remoteConfig.fetch(true)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        ContextHandler.attachBaseContext(base)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerLemuroidApplicationComponent.builder().create(this)
    }

    override fun workerInjector(): AndroidInjector<ListenableWorker> = workerInjector
}
