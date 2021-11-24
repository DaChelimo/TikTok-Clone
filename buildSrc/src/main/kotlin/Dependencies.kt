/*
 * MIT License
 *
 * Copyright (c) 2021 Andrew Chelimo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

object Versions {

    //Version codes for all the libraries
    const val kotlin = "1.6.0"
    const val buildToolsVersion = "4.1.1"
    const val appCompat = "1.3.0-rc01"
    const val constraintLayout = "2.1.0-beta02"
    const val ktx = "1.6.0-alpha02"
    const val material = "1.4.0-alpha02"
    const val coroutines = "1.4.3"
    const val coroutinesForPlayServices = "1.4.1"
    const val hilt = "2.35.1"
    const val hiltViewModel = "1.0.0-alpha03"
    const val androidXHiltCompiler = "1.0.0"
    const val firebaseBOM = "28.0.0"
    const val lifecycle = "2.3.1"
    const val lifecycleExtensions = "2.2.0"
    const val navigation = "2.3.5"
    const val videoCompressor = "0.1.0-alpha"
    const val facebook = "5.0.0"
    const val twitter = "3.0.0"
    const val gms = "19.0.0"
    const val ccp = "2.4.7"
    const val exoplayer = "2.13.3"
    const val timber = "4.7.1"
    const val circleImageView = "3.1.0"
    const val groupie = "2.9.0"
    const val editCodeView = "1.0.6"
    const val dexter = "6.2.2"
    const val glide = "4.11.0"
    const val scalableDimen = "1.0.6"
    const val cameraView = "2.6.4"
    const val swipeToRefresh = "1.1.0"

    //Version codes for all the JVM test libraries
    const val junit4 = "4.13.2"
    const val testRunner = "1.3.1-alpha03"
    const val espresso = "3.4.0-alpha03"
    const val annotation = "1.3.0-alpha01"
    const val mockitoKotlin = "2.2.0"
    const val truth = "1.1.2"
    const val archCore = "2.1.0"
    const val robolectric = "4.5.1"
    const val testCoreKtx = "1.3.0"
    const val androidJunit = "1.1.2"
    const val androidEspresso = "3.3.0"
    const val androidTestCore = "1.3.0"
    const val androidArchCore = "2.1.0"
    const val fragmentTesting = "1.3.3"

    // Gradle Plugins
    const val ktlint = "10.0.0"
    const val detekt = "1.17.0-RC2"
    const val spotless = "5.12.4"
    const val dokka = "1.4.32"
    const val gradleVersionsPlugin = "0.38.0"
    const val jacoco = "0.8.4"
    const val crashlytics = "2.6.0"
    const val safeArgs = "2.3.5"
}

object BuildPlugins {
    //All the build plugins are added here
    const val androidLibrary = "com.android.library"
    const val ktlintPlugin = "org.jlleitschuh.gradle.ktlint"
    const val detektPlugin = "io.gitlab.arturbosch.detekt"
    const val spotlessPlugin = "com.diffplug.spotless"
    const val dokkaPlugin = "org.jetbrains.dokka"
    const val androidApplication = "com.android.application"
    const val kotlinAndroid = "org.jetbrains.kotlin.android"
    const val kotlinKapt = "kotlin-kapt"
    const val kotlinParcelizePlugin = "org.jetbrains.kotlin.plugin.parcelize"
    const val gradleVersionsPlugin = "com.github.ben-manes.versions"
    const val jacocoAndroid = "com.hiya.jacoco-android"

    const val hilt = "dagger.hilt.android.plugin"
    const val googleServices = "com.google.gms.google-services"
    const val firebaseCrashlytics = "com.google.firebase.crashlytics"
    const val safeArgs = "androidx.navigation.safeargs.kotlin"
}

object Libraries {
    //Core
    const val kotlinStandardLibrary = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val constraintLayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val ktxCore = "androidx.core:core-ktx:${Versions.ktx}"
    const val materialComponents = "com.google.android.material:material:${Versions.material}"
    const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    const val navigationUI = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"

    // ViewModel
    const val lifecycleCommon = "androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycle}"
    const val liveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}"
    const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val lifecycleExtensions = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycleExtensions}"

    // Coroutines
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    const val coroutinesForGMS = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.coroutinesForPlayServices}"

    // Hilt
    const val hiltAndroid = "com.google.dagger:hilt-android:${Versions.hilt}"
    const val hiltViewModel = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hiltViewModel}"

    // Firebase
    const val firebaseBOM = "com.google.firebase:firebase-bom:${Versions.firebaseBOM}"
    const val firebaseAuth = "com.google.firebase:firebase-auth-ktx"
    const val firebaseDatabase = "com.google.firebase:firebase-database-ktx"
    const val firebaseStorage = "com.google.firebase:firebase-storage-ktx"
    const val firebaseMessaging = "com.google.firebase:firebase-messaging-ktx"
    const val firebaseAnalytics = "com.google.firebase:firebase-analytics-ktx"
    const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics-ktx"

    // Auth
    const val twitter = "com.twitter.sdk.android:twitter-core:${Versions.twitter}"
    const val facebook = "com.facebook.android:facebook-android-sdk:${Versions.facebook}"
    const val gms = "com.google.android.gms:play-services-auth:${Versions.gms}"

    // Video
    const val cameraView = "com.otaliastudios:cameraview:${Versions.cameraView}"
    const val exoplayer = "com.google.android.exoplayer:exoplayer:${Versions.exoplayer}"
    const val videoCompressor = "com.github.Andre-max:VideoCompressor:${Versions.videoCompressor}"

    // Custom views
    const val ccp = "com.hbb20:ccp:${Versions.ccp}"
    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val swipeToRefresh = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeToRefresh}"
    const val editCodeView = "com.bigbangbutton:editcodeview:${Versions.editCodeView}"
    const val circleImageView = "de.hdodenhof:circleimageview:${Versions.circleImageView}"

    // Recyclerview Adapter
    const val groupie = "com.github.lisawray.groupie:groupie:${Versions.groupie}"
    const val groupieViewBinding = "com.github.lisawray.groupie:groupie-viewbinding:${Versions.groupie}"

    // Scalable dimensions
    const val ssp = "com.intuit.ssp:ssp-android:${Versions.scalableDimen}"
    const val sdp = "com.intuit.sdp:sdp-android:${Versions.scalableDimen}"

    // Utility
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
    const val dexter = "com.karumi:dexter:${Versions.dexter}"
}

object TestLibraries {
    //Common for both JVM and Instrumentation
    const val junit4 = "junit:junit:${Versions.junit4}"
    const val testRunner = "androidx.test:runner:${Versions.testRunner}"
    const val annotation = "androidx.annotation:annotation:${Versions.annotation}"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    const val hilt = "com.google.dagger:hilt-android-testing:${Versions.hilt}"
    const val truth = "com.google.truth:truth:${Versions.truth}"

    // JVM tests only
    const val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockitoKotlin}"
    const val archCore = "androidx.arch.core:core-testing:${Versions.archCore}"
    const val robolectric = "org.robolectric:robolectric:${Versions.robolectric}"
    const val testCoreKtx = "androidx.test:core-ktx:${Versions.testCoreKtx}"

    // Instrumentation tests only
    const val androidJunit = "androidx.test.ext:junit:${Versions.androidJunit}"
    const val androidTestCore = "androidx.test:core:${Versions.androidTestCore}"
    const val androidFragmentTesting = "androidx.fragment:fragment-testing:${Versions.fragmentTesting}"
    const val androidEspresso = "androidx.test.espresso:espresso-core:${Versions.androidEspresso}"
    const val androidEspressoContrib = "androidx.test.espresso:espresso-idling-resource:${Versions.espresso}"

}

object KaptLibraries {
    const val glide = "com.github.bumptech.glide:compiler:${Versions.glide}"
    const val hilt = "com.google.dagger:hilt-compiler:${Versions.hilt}"
    const val hiltViewModel = "androidx.hilt:hilt-compiler:${Versions.androidXHiltCompiler}"
}

object AndroidSdk {
    const val minSdkVersion = 17
    const val compileSdkVersion = 30
    const val targetSdkVersion = compileSdkVersion
    const val versionCode = 1
    const val versionName = "0.1.0"
    const val buildToolsVersion = "30.0.3"
}