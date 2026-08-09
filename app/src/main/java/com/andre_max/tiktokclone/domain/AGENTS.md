# `domain/` — Agent Briefing

This layer is pure Kotlin business logic: use cases, domain models' contracts, and typed
errors. It has **zero** Android SDK or Firebase dependencies. See
[docs/SPEC.md](../../../../../../../../docs/SPEC.md) for the product-level behavior these use
cases implement; this file is only the layer-local *how*.

## Dependency direction (enforced by hand until a lint rule exists)

`domain` may be imported by `repo` and `presentation`. `domain` imports **nothing** from
either. Prove it before committing:

```bash
grep -rn "^import" app/src/main/java/com/andre_max/tiktokclone/domain | grep -Ei "android|firebase|com\.andre_max\.tiktokclone\.(repo|presentation)"
```

Must return nothing. If it doesn't, the boundary has already been violated — move the
offending type reference behind an interface instead of importing the concrete type.

## Result / error conventions

- `Result<D, E : AppError>` (`domain/core/Result.kt`) is the Philipp Lackner style — typed
  domain errors, no `Loading` state. `Loading` belongs in the presentation layer's `UiState`,
  never in a suspend function's return type.
- `DataError` (`domain/core/Result.kt`, `Network`/`Local` enums) is for cross-cutting
  data-layer failures (network down, file not found, insufficient storage). Use it as the `E`
  type for anything a repo can produce.
- Feature-specific errors (e.g. `PasswordError`, `EmailError`) live next to the use case that
  produces them, under `domain/<feature>/usecase/`, not in `domain/core/`. A validation error
  is not a data error — don't reuse `DataError` for it.

## Use cases — when to write one, when not to

A use case exists when there is a business rule, a multi-repo combination, or reuse across
ViewModels. It is **not** a reflexive 1:1 wrapper over every repo method — a ViewModel calling
a single repo method directly is fine.

A use case does not need a repo dependency. `ValidatePasswordUseCase` and
`ValidateEmailUseCase` are pure logic with no I/O — that's the common case for validation
rules, and it's what makes them trivially unit-testable on the JVM with no mocking.

## Before replacing existing business logic

**Grep for and read the current implementation before writing a use case that replaces it.**
Do not assume a "textbook" version of a rule (e.g. password complexity, email format) matches
what's actually enforced today — the current ViewModel or ad-hoc validation is the source of
truth for what "correct" means here, not a general convention. If the current rule looks
wrong, flag it to the user and let them decide whether to change it as a deliberate,
called-out step — not silently, inside an unrelated refactor.

*(This rule exists because the first draft of `ValidatePasswordUseCase` invented rules that
didn't match `EnterPasswordViewModel`'s actual logic — caught by the user, not by process.
Full story in [docs/CLEAN_ARCHITECTURE_MIGRATION.md](../../../../../../../../docs/CLEAN_ARCHITECTURE_MIGRATION.md).)*

## Known drift

`domain/videos/VideosRepo.kt` mixes `Result<D, DataError>` with legacy `Result<D, Nothing>`
and `TheResult` return types mid-migration. Don't fix it opportunistically — it's scoped for
the videos/feed migration slice. Match the surrounding style in whichever method you touch
rather than "fixing" the file's consistency as a drive-by.
