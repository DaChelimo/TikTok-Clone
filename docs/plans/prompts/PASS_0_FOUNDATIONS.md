# Pass 0 — Toolchain & Foundations

> Paste everything below the line into a fresh session. Run this **before** Pass 1.
> Prerequisite: `app/google-services.json` must already be in place (you, not the agent).

---

You are working on the TikTok-Clone Android app. Read `AGENTS.md`, then
`docs/plans/ONBOARDING_FEATURE_SPEC.md` (especially §3) before doing anything.

This session does **Pass 0 only**: unblock the build and lay the foundations every onboarding
screen depends on. Do not touch any screen, use case, or ViewModel logic beyond what §3d
specifies. If you find yourself writing a `Screen` composable, you have overrun the scope.

## Tasks, in order

1. **Verify the starting state.** Run `./gradlew assembleDebug` and capture the actual failure.
   Confirm it is the AGP 7.4.2 / D8 dexing crash described in
   `docs/plans/ONBOARDING_MIGRATION.md` §0b′ and not something new. Report what you find before
   changing anything.

2. **Upgrade AGP 7.4.2 → 8.x and the Gradle wrapper 7.6.4 → matching 8.x** (decision D1).
   Expect fallout and fix it in this same pass: `namespace` in `app/build.gradle` replacing the
   manifest `package` attribute, stricter manifest merging, possibly `buildConfig true` under
   `buildFeatures`. These are AGP-8 migration requirements, not new bugs.

3. **Enable core library desugaring.** `minSdk 21` cannot use `java.time`, and
   `ValidateAgeUseCase` needs `LocalDate`.

4. **Add the missing dependencies** listed in spec §3c. Several conventions the nested
   `AGENTS.md` files already mandate are currently unresolvable — notably
   `collectAsStateWithLifecycle()` and `ImmutableList`.

5. **Build the foundations** in spec §3d: `UiText` + `asString()`, `TikTokTheme` (Material 3),
   the `AuthEvent` one-shot channel type, and `@HiltViewModel` + constructor injection for the
   existing auth ViewModels.

6. **Get it running.** Use the `android-cli` skill to start an emulator, install the debug APK,
   launch it, and capture a screenshot proving the app opens. Save to `docs/designs/baseline/`.

## Hard constraints

- **The app must build and run when you finish** (non-negotiable #1). If the AGP 8 upgrade
  cascades into something large, stop and report rather than leaving a broken tree.
- **Do not pre-build a shared auth component library** (`AuthTextField`, `PrimaryButton`, etc.).
  `AGENTS.md` §5 — extract on the second real caller, not on anticipation.
- **Do not delete legacy code** as a drive-by (non-negotiable #2). `TheResult`, `safeAccess`,
  `BaseViewModel`, `BaseFragment` stay until their own dedicated step.
- `./gradlew test` needs `google-services.json`. If a Gradle task fails, confirm *which* task
  failed before concluding the code is broken.

## Definition of done

- [ ] `./gradlew assembleDebug` produces an installable APK
- [ ] `./gradlew testDebugUnitTest` runs (existing tests still pass)
- [ ] App launches on the emulator; baseline screenshot saved
- [ ] Spec §6 "Pass 0 — foundations" checklist fully ticked
- [ ] `docs/plans/ONBOARDING_MIGRATION.md` open decision #3 (AGP) marked resolved

Report at the end: what broke during the upgrade, what you changed, and anything you found that
the spec did not anticipate.
