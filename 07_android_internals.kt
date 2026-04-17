/**
 * ============================================================================
 * 07 — ANDROID INTERNALS
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Android Process Model
 *   2. Low Memory Killer (LMK)
 *   3. Binder IPC Framework
 *   4. Handler / Looper / MessageQueue
 *   5. App Startup Flow (Cold / Warm / Hot)
 *   6. Activity Launch Sequence (Zygote → Activity)
 *   7. APK Structure & Build Process
 *   8. ART Runtime & Compilation (AOT, JIT, Profiles)
 *   9. View Rendering Pipeline
 *  10. ANRs (Application Not Responding)
 *  11. Memory Management Depth (Heap, GC, onTrimMemory)
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. ANDROID PROCESS MODEL
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Each Android app runs in its OWN Linux process with its OWN Dalvik/ART VM.
 *
 * Process isolation provides:
 *   - Security: one app can't access another's memory
 *   - Stability: one app crashing doesn't bring down others
 *   - Resource management: OS can kill individual processes
 *
 * Components in the same app share the same process by default.
 * You can assign components to separate processes via android:process in manifest.
 *
 * ❓ Interview Question: When would you use multiple processes?
 *    A: WebView (isolate crashes), heavy background services (separate heap),
 *       push notification handling (lightweight process for messages).
 *    ⚠️ Trade-off: Each process gets its own heap, singletons, and static state.
 *       Data sharing requires IPC (Binder, ContentProvider, files).
 */


// ─────────────────────────────────────────────────────────────────────────────
// 2. LOW MEMORY KILLER (LMK)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Android kills processes based on priority to free memory.
 *
 * Process priority (high → low, killed last → first):
 *
 *   1. Foreground Process
 *      - Activity in onResume (user interacting)
 *      - Service bound to foreground Activity
 *      - BroadcastReceiver executing onReceive()
 *
 *   2. Visible Process
 *      - Activity in onPause (partially visible, e.g., behind dialog)
 *      - Service bound to a visible Activity
 *
 *   3. Service Process
 *      - Running a started Service (not bound, not foreground)
 *      - After ~30 minutes, demoted to cached
 *
 *   4. Cached (Background) Process
 *      - Activity in onStop (not visible)
 *      - Kept for fast switching, but expendable
 *      - LRU list — oldest killed first
 *
 *   5. Empty Process
 *      - No active components
 *      - Kept only for caching purposes
 *      - Killed first when memory needed
 *
 * 💡 Interview Tip: Explain how your architecture handles process death.
 *    SavedStateHandle, Room as SSOT, WorkManager for persistent work.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 3. BINDER IPC FRAMEWORK
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Binder = Android's inter-process communication (IPC) mechanism.
 *
 * How it works:
 *   1. Client calls a method on a proxy object
 *   2. Proxy serializes the call into a Parcel (marshalling)
 *   3. Parcel is sent to the Binder kernel driver
 *   4. Kernel driver forwards it to the server process
 *   5. Server deserializes (unmarshalling) and executes
 *   6. Response follows the same path back
 *
 * Everything in Android uses Binder under the hood:
 *   - startActivity()   → talks to ActivityManagerService via Binder
 *   - bindService()     → returns an IBinder to communicate
 *   - ContentResolver   → talks to ContentProvider via Binder
 *   - Intents           → serialized via Binder (hence the ~1MB limit)
 *
 * AIDL (Android Interface Definition Language):
 *   - Generates Binder boilerplate code
 *   - Used for cross-process service communication
 *   - Methods can be: oneway (async), in/out/inout params
 *
 * ⚠️ Binder Transaction Limit: ~1MB per process for ALL concurrent transactions.
 *    TransactionTooLargeException if exceeded (large Bundles, Bitmaps in Intents).
 */


