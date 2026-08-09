# Onboarding & Sign-up — Screen-by-Screen Migration Plan

Detailed expansion of slices 3–5 in [CLEAN_ARCHITECTURE_MIGRATION.md](../CLEAN_ARCHITECTURE_MIGRATION.md).
Behavior contracts live in [SPEC.md](../SPEC.md); layer conventions live in the nested
`AGENTS.md` files. This file is the *route* for the auth flow only.

> **⚠️ Partially superseded (2026-07-29).** Execution now runs through
> [ONBOARDING_FEATURE_SPEC.md](ONBOARDING_FEATURE_SPEC.md), which freezes the open decisions
> below and pins the API contracts. **Read that file first.** This file remains the route and
> the history — per-screen risks, what broke and why, what Twitter removal cost — and is still
> accurate on all of that. Where the two disagree, the spec wins.
>
> **Progress (2026-08-07):** screens 1, 2, 3 are done (real bodies, no stubs). Screens 4, 5, 6
> only exist as Pass-1 stubs — `TODO("Pass 2")` throughout their use cases, repos, ViewModels,
> and Screens — Pass 2 has not run for them yet. **Correction:** an earlier pass through this
> file marked 4-6 done based on their files existing; that was wrong, caught by grepping for
> `TODO("Pass 2")`. The "Open decisions" list below (M2 vs M3, min age, age precision) is
> genuinely resolved in favor of the spec's D4/D5, independent of the above. Screen 7 (sign-up
> landing) is not started, pulled into scope by spec D2's 2026-08-07 amendment, and blocked
> behind Pass 2 on 4-6 since screen 6 (select-basic-auth) is what screen 7 navigates into.
> Enter code remains blocked on the OTP dependency decision.

**Approval model — CHANGED, see spec §1.** ~~Each screen below is one approval gate; nothing in
screen N+1 starts until screen N is approved.~~ Three review personas now gate each screen
automatically and the human reviews once at the end of the phase. Slice 0 is still the
exception — it lands as one unit before any screen, because every screen depends on it.

## Current flow (from `mobile_navigation.xml`, verified 2026-07-25)

```
signUpFragment ──> ageFragment ──> selectBasicSignUpFragment ──┬─> createPasswordFragment ──┐
      │                                  (ViewPager:            │                            │
      ├──> loginFragment ────────────────> phone | email tabs)  └─> enterCodeFragment ───────┤
      │                                                                                      │
      └──> createUsernameFragment <────────────────────────────────────────────────────────┘
                    │                                            (social auth skips straight here)
                    └──> homeFragment
```

That's **7 screens / 17 Kotlin files**, not 5 — `SelectBasicAuth` hosts `BasicEmail` and
`BasicPhone` as ViewPager child fragments, which is easy to miss when counting.

## Slice 0 — Unblock and lay foundations (no screen work)

### 0a. Build health — the project does not compile (status 2026-07-25)

The repo has been broken for a while along several independent axes, not just Twitter. What's
been fixed so far, and what's left:

**Fixed (2026-07-25):**

| Was broken | Fix applied |
| --- | --- |
| 5 files importing the removed `com.twitter.sdk.android.core.*` | Twitter auth removed entirely: `App.kt`, both auth VMs, both auth Fragments, `AuthRepo.TwitterAuthRepo`, `CustomTwitterLoginButton.kt` (deleted), buttons in 2 layouts, `BuildConfig` fields, `gradle.properties` keys |
| `continue_with_twitter` declared **twice** in `strings.xml` (AAPT2 duplicate-resource error) | Both removed |
| `tw__*` colors/drawables referenced in 3 layouts, supplied only by the removed SDK AAR | References removed / substituted |
| `minSdkVersion 17` but Compose requires 21 | Raised to 21 |
| `compileSdkVersion 31` but Compose 1.7.0 requires 34 (45 AAR-metadata errors) | Raised to 34; stale `buildToolsVersion` pin dropped |
| `android:exported` missing on `SplashActivity` (required by targetSdk 31) | Added |
| `com_facebook_button_login_logo` no longer shipped by Facebook SDK 13.1.0 | Icon references removed from 2 layouts |
| `editcodeview` custom-view attributes in `fragment_enter_code.xml` | Replaced with a plain `EditText` stand-in (marked TODO — proper OTP input still an open decision) |

**Fixed (2026-07-25, second pass) — `./gradlew compileDebugKotlin` now succeeds.** All 26
Kotlin errors were dependency-upgrade fallout, not new problems from the Twitter removal:

