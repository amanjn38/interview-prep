/**
 * ============================================================================
 * 08 — PERFORMANCE OPTIMIZATION
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. UI Performance (Jank, Overdraw, Layout Optimization)
 *   2. RecyclerView Optimization
 *   3. Compose Performance
 *   4. Memory Optimization (Leaks, Heap, Bitmaps)
 *   5. Battery Optimization (Doze, Standby, Restrictions)
 *   6. APK Size Optimization
 *   7. Startup Optimization
 *   8. Profiling Tools (Android Studio Profiler, LeakCanary, Macrobenchmark)
 *   9. Network Optimization
 *  10. Perfetto & Systrace
 *  11. Android Vitals
 *  12. Baseline Profiles
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. UI PERFORMANCE — JANK & OVERDRAW
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Jank = dropped frames. Occurs when frame takes longer than 16.6ms (60fps).
 *
 * Common Causes:
 *   1. Main thread blocking (IO, heavy computation)
 *   2. Deep view hierarchies (nested LinearLayouts)
 *   3. Overdraw (multiple layers drawing on the same pixel)
 *   4. Expensive onDraw() operations
 *   5. Layout thrashing (requestLayout() called too often)
 *   6. Large bitmap decoding on main thread
 *
 * Overdraw Levels:
 *   No overdraw  (1x) → white/no tint
 *   1x overdraw  (2x) → blue
 *   2x overdraw  (3x) → green
 *   3x overdraw  (4x) → red — needs optimization
 *
 * Fixing Overdraw:
 *   - Remove unnecessary backgrounds (window background under Activity background)
 *   - Use clipRect() in custom views
 *   - Flatten view hierarchies
 *   - Use android:windowBackground for splash instead of Activity background
 */

// ❌ Deep hierarchy — slow layout passes
/*
<LinearLayout>
    <LinearLayout>
        <LinearLayout>
            <ImageView />
            <LinearLayout>
                <TextView />
                <TextView />
            </LinearLayout>
        </LinearLayout>
    </LinearLayout>
</LinearLayout>
*/

// ✅ Flat hierarchy with ConstraintLayout
/*
<ConstraintLayout>
    <ImageView />
    <TextView />    <!-- constrained to ImageView -->
    <TextView />    <!-- constrained to first TextView -->
</ConstraintLayout>
*/


// ─────────────────────────────────────────────────────────────────────────────
// 2. RECYCLERVIEW OPTIMIZATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * RecyclerView best practices for smooth scrolling:
 */

// ✅ Use DiffUtil for efficient updates (no notifyDataSetChanged)
class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(old: Article, new: Article): Boolean {
        return old.id == new.id
    }

    override fun areContentsTheSame(old: Article, new: Article): Boolean {
        return old == new  // data class equals
    }

    // Partial updates — only change specific fields
    override fun getChangePayload(old: Article, new: Article): Any? {
        return buildList {
            if (old.isBookmarked != new.isBookmarked) add("bookmark")
            if (old.likeCount != new.likeCount) add("likes")
        }.takeIf { it.isNotEmpty() }
    }
}

class ArticleAdapter : ListAdapter<Article, ArticleViewHolder>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        // ✅ Use ViewBinding — faster than findViewById
        val binding = ItemArticleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // Handle partial updates from DiffUtil payload
    override fun onBindViewHolder(
        holder: ArticleViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        // Only update changed fields — much faster than full rebind
        val changes = payloads.flatMap { it as List<*> }
        val article = getItem(position)
        if ("bookmark" in changes) holder.updateBookmark(article.isBookmarked)
        if ("likes" in changes) holder.updateLikes(article.likeCount)
    }
}

/**
 * More RecyclerView optimization tips:
 *
 *   setHasFixedSize(true)     → if item size doesn't change, skip measure pass
 *   setItemViewCacheSize(n)   → keep n views in cache for fast scroll back
 *   recycledViewPool.setMaxRecycledViews() → tune pool per view type
 *   setHasStableIds(true)     → helps RecyclerView track items across updates
 *   Prefetch: RecyclerView prefetches next items during idle time (default on)
 *
 * ⚠️ Common mistakes:
 *   - Creating new objects in onBindViewHolder (allocations → GC → jank)
 *   - Setting click listeners in onBindViewHolder (use onCreateViewHolder)
 *   - Not using DiffUtil (notifyDataSetChanged redraws everything)
 *   - Nested RecyclerViews without shared RecycledViewPool
 */

