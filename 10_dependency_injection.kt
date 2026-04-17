/**
 * ============================================================================
 * 10 — DEPENDENCY INJECTION
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Why DI Matters
 *   2. Hilt — Complete Setup & Usage
 *   3. Hilt Scoping & Components
 *   4. Hilt + ViewModel, WorkManager, Compose
 *   5. Dagger 2 Fundamentals
 *   6. Manual DI (without framework)
 *   7. Testing with DI
 *   8. @EntryPoint — Accessing Hilt from Non-Hilt Classes
 *   9. Multi-Module DI
 *  10. WorkManager + Hilt Configuration
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. WHY DI MATTERS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dependency Injection = providing an object's dependencies from the outside
 * rather than the object creating them itself.
 *
 * Benefits:
 *   ✅ Testability — swap real implementations with fakes/mocks
 *   ✅ Loose coupling — classes don't know about concrete implementations
 *   ✅ Reusability — same class works with different dependencies
 *   ✅ Scoping — control object lifetime (singleton, per-screen, etc.)
 *   ✅ Separation of concerns — class focuses on its job, not creation of deps
 *
 * Without DI:
 */

// ❌ Tight coupling — hard to test, hard to change
class BadRepository {
    private val api = RetrofitClient.create(ArticleApi::class.java)  // Hardcoded
    private val db = Room.databaseBuilder(/*...*/).build()           // Hardcoded
}

// ✅ Dependencies injected — testable, flexible
class GoodRepository(
    private val api: ArticleApi,         // Interface — can be faked
    private val dao: ArticleDao,         // Interface — can be faked
    private val dispatcher: CoroutineDispatcher  // Can use TestDispatcher
)


// ─────────────────────────────────────────────────────────────────────────────
// 2. HILT — COMPLETE SETUP
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Hilt = Google's recommended DI framework for Android.
 * Built on top of Dagger 2. Reduces boilerplate significantly.
 *
 * Setup requires:
 *   1. @HiltAndroidApp on Application class
 *   2. @AndroidEntryPoint on Activities, Fragments, Services
 *   3. @Inject on constructors (for classes) or @Module + @Provides (for 3rd party)
 *   4. @HiltViewModel on ViewModels
 */

// Step 1: Application
@HiltAndroidApp
class MyApplication : Application()

// Step 2: Module for providing dependencies
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor  // Hilt injects this automatically
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideArticleApi(retrofit: Retrofit): ArticleApi {
        return retrofit.create(ArticleApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context, AppDatabase::class.java, "app_database"
        ).build()
    }

    @Provides
    fun provideArticleDao(database: AppDatabase): ArticleDao {
        return database.articleDao()
    }
}

// Binding interfaces to implementations
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindArticleRepository(
        impl: ArticleRepositoryImpl
    ): ArticleRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}

// Constructor injection for your own classes
class ArticleRepositoryImpl @Inject constructor(
    private val api: ArticleApi,
    private val dao: ArticleDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ArticleRepository {
    // Hilt auto-provides api, dao, and ioDispatcher
}


// ─────────────────────────────────────────────────────────────────────────────
// 3. HILT SCOPING & COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Hilt Component Hierarchy:
 *
 *   SingletonComponent      → Application lifetime
 *       ↓
 *   ActivityRetainedComponent → ViewModel lifetime (survives config change)
 *       ↓
 *   ViewModelComponent      → ViewModel instance lifetime
 *       ↓
 *   ActivityComponent       → Activity lifetime
 *       ↓
 *   FragmentComponent       → Fragment lifetime
 *       ↓
 *   ViewComponent           → View lifetime
 *       ↓
 *   ViewWithFragmentComponent → View in Fragment lifetime
 *
 * Also: ServiceComponent (parallel to ActivityComponent)
 *
 * Scope annotations:
 *   @Singleton            → one instance for entire app
 *   @ActivityRetainedScoped → one per Activity (survives config change)
 *   @ViewModelScoped      → one per ViewModel instance
 *   @ActivityScoped       → one per Activity instance
 *   @FragmentScoped       → one per Fragment instance
 *
 * ⚠️ Default (no scope): new instance every time it's injected.
 *    This is often what you want for lightweight classes.
 */

// Qualifier annotations for providing different instances of same type
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}


// ─────────────────────────────────────────────────────────────────────────────
// 4. HILT + VIEWMODEL, WORKMANAGER, COMPOSE
// ─────────────────────────────────────────────────────────────────────────────

