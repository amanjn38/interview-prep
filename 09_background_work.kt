/**
 * ============================================================================
 * 09 — BACKGROUND WORK
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Choosing the Right API (Decision Framework)
 *   2. WorkManager — Persistent & Deferrable Work
 *   3. Foreground Services
 *   4. Broadcast Receivers
 *   5. Background Execution Limits
 *   6. AlarmManager
 *   7. Firebase Cloud Messaging (FCM)
 *   8. Foreground Service Launch Restrictions (API 31+)
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. CHOOSING THE RIGHT BACKGROUND API
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Decision Framework:
 *
 * ┌──────────────────────────────┬───────────────────────────────────────────┐
 * │ Requirement                  │ API to Use                                │
 * ├──────────────────────────────┼───────────────────────────────────────────┤
 * │ Must complete even if app    │ WorkManager                               │
 * │ closes or device restarts    │                                           │
 * ├──────────────────────────────┼───────────────────────────────────────────┤
 * │ Needs immediate execution    │ Coroutines (viewModelScope)               │
 * │ while user is in app         │                                           │
 * ├──────────────────────────────┼───────────────────────────────────────────┤
 * │ User-initiated long task     │ Foreground Service                        │
 * │ (music, navigation, upload)  │                                           │
 * ├──────────────────────────────┼───────────────────────────────────────────┤
 * │ Exact timing required        │ AlarmManager (setExactAndAllowWhileIdle)  │
 * │ (reminders, alarms)          │                                           │
 * ├──────────────────────────────┼───────────────────────────────────────────┤
 * │ React to system events       │ BroadcastReceiver                         │
 * │ (boot, connectivity)         │                                           │
 * ├──────────────────────────────┼───────────────────────────────────────────┤
 * │ Periodic background sync     │ WorkManager (PeriodicWorkRequest)         │
 * │                              │                                           │
 * └──────────────────────────────┴───────────────────────────────────────────┘
 *
 * 💡 Default answer: WorkManager for almost all background work.
 *    It handles Doze, battery optimization, and guarantees execution.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 2. WORKMANAGER — PERSISTENT & DEFERRABLE WORK
// ─────────────────────────────────────────────────────────────────────────────

/**
 * WorkManager features:
 *   ✅ Survives app restart and device reboot
 *   ✅ Constraint-based execution (network, battery, charging)
 *   ✅ Chaining (sequential and parallel work)
 *   ✅ Exponential backoff for retries
 *   ✅ Input/Output data passing
 *   ✅ Unique work (prevent duplicates)
 *   ✅ Expedited work (runs immediately, even under Doze)
 *   ✅ Observable work status via LiveData/Flow
 *
 * Under the hood: Uses JobScheduler (API 23+) or AlarmManager + BroadcastReceiver.
 */

// Simple Worker
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString("USER_ID") ?: return Result.failure()

        return try {
            val api = ApiService.create()
            val localDb = AppDatabase.getInstance(applicationContext)

            // Fetch from API
            val articles = api.getArticles(userId)

            // Store locally
            localDb.articleDao().upsertAll(articles.map { it.toEntity() })

            // Pass output data to next worker in chain
            val outputData = workDataOf(
                "SYNC_COUNT" to articles.size,
                "LAST_SYNC" to System.currentTimeMillis()
            )
            Result.success(outputData)

        } catch (e: IOException) {
            if (runAttemptCount < 3) {
                Result.retry()  // Exponential backoff
            } else {
                Result.failure(workDataOf("ERROR" to e.message))
            }
        } catch (e: Exception) {
            Result.failure(workDataOf("ERROR" to e.message))
        }
    }

    // Show notification for long-running work (API 31+ requires this for expedited)
    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, "sync_channel")
            .setContentTitle("Syncing articles...")
            .setSmallIcon(R.drawable.ic_sync)
            .setProgress(0, 0, true)
            .build()
        return ForegroundInfo(SYNC_NOTIFICATION_ID, notification)
    }

    companion object {
        const val SYNC_NOTIFICATION_ID = 1001
    }
}

// Scheduling Work
class WorkScheduler(private val context: Context) {

    // One-time work with constraints
    fun scheduleSync(userId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val inputData = workDataOf("USER_ID" to userId)

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("sync")
            .build()

        // KEEP: if existing work is running, keep it. Don't enqueue duplicate.
        WorkManager.getInstance(context)
            .enqueueUniqueWork("article_sync", ExistingWorkPolicy.KEEP, syncRequest)
    }

