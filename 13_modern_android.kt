/**
 * ============================================================================
 * 13 — MODERN ANDROID DEVELOPMENT (2025-2026)
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Kotlin Multiplatform (KMP)
 *   2. App Modularization
 *   3. Gradle Build System (Version Catalogs, Convention Plugins)
 *   4. Baseline Profiles (summary → 08_performance_optimization.kt)
 *   5. Android 15/16 Changes
 *   6. Predictive Back Gesture
 *   7. Accessibility
 *   8. App Links & Deep Links
 *   9. Dynamic Feature Modules
 *  10. Privacy Sandbox (Overview)
 *  11. Large Screen & Adaptive Layouts
 *  12. Credential Manager API
 *  13. Photo Picker
 *  14. CameraX & Media3
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. KOTLIN MULTIPLATFORM (KMP)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * KMP = share Kotlin code between Android, iOS, Desktop, Web.
 *
 * What to share:
 *   ✅ Business logic (UseCases, domain models)
 *   ✅ Networking (Ktor client)
 *   ✅ Data persistence (SQLDelight)
 *   ✅ Serialization (kotlinx.serialization)
 *   ✅ State management
 *   ❌ UI (platform-specific; though Compose Multiplatform is emerging)
 *   ❌ Platform APIs (camera, sensors, notifications)
 *
 * Project structure:
 *   shared/
 *     commonMain/       → shared Kotlin code
 *     androidMain/      → Android-specific implementations
 *     iosMain/          → iOS-specific implementations
 *   androidApp/         → Android app module
 *   iosApp/             → iOS Xcode project
 *
 * expect/actual mechanism:
 *   expect → declares platform-specific API in commonMain
 *   actual → provides implementation in each platform module
 */

// Common code (commonMain)
expect class PlatformLogger() {
    fun log(message: String)
}

// Android implementation (androidMain)
actual class PlatformLogger {
    actual fun log(message: String) {
        android.util.Log.d("KMP", message)
    }
}

// Shared UseCase
class GetArticlesUseCase(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(): Result<List<Article>> {
        return repository.getArticles()
    }
}

/**
 * 💡 Google's KMP investment:
 *   - Official support for KMP in Android Studio
 *   - Jetpack libraries being made KMP-compatible (Room, DataStore)
 *   - Google uses KMP internally for cross-platform code sharing
 *
 * ❓ Interview Question: When would you recommend KMP?
 *    A: When you have Android + iOS apps with significant shared business logic.
 *       Not worth it for: small apps, UI-heavy apps, or if team has no iOS devs.
 *       Start by sharing data/domain layers. Don't share UI initially.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 2. APP MODULARIZATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Modularization = splitting your app into multiple Gradle modules.
 *
 * Benefits:
 *   ✅ Build speed (only changed modules recompile)
 *   ✅ Code ownership (team per module)
 *   ✅ Strict dependency boundaries (can't accidentally access internals)
 *   ✅ Reusability (library modules shared across apps)
 *   ✅ Testability (test modules in isolation)
 *   ✅ Dynamic delivery (download features on demand)
 *
 * Module types:
 *   :app              → main application module (entry point)
 *   :core:network     → networking abstractions
 *   :core:database    → Room database
 *   :core:common      → shared utilities
 *   :core:ui          → shared composables, theme
 *   :core:model       → domain models
 *   :feature:articles → article list & detail screens
 *   :feature:profile  → user profile screens
 *   :feature:settings → settings screens
 *
 * Dependency rules:
 *   :feature modules → depend on :core modules (not other :feature modules)
 *   :core modules    → can depend on other :core modules
 *   :app module      → depends on all :feature modules
 *
 * 💡 Navigation between feature modules:
 *   Use Navigation Component with deep links or a shared navigation module.
 *   Features don't know about each other — they navigate via routes/URIs.
 */

// Module structure example
/*
project/
├── app/                          // Entry point, DI setup
│   └── build.gradle.kts
├── core/
│   ├── common/                   // Extensions, utils
│   ├── model/                    // Domain data classes
│   ├── database/                 // Room DB, DAOs, entities
│   ├── network/                  // Retrofit, API services
│   ├── data/                     // Repositories
│   └── ui/                       // Shared Compose components, theme
├── feature/
│   ├── articles/                 // Article screens + ViewModel
│   ├── profile/                  // Profile screens + ViewModel
│   └── settings/                 // Settings screens + ViewModel
└── build-logic/                  // Convention plugins
    └── convention/
*/

