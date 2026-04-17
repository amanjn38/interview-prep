/**
 * ============================================================================
 * 01 — ANDROID FUNDAMENTALS
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Activity Lifecycle (deep dive)
 *   2. Fragment Lifecycle
 *   3. Activity ↔ Fragment interleaved lifecycle
 *   4. Configuration Changes & State Restoration
 *   5. Intent System (Explicit, Implicit, PendingIntent)
 *   6. Android Manifest & Permissions
 *   7. Context Types (Application vs Activity)
 *   8. Launch Modes & Task Affinity
 *   9. Process Death & Restoration
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. ACTIVITY LIFECYCLE — DEEP DIVE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The full Activity lifecycle:
 *
 *   onCreate()    → Activity is first created. Initialize views, bind data.
 *                   Bundle? is non-null on recreation (config change / process death).
 *
 *   onStart()     → Activity becomes VISIBLE but not interactive.
 *                   Register UI-related observers here.
 *
 *   onResume()    → Activity is in FOREGROUND and interactive.
 *                   Resume animations, camera, sensors.
 *                   ⚠️ Google expects P99 onResume < 80ms for smooth UX.
 *
 *   onPause()     → Activity is PARTIALLY VISIBLE (dialog / multi-window).
 *                   Release exclusive resources (camera), pause animations.
 *                   ⚠️ Keep this fast — next Activity's onResume() waits for this.
 *
 *   onStop()      → Activity is NO LONGER VISIBLE.
 *                   Release heavy resources, unregister broadcast receivers.
 *                   Good place for database writes / analytics events.
 *
 *   onDestroy()   → Activity is being DESTROYED.
 *                   Final cleanup. isFinishing() distinguishes:
 *                     - true  → user navigated away (back press, finish())
 *                     - false → system is destroying for config change
 *
 *   onRestart()   → Called when Activity is coming back from STOPPED state
 *                   (user navigated away and came back).
 *                   Sequence: onStop() → onRestart() → onStart()
 *
 * 💡 Interview Tip: Draw the lifecycle diagram on the whiteboard.
 *    Know which callbacks are guaranteed (onPause/onStop) vs. not (onDestroy).
 */

class LifecycleAwareActivity : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private var cameraSource: CameraSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // savedInstanceState is non-null when recreating after config change or process death
        if (savedInstanceState != null) {
            val scrollPosition = savedInstanceState.getInt("SCROLL_POS", 0)
            val draftText = savedInstanceState.getString("DRAFT_TEXT", "")
            restoreUIState(scrollPosition, draftText)
        }

        initializeComponents()
    }

    override fun onStart() {
        super.onStart()
        // Register lifecycle-aware observers
        registerConnectivityObserver()
    }

    override fun onResume() {
        super.onResume()
        // Resume camera preview, start animations
        cameraSource?.start()
        startUIAnimations()
    }

    override fun onPause() {
        super.onPause()
        // Release camera — another activity might need it
        cameraSource?.stop()
        pauseUIAnimations()
    }

    override fun onStop() {
        super.onStop()
        // Heavy cleanup — Activity is no longer visible
        saveAnalyticsEvent("screen_exit")
        unregisterConnectivityObserver()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save transient UI state (NOT business data — that goes in ViewModel/DB)
        outState.putInt("SCROLL_POS", getCurrentScrollPosition())
        outState.putString("DRAFT_TEXT", getDraftText())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            // User is leaving for good — final cleanup
            releaseAllResources()
        }
        // If !isFinishing, system is destroying for config change — ViewModel survives
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 2. FRAGMENT LIFECYCLE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Fragment has additional lifecycle callbacks beyond Activity:
 *
 *   onAttach(context)    → Fragment attached to Activity. Get reference to Activity here.
 *   onCreate(bundle)     → Initialize non-view logic.
 *   onCreateView()       → Inflate the view hierarchy. Return the root view.
 *   onViewCreated()      → View is ready. Set up RecyclerView, observers, click listeners.
 *                          ⚠️ This is where you should observe LiveData/StateFlow.
 *   onStart()            → Fragment is visible.
 *   onResume()           → Fragment is interactive.
 *   onPause()            → Fragment loses focus.
 *   onStop()             → Fragment not visible.
 *   onDestroyView()      → View hierarchy is destroyed. Clean up view references.
 *                          ⚠️ Fragment instance may survive (backstack). Null out _binding here.
 *   onDestroy()          → Fragment instance destroyed.
 *   onDetach()           → Fragment detached from Activity.
 *
 * 💡 Key Insight: Fragment VIEW lifecycle ≠ Fragment INSTANCE lifecycle.
 *    When on backstack, view is destroyed but instance stays alive.
 *    This is the #1 source of memory leaks in Fragments.
 */

