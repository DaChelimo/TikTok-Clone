/*
 * MIT License
 * Copyright (c) 2021 Andrew Chelimo
 */

package com.andre_max.tiktokclone.presentation.ui.auth.username

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.domain.auth.usecase.CheckUsernameAvailabilityUseCase
import com.andre_max.tiktokclone.domain.auth.usecase.CompleteSignUpUseCase
import com.andre_max.tiktokclone.domain.auth.usecase.UsernameError
import com.andre_max.tiktokclone.domain.auth.usecase.ValidateUsernameUseCase
import com.andre_max.tiktokclone.domain.core.DataError
import com.andre_max.tiktokclone.domain.core.Result
import com.andre_max.tiktokclone.presentation.core.UiText
import com.andre_max.tiktokclone.presentation.ui.auth.AuthEvent
import com.andre_max.tiktokclone.presentation.ui.auth.SignUpSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Screen 4 — the only screen this phase with real I/O.
 *
 * Availability is checked **debounced by [DEBOUNCE_MILLIS] after typing stops**, never per
 * keystroke, and a stale in-flight result must never overwrite a newer query: [usernameChanges]
 * is piped through `debounce` + `flatMapLatest`, so a new keystroke cancels whatever check is
 * still in flight for the previous one rather than racing it.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class CreateUsernameViewModel @Inject constructor(
    private val validateUsername: ValidateUsernameUseCase,
    private val checkAvailability: CheckUsernameAvailabilityUseCase,
    private val completeSignUp: CompleteSignUpUseCase,
    private val signUpSession: SignUpSession,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateUsernameUiState())
    val uiState: StateFlow<CreateUsernameUiState> = _uiState.asStateFlow()

    /** One-shot navigation/error events, backed by a `Channel` — never replayed. */
    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    /**
     * Every username edit, valid or not; the debounce/availability pipeline lives here. `null`
     * means "nothing to check right now" (blank, or shape-invalid).
     *
     * A `StateFlow`, not a `SharedFlow`: the `launchIn` collector below is only *scheduled*
     * when the ViewModel is constructed, not actually running yet (it needs the dispatcher to
     * process it) — a `MutableSharedFlow(replay = 0)` would drop an emission that lands before
     * the collector subscribes. `StateFlow`'s conflation always replays its current value to a
     * new subscriber, so no keystroke can race the collector's startup.
     */
    private val usernameChanges = MutableStateFlow<String?>(null)

    init {
        usernameChanges
            .debounce(DEBOUNCE_MILLIS)
            .flatMapLatest { username ->
                // Going from a valid to an invalid username must also cancel whatever check was
                // still in flight for the old value — flatMapLatest only does that when the
                // *upstream* re-emits, so invalid states have to flow through here too, not be
                // filtered out before this operator, or a stale result could land after the user
                // has already made the field invalid again.
                if (username == null) {
                    emptyFlow()
                } else {
                    flow {
                        _uiState.update { it.copy(isCheckingAvailability = true) }
                        emit(checkAvailability(username))
                    }
                }
            }
            .onEach(::onAvailabilityResult)
            .launchIn(viewModelScope)
    }

    fun onAction(action: CreateUsernameAction) {
        when (action) {
            is CreateUsernameAction.UsernameChanged -> onUsernameChanged(action.value)
            CreateUsernameAction.ContinueClicked -> onContinueClicked()
            CreateUsernameAction.BackClicked -> _events.trySend(AuthEvent.NavigateBack)
        }
    }

    private fun onUsernameChanged(value: String) {
        val shapeErrors = validateUsername(value)

        _uiState.update {
            it.copy(
                username = value,
                error = shapeErrors.firstOrNull()?.let(::errorText),
                isAvailable = null,
                isCheckingAvailability = false,
                isContinueEnabled = false,
            )
        }

        // Empty/blank input never reaches the repo — nothing to check yet, and the shape error
        // for it is deliberately not surfaced (UsernameError.Empty keeps the UI quiet).
        usernameChanges.value = value.takeIf { shapeErrors.isEmpty() }
    }

    private fun onAvailabilityResult(result: Result<Boolean, UsernameError>) {
        _uiState.update {
            it.copy(
                isCheckingAvailability = false,
                isAvailable = (result as? Result.Success)?.data,
                error = when (result) {
                    is Result.Success -> if (result.data) null else UiText.Resource(R.string.name_unavailable)
                    is Result.Error -> errorText(result.error)
                },
                isContinueEnabled = result is Result.Success && result.data,
            )
        }
    }

    /**
     * The account is created here, not on the screen that collected the credentials — see
     * [CompleteSignUpUseCase] for why. This screen is therefore the only one in the flow that
     * can fail for a reason the user cannot fix by editing the field in front of them, which is
     * why its failures surface as a message rather than as field-level state.
     */
    private fun onContinueClicked() {
        val state = _uiState.value
        if (!state.isContinueEnabled || state.isLoading) return

        val credentials = signUpSession.credentials
        if (credentials == null) {
            // Entered without the earlier screens having run (deep link, restored back stack).
            _events.trySend(AuthEvent.ShowError(UiText.Resource(R.string.error_during_account_creation)))
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = completeSignUp(credentials, state.username)

            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is Result.Success -> {
                    signUpSession.clear()
                    _events.trySend(AuthEvent.NavigateToHome)
                }

                is Result.Error -> _events.trySend(AuthEvent.ShowError(signUpErrorText(result.error)))
            }
        }
    }

    private fun signUpErrorText(error: DataError): UiText = when (error) {
        DataError.Auth.EMAIL_ALREADY_IN_USE -> UiText.Resource(R.string.email_already_in_use)
        DataError.Auth.NO_INTERNET, DataError.Network.NO_INTERNET ->
            UiText.Resource(R.string.no_network_connection)
        DataError.Auth.TOO_MANY_REQUESTS -> UiText.Resource(R.string.too_many_requests)
        else -> UiText.Resource(R.string.error_during_account_creation)
    }

    private fun errorText(error: UsernameError): UiText? = when (error) {
        UsernameError.Empty -> null
        UsernameError.TooShort -> UiText.Resource(R.string.short_username)
        UsernameError.TooLong -> UiText.Resource(R.string.long_username)
        UsernameError.ContainsSpaces -> UiText.Resource(R.string.name_contains_spaces)
        is UsernameError.Network -> UiText.Resource(R.string.username_check_failed)
    }

    companion object {
        const val DEBOUNCE_MILLIS = 400L
    }
}