/**
 * ❓ Interview Question: How do you handle navigation between feature modules
 *    that don't know about each other?
 *
 *    A: Options:
 *    1. Deep link routes — each feature declares routes, navigation module maps them
 *    2. Interface-based — navigation interface in :core, implemented in :app
 *    3. Navigation Component with NavGraphs per feature + shared routes
 */


// ─────────────────────────────────────────────────────────────────────────────
// 3. GRADLE BUILD SYSTEM
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Version Catalogs (libs.versions.toml):
 *   Centralized dependency management for multi-module projects.
 *   Single file defines all versions, libraries, bundles, and plugins.
 */

// gradle/libs.versions.toml
/*
[versions]
kotlin = "2.0.0"
compose-bom = "2024.12.01"
hilt = "2.51"
room = "2.6.1"
coroutines = "1.9.0"

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }

[bundles]
compose = ["compose-ui", "compose-material3"]
room = ["room-runtime", "room-ktx"]

[plugins]
android-application = { id = "com.android.application", version = "8.7.0" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
*/

// Usage in build.gradle.kts
/*
dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.room)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
*/

/**
 * Convention Plugins:
 *   Shared build logic across modules (DRY principle).
 *   Instead of copy-pasting build.gradle config, define once in build-logic/.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 4. BASELINE PROFILES (SUMMARY)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Baseline Profiles tell ART which code paths to AOT-compile at install time.
 *
 * Impact: 20-30% faster startup, smoother interactions.
 *
 * Quick summary:
 *   1. Write a Macrobenchmark that exercises critical user journeys
 *   2. BaselineProfileRule generates a profile file
 *   3. Profile is bundled with AAB/APK
 *   4. At install, ART AOT-compiles listed methods as native code
 *   5. Cloud Profiles from Play Store complement developer-provided profiles
 *
 * → See 08_performance_optimization.kt § "BASELINE PROFILES" for the full
 *   implementation: BaselineProfileGenerator, StartupBenchmark, CI integration,
 *   and Gradle setup.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 5. ANDROID 15/16 PLATFORM CHANGES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Android 15 (API 35):
 *   - 16KB page size support (for newer hardware)
 *   - Foreground service dataSync type has 6-hour limit
 *   - Package visibility enforcement tightened
 *   - Private Space for hiding apps
 *
 * Android 16 (API 36 — 2026):
 *   - Predictive back gesture enforcement (must support or breaks)
 *   - Photo picker improvements
 *   - Health Connect integration
 *   - Richer widget APIs
 *   - Stricter background work limits
 *
 * Key patterns:
 *   - Target the latest SDK but test on older devices
 *   - Use AndroidX compat libraries for backward compatibility
 *   - Handle new permission requirements gracefully
 */


// ─────────────────────────────────────────────────────────────────────────────
// 6. PREDICTIVE BACK GESTURE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Predictive Back = system-level back gesture with preview animation.
 * User swipes from edge and sees a preview of where they'll go.
 *
 * Requirements (API 34+ enforced on API 36):
 *   1. Set android:enableOnBackInvokedCallback="true" in manifest
 *   2. Migrate from onBackPressed() to OnBackPressedCallback
 *   3. Support system back animations (cross-activity, cross-task)
 */

// Migrating back handling
class ModernActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Modern back handling with OnBackPressedCallback
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    showDiscardDialog()
                } else {
                    // Let system handle the back (default animation)
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    // ❌ Deprecated — breaks predictive back
    // override fun onBackPressed() { super.onBackPressed() }
}


// ─────────────────────────────────────────────────────────────────────────────
// 7. ACCESSIBILITY
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Google values accessibility. Mentioning it in interviews shows maturity.
 *
 * Key practices:
 *   - contentDescription on all interactive elements
 *   - Sufficient color contrast (4.5:1 for text)
 *   - Touch targets >= 48dp
 *   - Semantic headings for screen readers
 *   - Focus order matches visual order
 *   - Support TalkBack navigation
 */

