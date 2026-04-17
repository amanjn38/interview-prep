/**
 * ============================================================================
 * 15 — SYSTEM DESIGN FOR ANDROID
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. System Design Framework (6-Step Approach)
 *   2. Design: Offline-First Notes App
 *   3. Design: Image Loading Library
 *   4. Design: Chat/Messaging App
 *   5. Design: News Feed (Instagram-like)
 *   6. Design: Analytics SDK
 *   7. Design: File Downloader Library
 *   8. Design: Observability / Logging SDK
 *   9. Common Mobile-Specific Constraints
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. SYSTEM DESIGN FRAMEWORK — 6-STEP APPROACH
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Step 1: REQUIREMENTS FRAMING (5 min)
 *   Functional Requirements:
 *     - What can the user do? (CRUD, search, share, etc.)
 *     - What are the core screens/flows?
 *   Non-Functional Requirements:
 *     - Offline support?
 *     - Real-time updates?
 *     - Performance targets? (startup < 500ms, scroll 60fps)
 *     - Scale? (DAU, data volume)
 *     - Security requirements?
 *
 * Step 2: SCALE ESTIMATION (2 min)
 *   - Back-of-envelope calculations
 *   - DAU → concurrent users → requests/second
 *   - Data size per user → total storage → cache budget
 *   - Network bandwidth requirements
 *
 * Step 3: HIGH-LEVEL ARCHITECTURE (5 min)
 *   - Name your pattern and justify it in one sentence
 *   - Draw the layer diagram (UI → ViewModel → Repository → DataSources)
 *   - Identify major components
 *
 * Step 4: COMPONENT WALKTHROUGH (15 min)
 *   - Detail each major component
 *   - Data models and their relationships
 *   - API contract (request/response)
 *   - Caching strategy
 *   - Sync mechanism
 *
 * Step 5: FAILURE MODE ANALYSIS (5 min)
 *   - What happens when network drops?
 *   - What happens on process death?
 *   - What happens with conflicting edits?
 *   - Auth token expiration mid-request?
 *   - Database migration failure?
 *
 * Step 6: TRADE-OFFS & ALTERNATIVES (3 min)
 *   - What would you do differently at 10x scale?
 *   - Alternatives considered and why you rejected them
 *   - What technical debt does this design accept?
 */


// ─────────────────────────────────────────────────────────────────────────────
// 2. DESIGN: OFFLINE-FIRST NOTES APP (Google Keep-like)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Requirements:
 *   FR: Create, edit, delete notes. Rich text, images. Search. Labels/categories.
 *   NFR: Full offline support. Cross-device sync. Conflict resolution.
 *
 * Architecture: MVVM + Offline-First (Room as SSOT)
 *
 *   ┌─────────────┐
 *   │  Compose UI  │  ← observes StateFlow
 *   └──────┬───────┘
 *          │ events
 *   ┌──────▼───────┐
 *   │  ViewModel   │  ← processes intents, exposes state
 *   └──────┬───────┘
 *          │
 *   ┌──────▼───────┐
 *   │  Repository   │  ← coordinates sync, provides data
 *   └──┬────────┬───┘
 *      │        │
 *  ┌───▼──┐ ┌──▼────────┐
 *  │ Room │ │ Sync Engine │
 *  │(SSOT)│ │ (WorkMgr)  │
 *  └──────┘ └────────────┘
 */

// Data model with sync metadata
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val labelIds: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Sync metadata
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val serverVersion: Long = 0,
    val localVersion: Long = 1,
    val isDeleted: Boolean = false  // Soft delete for sync
)

enum class SyncStatus { SYNCED, PENDING, CONFLICT }

