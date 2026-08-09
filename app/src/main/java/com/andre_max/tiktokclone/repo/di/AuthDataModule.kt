/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.di

import android.app.Application
import com.andre_max.tiktokclone.domain.auth.AuthRepo
import com.andre_max.tiktokclone.domain.auth.PhoneAuthRepo
import com.andre_max.tiktokclone.domain.auth.UserProfileRepo
import com.andre_max.tiktokclone.domain.auth.UsernameRepo
import com.andre_max.tiktokclone.repo.network.auth.DefaultAuthRepo
import com.andre_max.tiktokclone.repo.network.phone.ActivityProvider
import com.andre_max.tiktokclone.repo.network.phone.DefaultPhoneAuthRepo
import com.andre_max.tiktokclone.repo.network.phone.ResumedActivityProvider
import com.andre_max.tiktokclone.repo.network.user.DefaultUserProfileRepo
import com.andre_max.tiktokclone.repo.network.username.DefaultUsernameRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the auth data-layer interfaces that live in `domain` to their Firebase implementations.
 *
 * The first Hilt module in the project: the auth ViewModels are the first to take repo
 * interfaces by constructor injection, and Dagger cannot construct an interface. Legacy repos
 * (`AuthRepo`, `NameRepo`, `UserRepo`) are still built inline by the legacy ViewModels and are
 * deliberately not bound here.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthDataModule {

    @Provides
    @Singleton
    fun provideUsernameRepo(): UsernameRepo = DefaultUsernameRepo()

    @Provides
    @Singleton
    fun provideAuthRepo(): AuthRepo = DefaultAuthRepo()

    @Provides
    @Singleton
    fun provideUserProfileRepo(): UserProfileRepo = DefaultUserProfileRepo()

    /**
     * Firebase's `PhoneAuthOptions.setActivity` needs the resumed `Activity`. The point of this
     * seam is that `PhoneAuthRepo` in `domain` never learns about it.
     */
    @Provides
    @Singleton
    fun provideActivityProvider(application: Application): ActivityProvider =
        ResumedActivityProvider(application)

    @Provides
    @Singleton
    fun providePhoneAuthRepo(activityProvider: ActivityProvider): PhoneAuthRepo =
        DefaultPhoneAuthRepo(activityProvider)
}
