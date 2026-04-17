/**
 * ============================================================================
 * 03 — JETPACK COMPOSE
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Compose Mental Model — Declarative vs Imperative
 *   2. Composition, Recomposition & Skipping
 *   3. State Management (remember, rememberSaveable, derivedStateOf)
 *   4. State Hoisting Pattern
 *   5. Side Effects (LaunchedEffect, DisposableEffect, SideEffect, etc.)
 *   6. Compose Lifecycle & Phases
 *   7. Stability & Performance (Stable, Immutable annotations)
 *   8. CompositionLocal
 *   9. Custom Layouts & Modifiers
 *  10. Compose Compiler Internals
 *  11. Compose Navigation
 *  12. Material 3, Theming & Accessibility
 *  13. Compose Interop (AndroidView, ComposeView)
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. DECLARATIVE VS IMPERATIVE — MENTAL MODEL
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Imperative (View System):
 *   val textView = findViewById<TextView>(R.id.title)
 *   textView.text = "Hello"
 *   textView.visibility = if (show) View.VISIBLE else View.GONE
 *   → You MUTATE existing view objects
 *
 * Declarative (Compose):
 *   @Composable
 *   fun Greeting(name: String) {
 *       Text("Hello, $name")
 *   }
 *   → You DESCRIBE what the UI should look like for a given state.
 *   → Compose framework handles creating/updating the actual UI nodes.
 *
 * 💡 Key Insight: In Compose, you don't update UI — you re-describe it.
 *    The framework diffs the new description against the old one and
 *    applies only the necessary changes (similar to React's virtual DOM).
 */


// ─────────────────────────────────────────────────────────────────────────────
// 2. COMPOSITION & RECOMPOSITION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Composition: First time Compose executes your @Composable functions
 *              and builds the UI tree (slot table / composition).
 *
 * Recomposition: When state changes, Compose re-executes ONLY the
 *                composables that read that state. This is "smart recomposition".
 *
 * ❓ Interview Question: When does recomposition happen?
 *
 *    Answer: When a State<T> object that was READ during composition changes.
 *    Compose tracks which composables read which state objects at runtime.
 *    Only those composables are re-executed.
 *
 * ❓ Follow-up: Can recomposition be skipped?
 *
 *    Answer: Yes! If a composable's parameters haven't changed (by equality),
 *    Compose can skip re-executing it. This requires parameters to be:
 *    - Primitive types
 *    - @Stable or @Immutable annotated classes
 *    - Other composable functions (lambda stability)
 *
 * ⚠️ GOTCHA: Recomposition can happen in ANY ORDER and CONCURRENTLY.
 *    Never rely on execution order or side effects in composable functions.
 *    Never write to shared mutable state inside a composable.
 */

@Composable
fun RecompositionDemo() {
    var count by remember { mutableStateOf(0) }

    Column {
        // This Text recomposes when `count` changes
        Text("Count: $count")

        // This Text does NOT recompose — it reads no state
        Text("Static text")

        Button(onClick = { count++ }) {
            Text("Increment")
        }
    }
}

/**
 * Positional Memoization:
 *   Compose identifies composables by their POSITION in the call tree,
 *   not by variable name. This is why:
 *   - The same composable function called twice creates two separate slots
 *   - `key()` is needed when positions can shift (dynamic lists)
 */

