export const meta = {
  name: 'feature-migration-cycle',
  description: 'Runs a feature vertical slice end-to-end: red test suite, then per-unit implement → one critical 3-persona review → one unverified rework pass',
  whenToUse: 'Migrating a feature vertical (onboarding, home feed, profile...) to Clean Architecture + Compose, once its FEATURE_SPEC and pass prompts exist and the build is green.',
  phases: [
    { title: 'Tests', detail: 'one agent writes stubs + the full red suite' },
    { title: 'Implement', detail: 'serial, one agent per unit — units share a Gradle module so they cannot run in parallel' },
    { title: 'Review', detail: 'security / QA / architecture personas in parallel, isolated contexts — runs once per unit' },
    { title: 'Rework', detail: 'fix every persona finding in one pass — not re-reviewed by personas afterward' },
  ],
}

// ---------------------------------------------------------------------------
// Config. Everything feature-specific is an arg, so this script is reusable for
// the home feed, profile, upload... not just onboarding.
// ---------------------------------------------------------------------------

const SPEC = (args && args.spec) || 'docs/plans/ONBOARDING_FEATURE_SPEC.md'
const PROMPTS = (args && args.prompts) || 'docs/plans/prompts'
const UNITS = (args && args.units) || [
  'age-verification',
  'basic-email',
  'enter-password',
  'create-username',
  'basic-phone',
  'select-basic-auth',
]
const SKIP_TESTS = !!(args && args.skipTests)

// The red suite is written once for the WHOLE feature (UNITS), but implementation can be
// staged a few units at a time so there is a human checkpoint before committing the full
// fan-out. Defaults to implementing everything the tests cover.
const IMPL_UNITS = (args && args.implementUnits) || UNITS

// The prompt FILES are the contract — agents read them rather than being handed a
// paraphrase. That keeps one source of truth and stops the orchestrator quietly
// drifting an agent's brief from what was reviewed and approved.
const PREAMBLE =
  `Read AGENTS.md, then ${SPEC}, then the nested AGENTS.md for every layer you touch. ` +
  `Follow them exactly — the non-negotiables in AGENTS.md section 4 override anything you infer from the code.`

// This is the ONLY review pass a unit gets — there is no re-review after rework, so a
// finding missed here never gets a second look. Each persona prompt is written to push
// for maximum rigor and to make every finding actionable enough that the rework agent
// (which will never talk to this persona again) can fix it correctly without asking.
const REVIEW_RIGOR =
  'This is the ONLY review this unit will receive — there is no second pass after rework, so be exhaustive now. ' +
  'Read every file you touch fully, not just the diff hunks; a bug outside the diff that the unit newly exercises still ' +
  'counts. Actively try to break the code: think through concrete inputs, race conditions, rotation, process death, and ' +
  'network failure, not just what looks wrong on read-through. Every finding must be immediately actionable by someone who ' +
  'will never talk to you again: exact file, exact line, exact failure scenario, and — where it is not obvious — what the ' +
  'fix should be. Do not soften or omit a real finding to keep the summary short.'

const PERSONAS = [
  {
    key: 'security',
    lens:
      'You are a security engineer. Focus on: secrets, passwords or PII reaching Timber logs or Crashlytics; ' +
      'validation enforced only in the UI (a client-side-only gate is bypassable); auth gates defeatable via rotation, ' +
      'back navigation or process death; username availability as a user-enumeration vector; error copy that leaks ' +
      'whether an account exists; swallowed CancellationException.',
  },
  {
    key: 'qa',
    lens:
      'You are a QA engineer. Focus on: test coverage against the required edge cases in the spec section 4; whether the app ' +
      'actually builds, installs and launches (use the android-cli skill and capture a screenshot); fidelity to the Mobbin ' +
      'reference in docs/designs/onboarding/ — icons, step order, copy; rotation and process-death state restoration; ' +
      'back navigation; loading and error states reachable; accessibility (content descriptions, touch targets, contrast).',
  },
  {
    key: 'architecture',
    lens:
      'You are a staff engineer reviewing for long-term quality. Focus on: the non-negotiables in AGENTS.md section 4; the ' +
      'dependency-direction grep from domain/AGENTS.md (run it and paste the output — java.time is a legitimate exception); ' +
      'Fragment shells at or under 50 lines; God ViewModels; premature abstraction (a component extracted at a single caller ' +
      'is a finding, per AGENTS.md section 5); ImmutableList and key= on lazy lists; the narrowest state slice passed down; ' +
      'UiText resolved only inside composables.',
  },
]

const VERDICT_SCHEMA = {
  type: 'object',
  required: ['verdict', 'findings'],
  properties: {
    verdict: { type: 'string', enum: ['PASS', 'FAIL'] },
    summary: { type: 'string', description: 'One or two sentences on the overall state of this unit.' },
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
          failureScenario: {
            type: 'string',
            description: 'Concrete inputs or state leading to a concrete wrong output or crash. Not a style opinion.',
          },
          suggestedFix: {
            type: 'string',
            description: 'What the fix should look like. This finding gets fixed with no chance to ask you follow-up questions — be specific.',
          },
        },
      },
    },
  },
}

// ---------------------------------------------------------------------------
// Pass 1 — one agent writes stubs + the entire red suite.
//
// This MUST be a different context from the implementers. An agent that writes both
// the test and the code shapes the test around what it happened to build; isolated
// contexts are what make "tests first" mean anything.
// ---------------------------------------------------------------------------

