# 1-Week Study Plan — Google Android SDE-2 (L4)

> **Target:** 4+ years of experience | **Daily time:** 3–4 hours | **Start:** [README.md](README.md)

Each day has three blocks: **Read** (concepts), **Practice** (out loud / on paper), **Review** (flashcards).

---

## Day 1 — Fundamentals + Architecture

**Goal:** Nail lifecycle ordering and MVVM/UDF cold.

| Block | Time | Activity |
|-------|------|----------|
| Read | 90 min | [01_android_fundamentals.kt](01_android_fundamentals.kt), [02_app_architecture.kt](02_app_architecture.kt) |
| Practice | 60 min | [16_interview_questions.md](16_interview_questions.md) Tier 1: Q1–Q6 — answer out loud |
| Review | 30 min | [learn.html](learn.html) — Topics 1–2 |

**Checklist:**
- [ ] Draw Activity A→B lifecycle from memory
- [ ] Explain ViewModel vs SavedStateHandle vs Room for state
- [ ] Describe offline-first repository pattern end-to-end
- [ ] Whiteboard MVVM data flow (UI → VM → Repo → Room/API)

---

## Day 2 — Compose + Coroutines

**Goal:** Recomposition, side effects, structured concurrency.

| Block | Time | Activity |
|-------|------|----------|
| Read | 90 min | [03_jetpack_compose.kt](03_jetpack_compose.kt), [04_coroutines_and_flows.kt](04_coroutines_and_flows.kt) |
| Practice | 60 min | Tier 1: Q2 (StateFlow vs SharedFlow vs Channel), Q3 (Compose optimization), Q4 (structured concurrency) |
| Review | 30 min | learn.html — Topics 3–4 |

**Checklist:**
- [ ] Explain Composition → Layout → Drawing phases
- [ ] When to use LaunchedEffect vs DisposableEffect vs rememberCoroutineScope
- [ ] coroutineScope vs supervisorScope with example
- [ ] Why Channel beats StateFlow for one-time navigation events

---

## Day 3 — Data + Networking + DI

**Goal:** Room as SSOT, network layer, Hilt scoping.

| Block | Time | Activity |
|-------|------|----------|
| Read | 90 min | [05_data_persistence.kt](05_data_persistence.kt), [06_networking.kt](06_networking.kt), [10_dependency_injection.kt](10_dependency_injection.kt) |
| Practice | 60 min | Tier 1: Q5 (offline-first), Q6 (Hilt scoping); Tier 2: Q10 (Room migrations) |
| Review | 30 min | learn.html — Topics 5–6, 10 |

**Checklist:**
- [ ] Walk through RemoteMediator + Paging 3 flow
- [ ] Explain auth interceptor token refresh pattern
- [ ] Draw Hilt component hierarchy (Singleton → ViewModel)
- [ ] @Provides vs @Binds — when and why

---

## Day 4 — Performance + Internals + Background

**Goal:** Vitals thresholds, process model, background API choice.

| Block | Time | Activity |
|-------|------|----------|
| Read | 90 min | [07_android_internals.kt](07_android_internals.kt), [08_performance_optimization.kt](08_performance_optimization.kt), [09_background_work.kt](09_background_work.kt) |
| Practice | 60 min | Tier 2: Q12 (WorkManager vs FGS), Q15 (performance diagnosis) |
| Review | 30 min | learn.html — Topics 7–9 |

**Checklist:**
- [ ] List ANR triggers and Play Console thresholds
- [ ] Explain Baseline Profiles and when to use Macrobenchmark
- [ ] Decision tree: WorkManager vs FGS vs AlarmManager vs Coroutines
- [ ] What survives process death vs config change

---

## Day 5 — System Design + Testing + Security

**Goal:** One full system design whiteboard; testing and security depth.

