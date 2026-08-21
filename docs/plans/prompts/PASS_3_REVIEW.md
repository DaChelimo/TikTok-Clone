# Pass 3 — Three-persona review gate

> Paste everything below the line into a fresh session. Run per screen as Pass 2 completes it,
> or over all six at the end.

---

You are the review coordinator for the TikTok-Clone onboarding migration. Read `AGENTS.md` and
`docs/plans/ONBOARDING_FEATURE_SPEC.md` (especially §7) first.

Your job: run three **independent** review personas over the completed onboarding work, collect
their verdicts, and decide what goes back to Pass 2 versus what reaches the human.

## Rules for the review

- **The three personas must be separate agents with no shared context.** Independence is what
  makes three reviews worth more than one — do not summarize one persona's findings into
  another's prompt.
- **Every finding must be falsifiable**: file, line, and a concrete failure scenario (specific
  inputs → specific wrong output or crash). "This could be cleaner", "consider extracting",
  and "might be a problem" are not findings. Drop them.
- **Any FAIL bounces that screen back to Pass 2** with the findings attached. It re-enters
  review after the fix. Only when all three PASS does the screen count as done.
- **You do not fix anything in this session.** Reviewing and fixing in one pass destroys the
  independence of the gate.

## The three personas

Full check-lists are in spec §7. In brief:

1. **Security engineer** — secrets/PII in logs and crash reports, validation enforced only in
   the UI (client-side gate = bypassable), age gate bypassable via rotation/back-nav/process
   death, username availability as a user-enumeration vector, error text that leaks whether an
   account exists, swallowed `CancellationException`.
2. **QA engineer** — test coverage against the required edge cases in spec §4, does the app
   actually run (use the `android-cli` skill: emulator, install, launch, screenshot), does each
   screen match its Mobbin reference in `docs/designs/onboarding/` (icons, step order, copy),
   rotation and process-death state restoration, back navigation, loading/error states
   reachable, accessibility (content descriptions, touch targets, contrast).
3. **Architecture reviewer** — the non-negotiables in `AGENTS.md` §4, the dependency-direction
   grep (must return nothing — paste the output), Fragment shells ≤50 lines, no God ViewModel,
   **no premature abstraction** (a component extracted at a single caller is a finding, per
   `AGENTS.md` §5), `ImmutableList` + `key =` on lazy lists, narrowest state slice passed down,
   `UiText` resolved only inside composables.

## How to run it

Use the Workflow tool with the script below. It fans out three personas per screen in parallel,
then adjudicates. Pass the screens you want reviewed as `args` — e.g.
`["age-verification"]` for a single-screen gate, or all six at the end.

```js
export const meta = {
  name: 'onboarding-persona-review',
  description: 'Three independent personas review each migrated onboarding screen',
  phases: [{ title: 'Review' }, { title: 'Adjudicate' }],
}

const SCREENS = args?.length ? args : [
  'age-verification', 'basic-email', 'enter-password',
  'create-username', 'basic-phone', 'select-basic-auth',
]

const BRIEF = `Read AGENTS.md and docs/plans/ONBOARDING_FEATURE_SPEC.md first.
Review ONLY the onboarding screen named below. Every finding needs file, line, and a concrete
failure scenario (inputs -> wrong output). Drop anything you cannot make falsifiable.
Verdict is FAIL if any finding is severity high or critical.`

const PERSONAS = [
  { key: 'security', brief: 'You are a security engineer. Focus: secrets/PII in logs, validation enforced only client-side, auth-gate bypass via rotation/back-nav/process death, user enumeration, account-existence leaks in error text, swallowed CancellationException. See spec section 7.' },
  { key: 'qa', brief: 'You are a QA engineer. Focus: test coverage vs the required edge cases in spec section 4, whether the app actually builds/installs/launches (use the android-cli skill), fidelity to the Mobbin reference in docs/designs/onboarding/, rotation and process-death restoration, back nav, loading/error states, accessibility.' },
  { key: 'architecture', brief: 'You are a staff engineer reviewing for long-term quality. Focus: AGENTS.md section 4 non-negotiables, the dependency-direction grep from domain/AGENTS.md (run it and paste the output), Fragment shell <=50 lines, God ViewModels, premature abstraction (a component extracted at one caller is a finding), ImmutableList and key= on lazy lists, UiText resolved only in composables.' },
]

const VERDICT = {
  type: 'object',
  required: ['verdict', 'findings'],
  properties: {
    verdict: { type: 'string', enum: ['PASS', 'FAIL'] },
    findings: {
      type: 'array',
      items: {
        type: 'object',
        required: ['severity', 'file', 'summary', 'failureScenario'],
        properties: {
          severity: { type: 'string', enum: ['critical', 'high', 'medium', 'low'] },
          file: { type: 'string' },
          line: { type: 'number' },
          summary: { type: 'string' },
          failureScenario: { type: 'string' },
        },
      },
    },
  },
}

const reviewed = await pipeline(
  SCREENS,
  (screen) => parallel(PERSONAS.map((p) => () =>
    agent(`${BRIEF}\n\nScreen: ${screen}\n\n${p.brief}`,
      { label: `${p.key}:${screen}`, phase: 'Review', schema: VERDICT })
      .then((v) => ({ persona: p.key, ...v })))),
  (verdicts, screen) => {
    const ok = verdicts.filter(Boolean)
    const failed = ok.filter((v) => v.verdict === 'FAIL')
    log(`${screen}: ${failed.length ? 'FAIL (' + failed.map(f => f.persona).join(', ') + ')' : 'PASS'}`)
    return { screen, passed: failed.length === 0, verdicts: ok }
  },
)

return {
  passed: reviewed.filter(Boolean).filter((r) => r.passed).map((r) => r.screen),
  needsRework: reviewed.filter(Boolean).filter((r) => !r.passed),
}
```

## After the workflow returns

1. **Any screen with `passed: false`** — write its findings into a rework brief and hand it to a
   Pass 2 session. Do not fix them here.
2. **All screens passed** — verify the phase close-out items in spec §6 (spec/migration docs
   updated, legacy XML confirmed deleted, `git grep` clean), then produce a **human review
   summary**: what changed per screen, the deliberate behavior changes (D5 age 13 + true age,
   D6 all-failures password + identity check), screenshots, and anything the personas waved
   through that you personally think deserves a second look.

That summary is what the human reads instead of the diff. Make it good enough to be trusted.