| Group | Files | Fix |
| --- | --- | --- |
| `Transformations.map` removed in Lifecycle 2.8 | `EnterPasswordViewModel`, `BasicPhoneViewModel` | Switched to the `LiveData<T>.map {}` extension (`androidx.lifecycle.map`) |
| `observe` unresolved | `EnterPasswordFragment`, `BasicPhoneFragment` | Resolved as a side effect of the above — both were cascading from the same broken `isValid` type |
| ExoPlayer 2.17 API change | `presentation/exoplayer/Player.kt` | `Player.EventListener` → `Player.Listener`; split `onPlayerStateChanged(playWhenReady, state)` into `onPlaybackStateChanged(state)` (the `playWhenReady` param was unused, so no behavior lost); `onPlayerError` now takes `PlaybackException` |
| `MotionEvent` now non-null under compileSdk 34 | `ui/components/video/MainLargeVideo.kt` | Dropped `?` from 3 `GestureDetector.SimpleOnGestureListener` overrides |
| **Half-finished edit** | `EnterPasswordViewModel` | Diagnosed as **dead code**, not a real WIP feature — see below |

**On the half-finished `EnterPasswordViewModel` edit:** `updatePassword()` and
`EnterPasswordUIState` referenced `validatePasswordUseCase`/`_uiState`/`mapPasswordError`, none
of which existed anywhere in the file. Grepping the whole codebase confirmed neither symbol is
referenced from anywhere else either — `EnterPasswordFragment` calls `verifyPassword()` and
observes `passwordInput`/`livePasswordStatus`/`isValid`/`navigate`, the legacy LiveData API,
exclusively. This was a stray half-paste of `BasicEmailViewModel`'s pattern that was never
wired up. **Deleted**, not migrated — the file's actual (still legacy, still working) password
validation logic in `verifyPassword()`/`getErrorFromPassword()` is untouched. Moving it onto
`ValidatePasswordUseCase` is the real screen 3 work and stays gated on its own approval.

**Also required to build at all:** `app/google-services.json` is absent (it's gitignored, as it
should be). Download it from the Firebase console. Both compilation passes above were verified
using a throwaway placeholder, deleted immediately after — it must not be used at runtime.

### 0b′. New blocker found while verifying — `assembleDebug` fails at dexing, not compiling

