# TikTok-Clone

A TikTok-style short-form video Android app: sign up, record or upload vertical videos with
tags and a description, and scroll an endless feed with like/comment/share/follow. Built solo
as a from-scratch Clean Architecture + Jetpack Compose app on a Firebase + Cloudflare backend.

## Tech stack

**Language & async**
- Kotlin
- Coroutines + Flow (`StateFlow`/`UiState` on every screen)

**UI**
- Jetpack Compose end-to-end — every screen, including camera capture (`AndroidView` interop)
- Coil (images + video-frame thumbnails)
- ExoPlayer (video playback)
- CameraView (video capture) + a custom `VideoCompressor` for upload compression

**Architecture**
- Clean Architecture, three layers with dependencies enforced inward:
  `presentation → domain ← repo`
- `domain`: pure Kotlin, zero Android/Firebase imports — use cases + a typed `Result`/`DataError`
  error model
- `repo`: the only layer allowed to see both a DTO and a domain model
- Hilt for dependency injection

**Backend**
- Firebase: Realtime Database, Auth (email, Google, Facebook), Crashlytics, Analytics, Cloud
  Messaging
- Cloudflare R2 + a Cloudflare Worker for video file storage — chosen over Firebase Storage to
  avoid egress costs (the dominant cost of a video feed) and to keep the Firebase project off
  the Blaze billing plan; the Worker verifies the caller's Firebase ID token and returns a
  presigned upload URL, so the device uploads directly to R2

**Testing**
- JUnit + Truth for domain/use-case logic (TDD: red → green → refactor)
- MockK for the Firebase/Android boundary in the repo layer
- Compose screens are tested as stateless functions of `UiState` — no ViewModel or Hilt needed

## Architecture & migration story

The app started as Fragment/LiveData/Groupie/DataBinding MVVM and was migrated to Clean
Architecture + Compose as a **strangler-fig migration**: old and new systems coexisted
side-by-side, one full vertical feature slice at a time (auth, then feed, then comments, then
profile, upload, discover, search, inbox/settings), with the app kept compiling and running at
every single step. Legacy code for a slice was only deleted once nothing referenced it anymore
— never as a drive-by cleanup mid-feature. The migration finished with a dedicated deletion
pass that removed the last of the legacy foundation (`BaseFragment`/`BaseViewModel`, Groupie,
DataBinding, Dexter, Glide) once every screen was confirmed Compose-only.

Notable things this migration caught and fixed along the way, rather than just relocating:
- A tag page that legacy code never actually wired navigation to (unreachable, despite being
  described in the product spec)
- A popular-tags query silently ordering by a field that didn't exist
- A user-search query bounded on the wrong field, so it always returned nothing
- A comment-like tap whose click handler was accidentally a no-op
- A share intent missing a MIME type, which could throw at runtime

## Features

| Surface | Status |
| --- | --- |
| **Auth** | Sign-up (age gate → phone/email → password or SMS code → username with debounced availability check) and log-in, both fully in Compose. Account creation happens atomically on the final step — an abandoned sign-up can't leave a half-created account behind, with rollback on failure. Credentials never travel as nav arguments. |
| **Feed** | Endless vertical-pager video feed (ExoPlayer), paginated in batches with dedupe against overlapping query windows. Tap to play/pause, double-tap to like, optimistic like/follow with automatic revert on failure. In-feed comment sheet with live updates. |
| **Upload** | Record via in-app camera or pick from gallery → preview → compress → upload → post, as one typed pipeline with a real failure state instead of a silently-cancelled coroutine. |
| **Comments** | Live comment thread with like, reachable from the feed and from a standalone video-detail screen that shares the same components. |
| **Profile** | Own and other-user profiles behind one ViewModel. Public/Liked/Private video tabs, with a visibility rule (owner-only private, opt-in liked-tab visibility) enforced by a dedicated use case rather than left to the UI. |
| **Discover / tags** | Tag rows with video previews → tag detail grid → video detail, with tag popularity actually ranked by count. |
| **Search** | Debounced username search against a correctly-indexed Firebase query. |
| **Settings** | Reachable settings screen (previously an unreachable stub) with the liked-videos visibility toggle. |

## Roadmap

**In progress / near-term**
- Complete Google and Facebook sign-in provider configuration (both are implemented in code,
  currently disabled pending config)
- Reply-to-comment UI (the data model already supports replies; no screen renders them yet)
- Room-backed offline/cache reads, so a failed network refresh degrades to stale-but-visible
  data instead of an error state

**Upcoming / needs product definition first**
- A real notifications/inbox system (currently a placeholder — no notification model exists
  yet; needs product decisions on what generates a notification and how it's stored, before
  there's anything to build)
- A ranked feed recommendation model (the batch-fetch use case is already structured as the
  seam this plugs into; today's feed is unranked, dedupe-only)

## License
```
MIT License

Copyright (c) 2021 Andre-max

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