@Composable
fun PositionalMemoizationExample(showHeader: Boolean) {
    Column {
        if (showHeader) {
            Text("Header")  // Slot 0 when present
        }
        // ⚠️ Without key(), this Text's slot shifts when showHeader changes,
        // potentially losing its remembered state
        Text("Content")

        // ✅ Fix: Use key() to give stable identity
        key("content") {
            Text("Content")
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 3. STATE MANAGEMENT
// ─────────────────────────────────────────────────────────────────────────────

/**
 * State in Compose = any value that can change over time and affects the UI.
 *
 * mutableStateOf()    → creates observable state
 * remember { }        → retains value across recompositions (NOT config change)
 * rememberSaveable {} → retains across recomposition AND config change/process death
 * derivedStateOf {}   → computed state; recomposes only when result changes
 */

@Composable
fun StateManagementShowcase() {
    // ── remember: survives recomposition, lost on config change ──
    var counter by remember { mutableStateOf(0) }

    // ── rememberSaveable: survives recomposition + config change + process death ──
    var userInput by rememberSaveable { mutableStateOf("") }

    // ── derivedStateOf: only recomposes when the RESULT changes ──
    val items = remember { mutableStateListOf<String>() }
    val hasItems by remember { derivedStateOf { items.isNotEmpty() } }
    // Even if items changes from ["a","b"] to ["a","c"], hasItems doesn't recompose
    // because the boolean value (true) didn't change.

    // ── snapshotFlow: convert Compose state to a Flow ──
    val listState = rememberLazyListState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .filter { it > 5 }
            .collect { /* Show "scroll to top" FAB */ }
    }

    // ── collectAsStateWithLifecycle: safely collect Flow as Compose state ──
    // Lifecycle-aware: stops collecting when UI goes to background
    // ✅ Use this instead of collectAsState()
    val viewModel: MyViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
}

/**
 * ❓ Interview Question: What's the difference between remember and rememberSaveable?
 *
 *   remember:
 *     - Stores value in the Composition's slot table
 *     - Survives recomposition
 *     - Lost on configuration change (Activity recreation)
 *     - Can store ANY type
 *
 *   rememberSaveable:
 *     - Backed by onSaveInstanceState Bundle
 *     - Survives config change AND process death
 *     - Limited to Bundle-compatible types (primitives, Parcelable, Serializable)
 *     - Custom types need a Saver
 *
 * ❓ Follow-up: How do you save a custom class with rememberSaveable?
 */

// Custom Saver for rememberSaveable
data class ComposeSearchFilter(
    val query: String,
    val category: String,
    val sortBy: SortOrder
)

val ComposeSearchFilterSaver = run {
    val queryKey = "query"
    val categoryKey = "category"
    val sortKey = "sort"
    mapSaver(
        save = { filter ->
            mapOf(
                queryKey to filter.query,
                categoryKey to filter.category,
                sortKey to filter.sortBy.name
            )
        },
        restore = { map ->
            ComposeSearchFilter(
                query = map[queryKey] as String,
                category = map[categoryKey] as String,
                sortBy = SortOrder.valueOf(map[sortKey] as String)
            )
        }
    )
}

@Composable
fun SaveableSearchScreen() {
    var filter by rememberSaveable(stateSaver = ComposeSearchFilterSaver) {
        mutableStateOf(ComposeSearchFilter("", "all", SortOrder.NEWEST))
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 4. STATE HOISTING
// ─────────────────────────────────────────────────────────────────────────────

/**
 * State Hoisting = moving state UP to the caller so the composable is stateless.
 *
 * Pattern:
 *   - Stateful composable: owns state internally (via remember)
 *   - Stateless composable: receives state + event callbacks as parameters
 *
 * Benefits:
 *   ✅ Reusable — no internal state assumptions
 *   ✅ Testable — inject any state for testing
 *   ✅ Previewable — create @Preview with any state
 *   ✅ Single source of truth — parent controls the state
 *
 * Google's Rule: Hoist state to the lowest common ancestor that needs it.
 */

// ❌ Bad: Stateful — hard to test, can't control externally
@Composable
fun SearchBarBad() {
    var query by remember { mutableStateOf("") }
    TextField(value = query, onValueChange = { query = it })
}

// ✅ Good: Stateless — state hoisted to caller
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
    )
}

// Stateful wrapper if needed
@Composable
fun SearchBarStateful() {
    var query by rememberSaveable { mutableStateOf("") }
    SearchBar(query = query, onQueryChange = { query = it })
}


// ─────────────────────────────────────────────────────────────────────────────
// 5. SIDE EFFECTS — COMPLETE GUIDE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Side effect = any work that escapes the scope of a composable function.
 * Examples: API calls, analytics, registering listeners, writing to DB.
 *
 * Compose provides specific APIs because:
 *   - Composable functions can be called many times (recomposition)
 *   - They can be called in any order or concurrently
 *   - They can be skipped if the result is the same
 *
 * ⚠️ NEVER perform side effects directly in a composable body.
 */

@Composable
fun SideEffectsShowcase(userId: String, analyticsHelper: AnalyticsHelper) {

    // ── LaunchedEffect ──
    // Runs a suspend function when keys change. Cancels + restarts on key change.
    // Use for: API calls, one-time operations, coroutine-based work
    LaunchedEffect(userId) {
        // This cancels & restarts when userId changes
        val userData = repository.fetchUser(userId)
        // update state...
    }

    // ── LaunchedEffect(Unit) ──
    // Runs ONCE when composable enters composition. Never restarts.
    LaunchedEffect(Unit) {
        // One-time initialization
        analyticsHelper.logScreenView("profile")
    }

    // ── DisposableEffect ──
    // For registering/unregistering listeners. Has cleanup via onDispose.
    // Use for: Lifecycle observers, broadcast receivers, callbacks
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> { /* ... */ }
                Lifecycle.Event.ON_PAUSE -> { /* ... */ }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ── SideEffect ──
    // Runs EVERY recomposition. No coroutine scope. Synchronous.
    // Use for: Updating non-Compose state that needs to stay in sync
    SideEffect {
        analyticsHelper.setUserProperty("screen", "profile_$userId")
    }

    // ── rememberCoroutineScope ──
    // Gets a CoroutineScope tied to the composable's lifecycle.
    // Use for: Launching coroutines from callbacks (onClick, etc.)
    val scope = rememberCoroutineScope()
    Button(onClick = {
        scope.launch {
            // Can do suspend work in response to user action
            repository.toggleBookmark(userId)
        }
    }) {
        Text("Bookmark")
    }

    // ── rememberUpdatedState ──
    // Captures the latest value of a parameter for use in long-running effects.
    // Prevents LaunchedEffect from capturing a stale closure.
    val currentOnTimeout by rememberUpdatedState(onTimeout)
    LaunchedEffect(Unit) {
        delay(5000)
        currentOnTimeout()  // Always calls the LATEST onTimeout, not the initial one
    }

    // ── produceState ──
    // Converts non-Compose state (Flow, callback) into Compose State.
    val imageState by produceState<ImageLoadState>(
        initialValue = ImageLoadState.Loading,
        key1 = imageUrl
    ) {
        value = try {
            val bitmap = imageLoader.load(imageUrl)
            ImageLoadState.Success(bitmap)
        } catch (e: Exception) {
            ImageLoadState.Error(e)
        }
    }
}

/**
 * ⚠️ GOTCHA — Side Effect Pitfalls:
 *
 * 1. LaunchedEffect(Unit) with changing parameters:
 *    If you use a parameter inside LaunchedEffect(Unit), it captures the
 *    INITIAL value. Use rememberUpdatedState or add the param to keys.
 *
 * 2. Forgetting onDispose in DisposableEffect:
 *    → Memory leak (listener not unregistered)
 *
 * 3. Performing network calls directly in composable:
 *    @Composable fun Bad() {
 *        val data = api.fetchData()  // ❌ Called on every recomposition!
 *    }
 *    → Use LaunchedEffect or move to ViewModel
 */


// ─────────────────────────────────────────────────────────────────────────────
// 6. COMPOSE PHASES & LIFECYCLE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Compose has 3 phases per frame:
 *
 *   1. Composition  → WHAT to show (runs @Composable functions, builds UI tree)
 *   2. Layout       → WHERE to place it (measure + place each node)
 *   3. Drawing      → HOW to render it (draws pixels to Canvas)
 *
 * Reading state in different phases:
 *   - State read in Composition → triggers recomposition
 *   - State read in Layout (via Modifier.layout) → triggers re-layout only
 *   - State read in Drawing (via Modifier.drawBehind) → triggers re-draw only
 *
 * 💡 Performance Tip: Defer state reads to the latest possible phase.
 *    If state only affects position, read it in Layout phase.
 *    If state only affects visuals, read it in Drawing phase.
 */

@Composable
fun PhaseOptimizationExample() {
    var offset by remember { mutableStateOf(0f) }

    // ❌ Reading offset in Composition — triggers full recomposition
    Box(modifier = Modifier.offset(x = offset.dp))

    // ✅ Reading offset in Layout phase — skips Composition
    Box(modifier = Modifier.offset { IntOffset(offset.toInt(), 0) })

    // ✅ Reading offset in Drawing phase — skips Composition AND Layout
    Box(modifier = Modifier.drawBehind {
        drawCircle(Color.Red, center = Offset(offset, 0f))
    })
}


// ─────────────────────────────────────────────────────────────────────────────
// 7. STABILITY & PERFORMANCE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * For Compose to skip recomposition, it must know if parameters changed.
 * This requires types to be "stable" — their equals() is reliable.
 *
 * Automatically stable:
 *   - Primitives (Int, String, Boolean, Float, etc.)
 *   - data class with all stable fields
 *   - Enum classes
 *   - Function types (lambdas)
 *
 * Unstable (can't skip):
 *   - List, Set, Map (interfaces — implementation might be mutable)
 *   - Classes from external modules (Compose compiler can't verify)
 *   - var properties
 */

// ❌ Unstable — List<T> is an interface; could be mutable
@Composable
fun UnstableExample(items: List<String>) { /* Always recomposes */ }

// ✅ Stable — ImmutableList is known to be immutable
@Composable
fun StableExample(items: ImmutableList<String>) { /* Can skip recomposition */ }

// ✅ Annotate your own classes
@Immutable  // Promise to Compose: all fields are deeply immutable
data class ArticleUi(
    val id: String,
    val title: String,
    val author: String,
    val tags: ImmutableList<String>
)

@Stable  // Promise to Compose: mutable, but changes are observable via Compose State
class CartState {
    var itemCount by mutableStateOf(0)
        private set
    fun addItem() { itemCount++ }
}

/**
 * Lambda Stability:
 *
 *   ❌ Unstable lambda (captures unstable variable):
 *       items.forEach { item ->
 *           Button(onClick = { onItemClick(item) })  // New lambda each recomposition
 *       }
 *
 *   ✅ Stable lambda via remember:
 *       val onClick = remember(item.id) { { onItemClick(item) } }
 *       Button(onClick = onClick)
 *
 *   ✅ Or use method reference:
 *       Button(onClick = viewModel::onItemClick)
 */


// ─────────────────────────────────────────────────────────────────────────────
// 8. COMPOSITIONLOCAL
// ─────────────────────────────────────────────────────────────────────────────

/**
 * CompositionLocal provides implicit data down the composition tree.
 * Like React's Context — avoids threading parameters through every composable.
 *
 * Built-in examples:
 *   LocalContext.current           → Android Context
 *   LocalLifecycleOwner.current   → Lifecycle owner
 *   LocalDensity.current          → Screen density
 *   LocalConfiguration.current    → Device configuration
 *
 * staticCompositionLocalOf   → Value rarely changes (theme, DI). More efficient.
 * compositionLocalOf         → Value may change. Recomposes readers on change.
 */

// Custom CompositionLocal
val LocalAnalytics = staticCompositionLocalOf<AnalyticsHelper> {
    error("No AnalyticsHelper provided")
}

@Composable
fun AppRoot() {
    CompositionLocalProvider(LocalAnalytics provides AnalyticsHelperImpl()) {
        AppNavigation()  // All descendants can access LocalAnalytics.current
    }
}

@Composable
fun DeepNestedScreen() {
    val analytics = LocalAnalytics.current  // No parameter threading needed
    LaunchedEffect(Unit) {
        analytics.logScreenView("deep_nested")
    }
}

/**
 * ⚠️ When NOT to use CompositionLocal:
 *   - For data that only a few composables need (just pass as parameter)
 *   - For frequently changing data (performance overhead)
 *   - When it makes the composable harder to test
 *
 * ✅ Good uses: Theme, analytics, navigation, DI-provided dependencies
 */


// ─────────────────────────────────────────────────────────────────────────────
// 9. CUSTOM LAYOUTS & MODIFIERS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Custom Layout — when standard Row/Column/Box aren't enough.
 * You get full control over measuring and placing children.
 */

@Composable
fun StaggeredGrid(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val columnWidth = constraints.maxWidth / columns
        val itemConstraints = constraints.copy(
            minWidth = 0, maxWidth = columnWidth
        )

        val placeables = measurables.map { it.measure(itemConstraints) }
        val columnHeights = IntArray(columns)

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { placeable ->
                val shortestColumn = columnHeights.indexOfMin()
                placeable.placeRelative(
                    x = shortestColumn * columnWidth,
                    y = columnHeights[shortestColumn]
                )
                columnHeights[shortestColumn] += placeable.height
            }
        }
    }
}

// Custom Modifier
fun Modifier.shimmer(isLoading: Boolean): Modifier = composed {
    if (!isLoading) return@composed this

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    this.drawWithContent {
        drawContent()
        val brush = Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.6f)
            ),
            start = Offset(translateAnim - 200f, 0f),
            end = Offset(translateAnim, 0f)
        )
        drawRect(brush = brush)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 10. COMPOSE COMPILER INTERNALS (ADVANCED)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * What the Compose compiler does to your @Composable functions:
 *
 * 1. Adds a Composer parameter to every @Composable function
 *    fun Greeting(name: String) → fun Greeting(name: String, $composer: Composer, $changed: Int)
 *
 * 2. Generates groups in the slot table:
 *    - startGroup / endGroup calls that track composable identity
 *    - Each call position gets a unique key
 *
 * 3. Adds comparison checks ($changed bitmask):
 *    - Tracks which parameters changed since last composition
 *    - If nothing changed, can skip the composable entirely
 *
 * 4. Transforms remember {} calls:
 *    - Stores/retrieves values from the slot table
 *    - Invalidates when keys change
 *
 * 5. Generates restartable groups:
 *    - Each composable that reads state becomes a "restart scope"
 *    - When state changes, only that scope re-executes
 *
 * 💡 Interview Tip: Understanding this helps explain WHY:
 *    - @Composable functions can only call other @Composable functions
 *    - Compose can skip unchanged composables
 *    - Stability matters for performance
 */


// ─────────────────────────────────────────────────────────────────────────────
// 11. COMPOSE NAVIGATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Navigation in Compose replaces the Fragment-based Navigation component.
 *
 * Core components:
 *   NavController       → manages the back stack and navigation actions
 *   NavHost             → container that swaps composables based on the current route
 *   NavGraph            → set of destinations (composable routes)
 *   NavBackStackEntry   → holds arguments and scoped state for a destination
 *
 * 💡 Interview Tip: Know both the string-based route API and the newer
 *    type-safe Kotlin Serializable route API (Navigation 2.8+).
 */

// ── String-based route approach ──

@Composable
fun StringRouteNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onArticleClick = { articleId ->
                    navController.navigate("article/$articleId")
                },
                onProfileClick = {
                    navController.navigate("profile")
                }
            )
        }

        composable(
            route = "article/{articleId}",
            arguments = listOf(
                navArgument("articleId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
            ArticleDetailScreen(articleId = articleId)
        }

        // Optional arguments with default values
        composable(
            route = "search?query={query}&category={category}",
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("category") {
                    type = NavType.StringType
                    defaultValue = "all"
                }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            val category = backStackEntry.arguments?.getString("category") ?: "all"
            SearchScreen(initialQuery = query, category = category)
        }

        // Deep link support
        composable(
            route = "article/{articleId}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://myapp.com/article/{articleId}" }
            )
        ) { /* ... */ }
    }
}

