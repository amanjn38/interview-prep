/**
 * ============================================================================
 * 06 — NETWORKING
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Retrofit — Setup, Interceptors, Error Handling
 *   2. OkHttp — Client Configuration, Interceptors, Caching
 *   3. Serialization (Moshi, Kotlin Serialization, Gson)
 *   4. Network Layer Architecture
 *   5. Caching Strategies
 *   6. Pagination (Paging 3)
 *   7. Network Connectivity Monitoring
 *   8. WebSocket / Server-Sent Events
 *   9. RemoteMediator Summary (full impl → 05_data_persistence.kt)
 *  10. API Design Best Practices
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. RETROFIT — SETUP & API DEFINITION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Retrofit = type-safe HTTP client built on top of OkHttp.
 * Converts HTTP API into a Kotlin interface.
 */

interface ArticleApiService {

    @GET("articles")
    suspend fun getArticles(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "published_at"
    ): Response<ArticleListResponse>

    @GET("articles/{id}")
    suspend fun getArticle(
        @Path("id") articleId: String
    ): Response<ArticleResponse>

    @POST("articles")
    suspend fun createArticle(
        @Body request: CreateArticleRequest
    ): Response<ArticleResponse>

    @PUT("articles/{id}")
    suspend fun updateArticle(
        @Path("id") articleId: String,
        @Body request: UpdateArticleRequest
    ): Response<ArticleResponse>

    @DELETE("articles/{id}")
    suspend fun deleteArticle(
        @Path("id") articleId: String
    ): Response<Unit>

    @Multipart
    @POST("articles/{id}/image")
    suspend fun uploadImage(
        @Path("id") articleId: String,
        @Part image: MultipartBody.Part
    ): Response<ImageUploadResponse>

    @Headers("Cache-Control: max-age=600")
    @GET("articles/trending")
    suspend fun getTrending(): Response<ArticleListResponse>
}


// ─────────────────────────────────────────────────────────────────────────────
// 2. OKHTTP — CLIENT CONFIGURATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * OkHttp handles the actual HTTP communication.
 * Configure timeouts, interceptors, caching, and security.
 */

fun provideOkHttpClient(
    authInterceptor: AuthInterceptor,
    loggingInterceptor: HttpLoggingInterceptor,
    cache: Cache
): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .cache(cache)
        // Application interceptors — called once, see final request/response
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        // Network interceptors — called for every network request (including redirects)
        .addNetworkInterceptor(CacheControlInterceptor())
        // Connection pool for HTTP/2 multiplexing
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        // Retry on connection failure
        .retryOnConnectionFailure(true)
        .build()
}

fun provideCache(context: Context): Cache {
    val cacheSize = 50L * 1024 * 1024  // 50 MB
    return Cache(File(context.cacheDir, "http_cache"), cacheSize)
}

/**
 * Interceptor Types:
 *
 * Application Interceptor:
 *   - Called once per request
 *   - Can short-circuit (return cached response without network)
 *   - Can retry and make multiple calls
 *   - Sees the original request (before OkHttp modifications)
 *
 * Network Interceptor:
 *   - Called for every network request (including redirects)
 *   - Can observe data as it flows over the network
 *   - Access to the Connection object (HTTP/2, etc.)
 *   - Sees the request after OkHttp modifications
 */

// Auth interceptor — adds token to every request
class AuthInterceptor(
    private val tokenProvider: TokenProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.getAccessToken()
        val request = chain.request().newBuilder().apply {
            token?.let { addHeader("Authorization", "Bearer $it") }
            addHeader("Accept", "application/json")
            addHeader("X-Platform", "Android")
            addHeader("X-App-Version", BuildConfig.VERSION_NAME)
        }.build()

        val response = chain.proceed(request)

        // Handle 401 — token expired, try refresh
        if (response.code == 401) {
            response.close()
            val newToken = tokenProvider.refreshToken()
                ?: throw AuthenticationException("Token refresh failed")

            val newRequest = chain.request().newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
            return chain.proceed(newRequest)
        }

        return response
    }
}