| Block | Time | Activity |
|-------|------|----------|
| Read | 90 min | [15_system_design_android.kt](15_system_design_android.kt), [11_testing.kt](11_testing.kt), [12_security.kt](12_security.kt) |
| Practice | 90 min | Whiteboard **offline-first notes app** OR **chat app** (15); Tier 2: Q14, Q16–Q17 |
| Review | 30 min | learn.html — Topics 11–12, 15 |

**Checklist:**
- [ ] Run 6-step system design framework on one problem
- [ ] Discuss conflict resolution strategies (LWW vs 3-way merge)
- [ ] Fakes vs mocks — when to use each
- [ ] Certificate pinning + Play Integrity layered defense

---

## Day 6 — DSA + Extended Q&A

**Goal:** Pattern recognition and legacy Android depth.

| Block | Time | Activity |
|-------|------|----------|
| Read | 60 min | [20_dsa_solutions.md](20_dsa_solutions.md) — focus on patterns table + study order |
| Code | 90 min | Solve on paper (no IDE): Two Sum, LRU Cache, Number of Islands, Course Schedule, Validate BST |
| Read | 60 min | [17_amit_shekhar_questions_answers.kt](17_amit_shekhar_questions_answers.kt) — sections 6, 7, 16, 18 only (RecyclerView, Java, Gradle) |
| Review | 30 min | learn.html — Topic 20 (DSA patterns) |

**Checklist:**
- [ ] Can write binary search template from memory
- [ ] Explain LRU Cache: HashMap + doubly linked list
- [ ] Topological sort (Kahn's algorithm) on whiteboard
- [ ] RecyclerView ViewHolder + DiffUtil — explain without notes

---

## Day 7 — Googleyness + Full Mock + Review

**Goal:** Behavioral readiness and final pass.

| Block | Time | Activity |
|-------|------|----------|
| Read | 60 min | [18_googleyness_and_leadership.md](18_googleyness_and_leadership.md), skim [19_googleyness_questions.md](19_googleyness_questions.md) categories |
| Practice | 90 min | Speak 6 STAR stories aloud (2–3 min each); mock Tier 3 Q20–Q22 |
| Review | 60 min | [print-guide.html](print-guide.html) — skim weak areas; learn.html — all topics marked "revisit" |
| Prep | 30 min | Prepare "Why Google?" with specific product/team; pack questions for interviewer |

**Checklist:**
- [ ] 6–8 STAR stories written with metrics
- [ ] One failure story and one disagreement story ready
- [ ] Practiced 5 random questions from [19_googleyness_questions.md](19_googleyness_questions.md)
- [ ] Full pass through [16_interview_questions.md](16_interview_questions.md) rapid-fire section

---

## Interview Week (Day Before Each Round)

| Round | Last-minute review |
|-------|-------------------|
| Phone screen | [20_dsa_solutions.md](20_dsa_solutions.md) patterns 1–14 + [01_android_fundamentals.kt](01_android_fundamentals.kt) |
| Domain | [16_interview_questions.md](16_interview_questions.md) Tier 1 + [learn.html](learn.html) |
| Coding | [20_dsa_solutions.md](20_dsa_solutions.md) hard problems 25–27 |
| System design | [15_system_design_android.kt](15_system_design_android.kt) framework + one design |
| Googleyness | [18_googleyness_and_leadership.md](18_googleyness_and_leadership.md) exemplars + story bank |

---

## Optional Extensions (Week 2+)

- [ ] [13_modern_android.kt](13_modern_android.kt) — KMP, Credential Manager, foldables
- [ ] [14_design_patterns_kotlin.kt](14_design_patterns_kotlin.kt) — GoF + Kotlin advanced
- [ ] Add 5 more DSA problems to [20_dsa_solutions.md](20_dsa_solutions.md)
- [ ] Regenerate [print-guide.html](print-guide.html): `python3 scripts/build_print_guide.py`

---

*Good luck. Depth beats breadth — one story told well beats five shallow ones.*
