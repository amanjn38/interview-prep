/**
 * ============================================================================
 * 05 — DATA PERSISTENCE
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Room Database (Entities, DAOs, Database)
 *   2. Room Relations (One-to-One, One-to-Many, Many-to-Many)
 *   3. Room Type Converters
 *   4. Room Migrations (Auto & Manual)
 *   5. DataStore (Preferences)
 *   6. SharedPreferences (Legacy but still asked)
 *   7. Content Providers
 *   8. Choosing the Right Storage
 *   9. Paging 3 + Room (PagingSource, RemoteMediator)
 *  10. Proto DataStore
 *  11. Room Advanced (@DatabaseView, FTS4, Testing)
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. ROOM DATABASE — COMPLETE SETUP
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Room = SQLite abstraction layer with compile-time query verification.
 *
 * Three core components:
 *   @Entity    → defines a table
 *   @Dao       → defines operations (queries)
 *   @Database  → ties entities and DAOs together
 *
 * 💡 Interview Tip: Google uses Room as the single source of truth
 *    in offline-first architectures. Know it deeply.
 */

// ── Entity ──
@Entity(
    tableName = "articles",
    indices = [
        Index(value = ["author_id"]),        // Query optimization
        Index(value = ["slug"], unique = true) // Unique constraint
    ]
)
data class ArticleEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "author_id")
    val authorId: String,

    @ColumnInfo(name = "slug")
    val slug: String,

    @ColumnInfo(name = "tags")
    val tags: List<String>,              // Needs TypeConverter

    @ColumnInfo(name = "published_at")
    val publishedAt: Long,

    @ColumnInfo(name = "is_bookmarked")
    val isBookmarked: Boolean = false,

    @ColumnInfo(name = "last_fetched")
    val lastFetched: Long = System.currentTimeMillis()
)

// ── Embedded objects ──
data class Address(
    val street: String,
    val city: String,
    val zipCode: String
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    @Embedded(prefix = "address_") val address: Address
    // Creates columns: address_street, address_city, address_zipCode
)


// ── DAO ──
@Dao
interface ArticleDao {

    // Basic CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: ArticleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)

    @Update
    suspend fun update(article: ArticleEntity)

    @Delete
    suspend fun delete(article: ArticleEntity)

    // Queries
    @Query("SELECT * FROM articles ORDER BY published_at DESC")
    suspend fun getAllArticles(): List<ArticleEntity>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: String): ArticleEntity?

    @Query("SELECT * FROM articles WHERE is_bookmarked = 1 ORDER BY published_at DESC")
    suspend fun getBookmarkedArticles(): List<ArticleEntity>

    // Reactive query — emits new list whenever data changes
    @Query("SELECT * FROM articles ORDER BY published_at DESC")
    fun observeAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id")
    fun observeArticle(id: String): Flow<ArticleEntity?>

    // Upsert (insert or update) — Room 2.5+
    @Upsert
    suspend fun upsert(article: ArticleEntity)

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    // Transaction — multiple operations atomically
    @Transaction
    suspend fun replaceAll(articles: List<ArticleEntity>) {
        deleteAll()
        insertAll(articles)
    }

    @Query("DELETE FROM articles")
    suspend fun deleteAll()

    // Pagination with LIMIT/OFFSET
    @Query("SELECT * FROM articles ORDER BY published_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getArticlesPaginated(limit: Int, offset: Int): List<ArticleEntity>

    // Full-text search (requires FTS4 setup)
    @Query("SELECT * FROM articles WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<ArticleEntity>
}


// ─────────────────────────────────────────────────────────────────────────────
// 2. ROOM RELATIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Room doesn't allow direct object references between entities.
 * Use @Relation for querying related data.
 *
 * Types of relationships:
 *   - One-to-One  (@Relation with entity reference)
 *   - One-to-Many (@Relation with parentColumn + entityColumn)
 *   - Many-to-Many (junction table with @Junction)
 */

// One-to-Many: User has many Articles
data class UserWithArticles(
    @Embedded val user: UserEntity,
    @Relation(
        parentColumn = "id",       // UserEntity.id
        entityColumn = "author_id" // ArticleEntity.authorId
    )
    val articles: List<ArticleEntity>
)

@Dao
interface UserDao {
    @Transaction  // ⚠️ Required for @Relation queries (ensures consistency)
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserWithArticles(userId: String): UserWithArticles

    @Transaction
    @Query("SELECT * FROM users")
    fun observeAllUsersWithArticles(): Flow<List<UserWithArticles>>
}

// Many-to-Many: Articles can have many Tags, Tags can have many Articles
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val tagId: String,
    val name: String
)

