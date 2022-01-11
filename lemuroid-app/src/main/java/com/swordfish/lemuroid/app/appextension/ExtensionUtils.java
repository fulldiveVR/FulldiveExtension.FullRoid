/*
 * Copyright (c) 2022 FullDive
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
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.swordfish.lemuroid.app.appextension;

import android.net.Uri;

public class ExtensionUtils {
    public static String KEY_WORK_STATUS = "work_status";
    public static String KEY_WORK_PROGRESS = "work_progress";
    public static String KEY_RESULT = "result";
    static String PREFERENCE_AUTHORITY = "com.swordfish.lemuroid.app.appextension.FulldiveContentProvider";
    static String BASE_URL = "content://" + PREFERENCE_AUTHORITY;

    public static Uri getContentUri(String value) {
        return Uri
                .parse(BASE_URL)
                .buildUpon().appendPath(KEY_WORK_STATUS)
                .appendPath(value)
                .build();
    }

    public static Uri getProgressContentUri(String value) {
        return Uri
                .parse(BASE_URL)
                .buildUpon().appendPath(KEY_WORK_PROGRESS)
                .appendPath(value)
                .build();
    }
}