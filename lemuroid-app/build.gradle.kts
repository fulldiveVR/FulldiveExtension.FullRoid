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
plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlinx-serialization")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }

    dependencies {
        classpath(deps.libs.googleServices.googleServices)
        classpath(deps.libs.firebase.firebase)
    }
}

android {
    val versionMajor = 1
    val versionMinor = 0
    val versionPatch = 9

   // namespace "com.swordfish.lemuroid"

    defaultConfig {
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "${versionMajor}.${versionMinor}.${versionPatch}"
        applicationId = "com.fulldive.extension.fullroid"
        buildConfigField("String", "FLURRY_API_KEY", file("../flurrykey.txt").readText())
        buildConfigField("String", "SERVER_CLIENT_ID", file("../googlekey.txt").readText())

        firebaseCrashlytics {
            mappingFileUploadEnabled = false
        }
    }

    if (usePlayDynamicFeatures()) {
        println("Building Google Play version. Bundling dynamic features.")
        dynamicFeatures.addAll(
            setOf(
                ":lemuroid_core_desmume",
                ":lemuroid_core_dosbox_pure",
                ":lemuroid_core_fbneo",
                ":lemuroid_core_fceumm",
                ":lemuroid_core_gambatte",
                ":lemuroid_core_genesis_plus_gx",
                ":lemuroid_core_handy",
                ":lemuroid_core_mame2003_plus",
                ":lemuroid_core_mednafen_ngp",
                ":lemuroid_core_mednafen_pce_fast",

                ":lemuroid_core_mednafen_wswan",
                ":lemuroid_core_melonds",
                ":lemuroid_core_mgba",
                ":lemuroid_core_mupen64plus_next_gles3",
                ":lemuroid_core_pcsx_rearmed",
                ":lemuroid_core_ppsspp",
                ":lemuroid_core_prosystem",
                ":lemuroid_core_snes9x",
                ":lemuroid_core_stella",

                ":lemuroid_core_citra"
            )
        )
    }

    // Since some dependencies are closed source we make a completely free as in free speech variant.
    flavorDimensions("opensource", "cores")

    productFlavors {

        create("free") {
            dimension = "opensource"
            applicationId = "com.fulldive.extension.fullroid"
            resValue("string", "lemuroid_name", "FullRoid")
        }

        create("pro") {
            dimension = "opensource"
            applicationId = "com.fulldive.extension.fullroid.pro"
            resValue("string", "lemuroid_name", "FullRoid X")
        }

        create("play") {
            dimension = "opensource"
            applicationId = "com.fulldive.extension.fullroid.play"
            resValue("string", "lemuroid_name", "PLAY Full Roid")
        }

        // Include cores in the final apk
        create("bundle") {
            dimension = "cores"
        }

        // Download cores on demand (from GooglePlay or GitHub)
        create("dynamic") {
            dimension = "cores"
        }
    }

    // Stripping created some issues with some libretro cores such as ppsspp
    packagingOptions {
        doNotStrip("*/*/*_libretro_android.so")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/library_release.kotlin_module")
    }

    signingConfigs {
        maybeCreate("release").apply {
            storeFile = file("../keys/keys.jks")
            keyAlias = System.getenv("FULLDIVE_ALIAS")
            storePassword = System.getenv("FULLDIVE_KEYSTORE_PASSWORD")
            keyPassword = System.getenv("FULLDIVE_ALIAS_PASSWORD")
        }
    }

    //Free Bundle
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs["release"]
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            applicationVariants.all {
                val variant = this
                variant.outputs
                    .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                    .forEach { output ->
                        val outputFileName = if (variant.flavorName.contains("pro")) {
                            "FullRoid-v${android.defaultConfig.versionName} X-${variant.buildType.name}.apk"

                        } else {
                            "FullRoid-v${android.defaultConfig.versionName}-${variant.buildType.name}.apk"
                        }
                        output.outputFileName = outputFileName
                    }
            }

            firebaseCrashlytics {
                mappingFileUploadEnabled = true
            }
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            applicationVariants.all {
                val variant = this
                variant.outputs
                    .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                    .forEach { output ->
                        val outputFileName = if (variant.flavorName.contains("pro")) {
                            "FullRoid-v${android.defaultConfig.versionName} X-${variant.buildType.name}.apk"
                        } else {
                            "FullRoid-v${android.defaultConfig.versionName}-${variant.buildType.name}.apk"
                        }
                        output.outputFileName = outputFileName
                    }
            }
            firebaseCrashlytics {
                mappingFileUploadEnabled = false
            }
        }
    }

    lint {
        disable += setOf("MissingTranslation", "ExtraTranslation", "EnsureInitializerMetadata")
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":retrograde-util"))
    implementation(project(":retrograde-app-shared"))
    implementation(project(":lemuroid-metadata-libretro-db"))
    implementation(project(":lemuroid-touchinput"))

    "bundleImplementation"(project(":bundled-cores"))

    "freeImplementation"(project(":lemuroid-app-ext-free"))
    "proImplementation"(project(":lemuroid-app-ext-free"))
    "playImplementation"(project(":lemuroid-app-ext-play"))

    implementation(deps.libs.androidx.navigation.navigationFragment)
    implementation(deps.libs.androidx.navigation.navigationUi)
    implementation(deps.libs.material)
    implementation(deps.libs.coil)
    implementation(deps.libs.androidx.appcompat.constraintLayout)
    implementation(deps.libs.androidx.activity.activity)
    implementation(deps.libs.androidx.activity.activityKtx)
    implementation(deps.libs.androidx.appcompat.appcompat)
    implementation(deps.libs.androidx.preferences.preferencesKtx)
    //implementation(deps.libs.rxbindings.core)
   // implementation(deps.libs.rxbindings.appcompat)
    implementation(deps.libs.arch.work.runtime)
    implementation(deps.libs.arch.work.runtimeKtx)
   // implementation(deps.libs.arch.work.rxjava2)
  //  implementation(deps.libs.arch.work.okhttp)
    implementation(deps.libs.androidx.lifecycle.commonJava8)
    implementation(deps.libs.androidx.lifecycle.reactiveStreams)
    implementation(deps.libs.epoxy.expoxy)
    implementation(deps.libs.epoxy.paging)

    kapt(deps.libs.epoxy.processor)
    kapt(deps.libs.androidx.lifecycle.processor)

    implementation(deps.libs.androidx.leanback.leanback)
    implementation(deps.libs.androidx.leanback.leanbackPreference)
    implementation(deps.libs.androidx.leanback.leanbackPaging)

    implementation(deps.libs.androidx.appcompat.recyclerView)
    implementation(deps.libs.androidx.paging.common)
    implementation(deps.libs.androidx.paging.runtime)
