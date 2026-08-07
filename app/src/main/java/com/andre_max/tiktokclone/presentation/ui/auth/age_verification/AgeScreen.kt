/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.age_verification

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.core.asString
import com.andre_max.tiktokclone.presentation.ui.theme.TikTokTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Stateless screen 1 — a pure function of [state] plus a single [onAction] sink, so it is
 * `@Preview`-able and testable with no ViewModel and no Hilt.
 *
 * Layout follows `docs/designs/onboarding/01-age-verification.png`: a "Sign up" bar, the birthday
 * prompt, the chosen date on an underlined field, the Next button, and a month/day/year wheel
 * pinned to the bottom. The legacy `fragment_age.xml` is consulted only for *behavior* — it put a
 * spinner `DatePicker` mid-screen with the button beneath it, which is not this layout.
 */
@Composable
fun AgeScreen(
    state: AgeUiState,
    onAction: (AgeAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        AgeTopBar(onBackClick = { onAction(AgeAction.BackClicked) })

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.when_is_your_birthday),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.dark_black),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.your_birthday_won_t_be_shown_publicly),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(R.color.medium_grey),
                )
            }
            // Decorative stand-in for the design's birthday-cake illustration: the app ships no
            // such asset, and drawing one is outside this slice.
            Text(
                text = "🎂",
                modifier = Modifier.clearAndSetSemantics { },
                style = MaterialTheme.typography.displaySmall,
            )
        }

        SelectedBirthdayField(dateOfBirth = state.dateOfBirth)

        AgeErrorText(error = state.error)

        Button(
            onClick = { onAction(AgeAction.ContinueClicked) },
            enabled = state.isContinueEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(52.dp)
                .testTag(AgeScreenTags.CONTINUE),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.pinkBtnBackground),
                contentColor = Color.White,
                disabledContainerColor = colorResource(R.color.pinkBtnBackground)
                    .copy(alpha = 0.4f),
                disabledContentColor = Color.White,
            ),
        ) {
            Text(
                text = stringResource(R.string.next),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        BirthdayWheelPicker(
            dateOfBirth = state.dateOfBirth,
            onDateChanged = { onAction(AgeAction.DateChanged(it)) },
            modifier = Modifier.padding(bottom = 32.dp),
        )
    }
}

@Composable
private fun AgeTopBar(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .testTag(AgeScreenTags.BACK),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.back_button),
                tint = colorResource(R.color.dark_black),
            )
        }
        Text(
            text = stringResource(R.string.sign_up),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 12.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.dark_black),
        )
    }
}

/** The underlined read-only field: the design's placeholder until a date exists, then the date. */
@Composable
private fun SelectedBirthdayField(dateOfBirth: LocalDate?) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = dateOfBirth?.format(birthdayFormatter()) ?: stringResource(R.string.birthday),
            style = MaterialTheme.typography.titleMedium,
            color = colorResource(
                if (dateOfBirth == null) R.color.medium_grey else R.color.dark_black
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = colorResource(R.color.light_grey))
    }
}

@Composable
private fun AgeErrorText(error: UiText?) {
    if (error == null) return

    Text(
        text = error.asString(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .testTag(AgeScreenTags.ERROR),
        style = MaterialTheme.typography.bodyMedium,
        // Not MaterialTheme.colorScheme.error: this screen paints on a hardcoded white surface, so
        // under a dark system theme the theme's error colour (#FFB4AB) would land on white at
        // 1.7:1 and vanish. A static resource keeps it legible whatever the system theme is.
        color = colorResource(R.color.dark_red),
    )
}

// region Wheel picker

@Composable
private fun birthdayFormatter(): DateTimeFormatter =
    remember { DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault()) }

/** Where the wheels sit before the user has picked anything; the field still reads "Birthday". */
private val DefaultWheelDate: LocalDate = LocalDate.of(2000, 1, 1)

private const val EARLIEST_YEAR = 1900
private val WheelItemHeight = 48.dp

/**
 * The design's three-column month/day/year wheel. Compose has no built-in wheel picker, and
 * Material 3's `DatePicker` is a calendar — a different control from the one the design specifies.
 */