// Conflict resolution strategy
class ConflictResolver {
    /**
     * Conflict Resolution Strategies:
     *
     * 1. Last-Write-Wins (LWW):
     *    - Simple: higher timestamp wins
     *    - Risk: silent data loss
     *
     * 2. Server-Wins:
     *    - Server version always takes priority
     *    - Client changes may be lost
     *
     * 3. Client-Wins:
     *    - Local version always takes priority
     *    - Other devices' changes may be lost
     *
     * 4. Merge (recommended for text):
     *    - 3-way merge using common ancestor
     *    - If auto-merge fails, show both versions to user
     *
     * For Google interview: discuss trade-offs of each approach.
     */
    fun resolve(local: NoteEntity, remote: NoteEntity, ancestor: NoteEntity?): NoteEntity {
        // Field-level merge strategy
        return NoteEntity(
            id = local.id,
            title = if (local.title != ancestor?.title) local.title else remote.title,
            content = mergeContent(local.content, remote.content, ancestor?.content),
            updatedAt = maxOf(local.updatedAt, remote.updatedAt),
            serverVersion = remote.serverVersion,
            localVersion = local.localVersion + 1,
            syncStatus = SyncStatus.SYNCED
        )
    }

    private fun mergeContent(local: String, remote: String, ancestor: String?): String {
        // Simplified — real implementation would use OT or CRDT
        if (ancestor == null) return local
        if (local == ancestor) return remote
        if (remote == ancestor) return local
        // Both changed — conflict, keep both
        return "$local\n---CONFLICT---\n$remote"
    }
}

