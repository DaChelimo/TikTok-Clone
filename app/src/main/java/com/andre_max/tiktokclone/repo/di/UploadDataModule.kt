/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.di

import android.content.Context
import com.andre_max.tiktokclone.BuildConfig
import com.andre_max.tiktokclone.domain.upload.VideoCompressor
import com.andre_max.tiktokclone.domain.upload.VideoStorageRepo
import com.andre_max.tiktokclone.repo.local.media.DefaultLocalMediaRepo
import com.andre_max.tiktokclone.repo.local.media.LocalMediaRepo
import com.andre_max.tiktokclone.repo.local.record.DefaultRecordVideoRepo
import com.andre_max.tiktokclone.repo.local.record.RecordVideoRepo
import com.andre_max.tiktokclone.repo.local.space.DefaultLocalSpaceRepo
import com.andre_max.tiktokclone.repo.local.space.DefaultVideoCompressor
import com.andre_max.tiktokclone.repo.local.space.LocalSpaceRepo
import com.andre_max.tiktokclone.repo.network.storage.R2VideoStorageRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/** Binds the upload vertical's data-layer interfaces (device media/storage + Cloudflare R2). */
@Module
@InstallIn(SingletonComponent::class)
object UploadDataModule {

    /**
     * Timeouts are per-phase, not one call timeout: a video PUT legitimately runs for minutes on a
     * slow connection, while a dead endpoint should still fail fast at connect.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.MINUTES)
        .build()

    /** Lenient about unknown keys so the Worker can add response fields without breaking clients. */
    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideVideoStorageRepo(
        httpClient: OkHttpClient,
        json: Json,
    ): VideoStorageRepo = R2VideoStorageRepo(
        workerBaseUrl = BuildConfig.R2_WORKER_BASE_URL,
        httpClient = httpClient,
        json = json,
    )

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
