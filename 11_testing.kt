/**
 * ============================================================================
 * 11 — TESTING
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Testing Pyramid & Strategy
 *   2. Unit Testing (JUnit, MockK, Turbine)
 *   3. Testing ViewModels
 *   4. Testing Repositories & UseCases
 *   5. Testing Coroutines & Flows
 *   6. UI Testing — Espresso (View System)
 *   7. UI Testing — Compose Test
 *   8. Fakes vs Mocks
 *   9. Robolectric
 *  10. Room Testing
 *  11. MockWebServer
 *  12. Screenshot / Visual Regression Testing
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. TESTING PYRAMID & STRATEGY
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Testing Pyramid (bottom to top):
 *
 *        /  E2E Tests  \          ← Slow, flaky, few
 *       / Integration   \         ← Medium speed, moderate
 *      / Unit Tests      \        ← Fast, reliable, many
 *     /___________________\
 *
 * Google's recommendation for Android:
 *   70% Unit Tests      → ViewModel, Repository, UseCase, utils
 *   20% Integration     → Screen tests, Navigation, Room + DAO
 *   10% E2E Tests       → Critical user flows
 *
 * Key principles:
 *   - Test behavior, not implementation
 *   - Tests should be fast, reliable, and independent
 *   - One assertion per test (when practical)
 *   - Test names describe the scenario and expected outcome
 *   - Use Given-When-Then or Arrange-Act-Assert pattern
 */


// ─────────────────────────────────────────────────────────────────────────────
// 2. UNIT TESTING FUNDAMENTALS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Tools:
 *   JUnit 4/5  → test framework
 *   MockK      → mocking library (Kotlin-friendly)
 *   Truth      → assertion library (Google)
 *   Turbine    → Flow testing library
 *   kotlinx-coroutines-test → coroutine testing utilities
 */

// Basic unit test structure
class ArticleMapperTest {

    @Test
    fun `toEntity maps all fields correctly`() {
        // Arrange
        val response = ArticleResponse(
            id = "123",
            title = "Kotlin Coroutines",
            content = "Deep dive into coroutines",
            authorName = "John",
            publishedAt = 1700000000000,
            tags = listOf("kotlin", "android")
        )

        // Act
        val entity = response.toEntity()

        // Assert
        assertEquals("123", entity.id)
        assertEquals("Kotlin Coroutines", entity.title)
        assertEquals("John", entity.authorName)
        assertEquals(2, entity.tags.size)
    }