class ExampleFragment : Fragment(R.layout.fragment_example) {

    private var _binding: FragmentExampleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExampleViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentExampleBinding.bind(view)

        // ✅ Use viewLifecycleOwner, NOT this (the Fragment instance)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // ⚠️ MUST null out binding to prevent memory leak
        // Fragment instance survives on backstack but view is destroyed
        _binding = null
    }
}

/**
 * ⚠️ GOTCHA — viewLifecycleOwner vs this (Fragment)
 *
 *   viewLifecycleOwner → tied to Fragment's VIEW lifecycle (onCreateView → onDestroyView)
 *   this               → tied to Fragment INSTANCE lifecycle (onCreate → onDestroy)
 *
 *   Using `this` to observe LiveData/StateFlow can cause:
 *   - Multiple active observers after backstack navigation
 *   - Updates delivered to a destroyed view → crash
 *
 *   ✅ ALWAYS use viewLifecycleOwner for UI observations
 */


// ─────────────────────────────────────────────────────────────────────────────
// 3. ACTIVITY ↔ ACTIVITY INTERLEAVED LIFECYCLE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Interview Question: Activity A launches Activity B. What's the lifecycle order?
 *
 *   A.onPause()
 *   B.onCreate()
 *   B.onStart()
 *   B.onResume()
 *   A.onStop()        ← A.onStop() happens AFTER B is fully visible
 *
 * ❓ Follow-up: What if B is a transparent/dialog Activity?
 *
 *   A.onPause()       ← A is partially visible, so onStop() is NOT called
 *   B.onCreate()
 *   B.onStart()
 *   B.onResume()
 *                     ← A stays in PAUSED state (still partially visible)
 *
 * ❓ Follow-up: User presses Back on Activity B?
 *
 *   B.onPause()
 *   A.onRestart()
 *   A.onStart()
 *   A.onResume()
 *   B.onStop()
 *   B.onDestroy()
 *
 * 💡 Interview Tip: The key insight is that A.onStop() only fires
 *    when A is COMPLETELY invisible. If B is translucent, A stays PAUSED.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 4. CONFIGURATION CHANGES & STATE RESTORATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Configuration changes that trigger Activity destruction + recreation:
 *   - Screen rotation (orientation)
 *   - Language change (locale)
 *   - Screen size change (foldable, multi-window)
 *   - Dark mode toggle (uiMode)
 *   - Font scale change
 *
 * State preservation layers (in order of precedence):
 *
 *   1. ViewModel             → Survives config change. NOT process death.
 *   2. onSaveInstanceState   → Survives config change AND process death.
 *                               Limited to ~1MB (Binder transaction limit).
 *   3. Persistent storage    → Survives everything (Room, DataStore, SharedPrefs).
 *
 * ⚠️ GOTCHA: ViewModel does NOT survive process death.
 *    Use SavedStateHandle in ViewModel for critical transient state.
 */

class RobustViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Survives config change (in ViewModel memory)
    private val _items = MutableStateFlow<List<Item>>(emptyList())
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    // Survives config change AND process death (via SavedStateHandle → Bundle)
    val searchQuery: StateFlow<String> =
        savedStateHandle.getStateFlow("search_query", "")

    fun updateSearchQuery(query: String) {
        savedStateHandle["search_query"] = query
    }

    // ❓ Follow-up: What data goes where?
    //   ViewModel memory  → fetched data, computed lists (re-fetchable)
    //   SavedStateHandle   → user input, scroll position, selected tab
    //   Room/DataStore     → user preferences, cached API data
}

/**
 * android:configChanges — Opting out of destruction
 *
 * You CAN declare in AndroidManifest:
 *   android:configChanges="orientation|screenSize"
 *
 * This prevents destruction. Instead, onConfigurationChanged() is called.
 *
 * ⚠️ Google discourages this except for specific cases (video players, games).
 * Reason: You must manually handle ALL config-dependent resources.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 5. INTENT SYSTEM
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Intents are the messaging system for inter-component communication.
 *
 * Types:
 *   Explicit Intent → targets a specific component (class name)
 *   Implicit Intent → declares an action; system finds matching components
 */

