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

package com.swordfish.lemuroid.app.mobile.feature.gamemenu

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.lib.android.RetrogradeAppCompatActivity
import com.swordfish.lemuroid.lib.injection.PerFragment
import dagger.android.ContributesAndroidInjector

class GameMenuActivity : RetrogradeAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empty_navigation_overlay)
        setSupportActionBar(findViewById(R.id.toolbar))

        val navController = findNavController(R.id.nav_host_fragment)
        navController.setGraph(R.navigation.mobile_game_menu, intent.extras)

        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp() || super.onSupportNavigateUp()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    @dagger.Module
    abstract class Module {

        @PerFragment
        @ContributesAndroidInjector(modules = [GameMenuCoreOptionsFragment.Module::class])
        abstract fun coreOptionsFragment(): GameMenuCoreOptionsFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GameMenuFragment.Module::class])
        abstract fun gameMenuFragment(): GameMenuFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GameMenuLoadFragment.Module::class])
        abstract fun gameMenuLoadFragment(): GameMenuLoadFragment

        @PerFragment
        @ContributesAndroidInjector(modules = [GameMenuSaveFragment.Module::class])
        abstract fun gameMenuSaveFragment(): GameMenuSaveFragment
    }
}