    // Periodic work
    fun schedulePeriodicSync() {
        val periodicSync = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 1, TimeUnit.HOURS,
            flexInterval = 15, TimeUnit.MINUTES  // Window for execution
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED) // Wi-Fi only
                    .build()
            )
            .addTag("periodic_sync")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "periodic_article_sync",
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicSync
            )
    }

    // Chaining workers
    fun scheduleChainedWork() {
        val downloadWork = OneTimeWorkRequestBuilder<DownloadWorker>().build()
        val processWork = OneTimeWorkRequestBuilder<ProcessWorker>().build()
        val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>().build()

        // Sequential chain: download → process → upload
        WorkManager.getInstance(context)
            .beginWith(downloadWork)
            .then(processWork)
            .then(uploadWork)
            .enqueue()
    }

    // Parallel then sequential
    fun scheduleParallelWork() {
        val fetchArticles = OneTimeWorkRequestBuilder<FetchArticlesWorker>().build()
        val fetchUsers = OneTimeWorkRequestBuilder<FetchUsersWorker>().build()
        val mergeWork = OneTimeWorkRequestBuilder<MergeDataWorker>().build()

        // fetchArticles + fetchUsers run in PARALLEL, then mergeWork runs
        WorkManager.getInstance(context)
            .beginWith(listOf(fetchArticles, fetchUsers))
            .then(mergeWork)
            .enqueue()
    }

    // Observing work status
    fun observeSync(workId: UUID): Flow<WorkInfo> {
        return WorkManager.getInstance(context)
            .getWorkInfoByIdFlow(workId)
    }

    // Expedited work (API 31+)
    fun scheduleExpedited(userId: String) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(workDataOf("USER_ID" to userId))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 3. FOREGROUND SERVICES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Foreground Service = visible ongoing operation (notification required).
 *
 * Use cases: Music playback, navigation, file upload/download, fitness tracking.
 *
 * API 34+ Changes:
 *   - Must declare foreground service TYPE in manifest
 *   - Types: camera, dataSync, health, location, mediaPlayback, mediaProjection,
 *            microphone, phoneCall, remoteMessaging, shortService, specialUse
 *   - Must have corresponding permission for each type
 *
 * ⚠️ API 35+: dataSync type has a 6-hour time limit.
 *    Use WorkManager for longer sync operations.
 */

class MusicPlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()

        // Must call within 5 seconds of startForegroundService()
        startForeground(NOTIFICATION_ID, notification)

        when (intent?.action) {
            ACTION_PLAY -> startPlayback(intent.getStringExtra("TRACK_URL"))
            ACTION_PAUSE -> pausePlayback()
            ACTION_STOP -> {
                stopPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_STICKY  // Restart if killed by system
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            "music_playback", "Music Playback",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, "music_playback")
            .setContentTitle("Playing music")
            .setSmallIcon(R.drawable.ic_music)
            .addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1)
            )
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 2001
        const val ACTION_PLAY = "PLAY"
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_STOP = "STOP"
    }
}

// Manifest declaration (API 34+)
/*
<service
    android:name=".MusicPlaybackService"
    android:exported="false"
    android:foregroundServiceType="mediaPlayback">
</service>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
*/

/**
 * Service types and their return values:
 *
 *   START_STICKY      → restart with null Intent if killed (music player)
 *   START_NOT_STICKY  → don't restart if killed (one-time operation)
 *   START_REDELIVER_INTENT → restart with last Intent (file download)
 */


// ─────────────────────────────────────────────────────────────────────────────
// 4. BROADCAST RECEIVERS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * BroadcastReceiver listens for system-wide or app-specific events.
 *
 * Registration:
 *   Manifest-declared → survives app death (limited since API 26)
 *   Context-registered → lives as long as registering component
 *
 * API 26+ restrictions:
 *   Most implicit broadcasts can NOT be manifest-declared.
 *   Exceptions: BOOT_COMPLETED, LOCALE_CHANGED, MY_PACKAGE_REPLACED
 *   Use WorkManager or context-registered receivers instead.
 */

// ✅ Modern approach: NetworkCallback (replaces deprecated CONNECTIVITY_ACTION)
class NetworkConnectivityObserver(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(false) }
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

// Usage in ViewModel — schedule sync when connectivity returns
class ConnectivityAwareViewModel(
    private val networkObserver: NetworkConnectivityObserver,
    private val workManager: WorkManager
) : ViewModel() {
    init {
        viewModelScope.launch {
            networkObserver.isOnline
                .filter { it }  // Only when connected
                .collect {
                    workManager.enqueueUniqueWork(
                        "connectivity_sync",
                        ExistingWorkPolicy.KEEP,
                        OneTimeWorkRequestBuilder<SyncWorker>().build()
                    )
                }
        }
    }
}

