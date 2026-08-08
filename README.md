# Google Android Interview Prep — SDE-2 (4+ YOE)

> Single source of truth for Google Android SDE-2 (L4) interview preparation.
> Technical depth, mock Q&A, behavioral prep, and DSA — all in one repo.

---

## Quick Start

1. **Follow the 7-day plan:** [STUDY_PLAN.md](STUDY_PLAN.md)
2. **Domain prep:** Read files `01`–`17` (start with Critical topics below)
3. **Flashcard review:** Open [learn.html](learn.html) in a browser
4. **Print / offline:** Open [print-guide.html](print-guide.html) or regenerate with `python3 scripts/build_print_guide.py`
5. **Behavioral:** [18_googleyness_and_leadership.md](18_googleyness_and_leadership.md) + [19_googleyness_questions.md](19_googleyness_questions.md)
6. **Coding round:** [20_dsa_solutions.md](20_dsa_solutions.md) (27 problems)

---

## Interview Format

```
Round 1: Phone Screen      — DSA + basic Android
Round 2: Domain-Specific   — Deep Android knowledge (files 01–17)
Round 3: Coding Round      — Algorithm problem-solving (file 20)
Round 4: System Design     — Android system design (file 15)
Round 5: Googleyness       — Behavioral / leadership (files 18–19)
```

---

## Study Guide

### Technical — Android Domain (Kotlin)

Each `.kt` file is self-contained: concepts, code snippets, interview tips (💡), gotchas (⚠️), and follow-up questions (❓).

| # | Topic | File | Priority |
|---|-------|------|----------|
| 01 | Android Fundamentals — Lifecycle, Components, Intents, Manifest | [01_android_fundamentals.kt](01_android_fundamentals.kt) | 🔴 Critical |
| 02 | App Architecture — MVVM, MVI, Clean Architecture, Navigation | [02_app_architecture.kt](02_app_architecture.kt) | 🔴 Critical |
| 03 | Jetpack Compose — State, Side Effects, Navigation, Performance | [03_jetpack_compose.kt](03_jetpack_compose.kt) | 🔴 Critical |
| 04 | Kotlin Coroutines & Flows — Structured Concurrency, Operators | [04_coroutines_and_flows.kt](04_coroutines_and_flows.kt) | 🔴 Critical |
| 05 | Data Persistence — Room, DataStore, Paging 3 | [05_data_persistence.kt](05_data_persistence.kt) | 🟠 High |
| 06 | Networking — Retrofit, OkHttp, Interceptors | [06_networking.kt](06_networking.kt) | 🟠 High |
| 07 | Android Internals — Process Model, Binder, ART, ANR | [07_android_internals.kt](07_android_internals.kt) | 🟠 High |
| 08 | Performance Optimization — UI, Memory, Battery, Vitals | [08_performance_optimization.kt](08_performance_optimization.kt) | 🔴 Critical |
| 09 | Background Work — WorkManager, FGS, FCM, Doze | [09_background_work.kt](09_background_work.kt) | 🟠 High |
| 10 | Dependency Injection — Hilt, Scoping, Workers | [10_dependency_injection.kt](10_dependency_injection.kt) | 🟠 High |
| 11 | Testing — Unit, UI, Fakes, Turbine | [11_testing.kt](11_testing.kt) | 🟠 High |
| 12 | Security — Keystore, Pinning, Play Integrity | [12_security.kt](12_security.kt) | 🟡 Medium |
| 13 | Modern Android — KMP, Modularization, Adaptive UI | [13_modern_android.kt](13_modern_android.kt) | 🟡 Medium |
| 14 | Design Patterns & Kotlin Advanced | [14_design_patterns_kotlin.kt](14_design_patterns_kotlin.kt) | 🟠 High |
| 15 | System Design — Offline-first, Chat, Image Loader | [15_system_design_android.kt](15_system_design_android.kt) | 🔴 Critical |
| 16 | Mock Q&A — 22 tiered interview questions with answers | [16_interview_questions.md](16_interview_questions.md) | 🔴 Critical |
| 17 | Amit Shekhar Q&A — Extended deep-dive answers (full reference) | [17_amit_shekhar_questions_answers.kt](17_amit_shekhar_questions_answers.kt) | 🟠 High |

### Behavioral — Googleyness & Leadership

| # | Topic | File |
|---|-------|------|
| 18 | Prep guide — STAR framework, exemplars, story bank | [18_googleyness_and_leadership.md](18_googleyness_and_leadership.md) |
| 19 | Question bank — 96 real reported G&L questions | [19_googleyness_questions.md](19_googleyness_questions.md) |

### DSA — Coding Round

| # | Topic | File |
|---|-------|------|
| 20 | DSA — 27 problems across 10 patterns | [20_dsa_solutions.md](20_dsa_solutions.md) |

### Study Tools

| File | Description |
|------|-------------|
| [STUDY_PLAN.md](STUDY_PLAN.md) | 7-day intensive schedule with daily checklists |
| [learn.html](learn.html) | Flashcard UI with progress tracking (19 topics) |
| [print-guide.html](print-guide.html) | Full printable guide — all `.kt` + `.md` embedded |

---

## Build Scripts

Regenerate artifacts after editing source files:

```bash
# Regenerate printable HTML guide after editing source files
python3 scripts/build_print_guide.py
```

Optional: `scripts/trim_amit_shekhar.py` stubs duplicate sections in file 17 — **not recommended** if you use the full Amit Shekhar reference for prep.

---

## How to Use

1. **Start with [STUDY_PLAN.md](STUDY_PLAN.md)** — structured 7-day path
2. **Critical topics first (🔴)** — appear in almost every Google Android interview
3. **Practice out loud** — [16_interview_questions.md](16_interview_questions.md) for mock answers
4. **Build 6–8 STAR stories** — [18_googleyness_and_leadership.md](18_googleyness_and_leadership.md)
5. **Review with flashcards** — [learn.html](learn.html) between sessions
6. **Code on paper** — [20_dsa_solutions.md](20_dsa_solutions.md) patterns before coding rounds

---

## SDE-2 Expectations (L4)

- **Depth over breadth** — explain *why*, not just *how*
- **Production experience** — discuss trade-offs from real projects
- **Performance awareness** — think at Google scale (500M+ DAU)
- **Modern stack** — Compose, Coroutines, Hilt, Room, WorkManager
- **Testing mindset** — testable architecture is non-negotiable

---

## Repo Structure

```
01–15   Android technical topics (.kt)
16      Domain round mock Q&A (.md)
17      Extended Q&A — full Amit Shekhar reference (.kt)
18–19   Googleyness prep (.md)
20      DSA — 27 problems (.md)
STUDY_PLAN.md      7-day schedule
learn.html         Interactive flashcards
print-guide.html   Full printable guide (generated)
scripts/           build_print_guide.py (trim_amit_shekhar.py optional)
README.md          Start here
```

Code snippets are reference material, not a Gradle project. Copy patterns into your own projects or use them for whiteboard discussion.

---

*Last updated: August 2026*
