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

/* ktlint-disable no-multi-spaces max-line-length */
object deps {
    object android {
        const val targetSdkVersion  = 34
        const val compileSdkVersion = 34
        const val minSdkVersion     = 23
        const val buildToolsVersion = "34.0.0"
    }

    object versions {
        const val dagger          = "2.19"
        const val gms             = "17.0.0"
        const val kotlin          = "1.8.20"
        const val okHttp          = "4.9.1"
        const val retrofit        = "2.9.0"
        const val work            = "2.9.0"
        const val navigation      = "2.5.2"
        const val lifecycle       = "2.6.1"
        const val leanback        = "1.1.0-rc01"
        const val googleApiClient = "1.32.1"
        const val paging          = "3.2.1"
        const val room            = "2.5.2"
        const val epoxy           = "4.0.0-beta3"
        const val serialization   = "1.2.2"
        const val fragment        = "1.5.1"
        const val activity        = "1.7.2"
        const val libretrodroid   = "0.12.1"
        const val radialgamepad   = "2.0.0"
        const val composeBom      = "2024.02.02"

        // Make sure this is compatible with current bom versions:
        // https://developer.android.com/jetpack/compose/bom/bom-mapping
        const val accompanist     = "0.34.0"

        const val lottie = "6.0.0"
        const val flurry = "14.0.0"
        const val firebase = "29.0.3"
        const val googleServices = "4.3.8"
        const val fabric = "2.5.2"
        const val firebaseAnalytics = "21.0.0"
        const val firebaseStorage = "19.1.0"
        const val crashlytics = "18.2.11"
    }

    object libs {
        object androidx {
            object appcompat {
                const val appcompat = "androidx.appcompat:appcompat:1.4.2"
                const val recyclerView = "androidx.recyclerview:recyclerview:1.2.1"
                const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.4"
            }
            object leanback {
                const val leanback = "androidx.leanback:leanback:${versions.leanback}"
                const val leanbackPreference = "androidx.leanback:leanback-preference:${versions.leanback}"
                const val leanbackPaging = "androidx.leanback:leanback-paging:1.1.0-alpha07"
                const val tvProvider = "androidx.tvprovider:tvprovider:1.0.0"
            }
            object ktx {
                const val core = "androidx.core:core-ktx:1.8.0"
                const val coreKtx = "androidx.core:core-ktx:1.8.0"
                const val collection = "androidx.collection:collection-ktx:1.1.0"
            }
            object lifecycle {
                const val commonJava8 = "androidx.lifecycle:lifecycle-common-java8:${versions.lifecycle}"
                const val processor = "androidx.lifecycle:lifecycle-compiler:${versions.lifecycle}"
                const val runtime = "androidx.lifecycle:lifecycle-runtime-ktx:${versions.lifecycle}"
                const val reactiveStreams = "android.arch.lifecycle:reactivestreams:1.1.1"
                const val viewModelCompose = "androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1"
            }
            object preferences {
                const val preferencesKtx = "androidx.preference:preference-ktx:1.1.1"
            }
            object paging {
                const val common = "androidx.paging:paging-common:${versions.paging}"
                const val runtime = "androidx.paging:paging-runtime:${versions.paging}"
                const val compose = "androidx.paging:paging-compose:${versions.paging}"
            }
            object navigation {
                const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:${versions.navigation}"
                const val navigationUi = "androidx.navigation:navigation-ui-ktx:${versions.navigation}"
                const val compose = "androidx.navigation:navigation-compose:${versions.navigation}"
            }
            object room {
                const val common = "androidx.room:room-common:${versions.room}"
                const val compiler = "androidx.room:room-compiler:${versions.room}"
                const val runtime = "androidx.room:room-runtime:${versions.room}"
                const val paging = "androidx.room:room-paging:${versions.room}"
                const val ktx = "androidx.room:room-ktx:${versions.room}"
            }
            object fragment {
                const val fragment = "androidx.fragment:fragment:${versions.fragment}"
                const val ktx = "androidx.fragment:fragment-ktx:${versions.fragment}"
            }
            const val documentfile = "androidx.documentfile:documentfile:1.0.1"
            object activity {
                const val activity = "androidx.activity:activity:${versions.activity}"
                const val activityKtx = "androidx.activity:activity-ktx:${versions.activity}"
                const val compose = "androidx.activity:activity-compose:${versions.activity}"
            }
            object compose {
                const val composeBom = "androidx.compose:compose-bom:${versions.composeBom}"
                const val material3 = "androidx.compose.material3:material3"
                const val extendedIcons = "androidx.compose.material:material-icons-extended"
                const val tooling = "androidx.compose.ui:ui-tooling"
                const val toolingPreview = "androidx.compose.ui:ui-tooling-preview"

