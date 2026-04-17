# 16 — INTERVIEW QUESTIONS & MOCK Q&A
## Google SDE-2 Android Developer — Domain Round

---

## How This Round Works at Google

- **Duration**: 45-60 minutes
- **Format**: Deep technical discussion with code on whiteboard/shared editor
- **Interviewer**: Senior Android engineer from Google
- **Expectation**: Production-quality thinking, not textbook answers
- **Follow-ups**: Expect 2-3 levels of follow-up questions going deeper

---

## TIER 1: MOST FREQUENTLY ASKED (Expect 2-3 of these)

### Q1: Explain the Activity lifecycle. What happens when Activity A launches Activity B?

**Expected Answer:**

Activity lifecycle callbacks: `onCreate` → `onStart` → `onResume` → `onPause` → `onStop` → `onDestroy`

When A launches B (B is opaque):
```
A.onPause()
B.onCreate()
B.onStart()
B.onResume()
A.onStop()    ← A stops AFTER B is fully visible
```

**Follow-ups:**
- What if B is transparent/dialog? → A only reaches `onPause`, never `onStop`
- Where should you save state? → `onSaveInstanceState` (after `onStop` on API 28+)
- What survives config change? → ViewModel survives, Bundle survives, nothing else
- What about process death? → ViewModel is lost, only Bundle and persistent storage survive

---

### Q2: Compare StateFlow and SharedFlow. When would you use each?

**Expected Answer:**

| Feature | StateFlow | SharedFlow |
|---------|-----------|------------|
| Initial value | Required | Not required |
| Replay | Always 1 (latest) | Configurable (0, 1, N) |
| Equality check | Yes (conflated) | No |
| Best for | UI State | Events/Signals |

```kotlin
// StateFlow — UI State (always has a current value)
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// SharedFlow — One-time events (no replay = no re-delivery)
private val _events = MutableSharedFlow<UiEvent>(replay = 0)
val events: SharedFlow<UiEvent> = _events.asSharedFlow()

// Channel — Guaranteed delivery events (navigation, toast)
private val _navigation = Channel<NavEvent>(Channel.BUFFERED)
val navigation = _navigation.receiveAsFlow()
```

**Follow-up:** Why Channel over SharedFlow for navigation?
→ Channel buffers events and delivers exactly once. SharedFlow with `replay=0` loses events if no collector is active (e.g., during config change).

---

### Q3: How does Jetpack Compose handle recomposition? How do you optimize it?

**Expected Answer:**

Compose tracks which composables READ which `State<T>` objects. When a state changes, only composables that read it are re-executed ("smart recomposition").

Recomposition can be **skipped** if:
- All parameters are stable (primitives, `@Stable`/`@Immutable` classes)
- Parameters haven't changed (by equality)

**Optimization techniques:**
```kotlin
// 1. Use derivedStateOf to avoid unnecessary recomposition
val showButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 5 } }

// 2. Defer state reads to later phases
Modifier.offset { IntOffset(x.toInt(), 0) }  // Layout phase, not Composition

// 3. Use ImmutableList instead of List
fun Items(items: ImmutableList<Item>)  // Skippable

// 4. Use key() for stable identity
LazyColumn { items(articles, key = { it.id }) { ... } }

// 5. Use collectAsStateWithLifecycle (not collectAsState)
val state by viewModel.uiState.collectAsStateWithLifecycle()
```

**Follow-up:** What are the 3 phases of Compose rendering?
→ Composition (what to show), Layout (where to place), Drawing (how to render). Reading state in a later phase avoids triggering earlier phases.

---

### Q4: Explain structured concurrency in Kotlin coroutines.

**Expected Answer:**

Structured concurrency = coroutines form a parent-child hierarchy with these guarantees:
1. Parent waits for all children to complete
2. Cancelling parent cancels all children
3. Child failure cancels parent and siblings (unless `SupervisorJob`)
4. Coroutines can only be launched in a `CoroutineScope`

```kotlin
// ✅ Structured — automatically cancelled when ViewModel is cleared
viewModelScope.launch {
    val user = async { fetchUser() }
    val posts = async { fetchPosts() }
    updateUI(user.await(), posts.await())
}

// ❌ Breaks structured concurrency
GlobalScope.launch { /* Leaked coroutine — never cancelled */ }
```

