# Onboarding & Auth — Feature Spec (agent-executable)

**Status:** decisions frozen 2026-07-29. Ready for Pass 1.

This is the contract for the onboarding/auth vertical slice. It exists so that three separate
agent sessions (write tests → implement → review) can execute without the human re-deciding
anything mid-flight.

**Read order for any agent touching this feature:** [AGENTS.md](../../AGENTS.md) →
[docs/SPEC.md](../SPEC.md) → this file → the nested `AGENTS.md` for your layer.

Route/history for this slice lives in [ONBOARDING_MIGRATION.md](ONBOARDING_MIGRATION.md).
That file is *how we got here*; this file is *what we build*. Where they disagree, this file
wins (it is newer and its decisions are user-approved).

---

## 1. The three-pass process

Each pass is a **separate session** with a clean context. Prompts live in
[prompts/](prompts/).

| Pass | Session does | Gate to advance |
| --- | --- | --- |
| **0** | Toolchain + foundations (§3). One-off, blocks everything. | `./gradlew assembleDebug` produces an APK that installs and launches |
| **1** | Writes the **entire test suite** for all 6 screens, plus stub classes so it compiles and fails **red** | Every test compiles; every test fails for the *right* reason (`NotImplementedError`, never a compile error) |
| **2** | Implements until **green**. May not change a test or a pinned signature without escalating. | `./gradlew testDebugUnitTest` green; app runs; screenshots captured |
| **3** | Three review personas (§7) run in parallel per screen | All three PASS → human review. Any FAIL → back to Pass 2 with findings |

**Why stubs in Pass 1:** a test that doesn't compile isn't red, it's absent. Stubs that
`TODO()` make red→green mechanically verifiable, which is the whole reason the human can stop
reading diffs line by line.

**Running it.** Passes 1–3 are automated by
[`.claude/workflows/feature-migration-cycle.js`](../../.claude/workflows/feature-migration-cycle.js) —
it writes the red suite once, then for each screen runs implement → 3 personas in parallel →
rework-on-failure → re-review, and stops the run if a screen can't pass. The prompt files in
[prompts/](prompts/) are not replaced by it: the workflow's agents *read* them, so there is one
approved contract rather than an orchestrator improvising each brief.

**Pass 0 stays manual, deliberately.** It's a one-off AGP-8 toolchain upgrade — the step most
likely to cascade, the least likely to be repeated, and the one where an unattended agent's
best move is to stop and ask anyway. Automating a one-time step with a high stall rate is
negative return.