`compileDebugKotlin` succeeding is not the same as a working APK. Running the full
`assembleDebug` surfaces a **separate, pre-existing toolchain mismatch**: AGP 7.4.2 (pinned in
root `build.gradle`) officially supports only up to `compileSdk 33`, and its bundled D8 dexer
crashes (`NullPointerException` inside D8's Kotlin-metadata reader) trying to desugar
`androidx.lifecycle:lifecycle-livedata-core:2.8.3`. This is downstream of the `compileSdk
31→34` bump slice 0a already made (forced by Compose 1.7.0) outrunning what AGP 7.4.2 can
dex — not something the Twitter removal or the Kotlin fixes caused.

Not fixed yet — it's an AGP (and likely Gradle wrapper) upgrade, a bigger toolchain decision
than a mechanical error fix, and needs your sign-off before touching it. Options: bump AGP to
8.x (matches `compileSdk 34` cleanly, but 8.x also tightens other things — namespace
declarations, manifest merging rules — that may surface *more* pre-existing issues), or pin
`compileSdk`/Compose back down to versions AGP 7.4.2 supports (avoids the upgrade now, defers
the same decision).

### 0c. Twitter removal — what was lost, and how to restore it

Worth knowing: `AuthRepo.TwitterAuthRepo` used **Firebase's** `OAuthProvider("twitter.com")`,
not the Twitter SDK, so the sign-in mechanism itself was never broken — only the SDK's button
and session-callback layer was. It was removed anyway because the button's icon and brand
color came from the dead AAR, and screen 7 rewrites this UI in Compose regardless.

To restore Twitter later: re-add ~25 lines of Firebase `OAuthProvider` code (recoverable from
git history) plus a brand asset. No Twitter SDK dependency needed.

### 0d. Foundations that must exist before screen 1

These qualify as "abstract now even at one caller" (architectural boundaries, per
[AGENTS.md](../../AGENTS.md) §5) — they are not speculative:

| Item | Why it can't wait |
| --- | --- |
| `@HiltViewModel` + constructor injection for auth VMs | `BasicEmailViewModel` currently does `ValidateEmailUseCase()` inline — untestable, and every new screen would copy it |
| `TikTokTheme` (colors/typography/shapes) | Screens currently wrap in a bare `MaterialTheme {}`; retrofitting a theme across 7 screens later is 7× the work. Decide Material 2 (current, `androidx.compose.material` 1.7.0) vs adding Material 3 here |
| `UiText` sealed type (`Dynamic` \| `Resource(@StringRes, args)`) | The convention in [presentation/AGENTS.md](../../app/src/main/java/com/andre_max/tiktokclone/presentation/AGENTS.md); VMs currently pass raw `Int?` string res |

**Not in slice 0:** a shared auth component library (`AuthTextField`, `PrimaryButton`, etc.).
Do not pre-build it. Extract components when the *second* screen demonstrably needs the same
one — otherwise you'll design the wrong API before you've seen the real cases.

## Screen order and rationale

Ordered by *what each screen teaches* and *what it unblocks*, not by flow order. The flow order
would start with `SignUpFragment`, which is the single most entangled screen in the app — a bad
place to learn.

| # | Screen | Teaches | Notable risk |
| --- | --- | --- | --- |
| 1 | Age verification | Full vertical slice on a clean slate; XML `DatePicker` → Compose | None — no DI, no VM, no I/O today |
| 2 | Basic email (retrofit) | Bringing the existing Compose screen up to the settled conventions | It's the template others copy — fixing it early stops the drift spreading |
| 3 | Enter password | `UiText`, error rendering, `Action` sealed interface | Use case already exists and is tested |
| 4 | Create username | First screen with real repo I/O — `Result`, `safeCall`, loading state | Username-availability check hits Firebase |
| 5 | Basic phone | Firebase phone auth initiation | Async callback → Flow bridging |
| 6 | **Select basic auth** | Collapses `ViewPager2` → `HorizontalPager`, **deletes two Fragment shells** | Structural payoff — first real hoist out of Fragments |
| 7 | Sign-up landing | Social auth, `ActivityResultLauncher` in Compose | Most entangled; deprecated `onActivityResult` |
| — | Enter code | **BLOCKED** | `editcodeview` dependency removed (jcenter); needs a replacement OTP input decision |

Screen 6 is the reason for this ordering. `SelectBasicAuth` hosts email and phone as child
fragments via `FragmentStateAdapter`, which is also why `BasicEmailFragment` needs that awkward
`getInstance(isLogin)` + `Delegates.notNull` mutable-var pattern. Once screens 2 and 5 are
composables, screen 6 collapses all three into one Compose screen with a `HorizontalPager`,
`isLogin` becomes an ordinary parameter, and two Fragments get deleted outright.

## Definition of done — applies to every screen

Work **inside-out** (domain → data → ViewModel → UI → Fragment shell). Not UI-first: the UI
shape falls out of the state contract, so building it first means rewriting it.

1. **Domain** — use case + typed error enum, written test-first (red → green → refactor).
2. **Data** — repo interface in `domain`, implementation wrapped in `safeCall`. Skip if the
   screen has no I/O.
3. **ViewModel** — `@HiltViewModel`, one immutable `UiState`, exposed as `StateFlow`, single
   `onAction(XyzAction)` if the screen has >4 events, domain errors mapped to `UiText`. No repo
   exposed publicly.
4. **UI** — stateless `XyzScreen(state, onAction)` with `@Preview`, plus stateful `XyzRoute`.
5. **Fragment shell** — `ComposeView` + `collectAsStateWithLifecycle()` + navigation callbacks
   only. **Hard cap: 50 lines.** If it exceeds that, logic has leaked back in.
6. **Delete** — the old XML layout and its ViewBinding/DataBinding references, in the same slice.
7. **Verify** — compiles, runs on a device, unit tests pass, and I show you the screenshot.

## Per-screen notes

### Screen 1 — Age verification ✅ Done
`AgeFragment.kt` (75 lines, no ViewModel at all today) + `fragment_age.xml`.

The validation rule currently lives inline in a click listener and needs to become
`ValidateAgeUseCase` + `AgeError`. **Read the existing rule before writing it** — two things
about it need your decision rather than my assumption:

- The current minimum age is **9** (`date.year > year - 9` rejects). TikTok's actual policy is
  13. This looks like a bug, but changing it is a product decision, not a refactor — tell me
  which you want.
- The check compares **years only**, ignoring month and day, so someone born in December is
  treated as a full year older than they are. Do we compute true age from the full date of
  birth in the use case, or preserve the year-only behavior?

Compose has no drop-in `DatePicker` equivalent in Material 2 — this screen needs either
Material 3's `DatePicker`, a wheel-picker composable, or three dropdowns. Worth deciding as
part of the M2-vs-M3 call in slice 0d.

### Screen 2 — Basic email (retrofit, not a fresh migration) ✅ Done
Already migrated in an earlier session, and it's the reference the rest will be modelled on —
so it should match the final conventions before it gets copied 5 more times. Current gaps:

- `collectAsState()` should be `collectAsStateWithLifecycle()` — the current form keeps
  collecting while the fragment is in the background.
- `BasicEmailViewModel` instantiates its use cases inline instead of injecting them.
- `BasicEmailUiState` carries `password`, `passwordErrorRes`, `isPasswordValid` — state that
  belongs to screen 3 and is dead here. `mapPasswordError()` is dead code in this file too.
- `BasicEmailScreenParams` mixes state and callbacks in one bag; the convention is
  `state` + `onAction`.
- Navigation via a `shouldNavigate` boolean + `onResetNavigation` works but is fragile; a
  one-shot event is the cleaner shape once we settle on one.

### Screen 3 — Enter password ✅ Done
`ValidatePasswordUseCase` + `PasswordError` already exist and are tested — this screen is
mostly UI plus the `UiText` mapping. Note the password rule was deliberately tightened
(special char + upper + lower) in an earlier session; that's intentional, not drift.

### Screen 4 — Create username 🔵 Pass 1 stubs only (Pass 2 not started)
First screen that talks to Firebase (availability check). Introduces loading state in `UiState`,
`Result`/`DataError` end-to-end, and debounce-on-typing. `CreateUsernameViewModel` is 125 lines
and takes `GoogleBody`/credential nav args — check what's actually still needed once slice 0a
resolves the social-auth question.

### Screen 5 — Basic phone 🔵 Pass 1 stubs only (Pass 2 not started)
Firebase phone auth is callback-based; bridging it into a Flow inside the repo (with `safeCall`
handling cancellation) is the real lesson here. Its downstream screen (enter code) is blocked,
so this screen ends at "code requested" until the OTP input is resolved.

### Screen 6 — Select basic auth 🔵 Pass 1 stub only (Pass 2 not started)
The payoff slice. `ViewPager2` + `TabLayoutMediator` + `FragmentStateAdapter` → `HorizontalPager`
+ `TabRow`. Deletes `BasicEmailFragment` and `BasicPhoneFragment` shells and the
`getInstance()`/`Delegates.notNull` pattern with them.

### Screen 7 — Sign-up landing Not started (pulled into scope by spec D2, 2026-08-07; blocked behind Pass 2 on screens 4-6)
Google sign-in via `rememberLauncherForActivityResult`, Facebook SDK, and the deprecated
`onActivityResult` override to remove. `SignUpViewModel` currently exposes
`googleAuthRepo`/`facebookAuthRepo` as public fields for the Fragment to call directly — that
inversion needs undoing before this screen is clean.

## Open decisions blocking the start

1. ~~**Twitter auth** — remove, or replace?~~ **Decided 2026-07-25: removed.** See §0c.
2. ~~**Fix the 26 Kotlin compile errors?**~~ **Done 2026-07-25.** `compileDebugKotlin`
   succeeds. See §0a.
3. ~~**AGP/D8 upgrade**~~ **Decided and done 2026-07-29: AGP bumped to 8.5.2, Gradle wrapper to
   8.7 (D1).** Fallout fixed in the same pass: `namespace` added to `app/build.gradle`
   (replacing the manifest `package` attribute), `buildConfig true` under `buildFeatures`,
   `android.nonTransitiveRClass=false` added to `gradle.properties` (AGP 8's new default broke
   `R.dimen._Nsdp`-style references into the `sdp-android`/`ssp-android` libraries),
   `navigation-safe-args-gradle-plugin` bumped 2.4.1 → 2.7.7 (AGP 8 requires ≥2.5.0), and
   `com.google.gms:google-services` bumped 4.3.10 → 4.4.2 (the old plugin generated its
   `google_app_id`/etc. resources on disk but never registered them into the resource merge
   under AGP 8, so `FirebaseApp.getInstance()` crashed at startup with "no default options were
   found" until this was bumped). See `ONBOARDING_FEATURE_SPEC.md` §3b for the full account.
4. ~~**Material 2 or Material 3**~~ **Decided (spec D4): Material 3.** `TikTokTheme` built on it.
5. ~~**Minimum age**~~ **Decided (spec D5): 13**, not the legacy 9.
6. ~~**Age precision**~~ **Decided (spec D5): true age from full date of birth**, not year-only.
7. **OTP input** — the enter-code screen currently has a plain `EditText` stand-in.

## Verification note

`./gradlew compileDebugKotlin` cannot run without `app/google-services.json`. The
Google Services plugin fails before the Kotlin compiler is reached, which masks all source
errors — so "the build fails" locally may mean nothing more than the missing config file.
Always confirm which task failed before concluding the code is broken.