@Entity(
    tableName = "article_tag_cross_ref",
    primaryKeys = ["articleId", "tagId"]  // Composite primary key
)
data class ArticleTagCrossRef(
    val articleId: String,
    val tagId: String
)

data class ArticleWithTags(
    @Embedded val article: ArticleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tagId",
        associateBy = Junction(ArticleTagCrossRef::class)
    )
    val tags: List<TagEntity>
)


// ─────────────────────────────────────────────────────────────────────────────
// 3. TYPE CONVERTERS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Room only stores primitive types. TypeConverters handle custom types.
 */

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun toInstant(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 4. ROOM MIGRATIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * When you change the database schema, you MUST provide a migration path.
 * Otherwise, Room throws IllegalStateException on app update.
 *
 * Options:
 *   1. Auto Migration (Room 2.4+) — for simple changes
 *   2. Manual Migration — for complex changes
 *   3. Destructive migration — drop and recreate (data loss!)
 */

// Database class with migrations
@Database(
    entities = [ArticleEntity::class, UserEntity::class, TagEntity::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),  // Simple column add
        AutoMigration(from = 2, to = 3, spec = Migration2To3::class)
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun userDao(): UserDao
}

// Auto Migration spec for complex changes (rename, delete)
@RenameColumn(tableName = "articles", fromColumnName = "body", toColumnName = "content")
class Migration2To3 : AutoMigrationSpec

// Manual migration for complex schema changes
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create new table with desired schema
        db.execSQL("""
            CREATE TABLE articles_new (
                id TEXT NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                author_id TEXT NOT NULL,
                slug TEXT NOT NULL,
                published_at INTEGER NOT NULL DEFAULT 0,
                word_count INTEGER NOT NULL DEFAULT 0
            )
        """)

        // Copy data from old table
        db.execSQL("""
            INSERT INTO articles_new (id, title, content, author_id, slug, published_at)
            SELECT id, title, content, author_id, slug, published_at
            FROM articles
        """)

        // Drop old table and rename
        db.execSQL("DROP TABLE articles")
        db.execSQL("ALTER TABLE articles_new RENAME TO articles")

        // Create indices
        db.execSQL("CREATE INDEX index_articles_author_id ON articles(author_id)")
        db.execSQL("CREATE UNIQUE INDEX index_articles_slug ON articles(slug)")
    }
}

// Building the database
fun provideDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "app_database"
    )
        .addMigrations(MIGRATION_3_4)
        // ⚠️ Only for development — destroys data on failed migration
        // .fallbackToDestructiveMigration()
        .build()
}


// ─────────────────────────────────────────────────────────────────────────────
// 5. DATASTORE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * DataStore = modern replacement for SharedPreferences.
 *
 * Two flavors:
 *   Preferences DataStore → key-value pairs (like SharedPreferences but async)
 *   Proto DataStore        → typed objects (Protocol Buffers)
 *
 * Advantages over SharedPreferences:
 *   ✅ Kotlin Coroutines & Flow API (async, no blocking)
 *   ✅ Handles errors via Flow's catch operator
 *   ✅ No runtime exceptions on bad types
 *   ✅ Transactional — no partial updates
 *   ✅ No apply() / commit() confusion
 */

// Preferences DataStore
private val Context.userPreferencesStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FONT_SIZE = intPreferencesKey("font_size")
        val USERNAME = stringPreferencesKey("username")
        val SORT_ORDER = stringPreferencesKey("sort_order")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                darkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
                fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 14,
                username = preferences[PreferencesKeys.USERNAME] ?: "",
                sortOrder = SortOrder.valueOf(
                    preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.NEWEST.name
                )
            )
        }

    suspend fun updateDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }
}

data class UserPreferences(
    val darkMode: Boolean,
    val fontSize: Int,
    val username: String,
    val sortOrder: SortOrder
)


