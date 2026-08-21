# Pass 2 — Implement until green

> Paste everything below the line into a fresh session. Run **after** Pass 1 leaves a red suite.
> Prerequisite: Mobbin design exports in `docs/designs/onboarding/`.

---

You are working on the TikTok-Clone Android app. Read `AGENTS.md`, then
`docs/plans/ONBOARDING_FEATURE_SPEC.md` in full, then the nested `AGENTS.md` for each layer you
touch.

Pass 1 left a complete, compiling, **failing** test suite plus stubs. Your job: make it green,
screen by screen, and build the Compose UI. Work through all six screens in spec order without
stopping for human approval — the personas in Pass 3 are the gate.

## Per-screen loop

Work **inside-out**: domain → data → ViewModel → UI → Fragment shell. Never UI-first; the UI
shape falls out of the state contract, so building it first means rewriting it.

1. **Domain** — implement the use case until its tests pass. Then stop and re-run only that
   screen's tests.
2. **Data** — repo interface already in `domain`; implement in `repo` wrapped in `safeCall`.
   Skip if the screen has no I/O.
3. **ViewModel** — `@HiltViewModel`, immutable `UiState` as `StateFlow`, single `onAction`,
   domain errors mapped to `UiText`, no repo exposed publicly.
4. **UI** — open the Mobbin reference for this screen **before writing the composable**. The
   design is the layout spec: icons, step order, copy, spacing. The old XML is consulted only
   for *behavior*, never for layout — it is known to have the wrong icons and missing steps,
   which is a large part of why this migration exists. Build stateless
   `XyzScreen(state, onAction)` with `@Preview`, plus stateful `XyzRoute`.
5. **Fragment shell** — `ComposeView` + `collectAsStateWithLifecycle()` + nav callbacks only.
   **Hard cap 50 lines.** Over that means logic leaked back in.
6. **Delete** the old XML layout and its ViewBinding/DataBinding references, in this same
   screen's slice — not later, not as a separate cleanup.
7. **Verify** — `./gradlew testDebugUnitTest` green; build, install and launch via the
   `android-cli` skill; screenshot the screen to `docs/designs/screenshots/`.
8. **Tick** that screen's row in spec §6, then move to the next screen.

## Hard constraints

- **Do not change a test to make it pass.** If a test looks wrong, stop and report it — a test
  that is wrong means the contract is wrong, and that is a human decision. Silently editing the
  suite defeats the entire process.
- **Do not change a pinned signature from spec §4** without escalating for the same reason.
- **The app compiles and runs after every screen** (non-negotiable #1). Never leave the tree
  broken between screens.
- **Do not delete `TheResult` / `safeAccess` / `BaseViewModel` / `BaseFragment`**
  (non-negotiable #2). They stay until nothing references them, in their own dedicated step.
- **Do not extract shared components on anticipation** (`AGENTS.md` §5). Duplicate a text field
  across two screens; extract on the **third** occurrence, or when the second screen
  demonstrably needs the identical API. A component extracted at one caller is a review finding.
- **If a Mobbin design is missing for a screen, stop and ask.** Do not invent a layout.
- Recomposition rules from `presentation/AGENTS.md` are not optional: immutable data classes,
  `ImmutableList` in `UiState`, `key = {}` on every lazy list, pass the narrowest state slice a
  composable needs, `LocalContext.current` read at the point of use.
- Screen 6 (select basic auth) cannot start until screens 2 and 5 are done — it collapses both.

## Behavior changes that are deliberate

These are decisions D5 and D6, user-approved. They are the reason this is a migration and not a
port. Do not "restore" the old behavior when you see it in the legacy code:

- Minimum age is **13**, computed as true age from full date of birth (old code: 9, year-only).
- `ValidatePasswordUseCase` returns **all** failed rules and takes an `identity` argument to
  reject passwords containing the user's name/email (old code: first error only, no identity
  check).

## Definition of done

- [ ] `./gradlew testDebugUnitTest` fully green
- [ ] `./gradlew assembleDebug` produces an installable APK; app launches
- [ ] All six screens rendered in Compose; every corresponding XML layout deleted
- [ ] Every Fragment shell ≤50 lines
- [ ] Screenshot of each screen saved
- [ ] Spec §6 screen table fully ticked
- [ ] Dependency-direction grep from `domain/AGENTS.md` returns nothing

Report at the end: per screen, what you implemented, any test you believe is wrong (and why you
did not change it), and any place the Mobbin design contradicted the legacy behavior — with
which one you followed.