@Composable
private fun BirthdayWheelPicker(
    dateOfBirth: LocalDate?,
    onDateChanged: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initial = dateOfBirth ?: DefaultWheelDate
    val latestYear = remember { LocalDate.now().year }

    val monthState = rememberLazyListState(initialFirstVisibleItemIndex = initial.monthValue - 1)
    val dayState = rememberLazyListState(initialFirstVisibleItemIndex = initial.dayOfMonth - 1)
    val yearState = rememberLazyListState(
        initialFirstVisibleItemIndex = initial.year - EARLIEST_YEAR
    )

    val months = remember {
        Month.entries
            .map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
            .toImmutableList()
    }
    val years = remember(latestYear) {
        (EARLIEST_YEAR..latestYear).map(Int::toString).toImmutableList()
    }
    // The day column is truncated to the selected month's real length, so February never offers
    // a 30th and a non-leap year never offers a 29th.
    val days = remember(monthState.firstVisibleItemIndex, yearState.firstVisibleItemIndex) {
        val year = wheelYear(yearState, latestYear)
        val length = YearMonth.of(year, wheelMonth(monthState)).lengthOfMonth()
        (1..length).map(Int::toString).toImmutableList()
    }

    ReportSettledDate(monthState, dayState, yearState, latestYear, onDateChanged)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .testTag(AgeScreenTags.DATE_PICKER),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        WheelColumn(
            labels = months,
            listState = monthState,
            columnLabel = stringResource(R.string.birthday_month),
            modifier = Modifier.weight(1f),
        )
        WheelColumn(
            labels = days,
            listState = dayState,
            columnLabel = stringResource(R.string.birthday_day),
            modifier = Modifier.weight(1f),
        )
        WheelColumn(
            labels = years,
            listState = yearState,
            columnLabel = stringResource(R.string.birthday_year),
            modifier = Modifier.weight(1f),
        )
    }
}

private fun wheelMonth(monthState: LazyListState): Int =
    (monthState.firstVisibleItemIndex + 1).coerceIn(1, 12)

private fun wheelYear(yearState: LazyListState, latestYear: Int): Int =
    (EARLIEST_YEAR + yearState.firstVisibleItemIndex).coerceIn(EARLIEST_YEAR, latestYear)

/**
 * Emits a [LocalDate] once the wheels come to rest after the user has moved them.
 *
 * Two things this deliberately does not do. It does not emit while a fling is in flight, so one
 * gesture is one validation rather than one per row passed. And it does not emit the position the
 * wheels *start* in — [DefaultWheelDate] is where the wheels park, not a date the user chose, and
 * treating it as one would light up "Next" over an untouched form.
 */
@Composable
private fun ReportSettledDate(
    monthState: LazyListState,
    dayState: LazyListState,
    yearState: LazyListState,
    latestYear: Int,
    onDateChanged: (LocalDate) -> Unit,
) {
    LaunchedEffect(monthState, dayState, yearState, latestYear, onDateChanged) {
        snapshotFlow {
            monthState.isScrollInProgress ||
                dayState.isScrollInProgress ||
                yearState.isScrollInProgress
        }
            .distinctUntilChanged()
            .filter { isScrolling -> !isScrolling }
            .drop(1) // the wheels' opening position, which no gesture produced
            .collect {
                val year = wheelYear(yearState, latestYear)
                val month = wheelMonth(monthState)
                val lastDay = YearMonth.of(year, month).lengthOfMonth()
                onDateChanged(
                    LocalDate.of(
                        year,
                        month,
                        (dayState.firstVisibleItemIndex + 1).coerceIn(1, lastDay),
                    )
                )
            }
    }
}

@Composable
private fun WheelColumn(
    labels: ImmutableList<String>,
    listState: LazyListState,
    columnLabel: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.height(WheelItemHeight * 3), contentAlignment = Alignment.Center) {
        HorizontalDivider(
            modifier = Modifier.padding(bottom = WheelItemHeight),
            color = colorResource(R.color.light_grey),
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = WheelItemHeight),
            color = colorResource(R.color.light_grey),
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = columnLabel },
            contentPadding = PaddingValues(vertical = WheelItemHeight),
            flingBehavior = rememberSnapFlingBehavior(listState),
        ) {
            items(count = labels.size, key = { index -> labels[index] }) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WheelItemHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = labels[index],
                        // The wheel is announced once as a whole; per-row labels would be noise.
                        modifier = Modifier.clearAndSetSemantics { },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorResource(
                            if (index == listState.firstVisibleItemIndex) {
                                R.color.dark_black
                            } else {
                                R.color.medium_grey
                            }
                        ),
                    )
                }
            }
        }
    }
}

// endregion

@Preview(showBackground = true, name = "Empty — Next disabled")
@Composable
private fun AgeScreenEmptyPreview() {
    TikTokTheme { AgeScreen(state = AgeUiState(), onAction = {}) }
}

@Preview(showBackground = true, name = "Valid date — Next enabled")
@Composable
private fun AgeScreenValidPreview() {
    TikTokTheme {
        AgeScreen(
            state = AgeUiState(
                dateOfBirth = LocalDate.of(1995, 2, 18),
                isContinueEnabled = true,
            ),
            onAction = {},
        )
    }
}

@Preview(showBackground = true, name = "Too young")
@Composable
private fun AgeScreenTooYoungPreview() {
    TikTokTheme {
        AgeScreen(
            state = AgeUiState(
                dateOfBirth = LocalDate.of(2020, 1, 1),
                error = UiText.Resource(R.string.user_too_young),
            ),
            onAction = {},
        )
    }
}

/** Test/preview tags for the nodes this screen must expose. */
object AgeScreenTags {
    const val DATE_PICKER = "age_date_picker"
    const val ERROR = "age_error"
    const val CONTINUE = "age_continue"
    const val BACK = "age_back"
}