**Approval model (supersedes ONBOARDING_MIGRATION.md's per-screen human gate):** personas gate
every screen automatically; the human reviews once, at the end of the phase. A screen may not
start until the previous screen is persona-PASS — that's what stops one bad template being
copied six times.

---

## 2. Decisions frozen

Do not re-litigate these. They are user-approved as of 2026-07-29.

| # | Decision | Value |
| --- | --- | --- |
| D1 | Toolchain | Bump to **AGP 8.x + Gradle 8.x**. Do not pin `compileSdk` back down. |
| D2 | Scope | **7 screens** (amended 2026-08-07). Age, basic email, enter password, create username, basic phone, select-basic-auth, **sign-up landing / social auth**. **Still out:** enter-code (blocked on OTP dep). |
| D3 | Test-first shape | Spec pins signatures → Pass 1 writes stubs + full red suite → Pass 2 fills bodies only. |
| D4 | Design system | **Material 3.** Add the dependency in Pass 0. |
| D5 | Minimum age | **13**, computed as **true age from full date of birth** (not year-only). |
| D6 | Password rule | Add **username/email containment** check, and return **all** failed rules, not just the first. |
| D7 | Approval | Whole phase runs unattended; personas gate each screen; human reviews at the end. |
| D8 | Screen 4 identity | **Unique username with a debounced availability check** (spec §4.4 stands). Mobbin's "Create nickname" — skippable, non-unique — is *not* what we build; adapt its layout, keep uniqueness. Rationale: matches the existing `NameRepo.doesNameExist` code, and it is the only screen this phase with real I/O. |
| D9 | Password max length | **20 characters**, matching the Mobbin checklist exactly. Adds `PasswordError.TooLong`. |

**D5 and D6 are deliberate behavior changes, not refactors.** The point of this migration is to
fix what the original XML got wrong — a straight port would carry both bugs forward. Record
them in [docs/SPEC.md](../SPEC.md) §5 when they land.

**D2 amendment (2026-08-07):** the original 6-screen scope is complete (all six screens are
Compose, per the checkpoint commits reviewed the same day). The user explicitly chose to pull
sign-up landing into this phase rather than defer it to its own future slice. Its per-screen
contract — the `googleAuthRepo`/`facebookAuthRepo` public-field inversion to fix,
`rememberLauncherForActivityResult` for Google sign-in, removing the deprecated
`onActivityResult` override — is unchanged from [ONBOARDING_MIGRATION.md](ONBOARDING_MIGRATION.md)'s
"Screen 7" notes; no new spec section was authored for it since that write-up already covers
the same ground this file would restate.

---

## 3. Pass 0 — prerequisites (blocking, do first)

### 3a. Human action required

`app/google-services.json` is absent. The Google Services Gradle plugin fails **before** the
Kotlin compiler runs, so without it every `test`/`assemble` task fails for reasons unrelated to
the code — an agent cannot tell red-from-my-test apart from red-from-missing-config. Download
it from the Firebase console into `app/`. It is correctly gitignored; do not commit it.

### 3b. Toolchain (D1)

- AGP `7.4.2` → `8.x`; Gradle wrapper `7.6.4` → matching `8.x`.
- Expect AGP 8 to demand a `namespace` in `app/build.gradle` (replacing the manifest's
  `package`), stricter manifest merging, and possibly `buildConfig true` under `buildFeatures`.
  Fix these in the same pass — they are AGP-8 migration fallout, not new bugs.
- Enable **core library desugaring** (`isCoreLibraryDesugaringEnabled = true` +
  `coreLibraryDesugaring "com.android.tools:desugar_jdk_libs:2.x"`). Required because
  `minSdk 21` cannot use `java.time` otherwise, and the age use case (§4.1) needs `LocalDate`.

### 3c. Missing test/runtime dependencies

None of these exist today. Several conventions the nested `AGENTS.md` files *mandate* are
currently unresolvable without them.

| Dependency | Why it's needed |
| --- | --- |
| `androidx.compose.ui:ui-test-junit4` + `ui-test-manifest` (debug) | [docs/SPEC.md](../SPEC.md) §8 names the stateless `XyzScreen` as a test target. Cannot test a composable without this. |
| `androidx.lifecycle:lifecycle-runtime-compose` | `collectAsStateWithLifecycle()` — mandated by [presentation/AGENTS.md](../../app/src/main/java/com/andre_max/tiktokclone/presentation/AGENTS.md), currently not on the classpath at all |
| `app.cash.turbine:turbine` | `StateFlow`/`Flow` assertions in ViewModel tests |
| `io.mockk:mockk` | Mocking the Firebase boundary in repo tests (mockito-kotlin struggles with suspend + final Firebase classes) |
| `org.jetbrains.kotlinx:kotlinx-collections-immutable` | `ImmutableList` in `UiState` — mandated by presentation/AGENTS.md, dependency absent |
| `androidx.compose.material3:material3` | D4 |
| `androidx.hilt:hilt-navigation-compose` | `hiltViewModel()` inside a `Route` composable |

### 3d. Foundations (architectural boundaries — build these before screen 1)

These qualify as "abstract now even at one caller" per [AGENTS.md](../../AGENTS.md) §5.

```kotlin
// presentation/core/UiText.kt
sealed interface UiText {
    data class Dynamic(val value: String) : UiText
    data class Resource(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
}
@Composable fun UiText.asString(): String   // the ONLY place a UiText is resolved
```

```kotlin
// presentation/ui/theme/TikTokTheme.kt  — Material 3
@Composable fun TikTokTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit)
```

Also in Pass 0: convert auth ViewModels to `@HiltViewModel` + constructor injection.
`BasicEmailViewModel` currently does `ValidateEmailUseCase()` inline — untestable, and every
new screen would copy it.

**Explicitly NOT in Pass 0:** a shared auth component library (`AuthTextField`, `PrimaryButton`).
Do not pre-build it. Extract a component when the **second** screen demonstrably needs the same
one — otherwise you design the wrong API before seeing the real cases ([AGENTS.md](../../AGENTS.md) §5).

---

## 4. Pinned API contracts

**These signatures are the contract between Pass 1 and Pass 2.** Pass 1 writes tests against
them and creates stubs matching them exactly. Pass 2 may not change them without escalating to
the human.

Existing and unchanged: `Result<D, E : AppError>`, `DataError`, `EmptyResult`, `onSuccess`,
`onError`, `map`, `fold` — all in `domain/core/Result.kt`. `ValidateEmailUseCase` / `EmailError`
are already correct; reuse as-is.

### 4.1 Screen 1 — Age verification

Current rule (`AgeFragment.kt`, read before changing — non-negotiable #5): rejects
`date.year > year - 9` as too young **and** `date.year < year - 150` as an invalid year. The
`DatePicker` sets `maxDate = now`, so future dates are blocked by the UI only.

```kotlin
// domain/auth/usecase/AgeError.kt
sealed interface AgeError : AppError {
    data object TooYoung : AgeError          // under 13 (D5)
    data object DateInFuture : AgeError      // was UI-only before; now enforced in domain
    data object ImplausiblyOld : AgeError    // > 150 years, preserves existing upper bound
}

// domain/auth/usecase/ValidateAgeUseCase.kt
class ValidateAgeUseCase {
    operator fun invoke(dateOfBirth: LocalDate, today: LocalDate = LocalDate.now()): Result<Unit, AgeError>
    companion object { const val MIN_AGE = 13; const val MAX_AGE = 150 }
}
```

`today` is a parameter, not a hidden `LocalDate.now()` call, so tests are deterministic.

```kotlin
// presentation/ui/auth/age_verification/AgeUiState.kt
data class AgeUiState(
    val dateOfBirth: LocalDate? = null,
    val error: UiText? = null,
    val isContinueEnabled: Boolean = false,
)
sealed interface AgeAction {
    data class DateChanged(val date: LocalDate) : AgeAction
    data object ContinueClicked : AgeAction
    data object BackClicked : AgeAction
}
```

**Required edge-case tests:** exactly 13 today (allow — boundary is inclusive); 13th birthday
tomorrow (reject); born Dec 31 vs Jan 1 across a year boundary (this is the bug D5 fixes — a
year-only comparison gets these wrong); leap-day birthday (Feb 29) evaluated in a non-leap year;
today itself; 151 years ago.

### 4.2 Screen 2 — Basic email (retrofit)

Use case exists and is correct. This screen is the template the other five copy, so it must
match final conventions. Known gaps to fix (from ONBOARDING_MIGRATION.md §Screen 2): `collectAsState()`
→ `collectAsStateWithLifecycle()`; inline use-case construction → injection; `password` /
`passwordErrorRes` / `isPasswordValid` / `mapPasswordError()` are **dead code in this file** and
must be deleted (they belong to screen 3); `BasicEmailScreenParams` → `state` + `onAction`.

```kotlin
data class BasicEmailUiState(
    val email: String = "",
    val emailError: UiText? = null,
    val isContinueEnabled: Boolean = false,
    val isLoading: Boolean = false,
)
sealed interface BasicEmailAction {
    data class EmailChanged(val value: String) : BasicEmailAction
    data object ContinueClicked : BasicEmailAction
    data object BackClicked : BasicEmailAction
}
```

**Navigation is a one-shot event, not state.** Replace the `shouldNavigate` +
`onResetNavigation` boolean pair with `Channel<AuthEvent>` exposed as
`receiveAsFlow()`. Use this shape on all six screens.

```kotlin
// presentation/ui/auth/AuthEvent.kt
sealed interface AuthEvent {
    data class NavigateToPassword(val email: String) : AuthEvent
    data object NavigateBack : AuthEvent
    data class ShowError(val message: UiText) : AuthEvent
}
```

**Required edge-case tests:** blank; whitespace-only; leading/trailing whitespace (trimmed);
missing `@`; missing TLD; 1-char TLD (rejected by the existing regex); unicode local part;
error clears on retype.

### 4.3 Screen 3 — Enter password

**Contract change (D6).** Current signature returns the first error only and knows nothing about
the user's identity. New:

```kotlin
// domain/auth/usecase/PasswordError.kt  — add one case
sealed interface PasswordError : AppError {
    data object TooShort : PasswordError
    data object NoDigit : PasswordError
    data object NoSpecialCharacter : PasswordError
    data object NoUppercase : PasswordError
    data object NoLowercase : PasswordError
    data object ContainsIdentity : PasswordError   // NEW (D6)
    data object TooLong : PasswordError            // NEW (D9) — over MAX_LENGTH
}

// domain/auth/usecase/ValidatePasswordUseCase.kt
class ValidatePasswordUseCase {
    /** @return empty list when valid; otherwise EVERY rule violated, in declaration order. */
    operator fun invoke(password: String, identity: String? = null): List<PasswordError>
    companion object { const val MIN_LENGTH = 8; const val MAX_LENGTH = 20 }
}
```

Note this drops `Result` for a `List` — a validation that reports *all* failures has no single
error to wrap, and an empty list is an unambiguous "valid". `identity` is the email local-part
or username; matching is **case-insensitive substring**, and skipped when `identity` is null or
shorter than 3 chars (a 2-char name would reject almost every password).

**The existing `ValidatePasswordUseCaseTest` must be rewritten, not extended** — its
`returns the FIRST rule violated` test asserts the exact behavior D6 removes.

```kotlin
data class EnterPasswordUiState(
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val failedRules: ImmutableList<PasswordError> = persistentListOf(),
    val hasBeenEdited: Boolean = false,   // don't show red rules before first keystroke
    val isContinueEnabled: Boolean = false,
    val isLoading: Boolean = false,
)
```

UI renders a **live rule checklist** (every rule always visible, each ticked/unticked), not a
single error string — that is what returning all failures buys.

**Required edge-case tests:** each rule individually; all rules failing at once (assert the full
list and its order); exactly 8 chars (allow); exactly 20 chars (allow); 21 chars (`TooLong`);
password equal to identity; password *containing* identity; case-insensitive containment
(`ANDREW` in `xANDREWx1!` vs identity `andrew`); identity null; identity shorter than 3 chars;
a valid password returning an empty list.

The UI checklist renders the rules we enforce (D6), not Mobbin's three-line copy — the design
governs layout, the decision governs the rule set. See
[../designs/onboarding/README.md](../designs/onboarding/README.md).

### 4.4 Screen 4 — Create username

First screen with real I/O. **`NameRepo.getErrorFromUsername` returns `Int?` — a raw
`R.string` id in the data layer.** That violates [docs/SPEC.md](../SPEC.md) §6 ("nothing below
presentation touches `Context`/`R.string`") and is exactly what this slice fixes.

**Pass 1 must first grep `DefaultNameRepo.getErrorFromUsername` and record the actual current
rules in this file before writing the use case tests** (non-negotiable #5 — do not assume a
textbook username rule). Flag any rule that looks wrong rather than silently changing it.

#### Actual current rules (recorded by Pass 1, 2026-07-30)

Verbatim from `repo/network/name/DefaultNameRepo.kt`:

```kotlin
override suspend fun getErrorFromUsername(username: String) = when {
    username.length < 4      -> R.string.short_username
    username.contains(" ")   -> R.string.name_contains_spaces
    username.length >= 25    -> R.string.long_username
    doesNameExist(username)  -> R.string.name_unavailable
    else                     -> null
}
```

| Rule as written | Maps to | Notes |
| --- | --- | --- |
| `length < 4` | `UsernameError.TooShort` | 4 characters is the shortest accepted name |
| `contains(" ")` | `UsernameError.ContainsSpaces` | plain substring check — `" andrew"` is rejected, **not** trimmed |
| `length >= 25` | `UsernameError.TooLong` | bound is **exclusive**: 24 is the longest accepted name |
| `doesNameExist(...)` | `Result.Success(false)` from `CheckUsernameAvailabilityUseCase` | uniqueness is not a validation error — it is an availability answer |
| *(nothing)* | — | there is **no** blank case: `""` falls into `length < 4` |
| *(nothing)* | — | there is **no** allowed-character rule at all |

Two of these are **flagged, not changed** (per non-negotiable #5 — the decision is the human's):

1. **No allowed-character rule.** Only the space character is rejected. `an!@#$rew`, emoji,
   newlines, tabs and RTL override characters are all accepted today. For a username that is
   rendered next to other users' content and used as a public handle, that is a real gap —
   likely a spoofing/display concern rather than a storage one. `ValidateUsernameUseCaseTest`
   pins today's permissive behavior so that changing it is a visible, deliberate diff.
2. **First-failure-only, and short-circuiting on I/O.** The legacy `when` returns one string id
   and calls `doesNameExist` inside the same expression, so a validation failure and a network
   failure are indistinguishable to the caller. `ValidateUsernameUseCase` (pure) +
   `CheckUsernameAvailabilityUseCase` (I/O) split that; the pure half returns **all** violated
   rules, matching D6's shape for passwords.

`UsernameError.Empty` is the one rule Pass 1 added rather than copied: the legacy code folds a
blank username into `TooShort`, which would light the "too short" rule red before the user has
typed anything. It is a presentation distinction, not a new restriction — a blank username is
still invalid either way.

```kotlin
// domain/auth/UsernameRepo.kt   (interface lives in domain, impl in repo)
interface UsernameRepo {
    suspend fun isUsernameAvailable(username: String): Result<Boolean, DataError.Network>
    suspend fun claimUsername(username: String): EmptyResult<DataError.Network>
}

// domain/auth/usecase/ValidateUsernameUseCase.kt   — pure, no I/O
class ValidateUsernameUseCase { operator fun invoke(username: String): List<UsernameError> }

// domain/auth/usecase/CheckUsernameAvailabilityUseCase.kt   — combines validation + repo
class CheckUsernameAvailabilityUseCase(
    private val validate: ValidateUsernameUseCase,
    private val repo: UsernameRepo,
) { suspend operator fun invoke(username: String): Result<Boolean, UsernameError> }
```

Availability is checked **debounced (400 ms) after typing stops**, never on every keystroke.

**Test doubles:** hand-written `FakeUsernameRepo` implementing the domain interface for use-case
and ViewModel tests (deterministic, no mocking framework). MockK only at the Firebase boundary
in `DefaultUsernameRepo` tests, per [docs/SPEC.md](../SPEC.md) §8.

**Required edge-case tests:** taken vs available; network error mid-check; debounce (three rapid
keystrokes → exactly one repo call); result of a stale in-flight check must not overwrite a newer
query; empty; too short/long; invalid characters; leading/trailing whitespace.

**Security note for the persona (§7):** an unauthenticated, unthrottled username-availability
endpoint is a user-enumeration vector. Flag it; do not silently redesign it.

### 4.5 Screen 5 — Basic phone

Ends at "verification code requested" — the downstream enter-code screen is out of scope (D2).

Firebase phone auth is callback-based and needs an `Activity`. **The domain interface must not
mention `Activity`** (non-negotiable #6). Bridge it in the repo:

```kotlin
// domain/auth/PhoneAuthRepo.kt
interface PhoneAuthRepo {
    fun requestVerificationCode(e164PhoneNumber: String): Flow<PhoneAuthStatus>
}
sealed interface PhoneAuthStatus {
    data class CodeSent(val verificationId: String) : PhoneAuthStatus
    data object AutoVerified : PhoneAuthStatus
    data class Failed(val error: DataError.Network) : PhoneAuthStatus
}
```

The implementation obtains its `Activity` from an injected provider — it is **not** a parameter
on the domain interface. `callbackFlow` + `awaitClose` is the bridge; cancellation must
propagate (`CancellationException` is never swallowed — [docs/SPEC.md](../SPEC.md) §6).

**Required edge-case tests:** valid E.164; missing country code; non-numeric; too short/long;
`callbackFlow` cancellation unregisters the Firebase callback (leak check); failure maps to the
right `DataError.Network`.

### 4.6 Screen 6 — Select basic auth (the structural payoff)

`ViewPager2` + `TabLayoutMediator` + `FragmentStateAdapter` → `HorizontalPager` + `TabRow`.

Deletes `BasicEmailFragment`, `BasicPhoneFragment`, and the `getInstance(isLogin)` +
`Delegates.notNull` pattern that only existed to serve `FragmentStateAdapter`. `isLogin` becomes
an ordinary composable parameter.

Cannot start until screens 2 and 5 are persona-PASS.

---

## 5. Definition of done — every screen

Work **inside-out**: domain → data → ViewModel → UI → Fragment shell. Not UI-first; the UI shape
falls out of the state contract, so building it first means rewriting it.

- [ ] Domain use case + typed error, test-first (red → green → refactor)
- [ ] Repo interface in `domain`, impl in `repo` wrapped in `safeCall` (skip if no I/O)
- [ ] `@HiltViewModel`, one immutable `UiState` as `StateFlow`, single `onAction(XyzAction)`,
      domain errors mapped to `UiText`, no repo exposed publicly
- [ ] Stateless `XyzScreen(state, onAction)` with `@Preview` + stateful `XyzRoute`
- [ ] Fragment shell: `ComposeView` + `collectAsStateWithLifecycle()` + nav callbacks only.
      **Hard cap 50 lines.** Over that means logic leaked back in.
- [ ] Old XML layout + its ViewBinding/DataBinding references deleted **in the same slice**
- [ ] Screen matches the Mobbin reference for this screen (icons, order, copy, spacing)
- [ ] `./gradlew testDebugUnitTest` green; app builds, installs, and launches
- [ ] Screenshot captured via the `android-cli` skill
- [ ] All three personas PASS

---

## 6. Master migration checklist

Tick as each completes. This is the phase's progress tracker.

### Pass 0 — foundations
- [x] `app/google-services.json` present (**human**)
- [x] AGP 8.x + Gradle 8.x; `assembleDebug` produces an installable APK (AGP 8.5.2 / Gradle 8.7)
- [x] Core library desugaring enabled (`java.time` on minSdk 21)
- [x] Test/runtime dependencies added (§3c)
- [x] `UiText` + `asString()`
- [x] `TikTokTheme` (Material 3)
- [x] `AuthEvent` one-shot event channel pattern
- [x] Auth ViewModels on `@HiltViewModel` + constructor injection — **`BasicEmailViewModel` only**
      (the spec's own motivating example: `ValidateEmailUseCase`/`ValidatePasswordUseCase` have
      no-arg constructors, so `@Inject constructor()` was mechanical). The other 6 auth
      ViewModels (`EnterPasswordViewModel`, `LogInViewModel`, `CreateUsernameViewModel`,
      `BasicPhoneViewModel`, `EnterCodeViewModel`, `SignUpViewModel`, `LogInViewModel`) still
      construct legacy `AuthRepo`/`NameRepo`/`UserRepo` inline — converting them requires either
      `@Inject`-annotating those repo classes or writing Hilt `@Module`/`@Provides` bindings for
      them, which is real DI plumbing beyond what §3d scoped and isn't pinned anywhere. Left for
      each screen's own Pass 2 (several of these VMs are being rewritten wholesale anyway, e.g.
      `EnterPasswordViewModel` per D6). `@HiltAndroidApp` on `App`, `@AndroidEntryPoint` on
      `MainActivity` + `BasicEmailFragment` are in place so this extends cleanly per screen.
- [x] Emulator running via `android-cli`; baseline screenshot of the app launching
      ([pass0-baseline.png](../designs/baseline/pass0-baseline.png))

### Screens
| # | Screen | Domain | Data | VM | UI | XML deleted | Personas |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Age verification | ✅ | n/a | ✅ | ✅ | ✅ | ☐ |
| 2 | Basic email (retrofit) | ✅ exists | n/a | ✅ | ✅ | ✅ | ☐ |
| 3 | Enter password | ✅ | ✅ | ✅ | ✅ | ✅ | ☐ |
| 4 | Create username | ✅ | ✅ | ✅ | ✅ | ✅ | ☐ |
| 5 | Basic phone | ✅ | ✅ | ✅ | ✅ | ✅ | ☐ |
| 6 | Select basic auth | n/a | n/a | n/a | ✅ | ✅ | ☐ |
| 7 | Sign-up landing | ✅ | ✅ | ✅ | ✅ | ✅ | ☐ |
| 8 | Enter code (SMS) | ✅ | ✅ | ✅ | ✅ | ✅ | ☐ |
| 9 | Log-in landing | ✅ | ✅ | ✅ | ✅ | ✅ | ☐ |

Screens 8 and 9 were outside D2's scope. Enter-code was pulled in because phone sign-up cannot
reach an account without it, and log-in because "authentication is complete" implies returning
users can get back in. Both were user-approved on 2026-08-09; see
[AUTH_COMPLETION_PLAN.md](AUTH_COMPLETION_PLAN.md) §4.

### Phase close-out
- [x] `docs/SPEC.md` §5 auth row updated with D5 + D6 behavior changes
- [x] `CLEAN_ARCHITECTURE_MIGRATION.md` slice table updated (slices 1–5)
- [x] `presentation/AGENTS.md` "only BasicEmailScreen is Compose" line updated
- [x] Legacy auth Fragments/XML confirmed deleted; `git grep` clean
- [ ] Human review

### Explicitly deferred (not this phase)
- ~~Enter-code screen — blocked on an OTP input decision~~ **Built 2026-08-09** (hand-rolled
  Compose 6-box input over a single field; decision D-A2 in `AUTH_COMPLETION_PLAN.md`).
- ~~Sign-up landing~~ **Built 2026-08-09.** Google + Facebook are implemented but render
  disabled until their provider config exists — neither can be finished from this repository.
- Twitter auth — removed 2026-07-25; restore via Firebase `OAuthProvider` if ever wanted
- Deleting `TheResult` / `safeAccess` / `BaseViewModel` / `BaseFragment` — one dedicated step,
  only once nothing references them (non-negotiable #2)

---

## 7. The three review personas (Pass 3)

Run **in parallel, per screen**, as independent agents with no shared context. Each returns a
verdict plus findings. Any FAIL bounces the screen back to Pass 2 with the findings attached;
the screen re-enters review after the fix. Only when all three PASS does the screen count as
done — and only when all six screens are done does it reach the human.

Each finding must be **falsifiable**: file, line, and a concrete failure scenario (inputs →
wrong output). "This could be cleaner" is not a finding.

| Persona | Owns | Sample checks |
| --- | --- | --- |
| **Security engineer** | Auth-specific risk | Passwords/tokens/PII in Timber logs or crash reports; validation enforced in domain rather than only in the UI (client-side-only gate = bypassable); age gate bypassable via rotation/back-nav/process death; username availability as an enumeration vector; error messages that leak whether an account exists; `CancellationException` swallowed; secrets in `BuildConfig`/committed files |
| **QA engineer** | Does it actually work | Test coverage vs the required edge cases in §4; app runs on a device (`android-cli`); screen matches the Mobbin reference — icons, step order, copy; rotation + process-death state restoration; back navigation; loading and error states reachable; accessibility (content descriptions, touch targets, contrast) |
| **Architecture reviewer** | Long-term quality | Non-negotiables in [AGENTS.md](../../AGENTS.md) §4; dependency-direction grep returns nothing; Fragment shell ≤50 lines; no God ViewModel; no premature abstraction (§5 — a component extracted at one caller is a finding); `ImmutableList` + `key =` on lazy lists; narrowest state slice passed down; `UiText` resolved only in composables; naming consistent with the codebase |

**Dependency-direction proof** — the architecture persona must run this and paste the output:

```bash
grep -rn "^import" app/src/main/java/com/andre_max/tiktokclone/domain | grep -Ei "android|firebase|com\.andre_max\.tiktokclone\.(repo|presentation)"
```

Must return nothing. One legitimate exception: `java.time` is not an Android dependency.

---

## 8. Mobbin design references

**In place as of 2026-07-30** in `docs/designs/onboarding/`, with
[a README](../designs/onboarding/README.md) recording exactly which Mobbin capture maps to which
screen, which states each shows, and the two places the design contradicts this spec (resolved
by D8 and D9). Read that README before using any frame.

Screen 6 has no standalone frame — it is the `Phone | Email` tab bar visible in `02` and `05`,
which is direct confirmation that the §4.6 `ViewPager2` → `HorizontalPager` collapse is right.

**Designs are consumed at Pass 2, before writing the composable — never as a Pass 3
diff-and-rebuild step.** The Mobbin frame is the layout spec; the old XML is consulted only for
*behavior* (what validation exists, what the state transitions are). Building the composable
from the XML and reconciling against Mobbin afterwards is the one workflow that guarantees
throwing work away.

If a design is missing for a screen, Pass 2 must **stop and ask**, not invent a layout.