// ─────────────────────────────────────────────────────────────────────────────
// 4. HANDLER / LOOPER / MESSAGEQUEUE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The foundation of Android's threading model:
 *
 *   Looper       → infinite loop that processes messages from a queue
 *   MessageQueue → FIFO queue of Messages and Runnables
 *   Handler      → sends Messages/Runnables to a specific Looper's queue
 *
 * Main Thread = a thread with a Looper (created by ActivityThread.main()).
 *
 * Flow:
 *   Handler.post(runnable) → enqueues Runnable in MessageQueue
 *   Looper.loop() → dequeues and dispatches to Handler.handleMessage()
 *
 * ❓ Interview Question: How does runOnUiThread work?
 *    Answer: It checks if current thread is main thread.
 *    If yes, runs immediately. If no, posts to main Looper's Handler.
 *
 * ❓ Follow-up: How does Dispatchers.Main work under the hood?
 *    Answer: It uses a Handler attached to the main Looper to post
 *    coroutine resumption to the main thread's MessageQueue.
 */

// How Handler/Looper work conceptually
class HandlerLooperExample {

    // Creating a background thread with a Looper
    fun createBackgroundLooper() {
        val handlerThread = HandlerThread("BackgroundThread")
        handlerThread.start()

        val backgroundHandler = Handler(handlerThread.looper)
        val mainHandler = Handler(Looper.getMainLooper())

        backgroundHandler.post {
            // This runs on the background thread
            val result = heavyComputation()

            mainHandler.post {
                // This runs on the main thread
                updateUI(result)
            }
        }
    }

    // This is essentially what Dispatchers.Main does
    fun postToMainThread(block: () -> Unit) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(block)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 5. APP STARTUP FLOW
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Cold Start (slowest):
 *   1. User taps app icon
 *   2. Linux kernel creates a new process (forked from Zygote)
 *   3. ART creates the Application object
 *   4. Application.onCreate() runs
 *   5. ActivityThread creates the main Activity
 *   6. Activity.onCreate() → onStart() → onResume()
 *   7. First frame drawn → app is interactive
 *
 * Warm Start:
 *   - Process exists but Activity was destroyed
 *   - Skips steps 2-4, starts from step 5
 *   - Faster because Application is already initialized
 *
 * Hot Start:
 *   - Process AND Activity exist (in onStop)
 *   - Just brings Activity to foreground
 *   - onRestart() → onStart() → onResume()
 *
 * 💡 Google Metric: Cold start should be < 500ms for good UX.
 *    Time To Initial Display (TTID) and Time To Full Display (TTFD).
 *
 * Optimizing Cold Start:
 *   - Lazy-initialize libraries in Application.onCreate()
 *   - Use App Startup library (Jetpack) for ContentProvider initialization
 *   - Defer non-critical initialization to background
 *   - Use Baseline Profiles for AOT compilation of hot paths
 *   - Minimize work in Activity.onCreate()
 */

// Optimized Application class
class OptimizedApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Only critical initialization here
        initializeCrashReporting()  // Must be first

