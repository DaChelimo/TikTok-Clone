# `presentation/` — Agent Briefing

This tree is **fully Compose/UiState/StateFlow** (migration completed 2026-08-09 — the route
is logged in
[docs/CLEAN_ARCHITECTURE_MIGRATION.md](../../../../../../../../docs/CLEAN_ARCHITECTURE_MIGRATION.md)).

Every Fragment is a `ComposeView` shell with navigation callbacks and system-bar chrome and
nothing else; if you find yourself adding logic to one, it belongs in the ViewModel. Shared
video building blocks (`VideoPage`, `CommentSheet`, `VideoPlayer`, `VideoThumbnail`) live in
`ui/components/video/` — features must not reach into each other's packages for UI.

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

## Dependency direction

`presentation` may depend on `domain` and `repo` (through Hilt-injected interfaces). It must
not reach into another feature's ViewModel or Fragment directly — cross-feature state goes
through a shared repo/use case, not a direct reference.
