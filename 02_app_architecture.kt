/**
 * ============================================================================
 * 02 — APP ARCHITECTURE
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Google's Recommended Architecture (UI → Domain → Data)
 *   2. MVVM Pattern — ViewModel + StateFlow/LiveData
 *   3. MVI Pattern — Unidirectional Data Flow
 *   4. Clean Architecture — UseCases / Interactors
 *   5. Repository Pattern — Single Source of Truth
 *   6. ViewModel Deep Dive
 *   7. MVVM vs MVI — When to Use Which
 *   8. Architecture Components (Lifecycle, Navigation)
 *   9. Multi-Source Data Merging (combine, debounce, flatMapLatest)
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. GOOGLE'S RECOMMENDED ARCHITECTURE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Google's official app architecture has 3 layers:
 *
 *   ┌──────────────────────┐
 *   │      UI Layer        │  Activities, Fragments, Composables
 *   │  (State Holders)     │  ViewModels, UI State classes
 *   ├──────────────────────┤
 *   │    Domain Layer      │  UseCases / Interactors (optional)
 *   │   (Business Logic)   │  Pure Kotlin, no Android deps
 *   ├──────────────────────┤
 *   │     Data Layer       │  Repositories, DataSources
 *   │  (Data Management)   │  API clients, DAOs, DataStore
 *   └──────────────────────┘
 *
 * Key Principles:
 *   - Separation of concerns: Each layer has one job.
 *   - Drive UI from data models: UI should be a pure function of state.
 *   - Single source of truth (SSOT): One authoritative data source per data type.
 *   - Unidirectional data flow (UDF): State flows down, events flow up.
 *
 * Dependency Rule:
 *   UI → Domain → Data (each layer depends only on the layer below)
 *   ❌ Data layer should NEVER reference UI layer
 */


// ─────────────────────────────────────────────────────────────────────────────
// 2. MVVM PATTERN — FULL IMPLEMENTATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * MVVM Components:
 *   Model → Data classes, Repository, database
 *   View  → Activity/Fragment/Composable (passive, no business logic)
 *   ViewModel → Holds UI state, processes user events, calls Repository
 *
 * Data flow:
 *   View ---(events)--→ ViewModel ---(calls)--→ Repository
 *   View ←--(state)---- ViewModel ←--(data)---- Repository
 */

// ── UI State ──
// Immutable data class representing the entire screen state
data class ArticleListUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedFilter: ArticleFilter = ArticleFilter.ALL,
    val isRefreshing: Boolean = false
)

enum class ArticleFilter { ALL, BOOKMARKED, RECENT }

// ── ViewModel ──
class ArticleListViewModel(
    private val getArticlesUseCase: GetArticlesUseCase,
    private val bookmarkArticleUseCase: BookmarkArticleUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleListUiState())
    val uiState: StateFlow<ArticleListUiState> = _uiState.asStateFlow()

    // Restore filter from process death
    private val selectedFilter: StateFlow<ArticleFilter> =
        savedStateHandle.getStateFlow("filter", ArticleFilter.ALL)

    init {
        // React to filter changes
        viewModelScope.launch {
            selectedFilter.collect { filter ->
                loadArticles(filter)
            }
        }
    }

    fun onFilterChanged(filter: ArticleFilter) {
        savedStateHandle["filter"] = filter
    }

    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadArticles(selectedFilter.value, forceRefresh = true)
        }
    }

    fun onBookmarkClicked(articleId: String) {
        viewModelScope.launch {
            bookmarkArticleUseCase(articleId)
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private suspend fun loadArticles(
        filter: ArticleFilter,
        forceRefresh: Boolean = false
    ) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        getArticlesUseCase(filter, forceRefresh)
            .onSuccess { articles ->
                _uiState.update {
                    it.copy(
                        articles = articles,
                        isLoading = false,
                        isRefreshing = false,
                        selectedFilter = filter
                    )
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message
                    )
                }
            }
    }
}

// ── Composable UI (View) ──
@Composable
fun ArticleListScreen(
    viewModel: ArticleListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ArticleListContent(
        uiState = uiState,
        onFilterChanged = viewModel::onFilterChanged,
        onRefresh = viewModel::onRefresh,
        onBookmarkClicked = viewModel::onBookmarkClicked,
        onErrorDismissed = viewModel::onErrorDismissed
    )
}

