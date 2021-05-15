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

import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.injectAndroidCore() {
    implementation(Libraries.kotlinStandardLibrary)
    implementation(Libraries.appCompat)
    implementation(Libraries.ktxCore)
    implementation(Libraries.constraintLayout)
    implementation(Libraries.materialComponents)
    implementation(Libraries.navigationFragment)
    implementation(Libraries.navigationUI)
    implementation(Libraries.timber)
    implementation(Libraries.ssp)
    implementation(Libraries.sdp)
}

fun DependencyHandler.injectCommonAuth() {
    implementation(Libraries.facebook)
    implementation(Libraries.twitter)
    implementation(Libraries.gms)
}

fun DependencyHandler.injectViewModel() {
    implementation(Libraries.liveData)
    implementation(Libraries.viewModel)
    implementation(Libraries.lifecycleCommon)
    implementation(Libraries.lifecycleExtensions)
}

fun DependencyHandler.injectCoroutines() {
    implementation(Libraries.coroutinesCore)
    implementation(Libraries.coroutinesAndroid)
    implementation(Libraries.coroutinesForGMS)
    testImplementation(TestLibraries.coroutines)
    androidTestImplementation(TestLibraries.coroutines)
}

fun DependencyHandler.injectHilt() {
    implementation(Libraries.hiltAndroid)
    kapt(KaptLibraries.hilt)

    implementation(Libraries.hiltViewModel)
    kapt(KaptLibraries.hiltViewModel)

    testImplementation(TestLibraries.hilt)
    kaptTest(KaptLibraries.hilt)

    androidTestImplementation(TestLibraries.hilt)
    kaptAndroidTest(KaptLibraries.hilt)
}

fun DependencyHandler.injectCommonFirebase() {
    implementation(platform(Libraries.firebaseBOM))
    implementation(Libraries.firebaseAuth)
    implementation(Libraries.firebaseDatabase)
    implementation(Libraries.firebaseStorage)
    implementation(Libraries.firebaseMessaging)
    implementation(Libraries.firebaseAnalytics)
    implementation(Libraries.firebaseCrashlytics)
}

fun DependencyHandler.injectCommonJVMTest() {
    testImplementation(TestLibraries.junit4)
    testImplementation(TestLibraries.truth)
    testImplementation(TestLibraries.mockitoKotlin)
    testImplementation(TestLibraries.robolectric)
    testImplementation(TestLibraries.testCoreKtx)
    testImplementation(TestLibraries.archCore)
}

fun DependencyHandler.injectCommonAndroidTest() {
    implementation(TestLibraries.androidTestCore)
    implementation(TestLibraries.androidEspressoContrib)
    debugImplementation(TestLibraries.androidFragmentTesting)
    androidTestImplementation(TestLibraries.junit4)
    androidTestImplementation(TestLibraries.androidJunit)
    androidTestImplementation(TestLibraries.testRunner)
    androidTestImplementation(TestLibraries.annotation)
    androidTestImplementation(TestLibraries.androidEspresso)
}