/**
 * ============================================================================
 * 04 — KOTLIN COROUTINES & FLOWS
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Coroutine Fundamentals (suspend, Scope, Context, Job)
 *   2. Dispatchers (Main, IO, Default, Unconfined)
 *   3. Structured Concurrency
 *   4. launch vs async — Concurrency Patterns
 *   5. Exception Handling (try-catch, SupervisorJob, CEH)
 *   6. Cancellation & Cooperative Cancellation
 *   7. Flow — Cold Streams
 *   8. StateFlow & SharedFlow — Hot Streams
 *   9. Flow Operators & Transformations
 *  10. Coroutines in Android (viewModelScope, lifecycleScope)
 *  11. Channel — Communication Between Coroutines
 *  12. Mutex & Thread Safety
 *  13. Testing Coroutines
 *  14. callbackFlow & channelFlow
 *  15. shareIn vs stateIn
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. COROUTINE FUNDAMENTALS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * What is a coroutine?
 *   A lightweight thread that can be SUSPENDED without blocking.
 *   Millions of coroutines can run on just a few threads.
 *
 * Key concepts:
 *   suspend function → can be paused and resumed
 *   CoroutineScope   → lifecycle boundary for coroutines
 *   CoroutineContext  → set of elements: Job + Dispatcher + Name + ExceptionHandler
 *   Job              → handle to a coroutine; used for cancellation
 *
 * ❓ Interview Question: What's the difference between a thread and a coroutine?
 *
 *   Thread: OS-level, heavy (~1MB stack), blocks when waiting
 *   Coroutine: Library-level, lightweight (~few bytes), suspends when waiting
 *
 *   A suspended coroutine does NOT occupy a thread. The thread is returned
 *   to the pool and can execute other coroutines.
 */

// Basic coroutine anatomy
suspend fun fetchUserProfile(userId: String): UserProfile {
    // This function can be suspended at any suspension point (delay, withContext, etc.)
    // The calling thread is freed during suspension
    val user = userApi.getUser(userId)           // Suspension point
    val posts = postApi.getUserPosts(userId)      // Suspension point
    return UserProfile(user, posts)
}


// ─────────────────────────────────────────────────────────────────────────────
// 2. DISPATCHERS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dispatchers determine WHICH thread(s) a coroutine runs on.
 *
 * Dispatchers.Main:
 *   - Main/UI thread
 *   - Use for: UI updates, lightweight work
 *   - ⚠️ Never do heavy work here (ANR after 5 seconds)
 *
 * Dispatchers.IO:
 *   - Shared pool of threads (default: 64 threads)
 *   - Use for: Disk I/O, network calls, database operations
 *   - Optimized for blocking I/O operations
 *
 * Dispatchers.Default:
 *   - Shared pool = number of CPU cores
 *   - Use for: CPU-intensive work (JSON parsing, sorting, image processing)
 *   - ⚠️ Don't use for I/O — limited thread count can cause congestion
 *
 * Dispatchers.Unconfined:
 *   - Starts in caller's thread, resumes in whatever thread the suspending function used
 *   - ⚠️ Rarely used in production. Mainly for testing.
 *
 * 💡 Interview Tip: Always explain your Dispatcher choice and WHY.
 */

class UserRepository(
    private val api: UserApi,
    private val dao: UserDao,
    private val dispatchers: DispatcherProvider  // Inject for testability
) {
    suspend fun getUser(id: String): User = withContext(dispatchers.io) {
        // Network + DB operations → IO dispatcher
        val cachedUser = dao.getUser(id)
        if (cachedUser != null && !cachedUser.isStale()) {
            return@withContext cachedUser.toDomain()
        }
        val networkUser = api.fetchUser(id)
        dao.upsert(networkUser.toEntity())
        networkUser.toDomain()
    }

    suspend fun processUserData(users: List<User>): AnalyticsReport {
        return withContext(dispatchers.default) {
            // CPU-intensive → Default dispatcher
            val grouped = users.groupBy { it.region }
            val aggregated = grouped.mapValues { (_, users) ->
                computeMetrics(users)  // Heavy computation
            }
            AnalyticsReport(aggregated)
        }
    }
}