// Sync engine using WorkManager
class NoteSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val noteDao: NoteDao,
    private val api: NoteApi,
    private val conflictResolver: ConflictResolver
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Push local changes
            val pendingNotes = noteDao.getPendingSync()
            for (note in pendingNotes) {
                val result = api.upsertNote(note.toRequest())
                when {
                    result.isSuccess -> {
                        noteDao.update(note.copy(
                            syncStatus = SyncStatus.SYNCED,
                            serverVersion = result.getOrThrow().version
                        ))
                    }
                    result.isConflict -> {
                        val serverNote = api.getNote(note.id).getOrThrow()
                        val resolved = conflictResolver.resolve(note, serverNote.toEntity(), null)
                        noteDao.update(resolved)
                    }
                }
            }

            // 2. Pull remote changes
            val lastSyncTime = noteDao.getLastSyncTime()
            val remoteChanges = api.getChanges(since = lastSyncTime)
            for (remote in remoteChanges) {
                val local = noteDao.getById(remote.id)
                if (local == null || local.syncStatus == SyncStatus.SYNCED) {
                    noteDao.upsert(remote.toEntity().copy(syncStatus = SyncStatus.SYNCED))
                }
                // If local has pending changes, skip — will resolve on next push
            }

            Result.success()
        } catch (e: IOException) {
            if (runAttemptCount < 5) Result.retry() else Result.failure()
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 3. DESIGN: IMAGE LOADING LIBRARY (Coil/Glide-like)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Requirements:
 *   - Load images from URL into ImageView/Compose
 *   - Memory cache (LRU) + Disk cache
 *   - Transformations (resize, crop, round corners)
 *   - Placeholder and error images
 *   - Lifecycle-aware (cancel on destroy)
 *   - Request deduplication
 *
 * Architecture:
 *
 *   ImageLoader.load(url)
 *       → Check Memory Cache (LruCache)
 *           → Hit: return Bitmap immediately
 *           → Miss: Check Disk Cache
 *               → Hit: decode, store in memory, return
 *               → Miss: Download from network
 *                   → Decode, apply transformations
 *                   → Store in disk cache + memory cache
 *                   → Return to caller
 */

class SimpleImageLoader(context: Context) {

    // Memory cache: ~1/8 of available heap
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    // Disk cache
    private val diskCache = DiskLruCache.open(
        File(context.cacheDir, "image_cache"),
        1, 1, 100L * 1024 * 1024  // 100 MB
    )

    // Request deduplication
    private val inflightRequests = ConcurrentHashMap<String, Deferred<Bitmap?>>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun load(
        url: String,
        width: Int = 0,
        height: Int = 0
    ): Bitmap? {
        val cacheKey = generateKey(url, width, height)

        // 1. Memory cache check
        memoryCache.get(cacheKey)?.let { return it }

        // 2. Deduplicate concurrent requests for same image
        inflightRequests[cacheKey]?.let {
            return it.await()
        }

        val deferred = scope.async {
            try {
                // 3. Disk cache check
                val diskBitmap = loadFromDisk(cacheKey)
                if (diskBitmap != null) {
                    memoryCache.put(cacheKey, diskBitmap)
                    return@async diskBitmap
                }

                // 4. Network download
                val networkBitmap = downloadAndDecode(url, width, height)
                if (networkBitmap != null) {
                    memoryCache.put(cacheKey, networkBitmap)
                    saveToDisk(cacheKey, networkBitmap)
                }
                networkBitmap
            } finally {
                inflightRequests.remove(cacheKey)
            }
        }
        inflightRequests[cacheKey] = deferred

        return deferred.await()
    }

    private fun generateKey(url: String, width: Int, height: Int): String {
        return "${url}_${width}x${height}".hashCode().toString()
    }

    private fun downloadAndDecode(url: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        // Download → sample decode → resize
        return null // Implementation
    }

    private fun loadFromDisk(key: String): Bitmap? = null // Implementation
    private fun saveToDisk(key: String, bitmap: Bitmap) {} // Implementation
}

/**
 * Key Discussion Points:
 *
 * 1. Memory pressure: LruCache eviction, respond to onTrimMemory()
 * 2. Disk cache: DiskLruCache with journaling for crash safety
 * 3. Sampling: BitmapFactory.Options.inSampleSize to decode at target size
 * 4. Lifecycle: cancel requests when target View is recycled/detached
 * 5. Bitmap pooling: reuse Bitmap memory (inBitmap) to reduce GC
 * 6. Thread pool: dedicated for decoding (CPU-bound), separate for network (IO)
 */


// ─────────────────────────────────────────────────────────────────────────────
// 4. DESIGN: CHAT/MESSAGING APP
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Requirements:
 *   FR: Send/receive text messages, images. Group chats. Read receipts.
 *       Typing indicators. Push notifications.
 *   NFR: Real-time delivery. Offline message queueing. E2E encryption.
 *        Message ordering guaranteed.
 *
 * Architecture:
 *
 *   ┌─────────────────┐
 *   │   Compose UI     │  ← LazyColumn with pagination
 *   └────────┬─────────┘
 *            │
 *   ┌────────▼─────────┐
 *   │   ViewModel      │  ← UI state, send/receive actions
 *   └────────┬─────────┘
 *            │
 *   ┌────────▼─────────┐
 *   │  ChatRepository   │  ← coordinates local + remote
 *   └───┬──────────┬────┘
 *       │          │
 *   ┌───▼──┐  ┌───▼──────────────┐
 *   │ Room │  │ WebSocket Client  │
 *   │(msgs)│  │ (real-time)       │
 *   └──────┘  └──────────────────┘
 *                     ↕
 *             ┌───────────────┐
 *             │  FCM (push)   │  ← when app is backgrounded
 *             └───────────────┘
 *
 * Message Flow:
 *   Send: UI → ViewModel → Repository → Room (PENDING) → WebSocket
 *              → Server ACK → Room (SENT) → Server delivers → Room (DELIVERED)
 *              → Recipient reads → Room (READ)
 *
 *   Receive: WebSocket → Repository → Room → Flow → ViewModel → UI
 *            (or FCM → WorkManager → Repository → Room when backgrounded)
 */

// Message data model
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val chatId: String,
    val senderId: String,
    val content: String,
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val serverTimestamp: Long? = null,
    val localSequence: Long = 0  // For ordering before server assigns timestamp
)

enum class MessageType { TEXT, IMAGE, VIDEO, AUDIO, FILE }
enum class MessageStatus { PENDING, SENT, DELIVERED, READ, FAILED }