if (!SKIP_TESTS) {
  phase('Tests')
  const testReport = await agent(
    `${PREAMBLE}\n\nNow read ${PROMPTS}/PASS_1_TESTS.md and execute it in full for these units: ` +
      `${UNITS.join(', ')}.\n\nWrite NO implementation. Every stub body is TODO(). You are done when ` +
      `./gradlew testDebugUnitTest compiles and every new test fails with NotImplementedError — never a compile error.`,
    { label: 'pass1:red-suite', phase: 'Tests' },
  )
  log(`Red suite written. ${testReport ? String(testReport).slice(0, 300) : 'no report returned'}`)
}

// ---------------------------------------------------------------------------
// Pass 2 + 3 — serial per unit.
//
// Serial is not a limitation to route around: every unit edits the same Gradle module,
// and select-basic-auth structurally depends on basic-email and basic-phone. Parallel
// implementers would collide, and worktree isolation would just move the collision to
// a six-way merge of the same files.
// ---------------------------------------------------------------------------

const results = []

for (const unit of IMPL_UNITS) {
  phase('Implement')
  await agent(
    `${PREAMBLE}\n\nNow read ${PROMPTS}/PASS_2_IMPLEMENT.md and execute it for ONE unit only: "${unit}".\n\n` +
      `Do not touch any other unit. Work inside-out: domain, then data, then ViewModel, then UI, then the Fragment shell. ` +
      `Open the Mobbin reference for this screen BEFORE writing the composable — the design is the layout spec, the old XML ` +
      `is consulted only for behavior. If the design is missing, stop and say so rather than inventing a layout.\n\n` +
      `You may not edit a test to make it pass, and you may not change a signature pinned in the spec section 4. ` +
      `If either looks wrong, leave it and report it.`,
    { label: `implement:${unit}`, phase: 'Implement' },
  )

  phase('Review')
  const verdicts = (
    await parallel(
      PERSONAS.map((p) => () =>
        agent(
          `${PREAMBLE}\n\nYou are reviewing ONE unit: "${unit}". Read ${PROMPTS}/PASS_3_REVIEW.md for the rules of this gate.\n\n` +
            `${REVIEW_RIGOR}\n\n${p.lens}\n\nEvery finding needs a file, a line, a concrete failure scenario, and a suggested ` +
            `fix. Discard anything you cannot make falsifiable — "this could be cleaner" is not a finding. Return FAIL if any ` +
            `finding is high or critical. You review only; you fix nothing.`,
          { label: `${p.key}:${unit}`, phase: 'Review', schema: VERDICT_SCHEMA },
        ).then((v) => ({ persona: p.key, ...v })),
      ),
    )
  ).filter(Boolean)

  const failed = verdicts.filter((v) => v.verdict === 'FAIL')
  let reworked = false

  if (failed.length === 0) {
    log(`${unit}: PASS (all three personas)`)
  } else {
    const blocking = failed
      .flatMap((v) => v.findings.map((f) => ({ persona: v.persona, ...f })))
      .filter((f) => f.severity === 'critical' || f.severity === 'high')

    log(`${unit}: FAIL from ${failed.map((f) => f.persona).join(', ')} — ${blocking.length} blocking finding(s), reworking ` +
      `(unverified — no re-review after this)`)

    phase('Rework')
    await agent(
      `${PREAMBLE}\n\nUnit "${unit}" failed its review gate. This is a ONE-SHOT fix — no persona will re-review this unit ` +
        `afterward, so be as thorough and careful as if you were the last line of defense. Fix ONLY these findings, then stop:\n\n` +
        `${JSON.stringify(blocking, null, 2)}\n\n` +
        `Constraints unchanged: do not edit a test to make it pass, do not change a signature pinned in spec section 4, ` +
        `and leave the app building and running (run the build/tests yourself before you finish — no one will catch a broken ` +
        `build after you). If a finding is wrong or you disagree, do not silently ignore it — fix what is real and state ` +
        `clearly which you rejected and why.`,
      { label: `rework:${unit}`, phase: 'Rework' },
    )
    reworked = true
  }

  // The unit's fate is decided by this single review, not by any later verification — a
  // reworked unit is unverified by design (that's the tradeoff this run mode makes for
  // speed/cost). Record it as such rather than pretending it "passed".
  results.push({ unit, verdicts, reworked })
}

const clean = results.filter((r) => !r.reworked).map((r) => r.unit)
const reworkedUnits = results.filter((r) => r.reworked)

log(
  `Cycle finished. ${clean.length}/${results.length} units passed review cleanly, ` +
    `${reworkedUnits.length} were reworked once and NOT re-reviewed — check those by hand.`,
)

return {
  reviewedCleanly: clean,
  reworkedUnverified: reworkedUnits.map((r) => ({
    unit: r.unit,
    findingsFixed: r.verdicts
      .filter((v) => v.verdict === 'FAIL')
      .flatMap((v) => v.findings.map((f) => ({ persona: v.persona, ...f }))),
  })),
  notReached: IMPL_UNITS.filter((u) => !results.some((r) => r.unit === u)),
  notYetImplemented: UNITS.filter((u) => !IMPL_UNITS.includes(u)),
}