        // Defer everything else
        val backgroundScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        backgroundScope.launch {
            initializeAnalytics()
            initializeRemoteConfig()
            preloadFonts()
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 6. ZYGOTE & ACTIVITY LAUNCH SEQUENCE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Zygote = template process that every Android app is forked from.
 *
 * Why Zygote?
 *   - Pre-loads common framework classes and resources
 *   - fork() is fast (copy-on-write memory)
 *   - New app process gets pre-warmed ART runtime
 *
 * Full Activity Launch Sequence:
 *
 *   Launcher → startActivity(intent)
 *       → ActivityManagerService (system process, Binder IPC)
 *           → resolves Intent, checks permissions
 *           → if process doesn't exist: tells Zygote to fork
 *       → Zygote forks a new process
 *           → New process: ActivityThread.main() starts
 *           → Creates Looper, Handler for main thread
 *           → Binds to ActivityManagerService
 *       → AMS tells new process to create Application
 *           → Application.onCreate()
 *       → AMS tells process to launch Activity
 *           → Activity.onCreate() → onStart() → onResume()
 *       → WindowManager adds Activity's window
 *       → First frame rendered (TTID)
 */


// ─────────────────────────────────────────────────────────────────────────────
// 7. APK STRUCTURE & BUILD PROCESS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * APK Contents:
 *   AndroidManifest.xml    → compiled binary XML
 *   classes.dex            → compiled bytecode (Dalvik Executable)
 *   resources.arsc         → compiled resources table
 *   res/                   → resources not compiled into resources.arsc
 *   lib/                   → native libraries (arm64-v8a, armeabi-v7a, x86_64)
 *   assets/                → raw files (accessed via AssetManager)
 *   META-INF/              → APK signature
 *
 * Build Process:
 *   .kt/.java → kotlinc/javac → .class → D8 → .dex
 *   R8 (release) → optimization, shrinking, obfuscation
 *   Resources → AAPT2 → compiled resources + R.java
 *   All combined → APK → zipalign → APK signing
 *
 * App Bundle (AAB):
 *   - Google Play generates optimized APKs per device
 *   - Split APKs: base + config (density, ABI, language)
 *   - Smaller downloads: user only gets what they need
 *   - Dynamic Feature Modules: download features on demand
 */


// ─────────────────────────────────────────────────────────────────────────────
// 8. ART RUNTIME
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ART (Android Runtime) replaced Dalvik in Android 5.0.
 *
 * Compilation strategies:
 *
 *   AOT (Ahead-Of-Time):
 *     - Compiles dex to native code at install time
 *     - Faster startup, higher storage cost
 *     - Used for frequently-used code paths
 *
 *   JIT (Just-In-Time):
 *     - Compiles hot methods at runtime
 *     - Adapts to actual usage patterns
 *     - Profiles stored for future AOT compilation
 *
 *   Profile-Guided Compilation:
 *     - JIT collects profiles of hot methods
 *     - On device idle/charging, AOT-compiles those methods
 *     - Baseline Profiles (developer-provided) + Cloud Profiles (from Play)
 *
 * Garbage Collection:
 *   - Concurrent, generational GC
 *   - Young generation: small, collected frequently (minor GC)
 *   - Old generation: large, collected rarely (major GC)
 *   - ⚠️ GC pauses can cause jank (skipped frames)
 *   - Avoid excessive allocations in onDraw() / composable recomposition
 */


// ─────────────────────────────────────────────────────────────────────────────
// 9. VIEW RENDERING PIPELINE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * How a frame is rendered (View system):
 *
 *   VSYNC signal (every 16.6ms for 60fps)
 *       → Choreographer dispatches frame callback
 *           → Input events processed
 *           → Animations updated
 *           → Measure pass (top-down, each view measures children)
 *           → Layout pass (top-down, each view positions children)
 *           → Draw pass (top-down, each view draws itself)
 *       → RenderThread → GPU renders display list
 *       → SurfaceFlinger composites all surfaces
 *       → Display shows frame
 *
 * Frame budget: 16.6ms (60fps) or 8.3ms (120fps)
 * If frame takes longer → dropped frame → jank
 *
 * Common causes of jank:
 *   - Deep view hierarchies (too many measure/layout passes)
 *   - Overdraw (drawing the same pixel multiple times)
 *   - Main thread blocking (IO, heavy computation)
 *   - GC pauses from excessive allocation
 *   - Large bitmap decoding on main thread
 *
 * Compose rendering is different:
 *   Composition → Layout → Drawing (3 phases)
 *   Uses a slot table instead of a view tree
 *   Can skip phases if state hasn't changed
 */


// ─────────────────────────────────────────────────────────────────────────────
// 10. ANRs (APPLICATION NOT RESPONDING)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ANR = system dialog shown when the app is unresponsive to user input.
 *
 * ANR Triggers:
 *   - Input dispatch timeout: 5 seconds (touch/key event not handled)
 *   - BroadcastReceiver timeout: 10 seconds (onReceive not finished)
 *   - Service timeout: 20 seconds (onCreate/onStartCommand not finished)
 *   - ContentProvider timeout: published within 10 seconds of creation
 *
 * Root Cause: almost always main thread blocking.
 *
 * Common Causes:
 *   1. Synchronous disk I/O on main thread (SharedPreferences.commit(), DB queries)
 *   2. Synchronous network call on main thread
 *   3. Long-running computation on main thread (JSON parsing, bitmap decode)
 *   4. Lock contention — main thread waiting on a lock held by another thread
 *   5. Deadlock between main thread and a worker thread
 *   6. Slow BroadcastReceiver (doing IO in onReceive)
 *   7. Binder call to a slow or hung remote service
 *
 * Diagnosis Tools:
 *   - /data/anr/traces.txt → full thread dump at time of ANR
 *   - StrictMode → detects disk/network IO on main thread during development
 *   - Systrace / Perfetto → system-level trace showing thread activity
 *   - Android Studio Profiler → CPU trace reveals blocking calls
 *   - Google Play Console → ANR clusters with stack traces
 *
 * ⚠️ Google Play ANR Thresholds:
 *   - User-perceived ANR rate > 0.47% → "bad behavior" warning
 *   - User-perceived ANR rate > 8% → app may be deprioritized in Play Store
 *   - These are measured per active daily users, not sessions
 *
 * 💡 Interview Tip: When asked "how do you prevent ANRs?", don't just say
 *    "move work off main thread." Discuss StrictMode for detection, coroutines
 *    with Dispatchers.IO for I/O, and monitoring with Play Console vitals.
 */

// StrictMode setup for detecting main thread violations in debug builds
class DebugApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .detectCustomSlowCalls()
                    .penaltyLog()        // Log to Logcat
                    .penaltyFlashScreen() // Red border flash on violation
                    .build()
            )

            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .detectLeakedSqlLiteObjects()
                    .detectActivityLeaks()
                    .penaltyLog()
                    .build()
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 11. MEMORY MANAGEMENT DEPTH
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ART Heap Structure:
 *
 *   Young Generation (Nursery):
 *     - Where newly allocated objects live
 *     - Small, collected frequently (minor/young GC)
 *     - Most objects die young (generational hypothesis)
 *     - Uses a semi-space or copying collector
 *
 *   Old Generation (Tenured):
 *     - Objects that survived multiple young GCs are promoted here
 *     - Larger, collected less frequently (major GC)
 *     - Uses a concurrent mark-sweep or compacting collector
 *
 *   Large Object Space (LOS):
 *     - Objects > 12KB (typically large arrays, bitmaps)
 *     - Allocated separately to avoid fragmentation
 *     - Collected as part of major GC
 *
 * Native Heap vs Dalvik/ART Heap:
 *   - ART heap: managed by GC, holds Java/Kotlin objects
 *   - Native heap: unmanaged (malloc/free), holds native allocations
 *   - Bitmaps (API 26+): allocated on native heap, not ART heap
 *   - JNI allocations, NDK code, graphics buffers → native heap
 *   - Native leaks don't show in Memory Profiler heap dump — use malloc debug
 *
 * android:largeHeap="true":
 *   - Requests a larger Dalvik/ART heap (typically 256-512MB vs 128-256MB)
 *   ⚠️ Why it's usually wrong:
 *     - Does NOT fix memory leaks — just delays OOM
 *     - Longer GC pauses (more heap to scan)
 *     - System may kill your app sooner (uses more memory overall)
 *     - Legitimate use cases: photo/video editors, games with large assets
 *
 * 💡 Interview Tip: If asked about memory optimization, discuss the heap
 *    structure, then onTrimMemory for responding to pressure, and finally
 *    profiling tools (Memory Profiler, LeakCanary). Mentioning largeHeap
 *    as an anti-pattern shows depth.
 */

/**
 * onTrimMemory / ComponentCallbacks2:
 *
 * Called by the system when memory is low. Your app should respond
 * by releasing non-critical resources to avoid being killed.
 *
 * Levels (increasing severity):
 *
 *   TRIM_MEMORY_RUNNING_MODERATE (5)
 *     → App is running, device is getting low on memory
 *     → Consider releasing non-essential caches
 *
 *   TRIM_MEMORY_RUNNING_LOW (10)
 *     → App is running, device is quite low on memory
 *     → Release caches and reduce memory footprint
 *
 *   TRIM_MEMORY_RUNNING_CRITICAL (15)
 *     → App is running, device is critically low
 *     → Release everything non-essential immediately
 *
 *   TRIM_MEMORY_UI_HIDDEN (20)
 *     → App's UI is no longer visible
 *     → Release UI-related resources (image caches, view caches)
 *
 *   TRIM_MEMORY_BACKGROUND (40)
 *     → App is in the LRU background list
 *     → Release as much as possible to survive longer
 *
 *   TRIM_MEMORY_MODERATE (60)
 *     → App is in the middle of the LRU list
 *     → Getting close to being killed
 *
 *   TRIM_MEMORY_COMPLETE (80)
 *     → App is near the end of the LRU list
 *     → Will be killed next if memory isn't freed
 */

class MemoryAwareApplication : Application() {  // Application already implements ComponentCallbacks2

