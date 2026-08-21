/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.basic_phone

import com.andre_max.tiktokclone.presentation.core.UiText

/**
 * Screen 5's state contract. Ends at "verification code requested" — the enter-code screen is
 * out of scope (D2).
 *
 * Lives in a new package because the legacy `phone_auth/enter_phone_number` one still holds the
 * `BasicPhoneViewModel` that `fragment_basic_phone.xml` data-binds to (non-negotiable #2).
 */
data class BasicPhoneUiState(
    /** Dial code from the country picker, e.g. `+254`. */
    val countryCode: String = "",
    /** Subscriber part as typed, without the country code. */
    val phoneNumber: String = "",
    val error: UiText? = null,
    val isContinueEnabled: Boolean = false,
    val isLoading: Boolean = false,
)

sealed interface BasicPhoneAction {
    data class CountryCodeChanged(val value: String) : BasicPhoneAction
    data class PhoneNumberChanged(val value: String) : BasicPhoneAction
    data object ContinueClicked : BasicPhoneAction
    data object BackClicked : BasicPhoneAction
}

/** Test/preview tags for the nodes this screen must expose. */
object BasicPhoneScreenTags {
    const val COUNTRY_CODE = "basic_phone_country_code"
    const val PHONE_FIELD = "basic_phone_field"
    const val ERROR = "basic_phone_error"
    const val CONTINUE = "basic_phone_continue"
    const val BACK = "basic_phone_back"
    const val LOADING = "basic_phone_loading"
}