// ─────────────────────────────────────────────────────────────────────────────
// 6. SHAREDPREFERENCES (LEGACY)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Still used in many apps. Know the issues for interviews.
 *
 * ⚠️ Problems:
 *   1. apply() is async but blocks the main thread on Activity stop (ANR risk)
 *   2. commit() is synchronous on the calling thread (blocks UI if on Main)
 *   3. No type safety — getString() on an int key returns null, not a crash
 *   4. No error handling — fails silently
 *   5. No transactional guarantees — partial writes possible
 *   6. getAll() returns a mutable reference to the underlying map
 *
 * 💡 Interview: Always mention migrating to DataStore for new code.
 */

// Migration from SharedPreferences to DataStore
private val Context.dataStore by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "old_shared_prefs"))
    }
)


// ─────────────────────────────────────────────────────────────────────────────
// 7. CONTENT PROVIDERS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Content Providers expose data to other apps via a structured URI-based API.
 *
 * Used for:
 *   - Contacts, MediaStore, Calendar
 *   - Sharing data between apps (your app → other apps)
 *   - Required for SearchRecentSuggestionsProvider
 *   - CursorLoader compatibility
 *
 * URI format: content://authority/path/id
 *   Example: content://com.google.app.provider/articles/123
 *
 * 💡 Interview Tip: For most modern apps, you don't need to create a
 *    ContentProvider. Use them to ACCESS system data (Contacts, Media).
 */

// Querying the MediaStore (Scoped Storage)
suspend fun getImages(context: Context): List<MediaImage> {
    return withContext(Dispatchers.IO) {
        val images = mutableListOf<MediaImage>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                )
                images.add(MediaImage(id, name, uri))
            }
        }
        images
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 8. CHOOSING THE RIGHT STORAGE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ┌──────────────────┬─────────────────────┬──────────────────────────────────┐
 * │ Storage          │ Use Case            │ Notes                            │
 * ├──────────────────┼─────────────────────┼──────────────────────────────────┤
 * │ Room             │ Structured data,    │ SSOT, reactive queries,          │
 * │                  │ complex queries     │ offline-first                    │
 * ├──────────────────┼─────────────────────┼──────────────────────────────────┤
 * │ DataStore        │ Key-value prefs,    │ Async, type-safe,                │
 * │ (Preferences)    │ settings            │ replaces SharedPreferences       │
 * ├──────────────────┼─────────────────────┼──────────────────────────────────┤
 * │ DataStore        │ Complex settings,   │ Schema evolution,                │
 * │ (Proto)          │ typed config        │ protocol buffers                 │
 * ├──────────────────┼─────────────────────┼──────────────────────────────────┤
 * │ Internal Storage │ Private app files   │ No permissions needed,           │
 * │                  │                     │ deleted on uninstall             │
 * ├──────────────────┼─────────────────────┼──────────────────────────────────┤
 * │ Scoped Storage   │ Media, documents    │ MediaStore for shared media,     │
 * │                  │                     │ SAF for user-selected files      │
 * ├──────────────────┼─────────────────────┼──────────────────────────────────┤
 * │ EncryptedSharedP │ Sensitive data      │ Backed by AndroidX Security,     │
 * │                  │ (tokens, keys)      │ AES-256 encryption               │
 * └──────────────────┴─────────────────────┴──────────────────────────────────┘
 */


// ─────────────────────────────────────────────────────────────────────────────
// 9. PAGING 3 + ROOM
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Paging 3 loads data in chunks (pages), reducing memory usage and network calls.
 *
 * Core components:
 *   PagingSource        → loads pages from a single source (DB or network)
 *   RemoteMediator      → orchestrates network + DB for offline-first paging
 *   Pager               → creates a PagingData flow from PagingSource/RemoteMediator
 *   PagingDataAdapter   → RecyclerView adapter (or collectAsLazyPagingItems in Compose)
 *
 * 💡 Interview Tip: Google's recommended architecture for large lists is
 *    Network → RemoteMediator → Room (single source of truth) → PagingSource → UI
 */

// ── Room DAO returning PagingSource ──

@Dao
interface PagingArticleDao {
    @Query("SELECT * FROM articles ORDER BY published_at DESC")
    fun pagingSource(): PagingSource<Int, ArticleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)

    @Query("DELETE FROM articles")
    suspend fun clearAll()
}

// ── RemoteMediator: offline-first paging ──

