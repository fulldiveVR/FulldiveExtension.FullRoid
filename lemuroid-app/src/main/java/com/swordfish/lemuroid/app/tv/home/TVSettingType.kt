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

package com.swordfish.lemuroid.app.tv.home

import com.swordfish.lemuroid.R

enum class TVSettingType(val icon: Int, val text: Int) {
    STOP_RESCAN(R.drawable.ic_stop_white_64dp, R.string.stop),
    RESCAN(R.drawable.ic_refresh_white_64dp, R.string.rescan),
    SHOW_ALL_FAVORITES(R.drawable.ic_more_games, R.string.show_all),
    CHOOSE_DIRECTORY(R.drawable.ic_folder_white_64dp, R.string.directory),
    SETTINGS(R.drawable.ic_settings_white_64dp, R.string.settings),
    SAVE_SYNC(R.drawable.ic_cloud_sync_64dp, R.string.save_sync),
}