// ── Type-safe route approach (Navigation 2.8+, Kotlin Serializable) ──

@Serializable
data class ArticleRoute(val articleId: String)

@Serializable
data class SearchRoute(
    val query: String = "",
    val category: String = "all"
)

@Serializable
object HomeRoute

@Serializable
object ProfileRoute

@Composable
fun TypeSafeNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onArticleClick = { id -> navController.navigate(ArticleRoute(id)) }
            )
        }

        composable<ArticleRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ArticleRoute>()
            ArticleDetailScreen(articleId = route.articleId)
        }

        composable<SearchRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<SearchRoute>()
            SearchScreen(initialQuery = route.query, category = route.category)
        }
    }
}

/**
 * Scoping ViewModels to a NavGraph:
 *   By default, ViewModels are scoped to the NavBackStackEntry of a destination.
 *   You can scope them to a nested nav graph to share across screens.
 */

@Composable
fun SharedViewModelExample(navController: NavHostController) {
    val parentEntry = remember(navController) {
        navController.getBackStackEntry("checkout_graph")
    }
    val sharedViewModel: CheckoutViewModel = hiltViewModel(parentEntry)
}

/**
 * Custom back handling:
 *   BackHandler from activity-compose intercepts the system back button.
 */

@Composable
fun ScreenWithCustomBack(onBack: () -> Unit) {
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = hasUnsavedChanges) {
        showDialog = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false; onBack() }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
            title = { Text("Unsaved changes") },
            text = { Text("Discard your changes?") }
        )
    }
}