// ViewModel with Hilt
@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val getArticlesUseCase: GetArticlesUseCase,
    private val savedStateHandle: SavedStateHandle  // Auto-provided by Hilt
) : ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow("query", "")

    private val _uiState = MutableStateFlow(ArticleUiState())
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    fun search(query: String) {
        savedStateHandle["query"] = query
        viewModelScope.launch {
            val result = getArticlesUseCase(query)
            _uiState.value = ArticleUiState(articles = result)
        }
    }
}

// Activity with Hilt
@AndroidEntryPoint
class ArticleActivity : AppCompatActivity() {
    private val viewModel: ArticleViewModel by viewModels()
}

// Compose with Hilt
@Composable
fun ArticleScreen(
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}

// WorkManager with Hilt
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: ArticleRepository  // Injected by Hilt
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.sync()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 5. DAGGER 2 FUNDAMENTALS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Hilt is built on Dagger 2. Knowing Dagger helps understand Hilt internals.
 *
 * Key Dagger concepts:
 *
 *   @Component → interface that connects @Module providers with @Inject consumers
 *   @Module    → class that provides dependencies
 *   @Provides  → method in Module that creates an instance
 *   @Binds     → abstract method that binds an interface to implementation
 *   @Inject    → marks constructor or field for injection
 *   @Scope     → controls instance lifetime
 *   @Subcomponent → child component that inherits from parent
 *
 * Dagger generates code at compile time (no reflection = fast).
 *
 * Hilt eliminates the need to:
 *   - Create Component interfaces manually
 *   - Write Component.Factory or Component.Builder
 *   - Manage component hierarchy
 *   - Inject into Activities/Fragments manually
 *
 * ❓ Interview Question: What's the difference between @Provides and @Binds?
 *    @Provides: creates instances, can have logic, returns concrete type
 *    @Binds: maps interface to implementation, no instance creation, must be abstract
 *    @Binds is more efficient (generates less code)
 */


// ─────────────────────────────────────────────────────────────────────────────
// 6. MANUAL DI (WITHOUT FRAMEWORK)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * For small apps or when asked "how would you do DI without Hilt/Dagger?"
 *
 * Pattern: Service Locator or Manual Constructor Injection
 */

// Manual DI container
class AppContainer(private val context: Context) {

    // Singletons
    private val json: Json by lazy {
        Json { ignoreUnknownKeys = true }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.example.com/v1/")
            .client(okHttpClient)
            .build()
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "app_db").build()
    }

    // Provide dependencies
    val articleApi: ArticleApi by lazy { retrofit.create(ArticleApi::class.java) }
    val articleDao: ArticleDao by lazy { database.articleDao() }

    val articleRepository: ArticleRepository by lazy {
        ArticleRepositoryImpl(articleApi, articleDao, Dispatchers.IO)
    }

    val getArticlesUseCase: GetArticlesUseCase by lazy {
        GetArticlesUseCase(articleRepository)
    }
}

// Usage in Application class
class ManualDIApplication : Application() {
    val container by lazy { AppContainer(this) }
}


// ─────────────────────────────────────────────────────────────────────────────
// 7. TESTING WITH DI
// ─────────────────────────────────────────────────────────────────────────────

/**
 * DI makes testing straightforward by allowing dependency replacement.
 */

// Hilt testing — replace modules
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ArticleViewModelTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue  // Replaces the real repository with a fake
    val repository: ArticleRepository = FakeArticleRepository()

    @Inject
    lateinit var viewModel: ArticleViewModel

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `search returns filtered articles`() = runTest {
        (repository as FakeArticleRepository).setArticles(testArticles)

        viewModel.search("kotlin")

        val state = viewModel.uiState.value
        assertTrue(state.articles.all { "kotlin" in it.title.lowercase() })
    }
}

// Without Hilt — manual DI makes testing just as easy
class ArticleViewModelUnitTest {