    private val imageCache = LruCache<String, Bitmap>(calculateMemoryCacheSize())

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        when {
            level >= ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // About to be killed — release everything
                imageCache.evictAll()
                clearAllCaches()
            }
            level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                // In background LRU — release most caches
                imageCache.trimToSize(imageCache.maxSize() / 4)
                clearNonEssentialCaches()
            }
            level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // UI hidden — release UI caches
                imageCache.trimToSize(imageCache.maxSize() / 2)
            }
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                // Running but critical — reduce aggressively
                imageCache.trimToSize(imageCache.maxSize() / 8)
            }
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                // Running but low — reduce caches
                imageCache.trimToSize(imageCache.maxSize() / 4)
            }
        }
    }

    private fun calculateMemoryCacheSize(): Int {
        val maxMemory = Runtime.getRuntime().maxMemory() / 1024  // KB
        return (maxMemory / 8).toInt()  // Use 1/8th of available memory
    }

    private fun clearAllCaches() { /* release all in-memory caches */ }
    private fun clearNonEssentialCaches() { /* release non-UI caches */ }
}


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: What is Zygote and why does Android use it?
 *    A: Zygote is a special process started at boot that pre-loads framework
 *       classes and resources. All app processes are forked from Zygote via
 *       fork(), which is fast because of copy-on-write memory semantics.
 *       This means each new app starts with framework already in memory.
 *
 * ❓ Q2: Explain the Binder transaction size limit and its implications.
 *    A: ~1MB per process for ALL concurrent transactions. This affects:
 *       - Intent extras (don't put large data in Intents)
 *       - onSaveInstanceState Bundles (keep small)
 *       - ContentProvider queries (use ContentResolver with cursors, not full data)
 *       Exceeding causes TransactionTooLargeException.
 *
 * ❓ Q3: Why can't you touch the UI from a background thread?
 *    A: Android's View system is NOT thread-safe. ViewRootImpl.checkThread()
 *       verifies all UI operations happen on the thread that created the view
 *       hierarchy (main thread). This is enforced to prevent race conditions
 *       during measure/layout/draw passes.
 *
 * ❓ Q4: How do Baseline Profiles improve app performance?
 *    A: They provide a list of "hot" classes and methods to the ART compiler.
 *       These are AOT-compiled at install time, so they're native code on
 *       first launch. Without profiles, JIT compilation causes slower first
 *       run. Can improve startup by 20-30%.
 *
 * ❓ Q5: What happens when you call startActivity()?
 *    A: Intent is sent via Binder to ActivityManagerService (AMS) in the
 *       system_server process. AMS resolves the Intent, checks permissions,
 *       determines target component, and instructs the target process to
 *       create/resume the Activity. If the process doesn't exist, AMS
 *       tells Zygote to fork a new one first.
 *
 * ❓ Q6: Your app has a high ANR rate. How do you diagnose and fix it?
 *    A: 1. Check Play Console ANR clusters for stack traces.
 *       2. Look at /data/anr/traces.txt for the thread dump — find what the
 *          main thread was blocked on (lock, I/O, binder call).
 *       3. Enable StrictMode in debug to catch disk/network on main thread.
 *       4. Use Systrace/Perfetto to see main thread activity around the ANR.
 *       5. Common fixes: move I/O to Dispatchers.IO, use
 *          SharedPreferences.apply() instead of commit(), use async BroadcastReceiver
 *          with goAsync() for long work, avoid holding locks across threads.
 *
 * ❓ Q7: Explain onTrimMemory levels and how you'd respond to memory pressure.
 *    A: onTrimMemory is called with increasing severity levels (5→80).
 *       RUNNING_* levels (5/10/15): app is foreground but device is low —
 *       progressively reduce caches. UI_HIDDEN (20): release UI resources.
 *       BACKGROUND/MODERATE/COMPLETE (40/60/80): app is in background LRU,
 *       risk of being killed — release everything non-essential.
 *       Implementation: use LruCache.trimToSize() to progressively shrink,
 *       clear image/data caches, release held resources. Responding properly
 *       keeps your app alive longer in the background.
 */