/**
 * ⚠️ Navigation Gotchas:
 *   1. Avoid creating a new NavController on recomposition — always use rememberNavController()
 *   2. navigate() adds to the back stack by default; use popUpTo for login/splash flows
 *   3. Don't pass complex objects as arguments — pass IDs and fetch from ViewModel/repository
 *   4. Use launchSingleTop = true to avoid duplicate destinations on rapid taps
 */


// ─────────────────────────────────────────────────────────────────────────────
// 12. MATERIAL 3, THEMING & ACCESSIBILITY
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Material 3 (Material You) in Compose:
 *
 * MaterialTheme provides:
 *   colorScheme  → color tokens (primary, secondary, surface, error, etc.)
 *   typography   → text styles (displayLarge, titleMedium, bodySmall, etc.)
 *   shapes       → corner rounding (small, medium, large, extraLarge)
 *
 * Dynamic Color (Material You — Android 12+):
 *   Colors derived from the user's wallpaper. Use dynamicDarkColorScheme()
 *   and dynamicLightColorScheme() with a fallback for older devices.
 */

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212)
        )
        else -> lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            background = Color(0xFFFFFBFE)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(
            titleLarge = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp
            )
        ),
        shapes = Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp)
        ),
        content = content
    )
}

/**
 * Accessibility in Compose:
 *
 * Compose uses a Semantics tree (parallel to the UI tree) that screen readers
 * (TalkBack) and testing tools use to understand your UI.
 *
 * Key APIs:
 *   contentDescription     → describes non-text elements (images, icons)
 *   semantics { }          → attach semantic properties to a composable
 *   mergeDescendants       → treat a subtree as a single accessible element
 *   clearAndSetSemantics   → override all child semantics (useful for custom widgets)
 *
 * ⚠️ Touch target size: Material guidelines require at least 48dp × 48dp.
 *    Use Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp) or
 *    Modifier.minimumInteractiveComponentSize() (Material 3).
 */

