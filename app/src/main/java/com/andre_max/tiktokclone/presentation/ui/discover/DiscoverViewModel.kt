/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.domain.core.fold
import com.andre_max.tiktokclone.domain.tags.TagsRepo
import com.andre_max.tiktokclone.domain.tags.usecase.GetTagVideosUseCase
import com.andre_max.tiktokclone.models.tag.Tag
import com.andre_max.tiktokclone.models.video.RemoteVideo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val tagRows: ImmutableList<DiscoverTagUi> = persistentListOf(),
    val isLoading: Boolean = false,
)

/** One discover row: the tag and its videos, filled in as the per-tag fetch lands. */
data class DiscoverTagUi(
    val tag: Tag,
    val videos: ImmutableList<RemoteVideo> = persistentListOf(),
)

/** Popular tags, each row's videos loaded per tag. */
@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val tagsRepo: TagsRepo,
    private val getTagVideos: GetTagVideosUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState(isLoading = true))
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val tags = tagsRepo.fetchPopularTags().fold({ it }, { emptyList() })
            _uiState.update { state ->
                state.copy(
                    tagRows = tags.map { DiscoverTagUi(tag = it) }.toImmutableList(),
                    isLoading = false,
                )
            }

            tags.forEach { tag ->
                launch {
                    val videos = getTagVideos(tag.name).fold({ it }, { emptyList() })
                    _uiState.update { state ->
                        state.copy(
                            tagRows = state.tagRows.map { row ->
                                if (row.tag.name == tag.name) {
                                    row.copy(videos = videos.toImmutableList())
                                } else row
                            }.toImmutableList(),
                        )
                    }
                }
            }
        }
    }
}
