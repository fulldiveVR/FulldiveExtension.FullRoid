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