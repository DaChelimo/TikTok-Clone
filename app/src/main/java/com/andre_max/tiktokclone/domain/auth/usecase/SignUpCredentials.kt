/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.domain.auth.usecase

/**
 * How the user proved who they are, carried from whichever screen collected it to the
 * create-username screen where the account is finally created.
 *
 * Modelled as a sealed type rather than a bag of nullable fields because the legacy flow used
 * the latter (`args.credential != null` / `args.emailBody != null` / `else -> throw`) and had to
 * throw at runtime for the case the types could have ruled out.
 */
sealed interface SignUpCredentials {

    data class Email(val email: String, val password: String) : SignUpCredentials

    /**
     * [profilePictureUrl] comes from the Google account and seeds the profile; the user can
     * change it later, and a null here is normal rather than an error.
     */
    data class Google(
        val idToken: String,
        val profilePictureUrl: String? = null,
    ) : SignUpCredentials
}