// Nested RecyclerView optimization
fun setupNestedRecyclerViews(parentRecyclerView: RecyclerView) {
    val viewPool = RecyclerView.RecycledViewPool()
    // Share pool across all inner RecyclerViews
    // Each inner RecyclerView uses:
    // innerRecyclerView.setRecycledViewPool(viewPool)
}


// ─────────────────────────────────────────────────────────────────────────────
// 3. COMPOSE PERFORMANCE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Compose-specific performance optimizations:
 */

// ✅ Use key() for stable identity in LazyColumn
@Composable
fun OptimizedList(items: ImmutableList<Article>) {
    LazyColumn {
        items(
            items = items,
            key = { it.id }  // Stable identity — enables item reuse and animations
        ) { article ->
            ArticleItem(article)
        }
    }
}

// ✅ Defer state reads to avoid unnecessary recomposition
@Composable
fun ScrollingHeader() {
    val listState = rememberLazyListState()

    // ❌ Bad: reads state in composition phase
    val showButton = listState.firstVisibleItemIndex > 0

    // ✅ Good: reads state in drawing phase via derivedStateOf
    val showButton2 by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    // ✅ Even better for animations: defer to layout/draw phase
    Box(modifier = Modifier.offset {
        IntOffset(0, -listState.firstVisibleItemScrollOffset)
    })
}

// ✅ Use ImmutableList for skippable recomposition
@Composable
fun StableList(items: ImmutableList<String>) {
    // Compose can skip this if items hasn't changed
    items.forEach { Text(it) }
}

// ✅ Avoid unnecessary recomposition with remember for lambdas
@Composable
fun OptimizedCallbacks(viewModel: ArticleViewModel) {
    val items by viewModel.items.collectAsStateWithLifecycle()

    LazyColumn {
        items(items, key = { it.id }) { article ->
            // ❌ Creates new lambda on every recomposition
            // ArticleItem(article, onClick = { viewModel.onClick(article.id) })

            // ✅ Stable reference — Compose can skip recomposition of ArticleItem
            val onClick = remember(article.id) { { viewModel.onClick(article.id) } }
            ArticleItem(article, onClick = onClick)
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 4. MEMORY OPTIMIZATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Common Memory Leaks in Android:
 *
 *   1. Static reference to Activity/Context
 *      Fix: Use Application Context or WeakReference
 *
 *   2. Inner class holding implicit reference to outer Activity
 *      Fix: Use static inner class + WeakReference
 *
 *   3. Handler with Activity reference
 *      Fix: Use lifecycleScope.launch instead
 *
 *   4. Unregistered listeners (LocationManager, SensorManager, BroadcastReceiver)
 *      Fix: Unregister in onPause/onStop/onDestroyView
 *
 *   5. Fragment View binding not cleared in onDestroyView
 *      Fix: Set _binding = null in onDestroyView
 *
 *   6. Coroutine scope outliving the component
 *      Fix: Use viewModelScope, lifecycleScope, viewLifecycleOwner.lifecycleScope
 *
 *   7. ViewModel holding View/Activity reference
 *      Fix: Never pass View or Activity to ViewModel
 */

// ❌ Classic memory leak — anonymous inner class holds Activity reference
class LeakyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ❌ Runnable holds implicit reference to this Activity
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // This keeps Activity alive for 10 seconds even after onDestroy
            updateUI()
        }, 10_000)
    }
}

// ✅ Fixed with lifecycleScope
class SafeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            delay(10_000)
            // Automatically cancelled when Activity is destroyed
            updateUI()
        }
    }
}