                object accompanist {
                    const val systemUiController = "com.google.accompanist:accompanist-systemuicontroller:${versions.accompanist}"
                    const val navigationMaterial = "com.google.accompanist:accompanist-navigation-material:${versions.accompanist}"
                    const val drawablePainter = "com.google.accompanist:accompanist-drawablepainter:${versions.accompanist}"
                }
            }
            const val profileInstaller = "androidx.profileinstaller:profileinstaller:1.3.1"
        }
        object arch {
            object work {
                const val runtime = "androidx.work:work-runtime:${versions.work}"
                const val runtimeKtx = "androidx.work:work-runtime-ktx:${versions.work}"
            }
        }
        object dagger {
            const val core = "com.google.dagger:dagger:${versions.dagger}"
            const val compiler = "com.google.dagger:dagger-compiler:${versions.dagger}"
            object android {
                const val core = "com.google.dagger:dagger-android:${versions.dagger}"
                const val processor = "com.google.dagger:dagger-android-processor:${versions.dagger}"
                const val support = "com.google.dagger:dagger-android-support:${versions.dagger}"
            }
        }
        object kotlin {
            const val stdlib = "stdlib"
            const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-core:${versions.serialization}"
            const val serializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:${versions.serialization}"
        }
        object epoxy {
            const val expoxy = "com.airbnb.android:epoxy:${versions.epoxy}"
            const val paging = "com.airbnb.android:epoxy-paging:${versions.epoxy}"
            const val processor = "com.airbnb.android:epoxy-processor:${versions.epoxy}"
        }
        object play {
            const val review = "com.google.android.play:review:2.0.0"
            const val reviewKtx = "com.google.android.play:review-ktx:2.0.0"
            const val featureDelivery = "com.google.android.play:feature-delivery:2.1.0"
            const val featureDeliveryKtx = "com.google.android.play:feature-delivery-ktx:2.1.0"
            const val playServices = "com.google.android.gms:play-services-auth:17.0.0"
            const val coroutine = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4"
        }
        object gdrive {
            const val apiClient            = "com.google.api-client:google-api-client:${versions.googleApiClient}"
            const val apiClientAndroid     = "com.google.api-client:google-api-client-android:${versions.googleApiClient}"
            const val apiServicesDrive     = "com.google.apis:google-api-services-drive:v3-rev20210725-${versions.googleApiClient}"
        }
        object coil {
            const val coil = "io.coil-kt:coil:2.6.0"
            const val coilCompose = "io.coil-kt:coil-compose:2.6.0"
        }

        object composeSettings {
            const val uiTiles = "com.github.alorma.compose-settings:ui-tiles:2.1.0"
            const val uiTilesExtended = "com.github.alorma.compose-settings:ui-tiles-extended:2.1.0"
            const val diskStorage = "com.github.alorma:compose-settings-storage-disk:2.0.0"
            const val memoryStorage = "com.github.alorma:compose-settings-storage-memory:2.0.0"
        }

        object flurry {
            const val flurry = "com.flurry.android:analytics:${versions.flurry}"
        }

        object firebase {
            const val firebase = "com.google.firebase:firebase-crashlytics-gradle:${versions.fabric}"
            const val firebaseAnalytics = "com.google.firebase:firebase-analytics:${versions.firebaseAnalytics}"
            const val firebaseStorage = "com.google.firebase:firebase-storage-ktx:${versions.firebaseStorage}"
            const val crashlytics = "com.google.firebase:firebase-crashlytics:${versions.crashlytics}"
        }

        object googleServices {
            const val googleServices = "com.google.gms:google-services:${versions.googleServices}"
        }

        const val kotlinxCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4"
        const val okio                     = "com.squareup.okio:okio:2.10.0"
        const val okHttp3                  = "com.squareup.okhttp3:okhttp:${versions.okHttp}"
        const val retrofit                 = "com.squareup.retrofit2:retrofit:${versions.retrofit}"
        const val flowPreferences          = "com.fredporciuncula:flow-preferences:1.8.0"
        const val timber                   = "com.jakewharton.timber:timber:5.0.1"
        const val material                 = "com.google.android.material:material:1.6.1"
        const val multitouchGestures       = "com.dinuscxj:multitouchgesturedetector:1.0.0"
        const val guava                    = "com.google.guava:guava:30.1.1-android"
        const val harmony                  = "com.frybits.harmony:harmony:1.1.9"
        const val startup                  = "androidx.startup:startup-runtime:1.1.1"
        const val composeHtmlText          = "de.charlex.compose.material3:material3-html-text:2.0.0-beta01"
        const val radialgamepad            = "com.github.Swordfish90:RadialGamePad:${versions.radialgamepad}"
        const val libretrodroid            = "com.github.Swordfish90:LibretroDroid:${versions.libretrodroid}"

        const val lottie = "com.airbnb.android:lottie-compose:${versions.lottie}"

        const val retrofitLogging          = "com.squareup.okhttp3:logging-interceptor:4.6.0"
        const val gsonAnnotations          = "com.google.code.gson:gson:2.9.0"
        const val retrofitGsonConverter    = "com.squareup.retrofit2:converter-gson:${versions.retrofit}"

    }

    object plugins {
        const val android = "com.android.tools.build:gradle:8.4.0"
        const val navigationSafeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:${versions.navigation}"
        const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${versions.kotlin}"
    }
}
