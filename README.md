# Google Android Developer Interview Prep — SDE-2 (4 YOE)

> A comprehensive, A-to-Z study guide for the **Android domain-specific round** at Google.
> Covers fundamentals through system design, with production-grade code snippets in Kotlin.

---

## Study Guide Index

| # | Topic | File | Priority |
|---|-------|------|----------|
| 01 | **Android Fundamentals** — Lifecycle, Components, Intents, Manifest | [01_android_fundamentals.kt](01_android_fundamentals.kt) | 🔴 Critical |
| 02 | **App Architecture** — MVVM, MVI, Clean Architecture, Navigation, Multi-Source Data Merging | [02_app_architecture.kt](02_app_architecture.kt) | 🔴 Critical |
| 03 | **Jetpack Compose** — Declarative UI, State, Side Effects, Compose Navigation, Material 3, Accessibility, Interop | [03_jetpack_compose.kt](03_jetpack_compose.kt) | 🔴 Critical |
| 04 | **Kotlin Coroutines & Flows** — Structured Concurrency, Dispatchers, Flow | [04_coroutines_and_flows.kt](04_coroutines_and_flows.kt) | 🔴 Critical |
| 05 | **Data Persistence** — Room, DataStore, SQLite, Content Providers | [05_data_persistence.kt](05_data_persistence.kt) | 🟠 High |
| 06 | **Networking** — Retrofit, OkHttp, Caching, Serialization | [06_networking.kt](06_networking.kt) | 🟠 High |
| 07 | **Android Internals** — Process Model, Binder, IPC, App Startup | [07_android_internals.kt](07_android_internals.kt) | 🟠 High |
| 08 | **Performance Optimization** — UI, Memory, Battery, APK Size | [08_performance_optimization.kt](08_performance_optimization.kt) | 🔴 Critical |
| 09 | **Background Work** — WorkManager, Services, Broadcasts, Doze | [09_background_work.kt](09_background_work.kt) | 🟠 High |
| 10 | **Dependency Injection** — Hilt, Dagger 2, Manual DI | [10_dependency_injection.kt](10_dependency_injection.kt) | 🟠 High |
| 11 | **Testing** — Unit, UI, Integration, TDD | [11_testing.kt](11_testing.kt) | 🟠 High |
| 12 | **Security** — Keystore, Permissions, Network Security, Encryption | [12_security.kt](12_security.kt) | 🟡 Medium |
| 13 | **Modern Android** — KMP, Modularization, Adaptive Layouts, Credential Manager, Photo Picker, CameraX, Media3 | [13_modern_android.kt](13_modern_android.kt) | 🟡 Medium |
| 14 | **Design Patterns & Kotlin Advanced** — Delegates, Sealed, DSL, Generics | [14_design_patterns_kotlin.kt](14_design_patterns_kotlin.kt) | 🟠 High |
| 15 | **System Design for Android** — Offline-first, Image Loading, Chat App | [15_system_design_android.kt](15_system_design_android.kt) | 🔴 Critical |
| 16 | **Interview Questions & Mock Q&A** — Frequently asked, tricky questions | [16_interview_questions.md](16_interview_questions.md) | 🔴 Critical |

---

## How to Use This Guide

1. **Start with Critical (🔴) topics** — these appear in almost every Google Android interview
2. **Each file is self-contained** — concepts, code snippets, interview tips, and gotchas
3. **Code is in Kotlin** — Google's preferred language; all snippets are production-grade
4. **Interview Tips (💡)** sections highlight what interviewers specifically look for
5. **Gotchas (⚠️)** sections cover common mistakes candidates make
6. **Follow-up Questions (❓)** sections prepare you for deep-dive follow-ups

## Google SDE-2 Android Interview Format

```
Round 1: Phone Screen — DSA + basic Android
Round 2: Domain-Specific — Deep Android knowledge (THIS GUIDE)
Round 3: Coding Round — Algorithm problem-solving
Round 4: System Design — Android system design
Round 5: Googleyness & Leadership — Behavioral
```

## SDE-2 Expectations (L4 at Google)

- **Depth over breadth**: You should be able to explain *why* things work, not just *how*
- **Production experience**: Discuss trade-offs, not textbook answers
- **Performance awareness**: Always think about scale (Google apps have 500M+ DAU)
- **Modern stack**: Compose, Coroutines, Hilt, Room, WorkManager
- **Testing mindset**: Testable architecture is non-negotiable

---

*Last updated: April 2026*