//    implementation(deps.libs.androidx.paging.rxjava2)
    implementation(deps.libs.androidx.room.common)
    implementation(deps.libs.androidx.room.runtime)
    implementation(deps.libs.androidx.room.rxjava2)
    implementation(deps.libs.androidx.room.ktx)
//    implementation(deps.libs.autodispose.android.archComponents)
//    implementation(deps.libs.autodispose.android.core)
//    implementation(deps.libs.autodispose.core)
    implementation(deps.libs.dagger.android.core)
    implementation(deps.libs.dagger.android.support)
    implementation(deps.libs.dagger.core)
//    implementation(deps.libs.koptional)
//    implementation(deps.libs.koptionalRxJava2)
    implementation(deps.libs.kotlinxCoroutinesAndroid)
    implementation(deps.libs.kotlinxCoroutinesRxJava2)
    implementation(deps.libs.okHttp3)
    implementation(deps.libs.okio)
    implementation(deps.libs.retrofit)
    implementation(deps.libs.retrofitRxJava2)
    implementation(deps.libs.rxAndroid2)
    implementation(deps.libs.rxJava2)
   implementation(deps.libs.flowPreferences)
//    implementation(deps.libs.rxPermissions2)
//    implementation(deps.libs.rxPreferences)
//    implementation(deps.libs.rxRelay2)
//    implementation(deps.libs.rxKotlin2)
    implementation(deps.libs.guava)
    implementation(deps.libs.androidx.documentfile)
    implementation(deps.libs.androidx.leanback.tvProvider)
    implementation(deps.libs.harmony)
    implementation(deps.libs.startup)
    implementation(deps.libs.kotlin.serialization)
    implementation(deps.libs.kotlin.serializationJson)

    implementation(deps.libs.libretrodroid)
    implementation(deps.libs.lottie)
    // Uncomment this when using a local aar file.
    //implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    kapt(deps.libs.dagger.android.processor)
    kapt(deps.libs.dagger.compiler)

    platform("com.google.firebase:firebase-bom:30.1.0")
    implementation("com.google.firebase:firebase-core:21.0.0")
    implementation(deps.libs.firebase.crashlytics) {
        isTransitive = true
    }
    implementation(deps.libs.firebase.firebaseAnalytics)
    implementation(deps.libs.firebase.firebaseStorage)


    implementation(deps.libs.flurry.flurry)

//    implementation(deps.libs.koptional)

    implementation(deps.libs.gdrive.apiClient)
    implementation(deps.libs.gdrive.apiClientAndroid)
    implementation(deps.libs.gdrive.apiServicesDrive)
    implementation(deps.libs.play.playServices)
}

fun usePlayDynamicFeatures(): Boolean {
    val task = gradle.startParameter.taskRequests.toString()
    return task.contains("Play") && task.contains("Dynamic")
}
