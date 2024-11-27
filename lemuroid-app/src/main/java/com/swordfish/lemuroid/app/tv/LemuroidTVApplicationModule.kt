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

package com.swordfish.lemuroid.app.tv

import com.swordfish.lemuroid.app.tv.folderpicker.TVFolderPickerActivity
import com.swordfish.lemuroid.app.tv.folderpicker.TVFolderPickerLauncher
import com.swordfish.lemuroid.app.tv.game.TVGameActivity
import com.swordfish.lemuroid.app.tv.gamemenu.TVGameMenuActivity
import com.swordfish.lemuroid.app.tv.input.TVGamePadBindingActivity
import com.swordfish.lemuroid.app.tv.main.MainTVActivity
import com.swordfish.lemuroid.app.tv.settings.TVSettingsActivity
import com.swordfish.lemuroid.lib.injection.PerActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class LemuroidTVApplicationModule {
    @PerActivity
    @ContributesAndroidInjector(modules = [MainTVActivity.Module::class])
    abstract fun tvMainActivity(): MainTVActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tvGameActivity(): TVGameActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tvGameMenuActivity(): TVGameMenuActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tvFolderPickerLauncher(): TVFolderPickerLauncher

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tvFolderPickerActivity(): TVFolderPickerActivity

    @PerActivity
    @ContributesAndroidInjector
    abstract fun tvGamePadBindingActivity(): TVGamePadBindingActivity

    @PerActivity
    @ContributesAndroidInjector(modules = [TVSettingsActivity.Module::class])
    abstract fun tvSettingsActivity(): TVSettingsActivity
}
