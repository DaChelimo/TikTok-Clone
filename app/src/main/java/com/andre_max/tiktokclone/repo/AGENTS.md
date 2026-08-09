# `repo/` — Agent Briefing

This is the data layer: `repo/network/*` (Firebase Realtime Database + Storage) and
`repo/local/*` (device media/storage access). It's the **only** layer allowed to see both a
DTO/SDK type and the domain model — that's the whole reason mapping happens here.

## `safeCall` — the data-layer boundary

`repo/network/utils/SafeCall.kt` wraps every suspend call that touches Firebase/network/disk.
It:

- Rethrows `CancellationException` unconditionally — **never** swallow it, that breaks
  structured concurrency (a cancelled coroutine that looks like it succeeded is a real bug
  class, not a style nitpick).
- Maps platform/SDK exceptions to `DataError` (`domain/core/Result.kt`) at this boundary only.
  Nothing above this layer should ever see a `FirebaseException`, an `IOException`, etc.

(`safeAccess`, the legacy predecessor that swallowed cancellation, was deleted with the last
`TheResult`-based repo on 2026-08-09 — every repo goes through `safeCall`/`safeAuthCall` now.)

## Mappers

`toDomain()` mapping functions live here, in the data layer, next to the repo that produces
the domain model. `toUi()` mapping lives in `presentation`, never here — a repo file must not
import anything from `presentation`. If a mapper needs both a DTO and a UI model in the same
file, that's a sign the boundary has already blurred; split it.

## Offline / cache semantics

Reading is always `Success` from the local source of truth (Room, where present) — a failed
network refresh does **not** turn cached data into an `Error`. Instead it sets an
`isOffline`/`isStale` flag that flows up into the `UiState` for the presentation layer to
render. Don't invent a new "stale-but-error" path; use the existing flag convention.

## Sync (WorkManager)

When a repo syncs multiple independent remote sources, each source reports its own
success/failure — never AND multiple sources into one Boolean. Aggregate results explicitly
so a partial failure is visible to the caller. Enqueue sync work via WorkManager; never block
`Application.onCreate` with real sync work.

## Dependency direction

`repo` depends on `domain` (implements its repo interfaces) and on Android/Firebase SDKs. It
must not import from `presentation`. Interfaces (`VideosRepo`, `AuthRepo`, ...) live in
`domain` or directly under `repo/<feature>/` when the interface is data-layer-specific
(no domain use case references it) — check the existing pattern for the feature you're in
before deciding which.