@Composable
fun ArticleListContent(
    uiState: ArticleListUiState,
    onFilterChanged: (ArticleFilter) -> Unit,
    onRefresh: () -> Unit,
    onBookmarkClicked: (String) -> Unit,
    onErrorDismissed: () -> Unit
) {
    // Stateless composable — all state comes from parameters
    // Easy to preview and test
    Scaffold { padding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(uiState.isRefreshing),
            onRefresh = onRefresh
        ) {
            if (uiState.isLoading && uiState.articles.isEmpty()) {
                LoadingScreen()
            } else {
                LazyColumn(contentPadding = padding) {
                    items(uiState.articles, key = { it.id }) { article ->
                        ArticleItem(
                            article = article,
                            onBookmark = { onBookmarkClicked(article.id) }
                        )
                    }
                }
            }
        }
    }

    uiState.errorMessage?.let { message ->
        ErrorSnackbar(message = message, onDismiss = onErrorDismissed)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 3. MVI PATTERN — UNIDIRECTIONAL DATA FLOW
// ─────────────────────────────────────────────────────────────────────────────

/**
 * MVI: Model-View-Intent
 *
 *   Intent  → user actions / events (not Android Intent)
 *   Model   → immutable state
 *   View    → renders state
 *
 * Flow: View → Intent → Reducer → New State → View
 *
 * Benefits over MVVM:
 *   ✅ Single immutable state — easier to debug, log, replay
 *   ✅ Predictable state transitions — reducer is a pure function
 *   ✅ Time-travel debugging possible
 *   ✅ Better for complex state machines (payments, checkout)
 *
 * Trade-offs:
 *   ❌ More boilerplate (sealed classes for Intent/State)
 *   ❌ Can be overkill for simple CRUD screens
 *
 * 💡 Google Interview: "When would you use MVI over MVVM?"
 *    Answer: Payment flows, fintech, state machines, audit logging.
 *    MVVM is better for simple forms and CRUD.
 */

// ── Sealed classes for Intent and State ──
sealed interface CheckoutIntent {
    data class LoadCart(val userId: String) : CheckoutIntent
    data class UpdateQuantity(val itemId: String, val qty: Int) : CheckoutIntent
    data class ApplyPromoCode(val code: String) : CheckoutIntent
    data object ProceedToPayment : CheckoutIntent
    data class SelectPaymentMethod(val method: PaymentMethod) : CheckoutIntent
    data object ConfirmPayment : CheckoutIntent
}

data class CheckoutState(
    val cartItems: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val promoCode: String? = null,
    val promoError: String? = null,
    val paymentMethod: PaymentMethod? = null,
    val checkoutStep: CheckoutStep = CheckoutStep.CART,
    val isProcessing: Boolean = false,
    val error: String? = null
)

enum class CheckoutStep { CART, PAYMENT_SELECTION, CONFIRMATION, SUCCESS, FAILED }

// Side effects that should happen once (navigation, toasts)
sealed interface CheckoutEffect {
    data class ShowToast(val message: String) : CheckoutEffect
    data object NavigateToSuccess : CheckoutEffect
    data class NavigateToError(val reason: String) : CheckoutEffect
}

// ── MVI ViewModel ──
class CheckoutViewModel(
    private val cartRepository: CartRepository,
    private val paymentRepository: PaymentRepository,
    private val promoRepository: PromoRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    // One-time effects via Channel (not StateFlow — avoids re-delivery)
    private val _effects = Channel<CheckoutEffect>(Channel.BUFFERED)
    val effects: Flow<CheckoutEffect> = _effects.receiveAsFlow()

    fun processIntent(intent: CheckoutIntent) {
        viewModelScope.launch {
            when (intent) {
                is CheckoutIntent.LoadCart -> handleLoadCart(intent.userId)
                is CheckoutIntent.UpdateQuantity -> handleUpdateQty(intent.itemId, intent.qty)
                is CheckoutIntent.ApplyPromoCode -> handleApplyPromo(intent.code)
                is CheckoutIntent.ProceedToPayment -> handleProceedToPayment()
                is CheckoutIntent.SelectPaymentMethod -> handleSelectPayment(intent.method)
                is CheckoutIntent.ConfirmPayment -> handleConfirmPayment()
            }
        }
    }

    // Reducer pattern — pure state transformation
    private fun reduce(transform: CheckoutState.() -> CheckoutState) {
        _state.update { it.transform() }
    }

    private suspend fun handleLoadCart(userId: String) {
        reduce { copy(isProcessing = true) }
        cartRepository.getCart(userId)
            .onSuccess { items ->
                val subtotal = items.sumOf { it.price * it.quantity }
                reduce {
                    copy(cartItems = items, subtotal = subtotal,
                         total = subtotal, isProcessing = false)
                }
            }
            .onFailure { e ->
                reduce { copy(error = e.message, isProcessing = false) }
            }
    }

    private suspend fun handleConfirmPayment() {
        reduce { copy(isProcessing = true, checkoutStep = CheckoutStep.CONFIRMATION) }
        val currentState = _state.value

        paymentRepository.processPayment(
            amount = currentState.total,
            method = currentState.paymentMethod!!
        ).onSuccess {
            reduce { copy(checkoutStep = CheckoutStep.SUCCESS, isProcessing = false) }
            _effects.send(CheckoutEffect.NavigateToSuccess)
        }.onFailure { e ->
            reduce { copy(checkoutStep = CheckoutStep.FAILED, isProcessing = false, error = e.message) }
            _effects.send(CheckoutEffect.NavigateToError(e.message ?: "Payment failed"))
        }
    }

    private suspend fun handleApplyPromo(code: String) { /* ... */ }
    private suspend fun handleUpdateQty(itemId: String, qty: Int) { /* ... */ }
    private suspend fun handleProceedToPayment() { /* ... */ }
    private suspend fun handleSelectPayment(method: PaymentMethod) { /* ... */ }
}


// ─────────────────────────────────────────────────────────────────────────────
// 4. CLEAN ARCHITECTURE — USECASES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * UseCases (Interactors) encapsulate a single piece of business logic.
 *
 * Benefits:
 *   ✅ ViewModels stay thin (delegate to UseCases)
 *   ✅ Reusable across ViewModels
 *   ✅ Easy to unit test (pure Kotlin, no Android deps)
 *   ✅ Clear API boundary between UI and Data layers
 *
 * Convention:
 *   - One public method (invoke / execute)
 *   - Use operator fun invoke() for clean call syntax
 *   - Naming: VerbNounUseCase (GetArticlesUseCase, BookmarkArticleUseCase)
 */

class GetArticlesUseCase(
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        filter: ArticleFilter,
        forceRefresh: Boolean = false
    ): Result<List<Article>> {
        return try {
            val userId = userRepository.getCurrentUserId()
            val articles = if (forceRefresh) {
                articleRepository.refreshArticles(userId)
            } else {
                articleRepository.getArticles(userId)
            }

            val filtered = when (filter) {
                ArticleFilter.ALL -> articles
                ArticleFilter.BOOKMARKED -> articles.filter { it.isBookmarked }
                ArticleFilter.RECENT -> articles.sortedByDescending { it.publishedAt }.take(20)
            }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// UseCase returning a Flow (for observing data changes)
class ObserveArticlesUseCase(
    private val articleRepository: ArticleRepository
) {
    operator fun invoke(filter: ArticleFilter): Flow<List<Article>> {
        return articleRepository.observeArticles()
            .map { articles ->
                when (filter) {
                    ArticleFilter.ALL -> articles
                    ArticleFilter.BOOKMARKED -> articles.filter { it.isBookmarked }
                    ArticleFilter.RECENT -> articles.sortedByDescending { it.publishedAt }.take(20)
                }
            }
            .catch { emit(emptyList()) }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 5. REPOSITORY PATTERN — SINGLE SOURCE OF TRUTH
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Repository sits between Domain and Data layers.
 *
 * Responsibilities:
 *   - Abstract data sources (network, local DB, cache)
 *   - Implement caching strategy (offline-first, cache-then-network)
 *   - Single source of truth (usually local DB)
 *   - Expose data as Flows/suspend functions
 *
 * 💡 Interview Tip: Google strongly favors offline-first architecture.
 *    Room is the SSOT. Network is a sync mechanism, not a data source.
 */

class ArticleRepositoryImpl(
    private val articleDao: ArticleDao,
    private val articleApi: ArticleApi,
    private val dispatchers: DispatcherProvider
) : ArticleRepository {

    // Offline-first: observe local DB as the source of truth
    override fun observeArticles(): Flow<List<Article>> {
        return articleDao.observeAllArticles()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun getArticles(userId: String): List<Article> {
        return withContext(dispatchers.io) {
            // Return cached data first
            val cached = articleDao.getAllArticles()
            if (cached.isNotEmpty() && !isStale(cached)) {
                return@withContext cached.map { it.toDomain() }
            }
            // If stale or empty, fetch from network and cache
            refreshArticles(userId)
        }
    }

    override suspend fun refreshArticles(userId: String): List<Article> {
        return withContext(dispatchers.io) {
            val response = articleApi.getArticles(userId)
            val entities = response.map { it.toEntity() }
            // Write to DB — observers will automatically emit
            articleDao.upsertAll(entities)
            entities.map { it.toDomain() }
        }
    }

    private fun isStale(articles: List<ArticleEntity>): Boolean {
        val latestUpdate = articles.maxOfOrNull { it.lastFetched } ?: 0L
        val fifteenMinutes = 15 * 60 * 1000L
        return System.currentTimeMillis() - latestUpdate > fifteenMinutes
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 6. VIEWMODEL DEEP DIVE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel Key Facts:
 *
 *   - Scoped to a ViewModelStoreOwner (Activity, Fragment, NavBackStackEntry)
 *   - Survives configuration changes (rotation, locale)
 *   - ❌ Does NOT survive process death (use SavedStateHandle)
 *   - Has viewModelScope (auto-cancelled in onCleared())
 *   - Should NOT hold references to Views, Activities, or Contexts
 *
 * ❓ Interview Question: How does ViewModel survive configuration changes?
 *
 *   Answer: ViewModel is stored in ViewModelStore, which is retained via
 *   NonConfigurationInstance mechanism. When Activity is destroyed for config
 *   change, the ViewModelStore is passed to the new Activity instance.
 *
 * ❓ Follow-up: How is ViewModel shared between Fragments?
 *
 *   Answer: Scope ViewModel to the Activity:
 *     val sharedVM: SharedViewModel by activityViewModels()
 *   Or scope to a NavGraph:
 *     val sharedVM: SharedViewModel by navGraphViewModels(R.id.checkout_graph)
 *
 * ⚠️ GOTCHA: ViewModel scoped to Fragment is destroyed when Fragment is destroyed.
 *    ViewModel scoped to Activity lives as long as the Activity.
 */

// Sharing ViewModel between Fragments via Navigation Graph scope
class StepOneFragment : Fragment() {
    // Scoped to the checkout nav graph — shared with StepTwoFragment
    private val checkoutVM: CheckoutViewModel by navGraphViewModels(R.id.checkout_graph)
}

class StepTwoFragment : Fragment() {
    // Same ViewModel instance as StepOneFragment
    private val checkoutVM: CheckoutViewModel by navGraphViewModels(R.id.checkout_graph)
}


// ─────────────────────────────────────────────────────────────────────────────
// 7. MVVM vs MVI — DECISION FRAMEWORK
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ┌────────────────────┬────────────────────────┬─────────────────────────┐
 * │ Criteria           │ MVVM                   │ MVI                     │
 * ├────────────────────┼────────────────────────┼─────────────────────────┤
 * │ State management   │ Multiple StateFlows    │ Single immutable state  │
 * │ Complexity         │ Lower boilerplate      │ More boilerplate        │
 * │ Debugging          │ Harder (multiple       │ Easier (single state,   │
 * │                    │ state sources)         │ time-travel possible)   │
 * │ Best for           │ CRUD, forms, simple    │ Payments, checkout,     │
 * │                    │ list/detail            │ complex state machines  │
 * │ Testing            │ Test each flow         │ Test reducer function   │
 * │ Side effects       │ In ViewModel methods   │ Explicit Effect channel │
 * │ Scalability        │ Can get messy at scale │ Scales well             │
 * └────────────────────┴────────────────────────┴─────────────────────────┘
 *
 * 💡 Google SDE-2 Answer:
 *   "I use MVVM for most screens because it's simpler and Google-recommended.
 *    I switch to MVI for screens with complex state transitions — payments,
 *    multi-step wizards, or anything where I need an audit trail of state
 *    changes. The key is choosing based on the screen's complexity, not
 *    applying one pattern everywhere."
 */


// ─────────────────────────────────────────────────────────────────────────────
// 8. NAVIGATION COMPONENT
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Jetpack Navigation:
 *   - NavHost — container for destinations
 *   - NavController — manages backstack and navigation
 *   - NavGraph — defines routes/destinations
 *   - Safe Args — type-safe argument passing
 *
 * Key features:
 *   - Deep linking support
 *   - ViewModel scoping to NavGraph
 *   - Animation transitions
 *   - Conditional navigation (auth flows)
 */

// Compose Navigation example
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "article_list") {
        composable("article_list") {
            ArticleListScreen(
                onArticleClick = { id ->
                    navController.navigate("article_detail/$id")
                }
            )
        }
        composable(
            route = "article_detail/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
            ArticleDetailScreen(articleId = articleId)
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 9. MULTI-SOURCE DATA MERGING
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Real production apps rarely show data from a single source.
 * A typical screen might combine:
 *   - Database (Room) for cached/persisted data
 *   - Network (Retrofit) for fresh data
 *   - In-memory state (user input, filters, sort order)
 *
 * Kotlin's combine() operator merges multiple Flows into a single stream.
 * The Repository owns the data merging; the ViewModel owns the presentation logic.
 *
 * 💡 Interview Tip: Google loves asking "How would you build a search screen
 *    with filters, sorting, and offline support?" — this pattern is the answer.
 *
 * Key tools:
 *   combine()        → merge N flows; emits whenever ANY input changes
 *   flatMapLatest {} → switch to a new flow when upstream emits (search query)
 *   debounce()       → delay emission until user stops typing
 *   distinctUntilChanged() → skip duplicate emissions
 */

// ── UI State for a search screen with filters ──
data class SearchScreenUiState(
    val query: String = "",
    val filter: SearchFilter = SearchFilter.ALL,
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val results: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class SearchFilter { ALL, BOOKMARKED, UNREAD }
enum class SortOrder { NEWEST, OLDEST, RELEVANCE }

// ── Repository: merging DB + network ──
class SearchRepository(
    private val articleDao: ArticleDao,
    private val articleApi: ArticleApi,
    private val dispatchers: DispatcherProvider
) {
    // Observe local DB as SSOT, but trigger network refresh in background
    fun observeSearchResults(query: String, filter: SearchFilter): Flow<List<Article>> {
        return articleDao.observeSearch("%$query%")
            .map { entities -> entities.map { it.toDomain() } }
            .map { articles ->
                when (filter) {
                    SearchFilter.ALL -> articles
                    SearchFilter.BOOKMARKED -> articles.filter { it.isBookmarked }
                    SearchFilter.UNREAD -> articles.filter { !it.isRead }
                }
            }
            .onStart {
                // Trigger network sync in background (fire-and-forget)
                withContext(dispatchers.io) {
                    try {
                        val remote = articleApi.search(query)
                        articleDao.upsertAll(remote.map { it.toEntity() })
                    } catch (_: Exception) { /* offline is OK — DB is SSOT */ }
                }
            }
            .flowOn(dispatchers.io)
    }
}

// ── ViewModel: combine() to merge user input + data ──
class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Each user-controllable dimension is its own StateFlow
    private val queryFlow: MutableStateFlow<String> =
        MutableStateFlow(savedStateHandle["query"] ?: "")

    private val filterFlow: MutableStateFlow<SearchFilter> =
        MutableStateFlow(savedStateHandle["filter"] ?: SearchFilter.ALL)

    private val sortOrderFlow: MutableStateFlow<SortOrder> =
        MutableStateFlow(savedStateHandle["sort"] ?: SortOrder.NEWEST)

    val uiState: StateFlow<SearchScreenUiState> = combine(
        queryFlow
            .debounce(300)                // Wait 300ms after user stops typing
            .distinctUntilChanged(),       // Skip if query hasn't actually changed
        filterFlow,
        sortOrderFlow
    ) { query, filter, sortOrder ->
        Triple(query, filter, sortOrder)
    }
        .flatMapLatest { (query, filter, sortOrder) ->
            // Switch to a new data flow every time inputs change.
            // flatMapLatest cancels the previous collection automatically.
            searchRepository.observeSearchResults(query, filter)
                .map { articles ->
                    val sorted = when (sortOrder) {
                        SortOrder.NEWEST -> articles.sortedByDescending { it.publishedAt }
                        SortOrder.OLDEST -> articles.sortedBy { it.publishedAt }
                        SortOrder.RELEVANCE -> articles // API returns relevance-sorted
                    }
                    SearchScreenUiState(
                        query = query,
                        filter = filter,
                        sortOrder = sortOrder,
                        results = sorted,
                        isLoading = false
                    )
                }
                .onStart {
                    emit(SearchScreenUiState(
                        query = query, filter = filter,
                        sortOrder = sortOrder, isLoading = true
                    ))
                }
                .catch { e ->
                    emit(SearchScreenUiState(
                        query = query, filter = filter,
                        sortOrder = sortOrder, error = e.message
                    ))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchScreenUiState()
        )

    // ── User actions ──
    fun onQueryChanged(query: String) {
        savedStateHandle["query"] = query
        queryFlow.value = query
    }

    fun onFilterChanged(filter: SearchFilter) {
        savedStateHandle["filter"] = filter
        filterFlow.value = filter
    }

    fun onSortChanged(sortOrder: SortOrder) {
        savedStateHandle["sort"] = sortOrder
        sortOrderFlow.value = sortOrder
    }
}

/**
 * Why this pattern matters at Google scale:
 *
 *   ✅ Reactive — any input change automatically triggers a new result
 *   ✅ Debounced — avoids hammering the DB/network on every keystroke
 *   ✅ Offline-first — Room is SSOT; network sync is best-effort
 *   ✅ Cancellation-safe — flatMapLatest cancels stale queries
 *   ✅ Process-death safe — SavedStateHandle persists user inputs
 *   ✅ Lifecycle-aware — WhileSubscribed stops work when UI is gone
 *
 * ⚠️ GOTCHA: combine() emits when ANY input changes. If you have 5 inputs
 *    and set all 5 in a row, you get 5 emissions. Use debounce() or
 *    conflate() if intermediate states are expensive.
 *
 * ⚠️ GOTCHA: debounce() delays ALL emissions, including the first one.
 *    If you want instant first result + debounced subsequent queries,
 *    use a custom operator or transformLatest with a conditional delay.
 */


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: Your ViewModel has grown to 500+ lines. How do you refactor?
 *    A: Extract business logic into UseCases. Each UseCase = one operation.
 *       ViewModel becomes an orchestrator that delegates to UseCases.
 *
 * ❓ Q2: How do you handle one-time events (show toast, navigate)?
 *    A: Use Channel + receiveAsFlow() in ViewModel. SharedFlow with replay=0
 *       also works but Channel guarantees delivery. Never use StateFlow for
 *       events (re-delivered on recomposition/collection restart).
 *
 * ❓ Q3: How do you share data between unrelated screens?
 *    A: Options (best → worst):
 *       1. Scope ViewModel to shared NavGraph
 *       2. Use Repository as SSOT — both VMs observe the same data source
 *       3. Navigation arguments for small, serializable data
 *       ❌ Static/global state, EventBus
 *
 * ❓ Q4: What's the difference between StateFlow and LiveData?
 *    A:
 *       - StateFlow requires an initial value; LiveData doesn't
 *       - StateFlow is Kotlin-native; LiveData is Android-specific
 *       - StateFlow doesn't auto-stop on lifecycle; needs repeatOnLifecycle
 *       - StateFlow uses equality check to skip duplicates; LiveData always notifies
 *       - StateFlow works in domain/data layers (no Android dependency)
 *
 * ❓ Q5: How does unidirectional data flow prevent bugs?
 *    A: State only changes in one place (ViewModel/Reducer). UI can't directly
 *       mutate state, so you can't have inconsistent state or race conditions.
 *       Every state change is traceable and testable.
 *
 * ❓ Q6: You have a search screen that combines a search query, filters, and
 *        Room data. The user types fast and the UI lags. How do you fix it?
 *    A: Three-pronged approach:
 *       1. debounce(300) on the query Flow — don't react to every keystroke
 *       2. flatMapLatest on the combined inputs — cancel stale queries
 *       3. Room as SSOT with network sync in onStart — no blocking network call
 *       The ViewModel uses combine(query, filter, sort).flatMapLatest { ... }
 *       to build a single reactive pipeline. WhileSubscribed(5000) in stateIn()
 *       keeps the pipeline alive briefly during config changes but stops work
 *       when the user truly leaves the screen.
 */