    @Test
    fun `toEntity uses default values for nullable fields`() {
        val response = ArticleResponse(
            id = "123",
            title = "Title",
            content = "Content",
            authorName = "Author",
            publishedAt = 0,
            tags = emptyList(),
            readTimeMinutes = null
        )

        val entity = response.toEntity()

        assertEquals(0, entity.readTimeMinutes)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 3. TESTING VIEWMODELS
// ─────────────────────────────────────────────────────────────────────────────

class ArticleViewModelTest {

    // Replace Main dispatcher with test dispatcher
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ArticleViewModel
    private val fakeRepository = FakeArticleRepository()
    private val savedStateHandle = SavedStateHandle()

    @Before
    fun setup() {
        viewModel = ArticleViewModel(
            getArticlesUseCase = GetArticlesUseCase(fakeRepository),
            savedStateHandle = savedStateHandle
        )
    }

    @Test
    fun `initial state is loading`() {
        val state = viewModel.uiState.value
        assertTrue(state.isLoading)
        assertTrue(state.articles.isEmpty())
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadArticles success updates state with articles`() = runTest {
        // Arrange
        val articles = listOf(
            Article(id = "1", title = "Article 1"),
            Article(id = "2", title = "Article 2")
        )
        fakeRepository.setArticles(articles)

        // Act
        viewModel.loadArticles()
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.articles.size)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadArticles failure updates state with error`() = runTest {
        fakeRepository.setShouldFail(true)

        viewModel.loadArticles()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertTrue(state.articles.isEmpty())
    }

    @Test
    fun `search saves query in savedStateHandle`() = runTest {
        viewModel.search("kotlin")
        advanceUntilIdle()

        assertEquals("kotlin", savedStateHandle.get<String>("query"))
    }

    // Testing with Turbine (Flow testing)
    @Test
    fun `uiState emits loading then success`() = runTest {
        val articles = listOf(Article(id = "1", title = "Test"))
        fakeRepository.setArticles(articles)
        fakeRepository.setDelay(100)

        viewModel.uiState.test {
            // Initial state
            val initial = awaitItem()
            assertTrue(initial.isLoading || initial.articles.isEmpty())

            viewModel.loadArticles()

            // May see loading state
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            // Then success
            val success = awaitItem()
            assertFalse(success.isLoading)
            assertEquals(1, success.articles.size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 4. TESTING REPOSITORIES & USECASES
// ─────────────────────────────────────────────────────────────────────────────

class ArticleRepositoryTest {

    private val fakeApi = FakeArticleApi()
    private val fakeDao = FakeArticleDao()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: ArticleRepositoryImpl

    @Before
    fun setup() {
        repository = ArticleRepositoryImpl(
            api = fakeApi,
            dao = fakeDao,
            dispatcher = testDispatcher
        )
    }

    @Test
    fun `getArticles returns cached data when fresh`() = runTest(testDispatcher) {
        // Arrange - populate cache
        val cached = listOf(ArticleEntity(id = "1", title = "Cached", lastFetched = System.currentTimeMillis()))
        fakeDao.insertAll(cached)

        // Act
        val result = repository.getArticles("user1")

        // Assert - should NOT call API
        assertEquals(1, result.size)
        assertEquals(0, fakeApi.callCount)
    }

    @Test
    fun `getArticles fetches from API when cache is stale`() = runTest(testDispatcher) {
        // Arrange - stale cache
        val stale = listOf(ArticleEntity(id = "1", title = "Old", lastFetched = 0))
        fakeDao.insertAll(stale)
        fakeApi.setArticles(listOf(ArticleResponse(id = "2", title = "Fresh")))

        // Act
        val result = repository.getArticles("user1")

        // Assert - should call API and update cache
        assertEquals("Fresh", result.first().title)
        assertEquals(1, fakeApi.callCount)
    }

    @Test
    fun `observeArticles emits when database changes`() = runTest(testDispatcher) {
        repository.observeArticles().test {
            // Initially empty
            assertEquals(emptyList<Article>(), awaitItem())

            // Add data
            fakeDao.insertAll(listOf(ArticleEntity(id = "1", title = "New")))

            // Should emit new list
            val updated = awaitItem()
            assertEquals(1, updated.size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 5. TESTING COROUTINES & FLOWS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Key testing utilities:
 *
 *   runTest { }              → runs test with virtual time (no real delays)
 *   advanceUntilIdle()       → advance all pending coroutines
 *   advanceTimeBy(ms)        → advance virtual clock by specific amount
 *   StandardTestDispatcher() → manual advancement (deterministic)
 *   UnconfinedTestDispatcher → immediate execution (less control)
 *   Turbine (.test { })      → structured Flow testing with assertions
 */

class FlowTestExample {

    @Test
    fun `debounced search flow emits correct results`() = runTest {
        val searchFlow = MutableSharedFlow<String>()
        val resultFlow = searchFlow
            .debounce(300)
            .filter { it.length >= 2 }
            .distinctUntilChanged()
            .map { query -> "Results for: $query" }

        resultFlow.test {
            searchFlow.emit("k")       // Too short, filtered
            searchFlow.emit("ko")      // Will be debounced
            searchFlow.emit("kot")     // Replaces "ko" during debounce

            advanceTimeBy(301)

            assertEquals("Results for: kot", awaitItem())

            searchFlow.emit("kot")     // Same value, distinctUntilChanged skips
            advanceTimeBy(301)
            expectNoEvents()           // No emission

            searchFlow.emit("kotlin")
            advanceTimeBy(301)
            assertEquals("Results for: kotlin", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}

// MainDispatcherRule for ViewModel tests
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
// 6. UI TESTING — ESPRESSO
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Espresso = UI testing framework for the View system.
 *
 * Core API:
 *   onView(matcher)          → find a view
 *   .perform(action)         → interact with it
 *   .check(assertion)        → verify its state
 */

@RunWith(AndroidJUnit4::class)
class ArticleListFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun displaysArticleList() {
        // Wait for list to load
        onView(withId(R.id.recycler_view))
            .check(matches(isDisplayed()))

        // Check first item
        onView(withText("Kotlin Coroutines"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickArticleNavigatesToDetail() {
        onView(withText("Kotlin Coroutines"))
            .perform(click())

        // Verify navigation
        onView(withId(R.id.article_detail_container))
            .check(matches(isDisplayed()))
    }

    @Test
    fun swipeToRefreshUpdatesArticles() {
        onView(withId(R.id.swipe_refresh))
            .perform(swipeDown())

        // Verify new data appears
        onView(withText("New Article"))
            .check(matches(isDisplayed()))
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 7. UI TESTING — COMPOSE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Compose Testing API:
 *   composeTestRule.onNodeWithText("text")   → find by text
 *   .onNodeWithTag("tag")                    → find by test tag
 *   .onNodeWithContentDescription("desc")    → find by accessibility
 *   .performClick()                          → click action
 *   .performTextInput("text")                → type text
 *   .assertIsDisplayed()                     → visibility check
 *   .assertTextEquals("text")                → text content check
 */

class ArticleListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `displays loading indicator initially`() {
        composeTestRule.setContent {
            ArticleListContent(
                uiState = ArticleListUiState(isLoading = true),
                onFilterChanged = {},
                onRefresh = {},
                onBookmarkClicked = {},
                onErrorDismissed = {}
            )
        }

        composeTestRule
            .onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
    }

    @Test
    fun `displays articles when loaded`() {
        val articles = listOf(
            Article(id = "1", title = "Kotlin Coroutines"),
            Article(id = "2", title = "Jetpack Compose")
        )

        composeTestRule.setContent {
            ArticleListContent(
                uiState = ArticleListUiState(articles = articles),
                onFilterChanged = {},
                onRefresh = {},
                onBookmarkClicked = {},
                onErrorDismissed = {}
            )
        }

        composeTestRule.onNodeWithText("Kotlin Coroutines").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jetpack Compose").assertIsDisplayed()
    }

    @Test
    fun `bookmark click calls callback with article id`() {
        var clickedId: String? = null
        val articles = listOf(Article(id = "1", title = "Test Article"))

        composeTestRule.setContent {
            ArticleListContent(
                uiState = ArticleListUiState(articles = articles),
                onFilterChanged = {},
                onRefresh = {},
                onBookmarkClicked = { clickedId = it },
                onErrorDismissed = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Bookmark Test Article")
            .performClick()

        assertEquals("1", clickedId)
    }

    @Test
    fun `error snackbar appears and can be dismissed`() {
        composeTestRule.setContent {
            ArticleListContent(
                uiState = ArticleListUiState(errorMessage = "Network error"),
                onFilterChanged = {},
                onRefresh = {},
                onBookmarkClicked = {},
                onErrorDismissed = {}
            )
        }

        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 8. FAKES VS MOCKS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Google prefers FAKES over MOCKS.
 *
 * Fake:
 *   - Working implementation with shortcuts (in-memory DB, static data)
 *   - Tests behavior, not implementation details
 *   - Reusable across tests
 *   - More resilient to refactoring
 *
 * Mock:
 *   - Verifies method calls and arguments
 *   - Tightly coupled to implementation
 *   - Brittle — breaks when implementation changes
 *   - Good for verifying interactions (e.g., analytics calls)
 *
 * 💡 Interview Tip: "I prefer fakes for data-providing dependencies
 *    (repositories, APIs, DAOs) and mocks for verifying interactions
 *    (analytics, logging, notifications)."
 */

// Fake Repository
class FakeArticleRepository : ArticleRepository {
    private val articles = mutableListOf<Article>()
    private val flow = MutableStateFlow<List<Article>>(emptyList())
    private var shouldFail = false
    private var delayMs = 0L

    fun setArticles(list: List<Article>) {
        articles.clear()
        articles.addAll(list)
        flow.value = list
    }

    fun setShouldFail(fail: Boolean) { shouldFail = fail }
    fun setDelay(ms: Long) { delayMs = ms }

    override fun observeArticles(): Flow<List<Article>> = flow

    override suspend fun getArticles(userId: String): List<Article> {
        if (delayMs > 0) delay(delayMs)
        if (shouldFail) throw IOException("Fake error")
        return articles.toList()
    }

    override suspend fun refreshArticles(userId: String): List<Article> {
        if (shouldFail) throw IOException("Fake error")
        return articles.toList()
    }
}

// Using MockK for interaction verification
class AnalyticsTest {
    private val analytics = mockk<AnalyticsHelper>(relaxed = true)

    @Test
    fun `screen view logged on navigation`() {
        val tracker = ScreenTracker(analytics)

        tracker.onScreenViewed("article_detail")

        verify { analytics.logEvent("screen_view", mapOf("screen" to "article_detail")) }
    }

    @Test
    fun `analytics not called for excluded screens`() {
        val tracker = ScreenTracker(analytics)

        tracker.onScreenViewed("splash")

        verify(exactly = 0) { analytics.logEvent(any(), any()) }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 9. ROBOLECTRIC
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Robolectric lets you run tests that depend on Android framework classes
 * on the JVM — no emulator or device needed.
 *
 * How it works:
 *   - Provides "shadow" classes that simulate Android framework behavior
 *   - Intercepts calls to android.* classes and routes them to shadows
 *   - Runs as a regular JUnit test (in test/ not androidTest/)
 *
 * When to use Robolectric vs instrumented tests:
 *   Robolectric   → fast feedback loop, CI-friendly, Activity/Fragment lifecycle tests,
 *                    testing code that uses Context, SharedPreferences, Resources
 *   Instrumented  → real device behavior, hardware-dependent features, Espresso UI tests,
 *                    Room with actual SQLite, accurate rendering tests
 *
 * Shadow classes:
 *   - ShadowApplication, ShadowActivity, ShadowLooper, ShadowAlarmManager, etc.
 *   - You can create custom shadows for your own classes
 *   - Access via Shadows.shadowOf(instance) to inspect internal state
 *
 * Limitations & gotchas:
 *   ⚠️ Rendering is not pixel-accurate (no real GPU)
 *   ⚠️ Some native APIs are not fully simulated (camera, Bluetooth)
 *   ⚠️ Shadow behavior may differ from real Android in edge cases
 *   ⚠️ Slower startup than pure unit tests (class loading overhead)
 *   ⚠️ Needs @Config(sdk = [X]) to pin SDK version for consistent behavior
 *
 * 💡 Interview Tip: "I use Robolectric when I need Android framework classes
 *    but want JVM speed. For anything that depends on real device behavior —
 *    especially UI rendering and SQLite — I use instrumented tests."
 */

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationSchedulerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `scheduleReminder sets alarm via AlarmManager`() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = Shadows.shadowOf(alarmManager)

        val scheduler = ReminderAlarmScheduler(context)
        val triggerTime = System.currentTimeMillis() + 60_000

        scheduler.scheduleReminder(1, triggerTime, "Test Reminder")

        val nextAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull(nextAlarm)
        assertEquals(AlarmManager.RTC_WAKEUP, nextAlarm.type)
    }

    @Test
    fun `activity launches with correct intent extras`() {
        val intent = Intent(context, ArticleDetailActivity::class.java).apply {
            putExtra("ARTICLE_ID", "123")
        }

        val scenario = ActivityScenario.launch<ArticleDetailActivity>(intent)
        scenario.onActivity { activity ->
            assertEquals("123", activity.intent.getStringExtra("ARTICLE_ID"))
        }
    }
}

@RunWith(RobolectricTestRunner::class)
class SharedPreferencesTest {

    @Test
    fun `saves and reads user preference`() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

        prefs.edit().putBoolean("dark_mode", true).apply()

        assertTrue(prefs.getBoolean("dark_mode", false))
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 10. ROOM TESTING
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Room testing strategy:
 *
 *   In-memory database  → fast, no disk I/O, auto-cleared between tests
 *   MigrationTestHelper → verifies schema migrations don't lose data
 *   DAO tests           → instrumented tests (real SQLite) for accuracy
 *
 * Why instrumented (not Robolectric)?
 *   Room uses real SQLite under the hood. Robolectric's SQLite shadow can
 *   differ in behavior (e.g., foreign key enforcement, WAL mode). Google
 *   recommends instrumented tests for Room.
 *
 * 💡 Interview Tip: "I always test Room with an in-memory database on a real
 *    device or emulator to ensure SQLite behavior matches production."
 */

@RunWith(AndroidJUnit4::class)
class ArticleDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ArticleDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()  // OK for tests only
            .build()
        dao = database.articleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveArticle() = runTest {
        val article = ArticleEntity(
            id = "1", title = "Testing Room", content = "Content",
            authorName = "Author", publishedAt = System.currentTimeMillis()
        )

        dao.insert(article)

        val result = dao.getArticleById("1")
        assertNotNull(result)
        assertEquals("Testing Room", result?.title)
    }

    @Test
    fun upsertReplacesExistingArticle() = runTest {
        val original = ArticleEntity(id = "1", title = "Original", content = "V1")
        val updated = ArticleEntity(id = "1", title = "Updated", content = "V2")

        dao.insert(original)
        dao.upsert(updated)

        val result = dao.getArticleById("1")
        assertEquals("Updated", result?.title)
        assertEquals("V2", result?.content)
    }

    @Test
    fun deleteRemovesArticle() = runTest {
        val article = ArticleEntity(id = "1", title = "To Delete", content = "Content")
        dao.insert(article)

        dao.deleteById("1")

        val result = dao.getArticleById("1")
        assertNull(result)
    }

    @Test
    fun observeArticlesEmitsOnChange() = runTest {
        dao.observeAll().test {
            assertEquals(emptyList<ArticleEntity>(), awaitItem())

            val article = ArticleEntity(id = "1", title = "New", content = "Content")
            dao.insert(article)

            val updated = awaitItem()
            assertEquals(1, updated.size)
            assertEquals("New", updated.first().title)

            cancelAndIgnoreRemainingEvents()
        }
    }
}

// Migration testing
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2() {
        // Create database at version 1
        helper.createDatabase("test_db", 1).apply {
            execSQL("INSERT INTO articles (id, title, content) VALUES ('1', 'Test', 'Content')")
            close()
        }

        // Run migration to version 2
        val db = helper.runMigrationsAndValidate("test_db", 2, true, MIGRATION_1_2)

        // Verify data survived migration and new column has default value
        val cursor = db.query("SELECT * FROM articles WHERE id = '1'")
        assertTrue(cursor.moveToFirst())
        assertEquals("Test", cursor.getString(cursor.getColumnIndex("title")))
        assertEquals(0, cursor.getInt(cursor.getColumnIndex("bookmark_count")))
        cursor.close()
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 11. MOCKWEBSERVER
// ─────────────────────────────────────────────────────────────────────────────

/**
 * OkHttp MockWebServer provides a local HTTP server for testing API interactions.
 *
 * Why use it:
 *   - Test real HTTP behavior (serialization, headers, error codes)
 *   - Verify request format (path, method, headers, body)
 *   - Simulate network conditions (slow responses, timeouts, errors)
 *   - No flaky network dependency in CI
 *
 * How it works:
 *   1. Start MockWebServer on localhost
 *   2. Point Retrofit at mockWebServer.url("/")
 *   3. Enqueue mock responses
 *   4. Execute API calls — they hit the local server
 *   5. Verify requests with takeRequest()
 *
 * 💡 Interview Tip: "I use MockWebServer to test the full networking stack —
 *    Retrofit, converters, interceptors — without real network calls.
 *    Fakes are great for repository tests, but MockWebServer catches
 *    serialization bugs that fakes miss."
 */

class ArticleApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: ArticleApi

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()

        api = retrofit.create(ArticleApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getArticles parses response correctly`() = runTest {
        val json = """
            [
                {"id": "1", "title": "Kotlin Coroutines", "content": "Deep dive"},
                {"id": "2", "title": "Jetpack Compose", "content": "Modern UI"}
            ]
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(json)
                .setHeader("Content-Type", "application/json")
        )

        val articles = api.getArticles("user1")

        assertEquals(2, articles.size)
        assertEquals("Kotlin Coroutines", articles[0].title)
    }

    @Test
    fun `getArticles sends correct request`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("[]").setResponseCode(200))

        api.getArticles("user1")

        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertTrue(request.path!!.contains("user1"))
    }

    @Test
    fun `getArticles throws on server error`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        try {
            api.getArticles("user1")
            fail("Expected HttpException")
        } catch (e: HttpException) {
            assertEquals(500, e.code())
        }
    }

    @Test
    fun `getArticles handles slow response`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody("[]")
                .setBodyDelay(2, TimeUnit.SECONDS)
        )

        val client = OkHttpClient.Builder()
            .readTimeout(1, TimeUnit.SECONDS)
            .build()

        val slowApi = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ArticleApi::class.java)

        try {
            slowApi.getArticles("user1")
            fail("Expected timeout")
        } catch (e: Exception) {
            assertTrue(e is java.net.SocketTimeoutException)
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 12. SCREENSHOT / VISUAL REGRESSION TESTING
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Screenshot testing catches unintended visual changes by comparing UI
 * snapshots against golden (reference) images.
 *
 * Tools:
 *
 * 1. Paparazzi (Cash App / Square):
 *    - Runs on JVM — no emulator needed
 *    - Renders Android views and Compose using layoutlib (the same renderer
 *      Android Studio uses for previews)
 *    - Records golden images, then verifies against them in CI
 *    - Commands: ./gradlew :app:recordPaparazziDebug (record)
 *               ./gradlew :app:verifyPaparazziDebug  (verify)
 *
 * 2. Compose Preview Screenshot Testing (Google, experimental):
 *    - Uses @Preview composables as screenshot test sources
 *    - Runs on JVM with layoutlib
 *    - Tightly integrated with Compose preview workflow
 *    - Commands: ./gradlew updateDebugScreenshotTest (record)
 *               ./gradlew validateDebugScreenshotTest (verify)
 *
 * When to use:
 *   ✅ Design system components (buttons, cards, inputs)
 *   ✅ Complex layouts with multiple states (loading, error, empty, populated)
 *   ✅ Dark mode / light mode verification
 *   ✅ Accessibility — large font / RTL layout checks
 *   ✅ Preventing accidental regressions in shared components
 *
 * CI integration:
 *   - Run verify task in CI pipeline on every PR
 *   - Record task generates/updates golden images (commit to repo)
 *   - Failed verification produces a diff image showing what changed
 *   - Review diffs in PR to decide if changes are intentional
 *
 * ⚠️ Gotchas:
 *   - Golden images are platform-sensitive (record + verify on same OS/JDK)
 *   - Dynamic content (timestamps, animations) must be pinned for determinism
 *   - Large golden image repos can bloat git — consider LFS
 *
 * 💡 Interview Tip: "I use Paparazzi for screenshot tests because it runs
 *    on JVM, making it fast and CI-friendly. It's especially valuable for
 *    design system components where visual regressions are costly."
 */

// Paparazzi test example
class ArticleCardScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_6,
        theme = "android:Theme.Material3.Light"
    )

    @Test
    fun articleCardDefault() {
        paparazzi.snapshot {
            ArticleCard(
                article = Article(
                    id = "1",
                    title = "Kotlin Coroutines Guide",
                    authorName = "Android Team",
                    readTimeMinutes = 8
                ),
                onBookmarkClick = {},
                onClick = {}
            )
        }
    }

    @Test
    fun articleCardBookmarked() {
        paparazzi.snapshot {
            ArticleCard(
                article = Article(
                    id = "1",
                    title = "Kotlin Coroutines Guide",
                    authorName = "Android Team",
                    readTimeMinutes = 8,
                    isBookmarked = true
                ),
                onBookmarkClick = {},
                onClick = {}
            )
        }
    }

    @Test
    fun articleCardLongTitle() {
        paparazzi.snapshot {
            ArticleCard(
                article = Article(
                    id = "1",
                    title = "An Extremely Long Article Title That Should Wrap Nicely Without Breaking the Layout",
                    authorName = "Android Team",
                    readTimeMinutes = 15
                ),
                onBookmarkClick = {},
                onClick = {}
            )
        }
    }
}

// Compose Preview Screenshot Testing example
/*
@Preview(showBackground = true)
@Composable
fun ArticleCardPreview() {
    AppTheme {
        ArticleCard(
            article = previewArticle,
            onBookmarkClick = {},
            onClick = {}
        )
    }
}

// The preview above doubles as a screenshot test source.
// Run: ./gradlew validateDebugScreenshotTest
*/


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: How do you test a ViewModel that uses viewModelScope?
 *    A: Use MainDispatcherRule to replace Dispatchers.Main with TestDispatcher.
 *       Use runTest {} for coroutine testing. Use advanceUntilIdle() to
 *       complete all pending coroutines. Use Turbine for Flow assertions.
 *
 * ❓ Q2: How do you test Room database operations?
 *    A: Use Room.inMemoryDatabaseBuilder() for fast, isolated tests.
 *       Run on Android device/emulator (instrumented test).
 *       Test: insert → query → verify. Test: migration paths.
 *
 * ❓ Q3: What makes a good test?
 *    A: Fast, isolated, repeatable, self-validating, timely (FIRST).
 *       Tests behavior not implementation. Clear name describes scenario.
 *       Single reason to fail. Arranges minimal context.
 *
 * ❓ Q4: How do you handle flaky tests?
 *    A: Identify the flakiness source: timing (use TestDispatcher, IdlingResource),
 *       shared state (isolate tests), network (use fakes).
 *       For Espresso: use IdlingResource for async operations.
 *       For Compose: use waitUntil {} for async assertions.
 *
 * ❓ Q5: When do you use instrumented tests vs local tests?
 *    A: Local (JVM): ViewModel, UseCase, Repository, mappers — fast feedback.
 *       Instrumented (device): Room DB, UI tests, integration tests.
 *       Rule of thumb: run as much as possible locally.
 *
 * ❓ Q6: When would you use Robolectric vs instrumented tests?
 *    A: Robolectric: when you need Android framework classes (Context,
 *       SharedPreferences, Activity lifecycle) but want JVM speed. Great for
 *       CI pipelines — no emulator needed. Instrumented: when behavior must
 *       match real Android exactly — Room/SQLite, hardware features, pixel-accurate
 *       UI rendering, multi-process scenarios. Robolectric's shadows approximate
 *       real behavior but can diverge in edge cases, so anything SQLite or
 *       rendering-sensitive should run instrumented.
 *
 * ❓ Q7: How do you use MockWebServer to test API interactions?
 *    A: Start MockWebServer, point Retrofit's baseUrl at mockWebServer.url("/").
 *       Enqueue MockResponse objects with status codes, JSON bodies, and headers.
 *       Execute API calls — they hit the local server. Use takeRequest() to
 *       verify the request method, path, headers, and body. Simulate errors
 *       with setResponseCode(500), slow responses with setBodyDelay(),
 *       and timeouts by combining delay with a short client timeout. This tests
 *       the full networking stack including serialization and interceptors.
 */
