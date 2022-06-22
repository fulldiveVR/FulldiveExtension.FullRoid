package com.swordfish.lemuroid.lib.fulldive

import android.util.Log
import com.swordfish.lemuroid.lib.BuildConfig


object FulldiveSettings {

    fun isProVersion(): Boolean {
        Log.d("TestB","isProVersion ${BuildConfig.BUILD_TYPE} ${BuildConfig.LIBRARY_PACKAGE_NAME} ")
       return true
    }
}