/**
 * Bitmap Memory Management:
 *   - Bitmaps are the #1 source of OOM crashes
 *   - A 4000x3000 photo at ARGB_8888 = 48MB of heap!
 *
 * Solutions:
 *   1. Use image loading libraries (Coil, Glide) — they handle caching + sampling
 *   2. inSampleSize — decode at reduced resolution
 *   3. Bitmap.Config.RGB_565 — half memory (no alpha), good for photos
 *   4. BitmapFactory.Options.inBitmap — reuse bitmap memory
 */

fun decodeSampledBitmap(
    resources: Resources,
    resId: Int,
    reqWidth: Int,
    reqHeight: Int
): Bitmap {
    // First pass: decode bounds only (no memory allocation)
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeResource(resources, resId, options)

    // Calculate sample size
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false

    return BitmapFactory.decodeResource(resources, resId, options)
}

fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val (height, width) = options.outHeight to options.outWidth
    var sampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2
        while (halfHeight / sampleSize >= reqHeight && halfWidth / sampleSize >= reqWidth) {
            sampleSize *= 2
        }
    }
    return sampleSize
}


// ─────────────────────────────────────────────────────────────────────────────
// 5. BATTERY OPTIMIZATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Doze Mode (API 23+):
 *   When device is stationary, unplugged, screen off:
 *   - Network access suspended
 *   - Alarms deferred
 *   - GPS/Wi-Fi scanning stopped
 *   - JobScheduler/WorkManager deferred
 *   - Periodic maintenance windows allow batched work
 *
 * App Standby Buckets (API 28+):
 *   Active     → currently in use, no restrictions
 *   Working    → recently used, mild restrictions
 *   Frequent   → used regularly, more restrictions
 *   Rare       → hardly used, heavy restrictions
 *   Restricted → not used + misbehaving, very heavy restrictions
 *
 * Battery optimization checklist:
 *   ✅ Use WorkManager with constraints (not AlarmManager)
 *   ✅ Batch network requests
 *   ✅ Use Firebase Cloud Messaging (FCM) instead of polling
 *   ✅ Minimize wake locks
 *   ✅ Use JobScheduler for deferrable work
 *   ✅ Reduce GPS accuracy when possible
 *   ✅ Use Fused Location Provider
 */


// ─────────────────────────────────────────────────────────────────────────────
// 6. APK SIZE OPTIMIZATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * APK size directly impacts install conversion rates.
 * Every 6MB increase → 1% drop in install rate (Google data).
 *
 * Optimization techniques:
 *
 *   R8 (ProGuard successor):
 *     - Code shrinking — removes unused classes/methods
 *     - Obfuscation — shortens names (a, b, c)
 *     - Optimization — inlines methods, simplifies code
 *     - Resource shrinking — removes unused resources
 *
 *   App Bundle (AAB):
 *     - Dynamic delivery — users download only what they need
 *     - Split APKs — per density, ABI, language
 *     - Dynamic feature modules — on-demand module download
 *     - Typically 15-20% smaller than universal APK
 *
 *   Other techniques:
 *     - Use WebP instead of PNG (25-34% smaller)
 *     - Vector drawables instead of multiple density PNGs
 *     - Remove unused libraries
 *     - Use android.enableR8.fullMode=true in gradle.properties
 *     - Compress native libraries: android.bundle.enableUncompressedNativeLibs=true
 */

// build.gradle.kts — R8 configuration
/*
android {
    buildTypes {
        release {
            isMinifyEnabled = true       // Code shrinking
            isShrinkResources = true     // Resource shrinking
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
*/


// ─────────────────────────────────────────────────────────────────────────────
// 7. STARTUP OPTIMIZATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Goal: Cold start < 500ms (Google benchmark)
 *
 * Techniques:
 *
 *   1. Lazy initialization:
 *      Don't initialize everything in Application.onCreate().
 *      Use by lazy {} or Hilt's @Inject for on-demand creation.
 *
 *   2. App Startup library:
 *      Replaces multiple ContentProviders (which run before Application.onCreate)
 *      with a single one that initializes libraries efficiently.
 *
 *   3. Baseline Profiles:
 *      Pre-compile critical code paths (splash → home screen).
 *      20-30% startup improvement typical.
 *
 *   4. Splash Screen API (API 31+):
 *      System-managed splash screen — shows while app initializes.
 *      Replaces custom splash Activity pattern.
 *
 *   5. Reduce main thread work:
 *      Move initialization to background threads.
 *      Use Dispatchers.Default for CPU-bound init.
 */