**Follow-up:** What's the difference between `coroutineScope` and `supervisorScope`?
→ `coroutineScope`: one child failure cancels all siblings. `supervisorScope`: siblings continue even if one fails. Use `supervisorScope` for partial failure tolerance (e.g., loading user profile where posts can fail but user data is essential).

---

### Q5: How would you architect an offline-first app?

**Expected Answer:**

**Core principle:** Room is the Single Source of Truth. UI reads from Room, never directly from network.

```
UI → ViewModel → Repository → Room (SSOT)
                      ↓
               Network (sync)
```

**Pattern:**
1. UI observes Room via `Flow<List<T>>`
2. Repository checks if cache is stale
3. If stale, fetch from API and upsert into Room
4. Room `Flow` automatically emits updated data to UI
5. Writes go to Room immediately (optimistic), sync to server via WorkManager

**Conflict resolution:** Last-Write-Wins for simple data, 3-way merge for text, user resolution for complex conflicts.

```kotlin
override fun observeArticles(): Flow<List<Article>> =
    articleDao.observeAll().map { entities -> entities.map { it.toDomain() } }

override suspend fun refreshArticles() {
    val remote = api.getArticles()
    articleDao.upsertAll(remote.map { it.toEntity() })
    // Flow automatically emits to observers
}
```

---

### Q6: How does Hilt work? Explain scoping.

**Expected Answer:**

Hilt generates Dagger components at compile time for Android classes.

**Component hierarchy (outer → inner):**
```
SingletonComponent (Application)
  └── ActivityRetainedComponent (survives config change)
        └── ViewModelComponent (per ViewModel)
        └── ActivityComponent (per Activity)
              └── FragmentComponent (per Fragment)
```

```kotlin
// Singleton — one instance for app lifetime
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideRetrofit(): Retrofit = ...
}

// ViewModel injection
@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: GetArticlesUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel()

// @Binds for interface → implementation mapping (more efficient than @Provides)
@Binds abstract fun bindRepo(impl: RepoImpl): Repo
```

**Follow-up:** What's the difference between `@Provides` and `@Binds`?
→ `@Provides`: creates instances, can have logic. `@Binds`: maps interface to implementation, must be abstract, generates less code.

---

## TIER 2: FREQUENTLY ASKED (Expect 1-2 of these)

### Q7: Explain ViewModel. How does it survive configuration changes?

ViewModel is stored in `ViewModelStore`, which is retained via `NonConfigurationInstance` mechanism. When Activity is destroyed for config change, the ViewModelStore is transferred to the new Activity instance.

**Key facts:**
- Survives config change ✅
- Does NOT survive process death ❌ (use `SavedStateHandle`)
- Has `viewModelScope` (cancelled in `onCleared()`)
- Should NEVER hold View or Activity references

---

### Q8: What are `LaunchedEffect`, `DisposableEffect`, and `SideEffect`?

```kotlin
// LaunchedEffect — coroutine work, restarts on key change
LaunchedEffect(userId) {
    val user = repository.fetchUser(userId)
}

// DisposableEffect — register/unregister pattern
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { ... }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}

// SideEffect — runs every recomposition (sync non-Compose state)
SideEffect {
    analytics.setUserProperty("screen", screenName)
}
```

---

### Q9: How do you handle one-time events in MVVM?

**Problem:** StateFlow re-delivers the last value to new collectors. Navigation events, toasts, snackbars should fire once.

**Solution:** Use `Channel` + `receiveAsFlow()`:
```kotlin
private val _events = Channel<UiEvent>(Channel.BUFFERED)
val events = _events.receiveAsFlow()

// In ViewModel
_events.send(UiEvent.ShowToast("Success"))

// In Composable
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is UiEvent.ShowToast -> snackbarHostState.showSnackbar(event.message)
            is UiEvent.Navigate -> navController.navigate(event.route)
        }
    }
}
```

---

### Q10: Room migrations — how do you handle them?

```kotlin
// Auto migration (simple changes — add column, add table)
@Database(
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = RenameSpec::class)
    ]
)

// Manual migration (complex changes)
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE new_table (...)")
        db.execSQL("INSERT INTO new_table SELECT ... FROM old_table")
        db.execSQL("DROP TABLE old_table")
        db.execSQL("ALTER TABLE new_table RENAME TO old_table")
    }
}

// Always test migrations!
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(...)
}
```