// Cache control interceptor — modify caching behavior
class CacheControlInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val cacheControl = CacheControl.Builder()
            .maxAge(10, TimeUnit.MINUTES)
            .build()
        return response.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .removeHeader("Pragma")
            .build()
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 3. SERIALIZATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Options:
 *   Kotlin Serialization → compile-time, multiplatform, no reflection
 *   Moshi                → efficient, Kotlin-friendly, code-gen or reflection
 *   Gson                 → legacy, reflection-based, slower
 *
 * 💡 Google recommendation: Kotlin Serialization for new projects.
 */

// Kotlin Serialization
@Serializable
data class ArticleResponse(
    val id: String,
    val title: String,
    val content: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("published_at") val publishedAt: Long,
    val tags: List<String> = emptyList(),
    @SerialName("read_time_minutes") val readTimeMinutes: Int? = null
)

@Serializable
data class ArticleListResponse(
    val articles: List<ArticleResponse>,
    @SerialName("total_count") val totalCount: Int,
    @SerialName("has_more") val hasMore: Boolean
)

// Retrofit setup with Kotlin Serialization
fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    val json = Json {
        ignoreUnknownKeys = true   // Don't crash on extra fields
        isLenient = true           // Accept non-strict JSON
        coerceInputValues = true   // Use default values for null on non-nullable fields
        encodeDefaults = false     // Don't send default values in requests
    }

    return Retrofit.Builder()
        .baseUrl("https://api.example.com/v1/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}


// ─────────────────────────────────────────────────────────────────────────────
// 4. NETWORK LAYER ARCHITECTURE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Clean network layer pattern:
 *   API Service → Remote Data Source → Repository → ViewModel
 *
 * Remote Data Source handles:
 *   - Calling the API
 *   - Mapping DTO → Domain model
 *   - Error handling and wrapping
 */

// Sealed class for network results
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>
}

// Safe API call wrapper
suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<T>
): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error(response.code(), "Empty response body")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            NetworkResult.Error(response.code(), errorBody ?: "Unknown error")
        }
    } catch (e: IOException) {
        NetworkResult.Exception(e)  // Network error
    } catch (e: HttpException) {
        NetworkResult.Error(e.code(), e.message())
    } catch (e: SerializationException) {
        NetworkResult.Exception(e)  // Parsing error
    }
}

