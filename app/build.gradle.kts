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

plugins {
    id(BuildPlugins.androidApplication)
    id(BuildPlugins.kotlinAndroid)
    id(BuildPlugins.kotlinKapt)
    id(BuildPlugins.kotlinParcelizePlugin)
    id(BuildPlugins.ktlintPlugin)
    id(BuildPlugins.jacocoAndroid)

    id(BuildPlugins.googleServices)
    id(BuildPlugins.firebaseCrashlytics)
    id(BuildPlugins.safeArgs)
}

jacoco {
    toolVersion = Versions.jacoco
}

android {
    compileSdkVersion(AndroidSdk.compileSdkVersion)
    buildToolsVersion(AndroidSdk.buildToolsVersion)

    buildFeatures.dataBinding = true
    buildFeatures.viewBinding = true

    val facebookAppId: String by project
    val facebookLoginProtocolScheme: String by project

    val twitterConsumerKey: String by project
    val twitterConsumerSecret: String by project

    val googleWebClientId: String by project

    defaultConfig {
        applicationId = "com.andre_max.tiktokclone"
        minSdkVersion(AndroidSdk.minSdkVersion)
        targetSdkVersion(AndroidSdk.targetSdkVersion)
        versionCode = AndroidSdk.versionCode
        versionName = AndroidSdk.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true

        // Inject the Facebook API key and login protocol scheme into the manifest
        manifestPlaceholders["facebook_app_id"] = facebookAppId
        manifestPlaceholders["fb_login_protocol_scheme"] = facebookLoginProtocolScheme
    }

    testOptions {
        animationsDisabled = true
        unitTests.apply {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xallow-result-return-type")
    }

    buildTypes {
        forEach {
            // Facebook
            it.buildConfigField("String", "FACEBOOK_APP_ID", "\"$facebookAppId\"")

            // Google
            it.buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")

            // Twitter
            it.buildConfigField("String", "TWITTER_CONSUMER_KEY", "\"$twitterConsumerKey\"")
            it.buildConfigField("String", "TWITTER_CONSUMER_SECRET", "\"$twitterConsumerSecret\"")
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    injectAndroidCore()
    injectViewModel()
    injectCoroutines()
    injectCommonAuth()
    injectCommonFirebase()
    injectCommonJVMTest()
    injectCommonAndroidTest()

    implementation(Libraries.cameraView)
    implementation(Libraries.exoplayer)
    implementation(Libraries.videoCompressor)

    implementation(Libraries.ccp)
    implementation(Libraries.swipeToRefresh)
    implementation(Libraries.editCodeView)
    implementation(Libraries.circleImageView)

    implementation(Libraries.glide)
    kapt(KaptLibraries.glide)

    implementation(Libraries.groupie)
    implementation(Libraries.groupieViewBinding)

    implementation(Libraries.ssp)
    implementation(Libraries.sdp)

    implementation(Libraries.kotlinReflect)
    implementation(Libraries.timber)
    implementation(Libraries.dexter)
}