// WebSocket connection management
class ChatWebSocketClient(
    private val okHttpClient: OkHttpClient,
    private val messageDao: MessageDao,
    private val scope: CoroutineScope
) {
    private var webSocket: WebSocket? = null
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<ServerMessage>(
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingMessages: SharedFlow<ServerMessage> = _incomingMessages.asSharedFlow()

    fun connect(authToken: String) {
        val request = Request.Builder()
            .url("wss://chat.example.com/ws")
            .header("Authorization", "Bearer $authToken")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.Connected
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val message = Json.decodeFromString<ServerMessage>(text)
                scope.launch { _incomingMessages.emit(message) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.Error(t)
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.Disconnected
            }
        })
    }

    private fun scheduleReconnect() {
        scope.launch {
            var delay = 1000L
            while (_connectionState.value !is ConnectionState.Connected) {
                delay(delay)
                delay = minOf(delay * 2, 30_000)  // Exponential backoff, max 30s
                connect(getStoredToken())
            }
        }
    }

    fun send(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
    }
}

sealed interface ConnectionState {
    data object Connected : ConnectionState
    data object Connecting : ConnectionState
    data object Disconnected : ConnectionState
    data class Error(val throwable: Throwable) : ConnectionState
}

/**
 * Key Design Decisions to Discuss:
 *
 * 1. Message Ordering: Use server-assigned timestamps for final ordering,
 *    local sequence numbers for optimistic display.
 *
 * 2. Offline Queue: Messages saved to Room with PENDING status.
 *    When online, WorkManager or WebSocket reconnect sends pending messages.
 *
 * 3. Pagination: Load messages from Room with Paging 3.
 *    Older messages fetched from API and cached in Room.
 *
 * 4. Push Notifications: FCM for when WebSocket is disconnected.
 *    Data message (not notification message) for custom handling.
 *
 * 5. E2E Encryption: Signal Protocol (libsignal).
 *    Keys stored in Android Keystore. Key exchange on first contact.
 *
 * 6. Reconnection: Exponential backoff with jitter.
 *    On reconnect, fetch missed messages since last known timestamp.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 5. DESIGN: NEWS FEED
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Requirements:
 *   - Scrollable feed of posts (text, images, video)
 *   - Pull-to-refresh
 *   - Infinite scroll with pagination
 *   - Like, comment, share actions
 *   - Efficient image/video loading
 *   - Offline reading of cached content
 *
 * Key Components:
 *
 *   1. Feed ViewModel:
 *      - Paging 3 with RemoteMediator
 *      - Room as cache, API as remote source
 *
 *   2. Image Loading:
 *      - Coil with memory + disk cache
 *      - Placeholder → low-res thumbnail → full image
 *
 *   3. Video:
 *      - ExoPlayer with preloading
 *      - Auto-play when >50% visible
 *      - Pause when scrolled away
 *
 *   4. RecyclerView/LazyColumn Optimization:
 *      - Stable keys for items
 *      - DiffUtil for minimal updates
 *      - View recycling across view types
 *      - Prefetch for smooth scrolling
 *
 *   5. Real-time Updates:
 *      - New post indicator (don't auto-scroll)
 *      - Like count updates via WebSocket/SSE
 *      - Optimistic UI updates for user actions
 */


// ─────────────────────────────────────────────────────────────────────────────
// 6. DESIGN: ANALYTICS SDK
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Requirements:
 *   - Track events (screen views, clicks, custom events)
 *   - Batch and send periodically (minimize network calls)
 *   - Persist events if network unavailable
 *   - Minimal performance impact
 *   - Thread-safe
 *   - Battery-friendly
 *
 * Architecture:
 *   Event → Queue (in-memory) → Batcher → Disk (if threshold/timer)
 *         → WorkManager → API upload → Clear from disk
 */

class AnalyticsSDK private constructor(private val context: Context) {

    private val eventQueue = Channel<AnalyticsEvent>(Channel.UNLIMITED)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val db: AnalyticsDatabase by lazy { AnalyticsDatabase.create(context) }

    init {
        // Consumer: batch events and persist
        scope.launch {
            val batch = mutableListOf<AnalyticsEvent>()
            for (event in eventQueue) {
                batch.add(event)
                if (batch.size >= BATCH_SIZE) {
                    persistBatch(batch.toList())
                    batch.clear()
                    scheduleUpload()
                }
            }
        }

        // Periodic flush
        scope.launch {
            while (true) {
                delay(FLUSH_INTERVAL_MS)
                flushPendingEvents()
            }
        }
    }

