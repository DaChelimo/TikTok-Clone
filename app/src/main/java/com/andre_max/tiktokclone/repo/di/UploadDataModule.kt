/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.di

import android.content.Context
import com.andre_max.tiktokclone.domain.upload.VideoCompressor
import com.andre_max.tiktokclone.domain.upload.VideoStorageRepo
import com.andre_max.tiktokclone.repo.local.media.DefaultLocalMediaRepo
import com.andre_max.tiktokclone.repo.local.media.LocalMediaRepo
import com.andre_max.tiktokclone.repo.local.record.DefaultRecordVideoRepo
import com.andre_max.tiktokclone.repo.local.record.RecordVideoRepo
import com.andre_max.tiktokclone.repo.local.space.DefaultLocalSpaceRepo
import com.andre_max.tiktokclone.repo.local.space.DefaultVideoCompressor
import com.andre_max.tiktokclone.repo.local.space.LocalSpaceRepo
import com.andre_max.tiktokclone.repo.network.storage.FirebaseVideoStorageRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds the upload vertical's data-layer interfaces (device media/storage + Firebase Storage). */
@Module
@InstallIn(SingletonComponent::class)
object UploadDataModule {

    @Provides
    @Singleton
    fun provideVideoStorageRepo(): VideoStorageRepo = FirebaseVideoStorageRepo()

    @Provides
    @Singleton
    fun provideVideoCompressor(): VideoCompressor = DefaultVideoCompressor()

    @Provides
    @Singleton
    fun provideLocalMediaRepo(@ApplicationContext context: Context): LocalMediaRepo =
        DefaultLocalMediaRepo(context)

    @Provides
    @Singleton
    fun provideRecordVideoRepo(@ApplicationContext context: Context): RecordVideoRepo =
        DefaultRecordVideoRepo(context)

    @Provides
    @Singleton
    fun provideLocalSpaceRepo(): LocalSpaceRepo = DefaultLocalSpaceRepo()
}
