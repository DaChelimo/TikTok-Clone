# Auth Completion Plan — from "screens built" to "authentication works"

**Status:** executed 2026-08-09. Every step in §3 landed except the provider-config items in §5, which cannot be done from this repository.
**Scope:** authentication only. No feed/profile/upload work.

Read with [ONBOARDING_FEATURE_SPEC.md](ONBOARDING_FEATURE_SPEC.md) (the frozen contract) and
[ONBOARDING_MIGRATION.md](ONBOARDING_MIGRATION.md) (the history). Where this file disagrees
with either, it is because verification against the code found them out of date — the
discrepancies are itemised in §1.

---

## 0. What just landed (committed this session)

The working tree was carrying an entire unreviewed Pass 2. It is now four commits on
`migrate-to-compose`:

| Commit | Contents |
| --- | --- |
| `80e93af` | Agent briefings (root + 3 nested `AGENTS.md`), onboarding spec, pass prompts, Mobbin design refs, `feature-migration-cycle.js` |
| `4685c5d` | Pass 2 bodies: username + phone use cases, their repo impls, and the Compose screens/ViewModels for create-username, basic-phone, select-basic-auth |
| `728bc34` | `.idea` config fallout from the AGP 8 / Compose toolchain bump |
| — | `.gitignore` now excludes `.claude/settings.local.json` and `firebase-debug.log` (folded into `80e93af`) |

**Verified after committing:**

- `./gradlew assembleDebug` — **green**.
- `./gradlew testDebugUnitTest` — 150 tests, **13 failures, all pre-existing legacy suites**
  (`HomeViewModelTest`, `SearchPageViewModelTest`, the *legacy* `sign_up.create_username.CreateUsernameViewModelTest`,
  `TagRepoTest`, `TimeUtilsTest`). Every new auth domain/presentation test passes.

