# Pass 1 — Write the tests (red)

> Paste everything below the line into a fresh session. Run **after** Pass 0 is green.
> This session writes **no implementation**. That is the point.

---

You are working on the TikTok-Clone Android app. Read `AGENTS.md`, then
`docs/plans/ONBOARDING_FEATURE_SPEC.md` in full, then the nested `AGENTS.md` for each layer you
touch (`domain/`, `presentation/`, `repo/`).

Your job this session: **write the complete test suite for all six onboarding screens, plus the
stub classes needed to make it compile and fail red.** You write no working logic. A later
session makes it green.

## Why this shape

A test that does not compile is not a failing test, it is an absent one. Stubs that throw
`TODO()` make red→green mechanically verifiable, which is what lets the human stop reviewing
diffs line by line. The signatures in spec §4 are the contract; your tests pin it, and Pass 2
is not allowed to drift from it.

## Order of work

**Step 1 — read before you write.** For every screen, read the existing Fragment/ViewModel
first. Non-negotiable #5: never assume a textbook version of a business rule. Specifically:

- `presentation/ui/auth/age_verification/AgeFragment.kt` — the real age rule
- `repo/network/name/DefaultNameRepo.kt` — `getErrorFromUsername`'s **actual** username rules.
  **Record them in spec §4.4 before writing any username test.** If a rule looks wrong, flag it
  in your report; do not silently change it.
- `presentation/ui/auth/email_auth/enter_password/EnterPasswordViewModel.kt` — current password
  handling
- `presentation/ui/auth/email_auth/enter_email/BasicEmailScreen.kt` — the existing Compose
  screen, which is the template the others copy

**Step 2 — create stubs matching spec §4 exactly.** Every use case, error type, `UiState`,
`Action`, repo interface, and ViewModel named in §4. Bodies are `TODO("Pass 2")`. Signatures
must match the spec character for character — if the spec is wrong or under-specified, stop and
ask rather than improvising a different shape.

**Step 3 — write the tests.** Cover, per screen:

- **Use cases** — JUnit + Truth, no mocking. Every branch, plus every required edge case listed
  under that screen in spec §4. The edge-case lists there are a floor, not a ceiling.
- **ViewModels** — Turbine on the `StateFlow`. Initial state; each `Action`; error mapping to
  `UiText`; loading state entered and left; one-shot `AuthEvent`s emitted exactly once and not
  replayed on reconfiguration.
- **Repos with I/O** (`UsernameRepo`, `PhoneAuthRepo`) — MockK at the Firebase boundary. Assert
  on the mapped `Result`/`DataError`, never on a leaked Firebase exception type.
- **Compose screens** — `ui-test-junit4` against the **stateless** `XyzScreen(state, onAction)`.
  Render each meaningful state (empty, typing, error, loading, valid) and assert the right nodes
  exist and the right `Action` fires on click. No ViewModel, no Hilt.

**Test doubles:** hand-written fakes implementing the domain repo interfaces (deterministic, no
framework) for use-case and ViewModel tests. MockK **only** at the Firebase boundary in repo
tests. Reuse `test_utils/CoroutineTestRule`. Follow the naming style already in
`ValidatePasswordUseCaseTest` — backticked sentence names.

**Step 4 — rewrite, do not extend, `ValidatePasswordUseCaseTest`.** Decision D6 changes the
contract to return *all* failures as a `List`, so its existing
``returns the FIRST rule violated`` test asserts behavior that is being deliberately removed.

## Hard constraints

- **No implementation.** Every stub body is `TODO()`. If you catch yourself writing an `if`
  inside a use case, stop.
- **Every test must fail with `NotImplementedError`, never a compile error.** Run
  `./gradlew testDebugUnitTest` and confirm this before you finish. A compile error means the
  contract is inconsistent — fix the stub, not the test.
- **Do not modify** `domain/core/Result.kt`, `ValidateEmailUseCase`, or `EmailError`. They are
  already correct.
- **Do not delete legacy code** (non-negotiable #2). Old Fragments and XML stay until Pass 2
  replaces each screen.
- The dependency-direction grep in `domain/AGENTS.md` must return nothing (`java.time` is a
  legitimate exception — it is not an Android dependency).

## Definition of done

- [ ] Stubs exist for every symbol in spec §4
- [ ] `./gradlew testDebugUnitTest` compiles; every new test fails with `NotImplementedError`
- [ ] Compose screen tests compile and run against stateless composables
- [ ] Actual username rules recorded in spec §4.4
- [ ] `git diff --stat` shows only test files, stub files, and the spec edit

Report at the end: the test count per screen, any place the spec's pinned signature turned out
to be unworkable (and what you would change it to), and any business rule you found in the
legacy code that contradicts the spec.
