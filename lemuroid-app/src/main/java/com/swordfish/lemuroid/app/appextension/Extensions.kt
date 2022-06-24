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

package com.swordfish.lemuroid.app.appextension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import androidx.core.content.ContextCompat
import com.swordfish.lemuroid.BuildConfig

fun Context.getPrivateSharedPreferences(): SharedPreferences {
    return this.getSharedPreferences(packageName + "_preference", Context.MODE_PRIVATE)
}

fun SharedPreferences.setProperty(tag: String, value: Int) {
    try {
        val spe = edit()
        spe.putInt(tag, value).apply()
    } catch (ex: Exception) {
    }
}

fun SharedPreferences.setProperty(tag: String, value: Boolean) {
    try {
        val spe = edit()
        spe.putBoolean(tag, value).apply()
    } catch (ex: Exception) {
    }
}

fun SharedPreferences.getProperty(tag: String, default_value: Int): Int {
    var result = default_value
    try {
        result = getInt(tag, default_value)
    } catch (ex: Exception) {
    }
    return result
}

fun SharedPreferences.getProperty(tag: String, default_value: Boolean): Boolean {
    var result = default_value
    try {
        result = getBoolean(tag, default_value)
    } catch (ex: Exception) {
    }
    return result
}

fun Context.openAppInGooglePlay(appPackageName: String? = null) {
    val packName = appPackageName ?: packageName
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$packName")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (anfe: android.content.ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packName")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

fun launchApp(activity: Activity, appPackageName: String): Boolean {
    return try {
        val launchIntent = activity
            .packageManager.getLaunchIntentForPackage(appPackageName)
        activity.startActivity(launchIntent)
        true
    } catch (ex: Exception) {
        false
    }
}

fun PackageManager.isPackageInstalled(packageName: String): Boolean {
    return try {
        getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun PackageManager.isFullRoidProInstalled(): Boolean {
    return isPackageInstalled(FulldiveConfigs.FULLROID_PRO_PACKAGE_NAME)
}

fun isProVersion(): Boolean = BuildConfig.FLAVOR.contains("pro")

fun fromHtmlToSpanned(html: String?): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html.orEmpty(), Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html.orEmpty())
    }
}

fun Context.getHexColor(id: Int): String {
    return String.format("#%06x", ContextCompat.getColor(this, id).and(0xffffff))
}