Two repo tests were deleted in that Pass 2 (`DefaultPhoneAuthRepoTest`, `DefaultUsernameRepoTest`).
I committed the deletion rather than reverting it — both drove the Firebase SDK through MockK and
asserted the mock rather than the mapping — but it does mean §8 of `docs/SPEC.md` ("MockK at the
Firebase boundary") is currently unimplemented. Listed as a task in §3, not silently dropped.

---

## 1. The headline finding: the Compose auth flow does not authenticate anyone

This is the P0 that AGENTS.md §6 says to stop and raise rather than work around.

**Account creation exists in exactly one place, and it is legacy code that the new flow does not
call.** `presentation/ui/auth/create_username/CreateUsernameViewModel.completeSignIn()` (the old
Fragment/LiveData one) is what actually:

1. calls `AuthRepo.signUpWithEmailBody(...)` **or** `AuthRepo.signInWithCredential(...)`,
   chosen from the nav args, and
2. calls `UserRepo.addUserToDatabase(username, authResult, profilePicture)`, and
3. only then registers the username.

Its comment names the reason for that ordering: *"We create the user at the last stage to prevent
creating a void user in case he/she leaves the app mid-way."* That is a real product decision, and
it is nowhere in `ONBOARDING_FEATURE_SPEC.md`.

The new chain does none of it:

```
BasicEmailScreen ──NavigateToPassword(email)──> EnterPasswordScreen
    └── EnterPasswordViewModel.onContinueClicked() → AuthEvent.NavigateToUsername   (password dropped)
CreateUsernameViewModel.onContinueClicked() → usernameRepo.claimUsername(name) → NavigateToHome
```

`DefaultUsernameRepo.claimUsername` writes one string to `takenUsernames/`. **No Firebase account
is ever created; no user record is ever written; the email and password are never carried past
their own screens.** A user completing the new flow arrives at Home unauthenticated with a
username reserved to nobody.

Root cause is in the spec, not the implementation: §4.4 pins `UsernameRepo` as
`isUsernameAvailable` + `claimUsername` and stops there. Pass 2 built exactly what was pinned.

### Related documentation drift found while verifying

| Claim | Reality |
| --- | --- |
| `CLEAN_ARCHITECTURE_MIGRATION.md` slice 3: "Auth use cases (sign up/in with email) ✅ Done" | `domain/auth/usecase/` contains **only** validators + `CheckUsernameAvailabilityUseCase`. No sign-up/sign-in use case exists. |
| Spec §6 screen table: screens 3–6 all `☐` | Screens 3–6 are implemented (real bodies, green tests) — the table was never ticked. |
| `ONBOARDING_MIGRATION.md`: screens 4–6 "Pass 2 not started" | Pass 2 ran; the table just wasn't updated. |

I'd rather fix these as part of the work than leave a third round of "marked done because the file
exists."

---

## 2. What is actually done vs. pending

### ✅ Done and verified

- **Foundations** — `Result`/`DataError`/`safeCall`, `UiText` + `asString()`, `TikTokTheme`
  (Material 3), `AuthEvent` one-shot channel, AGP 8.5.2 / Gradle 8.7, desugaring, Hilt bootstrapped
  (`AuthDataModule` is the first module).
- **Domain validators** — age (true DOB, min 13), email, password (all-failures list, 8–20,
  identity containment), phone (E.164), username (rules pinned to legacy behaviour), plus
  `CheckUsernameAvailabilityUseCase`. All test-covered and green.
- **Six Compose screens exist** with `UiState` + `onAction` + `Route`/`Screen` split — age,
  basic-email, enter-password, create-username, basic-phone, select-basic-auth.
- **Three are wired and reachable** — age, basic-email, enter-password (they have `ComposeView`
  Fragment shells).

### 🔴 Pending — blocking "authentication works"

| # | Gap | Evidence |
| --- | --- | --- |
| **P0-1** | No account creation anywhere in the new flow (§1) | `CreateUsernameViewModel` only calls `claimUsername` |
| **P0-2** | Screens 4, 5, 6 are **unwired dead code** | `mobile_navigation.xml` still names `create_username.CreateUsernameFragment`, `phone_auth.enter_phone_number.BasicPhoneFragment`, and the ViewPager2 `SelectBasicAuthFragment`. Only 3 files in `ui/auth/` reference `ComposeView`. |
| **P0-3** | Phone auth crashes on first use | `AuthDataModule.provideActivityProvider` is a live `ActivityProvider { TODO("Pass 2: track the resumed Activity") }` |
| **P0-4** | Phone sign-up is a dead end | `BasicPhoneViewModel` emits `NavigateToVerificationCode`, but enter-code is out of scope (D2) and its layout is a plain `EditText` stand-in |
| **P0-5** | Google Sign-In cannot work | `app/google-services.json` has `"oauth_client": []` — no SHA-1 / OAuth client registered for the Android app. `GoogleSignIn` will fail with `ApiException` status 10. |
| **P0-6** | Facebook login very likely broken | Facebook SDK 13.1.0 requires a `com.facebook.sdk.ClientToken` manifest entry (mandatory since v12); the manifest declares `ApplicationId` and the login protocol scheme only. |
| **P0-7** | Log-in (as opposed to sign-up) never migrated | `LoginFragment` / `LogInViewModel` are untouched legacy, exposing `googleAuthRepo`/`facebookAuthRepo` as public fields |
| **P0-8** | Sign-up landing (screen 7) not started | `SignUpFragment` still uses the deprecated `onActivityResult` override and reaches into `viewModel.facebookAuthRepo` directly |

### 🟡 Pending — quality/close-out, not blocking

- Repo-boundary tests re-established (the two deleted files).
- Session gate: nothing checks `Firebase.auth.currentUser` at launch except `MeFragment`;
  `SplashActivity` does not route a signed-in user past onboarding.
- Legacy auth Fragments + XML deletion (non-negotiable #2 — one dedicated step, at the end).
- Spec §6 checklist, `CLEAN_ARCHITECTURE_MIGRATION.md` slice table, and
  `presentation/AGENTS.md` all need the close-out updates listed in spec §6.
- The 13 pre-existing legacy test failures (out of auth scope; flagging, not fixing).

---

## 3. Proposed plan

Ordered so the app compiles and runs after every step (non-negotiable #1), and so the
architecture gap is closed **before** more screens are built on top of it.

### Step 1 — Close the account-creation hole (P0-1)

The missing layer, built test-first inside-out:

```kotlin
// domain/auth/AuthRepo.kt        (interface in domain, impl in repo)
interface AuthRepo {
    suspend fun signUpWithEmail(email: String, password: String): Result<UserId, DataError.Auth>
    suspend fun signInWithEmail(email: String, password: String): Result<UserId, DataError.Auth>
    suspend fun signInWithCredential(credential: AuthCredential): Result<UserId, DataError.Auth>   // see Q3
    fun currentUserId(): UserId?
    fun signOut()
}

// domain/auth/usecase/CompleteSignUpUseCase.kt
// Preserves the legacy ordering: create account → write user record → claim username,
// and rolls back the account if the record write fails.
```

Introduces `DataError.Auth` with typed cases (`EmailAlreadyInUse`, `WeakPassword`,
`InvalidCredentials`, `UserNotFound`, `TooManyRequests`, `Unknown`) rather than folding auth
failures into `DataError.Network`. Firebase's `FirebaseAuthException` error codes map onto these.

**Carrying credentials across screens.** The new flow drops email and password between screens.
Two options — see **Q1**; my recommendation is a `@ViewModelScoped` / nav-graph-scoped
`SignUpSession` holder rather than nav arguments, because passing a password through a
`SafeArgs` `Bundle` puts it in saved-instance-state and in `navController` back-stack dumps.

### Step 2 — Wire screens 4, 5, 6 into navigation (P0-2)

Add `ComposeView` shells (≤50 lines, per spec §5) for create-username, basic-phone and
select-basic-auth; repoint `mobile_navigation.xml` at them; delete the ViewPager2 host,
`BasicFragmentStateAdapter`, and the `getInstance(isLogin)` + `Delegates.notNull` pattern.
`BasicEmailFragment`/`BasicPhoneFragment` stop being ViewPager children and their `getInstance`
factories go away. XML layouts deleted in this same step, per spec §5.

### Step 3 — Make phone auth real (P0-3, P0-4)

- Implement `ActivityProvider` as an `Application.ActivityLifecycleCallbacks` registration in
  `App` that tracks the resumed Activity via a `WeakReference`. Removes the `TODO()`.
- Build the **enter-code screen** — this is a scope expansion over D2, and I'm flagging it rather
  than absorbing it: without it phone sign-up cannot complete, so "authentication is complete"
  and "enter-code is out of scope" are incompatible. See **Q2** for the OTP-input decision that
  has been open since 2026-07-25.

### Step 4 — Social auth: Google and Facebook (P0-5, P0-6, P0-8)

Screen 7 (sign-up landing) rebuilt in Compose: `rememberLauncherForActivityResult` for Google,
the Facebook `CallbackManager` registered against the `ActivityResultRegistry` instead of the
deprecated `onActivityResult`, and `googleAuthRepo`/`facebookAuthRepo` moved behind the
`AuthRepo` interface from Step 1 so the Fragment stops reaching into the ViewModel's fields.

**The config side is what actually blocks these, and it is fixable — see Q4.** I am authenticated
to the Firebase CLI as `tech.octagontechnologies@gmail.com`, and the Android app
`1:585896630733:android:24704276fb85173579cc84` is visible. Registering the debug keystore SHA-1
and re-downloading `google-services.json` would populate `oauth_client` and unblock Google
Sign-In. Facebook needs a client token from the Meta developer console — I cannot obtain that.

**Fallback if you'd rather not do the console work now:** both buttons get disabled with a
"coming soon" state, the reason recorded in `docs/SPEC.md`, and TODOs filed. Email and phone
would then be the complete, working paths. This is a real option, not a failure mode — say the
word and Step 4 shrinks to the disable + document version.

### Step 5 — Log-in parity (P0-7)

Migrate `LoginFragment`/`LogInViewModel` onto the same screens. Most of the work is already
done — `select-basic-auth` takes `isLogin` as an ordinary composable parameter, so log-in reuses
the email/phone screens and routes to `signInWithEmail` instead of `signUpWithEmail`. The
security persona should specifically check that a wrong-password and an unknown-account error
render the *same* message (account-enumeration).

### Step 6 — Session gate

`SplashActivity` routes to Home when `currentUserId() != null`, to the sign-up landing otherwise.
Plus a sign-out path, which currently does not exist anywhere.

### Step 7 — Close-out

Re-add repo-boundary tests; run the three review personas (spec §7) across the whole vertical;
delete legacy auth Fragments/XML/`SignUpViewModel`/legacy `CreateUsernameViewModel` once
`git grep` is clean; update `docs/SPEC.md` §5, the migration slice table, `presentation/AGENTS.md`,
and the spec §6 checklist. `TheResult`/`BaseViewModel`/`BaseFragment` stay — they're still used
outside auth (non-negotiable #2).

### Execution mechanics

Steps 1–6 each run as a red-tests → implement → review cycle. I'd drive them with the existing
`feature-migration-cycle` skill where a step is screen-shaped (2, 3, 4, 5), and directly where it
isn't (1, 6, 7). Every step ends with `assembleDebug` + `testDebugUnitTest` and, for UI steps, a
screenshot via `android-cli`. I'll check in with you at the end of each step rather than running
all seven unattended.

---

## 5. What is left, and why it is not code

Everything in §3 landed. Two things remain, both outside this repository.

**Google sign-in.** The code path is complete — `GoogleSignInContract` +
`rememberLauncherForActivityResult` on both landings, `idToken` through
`AuthRepo.signInWithGoogle`. The debug **SHA-1 and SHA-256 are now registered** on Firebase app
`1:585896630733:android:24704276fb85173579cc84` (project `tiktok-clone-for-fun`). Two
console-only steps remain:

1. **Enable Google as a sign-in provider** (Firebase console → Authentication → Sign-in method →
   Google). Until then `google-services.json` keeps `"oauth_client": []` — registering a SHA does
   not create the OAuth clients on its own, which re-pulling the config after registration
   confirmed.
2. **Replace `GOOGLE_WEB_CLIENT_ID` in `gradle.properties`.** The value there is
   `1031105970956-…`, which belongs to a *different* Google Cloud project than this app's
   `585896630733`. A token minted by a foreign project's client is rejected at
   `signInWithCredential`. Copy the `client_type: 3` entry out of the refreshed
   `google-services.json` once step 1 is done.

`SocialAuthModule.provideGoogleWebClientId` compares the id's project-number prefix against the
Firebase sender id and returns null when they disagree, so the button stays disabled and says so
rather than failing at the chooser. It enables itself once both steps land — no code change.

**Facebook sign-in.** Disabled by decision D-A4. SDK 12+ requires a
`com.facebook.sdk.ClientToken` manifest entry that only the Meta developer console can issue;
the manifest declares `ApplicationId` and the login protocol scheme but no client token. Flip
`SocialAuthModule.provideIsFacebookEnabled` once the token is in `gradle.properties` and declared
in the manifest — the button and its wiring already exist.

---

## 4. Decisions (user-approved 2026-08-08)

These were the open questions; all are now closed. Do not re-litigate them.

| # | Question | Decision |
| --- | --- | --- |
| **D-A1** | Credential hand-off between screens | **Nav-graph-scoped `SignUpSession` holder**, not `SafeArgs`. A password in a `Bundle` survives into saved-instance-state and back-stack dumps. |
| **D-A2** | OTP input | **Build it, hand-rolled in Compose** (no new dependency) — but **as its own feature slice**, with its own commits, not folded into the rest of the auth work. |
| **D-A3** | `AuthCredential` in domain | **Keep Firebase types out of `domain` entirely.** `signInWithGoogle(idToken: String)`, not `signInWithCredential(AuthCredential)`. Clean separation wherever it can be had. |
| **D-A4** | Provider config | **Google: go ahead** — register the debug SHA-1 via the Firebase CLI and refresh `google-services.json`. **Facebook: disabled for now**, with the button in a disabled state, a TODO, and the reason recorded in `docs/SPEC.md`. |
| **D-A5** | Log-in | **In scope.** Step 5 stands. |
| **D-A6** | The 13 legacy test failures | **Fix them**, grouped by root cause. |

**D-A7 — Commit hygiene (applies to every step below).** One commit resolves one specific thing,
and only the changes belonging to that thing are staged into it. Never batch unrelated fixes
together — the legacy test repairs in particular get their own commits, split by cause rather
than by file. The user reviews this branch commit-by-commit, so a mixed commit is the single
most expensive thing to hand them.
