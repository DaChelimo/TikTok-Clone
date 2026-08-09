# Clean Architecture + Compose Migration Plan

This tracks the migration from the original Fragment/LiveData/Groupie architecture to
Clean Architecture + Jetpack Compose. Read this at the start of any new session on this topic.

## Principles we're following

1. **App compiles and runs after every step.** Never leave it broken between sessions.
2. **Old and new systems coexist during migration** (strangler-fig approach). `TheResult` /
   `safeAccess` / `BaseViewModel` / `BaseFragment` stay alive until nothing references them, then
   get deleted in the final step. This keeps every intermediate step non-breaking.
3. **Tests first for anything with logic** — especially use cases. Red → Green → Refactor.
4. **One feature vertical slice at a time**, not one architectural layer across all features.
5. Every generated file is reviewed and approved before moving to the next step.

## Core patterns (the "why", not just the "what")

Moved to the layer-local `AGENTS.md` files (place-based knowledge, loaded automatically when
working in that tree) and to [docs/SPEC.md](SPEC.md) (product-level contracts):

- [domain/AGENTS.md](../app/src/main/java/com/andre_max/tiktokclone/domain/AGENTS.md) —
  `Result`/`DataError`, use-case conventions, the "read existing logic first" rule.
- [presentation/AGENTS.md](../app/src/main/java/com/andre_max/tiktokclone/presentation/AGENTS.md) —
  UiState, Route/Screen split, Actions, recomposition, `LocalContext`, side effects.
- [repo/AGENTS.md](../app/src/main/java/com/andre_max/tiktokclone/repo/AGENTS.md) — `safeCall`,
  mappers, offline/cache semantics, WorkManager sync.
- [SPEC.md](SPEC.md) §6 — the cross-cutting behavior contract these all implement.

## Migration order

| # | Slice | Status | Teaches |
|---|---|---|---|
| 0 | `Result` foundation (`Result`, `ResultExt`, `DataError`, `safeCall`) | ✅ Done | typed errors, variance, safeCall |
| 1 | Validation use cases (`ValidatePasswordUseCase`, `ValidateEmailUseCase`) | ✅ Done | use case with no repo, TDD loop |
| 2 | Auth repo → interface + move Android SDK types out of data layer | ✅ Done | dependency inversion for OAuth flows |
| 3 | Auth use cases (sign up/in, combining validation + repo) | ✅ Done (2026-08-09) | use cases that combine repo + logic |
| 4 | One auth screen → Compose + UiState + StateFlow | ✅ Done (age verification) | full UI chain, collectAsStateWithLifecycle |
| 5 | Remaining auth screens | ✅ **Done (2026-08-09)** — all of `presentation/ui/auth/**` is Compose: landing, age, phone\|email tabs, password, SMS code, username, log-in. Legacy `AuthRepo`/`NameRepo`/`GoogleBody`/`EmailBody` deleted. | repetition, user-driven |
| 6 | Videos/feed slice | ✅ **Done (2026-08-09)** — `HomeFragment` is a Compose shell over a `VerticalPager` feed; comment *data* layer (`domain/comments`, `FirebaseCommentsRepo`) landed here too because the feed screen embeds the comment sheet. `GetFeedBatchUseCase` is the seam the future recommendation model plugs into. | VerticalPager, recomposition, HomeAction, pagination, callbackFlow |
| 7a | Comments / large_video detail | ✅ **Done (2026-08-09)** — `LargeVideoFragment` is a Compose shell over the shared `components/video/` `VideoPage` + `CommentSheet` (extracted from the feed when this second consumer appeared). | shared components, SavedStateHandle nav args |
| 7b | Profile | ✅ **Done (2026-08-09)** — one `ProfileViewModel` for own/other profiles; `GetVisibleVideoTypesUseCase` encodes the newly-decided visibility rule; fixed the legacy Liked/Private tab label swap. | SavedStateHandle fallback, HorizontalPager tabs, video-frame thumbnails (coil-video) |
| 7c | Upload, discover, search | Not started | pattern repetition |
| 8 | Delete `TheResult`, `safeAccess`, `BaseViewModel`, `BaseFragment` | Not started | cleanup once unreferenced |

> **Slice 3 was previously marked done in error.** Until 2026-08-09 `domain/auth/usecase/` held
> only validators and `CheckUsernameAvailabilityUseCase` — there was no sign-up or sign-in use
> case, and the migrated flow reached Home without ever creating a Firebase account. The account
> creation still lived in the legacy `create_username` ViewModel that nothing navigated to.
> `CompleteSignUpUseCase` closed it. This is the second time a row here was ticked from file
> presence rather than behaviour; check what a class *does* before ticking.
>
> Auth's remaining gaps are configuration, not code: Google and Facebook sign-in are implemented
> but render disabled until their provider config exists — see
> [plans/AUTH_COMPLETION_PLAN.md](plans/AUTH_COMPLETION_PLAN.md) §5.


## Files created so far

Grown too large to enumerate by hand without drifting out of date again (that's what happened
to this section — it stopped at the password use case while five more screens landed). The
source tree is the source of truth: `domain/auth/`, `presentation/ui/auth/`,
`repo/network/{phone,username}/` for the auth vertical; `domain/core/Result.kt` +
`repo/network/utils/SafeCall.kt` for the shared foundation.

## Known issues / TODOs surfaced during migration

- ~~`domain/videos/VideosRepo.kt` has inconsistent return types mid-migration~~ Fixed in
  slice #6 (2026-08-09): the domain interface is fully typed (`Result`/`DataError.Network`),
  implemented by `FirebaseVideosRepo`. The legacy `repo/network/videos/VideosRepo` +
  `DefaultVideosRepo` pair stays alive for legacy callers (upload, profile) until those
  slices migrate.
- Feed dedupe (slice #6, deliberate deviation): the backing `limitToFirst(12)` query returns
  an overlapping window on every "load more"; the legacy feed appended those duplicates
  verbatim, which a keyed Compose pager would crash on. `GetFeedBatchUseCase` filters
  already-shown ids instead. Two smaller wired-up gaps: tapping a comment's heart now
  actually likes it (the legacy click lambda was accidentally empty), and the share intent
  got a mime type + chooser (the legacy bare `ACTION_SEND` could throw
  `ActivityNotFoundException`).
- Sandbox note: local dev environment can't run `./gradlew test` — missing `google-services.json`
  (Firebase config with credentials, correctly not present in this environment). Run tests in
  Android Studio locally instead.
- **Process lesson (2026-07-03):** the first draft of `ValidatePasswordUseCase` used invented
  rules (required uppercase, no special-char check) instead of reading the existing rule in
  `presentation/ui/auth/email_auth/enter_password/EnterPasswordViewModel.kt` first (which only
  required length ≥ 8 + any letter + any digit). Caught by the user, not by the process.
  **Rule going forward: before writing any use case that replaces existing logic, grep for and
  read the current implementation first — never assume a "textbook" version of a business rule.**
  The password rule was then intentionally upgraded (per user's explicit request) to require a
  special character, uppercase, and lowercase — so the current stricter version is deliberate,
  not another guess.