@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator(
    private val api: ArticleApi,
    private val database: AppDatabase
) : RemoteMediator<Int, ArticleEntity>() {

    private val articleDao = database.articleDao()

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.HOURS.toMillis(1)
        val lastFetched = database.remoteKeyDao().getLastFetchTime() ?: 0L
        return if (System.currentTimeMillis() - lastFetched < cacheTimeout) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                database.remoteKeyDao().getNextPage(lastItem.id)
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return try {
            val response = api.getArticles(page = page, pageSize = state.config.pageSize)
            val endReached = response.articles.size < state.config.pageSize

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    articleDao.clearAll()
                    database.remoteKeyDao().clearAll()
                }
                articleDao.insertAll(response.articles.map { it.toEntity() })
                database.remoteKeyDao().insertKey(
                    RemoteKey(
                        articleId = response.articles.last().id,
                        nextPage = if (endReached) null else page + 1,
                        lastFetchTime = System.currentTimeMillis()
                    )
                )
            }

            MediatorResult.Success(endOfPaginationReached = endReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}

// ── Remote key entity for tracking pagination state ──

@Entity(tableName = "remote_keys")
data class RemoteKey(
    @PrimaryKey val articleId: String,
    val nextPage: Int?,
    val lastFetchTime: Long
)

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: RemoteKey)

    @Query("SELECT nextPage FROM remote_keys WHERE articleId = :articleId")
    suspend fun getNextPage(articleId: String): Int?

    @Query("SELECT MAX(lastFetchTime) FROM remote_keys")
    suspend fun getLastFetchTime(): Long?

    @Query("DELETE FROM remote_keys")
    suspend fun clearAll()
}

// ── Wiring it together: Pager + cachedIn ──

class ArticleRepository(
    private val api: ArticleApi,
    private val database: AppDatabase
) {
    @OptIn(ExperimentalPagingApi::class)
    fun getArticlesPaged(): Flow<PagingData<ArticleEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = ArticleRemoteMediator(api, database),
            pagingSourceFactory = { database.articleDao().pagingSource() }
        ).flow
    }
}

class PagingViewModel(repository: ArticleRepository) : ViewModel() {
    val articles: Flow<PagingData<ArticleEntity>> = repository
        .getArticlesPaged()
        .cachedIn(viewModelScope)  // Survives config changes
}

// ── Compose UI with LazyPagingItems ──

@Composable
fun ArticleListScreen(viewModel: PagingViewModel = hiltViewModel()) {
    val articles = viewModel.articles.collectAsLazyPagingItems()

    LazyColumn {
        items(
            count = articles.itemCount,
            key = articles.itemKey { it.id }
        ) { index ->
            val article = articles[index]
            if (article != null) {
                ArticleCard(article)
            }
        }

        // Loading/error states
        when (articles.loadState.append) {
            is LoadState.Loading -> item { CircularProgressIndicator() }
            is LoadState.Error -> item {
                RetryButton(onClick = { articles.retry() })
            }
            else -> {}
        }
    }
}

/**
 * ⚠️ Paging Gotchas:
 *   1. Always call cachedIn(viewModelScope) to avoid re-fetching on config change
 *   2. PagingSource is invalidated automatically by Room when data changes
 *   3. RemoteMediator.initialize() controls whether to skip initial network call
 *   4. Never load all pages eagerly — defeats the purpose of paging
 */


// ─────────────────────────────────────────────────────────────────────────────
// 10. PROTO DATASTORE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Proto DataStore stores typed objects using Protocol Buffers.
 *
 * When to use Proto vs Preferences DataStore:
 *   Preferences DataStore → simple key-value pairs (dark mode, font size)
 *   Proto DataStore        → complex/nested settings with schema evolution
 *
 * Proto advantages:
 *   ✅ Full type safety at compile time (no string keys)
 *   ✅ Schema evolution with backward compatibility
 *   ✅ Nested objects and repeated fields
 *   ✅ Smaller serialized size than JSON
 *
 * Setup requires:
 *   1. .proto schema file
 *   2. Serializer implementation
 *   3. protobuf Gradle plugin
 */

// ── Step 1: Proto schema (app/src/main/proto/user_settings.proto) ──