// Compose accessibility
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    isBookmarked: Boolean
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)  // Minimum touch target
            .semantics {
                contentDescription = if (isBookmarked) {
                    "Remove $text from bookmarks"
                } else {
                    "Add $text to bookmarks"
                }
                role = Role.Button
                stateDescription = if (isBookmarked) "Bookmarked" else "Not bookmarked"
            }
    ) {
        Icon(
            imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = null  // Described by parent semantics
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 8. APP LINKS & DEEP LINKS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Three types of links that open your app:
 *
 * Deep Links:
 *   - Custom scheme URIs: myapp://article/123
 *   - Matched via intent-filter
 *   - ⚠️ Any app can register the same scheme — not verified
 *
 * Web Links:
 *   - HTTP/HTTPS URIs: https://example.com/article/123
 *   - Shows disambiguation dialog if multiple apps handle it
 *
 * App Links (verified, recommended):
 *   - HTTPS only: https://example.com/article/123
 *   - Verified via Digital Asset Links (assetlinks.json on your domain)
 *   - No disambiguation dialog — opens directly in your app
 *   - Falls back to browser if app not installed
 *
 * Digital Asset Links:
 *   Host a JSON file at: https://example.com/.well-known/assetlinks.json
 *   Contains your app's package name + signing certificate fingerprint.
 *   Android verifies this at install time.
 */

// Intent filter for App Links (AndroidManifest.xml)
/*
<activity android:name=".DeepLinkActivity"
    android:exported="true">

    <!-- Deep link (custom scheme) -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="myapp" android:host="article" />
    </intent-filter>

    <!-- Verified App Link (HTTPS + autoVerify) -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https"
              android:host="example.com"
              android:pathPrefix="/article" />
    </intent-filter>
</activity>
*/

// assetlinks.json (hosted at https://example.com/.well-known/assetlinks.json)
/*
[{
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
        "namespace": "android_app",
        "package_name": "com.example.app",
        "sha256_cert_fingerprints": [
            "AB:CD:EF:12:34:56:..."
        ]
    }
}]
*/

// Navigation Component deep links
class ArticleNavigation {

    // Type-safe deep link in Navigation Component
    fun setupNavGraph(navController: NavController) {
        navController.graph = navController.createGraph(
            startDestination = "home"
        ) {
            composable(
                route = "article/{articleId}",
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "https://example.com/article/{articleId}"
                    }
                )
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getString("articleId")
                ArticleScreen(articleId = articleId)
            }
        }
    }
}

// Testing deep links
/*
# Via ADB:
adb shell am start -a android.intent.action.VIEW \
    -d "https://example.com/article/123" \
    com.example.app

# Verify app link status:
adb shell pm get-app-links com.example.app
*/


// ─────────────────────────────────────────────────────────────────────────────
// 9. DYNAMIC FEATURE MODULES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Play Feature Delivery: Download parts of your app on demand.
 * Requires Android App Bundle (AAB) — not APKs.
 *
 * Delivery modes:
 *   install-time   → installed with the base app (like a regular module)
 *   on-demand      → downloaded when user triggers the feature
 *   conditional    → installed only on devices matching conditions (API, country, hardware)
 *
 * Benefits:
 *   - Smaller initial download size (base APK is smaller)
 *   - Users only download features they actually use
 *   - Reduces install abandonment (large apps lose users)
 *
 * When to use:
 *   - Large features used by a small % of users (e.g., AR try-on, video editor)
 *   - Country-specific features (payment methods, compliance)
 *   - Premium/paid features
 *
 * SplitInstallManager API:
 *   - Request feature install, monitor progress, handle errors
 *   - Deferred install (download when on Wi-Fi)
 */

// On-demand feature installation
class FeatureInstaller(private val context: Context) {

    private val splitInstallManager: SplitInstallManager =
        SplitInstallManagerFactory.create(context)