    fun track(eventName: String, properties: Map<String, Any> = emptyMap()) {
        val event = AnalyticsEvent(
            name = eventName,
            properties = properties,
            timestamp = System.currentTimeMillis(),
            sessionId = SessionManager.currentSessionId
        )
        eventQueue.trySend(event)
    }

    private suspend fun persistBatch(events: List<AnalyticsEvent>) {
        db.eventDao().insertAll(events.map { it.toEntity() })
    }

    private fun scheduleUpload() {
        val uploadWork = OneTimeWorkRequestBuilder<AnalyticsUploadWorker>()
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("analytics_upload", ExistingWorkPolicy.KEEP, uploadWork)
    }

    companion object {
        private const val BATCH_SIZE = 50
        private const val FLUSH_INTERVAL_MS = 30_000L

        @Volatile private var instance: AnalyticsSDK? = null

        fun init(context: Context): AnalyticsSDK {
            return instance ?: synchronized(this) {
                instance ?: AnalyticsSDK(context.applicationContext).also { instance = it }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 7. DESIGN: FILE DOWNLOADER LIBRARY
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Requirements:
 *   - Download files from URL with pause/resume support
 *   - Progress reporting per download
 *   - Multiple parallel downloads with queue management
 *   - Survive process death and device reboot
 *   - Retry on failure with backoff
 *   - Notification for each active download
 *
 * Architecture:
 *   UI → DownloadManager API → Room (state) → WorkManager (execution)
 *       → OkHttp with Range headers → Disk
 *
 *   Room stores: URL, file path, status, bytes downloaded, total bytes
 *   WorkManager: one worker per download, unique work by URL
 *   OkHttp: HTTP Range header for resume support
 *   Flow: progress updates from worker → UI
 */

// Download state persisted in Room
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val url: String,
    val fileName: String,
    val destinationPath: String,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = -1,  // -1 = unknown until server responds
    val createdAt: Long = System.currentTimeMillis(),
    val error: String? = null,
    val retryCount: Int = 0
)

enum class DownloadStatus { QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED }

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getById(id: String): DownloadEntity?

    @Upsert
    suspend fun upsert(download: DownloadEntity)

    @Query("UPDATE downloads SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: DownloadStatus)

    @Query("UPDATE downloads SET bytesDownloaded = :bytes WHERE id = :id")
    suspend fun updateProgress(id: String, bytes: Long)
}

// Download worker with resume support via HTTP Range headers
class FileDownloadWorker(
    context: Context,
    params: WorkerParameters,
    private val downloadDao: DownloadDao,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val downloadId = inputData.getString("download_id") ?: return Result.failure()
        val download = downloadDao.getById(downloadId) ?: return Result.failure()

        downloadDao.updateStatus(downloadId, DownloadStatus.DOWNLOADING)

        return try {
            val file = File(download.destinationPath)
            val existingBytes = if (file.exists()) file.length() else 0L

            // Range header for resume support
            val request = Request.Builder()
                .url(download.url)
                .apply {
                    if (existingBytes > 0) {
                        addHeader("Range", "bytes=$existingBytes-")
                    }
                }
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful && response.code != 206) {
                    throw IOException("Unexpected code: ${response.code}")
                }

                val totalBytes = if (response.code == 206) {
                    existingBytes + (response.body?.contentLength() ?: 0)
                } else {
                    response.body?.contentLength() ?: -1
                }

                downloadDao.upsert(download.copy(totalBytes = totalBytes))

                val sink = file.appendingSink().buffer()
                val source = response.body?.source() ?: throw IOException("Empty body")

                var bytesRead = existingBytes
                val buffer = ByteArray(8192)
                var read: Int

                while (source.read(buffer).also { read = it.toInt() } != -1L) {
                    sink.write(buffer, 0, read)
                    bytesRead += read

                    // Report progress
                    downloadDao.updateProgress(downloadId, bytesRead)
                    setProgress(workDataOf(
                        "progress" to bytesRead,
                        "total" to totalBytes
                    ))
                }
                sink.flush()
                sink.close()
            }

            downloadDao.updateStatus(downloadId, DownloadStatus.COMPLETED)
            Result.success()
        } catch (e: IOException) {
            if (runAttemptCount < 3) {
                downloadDao.updateStatus(downloadId, DownloadStatus.QUEUED)
                Result.retry()
            } else {
                downloadDao.upsert(download.copy(
                    status = DownloadStatus.FAILED,
                    error = e.message
                ))
                Result.failure()
            }
        }
    }
}