/**
 * ⚠️ ConnectivityManager.CONNECTIVITY_ACTION is deprecated (API 28+).
 * Always use NetworkCallback + callbackFlow for modern connectivity monitoring.
 * See also: 06_networking.kt → NetworkMonitor for a complete implementation.
 */

/**
 * Local Broadcasts (deprecated):
 *   LocalBroadcastManager was deprecated in favor of:
 *   - SharedFlow / StateFlow for in-app events
 *   - LiveData for lifecycle-aware observation
 *   These are more efficient (no IPC overhead).
 */


// ─────────────────────────────────────────────────────────────────────────────
// 5. BACKGROUND EXECUTION LIMITS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * API 26+ Background Execution Limits:
 *   - Background services stopped ~1 minute after app goes to background
 *   - Most implicit broadcasts can't be manifest-registered
 *   - Background location limited to a few times per hour
 *
 * API 28+ App Standby Buckets restrict:
 *   - Job scheduling frequency
 *   - Alarm execution
 *   - Network access
 *   - FCM message priority
 *
 * API 31+ Exact Alarm Restrictions:
 *   - Need SCHEDULE_EXACT_ALARM permission (user can revoke)
 *   - Or USE_EXACT_ALARM (for alarm/timer apps, auto-granted)
 *
 * API 34+ Foreground Service Restrictions:
 *   - Must declare foreground service type
 *   - BOOT_COMPLETED receiver can't launch foreground services for some types
 *
 * 💡 Interview Strategy: Always mention these restrictions when discussing
 *    background work. Shows awareness of real-world Android constraints.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 6. ALARMMANAGER
// ─────────────────────────────────────────────────────────────────────────────

/**
 * AlarmManager fires an Intent at a specified time, even if the app is not running.
 *
 * Clock types:
 *   RTC               → wall-clock time, does NOT wake device
 *   RTC_WAKEUP        → wall-clock time, WAKES device
 *   ELAPSED_REALTIME  → time since boot, does NOT wake device
 *   ELAPSED_REALTIME_WAKEUP → time since boot, WAKES device
 *
 * Scheduling methods:
 *   set()                       → inexact, batched with other alarms (API 19+)
 *   setExact()                  → exact time, but deferred during Doze
 *   setExactAndAllowWhileIdle() → exact time, fires even in Doze (limited to ~1/9 min)
 *   setAlarmClock()             → treated as user-visible alarm clock; always fires,
 *                                  shows alarm icon in status bar
 *
 * Permissions (API 31+):
 *   SCHEDULE_EXACT_ALARM  → required for setExact / setExactAndAllowWhileIdle
 *                            user can revoke in Settings → Alarms & Reminders
 *   USE_EXACT_ALARM       → for alarm clock / timer apps (auto-granted, not revocable)
 *
 * ⚠️ Always check canScheduleExactAlarms() before scheduling on API 31+.
 *    If revoked, fall back to inexact or guide user to Settings.
 *
 * AlarmManagerCompat (Jetpack) provides backward-compatible helpers:
 *   AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, type, triggerAt, pi)
 *
 * When to use AlarmManager vs WorkManager:
 *   AlarmManager → exact-time delivery matters (medication reminders, alarm clocks)
 *   WorkManager  → task must complete but timing is flexible (sync, uploads)
 *
 * 💡 Interview Tip: "I use AlarmManager only when the user expects something
 *    at a precise time. For everything else, WorkManager is the better choice."
 */

// Reminder alarm example
class ReminderAlarmScheduler(private val context: Context) {

    fun scheduleReminder(reminderId: Int, triggerAtMillis: Long, title: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminderId)
            putExtra("TITLE", title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, reminderId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // API 31+: check permission before scheduling exact alarm
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                )
            } else {
                // Guide user to Settings or fall back to inexact
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                )
            }
        } else {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager, AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
            )
        }
    }

    fun cancelReminder(reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, reminderId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("TITLE") ?: "Reminder"
        val reminderId = intent.getIntExtra("REMINDER_ID", 0)

        val notification = NotificationCompat.Builder(context, "reminders")
            .setSmallIcon(R.drawable.ic_reminder)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(reminderId, notification)
    }
}

/*
Manifest declarations:
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<receiver android:name=".ReminderReceiver" android:exported="false" />
*/


