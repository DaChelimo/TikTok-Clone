/*
 * MIT License
 *
 * Copyright (c) 2021 Andre-max
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andre_max.tiktokclone.presentation.ui.auth.email_auth.enter_email

import androidx.lifecycle.ViewModel
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.auth.usecase.EmailError
import com.andre_max.tiktokclone.domain.auth.usecase.ValidateEmailUseCase
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.presentation.ui.auth.SignUpSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * Screen 2. Retrofitted to the conventions the other five screens copy (spec §4.2): the
 * password fields that used to live in this file were dead code belonging to screen 3, and
 * `shouldNavigate` / `resetShouldNavigate` are replaced by one-shot [AuthEvent]s.
 *
 * [BasicEmailUiState] and [BasicEmailAction] live in `BasicEmailContract.kt`.
 */
@HiltViewModel
class BasicEmailViewModel @Inject constructor(
    private val validateEmail: ValidateEmailUseCase,
    private val signUpSession: SignUpSession,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BasicEmailUiState())
    val uiState: StateFlow<BasicEmailUiState> = _uiState.asStateFlow()

    /** One-shot navigation/error events, backed by a `Channel` — never replayed. */
    private val _events = Channel<AuthEvent>(Channel.BUFFERED)

    /**
     * `onStart` re-arms [isNavigating]: this ViewModel outlives its composition (the page
     * Fragment stays in the host's child `FragmentManager` while screen 3 sits on top), so a
     * fresh collector means the user is back on this screen and may legitimately continue again.
     */
    val events: Flow<AuthEvent> = _events.receiveAsFlow().onStart { isNavigating = false }

    /**
     * True from the moment [AuthEvent.NavigateToPassword] is queued until the screen is
     * re-entered. Repeated Continue taps land in the same frame, *before* the Route's collector
     * runs, so the channel is not a gate on its own: without this flag a double tap queues two
     * navigation events and the second `navigate()` executes from a destination that no longer
     * owns the action.
     *
     * Deliberately not a [BasicEmailUiState] field — `isLoading` means "async work in flight",
     * and this screen does none; leaving it switched on would also survive into the back-entry
     * above and permanently disable the button.
     */
    private var isNavigating = false

    fun onAction(action: BasicEmailAction) {
        when (action) {
            is BasicEmailAction.EmailChanged -> onEmailChanged(action.value)
            BasicEmailAction.ContinueClicked -> onContinueClicked()
            BasicEmailAction.BackClicked -> _events.trySend(AuthEvent.NavigateBack)
        }
    }

    /**
     * The raw keystrokes are echoed back into [BasicEmailUiState.email] so the text field stays
     * controlled — surrounding whitespace is only trimmed for *validation* and for the value
     * handed downstream, never silently removed from under the user's cursor.
     */
    private fun onEmailChanged(value: String) {
        val result = validateEmail(value)
        _uiState.update {
            it.copy(
                email = value,
                emailError = result.errorText(),
                isContinueEnabled = result is Result.Success,
            )
        }
    }

    private fun onContinueClicked() {
        if (isNavigating) return

        val email = _uiState.value.email.trim()

        // Re-validated here rather than trusting isContinueEnabled: a disabled button is a UI
        // affordance, not a gate, so the rule has to hold in the ViewModel too.
        when (val result = validateEmail(email)) {
            is Result.Success -> {
                isNavigating = true
                signUpSession.setEmail(email)
                _events.trySend(AuthEvent.NavigateToPassword(email))
            }

            is Result.Error -> _uiState.update {
                it.copy(emailError = result.errorText(), isContinueEnabled = false)
            }
        }
    }

    private fun Result<Unit, EmailError>.errorText(): UiText? = when (this) {
        is Result.Success -> null

        is Result.Error -> when (error) {
            EmailError.Empty -> UiText.Resource(R.string.email_required)
            EmailError.InvalidFormat -> UiText.Resource(R.string.enter_valid_email)
        }
    }
}
