# TikTok-Clone — Architecture & Behavioral Spec

This is the product's source of truth for **what the system does and how it's structured**.
[AGENTS.md](../AGENTS.md) routes work here and to the nested per-layer files; this document
holds the facts those files assume. If code and this spec disagree, that's a P0 — stop and
reconcile with the user, don't silently pick one.

This spec describes the **target architecture** the codebase is migrating toward (see
[CLEAN_ARCHITECTURE_MIGRATION.md](CLEAN_ARCHITECTURE_MIGRATION.md) for exactly how far each
slice has gotten). Sections below call out where current code still diverges.

## 1. What the product is

A TikTok-style short-form video app: users sign up/log in, record or upload short vertical
videos with tags and a description, and consume an endless feed of other users' videos with
like/comment/share interactions. Supporting surfaces: profile (own + others'), discover/tags,
search, inbox (notifications), and settings.

## 2. Tech stack (as actually wired, not aspirational)

- **Language**: Kotlin. **Async**: Coroutines + Flow.
- **DI**: Hilt.
- **Backend**: Firebase — Realtime Database (`repo/network/*`), Storage (video/image
  upload), Auth (email, Google, Facebook; Twitter integration is dead — see §7), Crashlytics,
  Analytics, Cloud Messaging.
- **UI**: Jetpack Compose + `StateFlow` + `UiState` on every screen (migration completed
  2026-08-09). Fragments remain only as thin `ComposeView` shells inside the XML nav graph;
  Groupie/DataBinding/Dexter/Glide are gone (Coil handles images, including video-frame
  thumbnails). See
  [presentation/AGENTS.md](../app/src/main/java/com/andre_max/tiktokclone/presentation/AGENTS.md).
- **Media**: ExoPlayer for playback, CameraView for capture, a custom `VideoCompressor` for
  upload compression.
- **Local persistence**: Room is planned for the offline/cache model in §6 but not yet wired
  into any repo — today, reads go straight to Firebase. Treat §6 as the target, not the
  current state, until a repo actually implements it.
- **Known drift from `README.md`**: the README's tech-stack list mentions ktlint, Detekt,
  Spotless, and Jacoco Gradle plugins and a staging/production flavor split. None of these are
  currently applied in `app/build.gradle` or `settings.gradle` (verified 2026-07-25). Don't
  assume `./gradlew ktlintCheck`, `detekt`, or flavor-specific tasks exist — check
  `app/build.gradle`'s `plugins {}` block first if you need one of them.

## 3. Domain entities

These are the shapes actually persisted/transferred today (`models/`); the equivalent
`domain`-layer models are introduced use-case-by-use-case as slices migrate (§ in
[domain/AGENTS.md](../app/src/main/java/com/andre_max/tiktokclone/domain/AGENTS.md)).

| Entity | Key fields | Notes |
| --- | --- | --- |
| `User` | `uid`, `username`, `followers`, `following`, `totalLikes`, `profilePictureUrl?`, `showLikedVideos` | `showLikedVideos` is toggled from the settings screen (2026-08-09) and gates the liked tab's visibility to other users. |
| `RemoteVideo` | `videoId`, `url`, `description?`, `tags: Map<String,String>`, `duration`, `dateCreated`, `likes`, `views`, `authorUid`, `totalCommentsSize` | Parcelable. Tags are a map, not a list — key/value shape isn't documented in code; confirm with the user before relying on it. |
| `VideoType` | enum `LIKED` / `PRIVATE` / `PUBLIC` | Selects which of a user's video collections is being read (also used as a Firebase path segment via `toString()`). |
| `Comment` | `commentId`, `authorUid`, `commentText`, `commentLikes`, `dateCreated`, `replies: List<Reply>` | Replies are nested under a comment, not a separate top-level collection. |
| `Reply` | `replyKey`, `authorUid`, `replyText`, `replyLikes`, `dateCreated` | |
| `Tag` | `name`, `count` | Powers the discover/tags surface. |
| `Progress` | enum `IDLE` / `ACTIVE` / `DONE` / `FAILED` | Upload progress state machine. |
| `LocalVideo` / `LocalImage` / `LocalRecordLocation` | — | Device-local capture artifacts before upload; see `models/local/`. |

## 4. Architecture — layers and dependency direction

```
presentation  →  domain  ←  repo
  (Compose/UiState)           (Firebase, Room,
                               device media)
```

- **`domain`**: pure Kotlin, zero Android/Firebase imports. Use cases + `Result`/`DataError` +
  repo interfaces. Detail: [domain/AGENTS.md](../app/src/main/java/com/andre_max/tiktokclone/domain/AGENTS.md).
- **`repo`**: implements domain's repo interfaces against Firebase/device APIs; the only layer
  allowed to see both a DTO and a domain model. Detail:
  [repo/AGENTS.md](../app/src/main/java/com/andre_max/tiktokclone/repo/AGENTS.md).
- **`presentation`**: ViewModels + UI. Depends on `domain` (use cases) and `repo` (direct repo
  calls where no use case exists yet — allowed, see domain/AGENTS.md's "when to write a use
  case"). Detail:
  [presentation/AGENTS.md](../app/src/main/java/com/andre_max/tiktokclone/presentation/AGENTS.md).

Dependencies point inward (`presentation`/`repo` → `domain`, never the reverse). This is
enforced by hand today — no lint rule exists yet — via the grep in `domain/AGENTS.md`.

**Strangler-fig migration — completed 2026-08-09**: the legacy system
(`BaseFragment`/`BaseViewModel`/`TheResult`/`safeAccess`, Groupie, DataBinding) coexisted with
the new one (`Result`/`safeCall`/`UiState`) until each vertical migrated, then was deleted in
dedicated commits once unreferenced. Only the new system exists now; the route it took is
recorded in [CLEAN_ARCHITECTURE_MIGRATION.md](CLEAN_ARCHITECTURE_MIGRATION.md).

## 5. Feature surfaces

Each row is what the screen does today per the existing Fragment/ViewModel, not a redesign.
Where behavior isn't fully determinable from code alone, that's called out rather than guessed.

| Surface | Package | Behavior |
| --- | --- | --- |
| Auth | `presentation/ui/auth/*` | **Fully migrated to Clean Architecture + Compose (2026-08-09).** Sign-up: landing → age (min 13, true age from full date of birth) → phone \| email tabs → password (all failed rules reported, 8–20 chars, no identity substring) or SMS code → username (debounced availability check). Log-in reuses the same email/phone screens and skips username. **The account is created on the create-username screen, not when credentials are collected** — an abandoned sign-up must not leave a credentialed account behind; `CompleteSignUpUseCase` rolls back each step on failure. Credentials travel in an in-memory `SignUpSession`, never a nav argument. Sign-out lives on the profile menu. Google and Facebook buttons render disabled until their config exists (§7). Twitter auth removed 2026-07-25. |
| Feed / home | `presentation/ui/home` | **Migrated to Clean Architecture + Compose (2026-08-09).** Vertical pager feed of `RemoteVideo`s via ExoPlayer, fetched in batches of 12 through `GetFeedBatchUseCase` — the seam where the future recommendation model plugs in (today: unranked fetch + already-shown-id dedupe, because the backing query window overlaps between fetches). Near-end pagination (within 5 pages), single tap play/pause, double-tap like, optimistic like/follow with revert-on-error. The in-feed comment sheet (live thread, send, comment-like) ships with this surface; likes remain three non-atomic writes (partial failure still a known, accepted risk). Signed-out users can watch but like/follow/comment are inert, as before. |
| Upload | `presentation/ui/upload`, `repo/local/*`, `repo/network/storage` | **Migrated to Clean Architecture + Compose (2026-08-09).** Capture (CameraView inside `AndroidView`) or pick a local video (gallery tabs; images render but were never selectable, unchanged) → preview (looping) → post. `PostVideoUseCase` owns compress (`VideoCompressor`, typed `COMPRESSION_FAILED` instead of a cancelled coroutine) → Storage upload → `VideosRepo.saveVideo` (two-way storage + tag count/tag-videos writes, all awaited). Posts are always public — the legacy screen hardcoded `isPrivate=false` with no toggle, preserved as-is. `Progress` still drives the upload UI state machine. A finished upload lands on the Home tab. |
| Comments / video detail | `presentation/ui/large_video`, `presentation/ui/components/video`, `repo/network/comment` | **Migrated to Clean Architecture + Compose (2026-08-09).** The single-video detail screen (reached from profile/tag/search grids) loops its video and shares the feed's `VideoPage` + `CommentSheet` components: live thread, send, comment-like. Reply UI does not exist yet in either legacy or migrated form (the `Reply` model is persisted but no screen renders it) — still unspecified, and there is still no pagination cap on a thread. |
| Profile | `presentation/ui/profile` | **Migrated to Clean Architecture + Compose (2026-08-09).** Own vs. other-user profile behind one `ProfileViewModel` (uid nav arg, falling back to the signed-in user on the Me tab). Visibility rule (decided 2026-08-09, enforced by `GetVisibleVideoTypesUseCase`): private videos are owner-only; the liked tab shows to others only when the owner's `showLikedVideos` flag is on; public always. Tab order Public → Liked → Private with matching labels (the legacy screen labelled the Liked tab "private" and vice versa). Sign-out confirm lives on the own-profile menu; the menu/edit controls no longer render inertly on other users' profiles. Signed-out Me tab shows the sign-up prompt. |
| Discover / tags | `presentation/ui/discover`, `presentation/ui/selected_tag`, `repo/network/tag` | **Migrated to Clean Architecture + Compose (2026-08-09).** Tag rows (name, count, horizontal video previews) → tag page (grid of the tag's videos) → video detail. Note: the legacy code never wired this navigation — the tag page was an empty unreachable fragment; the migration implemented what this row always described. Popular-tags ranking fixed to actually order by count descending. |
| Search | `presentation/ui/search`, `repo/network/suggestions` | **Migrated to Clean Architecture + Compose (2026-08-09).** Debounced (1250ms) username prefix search over `users/*/basic-data/username`; the arrow icon copies a suggestion into the box, tapping the row opens that user's profile. The never-implemented "search results" screen was removed — search resolves to profiles only until a results surface is actually specified. |
| Inbox | `presentation/ui/inbox` | **Compose (2026-08-09), still a placeholder by design.** The legacy screen was only ever the static "notifications will be displayed here" message; no notification model exists anywhere in the backend. A real inbox needs product decisions (what generates a notification, where it's stored) before there is anything to build. |
| Settings | `presentation/ui/settings` | **Migrated to Clean Architecture + Compose (2026-08-09) and made reachable** (own-profile menu → Settings; the legacy fragment was an unreachable "TextView" stub outside the nav graph). Holds the one specified setting: the `showLikedVideos` toggle (closing the `User` model's "no settings UI yet" TODO), optimistic write with revert-on-error. |

## 6. Cross-cutting behavior rules

These apply regardless of which screen or layer you're in — the nested `AGENTS.md` files
restate the layer-local mechanics, this is the contract itself.

- **Errors are typed, not stringly-typed.** Every suspend function that can fail returns
  `Result<D, E : AppError>`. Presentation maps `E` to user-facing `UiText`; nothing below
  presentation touches `Context`/`R.string`.
- **`Loading` is a UI concern, not a domain one.** A `Result` is only ever `Success` or
  `Error`; `UiState.isLoading` (or similar) is what represents in-flight work.
- **Offline reads never look like errors.** Once Room-backed caching lands for a given repo
  (target state, not current — see §2), a failed network refresh sets `isOffline`/`isStale`
  on `UiState`; cached data still renders as `Success`. Don't invent a new state for this.
- **Cancellation is never swallowed.** `CancellationException` propagates through every
  `safeCall`/`safeAccess` boundary unconditionally.
- **Multi-source sync reports per-source, not ANDed.** If a future sync job pulls from N
  independent sources, each source's success/failure is visible independently in the result.

## 7. Known dead/removed integrations

Do not "fix" these opportunistically — they're flagged in source comments as deliberately
disabled, pending an explicit decision to either finish removing them or replace the
dependency:

- **Twitter auth** — `twitter-core` SDK removed (jcenter shutdown, SDK is EOL). Source in
  `CustomTwitterLoginButton.kt`, `AuthRepo.kt`, the Log-in/Sign-up VMs and Fragments, and
  `App.kt` still reference it.
- **`editcodeview`** — jcenter-only artifact, no longer resolvable. Source in
  `EnterCodeFragment.kt`, `EnterCodeViewModel.kt`, `fragment_enter_code.xml` still reference
  it.

If a task touches either area, surface this to the user before assuming which direction to
go (finish the removal vs. find a replacement dependency) — it's a product decision, not an
engineering default.

## 8. Testing strategy

- **Domain (use cases, pure logic)**: JUnit + Truth, no mocking needed when a use case has no
  repo dependency (e.g. `ValidatePasswordUseCaseTest`). TDD (red → green → refactor) for any
  new use case per the migration principles.
- **Repo layer**: mock the Firebase/Android boundary (MockK/Mockito), assert on the mapped
  `Result`/`DataError`, not on SDK-specific exception types leaking through.
- **Compose screens**: the stateless `XyzScreen` composable is the test target (pure function
  of `UiState` + lambdas) — no ViewModel or Hilt needed to test it.
- **Local dev caveat**: `./gradlew test` doesn't run in a sandboxed environment missing
  `google-services.json` (Firebase config with credentials, correctly not committed). Run
  tests in Android Studio locally instead of assuming a broken sandbox run means broken code.

## 9. Non-negotiables

Violating any of these is a defect regardless of what the task asked for.

1. **The app compiles and runs after every step.** Never leave it broken between sessions or
   commits — this is what makes the strangler-fig migration safe to do incrementally.
2. **Old and new systems coexist until fully unreferenced**, then get deleted in one
   dedicated step — never delete a legacy piece "while I'm in here" on an unrelated task.
3. **Tests first for anything with logic**, especially use cases.
4. **One feature vertical slice at a time**, not one architectural layer across every
   feature at once (don't migrate all ViewModels to `StateFlow` in one PR; migrate one
   screen's full stack).
5. **Read the current implementation before replacing business logic.** Never assume a
   "textbook" version of a rule matches what's actually enforced — grep for it first (see
   [domain/AGENTS.md](../app/src/main/java/com/andre_max/tiktokclone/domain/AGENTS.md)).
6. **Dependencies point inward.** `domain` imports nothing from `repo`/`presentation`.

## 10. Maintaining this spec

- This file describes behavior and structure (the "what"/"why"). Layer-local coding
  conventions (the "how") live in the nested `AGENTS.md` files — don't duplicate them here;
  link to them instead.
- When a feature surface in §5 changes behavior, update its row here in the same PR — this
  is what keeps the spec from drifting the way `README.md`'s tech-stack list already has.
- Migration progress (what's done, what's next) is tracked in
  [CLEAN_ARCHITECTURE_MIGRATION.md](CLEAN_ARCHITECTURE_MIGRATION.md), not here — this file is
  the destination, that file is the route.