/**
 * syntax = "proto3";
 *
 * option java_package = "com.example.app";
 * option java_multiple_files = true;
 *
 * message UserSettings {
 *     bool dark_mode = 1;
 *     int32 font_size = 2;
 *     string language = 3;
 *     NotificationConfig notifications = 4;
 *
 *     message NotificationConfig {
 *         bool enabled = 1;
 *         bool sound = 2;
 *         bool vibrate = 3;
 *     }
 * }
 */

// ── Step 2: Serializer ──

object UserSettingsSerializer : Serializer<UserSettings> {
    override val defaultValue: UserSettings = UserSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserSettings {
        try {
            return UserSettings.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: UserSettings, output: OutputStream) {
        t.writeTo(output)
    }
}

// ── Step 3: Create the DataStore ──

private val Context.userSettingsStore: DataStore<UserSettings> by dataStore(
    fileName = "user_settings.pb",
    serializer = UserSettingsSerializer
)

// ── Step 4: Repository for reading/writing ──

class UserSettingsRepository(private val dataStore: DataStore<UserSettings>) {

    val settings: Flow<UserSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(UserSettings.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.updateData { current ->
            current.toBuilder().setDarkMode(enabled).build()
        }
    }

    suspend fun updateNotifications(block: UserSettings.NotificationConfig.Builder.() -> Unit) {
        dataStore.updateData { current ->
            current.toBuilder().setNotifications(
                current.notifications.toBuilder().apply(block).build()
            ).build()
        }
    }
}

/**
 * 💡 Interview Tip: Proto DataStore is the right choice when you have:
 *    - More than ~5 related settings
 *    - Nested configuration objects
 *    - Need for schema evolution (adding/removing fields safely)
 *    - Strong type safety requirements
 *    For simple boolean flags or a single preference, Preferences DataStore is simpler.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 11. ROOM ADVANCED
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Advanced Room features that come up in SDE-2+ interviews:
 *   - @DatabaseView (virtual table from a query)
 *   - FTS4 (full-text search)
 *   - Migration testing with MigrationTestHelper
 *   - In-memory database for unit tests
 */

// ── @DatabaseView — precomputed query as a virtual table ──

@DatabaseView(
    value = """
        SELECT a.id, a.title, a.published_at, u.name AS authorName,
               COUNT(c.id) AS commentCount
        FROM articles a
        INNER JOIN users u ON a.author_id = u.id
        LEFT JOIN comments c ON c.article_id = a.id
        GROUP BY a.id
    """,
    viewName = "article_summary"
)
data class ArticleSummary(
    val id: String,
    val title: String,
    val publishedAt: Long,
    val authorName: String,
    val commentCount: Int
)

@Dao
interface ArticleSummaryDao {
    @Query("SELECT * FROM article_summary ORDER BY publishedAt DESC")
    fun observeSummaries(): Flow<List<ArticleSummary>>

    @Query("SELECT * FROM article_summary WHERE commentCount > :minComments")
    suspend fun getPopularArticles(minComments: Int): List<ArticleSummary>
}

// Register the view in @Database
// @Database(entities = [...], views = [ArticleSummary::class], version = ...)

/**
 * 💡 @DatabaseView benefits:
 *    - Encapsulates complex JOIN queries
 *    - Can be queried like a regular table
 *    - Updated automatically when underlying tables change
 *    - ⚠️ Read-only — no INSERT/UPDATE/DELETE
 */

// ── FTS4 — Full-Text Search ──

@Fts4(contentEntity = ArticleEntity::class)
@Entity(tableName = "articles_fts")
data class ArticleFts(
    val title: String,
    val content: String
)

@Dao
interface ArticleFtsDao {
    @Query("""
        SELECT articles.* FROM articles
        JOIN articles_fts ON articles.rowid = articles_fts.rowid
        WHERE articles_fts MATCH :query
        ORDER BY rank
    """)
    suspend fun search(query: String): List<ArticleEntity>

