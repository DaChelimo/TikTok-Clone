# `presentation/` — Agent Briefing

This tree is **mid-migration**: legacy Fragment/LiveData/Groupie screens and new
Compose/UiState/StateFlow screens coexist deliberately (strangler-fig). Check which pattern
the screen you're touching already uses before assuming — see
[docs/CLEAN_ARCHITECTURE_MIGRATION.md](../../../../../../../../docs/CLEAN_ARCHITECTURE_MIGRATION.md)
for which slice is done vs. not started. **The whole of `presentation/ui/auth/**` is Compose**
as of 2026-08-09 — copy its conventions rather than the older screens'. Feed, profile, upload,
discover, search, comments, inbox and settings are all still Fragment/ViewModel/LiveData.

Auth's Fragments are `ComposeView` shells with navigation callbacks and nothing else; if you
find yourself adding logic to one, it belongs in the ViewModel.

## Conventions for new/migrated Compose screens

- **One immutable `data class` `UiState` per screen**, exposed as `StateFlow`, collected via
  `collectAsStateWithLifecycle()`. Not LiveData, not mutable state hoisted into the ViewModel.
- **Stateful/stateless split**: `XyzRoute` (wires the ViewModel, owns the `collectAsState`)
  + stateless `XyzScreen` (pure function of `state` + lambdas, no ViewModel reference) — so
  `XyzScreen` is directly `@Preview`-able and unit-testable without a ViewModel.
- **Actions**: once a screen has more than ~4 distinct events, collapse them into one sealed
  interface `XyzAction` + a single `onAction: (XyzAction) -> Unit`, instead of N separate
  lambda parameters threaded through the composable tree.
- **Error text**: ViewModels map domain error enums (`DataError`, `PasswordError`, ...) to a
  `UiText` sealed type (`Dynamic(String)` or `Resource(@StringRes id, args)`). Only the
  Composable resolves it, via `stringResource`. Use cases and repos never touch `Context` or
  `R.string` — that's what keeps them unit-testable on the JVM.
- **Recomposition**: immutable data classes, `ImmutableList` (kotlinx.collections.immutable)
  for any list in `UiState`, `key = { it.id }` on every `LazyColumn`/`LazyRow`, and pass the
  narrowest state slice a composable actually needs (not the whole `UiState`) — that's what
  makes recomposition surgical instead of whole-screen.
- **`LocalContext.current`**: read it at the exact composable that needs it. Never add a
  `Context` parameter to thread it down from a caller.
- **Side effects**: a fire-and-forget effect triggered by a user gesture (e.g. launching a
  share `Intent`) goes directly in the click lambda — no `LaunchedEffect` wrapper needed.
  `LaunchedEffect`/`DisposableEffect` are for effects bound to composition lifecycle, not for
  click handlers.

## Legacy Fragment/LiveData/Groupie screens

Leave `BaseFragment`/`BaseViewModel`/`TheResult`/`safeAccess` alone unless you're doing the
migration slice that replaces them. They're intentionally still alive per the strangler-fig
plan — do not "clean them up" as a side effect of an unrelated change, and do not add new
screens on top of them (new screens are Compose, per the migration order in
[docs/CLEAN_ARCHITECTURE_MIGRATION.md](../../../../../../../../docs/CLEAN_ARCHITECTURE_MIGRATION.md)).

## Dependency direction

`presentation` may depend on `domain` and `repo` (through Hilt-injected interfaces). It must
not reach into another feature's ViewModel or Fragment directly — cross-feature state goes
through a shared repo/use case, not a direct reference.
