/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.repo.di

import com.andre_max.tiktokclone.domain.comments.CommentsRepo
import com.andre_max.tiktokclone.domain.search.SuggestionsRepo
import com.andre_max.tiktokclone.domain.tags.TagsRepo
import com.andre_max.tiktokclone.domain.user.UserRepo
import com.andre_max.tiktokclone.domain.videos.VideosRepo
import com.andre_max.tiktokclone.repo.network.comment.FirebaseCommentsRepo
import com.andre_max.tiktokclone.repo.network.suggestions.FirebaseSuggestionsRepo
import com.andre_max.tiktokclone.repo.network.tag.FirebaseTagsRepo
import com.andre_max.tiktokclone.repo.network.user.FirebaseUserRepo
import com.andre_max.tiktokclone.repo.network.videos.FirebaseVideosRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the data-layer interfaces the feed vertical introduced. [UserRepo] and [CommentsRepo]
 * are not feed-specific — profile and the video-detail screen reuse them as those slices
 * migrate.
 */
@Module
@InstallIn(SingletonComponent::class)
object FeedDataModule {

    @Provides
    @Singleton
    fun provideVideosRepo(): VideosRepo = FirebaseVideosRepo()

    @Provides
    @Singleton
    fun provideUserRepo(): UserRepo = FirebaseUserRepo()

    @Provides
    @Singleton
    fun provideCommentsRepo(): CommentsRepo = FirebaseCommentsRepo()

    @Provides
    @Singleton
    fun provideTagsRepo(): TagsRepo = FirebaseTagsRepo()

    @Provides
    @Singleton
    fun provideSuggestionsRepo(): SuggestionsRepo = FirebaseSuggestionsRepo()
}