// ─────────────────────────────────────────────────────────────────────────────
// 7. FIREBASE CLOUD MESSAGING (FCM)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * FCM delivers push messages from your server to the device.
 *
 * Message types:
 *   ┌──────────────────┬──────────────────────────────────────────────────┐
 *   │ Type             │ Behavior                                        │
 *   ├──────────────────┼──────────────────────────────────────────────────┤
 *   │ Notification msg │ System tray auto-display when app is background │
 *   │                  │ onMessageReceived() ONLY when app is foreground │
 *   ├──────────────────┼──────────────────────────────────────────────────┤
 *   │ Data message     │ Always delivered to onMessageReceived()         │
 *   │                  │ App handles display/processing in ALL states    │
 *   ├──────────────────┼──────────────────────────────────────────────────┤
 *   │ Both (combined)  │ Notification shown by system in background,     │
 *   │                  │ data payload in intent extras of launcher       │
 *   └──────────────────┴──────────────────────────────────────────────────┘
 *
 * Priority:
 *   HIGH   → wake device immediately, bypass Doze for up to ~10 seconds
 *            (quota-limited; overuse downgrades to NORMAL)
 *   NORMAL → delivered on next maintenance window during Doze
 *
 * Topic messaging:
 *   Subscribe: FirebaseMessaging.getInstance().subscribeToTopic("news")
 *   Server sends to "/topics/news" — delivered to all subscribers.
 *
 * FCM + Doze:
 *   - HIGH priority messages can wake the device during Doze
 *   - Limited to ~10 high-priority messages per hour per app
 *   - Use high priority only for user-visible, time-sensitive messages
 *
 * ⚠️ Interview Tip: "I always use data-only messages for full control.
 *    Notification messages are convenient but you lose control when the
 *    app is in the background."
 */

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Send updated token to your server
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Data messages: always arrives here (foreground AND background)
        val data = message.data
        val type = data["type"]

        when (type) {
            "new_article" -> {
                val articleId = data["article_id"] ?: return
                showArticleNotification(articleId, data["title"] ?: "New Article")

                // Schedule background sync for the full article
                WorkManager.getInstance(applicationContext)
                    .enqueue(
                        OneTimeWorkRequestBuilder<SyncWorker>()
                            .setInputData(workDataOf("ARTICLE_ID" to articleId))
                            .build()
                    )
            }
            "silent_sync" -> {
                // Silent data message — just sync, no notification
                WorkManager.getInstance(applicationContext)
                    .enqueueUniqueWork(
                        "fcm_sync",
                        ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequestBuilder<SyncWorker>().build()
                    )
            }
        }

        // Notification messages when app is in FOREGROUND
        message.notification?.let { notification ->
            showNotification(notification.title ?: "", notification.body ?: "")
        }
    }

    private fun sendTokenToServer(token: String) { /* API call */ }

    private fun showArticleNotification(articleId: String, title: String) {
        val intent = Intent(this, ArticleDetailActivity::class.java).apply {
            putExtra("ARTICLE_ID", articleId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, articleId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "articles")
            .setSmallIcon(R.drawable.ic_article)
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(articleId.hashCode(), notification)
    }

    private fun showNotification(title: String, body: String) { /* ... */ }
}

/*
Manifest declaration:
<service
    android:name=".MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
*/


// ─────────────────────────────────────────────────────────────────────────────
// 8. FOREGROUND SERVICE LAUNCH RESTRICTIONS (API 31+)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Starting with API 31 (Android 12), apps CANNOT start a foreground service
 * from the background. Doing so throws ForegroundServiceStartNotAllowedException.
 *
 * Exemptions (cases where background FGS launch IS allowed):
 *   ✅ User taps a notification (PendingIntent with FROM_NOTIFICATION)
 *   ✅ Alarm callback (setAlarmClock, or exact alarm with SCHEDULE_EXACT_ALARM)
 *   ✅ HIGH-priority FCM message (within ~10 second window)
 *   ✅ Device owner / profile owner apps
 *   ✅ App is in the temporary allowlist (system-granted)
 *   ✅ Companion device manager role
 *   ✅ BOOT_COMPLETED for some FGS types (but NOT all — e.g., not mediaPlayback)
 *
 * BroadcastReceiver restrictions:
 *   - Manifest-declared receivers that fire in background CANNOT start FGS
 *   - Context-registered receivers CAN start FGS if the app is still "visible"
 *   - BOOT_COMPLETED is an exception but has type-specific limitations
 *
 * Recommended alternatives:
 *   1. WorkManager with setExpedited() — runs immediately, short-lived
 *   2. Show notification first, let user tap to trigger FGS
 *   3. Use setAlarmClock() to get into the allowlist for time-critical work
 *
 * API 34+ additional restrictions:
 *   - Must declare foregroundServiceType in manifest
 *   - Specific permissions required per type
 *   - dataSync type has 6-hour limit (API 35+)
 *
 * 💡 Interview Tip: "On API 31+, I default to WorkManager expedited work for
 *    urgent background tasks. I only use FGS when the user explicitly triggers
 *    the action from the foreground or through a notification."
 */