    @Query("""
        SELECT snippet(articles_fts, '<b>', '</b>', '...', 1, 30) AS snippet
        FROM articles_fts WHERE articles_fts MATCH :query
    """)
    suspend fun searchWithSnippets(query: String): List<SearchSnippet>
}

data class SearchSnippet(val snippet: String)

/**
 * FTS4 tips:
 *   - contentEntity links FTS table to the source entity (keeps them in sync)
 *   - MATCH operator uses FTS syntax: "android kotlin" (AND), "android OR kotlin"
 *   - snippet() highlights matches in results
 *   - ⚠️ FTS tables use more disk space (duplicate indexed text)
 *   - Register FTS entity in @Database(entities = [..., ArticleFts::class])
 */

// ── Room Testing: MigrationTestHelper ──

/**
 * MigrationTestHelper validates that migrations work correctly by:
 *   1. Creating the database at an older version
 *   2. Running the migration
 *   3. Validating the schema matches expectations
 *
 * ⚠️ ALWAYS test migrations. A bad migration can corrupt user data or crash on launch.
 */

class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate3to4() {
        // Create database at version 3
        helper.createDatabase("test_db", 3).apply {
            execSQL("""
                INSERT INTO articles (id, title, content, author_id, slug, published_at)
                VALUES ('1', 'Test', 'Body', 'author1', 'test-slug', 1234567890)
            """)
            close()
        }

        // Run migration and validate
        val db = helper.runMigrationsAndValidate("test_db", 4, true, MIGRATION_3_4)

        // Verify data survived
        val cursor = db.query("SELECT * FROM articles WHERE id = '1'")
        assertTrue(cursor.moveToFirst())
        assertEquals("Test", cursor.getString(cursor.getColumnIndex("title")))
        assertEquals(0, cursor.getInt(cursor.getColumnIndex("word_count")))
        cursor.close()
    }
}

// ── In-Memory Database for Unit Tests ──

class ArticleDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var articleDao: ArticleDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()  // Only in tests!
            .build()

        articleDao = database.articleDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveArticle() = runTest {
        val article = ArticleEntity(
            id = "1", title = "Test", content = "Body",
            authorId = "a1", slug = "test", tags = emptyList(),
            publishedAt = System.currentTimeMillis()
        )
        articleDao.insert(article)
        val result = articleDao.getArticleById("1")
        assertEquals(article.title, result?.title)
    }

    @Test
    fun observeArticlesEmitsOnChange() = runTest {
        val emissions = mutableListOf<List<ArticleEntity>>()
        val job = launch {
            articleDao.observeAllArticles().take(2).collect { emissions.add(it) }
        }

        val article = ArticleEntity(
            id = "1", title = "Test", content = "Body",
            authorId = "a1", slug = "test", tags = emptyList(),
            publishedAt = System.currentTimeMillis()
        )
        articleDao.insert(article)
        advanceUntilIdle()
        job.join()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0].isEmpty())
        assertEquals(1, emissions[1].size)
    }
}

/**
 * 💡 Testing best practices:
 *    - Use in-memory database for DAO unit tests (fast, no disk I/O)
 *    - Use MigrationTestHelper for migration tests (catches schema issues)
 *    - allowMainThreadQueries() only in tests — never in production
 *    - Test reactive queries (Flow) with Turbine or take(n) + advanceUntilIdle
 */


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: Why does Room require @Transaction for @Relation queries?
 *    A: Without it, the parent query and child query run as separate DB ops.
 *       Another thread could modify data between them, causing inconsistency.
 *       @Transaction wraps both queries in a single SQLite transaction.
 *
 * ❓ Q2: How would you handle a schema migration that renames a table?
 *    A: Use AutoMigration with @RenameTable spec, OR write a manual migration
 *       that creates the new table, copies data, drops old table, and renames.
 *       Always test migrations with Room's MigrationTestHelper.
 *
 * ❓ Q3: Room vs SQLite — what does Room add?
 *    A: Compile-time SQL verification, type-safe queries, Flow/LiveData support,
 *       automatic threading (suspend functions), migration support, testing helpers.
 *       ❌ Raw SQLite gives you none of these.
 *
 * ❓ Q4: What happens if DataStore.edit throws an IOException?
 *    A: The transaction is NOT committed (atomic). Collect errors via
 *       dataStore.data.catch { }. Unlike SharedPreferences, no silent corruption.
 *
 * ❓ Q5: How do you handle large datasets in Room efficiently?
 *    A: Use Paging 3 library with PagingSource from Room.
 *       Room provides built-in PagingSource support:
 *       @Query("SELECT * FROM articles") fun pagingSource(): PagingSource<Int, ArticleEntity>
 */