// Explicit Intent — navigate within your app
fun launchDetailActivity(context: Context, itemId: String) {
    val intent = Intent(context, DetailActivity::class.java).apply {
        putExtra("ITEM_ID", itemId)
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    context.startActivity(intent)
}

// Implicit Intent — delegate to another app
fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    // ⚠️ Always check if any app can handle the intent
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }
}

/**
 * PendingIntent — a token given to another app to execute an Intent on your behalf.
 *
 * Used in: Notifications, AlarmManager, Widgets, MediaSession
 *
 * Flags:
 *   FLAG_IMMUTABLE     → the PendingIntent cannot be modified (API 31+ default)
 *   FLAG_MUTABLE       → allows modification (e.g., inline reply in notifications)
 *   FLAG_UPDATE_CURRENT → updates extras of existing PendingIntent
 *   FLAG_ONE_SHOT       → can only be used once
 *
 * ⚠️ Security: API 31+ requires explicit mutability flag.
 */
fun createNotificationPendingIntent(context: Context): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    return PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}


// ─────────────────────────────────────────────────────────────────────────────
// 6. CONTEXT TYPES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Interview Question: What's the difference between Application Context and Activity Context?
 *
 * Application Context:
 *   - Lives for the entire app process lifetime
 *   - No reference to any Activity or window
 *   - ✅ Safe for: singletons, repositories, DI, WorkManager
 *   - ❌ Cannot: show dialogs, inflate themed views, start Activities (without FLAG_NEW_TASK)
 *
 * Activity Context:
 *   - Lives for the Activity's lifetime
 *   - Has access to the window, theme, and UI
 *   - ✅ Safe for: dialogs, toasts, inflating views, theme-dependent operations
 *   - ⚠️ Danger: storing a reference in a singleton → MEMORY LEAK
 *
 * Service Context:
 *   - Similar to Application Context (no UI)
 *
 * 💡 Rule of Thumb:
 *    Use Application Context for anything that outlives an Activity.
 *    Use Activity Context for anything UI-related.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 7. LAUNCH MODES & TASK AFFINITY
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Launch modes control how Activities are instantiated and placed in tasks.
 *
 * standard (default):
 *   - New instance created every time.
 *   - A → B → A → B creates 4 instances in one task.
 *
 * singleTop:
 *   - If already at the top of the stack, reuse via onNewIntent().
 *   - A → B → B (singleTop) → B receives onNewIntent(), not recreated.
 *   - Use case: search results, notification tap.
 *
 * singleTask:
 *   - Only one instance in the entire system. Gets its own task.
 *   - If exists, all Activities above it are destroyed, onNewIntent() called.
 *   - Use case: home/dashboard screen.
 *
 * singleInstance:
 *   - Like singleTask but the Activity is the ONLY one in its task.
 *   - Use case: phone dialer, payment screen.
 *
 * singleInstancePerTask (API 31+):
 *   - One instance per task, but can exist in multiple tasks.
 *
 * ❓ Interview Follow-up: What's the difference between setting launchMode
 *    in the manifest vs. using Intent flags?
 *
 *    - Manifest: affects ALL launches of that Activity
 *    - Intent flags: affect only that specific launch
 *    - Intent flags take precedence over manifest declarations
 *
 * Common Intent Flags:
 *   FLAG_ACTIVITY_NEW_TASK       → behaves like singleTask
 *   FLAG_ACTIVITY_CLEAR_TOP      → destroys all above; combined with NEW_TASK = singleTask
 *   FLAG_ACTIVITY_SINGLE_TOP     → behaves like singleTop
 *   FLAG_ACTIVITY_CLEAR_TASK     → clears entire task before launching
 *   FLAG_ACTIVITY_NO_HISTORY     → Activity not kept in backstack
 */