@Composable
fun AccessibilityExample() {
    // contentDescription for images
    Image(
        painter = painterResource(R.drawable.logo),
        contentDescription = "App logo",  // Read by TalkBack
        modifier = Modifier.size(48.dp)
    )

    // Decorative image — null contentDescription is intentional
    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = null  // Ignored by screen readers
    )

    // mergeDescendants — card read as a single element
    Row(
        modifier = Modifier.semantics(mergeDescendants = true) { }
    ) {
        Image(
            painter = painterResource(R.drawable.avatar),
            contentDescription = null  // Merged into parent
        )
        Column {
            Text("Jane Doe")
            Text("Senior Engineer")
        }
    }
    // TalkBack reads: "Jane Doe, Senior Engineer" as one item

    // clearAndSetSemantics — replace all child semantics
    Row(
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = "Rating: 4 out of 5 stars"
        }
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < 4) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null
            )
        }
    }

    // Minimum touch target
    IconButton(
        onClick = { /* ... */ },
        modifier = Modifier.minimumInteractiveComponentSize()
    ) {
        Icon(Icons.Default.Close, contentDescription = "Close")
    }
}

/**
 * Focus management for screen readers:
 *   Use FocusRequester to programmatically move focus after state changes
 *   (e.g., after showing an error, move focus to the error message).
 */

