/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.di

import com.andre_max.tiktokclone.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * Configuration for the social sign-in providers, resolved once at graph construction.
 *
 * The two providers are bound as *availability*, not as feature flags someone remembers to
 * flip: each is derived from whether the config it needs is actually present, so a provider
 * turns itself on the moment its setup lands and cannot be switched on while still broken.
 */
@Module
@InstallIn(SingletonComponent::class)
object SocialAuthModule {

    const val GOOGLE_WEB_CLIENT_ID = "googleWebClientId"
    const val IS_FACEBOOK_ENABLED = "isFacebookEnabled"

    /**
     * The OAuth web client id, or null when it cannot work.
     *
     * A Google ID token is only accepted by a Firebase project that issued the client that
     * minted it, and a client id is prefixed with its own project number — so an id whose
     * prefix is not this project's sender id is guaranteed to fail at
     * `signInWithCredential`, which is worth catching here rather than as a runtime error the
     * user sees. `gradle.properties` currently holds an id belonging to a different project.
     */
    @Provides
    @Singleton
    @Named(GOOGLE_WEB_CLIENT_ID)
    fun provideGoogleWebClientId(): String? {
        val configured = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (configured.isBlank()) return null

        val projectNumber = Firebase.app.options.gcmSenderId ?: return null
        return configured.takeIf { it.startsWith("$projectNumber-") }
    }

    /**
     * Facebook login is off pending a Meta developer console step: SDK 12+ requires a
     * `com.facebook.sdk.ClientToken`, which the manifest does not declare, and the token has to
     * come from the console. Wired as a constant rather than a config lookup because there is
     * nothing to look up until that token exists.
     *
     * TODO: enable once `facebook_client_token` is in gradle.properties and the manifest
     *  declares `com.facebook.sdk.ClientToken`.
     */
    @Provides
    @Singleton
    @Named(IS_FACEBOOK_ENABLED)
    fun provideIsFacebookEnabled(): Boolean = false
}