    private val fakeRepository = FakeArticleRepository()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: ArticleViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ArticleViewModel(
            getArticlesUseCase = GetArticlesUseCase(fakeRepository),
            savedStateHandle = SavedStateHandle()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 8. @ENTRYPOINT — ACCESSING HILT FROM NON-HILT CLASSES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * @EntryPoint lets you access Hilt-managed dependencies from classes that
 * Hilt does NOT inject into (ContentProvider, legacy code, third-party libraries).
 *
 * Why needed:
 *   - Hilt only injects into @AndroidEntryPoint-annotated Android classes
 *   - ContentProvider is created BEFORE Application.onCreate() — Hilt isn't ready
 *   - Some classes are instantiated by the framework, not by you
 *
 * Pattern:
 *   1. Define an @EntryPoint interface inside the consuming class
 *   2. Annotate it with @InstallIn(SingletonComponent::class)
 *   3. Access it via EntryPointAccessors.fromApplication()
 *
 * ⚠️ Use sparingly. If you find yourself using @EntryPoint often, the class
 *    should probably be refactored to use constructor injection instead.
 */

class ArticleContentProvider : ContentProvider() {

    // Step 1: Declare what you need from the Hilt graph
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ArticleContentProviderEntryPoint {
        fun articleRepository(): ArticleRepository
        fun json(): Json
    }

    private lateinit var repository: ArticleRepository
    private lateinit var json: Json

    override fun onCreate(): Boolean {
        // Step 2: Access the Hilt graph via EntryPointAccessors
        val entryPoint = EntryPointAccessors.fromApplication(
            context!!.applicationContext,
            ArticleContentProviderEntryPoint::class.java
        )
        repository = entryPoint.articleRepository()
        json = entryPoint.json()
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        // Use injected repository to serve data
        return null // Implementation depends on URI matching
    }

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, sel: String?, args: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, sel: String?, args: Array<String>?): Int = 0
}

/**
 * 💡 Interview Tip: "@EntryPoint is the escape hatch when you can't use
 *    constructor injection. ContentProvider is the most common use case
 *    because it's created before the Hilt component is ready."
 */


// ─────────────────────────────────────────────────────────────────────────────
// 9. MULTI-MODULE DI
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Hilt in multi-module projects:
 *
 * Structure:
 *   :app              → @HiltAndroidApp, @AndroidEntryPoint
 *   :core:network     → NetworkModule (@InstallIn SingletonComponent)
 *   :core:database    → DatabaseModule (@InstallIn SingletonComponent)
 *   :feature:articles → ArticleViewModel, feature-scoped modules
 *
 * Key rules:
 *
 * 1. Each module defines its own @Module classes with @InstallIn.
 *    Hilt's annotation processor aggregates all modules at compile time.
 *
 * 2. The :app module must depend on all modules (directly or transitively)
 *    for Hilt to see their @Module and @EntryPoint annotations.
 *
 * 3. api vs implementation for DI modules:
 *    - Use `api` if the module exposes types that other modules inject
 *      (e.g., :core:network provides OkHttpClient used by :feature:articles)
 *    - Use `implementation` if the module's bindings are internal only
 *    - Rule of thumb: interfaces go in api, implementations in implementation
 *
 * 4. Avoiding duplicate singletons:
 *    - @Singleton in :core:network guarantees one instance app-wide
 *    - If two feature modules each provide their own OkHttpClient,
 *      you get duplicates. Centralize shared singletons in :core modules.
 *    - Use @Qualifier to differentiate when you intentionally want multiple
 *      instances (e.g., authenticated vs unauthenticated OkHttpClient)
 *
 * 5. Interface + implementation split:
 *    - Define interface in a :core:data-api module (no Hilt dependency)
 *    - Provide implementation in :core:data-impl with @Binds
 *    - Feature modules depend only on :core:data-api
 *
 * ⚠️ Common pitfall: using @InstallIn(ActivityComponent::class) in a library
 *    module. Library modules should almost always use SingletonComponent
 *    or ActivityRetainedComponent to avoid lifecycle assumptions.
 */

// :core:network module
@Module
@InstallIn(SingletonComponent::class)
object CoreNetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Qualifier
    annotation class AuthenticatedClient

    @Provides
    @Singleton
    @AuthenticatedClient
    fun provideAuthenticatedClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}

// :feature:articles module — depends on :core:network via api/implementation
@Module
@InstallIn(SingletonComponent::class)
object ArticleNetworkModule {

    @Provides
    @Singleton
    fun provideArticleApi(
        @AuthenticatedClient okHttpClient: OkHttpClient
    ): ArticleApi {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/v1/")
            .client(okHttpClient)
            .build()
            .create(ArticleApi::class.java)
    }
}

/**
 * 💡 Interview Tip: "In multi-module projects, I keep shared singletons
 *    in :core modules and use @Qualifier when multiple instances of the
 *    same type are needed. Feature modules only declare feature-scoped bindings."
 */