---

### Q11: How do you prevent memory leaks in Android?

**Common leaks and fixes:**

| Leak Source | Fix |
|-------------|-----|
| Static Activity reference | Use Application Context or WeakReference |
| Handler with Activity ref | Use `lifecycleScope.launch` |
| Fragment View binding | Null out `_binding` in `onDestroyView` |
| Unregistered listeners | Unregister in matching lifecycle callback |
| ViewModel holding View ref | Never pass View/Activity to ViewModel |
| Anonymous inner classes | Use static class + WeakReference |
| Coroutine scope outliving component | Use viewModelScope / lifecycleScope |

**Detection:** LeakCanary (auto in debug), Memory Profiler (manual heap dumps).

---

### Q12: WorkManager vs other background APIs

| API | Use Case | Survives reboot? |
|-----|----------|------------------|
| WorkManager | Deferrable, guaranteed work | ✅ |
| Coroutines | Immediate, in-app work | ❌ |
| Foreground Service | Long-running visible task | Depends |
| AlarmManager | Exact-time triggers | ✅ |

**WorkManager is the default answer** for almost all background work:
- Constraints (network, battery, charging)
- Chaining (sequential and parallel)
- Retry with backoff
- Unique work (prevent duplicates)
- Observable progress

---

### Q13: Notifications & Channels

**Expected Answer:**

NotificationChannel (API 26+): Group notifications by type (messages, promotions, alerts). Each channel has user-controllable importance level:
- `IMPORTANCE_HIGH` → heads-up notification
- `IMPORTANCE_DEFAULT` → sound + status bar
- `IMPORTANCE_LOW` → no sound, status bar only
- `IMPORTANCE_MIN` → no sound, no status bar

```kotlin
// Create channel (safe to call repeatedly — no-op if exists)
val channel = NotificationChannel("messages", "Messages", NotificationManager.IMPORTANCE_HIGH)
notificationManager.createNotificationChannel(channel)

// Build notification — PendingIntent must be FLAG_IMMUTABLE (API 31+)
val pendingIntent = PendingIntent.getActivity(
    context, 0, intent,
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
)

val notification = NotificationCompat.Builder(context, "messages")
    .setContentTitle("New message")
    .setContentText("Hey, are you free?")
    .setSmallIcon(R.drawable.ic_message)
    .setContentIntent(pendingIntent)
    .setStyle(NotificationCompat.BigTextStyle().bigText(longMessage))
    .build()
```

**Key changes:**
- API 31+: PendingIntent must specify `FLAG_MUTABLE` or `FLAG_IMMUTABLE`
- API 33+: `POST_NOTIFICATIONS` runtime permission required
- Notification styles: BigTextStyle, InboxStyle, MessagingStyle (for chat)

---

### Q14: Testing Strategy

**Expected Answer:**

| Layer | Test Type | Tools |
|-------|-----------|-------|
| ViewModel / UseCase | Unit test | JUnit, Turbine, MockK |
| Repository | Unit test (fakes) | JUnit, fake data sources |
| Room DAO | Integration test | AndroidJUnit4, in-memory DB |
| Compose UI | UI test | ComposeTestRule |
| End-to-end | E2E | Espresso, UI Automator |

**Fakes vs Mocks:**
- Prefer **fakes** (manual implementations of interfaces) — easier to debug, no mocking framework
- Use **mocks** (MockK/Mockito) only when fakes are impractical (e.g., Android framework classes)

```kotlin
// Testing coroutines/flows with Turbine
@Test
fun `articles load successfully`() = runTest {
    val fakeRepo = FakeArticleRepository(articles = testArticles)
    val viewModel = ArticleViewModel(fakeRepo)

    viewModel.uiState.test {
        assertEquals(UiState.Loading, awaitItem())
        assertEquals(UiState.Success(testArticles), awaitItem())
        cancelAndConsumeRemainingEvents()
    }
}
```

**Follow-up:** How do you decide what to unit test?
→ High-value: business logic, state machines, data transformations. Low-value: simple getters, Android framework wrappers.

---

### Q15: Performance Diagnosis

**Expected Answer:**

