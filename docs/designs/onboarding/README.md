# Onboarding design references

Mobbin TikTok Android captures, mapped to the six screens in
[../../plans/ONBOARDING_FEATURE_SPEC.md](../../plans/ONBOARDING_FEATURE_SPEC.md) §4.
Source files live in `../screens/` (a broad capture of the whole app — most of it is feed,
profile, camera and DM surfaces irrelevant to this slice).

| File | Screen | Source | State shown |
| --- | --- | --- | --- |
| `01-age-verification.png` | 1 — Age verification | `screens 29` | Empty; wheel picker Jul/Aug/Sep · 17/18/19 · 2022/2023; Next disabled |
| `01b-age-verification-filled.png` | 1 — Age verification | `screens 33` | Filled (February 18, 1995); Next in loading state |
| `02-basic-email.png` | 2 — Basic email | `screens 32` | Email tab active, address entered, Continue enabled, domain suggestion chips |
| `03-enter-password.png` | 3 — Enter password | `screens 26` | Empty; rule checklist grey; Next disabled |
| `03b-enter-password-valid.png` | 3 — Enter password | `screens 28` | All rules ticked green; strength bar "Strong"; Next enabled |
| `04-create-username.png` | 4 — Create username | `screens 30` | "Create nickname", `samlee18`, 8/30 counter, Skip, Confirm |
| `05-basic-phone.png` | 5 — Basic phone | `screens 34` | Phone tab active, `US +1` country picker, Send code disabled |
| — | 6 — Select basic auth | see below | — |
| `OUT-OF-SCOPE-signup-landing.png` | *(not this phase)* | `screens 27` | Sign-up landing with Facebook/Google — excluded by decision D2 |

## Screen 6 has no standalone frame — and that validates the plan

In the real app, "select basic auth" is not its own screen: it is the `Phone | Email` tab bar
visible at the top of both `02-basic-email.png` and `05-basic-phone.png`. Those two images are
the same screen with a different tab selected.

That is exactly the structure spec §4.6 plans to build — one Compose screen with a `TabRow` +
`HorizontalPager`, replacing the `ViewPager2` + `FragmentStateAdapter` + two Fragment shells.
The design confirms the collapse is correct rather than merely convenient. Use `02` and `05`
together as screen 6's reference.

## Two places the design contradicts the spec

Raise these before implementing — do not silently pick one (`AGENTS.md` §6).

1. **Password rules.** Mobbin shows *"8 characters (20 max) · 1 letter and 1 number · 1 special
   character"* — no separate uppercase/lowercase requirement, and a **20-character maximum**
   the spec does not have. Spec §4.3 (decision D6) requires digit + special + upper + lower.
   D6 is a deliberate, user-approved rule change, so it governs the *rules*; the design governs
   the *layout* (a live checklist, which D6 already anticipated). The 20-char cap is genuinely
   new information and is unresolved.

2. **Nickname ≠ username.** Mobbin's screen is *"Create nickname"* — skippable, changeable
   later, 30-char limit, auto-assigned if skipped, and **not unique**. Spec §4.4 specifies a
   *username* with a debounced Firebase availability check (`UsernameRepo.isUsernameAvailable`),
   which is what the current `CreateUsernameFragment` / `NameRepo.doesNameExist` code actually
   does. These are different product concepts and the difference changes the entire screen —
   whether `UsernameRepo`, debounce, and the availability state exist at all.