// Handling FGS restriction: prefer WorkManager expedited for background-started work
class BackgroundTaskHandler(private val context: Context) {

    fun handleUrgentTask(taskData: String) {
        if (isAppInForeground()) {
            // Safe to start FGS from foreground
            val intent = Intent(context, UploadService::class.java).apply {
                putExtra("DATA", taskData)
            }
            ContextCompat.startForegroundService(context, intent)
        } else {
            // Background: use WorkManager expedited instead
            val request = OneTimeWorkRequestBuilder<UploadWorker>()
                .setInputData(workDataOf("DATA" to taskData))
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        return appProcesses.any {
            it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                it.processName == context.packageName
        }
    }
}

/**
 * ⚠️ Common mistake: catching ForegroundServiceStartNotAllowedException
 *    and silently ignoring it. The work never runs. Always have a fallback.
 *
 * Pattern for FCM-triggered FGS:
 *   1. Receive HIGH-priority FCM data message
 *   2. In onMessageReceived(), you have ~10 seconds
 *   3. Call startForegroundService() within that window
 *   4. If the window is missed, fall back to WorkManager
 */


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: How would you implement a photo upload that survives app kill?
 *    A: Use WorkManager with CoroutineWorker.
 *       Constraints: NetworkType.CONNECTED
 *       Pass photo URI as input data.
 *       Show progress via setForeground() notification.
 *       Use unique work to prevent duplicate uploads.
 *       Chain: compress → upload → cleanup.
 *
 * ❓ Q2: WorkManager vs JobScheduler vs AlarmManager — when to use each?
 *    A: WorkManager: Almost all persistent background work. Abstracts over
 *       JobScheduler/AlarmManager. Constraint-based, chainable, observable.
 *       JobScheduler: Direct API for scheduled work (WorkManager uses this internally).
 *       AlarmManager: Only for exact-time requirements (actual alarm clock, reminders).
 *
 * ❓ Q3: What happens to a Foreground Service when the user force-stops the app?
 *    A: It's killed immediately. START_STICKY won't restart it.
 *       Force-stop also cancels WorkManager, AlarmManager, and kills the process.
 *       Only BOOT_COMPLETED or FCM can restart components after force-stop.
 *
 * ❓ Q4: How do you handle WorkManager work that depends on previous work's output?
 *    A: Chain workers with then(). Output data from one worker becomes
 *       input data for the next. For parallel work merging, use InputMerger
 *       (ArrayCreatingInputMerger or OverwritingInputMerger).
 *
 * ❓ Q5: What's the difference between expedited work and foreground service?
 *    A: Expedited work: short-lived, high priority, runs even under Doze.
 *       Has a quota (OS limits how often). Falls back to regular work if exceeded.
 *       Foreground Service: long-running, visible to user via notification.
 *       No time limit (except dataSync type on API 35+).
 *
 * ❓ Q6: What's the difference between FCM data messages and notification messages?
 *    A: Data messages: always delivered to onMessageReceived() regardless of app state.
 *       You have full control over parsing, display, and behavior.
 *       Notification messages: system auto-displays when app is in background.
 *       onMessageReceived() only fires in foreground. Combined messages get notification
 *       in background and data in the launcher intent's extras — easy to miss.
 *       Best practice: use data-only messages for full control in production apps.
 *
 * ❓ Q7: How do you handle starting a Foreground Service from the background on API 31+?
 *    A: You can't — it throws ForegroundServiceStartNotAllowedException.
 *       Solutions: (1) Use WorkManager with setExpedited() for urgent short-lived tasks.
 *       (2) Show a notification and let the user tap to start the FGS (FROM_NOTIFICATION).
 *       (3) Use a high-priority FCM message — gives ~10 seconds to call startForegroundService().
 *       (4) For alarm-like use cases, setAlarmClock() puts the app on the temporary allowlist.
 *       Always have a WorkManager fallback — never silently fail.
 */