/**
 * Key Discussion Points:
 *
 * 1. Resume: HTTP Range header + append to existing file. Server must support
 *    206 Partial Content. If not, restart from scratch.
 *
 * 2. Parallel downloads: Each download is a separate WorkManager unique work.
 *    Limit concurrency with a custom Configuration (max workers).
 *
 * 3. Queue management: Room tracks all downloads. UI observes via Flow.
 *    Priority queue can be implemented with a priority field in entity.
 *
 * 4. Pause/Resume: Pause = cancel the worker + save bytesDownloaded in Room.
 *    Resume = re-enqueue work, worker reads bytesDownloaded and sets Range header.
 *
 * 5. Notifications: setForeground(ForegroundInfo) in CoroutineWorker
 *    for ongoing notification with progress bar.
 *
 * 6. Integrity: After download, verify checksum (MD5/SHA-256) if server provides one.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 8. DESIGN: OBSERVABILITY / LOGGING SDK
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Requirements:
 *   - Structured logging (tag, level, message, metadata)
 *   - Crash reporting (uncaught exception handler)
 *   - Performance metrics (startup time, frame drops, network latency)
 *   - Batched upload to backend (minimize network calls)
 *   - Persist logs to disk if network unavailable
 *   - Minimal impact on app performance and battery
 *
 * Architecture:
 *   App code → Logger API → In-memory ring buffer → Flush to disk
 *             → WorkManager → Batch upload to backend
 *             → Crash handler → Immediate disk write → Upload on next launch
 *
 * Log levels: VERBOSE, DEBUG, INFO, WARN, ERROR, FATAL
 *
 * Sampling:
 *   - DEBUG/VERBOSE: only in debug builds (never ship)
 *   - INFO: sample at 10-50% of sessions to reduce volume
 *   - WARN/ERROR: always log, always upload
 *   - FATAL (crash): always log, persist immediately, upload on next launch
 *
 * Privacy considerations:
 *   - NEVER log PII (emails, passwords, tokens, phone numbers)
 *   - Strip sensitive fields before logging
 *   - Allow users to opt out of analytics/logging
 *   - Comply with GDPR/CCPA — include session ID, not user ID, unless consented
 *
 * Integration with existing tools:
 *   - Firebase Crashlytics: crash reports + non-fatals, auto stack deobfuscation
 *   - Play Console Android Vitals: ANRs, crash rates, startup time, battery
 *   - Perfetto: system-level traces for debugging performance
 *   - Custom backend: for proprietary metrics and business-specific logging
 */

data class LogEntry(
    val level: LogLevel,
    val tag: String,
    val message: String,
    val metadata: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String,
    val threadName: String = Thread.currentThread().name,
    val stackTrace: String? = null
)

enum class LogLevel { VERBOSE, DEBUG, INFO, WARN, ERROR, FATAL }

class ObservabilitySDK private constructor(private val context: Context) {

    private val buffer = ArrayDeque<LogEntry>(BUFFER_CAPACITY)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val sessionId = UUID.randomUUID().toString()

    fun log(level: LogLevel, tag: String, message: String, metadata: Map<String, String> = emptyMap()) {
        if (level.ordinal < minLogLevel.ordinal) return

        val entry = LogEntry(
            level = level,
            tag = tag,
            message = message,
            metadata = metadata,
            sessionId = sessionId
        )

        synchronized(buffer) {
            buffer.addLast(entry)
            if (buffer.size >= FLUSH_THRESHOLD) {
                flushBufferToDisk()
            }
        }
    }

