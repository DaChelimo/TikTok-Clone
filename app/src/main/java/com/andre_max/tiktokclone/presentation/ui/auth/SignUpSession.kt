/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth

import com.andre_max.tiktokclone.domain.auth.usecase.SignUpCredentials
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

/**
 * Carries what the user has entered so far across the sign-up screens, up to the create-username
 * screen where `CompleteSignUpUseCase` finally consumes it.
 *
 * **In-memory only, deliberately.** The obvious alternative — passing the credentials as nav
 * arguments — writes the password into a `Bundle`, which means it lands in saved-instance-state
 * and in back-stack dumps. Holding it here instead means the password exists only in the heap,
 * for exactly as long as the flow does.
 *
 * The cost of that choice is that a process death loses the in-progress sign-up and the user
 * restarts the flow. That is the right trade for a handful of screens: the alternative persists
 * a plaintext password to disk to save re-typing it.
 *
 * `@ActivityRetainedScoped` rather than `@Singleton` so it dies with the Activity — a singleton
 * would keep a password alive for the whole process, including long after the user reached Home.
 */
@ActivityRetainedScoped
class SignUpSession @Inject constructor() {

    /** The email collected on screen 2, needed by screen 3 for the identity-containment rule. */
    var email: String? = null
        private set

    var credentials: SignUpCredentials? = null
        private set

    fun setEmail(email: String) {
        this.email = email
    }

    fun setCredentials(credentials: SignUpCredentials) {
        this.credentials = credentials
    }

    /**
     * Drops everything held. Called once sign-up completes, and whenever the flow is abandoned,
     * so a password never outlives the screens that needed it.
     */
    fun clear() {
        email = null
        credentials = null
    }
}