// Inject dispatchers for testability
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val main = Dispatchers.Main
    override val io = Dispatchers.IO
    override val default = Dispatchers.Default
}


// ─────────────────────────────────────────────────────────────────────────────
// 3. STRUCTURED CONCURRENCY
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Structured concurrency = coroutines form a parent-child hierarchy.
 *
 * Rules:
 *   1. A parent coroutine waits for ALL children to complete
 *   2. When a parent is cancelled, ALL children are cancelled
 *   3. When a child fails, the parent (and siblings) are cancelled
 *      (unless using SupervisorJob)
 *   4. Coroutines can only be launched in a CoroutineScope
 *
 * This prevents:
 *   ❌ Leaked coroutines (forgotten background work)
 *   ❌ Lost exceptions (unhandled failures)
 *   ❌ Zombie work (work continues after screen is destroyed)
 *
 * ❓ Interview Question: What happens if you use GlobalScope.launch?
 *
 *   Answer: It breaks structured concurrency. The coroutine:
 *   - Lives for the entire app process
 *   - Is NOT cancelled when the screen/ViewModel is destroyed
 *   - Exceptions are not propagated to any parent
 *   - Can cause memory leaks and wasted resources
 */

// ✅ Structured: coroutines are children of viewModelScope
class StructuredViewModel : ViewModel() {
    init {
        viewModelScope.launch {
            // Automatically cancelled when ViewModel is cleared
            val user = fetchUser()
            val posts = fetchPosts(user.id)
        }
    }
}