**Diagnosing slow app startup:**
1. Measure: `adb shell am start -W` (TotalTime), reportFullyDrawn(), Macrobenchmark
2. Profile: Android Studio Profiler or Perfetto trace during cold start
3. Common culprits: heavy Application.onCreate(), synchronous disk I/O on main thread, large view hierarchies, eager DI initialization

**Tools:**
- **Perfetto**: System-level tracing — CPU, memory, disk I/O, frame rendering
- **Macrobenchmark**: Automated startup/scroll benchmarks in CI
- **Baseline Profiles**: AOT-compile hot paths for 20-30% faster startup
- **Android Vitals** (Play Console): Production crash rate, ANR rate, startup time, stuck frames

```kotlin
// Macrobenchmark for startup
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun coldStartup() = rule.measureRepeated(
        packageName = "com.example.app",
        metrics = listOf(StartupTimingMetric()),
        startupMode = StartupMode.COLD,
        iterations = 10
    ) {
        pressHome()
        startActivityAndWait()
    }
}
```

---

## TIER 3: DEEP DIVE QUESTIONS (For strong candidates)

### Q16: Explain the Compose Snapshot system.

Compose uses a snapshot system (similar to software transactional memory) to track state reads and writes. When `mutableStateOf()` is read during composition, the snapshot records it. When the state value changes, all compositions that read it are invalidated and scheduled for recomposition.

---

### Q17: How does the Binder IPC mechanism work?

Client → Proxy → Parcel → Binder Driver (kernel) → Server process → unparcel → execute → return path. Everything in Android uses Binder: `startActivity()`, `bindService()`, `ContentResolver`. Transaction limit ~1MB per process.

---

### Q18: Design a pagination system for a chat app.

- Messages loaded from Room with `PagingSource`
- Oldest-first loading (scroll up to load older messages)
- `RemoteMediator` fetches older messages from API, stores in Room
- New incoming messages via WebSocket → insert to Room → Flow emits → UI updates
- Handle: message ordering, gap detection, duplicate prevention

---

## BEHAVIORAL TIPS FOR GOOGLE

1. **Think out loud** — explain your reasoning as you go
2. **Discuss trade-offs** — never present one solution as perfect
3. **Mention scale** — Google apps serve billions of users
4. **Ask clarifying questions** — don't assume requirements
5. **Write clean code** — even on whiteboard, proper naming and structure
6. **Mention testing** — how would you test this?
7. **Be honest** — if you don't know something, say "I haven't used this directly, but here's my understanding..."

---

## RAPID FIRE — Quick Answers to Know

- **`repeatOnLifecycle` vs `launchWhenStarted`?** → `repeatOnLifecycle` cancels and restarts; `launchWhenStarted` just suspends (deprecated, can waste resources)
- **`remember` vs `rememberSaveable`?** → `remember` survives recomposition; `rememberSaveable` also survives config change + process death
- **`by viewModels()` vs `by activityViewModels()`?** → Fragment-scoped vs Activity-scoped ViewModel
- **`data class` vs `class` for UI state?** → `data class` for immutable state (copy, equals, hashCode for free)
- **Why `Flow` over `LiveData`?** → Kotlin-native, no Android dependency, richer operators, works in domain/data layers
- **`flatMapLatest` vs `flatMapConcat`?** → Latest cancels previous (search); Concat is sequential (ordered processing)
- **App Bundle vs APK?** → Bundle lets Play generate optimized APKs per device (15-20% smaller)
- **`@Stable` vs `@Immutable`?** → `@Immutable`: all properties never change. `@Stable`: changes are observable via Compose State
- **Process death test?** → ADB: `adb shell am kill <package>`, or Android Studio terminate button
- **What is `callbackFlow`?** → Wraps callback-based APIs as a Flow; needs `awaitClose` to unregister the callback when the flow collector cancels
- **Compose Navigation type-safe routes?** → Use Kotlin `@Serializable` classes as route types instead of string routes (Navigation 2.8+)
- **`onTrimMemory`?** → System callback signaling memory pressure; clear caches at appropriate levels (`TRIM_MEMORY_RUNNING_LOW`, `TRIM_MEMORY_UI_HIDDEN`)
- **ProGuard vs R8?** → R8 replaced ProGuard as default; faster, Kotlin-aware, single-step DEX+shrink, same rule format

---

*Good luck with your Google interview! 🎯*