// Remote Data Source
class ArticleRemoteDataSource(
    private val api: ArticleApiService
) {
    suspend fun getArticles(page: Int): NetworkResult<List<Article>> {
        return when (val result = safeApiCall { api.getArticles(page) }) {
            is NetworkResult.Success -> {
                val articles = result.data.articles.map { it.toDomain() }
                NetworkResult.Success(articles)
            }
            is NetworkResult.Error -> result
            is NetworkResult.Exception -> result
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 5. CACHING STRATEGIES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Caching levels:
 *
 * 1. OkHttp HTTP Cache (disk):
 *    - Respects Cache-Control headers
 *    - Automatic ETag / If-Modified-Since
 *    - Good for static resources (images, configs)
 *
 * 2. In-Memory Cache (app-level):
 *    - Fast access, no serialization
 *    - Lost on process death
 *    - Good for frequently accessed, computed data
 *
 * 3. Database Cache (Room):
 *    - Survives process death
 *    - Single source of truth (offline-first)
 *    - Reactive queries via Flow
 *
 * Offline-First Pattern (Google recommended):
 *    UI → Room (SSOT) → Repository → API (sync mechanism)
 */

// In-memory cache with expiration
class InMemoryCache<K, V>(
    private val maxSize: Int = 100,
    private val expirationMs: Long = 5 * 60 * 1000  // 5 minutes
) {
    private val cache = LinkedHashMap<K, CacheEntry<V>>(maxSize, 0.75f, true)

    data class CacheEntry<V>(val value: V, val timestamp: Long)

    @Synchronized
    fun get(key: K): V? {
        val entry = cache[key] ?: return null
        if (System.currentTimeMillis() - entry.timestamp > expirationMs) {
            cache.remove(key)
            return null
        }
        return entry.value
    }

    @Synchronized
    fun put(key: K, value: V) {
        if (cache.size >= maxSize) {
            val oldest = cache.entries.first()
            cache.remove(oldest.key)
        }
        cache[key] = CacheEntry(value, System.currentTimeMillis())
    }

    @Synchronized
    fun invalidate(key: K) = cache.remove(key)

    @Synchronized
    fun invalidateAll() = cache.clear()
}


// ─────────────────────────────────────────────────────────────────────────────
// 6. PAGINATION WITH PAGING 3
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Paging 3 = Jetpack library for loading data in pages.
 *
 * Components:
 *   PagingSource     → loads pages from a single data source
 *   RemoteMediator   → loads from network, caches in DB
 *   Pager            → creates a Flow<PagingData>
 *   PagingDataAdapter → RecyclerView adapter (or collectAsLazyPagingItems in Compose)
 */

class ArticlePagingSource(
    private val api: ArticleApiService,
    private val query: String
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        return try {
            val response = api.getArticles(page, params.loadSize)
            if (response.isSuccessful) {
                val body = response.body()!!
                LoadResult.Page(
                    data = body.articles.map { it.toDomain() },
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (body.hasMore) page + 1 else null
                )
            } else {
                LoadResult.Error(HttpException(response))
            }
        } catch (e: IOException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

// ViewModel with Paging
class PaginatedViewModel(private val api: ArticleApiService) : ViewModel() {
    val articles: Flow<PagingData<Article>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            prefetchDistance = 5,   // Load next page when 5 items from end
            enablePlaceholders = false,
            initialLoadSize = 40    // First page loads more
        ),
        pagingSourceFactory = { ArticlePagingSource(api, "") }
    ).flow.cachedIn(viewModelScope)  // Survives config changes
}

// Compose UI with Paging
@Composable
fun PaginatedList(viewModel: PaginatedViewModel = hiltViewModel()) {
    val articles = viewModel.articles.collectAsLazyPagingItems()

    LazyColumn {
        items(articles.itemCount, key = { articles[it]?.id ?: it }) { index ->
            articles[index]?.let { article ->
                ArticleItem(article)
            }
        }

        // Loading states
        when (articles.loadState.append) {
            is LoadState.Loading -> item { CircularProgressIndicator() }
            is LoadState.Error -> item {
                RetryButton(onClick = { articles.retry() })
            }
            else -> {}
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 7. NETWORK CONNECTIVITY MONITORING
// ─────────────────────────────────────────────────────────────────────────────

class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                )
                trySend(hasInternet)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        // Emit current state
        val currentNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
        trySend(capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}


// ─────────────────────────────────────────────────────────────────────────────
// 8. WEBSOCKET & SERVER-SENT EVENTS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * WebSocket = full-duplex, persistent connection between client and server.
 * SSE (Server-Sent Events) = one-way stream from server to client over HTTP.
 *
 * When to use what:
 *   WebSocket → bidirectional real-time (chat, gaming, collaborative editing)
 *   SSE       → server-push only (live feeds, notifications, stock tickers)
 *   Polling   → low-frequency updates, simplest to implement, highest latency
 *
 * 💡 Interview Tip: Google favors WebSocket for bidirectional and SSE for
 *    unidirectional streams. Know the trade-offs (connection overhead, proxy
 *    support, automatic reconnection).
 *
 * ⚠️ SSE advantages over WebSocket:
 *    - Built-in reconnection and event ID tracking
 *    - Works through HTTP proxies without special config
 *    - Simpler server implementation
 *    ⚠️ SSE limitations: text-only (no binary), unidirectional
 */

// WebSocket client with OkHttp
class ArticleWebSocketClient(
    private val okHttpClient: OkHttpClient,
    private val onMessage: (String) -> Unit,
    private val onConnectionChange: (Boolean) -> Unit
) {
    private var webSocket: WebSocket? = null
    private var retryCount = 0
    private val maxRetries = 5
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun connect() {
        val request = Request.Builder()
            .url("wss://api.example.com/ws/articles")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                retryCount = 0
                onConnectionChange(true)
                // Send auth or subscribe message
                webSocket.send("""{"type":"subscribe","channel":"articles"}""")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                onConnectionChange(false)
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: okhttp3.Response?
            ) {
                onConnectionChange(false)
                reconnectWithBackoff()
            }
        })
    }

    private fun reconnectWithBackoff() {
        if (retryCount >= maxRetries) return
        val delayMs = (1000L * (1 shl retryCount)).coerceAtMost(30_000L) // Exponential, cap 30s
        retryCount++
        scope.launch {
            delay(delayMs)
            connect()
        }
    }

    fun send(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        scope.cancel()
    }
}

// Server-Sent Events (SSE) with OkHttp
class SseClient(private val okHttpClient: OkHttpClient) {

    fun observeEvents(url: String): Flow<SseEvent> = callbackFlow {
        val request = Request.Builder()
            .url(url)
            .header("Accept", "text/event-stream")
            .build()

        val call = okHttpClient.newCall(request)
        val response = call.execute()
        val source = response.body?.source() ?: run {
            close()
            return@callbackFlow
        }

        var eventType = ""
        val dataBuilder = StringBuilder()

        try {
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                when {
                    line.startsWith("event:") -> eventType = line.removePrefix("event:").trim()
                    line.startsWith("data:") -> dataBuilder.append(line.removePrefix("data:").trim())
                    line.isEmpty() && dataBuilder.isNotEmpty() -> {
                        trySend(SseEvent(eventType, dataBuilder.toString()))
                        eventType = ""
                        dataBuilder.clear()
                    }
                }
            }
        } finally {
            response.close()
        }

        awaitClose { call.cancel() }
    }
}

data class SseEvent(val event: String, val data: String)


// ─────────────────────────────────────────────────────────────────────────────
// 9. REMOTEMEDIATOR (OFFLINE-FIRST PAGING) — SUMMARY
// ─────────────────────────────────────────────────────────────────────────────

/**
 * RemoteMediator = bridge between Paging 3 and a remote data source.
 * It loads data from the network and caches it in Room.
 * PagingSource then reads from Room (single source of truth).
 *
 * Flow:
 *   LazyColumn requests page → Pager asks PagingSource (Room)
 *     → if Room has data → display
 *     → if Room exhausted → RemoteMediator.load() called
 *       → fetches from API → inserts into Room → PagingSource emits new data
 *
 * 💡 Interview Tip: This is the Google-recommended pattern for offline-first
 *    paginated lists. Know the full flow end-to-end — it's a very common
 *    SDE-2 system design question.
 *
 * ⚠️ Key: The DB transaction in load() must be atomic — delete old data
 *    (on REFRESH) and insert new data in a single transaction.
 *
 * → See 05_data_persistence.kt § "PAGING 3 + ROOM" for the full
 *   RemoteMediator implementation, RemoteKey entity, DAO, Pager wiring,
 *   and Compose UI with LazyPagingItems.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 10. API DESIGN BEST PRACTICES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * REST Versioning:
 *   - URL versioning: /api/v1/articles  (most common, Google uses this)
 *   - Header versioning: Accept: application/vnd.api+json;version=1
 *   - Query param: /api/articles?version=1
 *
 * Pagination Contracts:
 *
 *   Offset-based: ?page=3&limit=20
 *     ✅ Simple, supports random page access
 *     ❌ Inconsistent when data changes (duplicates/gaps on insert/delete)
 *
 *   Cursor-based: ?cursor=abc123&limit=20
 *     ✅ Consistent results even with data changes
 *     ✅ Better performance (no OFFSET scan on DB side)
 *     ❌ No random page access, forward-only (or with prev_cursor)
 *     💡 Google/Twitter/Slack all use cursor-based pagination
 *
 * Error Envelope Pattern:
 *   Consistent error structure across all endpoints:
 */

// Standardized error envelope
@Serializable
data class ApiErrorEnvelope(
    val error: ApiErrorDetail
)

@Serializable
data class ApiErrorDetail(
    val code: String,             // Machine-readable: "ARTICLE_NOT_FOUND"
    val message: String,          // Human-readable: "The article was not found"
    val target: String? = null,   // Which field caused the error
    val details: List<ApiErrorDetail>? = null  // Nested validation errors
)

/**
 * Idempotency Keys:
 *   Problem: network timeout on POST → did the server process it?
 *   Solution: client sends a unique key; server deduplicates.
 *
 *   Client sends: POST /orders  Header: Idempotency-Key: <UUID>
 *   Server: if key seen before → return cached response (no duplicate order)
 *           if key is new → process and store response keyed by Idempotency-Key
 *
 *   ⚠️ Critical for payment, order creation, any non-idempotent mutation.
 *
 * gRPC (relevant at Google):
 *   - Protocol Buffers (protobuf) for serialization — smaller, faster than JSON
 *   - HTTP/2 based — multiplexing, header compression, bidirectional streaming
 *   - Strongly typed service contracts (.proto files)
 *   - Google uses gRPC extensively for internal microservice communication
 *   - On Android: use grpc-kotlin for coroutine-based stubs
 *
 * 💡 Interview Tip: Mentioning gRPC in a Google interview shows awareness of
 *    their tech stack. Know basic trade-offs vs REST (binary vs text, streaming
 *    support, tooling maturity, browser support).
 */


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: What's the difference between Application and Network interceptors?
 *    A: Application interceptors: called once, see original request, can short-circuit.
 *       Network interceptors: called per network request (including redirects),
 *       can access Connection, see request after OkHttp modifications.
 *
 * ❓ Q2: How do you handle token refresh in a multi-threaded environment?
 *    A: Use OkHttp Authenticator or synchronize the refresh.
 *       With an interceptor: check 401, synchronize refresh (Mutex or synchronized),
 *       check if another thread already refreshed, retry with new token.
 *
 * ❓ Q3: How would you implement offline-first with Paging 3?
 *    A: Use RemoteMediator. It fetches from API, stores in Room.
 *       PagingSource reads from Room. UI always sees local data.
 *       RemoteMediator handles network fetch + DB upsert.
 *
 * ❓ Q4: What is ETag caching?
 *    A: Server sends ETag header with response. Client sends If-None-Match
 *       on next request. If data hasn't changed, server returns 304 (Not Modified),
 *       client uses cached version. Saves bandwidth, reduces server load.
 *
 * ❓ Q5: Retrofit with coroutines — what happens on network error?
 *    A: If the function returns Response<T>, you get a Response object (check isSuccessful).
 *       If it returns T directly, network errors throw IOException/HttpException.
 *       Always wrap in try-catch or safeApiCall.
 *
 * ❓ Q6: When would you choose WebSocket vs SSE vs polling?
 *    A: WebSocket: bidirectional real-time (chat, multiplayer). Full-duplex,
 *       binary + text support, but requires explicit reconnection logic.
 *       SSE: server-push only (live feeds, notifications). Built-in reconnect,
 *       works through HTTP proxies, text-only. Simpler than WebSocket.
 *       Polling: simplest, works everywhere, highest latency. Use for
 *       low-frequency updates where real-time isn't critical.
 *
 * ❓ Q7: Walk through how RemoteMediator coordinates network and local cache.
 *    A: Pager reads from Room via PagingSource. When Room data is exhausted,
 *       Pager calls RemoteMediator.load(). RemoteMediator fetches the next
 *       page from the API, then in a single DB transaction inserts the new
 *       data and remote keys into Room. Room's PagingSource is invalidated
 *       automatically (via InvalidationTracker), emitting the new page to the UI.
 *       On REFRESH, old data and keys are cleared before inserting.
 *       initialize() can skip refresh if cache is fresh (e.g., < 1 hour old).
 */