// ❌ Unstructured: leaked coroutine
class LeakyViewModel : ViewModel() {
    init {
        GlobalScope.launch {
            // ⚠️ This keeps running even after ViewModel is destroyed
            while (true) {
                pollForUpdates()
                delay(30_000)
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 4. LAUNCH VS ASYNC — CONCURRENCY PATTERNS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * launch → Fire-and-forget. Returns a Job. No result.
 * async  → Returns a Deferred<T>. Call await() to get the result.
 *
 * Key difference: async is for PARALLEL decomposition.
 */

// Sequential execution (slow)
suspend fun loadProfileSequential(userId: String): ProfileData {
    val user = fetchUser(userId)         // 500ms
    val posts = fetchPosts(userId)       // 300ms
    val followers = fetchFollowers(userId) // 400ms
    // Total: ~1200ms
    return ProfileData(user, posts, followers)
}

// Parallel execution with async (fast)
suspend fun loadProfileParallel(userId: String): ProfileData {
    return coroutineScope {
        val userDeferred = async { fetchUser(userId) }           // Starts immediately
        val postsDeferred = async { fetchPosts(userId) }         // Starts immediately
        val followersDeferred = async { fetchFollowers(userId) } // Starts immediately
        // Total: ~500ms (max of the three)
        ProfileData(
            user = userDeferred.await(),
            posts = postsDeferred.await(),
            followers = followersDeferred.await()
        )
    }
}

/**
 * ⚠️ GOTCHA: async without await
 *   If you launch async but never call await(), exceptions are SWALLOWED.
 *   The coroutine fails silently.
 *
 * ⚠️ GOTCHA: coroutineScope vs supervisorScope with async
 *   coroutineScope: if ONE async fails, ALL siblings are cancelled
 *   supervisorScope: siblings continue even if one fails
 */

// supervisorScope — partial failure is acceptable
suspend fun loadProfileWithGracefulDegradation(userId: String): ProfileData {
    return supervisorScope {
        val user = async { fetchUser(userId) }
        val posts = async {
            try { fetchPosts(userId) } catch (e: Exception) { emptyList() }
        }
        val followers = async {
            try { fetchFollowers(userId) } catch (e: Exception) { emptyList() }
        }
        // User is required, but posts/followers degrade gracefully
        ProfileData(user.await(), posts.await(), followers.await())
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 5. EXCEPTION HANDLING
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Exception propagation rules:
 *
 * launch:
 *   - Exception propagates UP to the parent scope
 *   - Parent cancels all children, then fails itself
 *   - CoroutineExceptionHandler catches it (if installed on scope)
 *
 * async:
 *   - Exception is DEFERRED until await() is called
 *   - Still propagates to parent (cancelling siblings)
 *   - try-catch around await() handles it
 *
 * SupervisorJob / supervisorScope:
 *   - Child failure does NOT cancel parent or siblings
 *   - Each child must handle its own exceptions
 */

// Pattern 1: try-catch in the coroutine
class ExceptionHandlingViewModel(
    private val repository: DataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)

    fun loadData() {
        viewModelScope.launch {
            try {
                val result = repository.fetchData()
                _uiState.value = UiState.Success(result)
            } catch (e: HttpException) {
                _uiState.value = UiState.Error("Server error: ${e.code()}")
            } catch (e: IOException) {
                _uiState.value = UiState.Error("Network error")
            }
        }
    }

    // Pattern 2: CoroutineExceptionHandler (last resort, doesn't recover)
    private val handler = CoroutineExceptionHandler { _, throwable ->
        Log.e("VM", "Unhandled", throwable)
        _uiState.value = UiState.Error(throwable.message)
    }

    fun loadDataWithHandler() {
        viewModelScope.launch(handler) {
            repository.fetchData()
        }
    }
}

// Pattern 3: Result wrapper (recommended for repositories)
class SafeRepository(private val api: Api) {
    suspend fun fetchData(): Result<Data> {
        return try {
            val data = api.getData()
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * ❓ Interview Question: What happens with this code?
 *
 *   viewModelScope.launch {
 *       launch { throw RuntimeException("crash") }
 *       launch { delay(1000); println("never printed") }
 *   }
 *
 *   Answer: First child throws → parent cancels second child → "never printed"
 *   is never printed. If there's no CoroutineExceptionHandler, app crashes.
 *
 * ❓ Follow-up: How do you prevent the second child from being cancelled?
 *
 *   Answer: Use supervisorScope:
 *   viewModelScope.launch {
 *       supervisorScope {
 *           launch { throw RuntimeException("crash") }
 *           launch { delay(1000); println("this WILL print") }
 *       }
 *   }
 */


// ─────────────────────────────────────────────────────────────────────────────
// 6. CANCELLATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Cancellation is COOPERATIVE in Kotlin coroutines.
 * A coroutine must CHECK for cancellation to actually stop.
 *
 * Automatic checks happen at:
 *   - Every suspension point (delay, withContext, yield, channel operations)
 *   - isActive check in loops
 *
 * ⚠️ CPU-bound loops without suspension points NEVER cancel!
 */

// ❌ Won't cancel — no suspension points
suspend fun cpuBoundBad() = withContext(Dispatchers.Default) {
    var i = 0
    while (i < 1_000_000_000) {
        i++  // No suspension point — cancellation not checked
    }
}

// ✅ Cooperative cancellation
suspend fun cpuBoundGood() = withContext(Dispatchers.Default) {
    var i = 0
    while (i < 1_000_000_000) {
        i++
        if (i % 1000 == 0) ensureActive()  // Check for cancellation
    }
}

// ✅ Alternative: Use yield()
suspend fun cpuBoundAlternative() = withContext(Dispatchers.Default) {
    var i = 0
    while (i < 1_000_000_000) {
        i++
        if (i % 1000 == 0) yield()  // Checks cancellation + gives other coroutines a turn
    }
}

/**
 * NonCancellable — for cleanup that MUST complete even during cancellation
 *
 * When a coroutine is cancelled, its Job enters "cancelling" state.
 * New suspend calls throw CancellationException immediately.
 * Use NonCancellable to run critical cleanup.
 */
suspend fun fetchAndCache(id: String) {
    try {
        val data = api.fetch(id)
        cache.store(id, data)
    } finally {
        // This runs even on cancellation
        withContext(NonCancellable) {
            // Can call suspend functions here despite cancellation
            cache.markComplete(id)
            analytics.logCompletion(id)
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 7. FLOW — COLD STREAMS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Flow is a cold, asynchronous stream that emits values sequentially.
 *
 * Cold = doesn't produce values until collected. Each collector gets
 *        its own independent execution of the flow.
 *
 * Think of it as: suspend fun that returns multiple values over time.
 */

// Creating flows
fun searchResults(query: String): Flow<List<SearchResult>> = flow {
    // This block runs for EACH collector independently
    val results = api.search(query)
    emit(results)

    // Can emit multiple values over time
    delay(5000)
    val freshResults = api.search(query)
    emit(freshResults)
}

// Flow from Room (automatically emits when DB changes)
@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY created_at DESC")
    fun observeArticles(): Flow<List<ArticleEntity>>
}

// ❓ Interview Question: What's the difference between Flow and suspend function?
//    suspend fun → single value
//    Flow        → multiple values over time (stream)


// ─────────────────────────────────────────────────────────────────────────────
// 8. STATEFLOW & SHAREDFLOW — HOT STREAMS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * StateFlow:
 *   - Hot stream (emits regardless of collectors)
 *   - Always has a current value (initial value required)
 *   - Conflated: only the LATEST value is delivered
 *   - Uses equality check: won't emit if new value == old value
 *   - ✅ Perfect for UI state
 *
 * SharedFlow:
 *   - Hot stream
 *   - No initial value required
 *   - Configurable replay cache and buffer
 *   - Does NOT use equality check — always emits
 *   - ✅ Perfect for events/signals
 *
 * ┌───────────────────┬──────────────────┬──────────────────┐
 * │ Feature           │ StateFlow        │ SharedFlow       │
 * ├───────────────────┼──────────────────┼──────────────────┤
 * │ Initial value     │ Required         │ Optional         │
 * │ Replay            │ Always 1 (latest)│ Configurable     │
 * │ Equality check    │ Yes (conflated)  │ No               │
 * │ Use case          │ UI State         │ Events, signals  │
 * │ Collectors missed │ Only latest value│ Depends on buffer│
 * │ values            │ matters          │ config           │
 * └───────────────────┴──────────────────┴──────────────────┘
 */

class EventDrivenViewModel : ViewModel() {

    // StateFlow for UI state — always has latest value
    private val _uiState = MutableStateFlow(ScreenUiState())
    val uiState: StateFlow<ScreenUiState> = _uiState.asStateFlow()

    // SharedFlow for one-time events — replay = 0 means no re-delivery
    private val _events = MutableSharedFlow<UiEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // ✅ Alternative for events: Channel (guarantees delivery to one collector)
    private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    fun onSubmit() {
        viewModelScope.launch {
            val result = repository.submit()
            if (result.isSuccess) {
                _navigationEvents.send(NavigationEvent.GoToSuccess)
            } else {
                _events.emit(UiEvent.ShowError(result.exceptionOrNull()?.message ?: "Error"))
            }
        }
    }
}

/**
 * ❓ Interview Question: Why use Channel instead of SharedFlow for navigation events?
 *
 *   Answer: Channel guarantees exactly-once delivery to a single collector.
 *   SharedFlow with replay=0 can LOSE events if no collector is active
 *   (e.g., during configuration change). SharedFlow with replay=1 can
 *   RE-DELIVER events after config change.
 *   Channel buffers the event and delivers when a collector is available.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 9. FLOW OPERATORS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Essential Flow operators for interviews:
 */

fun flowOperatorsShowcase() {
    val searchFlow: Flow<String> = TODO()

    // map — transform each value
    searchFlow.map { query -> query.trim().lowercase() }

    // filter — keep values matching predicate
    searchFlow.filter { it.length >= 3 }

    // debounce — wait for pause in emissions (search-as-you-type)
    searchFlow.debounce(300)

    // distinctUntilChanged — skip consecutive duplicates
    searchFlow.distinctUntilChanged()

    // flatMapLatest — cancel previous inner flow when new value arrives
    searchFlow
        .debounce(300)
        .filter { it.isNotBlank() }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            // Previous search is CANCELLED when new query arrives
            repository.search(query)
        }

    // combine — merge multiple flows, emit when ANY flow emits
    val filterFlow: StateFlow<Filter> = TODO()
    val sortFlow: StateFlow<SortOrder> = TODO()
    val itemsFlow: Flow<List<Item>> = TODO()

    combine(itemsFlow, filterFlow, sortFlow) { items, filter, sort ->
        items.filter { filter.matches(it) }
             .sortedWith(sort.comparator)
    }

    // zip — pair values 1:1 from two flows
    val namesFlow = flowOf("Alice", "Bob")
    val agesFlow = flowOf(30, 25)
    namesFlow.zip(agesFlow) { name, age -> "$name is $age" }

    // catch — handle upstream errors
    searchFlow
        .map { riskyOperation(it) }
        .catch { e -> emit("Error: ${e.message}") }

    // flowOn — change dispatcher for UPSTREAM operations
    flow {
        emit(heavyComputation())  // Runs on Default
    }.flowOn(Dispatchers.Default)
     .collect { /* Runs on caller's dispatcher (Main) */ }

    // onEach — perform action for each value without consuming
    searchFlow.onEach { analytics.log("search: $it") }

    // stateIn — convert cold Flow to StateFlow
    val stateFlow: StateFlow<List<Item>> = itemsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}

/**
 * SharingStarted strategies:
 *
 *   Eagerly          → starts immediately, never stops
 *   Lazily           → starts on first subscriber, never stops
 *   WhileSubscribed  → starts on first subscriber, stops when last unsubscribes
 *     - stopTimeoutMillis: delay before stopping (default 0)
 *
 * 💡 WhileSubscribed(5000) is the recommended default:
 *    - Survives quick config changes (5s buffer)
 *    - Stops when screen is truly gone (saves resources)
 */


// ─────────────────────────────────────────────────────────────────────────────
// 10. COROUTINES IN ANDROID
// ─────────────────────────────────────────────────────────────────────────────

/**
 * viewModelScope:
 *   - Uses Dispatchers.Main.immediate
 *   - Cancelled in ViewModel.onCleared()
 *   - ✅ Use for all ViewModel coroutines
 *
 * lifecycleScope:
 *   - Uses Dispatchers.Main.immediate
 *   - Cancelled in onDestroy()
 *   - ✅ Use for UI-related coroutines in Activity/Fragment
 *
 * repeatOnLifecycle:
 *   - Restarts the block when lifecycle reaches given state
 *   - Cancels when lifecycle falls below that state
 *   - ✅ The correct way to collect Flows in Activities/Fragments
 */

// ✅ Correct way to collect Flow in Fragment
class ArticleFragment : Fragment() {
    private val viewModel: ArticleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // This block:
                // - Starts when lifecycle reaches STARTED
                // - Cancels when lifecycle drops below STARTED (onStop)
                // - Restarts when lifecycle reaches STARTED again (onStart)
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }

        // Collecting multiple flows in parallel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { updateUI(it) } }
                launch { viewModel.events.collect { handleEvent(it) } }
            }
        }
    }
}

/**
 * ❓ Why not just use lifecycleScope.launch { flow.collect {} }?
 *
 *    Because it only cancels on DESTROY, not STOP.
 *    Your app keeps processing updates while in the background,
 *    wasting CPU/battery and potentially crashing (updating invisible UI).
 *
 *    repeatOnLifecycle(STARTED) cancels collection in onStop
 *    and restarts in onStart — matching what the user can see.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 11. CHANNEL — INTER-COROUTINE COMMUNICATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Channel = a concurrent queue for sending values between coroutines.
 *
 * Types:
 *   RENDEZVOUS (0)   → sender suspends until receiver is ready (handshake)
 *   BUFFERED (64)    → buffer of 64 elements before suspending
 *   UNLIMITED        → unlimited buffer (can cause OOM)
 *   CONFLATED        → only keeps the latest value (drops old ones)
 *
 * ✅ Use Channel for: one-time events, fan-out, producer-consumer patterns
 * ❌ Don't use for: observable state (use StateFlow instead)
 */

// Producer-Consumer pattern with Channel
fun CoroutineScope.produceImages(urls: List<String>): ReceiveChannel<Bitmap> {
    return produce(Dispatchers.IO, capacity = 5) {
        for (url in urls) {
            val bitmap = downloadImage(url)
            send(bitmap)  // Suspends if buffer is full
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 12. MUTEX & THREAD SAFETY
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Coroutines can access shared mutable state from different threads.
 * Need synchronization to prevent race conditions.
 *
 * Options:
 *   Mutex              → coroutine-friendly lock (non-blocking)
 *   Single-thread      → confine to one dispatcher
 *   Atomic operations  → AtomicInteger, AtomicReference
 *   StateFlow          → thread-safe state updates via update {}
 */

class ThreadSafeCounter {
    private val mutex = Mutex()
    private var count = 0

    suspend fun increment() {
        mutex.withLock {
            count++  // Safe — only one coroutine at a time
        }
    }
}

// ❓ Interview Question: Why Mutex instead of synchronized?
//    synchronized BLOCKS the thread. Mutex SUSPENDS the coroutine.
//    The thread is free to execute other coroutines while waiting.


// ─────────────────────────────────────────────────────────────────────────────
// 13. TESTING COROUTINES
// ─────────────────────────────────────────────────────────────────────────────

class ArticleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ArticleViewModel
    private val fakeRepository = FakeArticleRepository()

    @Before
    fun setup() {
        viewModel = ArticleViewModel(fakeRepository)
    }

    @Test
    fun `loading state emitted before data`() = runTest {
        fakeRepository.setDelay(1000)

        viewModel.loadArticles()

        // advanceUntilIdle() or turbine for Flow testing
        val states = mutableListOf<UiState>()
        val job = launch {
            viewModel.uiState.collect { states.add(it) }
        }

        advanceUntilIdle()
        job.cancel()

        assertEquals(UiState.Loading, states[0])
        assertTrue(states.last() is UiState.Success)
    }
}

// MainDispatcherRule — replaces Main dispatcher in tests
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 14. CALLBACKFLOW & CHANNELFLOW
// ─────────────────────────────────────────────────────────────────────────────

/**
 * callbackFlow:
 *   Bridges callback-based APIs into Flow. Creates a Channel internally
 *   and allows sending values from callback lambdas.
 *
 *   Common use cases:
 *     - Location updates (FusedLocationProviderClient)
 *     - Firebase Realtime Database / Firestore listeners
 *     - Sensor events
 *     - BroadcastReceiver events
 *
 * channelFlow:
 *   Like flow { } but allows concurrent emissions from multiple coroutines.
 *   Use when you need to send from different coroutine contexts.
 *
 * ⚠️ CRITICAL: callbackFlow MUST call awaitClose { } to:
 *    1. Keep the flow alive (otherwise it completes immediately)
 *    2. Clean up the callback registration when the flow is cancelled
 */

// ── callbackFlow: wrapping a location callback ──

fun FusedLocationProviderClient.locationUpdates(
    request: LocationRequest
): Flow<Location> = callbackFlow {
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                trySend(location)  // Non-suspending send — drops if buffer full
            }
        }
    }

    requestLocationUpdates(request, callback, Looper.getMainLooper())
        .addOnFailureListener { e -> close(e) }

    awaitClose {
        removeLocationUpdates(callback)
    }
}

// Usage in ViewModel
class LocationViewModel(
    locationClient: FusedLocationProviderClient
) : ViewModel() {

    val locationState: StateFlow<Location?> = locationClient
        .locationUpdates(
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L).build()
        )
        .catch { emit(Location("default")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

// ── callbackFlow: wrapping a Firebase Firestore listener ──

fun documentFlow(docRef: DocumentReference): Flow<DocumentSnapshot> = callbackFlow {
    val listener = docRef.addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        if (snapshot != null) {
            trySend(snapshot)
        }
    }

    awaitClose { listener.remove() }
}

// ── channelFlow: concurrent emissions ──

fun fetchAllPages(api: PaginatedApi): Flow<List<Item>> = channelFlow {
    val firstPage = api.getPage(1)
    send(firstPage.items)

    // Fan out remaining pages concurrently
    val totalPages = firstPage.totalPages
    (2..totalPages).map { page ->
        launch {
            val result = api.getPage(page)
            send(result.items)  // Safe — channelFlow allows concurrent sends
        }
    }
}

/**
 * ❓ Interview Question: callbackFlow vs channelFlow?
 *
 *   callbackFlow: designed for wrapping external callbacks. Requires awaitClose.
 *   channelFlow:  designed for concurrent coroutine emissions inside the flow.
 *
 *   Both use a Channel internally, but callbackFlow enforces the awaitClose
 *   contract with a lint warning if omitted.
 *
 * ⚠️ GOTCHA: trySend vs send
 *   send()    → suspends if buffer is full (must be called from a coroutine)
 *   trySend() → non-suspending; returns ChannelResult. Use in callbacks.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 15. SHAREIN VS STATEIN
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Both convert a cold Flow into a hot shared Flow.
 *
 * stateIn  → produces StateFlow (always has current value, conflated)
 * shareIn  → produces SharedFlow (configurable replay, no required initial value)
 *
 * ┌───────────────────┬──────────────────────────┬──────────────────────────┐
 * │ Feature           │ stateIn                  │ shareIn                  │
 * ├───────────────────┼──────────────────────────┼──────────────────────────┤
 * │ Return type       │ StateFlow<T>             │ SharedFlow<T>            │
 * │ Initial value     │ Required                 │ Not required             │
 * │ Replay            │ Always 1 (latest)        │ Configurable (0, 1, N)  │
 * │ Equality check    │ Yes (conflated)          │ No                       │
 * │ Best for          │ UI state (single latest) │ Events, multi-collector  │
 * │ Collectors get    │ Latest value immediately │ replay cache + new values│
 * └───────────────────┴──────────────────────────┴──────────────────────────┘
 */

class ArticleListViewModel(
    private val repository: ArticleRepository
) : ViewModel() {

    // ── stateIn: UI state — collectors always get the latest value ──
    val articles: StateFlow<List<Article>> = repository
        .observeArticles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // ── shareIn: share an expensive upstream among multiple collectors ──
    val priceUpdates: SharedFlow<PriceUpdate> = repository
        .observePrices()
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            replay = 1  // New collectors get the last emitted price
        )
}

/**
 * SharingStarted strategies — detailed comparison:
 *
 *   Eagerly:
 *     - Starts collecting the upstream IMMEDIATELY when stateIn/shareIn is called
 *     - Never stops, even if no collectors are active
 *     - ✅ Use for: data that must always be up-to-date (rare)
 *     - ⚠️ Keeps the upstream alive forever → potential resource waste
 *
 *   Lazily:
 *     - Starts on the FIRST collector
 *     - Never stops after starting
 *     - ✅ Use for: expensive init that should only happen when needed
 *     - ⚠️ First collector may miss values emitted before it subscribed
 *       (mitigated by replay in shareIn, or initialValue in stateIn)
 *
 *   WhileSubscribed(stopTimeoutMillis):
 *     - Starts on first collector, stops when last collector unsubscribes
 *       (after stopTimeoutMillis delay)
 *     - ✅ Recommended default: WhileSubscribed(5000)
 *       → survives config changes (within 5s)
 *       → stops upstream when screen is truly gone (saves battery/network)
 *
 * 💡 Interview Tip: Always explain why you chose a specific SharingStarted
 *    strategy. WhileSubscribed(5000) is the safest default for most ViewModel use cases.
 */

// Multi-collector scenario
class DashboardViewModel(repo: DashboardRepository) : ViewModel() {

    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    // Shared upstream: expensive query, collected by multiple UI elements
    val dashboardData: StateFlow<DashboardData> = _refreshTrigger
        .flatMapLatest { repo.fetchDashboard() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardData.Loading)

    fun refresh() {
        _refreshTrigger.tryEmit(Unit)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: What's the difference between Dispatchers.Main and Dispatchers.Main.immediate?
 *    A: Main always posts to the queue. Main.immediate executes immediately
 *       if already on the main thread (avoids one frame delay).
 *       viewModelScope uses Main.immediate by default.
 *
 * ❓ Q2: What happens if you emit from a different thread in a flow { } builder?
 *    A: It throws IllegalStateException. Flow builder is sequential.
 *       Use channelFlow { } for concurrent emissions from multiple coroutines.
 *
 * ❓ Q3: How do you implement search-as-you-type with debouncing?
 *    A: searchQuery
 *         .debounce(300)
 *         .filter { it.length >= 2 }
 *         .distinctUntilChanged()
 *         .flatMapLatest { query -> repository.search(query) }
 *         .catch { emit(emptyList()) }
 *
 * ❓ Q4: What's the difference between flatMapLatest, flatMapConcat, flatMapMerge?
 *    A: flatMapLatest  → cancels previous; use for search (latest query wins)
 *       flatMapConcat  → sequential; use when order matters
 *       flatMapMerge   → concurrent; use when all results needed in parallel
 *
 * ❓ Q5: CancellationException — is it treated as a failure?
 *    A: No. CancellationException is considered NORMAL cancellation.
 *       It propagates up but doesn't trigger CoroutineExceptionHandler.
 *       ⚠️ Never catch CancellationException without rethrowing.
 *
 * ❓ Q6: What's the difference between withTimeout and withTimeoutOrNull?
 *    A: withTimeout throws TimeoutCancellationException (which IS a CancellationException).
 *       withTimeoutOrNull returns null on timeout. Safer for non-critical timeouts.
 *
 * ❓ Q7: How would you convert Firebase Realtime Database listeners into a Flow?
 *    A: Use callbackFlow:
 *       fun observeNode(ref: DatabaseReference): Flow<DataSnapshot> = callbackFlow {
 *           val listener = object : ValueEventListener {
 *               override fun onDataChange(snapshot: DataSnapshot) { trySend(snapshot) }
 *               override fun onCancelled(error: DatabaseError) { close(error.toException()) }
 *           }
 *           ref.addValueEventListener(listener)
 *           awaitClose { ref.removeEventListener(listener) }
 *       }
 *       Key points: use trySend (not send) in callbacks, always call awaitClose
 *       for cleanup, and use close(exception) to propagate errors.
 *
 * ❓ Q8: Why should you NEVER catch CancellationException without rethrowing?
 *    A: CancellationException is the mechanism coroutines use to propagate
 *       cancellation through the hierarchy. If you swallow it:
 *       - The parent doesn't know the child was cancelled
 *       - Structured concurrency breaks (parent waits forever)
 *       - Resources are leaked (coroutine continues running)
 *       ⚠️ CRITICAL pattern:
 *       try { suspendingWork() }
 *       catch (e: Exception) {
 *           if (e is CancellationException) throw e  // Always rethrow!
 *           handleError(e)
 *       }
 *       Or better: use runCatching only on non-suspending code, and catch
 *       specific exception types instead of broad Exception.
 */