    fun installFeature(
        featureName: String,
        onProgress: (Float) -> Unit,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val request = SplitInstallRequest.newBuilder()
            .addModule(featureName)
            .build()

        splitInstallManager.startInstall(request)
            .addOnSuccessListener { sessionId ->
                // Monitor installation progress
                splitInstallManager.registerListener { state ->
                    when (state.status()) {
                        SplitInstallSessionStatus.DOWNLOADING -> {
                            val progress = state.bytesDownloaded().toFloat() /
                                state.totalBytesToDownload()
                            onProgress(progress)
                        }
                        SplitInstallSessionStatus.INSTALLED -> {
                            onSuccess()
                        }
                        SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                            // Large download — system asks for user confirmation
                            splitInstallManager.startConfirmationDialogForResult(
                                state, context as Activity, REQUEST_CODE
                            )
                        }
                        SplitInstallSessionStatus.FAILED -> {
                            onError(Exception("Install failed: ${state.errorCode()}"))
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun isFeatureInstalled(featureName: String): Boolean {
        return splitInstallManager.installedModules.contains(featureName)
    }

    companion object {
        private const val REQUEST_CODE = 1001
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 10. PRIVACY SANDBOX (OVERVIEW)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Android Privacy Sandbox = Google's initiative to replace cross-app tracking
 * with privacy-preserving alternatives. Phasing out Advertising ID.
 *
 * Key APIs:
 *
 * 1. Topics API (interest-based ads without cross-app tracking):
 *    - Device locally classifies apps into interest "topics" (e.g., Sports, Travel)
 *    - Returns 3 topics per epoch (1 week) — coarse-grained, not per-app
 *    - Ad SDKs receive topics to show relevant ads without knowing which apps are installed
 *    - User can view and remove topics in Settings
 *
 * 2. Attribution Reporting API:
 *    - Measures ad conversions (ad click → app install/purchase)
 *    - Reports are delayed and aggregated — no real-time per-user tracking
 *    - Replaces direct referrer tracking
 *
 * 3. SDK Runtime (API 34+):
 *    - Third-party ad SDKs run in an isolated sandbox process
 *    - Cannot access the app's storage, permissions, or identifiers
 *    - Reduces data leakage from ad SDKs
 *
 * Why this matters for Google interviews:
 *   - Shows awareness of Google's current strategic direction
 *   - Privacy-first thinking is valued at Google
 *   - Understanding the trade-offs between privacy and ad monetization
 *   - May be asked: "How would you handle ad attribution without GAID?"
 *     Answer: Use Attribution Reporting API with aggregated reports.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 11. LARGE SCREEN & ADAPTIVE LAYOUTS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Google is heavily pushing support for foldables, tablets, and Chromebooks.
 * Apps that don't adapt lose ranking and users on these growing form factors.
 *
 * Window Size Classes (Jetpack):
 *   Compact  → phone portrait (< 600dp width)
 *   Medium   → tablet portrait, foldable unfolded, large phone landscape (600-840dp)
 *   Expanded → tablet landscape, desktop (> 840dp)
 *
 * Use WindowSizeClass to choose layout strategy, NOT screen orientation.
 *
 * Canonical Layouts (Google's recommended patterns):
 *   List-Detail       → list on left, detail on right (email, messaging)
 *   Feed              → single-column on compact, multi-column on expanded
 *   Supporting Panel  → main content + side panel (maps, editors)
 *
 * Key APIs:
 *   - WindowSizeClass (Material 3): calculateWindowSizeClass(activity)
 *   - adaptive-navigation (Jetpack): NavigationSuiteScaffold auto-switches
 *     between bottom nav (compact), nav rail (medium), and nav drawer (expanded)
 *   - WindowInfoTracker: observe fold state and hinge position for foldables
 */

@Composable
fun AdaptiveArticleScreen(windowSizeClass: WindowSizeClass) {
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Phone: single-pane, navigate to detail screen
            ArticleListScreen()
        }
        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> {
            // Tablet/Foldable: list-detail side by side
            Row {
                ArticleListScreen(modifier = Modifier.weight(0.4f))
                ArticleDetailScreen(modifier = Modifier.weight(0.6f))
            }
        }
    }
}

// NavigationSuiteScaffold — auto-adapts nav component to window size
@Composable
fun AdaptiveNavigation(windowSizeClass: WindowSizeClass) {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") },
                selected = true,
                onClick = { /* navigate */ }
            )
            item(
                icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                label = { Text("Search") },
                selected = false,
                onClick = { /* navigate */ }
            )
        }
    ) {
        // Screen content
        // On Compact: renders BottomNavigation
        // On Medium: renders NavigationRail
        // On Expanded: renders NavigationDrawer
    }
}

/**
 * ❓ Interview Question: How do you make your app work well on foldables?
 *    A: Use WindowSizeClass to choose layout (not orientation).
 *       Observe fold state via WindowInfoTracker to avoid placing content on the hinge.
 *       Support drag-and-drop for multi-window.
 *       Test with the foldable emulator profiles in Android Studio.
 *
 * 💡 Interview Tip: Google evaluates apps on large screens for Play Store featuring.
 *    Mentioning adaptive layouts shows awareness of Google's current priorities.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 12. CREDENTIAL MANAGER API
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Credential Manager = unified Jetpack API for sign-in (replaces deprecated
 * SmartLock, One Tap Sign-In, and legacy FIDO2 APIs).
 *
 * Supports:
 *   - Passkeys (FIDO2 WebAuthn — passwordless, phishing-resistant)
 *   - Passwords (saved in Google Password Manager)
 *   - Federated identity (Sign in with Google)
 *
 * Why passkeys matter:
 *   - Google is actively pushing passkey adoption
 *   - No password to phish, leak, or reuse
 *   - Backed by device biometrics (fingerprint/face)
 *   - Cross-device via Google Account sync
 *
 * Key classes:
 *   CredentialManager           → entry point, created via CredentialManager.create(context)
 *   GetCredentialRequest        → request to retrieve an existing credential
 *   CreatePublicKeyCredentialRequest → request to create a new passkey
 *   GetPasswordOption           → option to retrieve a saved password
 *   GetPublicKeyCredentialOption → option to retrieve a passkey
 */

suspend fun signIn(context: Context): String? {
    val credentialManager = CredentialManager.create(context)

    val request = GetCredentialRequest(listOf(
        GetPasswordOption(),                    // Saved passwords
        GetPublicKeyCredentialOption(           // Passkeys
            requestJson = generatePasskeyRequestJson()
        )
    ))

    return try {
        val result = credentialManager.getCredential(context, request)
        when (val credential = result.credential) {
            is PasswordCredential -> {
                // Authenticate with username/password
                authenticate(credential.id, credential.password)
            }
            is PublicKeyCredential -> {
                // Verify passkey assertion with your server
                verifyPasskeyAssertion(credential.authenticationResponseJson)
            }
            else -> null
        }
    } catch (e: GetCredentialCancellationException) {
        null  // User cancelled
    } catch (e: NoCredentialException) {
        null  // No saved credentials — show manual sign-in
    }
}

/**
 * 💡 Interview Tip: "For new apps, I implement Credential Manager with passkey
 *    support as the primary sign-in method, with password as fallback. Passkeys
 *    are Google's strategic direction and eliminate entire categories of attacks."
 */


// ─────────────────────────────────────────────────────────────────────────────
// 13. PHOTO PICKER
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Photo Picker (API 33+, backported to API 19 via Google Play services):
 *   System-provided UI for selecting photos/videos.
 *   NO runtime permissions required — the system grants access only to
 *   the specific items the user selects.
 *
 * Why use it:
 *   ✅ No READ_MEDIA_IMAGES permission needed
 *   ✅ Privacy-preserving — app only sees selected items
 *   ✅ Consistent UI across devices
 *   ✅ Supports single/multi selection and media type filtering
 *
 * When NOT to use it:
 *   ❌ If you need to browse all media (gallery app, file manager)
 *   ❌ If you need access to file metadata beyond the content URI
 */

// Single photo selection
class PhotoPickerActivity : AppCompatActivity() {

    private val pickMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // User selected a photo — use the URI
            loadImage(uri)
        }
    }