// ─────────────────────────────────────────────────────────────────────────────
// 8. PROCESS DEATH & RESTORATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Android can kill your app's process when it's in the background.
 *
 * What survives process death:
 *   ✅ onSaveInstanceState() Bundle
 *   ✅ SavedStateHandle in ViewModel
 *   ✅ Persistent storage (Room, DataStore, files)
 *   ❌ ViewModel (destroyed with the process)
 *   ❌ Static variables
 *   ❌ In-memory caches
 *   ❌ Singleton state
 *
 * What happens when user returns after process death:
 *   1. New process is created
 *   2. Application.onCreate() runs
 *   3. Activity is recreated with savedInstanceState Bundle
 *   4. ViewModel is NEW — SavedStateHandle is restored from Bundle
 *   5. Navigation state is restored (NavController backstack)
 *
 * 💡 How to test process death:
 *   - Android Studio: Terminate Application button (⏹️ with red X)
 *   - ADB: adb shell am kill <package>
 *   - Developer Options: "Don't keep activities" (less accurate)
 *
 * ⚠️ Common bug: App crashes after process death because:
 *   1. Fragment arguments not using SafeArgs / savedInstanceState
 *   2. Companion object / singleton state assumed to persist
 *   3. NavArgs or Intent extras not re-read on recreation
 */

// Pattern to handle process death gracefully in ViewModel
class ProcessDeathSafeViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // Check if restoring from process death
        val restoredQuery = savedStateHandle.get<String>("query")
        if (restoredQuery != null) {
            search(restoredQuery)
        } else {
            loadInitialData()
        }
    }

    fun search(query: String) {
        savedStateHandle["query"] = query  // Persists across process death
        viewModelScope.launch {
            _uiState.value = UiState(isLoading = true)
            val results = repository.search(query)
            _uiState.value = UiState(items = results)
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 9. ANDROID MANIFEST — KEY CONCEPTS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Critical manifest declarations to know:
 *
 * <uses-permission>     → declares permissions the app needs
 * <uses-feature>        → declares hardware/software features required
 * <application>         → app-level config (theme, backup, network security)
 * <activity>            → each Activity with intent-filters, launch mode
 * <service>             → foreground service type (API 34+)
 * <receiver>            → broadcast receivers
 * <provider>            → content providers, FileProvider
 * <meta-data>           → configuration for libraries (Google Maps key, etc.)
 *
 * ❓ Interview Question: What is exported=true vs exported=false?
 *
 *   exported=true   → other apps can start this component
 *   exported=false  → only your app (same UID) can start it
 *
 *   ⚠️ API 31+: If a component has intent-filters, you MUST explicitly
 *   declare android:exported. Previously it defaulted to true.
 *
 * ❓ Interview Question: What is android:allowBackup?
 *
 *   Controls Auto Backup. Set to false for sensitive apps (banking).
 *   API 31+: Use android:dataExtractionRules for fine-grained control.
 */


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS FOR THIS TOPIC
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: Activity A starts Activity B (opaque). User rotates the device.
 *        What lifecycle events fire and in what order?
 *
 *    Answer: Only Activity B is affected (A is already stopped).
 *      B.onPause() → B.onStop() → B.onSaveInstanceState()
 *      → B.onDestroy() → B.onCreate(bundle) → B.onStart() → B.onResume()
 *
 * ❓ Q2: How does a Fragment communicate with its host Activity?
 *
 *    Answer (modern): Use shared ViewModel scoped to Activity.
 *      val sharedVM: SharedViewModel by activityViewModels()
 *    Answer (alternative): Use FragmentResult API.
 *      setFragmentResult("key", bundleOf("data" to value))
 *    ❌ Avoid: Direct Activity reference casts (tight coupling).
 *
 * ❓ Q3: What's the Binder transaction limit and how does it affect
 *        onSaveInstanceState()?
 *
 *    Answer: ~1MB total for all transactions in a process.
 *    Saving too much data in the Bundle can cause TransactionTooLargeException.
 *    Rule: Save only IDs and minimal state; reload data from ViewModel/DB.
 *
 * ❓ Q4: You have a singleton holding an Activity reference.
 *        What happens and how do you fix it?
 *
 *    Answer: Memory leak — Activity can't be GC'd even after onDestroy().
 *    Fix: Use Application Context, WeakReference, or lifecycle-aware components.
 *
 * ❓ Q5: What is the difference between finish() and moveTaskToBack()?
 *
 *    Answer:
 *      finish()          → destroys the Activity, removes from backstack
 *      moveTaskToBack()  → moves entire task to background (like pressing Home)
 */
