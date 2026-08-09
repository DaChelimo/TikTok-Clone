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
| 2 | Auth repo → interface + move Android SDK types out of data layer | ✅ Done (phone/username repos; email/password need no repo) | dependency inversion for OAuth flows |
| 3 | Auth use cases (sign up/in with email, combining validation + repo) | ✅ Done | use cases that combine repo + logic |
| 4 | One auth screen → Compose + UiState + StateFlow (first Fragment→Compose swap) | ✅ Done (age verification) | full UI chain, collectAsStateWithLifecycle |
| 5 | Remaining auth screens | 🔵 In progress — basic email, enter password done (real bodies). Create username, basic phone, select-basic-auth exist only as Pass-1 stubs (`TODO("Pass 2")` throughout use cases/repos/ViewModels/Screens) — Pass 2 not started. Sign-up landing (screen 7) not started, blocked on Pass 2 for the others. Enter-code still blocked on OTP dep. | repetition, user-driven |

> Slices 3–5 are planned screen-by-screen in
> [plans/ONBOARDING_MIGRATION.md](plans/ONBOARDING_MIGRATION.md), with the frozen execution
> contract in [plans/ONBOARDING_FEATURE_SPEC.md](plans/ONBOARDING_FEATURE_SPEC.md) (see that
> file's D2 for current scope — amended 2026-08-07 to add sign-up landing).
>
> **Correction (2026-08-07):** an earlier pass through this file marked create-username,
> basic-phone, and select-basic-auth "✅ Done" based on their files existing. They don't have
> real bodies — verify with `grep -rln 'TODO("Pass 2")' app/src/main/java` before trusting a
> "done" status in this doc again; file presence means Pass 1 (stubs + red tests) ran, not that
> Pass 2 (implementation) did.
| 6 | Videos/feed slice | Not started | LazyColumn, recomposition, FeedAction, pagination |
| 7 | Comments, profile, upload, discover, search | Not started | pattern repetition |
| 8 | Delete `TheResult`, `safeAccess`, `BaseViewModel`, `BaseFragment` | Not started | cleanup once unreferenced |

## Files created so far

Grown too large to enumerate by hand without drifting out of date again (that's what happened
to this section — it stopped at the password use case while five more screens landed). The
source tree is the source of truth: `domain/auth/`, `presentation/ui/auth/`,
`repo/network/{phone,username}/` for the auth vertical; `domain/core/Result.kt` +
`repo/network/utils/SafeCall.kt` for the shared foundation.

## Known issues / TODOs surfaced during migration

- `domain/videos/VideosRepo.kt` has inconsistent return types mid-migration
  (`Result<RemoteVideo, Nothing>` should be `Result<RemoteVideo, DataError>`, `isVideoLiked` still
  returns old `TheResult`). Fix when we do the videos/feed slice (#6), not before.
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