    private val pickMultipleMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            processSelectedMedia(uris)
        }
    }

    fun openPhotoPicker() {
        // Pick images only (no video)
        pickMedia.launch(PickVisualMediaRequest(
            ActivityResultContracts.PickVisualMedia.ImageOnly
        ))
    }

    fun openMultiPicker() {
        // Pick images and video
        pickMultipleMedia.launch(PickVisualMediaRequest(
            ActivityResultContracts.PickVisualMedia.ImageAndVideo
        ))
    }
}

/**
 * ❓ Interview Question: How do you handle image selection in a modern Android app?
 *    A: Use the system Photo Picker — no permissions, privacy-preserving, consistent UX.
 *       For custom gallery UI, use READ_MEDIA_IMAGES (API 33+) or READ_MEDIA_VISUAL_USER_SELECTED
 *       (API 34+) for partial access. For document files, use Storage Access Framework (SAF).
 */


// ─────────────────────────────────────────────────────────────────────────────
// 14. CAMERAX & MEDIA3
// ─────────────────────────────────────────────────────────────────────────────

/**
 * CameraX (Jetpack):
 *   Lifecycle-aware camera API that abstracts device-specific quirks.
 *
 *   Use cases:
 *     Preview      → show camera viewfinder
 *     ImageCapture → take photos
 *     VideoCapture → record video
 *     ImageAnalysis → process frames (barcode scanning, ML)
 *
 *   Key benefits over Camera2:
 *     ✅ Lifecycle-aware — binds to LifecycleOwner, auto start/stop
 *     ✅ Device-specific fixes built in (CameraX Extensions for HDR, Night, etc.)
 *     ✅ Much less boilerplate than Camera2
 *     ✅ Concurrent use cases (preview + analysis + capture)
 *
 *   Architecture:
 *     ProcessCameraProvider → bind use cases to a lifecycle
 *     CameraSelector → choose front/back camera
 *     Preview.SurfaceProvider → surface for viewfinder
 */