// ─────────────────────────────────────────────────────────────────────────────
// 10. WORKMANAGER + HILT CONFIGURATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * @HiltWorker requires custom WorkManager initialization because Hilt needs
 * to supply its own WorkerFactory to inject dependencies into Workers.
 *
 * Steps:
 *   1. Disable default WorkManager initializer in AndroidManifest
 *   2. Implement Configuration.Provider in your Application class
 *   3. Inject HiltWorkerFactory and return it in workManagerConfiguration
 *   4. Annotate your Worker with @HiltWorker and use @AssistedInject
 *
 * Why custom initialization?
 *   Default WorkManager uses the built-in WorkerFactory that only knows how
 *   to call Worker(context, params). @HiltWorker needs additional constructor
 *   params (your injected dependencies), which only HiltWorkerFactory can provide.
 */

// Step 1: Disable default initializer in AndroidManifest.xml
/*
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false"
    tools:node="merge">
    <meta-data
        android:name="androidx.work.WorkManagerInitializer"
        android:value="androidx.startup"
        tools:node="remove" />
</provider>
*/

// Step 2: Application class with custom WorkManager config
@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(
                if (BuildConfig.DEBUG) android.util.Log.DEBUG
                else android.util.Log.ERROR
            )
            .build()
}

// Step 3: Worker with @HiltWorker
@HiltWorker
class ArticleSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: ArticleRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val count = repository.syncAll()
            if (count > 0) {
                notificationHelper.showSyncComplete(count)
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = notificationHelper.buildSyncProgressNotification()
        return ForegroundInfo(SYNC_NOTIFICATION_ID, notification)
    }

    companion object {
        const val SYNC_NOTIFICATION_ID = 3001
    }
}

/**
 * ⚠️ Common mistake: forgetting to disable the default initializer.
 *    WorkManager will crash at runtime with "WorkManager is already initialized"
 *    because both the default and custom initializers try to run.
 *
 * 💡 Interview Tip: "The three pieces to remember for @HiltWorker are:
 *    disable the default initializer, implement Configuration.Provider,
 *    and use @AssistedInject with @Assisted for context and params."
 */


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: What's the difference between @Singleton and no scope in Hilt?
 *    A: @Singleton: one instance for entire app lifetime (cached in SingletonComponent).
 *       No scope: new instance created every time it's injected.
 *       Use @Singleton for expensive objects (OkHttpClient, Database).
 *       Use no scope for lightweight objects (UseCases, mappers).
 *
 * ❓ Q2: How does Hilt handle ViewModel creation?
 *    A: @HiltViewModel + @Inject constructor. Hilt generates a custom
 *       ViewModelProvider.Factory that creates the ViewModel with injected
 *       dependencies. SavedStateHandle is auto-provided.
 *
 * ❓ Q3: When would you use @Binds vs @Provides?
 *    A: @Binds when mapping interface → implementation (no logic needed).
 *       @Provides when: creating 3rd party objects, complex creation logic,
 *       or when the module isn't abstract. @Binds generates less code.
 *
 * ❓ Q4: How do you provide different implementations for debug/release?
 *    A: Create separate modules in debug/ and release/ source sets.
 *       Or use @Qualifier annotations with different @Provides methods.
 *       Or use Hilt's @TestInstallIn to replace modules in tests.
 *
 * ❓ Q5: What's the advantage of Dagger/Hilt over Koin?
 *    A: Dagger/Hilt: compile-time validation (catches errors at build time),
 *       no runtime overhead (generated code), better performance.
 *       Koin: simpler setup, DSL-based, runtime resolution (errors at runtime),
 *       slight runtime overhead. Google recommends Hilt for production apps.
 *
 * ❓ Q6: What is @EntryPoint and when would you use it?
 *    A: @EntryPoint is an interface annotated with @InstallIn that lets you
 *       access Hilt-managed dependencies from classes Hilt doesn't inject into.
 *       Primary use case: ContentProvider (created before Application.onCreate()).
 *       Also useful for legacy code, third-party library callbacks, or any class
 *       where you can't use @Inject constructor. Access it via
 *       EntryPointAccessors.fromApplication(). Use sparingly — prefer refactoring
 *       to constructor injection when possible.
 *
 * ❓ Q7: How does Hilt work in a multi-module Gradle project?
 *    A: Each module defines its own @Module + @InstallIn classes. Hilt's annotation
 *       processor aggregates all modules at compile time in the :app module.
 *       Shared singletons (OkHttpClient, Database) belong in :core modules.
 *       Use @Qualifier to differentiate multiple instances of the same type.
 *       Use `api` dependency for types other modules inject, `implementation` for
 *       internal-only bindings. Avoid @InstallIn(ActivityComponent) in libraries —
 *       use SingletonComponent or ActivityRetainedComponent instead.
 */