@Composable
fun FocusManagementExample() {
    val focusRequester = remember { FocusRequester() }
    var showError by remember { mutableStateOf(false) }

    Column {
        if (showError) {
            Text(
                "Invalid input",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.focusRequester(focusRequester)
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
        TextField(value = "", onValueChange = {})
    }
}

/**
 * 💡 Interview Tip: Google cares deeply about accessibility. Be ready to discuss:
 *    - How you'd make a custom component accessible
 *    - The difference between contentDescription and text for semantics
 *    - How mergeDescendants affects the TalkBack traversal order
 *    - Testing accessibility with Accessibility Scanner or Espresso
 */


// ─────────────────────────────────────────────────────────────────────────────
// 13. COMPOSE INTEROP
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Compose ↔ View system interoperability is essential for incremental migration.
 *
 * Two directions:
 *   1. Compose inside Views:  ComposeView (embed Compose in Fragment/Activity XML)
 *   2. Views inside Compose:  AndroidView (embed legacy View widgets in Compose)
 *
 * 💡 Interview Tip: Google's recommended migration strategy is bottom-up —
 *    start by replacing individual screens or leaf components with Compose,
 *    while the Activity/Fragment shell remains.
 */

// ── AndroidView: Embedding a View widget inside Compose ──

@Composable
fun MapComposable(
    onMapReady: (MapView) -> Unit,
    modifier: Modifier = Modifier
) {
    var mapView: MapView? by remember { mutableStateOf(null) }

    AndroidView(
        factory = { context ->
            MapView(context).also {
                it.onCreate(null)
                mapView = it
            }
        },
        update = { view ->
            // Called on every recomposition — update the View's state here
            view.getMapAsync { googleMap ->
                onMapReady(view)
            }
        },
        modifier = modifier
    )

    // Handle lifecycle for the MapView
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView?.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

// Wrapping an AdView (common real-world interop case)
@Composable
fun AdBanner(adUnitId: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = modifier
    )
}

// ── ComposeView: Embedding Compose inside a Fragment ──

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                AppTheme {
                    ProfileScreen()
                }
            }
        }
    }
}