    fun installCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            log(
                level = LogLevel.FATAL,
                tag = "CRASH",
                message = throwable.message ?: "Unknown crash",
                metadata = mapOf("thread" to thread.name)
            )
            // Flush immediately — process is about to die
            flushBufferToDisk()
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun flushBufferToDisk() {
        val entries: List<LogEntry>
        synchronized(buffer) {
            entries = buffer.toList()
            buffer.clear()
        }
        scope.launch {
            // Persist to local file or Room, then schedule upload
            persistEntries(entries)
            scheduleUpload()
        }
    }

    private suspend fun persistEntries(entries: List<LogEntry>) { /* Write to disk */ }

    private fun scheduleUpload() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "log_upload",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<LogUploadWorker>()
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
                .build()
        )
    }

    companion object {
        private const val BUFFER_CAPACITY = 500
        private const val FLUSH_THRESHOLD = 100
        private var minLogLevel = LogLevel.INFO
    }
}

/**
 * Key Discussion Points:
 *
 * 1. Ring buffer: Fixed-size in-memory buffer avoids OOM.
 *    Oldest entries evicted if not flushed fast enough.
 *
 * 2. Crash reporting: Install UncaughtExceptionHandler, flush synchronously,
 *    then delegate to original handler. Upload on next app launch.
 *
 * 3. Performance metrics: Track via FrameMetrics API (API 24+) for jank,
 *    reportFullyDrawn() for startup. Send as structured log entries.
 *
 * 4. Batched upload: Reduces battery and network usage. Compress (gzip) before upload.
 *    Back-pressure: if disk queue grows too large, drop lowest-severity entries.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 9. COMMON MOBILE-SPECIFIC CONSTRAINTS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Always discuss these in mobile system design:
 *
 * 1. Network: Hostile environment — drops, slow, metered
 *    → Offline-first, retry with backoff, request deduplication
 *
 * 2. Battery: Background work is heavily restricted
 *    → WorkManager, batch operations, avoid polling
 *
 * 3. Memory: Limited heap (256MB-512MB typical)
 *    → LRU caches, bitmap sampling, leak detection
 *
 * 4. Storage: Users care about app size
 *    → On-demand resources, aggressive caching policy with eviction
 *
 * 5. Process Death: App can be killed at any time
 *    → Restore state from SavedStateHandle + persistent storage
 *
 * 6. Security: Device can be rooted, APK can be decompiled
 *    → Keystore for secrets, cert pinning, obfuscation
 *
 * 7. Lifecycle: Screens can be created/destroyed unpredictably
 *    → Lifecycle-aware components, scoped work
 *
 * 8. Fragmentation: Thousands of device configs
 *    → Responsive layouts, API level checks, Compat libraries
 */


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: How do you handle sync conflicts in an offline-first app?
 *    A: Depends on the data type:
 *       - Last-Write-Wins for simple fields (user profile)
 *       - 3-way merge for text (notes, documents)
 *       - CRDTs for concurrent data structures (counters, sets)
 *       - Manual resolution (show both versions to user)
 *       Always discuss: What's the cost of data loss vs. complexity?
 *
 * ❓ Q2: Design a file download manager with pause/resume support.
 *    A: Room tracks download state (URL, progress, status, temp file path).
 *       OkHttp with Range headers for resume.
 *       WorkManager for background execution.
 *       CoroutineWorker with progress reporting.
 *       Notification for progress. Unique work per URL.
 *
 * ❓ Q3: How would you implement optimistic UI updates?
 *    A: Update local state immediately (Room + StateFlow).
 *       Send request to server in background.
 *       On success: update sync status.
 *       On failure: revert local state, show error.
 *       Example: Like button — show liked immediately, sync later.
 *
 * ❓ Q4: How do you handle token expiration in a WebSocket connection?
 *    A: Server sends a "token_expired" message.
 *       Client refreshes token, reconnects with new token.
 *       During refresh, queue outgoing messages.
 *       After reconnect, replay pending messages + fetch missed messages
 *       using last known server timestamp.
 */