// App Startup library
class AnalyticsInitializer : Initializer<Analytics> {
    override fun create(context: Context): Analytics {
        return Analytics.init(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // Declare dependencies — these initialize first
        return listOf(CrashReportingInitializer::class.java)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 8. PROFILING TOOLS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Android Studio Profiler:
 *
 *   CPU Profiler:
 *     - System Trace → see thread activity, frame rendering, system events
 *     - Method Trace → see exact method calls and durations
 *     - Use for: finding jank, slow methods, main thread blocking
 *
 *   Memory Profiler:
 *     - Real-time heap usage graph
 *     - Heap dump → see all live objects
 *     - Allocation tracking → see where objects are created
 *     - Use for: finding memory leaks, excessive allocations
 *
 *   Network Profiler:
 *     - Timeline of network requests
 *     - Request/response inspection
 *     - Use for: finding redundant calls, large payloads
 *
 *   Energy Profiler:
 *     - CPU, Network, GPS, Wake Lock usage
 *     - Use for: finding battery drain causes
 *
 * LeakCanary:
 *   - Automatic memory leak detection in debug builds
 *   - Detects: Activity, Fragment, ViewModel, View, Service leaks
 *   - Shows leak trace (reference chain) for debugging
 *
 * Macrobenchmark & Microbenchmark:
 *   - Macrobenchmark: measures app-level metrics (startup, scrolling)
 *   - Microbenchmark: measures code-level metrics (function timing)
 *   - Both provide deterministic, reproducible results
 */


// ─────────────────────────────────────────────────────────────────────────────
// 9. NETWORK OPTIMIZATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Connection Pooling & HTTP/2 Multiplexing:
 *   - OkHttp maintains a ConnectionPool (default: 5 idle connections, 5 min keepalive)
 *   - HTTP/2 multiplexes multiple requests over a single TCP connection
 *   - Reduces latency from TCP handshake + TLS negotiation
 *   - Configure: ConnectionPool(maxIdleConnections, keepAliveDuration, timeUnit)
 *
 * Request Batching & Deduplication:
 *   - Batch multiple small requests into one (e.g., GraphQL, batch REST endpoints)
 *   - Deduplicate identical in-flight requests (return same Deferred/Flow to callers)
 *   - OkHttp automatically deduplicates concurrent requests to the same URL with
 *     the same Cache-Control behavior
 *
 * Compression:
 *   - OkHttp adds Accept-Encoding: gzip automatically
 *   - Server must support Content-Encoding: gzip
 *   - Reduces payload size by 60-80% for JSON/text responses
 *   - Don't manually gzip if OkHttp handles transparent decompression
 *
 * Prefetching Strategies:
 *   - Prefetch next page of paginated data when user is near the end
 *   - Prefetch likely navigation targets (e.g., article detail when scrolling list)
 *   - Use WorkManager or idle time for background prefetch
 *   ⚠️ Balance: prefetching wastes bandwidth if user doesn't use the data
 *
 * Reducing Payload Size:
 *   - Request only needed fields (sparse fieldsets, GraphQL partial queries)
 *   - Use Protocol Buffers (protobuf) instead of JSON for smaller payloads
 *   - Paginate — don't fetch entire collections
 *   - Use delta sync / If-Modified-Since / ETag for incremental updates
 *
 * Image Optimization:
 *   - WebP: 25-34% smaller than PNG, supports transparency and animation
 *   - Progressive loading: show low-quality placeholder, load full-res async
 *   - Thumbnails: request server-side resized images matching display size
 *   - Coil/Glide handle disk caching, memory caching, and downsampling
 *
 * 💡 Interview Tip: Discuss the full stack — client-side (pooling, caching,
 *    compression) AND API-side (pagination, field selection, image CDN).
 */


// ─────────────────────────────────────────────────────────────────────────────
// 10. PERFETTO & SYSTRACE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Perfetto = modern system-level tracing tool (replaces Systrace).
 * Captures kernel-level + userspace events across ALL processes.
 *
 * What Perfetto captures:
 *   - CPU scheduling (which threads run on which cores)
 *   - Binder transactions
 *   - Rendering pipeline (Choreographer, RenderThread, SurfaceFlinger)
 *   - Disk I/O, memory allocations
 *   - Custom trace sections (android.os.Trace / androidx.tracing)
 *
 * How to capture:
 *   1. Android Studio → Profiler → CPU → System Trace (uses Perfetto)
 *   2. Command line: adb shell perfetto -o /data/misc/perfetto-traces/trace.pb
 *   3. On-device: Developer Options → System Tracing
 *   4. Open traces at ui.perfetto.dev
 *
 * Reading a Perfetto trace:
 *   - Each row = a thread or process
 *   - Colored blocks = trace sections (slices)
 *   - Look for long slices on the main thread (> 16ms = jank source)
 *   - "Choreographer#doFrame" → the frame boundary
 *   - Gaps between slices = thread was sleeping or waiting
 *
 * Identifying jank:
 *   - Find frames where doFrame > 16.6ms
 *   - Check what's on the main thread during that frame:
 *     → Long measure/layout? Deep view hierarchy
 *     → Binder transaction? Slow IPC call
 *     → Lock contention? Deadlock or resource competition
 *     → Disk I/O? Blocking read on main thread
 *
 * Perfetto vs Android Studio Profiler:
 *   - Profiler: easier UI, integrated in IDE, app-scoped
 *   - Perfetto: system-wide, more detail, better for cross-process issues
 *   - Use Profiler for quick checks, Perfetto for deep investigation
 *
 * Adding custom trace sections:
 */

// Custom trace sections for identifying slow code paths
class TracedRepository(private val dao: ArticleDao, private val api: ArticleApiService) {

    suspend fun loadArticles(): List<Article> {
        // Appears as "ArticleRepository.loadArticles" in Perfetto/Systrace
        return Trace.beginAsyncSection("ArticleRepository.loadArticles", 0).let {
            try {
                val cached = traceSection("loadFromDb") { dao.getAll() }
                if (cached.isNotEmpty()) return cached

                val remote = traceSection("fetchFromApi") { api.getArticles(1).body()!! }
                val entities = remote.articles.map { it.toEntity() }
                traceSection("saveToDb") { dao.insertAll(entities) }
                entities.map { it.toDomain() }
            } finally {
                Trace.endAsyncSection("ArticleRepository.loadArticles", 0)
            }
        }
    }
}

// Helper for synchronous trace sections
inline fun <T> traceSection(label: String, block: () -> T): T {
    Trace.beginSection(label)
    return try {
        block()
    } finally {
        Trace.endSection()
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 11. ANDROID VITALS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Android Vitals = performance metrics dashboard in Google Play Console.
 * Google uses these metrics to determine app quality and search ranking.
 *
 * Core Vitals (affect store visibility):
 *
 *   ANR Rate:
 *     - User-perceived ANR rate (ANRs / daily active users)
 *     - Bad threshold: > 0.47%
 *     - Excessive threshold: > 8%
 *     - Main cause: main thread blocking (IO, locks, slow binder calls)
 *
 *   Crash Rate:
 *     - User-perceived crash rate
 *     - Bad threshold: > 1.09%
 *     - Excessive threshold: > 8%
 *     - Includes Java/Kotlin exceptions AND native crashes
 *
 *   Slow Rendering (Frozen Frames):
 *     - Frames that take > 700ms to render
 *     - Bad threshold: > 0.1% of sessions
 *     - Caused by main thread blocking during frame rendering
 *
 *   Excessive Wakeups:
 *     - AlarmManager wakeups that wake the device from Doze
 *     - Bad threshold: > 10 wakeups per hour
 *     - Fix: use WorkManager, avoid exact alarms unless necessary
 *
 *   Stuck Background Wake Locks:
 *     - Wake locks held for > 1 hour while in background
 *     - Fix: always set timeout on wake locks, release in finally blocks
 *
 * Using Vitals to Drive Performance Work:
 *   1. Check Play Console weekly for vitals regressions
 *   2. Correlate regressions with app version releases
 *   3. Use ANR/crash clusters → group by stack trace → prioritize by volume
 *   4. Set up alerting for threshold violations
 *   5. Track vitals improvement as a team OKR
 *
 * ⚠️ Apps with bad vitals get:
 *   - Warning badge in Play Store listing
 *   - Lower search ranking and recommendations
 *   - Potential removal from featured placements
 *
 * 💡 Interview Tip: Mentioning Android Vitals shows you think about production
 *    quality, not just feature development. Know the thresholds and how you'd
 *    set up a process to monitor and improve them.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 12. BASELINE PROFILES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Baseline Profiles = developer-provided list of critical code paths for AOT compilation.
 *
 * How it works:
 *   1. Developer writes a Macrobenchmark test that exercises critical user journeys
 *   2. BaselineProfileRule records which classes/methods are touched
 *   3. Generated profile is bundled in the APK/AAB
 *   4. At install time, ART AOT-compiles these methods (instead of interpreting/JIT)
 *   5. On first launch: profiled code runs as native machine code
 *
 * Benefits:
 *   - 20-30% faster cold startup (typical improvement)
 *   - Smoother first-time scrolling (avoids JIT compilation stutters)
 *   - Consistent performance from first launch
 *   - Cloud Profiles (Play Store) complement developer-provided profiles
 *
 * ⚠️ Without Baseline Profiles:
 *   First launch → ART interprets bytecode → JIT compiles hot methods over time
 *   → First run is noticeably slower than subsequent runs
 *
 * 💡 Interview Tip: Baseline Profiles are a Google-created technology.
 *    Demonstrating hands-on knowledge (Macrobenchmark, BaselineProfileRule,
 *    CI integration) is a strong signal for SDE-2 performance questions.
 */

// Step 1: Add the Macrobenchmark module (build.gradle.kts)
/*
// :benchmark module build.gradle.kts
plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.benchmark"
    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation("androidx.benchmark:benchmark-macro-junit4:1.2.0")
    implementation("androidx.test.ext:junit:1.1.5")
    implementation("androidx.test.uiautomator:uiautomator:2.2.0")
}
*/

// Step 2: Create the Baseline Profile generator
class BaselineProfileGenerator {

    // @get:Rule
    // val rule = BaselineProfileRule()

    // @Test
    fun generateBaselineProfile() {
        // rule.collect(packageName = "com.example.app") {
        //     // Cold start the app
        //     pressHome()
        //     startActivityAndWait()
        //
        //     // Exercise critical user journeys
        //     // Navigate to home screen
        //     device.waitForIdle()
        //
        //     // Scroll the main feed (exercises RecyclerView/LazyColumn rendering)
        //     val feedList = device.findObject(By.res("article_list"))
        //     feedList.setGestureMargin(device.displayWidth / 5)
        //     feedList.fling(Direction.DOWN)
        //     feedList.fling(Direction.DOWN)
        //     device.waitForIdle()
        //
        //     // Open article detail (exercises navigation + detail rendering)
        //     val firstArticle = device.findObject(By.res("article_item"))
        //     firstArticle.click()
        //     device.waitForIdle()
        //
        //     // Navigate back
        //     device.pressBack()
        //     device.waitForIdle()
        // }
    }
}

// Step 3: Startup benchmark to measure improvement
class StartupBenchmark {

    // @get:Rule
    // val rule = MacrobenchmarkRule()

    // @Test
    fun startupWithBaseline() {
        // rule.measureRepeated(
        //     packageName = "com.example.app",
        //     metrics = listOf(StartupTimingMetric()),
        //     compilationMode = CompilationMode.Partial(
        //         baselineProfileMode = BaselineProfileMode.Require
        //     ),
        //     startupMode = StartupMode.COLD,
        //     iterations = 10
        // ) {
        //     pressHome()
        //     startActivityAndWait()
        // }
    }
}

/**
 * CI Integration for Baseline Profiles:
 *
 *   1. Run profile generation on a CI device/emulator:
 *      ./gradlew :benchmark:pixel6Api31BenchmarkAndroidTest
 *        -P android.testInstrumentationRunnerArguments.class=BaselineProfileGenerator
 *
 *   2. Copy generated profile to app module:
 *      src/main/baseline-prof.txt
 *
 *   3. R8/D8 embeds the profile into the APK during release build.
 *
 *   4. Automate: run on every release branch merge, commit updated profile.
 *
 *   5. Verify with Macrobenchmark startup tests:
 *      Compare CompilationMode.None() vs CompilationMode.Partial() metrics.
 *
 * ⚠️ Profiles must be regenerated when critical code paths change.
 *    Stale profiles still help but won't cover new hot paths.
 */


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: Your app drops frames during scrolling. How do you diagnose and fix it?
 *    A: 1. Enable "Profile GPU rendering" bars in Developer Options
 *       2. Run System Trace in CPU Profiler — identify long frames
 *       3. Check for main thread blocking (IO, heavy computation)
 *       4. Check Layout Inspector for deep hierarchies
 *       5. Enable Debug GPU Overdraw to find overdraw
 *       6. Check RecyclerView: ensure DiffUtil, no allocations in onBind
 *       7. For Compose: check recomposition counts, use Composition tracing
 *
 * ❓ Q2: How do you detect and fix memory leaks?
 *    A: Detection: LeakCanary (auto), Memory Profiler (manual heap dump)
 *       Common fixes:
 *       - Null out binding in onDestroyView
 *       - Use viewLifecycleOwner for observers
 *       - Use Application Context in singletons
 *       - Cancel coroutines/callbacks in appropriate lifecycle methods
 *
 * ❓ Q3: Your APK is 80MB. How do you reduce it?
 *    A: 1. Enable R8 (minifyEnabled + shrinkResources)
 *       2. Switch to App Bundle (AAB)
 *       3. Analyze APK with Android Studio's APK Analyzer
 *       4. Convert PNGs to WebP, use vector drawables
 *       5. Remove unused libraries and native ABIs
 *       6. Use dynamic feature modules for large features
 *       7. Enable proguard for all build types
 *
 * ❓ Q4: Explain Baseline Profiles and how they improve performance.
 *    A: Developer-defined list of critical code paths (classes + methods).
 *       ART AOT-compiles these at install time instead of waiting for JIT.
 *       Result: faster startup (no interpretation/JIT compilation overhead).
 *       Create with Macrobenchmark library, generate with BaselineProfileRule.
 *       CI integration: regenerate on release branches, verify with benchmarks.
 *
 * ❓ Q5: How does R8 differ from ProGuard?
 *    A: R8 is Google's replacement for ProGuard. Key differences:
 *       - Faster build times (single-step DEX + shrink)
 *       - Better code optimization (more aggressive inlining)
 *       - Kotlin-aware (understands Kotlin metadata)
 *       - Full mode: more aggressive (breaks some reflection patterns)
 *       - Uses same ProGuard rules format
 *
 * ❓ Q6: How would you use Perfetto to diagnose a jank issue?
 *    A: 1. Capture a system trace via Android Studio Profiler or adb shell perfetto.
 *       2. Open at ui.perfetto.dev.
 *       3. Find the main thread row and locate Choreographer#doFrame slices.
 *       4. Identify frames exceeding 16.6ms.
 *       5. Inspect what's happening in that frame: long measure/layout (deep
 *          hierarchy), binder transactions (slow IPC), lock contention, or
 *          disk I/O. Cross-reference with RenderThread for GPU work.
 *       6. Add custom Trace.beginSection()/endSection() to narrow down the
 *          specific code path causing the delay.
 *
 * ❓ Q7: What are Android Vitals and how do they affect your app?
 *    A: Android Vitals is the Play Console dashboard tracking core performance
 *       metrics: ANR rate (> 0.47% is bad), crash rate (> 1.09% is bad),
 *       frozen frames (> 0.1% sessions), excessive wakeups, stuck wake locks.
 *       Apps exceeding thresholds get warning badges, lower search ranking,
 *       and reduced recommendations. Use vitals to prioritize performance
 *       work: correlate regressions with releases, use ANR/crash clusters
 *       to find root causes, and track improvement over time.
 */