fun bindCamera(
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    // Process frame (e.g., ML Kit barcode scanning)
                    imageProxy.close()
                }
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, preview, imageCapture, imageAnalysis
        )
    }, ContextCompat.getMainExecutor(previewView.context))
}

/**
 * Media3 (Jetpack, replaces ExoPlayer standalone):
 *   Unified media playback library.
 *
 *   Key components:
 *     ExoPlayer          → the player engine (now under Media3 namespace)
 *     MediaSession       → publish playback state to system (lock screen, Bluetooth)
 *     MediaController    → client-side control of a MediaSession
 *     PlayerView         → ready-made UI for video playback (View system)
 *
 *   When to mention in interviews:
 *     - Chat app system design → video message playback
 *     - News feed design → auto-play video in feed
 *     - Music player → background playback with MediaSession
 *
 *   Key features:
 *     ✅ Adaptive streaming (HLS, DASH)
 *     ✅ Caching (CacheDataSource for offline playback)
 *     ✅ DRM (Widevine, PlayReady)
 *     ✅ Compositor for transitions and effects
 *
 * 💡 Interview Tip: "For video playback, I use Media3 ExoPlayer with lifecycle-aware
 *    binding. For feed auto-play, I observe item visibility in LazyColumn and
 *    play/pause the player based on the >50% visibility threshold."
 */


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: How do you decide what to put in each module?
 *    A: Follow the dependency rule: features depend on core, not each other.
 *       Core modules provide shared infrastructure (network, DB, models).
 *       Feature modules contain screens + ViewModels for one feature.
 *       Use the "could another team own this?" test.
 *
 * ❓ Q2: How do Baseline Profiles interact with Cloud Profiles?
 *    A: Developer Baseline Profiles are bundled with the app.
 *       Cloud Profiles are crowd-sourced from user devices via Play.
 *       Both are merged by ART for optimal compilation.
 *       Baseline Profiles help first-time users; Cloud Profiles help over time.
 *
 * ❓ Q3: When would you use KMP vs Flutter vs React Native?
 *    A: KMP: native UI per platform, share business logic, gradual adoption.
 *       Flutter: single codebase for UI + logic, fast prototyping, custom UI.
 *       React Native: JS ecosystem, web team doing mobile.
 *       Google recommends KMP for Android+iOS with native quality.
 *
 * ❓ Q4: How do you handle the predictive back gesture for custom back handling?
 *    A: Use OnBackPressedCallback with isEnabled flag. Enable when there's
 *       custom back behavior (unsaved changes, bottom sheet). Disable to let
 *       system handle default animation. Never override onBackPressed().
 *
 * ❓ Q5: What are the key Gradle performance optimizations?
 *    A: Enable configuration cache, build cache, parallel builds.
 *       Use version catalogs (centralized deps). Convention plugins (DRY config).
 *       Avoid unnecessary plugins. Use buildSrc sparingly (invalidates all caches).
 *
 * ❓ Q6: How do you support foldables and tablets in a Compose app?
 *    A: Use WindowSizeClass from Material 3 to pick layout strategy:
 *       Compact → single pane, Medium → nav rail + content, Expanded → list-detail.
 *       Use NavigationSuiteScaffold to auto-switch between bottom nav, rail, and drawer.
 *       Observe fold state via WindowInfoTracker to avoid placing content on the hinge.
 *       Test with foldable emulator profiles in Android Studio.
 *
 * ❓ Q7: What is the Credential Manager API and why does it matter?
 *    A: Unified Jetpack API for sign-in that replaces SmartLock, One Tap, and FIDO2.
 *       Supports passkeys (phishing-resistant, biometric-backed), passwords, and
 *       federated identity (Sign in with Google). Passkeys are Google's strategic
 *       direction for authentication — no password to leak or phish.
 *
 * ❓ Q8: When should you use the system Photo Picker vs requesting media permissions?
 *    A: Photo Picker for most cases — no permissions needed, privacy-preserving.
 *       Request READ_MEDIA_IMAGES only if you need to browse all media (gallery app).
 *       On API 34+, READ_MEDIA_VISUAL_USER_SELECTED enables partial access where
 *       the user selects specific files each session.
 */