/**
 * ViewCompositionStrategy — controls when Compose disposes:
 *
 *   DisposeOnDetachedFromWindow        → default; disposes when view detaches
 *   DisposeOnDetachedFromWindowOrReleasedFromPool → for RecyclerView items
 *   DisposeOnViewTreeLifecycleDestroyed → ✅ best for Fragments (matches view lifecycle)
 *   DisposeOnLifecycleDestroyed        → ties to a specific Lifecycle
 *
 * ⚠️ Common Pitfalls:
 *   1. Theme inheritance: ComposeView doesn't inherit Material theme from XML.
 *      You must wrap content in your Compose theme.
 *   2. Lifecycle mismatch: Fragment's view lifecycle ≠ Fragment lifecycle.
 *      Always use DisposeOnViewTreeLifecycleDestroyed in Fragments.
 *   3. Inflation order: If mixing XML and Compose in the same layout,
 *      remember that AndroidView factory runs once, update runs on recomposition.
 *   4. Keyboard handling: IME behavior can differ between View and Compose text fields.
 */


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: What's the difference between State and MutableState?
 *    A: State<T> is read-only interface. MutableState<T> extends it with a setter.
 *       Expose State<T> publicly, keep MutableState<T> private — same as
 *       StateFlow/MutableStateFlow pattern.
 *
 * ❓ Q2: Why shouldn't you use collectAsState for StateFlow from a ViewModel?
 *    A: collectAsState keeps collecting even when the app is in background.
 *       Use collectAsStateWithLifecycle() instead — it stops collecting in
 *       onStop and resumes in onStart, saving resources.
 *
 * ❓ Q3: What happens if you use var x = 0 instead of mutableStateOf?
 *    A: Compose won't track changes. The variable will change but
 *       recomposition won't be triggered. UI stays stale.
 *
 * ❓ Q4: How do you animate between two screens in Compose?
 *    A: AnimatedNavHost (Navigation Compose + animation), AnimatedContent,
 *       Crossfade, or SharedElement transitions (Compose 1.7+).
 *
 * ❓ Q5: What is the Snapshot system in Compose?
 *    A: Compose uses a snapshot system (similar to software transactional memory)
 *       to track state reads/writes. When mutableStateOf() is read during
 *       composition, the snapshot records it. When written to, all compositions
 *       that read it are scheduled for recomposition.
 *
 * ❓ Q6: How do you handle lists in Compose efficiently?
 *    A: LazyColumn/LazyRow with key parameter for stable identity.
 *       Use ImmutableList for skippable recomposition.
 *       Use derivedStateOf for computed list properties.
 *       Avoid creating new list instances in every recomposition.
 *
 * ❓ Q7: How would you implement a multi-screen checkout flow with shared state
 *        using Compose Navigation?
 *    A: Create a nested NavGraph for the checkout flow. Scope a shared
 *       CheckoutViewModel to the nested graph's NavBackStackEntry using
 *       hiltViewModel(navController.getBackStackEntry("checkout_graph")).
 *       All screens in the graph share the same ViewModel instance.
 *       On completion, pop the entire graph: navController.popBackStack("checkout_graph", inclusive = true).
 *
 * ❓ Q8: How do you make a custom rating bar accessible for TalkBack users?
 *    A: Use clearAndSetSemantics on the container to replace individual star
 *       semantics with a single description like "Rating: 4 out of 5 stars".
 *       Also add a stateDescription for the current value and an adjustable
 *       action (via semantics { }) so users can change the rating with
 *       swipe-up/swipe-down gestures in TalkBack.
 */
