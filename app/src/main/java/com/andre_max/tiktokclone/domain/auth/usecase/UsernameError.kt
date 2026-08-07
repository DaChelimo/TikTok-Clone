/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

import com.andre_max.tiktokclone.domain.core.AppError
import com.andre_max.tiktokclone.domain.core.DataError

/**
 * Reasons a username can be rejected.
 *
 * These mirror the rules actually enforced today by `DefaultNameRepo.getErrorFromUsername`
 * (recorded in `docs/plans/ONBOARDING_FEATURE_SPEC.md` §4.4) — they are **not** a textbook
 * username rule set. The only structural rule the legacy code has is "no spaces"; there is no
 * general allowed-character rule, which is flagged rather than silently changed.
 *
 * Declaration order is the order [ValidateUsernameUseCase] reports failures in.
 */
sealed interface UsernameError : AppError {
    /** Blank input. Legacy folded this into the length rule; split out so the UI can stay quiet
     * before the first keystroke. */
    data object Empty : UsernameError

    /** Fewer than [ValidateUsernameUseCase.MIN_LENGTH] characters. */
    data object TooShort : UsernameError

    /** [ValidateUsernameUseCase.MAX_LENGTH] characters or more (legacy bound is exclusive). */
    data object TooLong : UsernameError

    /** Contains a space character. */
    data object ContainsSpaces : UsernameError

    /**
     * The availability check itself failed. Carries the mapped data-layer error so the
     * ViewModel can tell "taken" (a `Success(false)`) apart from "we could not find out".
     */
    data class Network(val error: DataError.Network) : UsernameError
}
