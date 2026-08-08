/**
 * ============================================================================
 * 12 — SECURITY
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Runtime Permissions Model
 *   2. Network Security Configuration
 *   3. Certificate Pinning
 *   4. Android Keystore System
 *   5. Encrypted Storage (EncryptedSharedPreferences, EncryptedFile)
 *   6. Biometric Authentication
 *   7. Common Vulnerabilities & Mitigations
 *   8. ProGuard / R8 — Code Obfuscation
 *   9. Content Provider Security
 *  10. WebView Security
 *  11. Play Integrity API
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. RUNTIME PERMISSIONS MODEL
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Permission types:
 *   Normal      → granted automatically at install (INTERNET, VIBRATE)
 *   Dangerous   → requires runtime request (CAMERA, LOCATION, CONTACTS)
 *   Special     → requires Settings intent (SYSTEM_ALERT_WINDOW, WRITE_SETTINGS)
 *
 * Permission groups:
 *   ⚠️ Don't rely on groups — Google can change grouping at any time.
 *   Always request the specific permission you need.
 *
 * API 33+ changes:
 *   - POST_NOTIFICATIONS is now a runtime permission
 *   - Photo picker doesn't need READ_MEDIA_IMAGES for basic access
 *   - Granular media permissions (READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO)
 *
 * API 34+ changes:
 *   - Partial photo/video access (user selects specific files)
 *   - READ_MEDIA_VISUAL_USER_SELECTED for partial access
 */

// Modern permission handling with Activity Result API
class PermissionActivity : AppCompatActivity() {

    // Single permission
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            // Check if we should show rationale
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showRationaleDialog()
            } else {
                // User permanently denied — direct to Settings
                showSettingsDialog()
            }
        }
    }

    // Multiple permissions
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        when {
            fineLocationGranted -> enablePreciseLocation()
            coarseLocationGranted -> enableApproximateLocation()
            else -> showLocationDeniedMessage()
        }
    }

    fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showRationaleDialog()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 2. NETWORK SECURITY CONFIGURATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Network Security Config (res/xml/network_security_config.xml):
 *   Controls app's network security behavior declaratively.
 *
 * Key features:
 *   - Cleartext traffic policy (HTTP vs HTTPS)
 *   - Custom trust anchors (CA certificates)
 *   - Certificate pinning
 *   - Debug overrides
 *
 * API 28+: Cleartext (HTTP) blocked by default.
 * API 24+: User-installed CAs not trusted by default.
 */

// Example network_security_config.xml
/*
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Default: HTTPS only, system CAs only -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>

    <!-- Exception: allow cleartext for local dev server -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
    </domain-config>

    <!-- Certificate pinning for production API -->
    <domain-config>
        <domain includeSubdomains="true">api.example.com</domain>
        <pin-set expiration="2027-12-31">
            <pin digest="SHA-256">base64EncodedPin1=</pin>
            <pin digest="SHA-256">base64EncodedBackupPin=</pin>
        </pin-set>
    </domain-config>

    <!-- Debug only: trust user-installed CAs for proxy tools -->
    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
*/


// ─────────────────────────────────────────────────────────────────────────────
// 3. CERTIFICATE PINNING — DEEP DIVE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Certificate pinning = only trust specific public keys (SPKI hashes) for a domain.
 * Prevents MITM even if a CA is compromised or a rogue cert is installed.
 *
 * What to pin:
 *   - Public key hash (SHA-256 of SubjectPublicKeyInfo) — survives cert renewal
 *     if the same key pair is reused
 *   - NOT the full certificate — expires with cert and breaks on every rotation
 *
 * Approaches:
 *   1. Network Security Config (XML) — recommended, declarative, debug overrides
 *   2. OkHttp CertificatePinner — programmatic, per-client control
 *   3. Custom TrustManager — last resort; easy to get wrong
 *
 * ⚠️ Pin rotation strategy (critical for production):
 *   - Always ship TWO pins: current + backup (next cert's SPKI hash)
 *   - Set pin-set expiration in XML so old pins auto-expire
 *   - Coordinate with backend/infra before cert rotation
 *   - Have a remote-config kill switch to disable pinning in emergencies
 *   - Without backup pin, a cert rotation BRICKS the app for all users
 *
 * How to extract SPKI pin (for OkHttp or XML):
 *   openssl s_client -connect api.example.com:443 | \
 *     openssl x509 -pubkey -noout | \
 *     openssl pkey -pubin -outform der | \
 *     openssl dgst -sha256 -binary | openssl enc -base64
 *
 * 💡 Interview tip: Pinning protects against MITM; it does NOT stop reverse
 *    engineering or rooted-device tampering — pair with Play Integrity for that.
 */

// OkHttp — programmatic pinning with backup pin
fun createPinnedClient(): OkHttpClient {
    val certificatePinner = CertificatePinner.Builder()
        .add(
            "api.example.com",
            "sha256/CURRENT_SPKI_HASH=",   // Active cert public key
            "sha256/BACKUP_SPKI_HASH="     // Next rotation — ship before switch
        )
        .build()

    return OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .build()
}

// Pinning failure handling — log for monitoring, don't crash silently
class PinningEventListener : EventListener() {
    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        // Handshake succeeded — pin matched
    }
    // CertificatePinningException thrown on mismatch — catch at call site
}

// Remote-config fallback pattern (pseudo-code)
/*
fun createApiClient(remoteConfig: RemoteConfig): OkHttpClient {
    val builder = OkHttpClient.Builder()
    if (remoteConfig.getBoolean("cert_pinning_enabled", default = true)) {
        builder.certificatePinner(buildCertificatePinner())
    }
    return builder.build()
}
*/

/**
 * ❓ Follow-up: What happens when a pin doesn't match?
 *    A: OkHttp throws SSLPeerUnverifiedException / CertificatePinningException.
 *       All API calls fail. Users see network errors. Fix: ship app update with
 *       new pin OR disable pinning via remote config (if you planned for it).
 *
 * ❓ Follow-up: XML pinning vs OkHttp pinning — when to use which?
 *    A: XML for app-wide default (WebView, Cronet, default OkHttp if configured).
 *       OkHttp for per-client pins (different APIs, staging vs prod clients).
 *       Use both consistently — mismatched configs cause confusing debug sessions.
 */


// ─────────────────────────────────────────────────────────────────────────────
// 11. PLAY INTEGRITY API (replaces SafetyNet Attestation)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Play Integrity API verifies the app's runtime environment before trusting
 * sensitive operations (payments, premium features, anti-fraud).
 *
 * Verdict tokens (server-side decode only — never trust client-side):
 *   - deviceIntegrity: MEETS_DEVICE_INTEGRITY, MEETS_BASIC_INTEGRITY, etc.
 *   - appIntegrity: PLAY_RECOGNIZED, UNRECOGNIZED_VERSION
 *   - accountDetails: app licensing status
 *
 * vs SafetyNet (deprecated):
 *   SafetyNet Attestation → replaced by Play Integrity
 *   SafetyNet Verify Apps → replaced by Play Protect APIs
 *
 * Architecture:
 *   App → requestIntegrityToken() → Google Play → signed JWT token
 *   App → send token to YOUR backend → decode with Google API → allow/deny
 *
 * ⚠️ Never make security decisions solely on-device. Always verify server-side.
 *
 * 💡 Interview tip: Play Integrity detects rooted/emulated/tampered environments.
 *    It's not foolproof (Magisk hide, custom ROMs) — use as one signal in a
 *    layered defense, not the only gate.
 */

// Client-side: request integrity token before sensitive API call
class IntegrityChecker(
    private val integrityManager: IntegrityManager,
    private val cloudProjectNumber: Long  // from Google Cloud Console
) {
    suspend fun requestToken(): String = suspendCancellableCoroutine { cont ->
        val request = IntegrityTokenRequest.builder()
            .setCloudProjectNumber(cloudProjectNumber)
            .build()

        integrityManager.requestIntegrityToken(request)
            .addOnSuccessListener { response ->
                if (cont.isActive) cont.resume(response.token())
            }
            .addOnFailureListener { e ->
                if (cont.isActive) cont.resumeWithException(e)
            }
    }
}

// Usage in repository — token sent to backend, NOT decoded on device
class PaymentRepository(
    private val api: PaymentApi,
    private val integrityChecker: IntegrityChecker
) {
    suspend fun initiatePayment(amount: Cents): Result<PaymentResult> {
        return try {
            val integrityToken = integrityChecker.requestToken()
            val response = api.createPayment(
                PaymentRequest(amount = amount, integrityToken = integrityToken)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Server-side verification (conceptual — runs on your backend, not Android):
 *
 *   POST https://playintegrity.googleapis.com/v1/{packageName}:decodeIntegrityToken
 *   Body: { "integrityToken": "<token from app>" }
 *
 *   Check response.payload:
 *     deviceIntegrity.deviceRecognitionVerdict contains "MEETS_DEVICE_INTEGRITY"
 *     appIntegrity.appRecognitionVerdict == "PLAY_RECOGNIZED"
 *     requestDetails.requestPackageName matches your app
 *
 * Decision matrix (example):
 *   ┌─────────────────────────────┬────────────────────────────────────┐
 *   │ Verdict                     │ Action                             │
 *   ├─────────────────────────────┼────────────────────────────────────┤
 *   │ Full integrity + recognized │ Allow sensitive operation          │
 *   │ Basic integrity only        │ Allow read-only, block payments    │
 *   │ App unrecognized (sideload) │ Block or limit features            │
 *   │ Token decode fails          │ Reject — possible tampering        │
 *   └─────────────────────────────┴────────────────────────────────────┘
 */

/**
 * ❓ Q6: How does Play Integrity differ from certificate pinning?
 *    A: Pinning = protect network traffic from MITM (transport layer).
 *       Play Integrity = verify app binary + device environment (runtime layer).
 *       Complementary: pin API calls, verify device before high-value actions.
 *
 * ❓ Q7: What if Play Integrity API is unavailable (no Play Store, China devices)?
 *    A: Graceful degradation — don't hard-crash. Fall back to reduced functionality,
 *       server-side rate limiting, or alternate auth. Document the trade-off.
 *
 * ❓ Q8: How do you test Play Integrity in development?
 *    A: Use Play Console → App Integrity → test responses. Register test devices.
 *       Debug builds won't get PLAY_RECOGNIZED — test the failure path too.
 */

// ─────────────────────────────────────────────────────────────────────────────

/**
 * Android Keystore = hardware-backed secure storage for cryptographic keys.
 *
 * Key features:
 *   - Keys never leave the secure hardware (TEE/Strongbox)
 *   - Keys can require user authentication (biometric)
 *   - Keys can have validity periods
 *   - Extraction is computationally infeasible
 *
 * Use cases:
 *   - Encrypting local data
 *   - Signing data for API authentication
 *   - Biometric-gated key access
 */

object KeystoreHelper {

    private const val KEY_ALIAS = "app_encryption_key"

    fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        // Return existing key if available
        keyStore.getKey(KEY_ALIAS, null)?.let { return it as SecretKey }

        // Generate new key
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return keyGenerator.generateKey()
    }

    fun encrypt(data: ByteArray): Pair<ByteArray, ByteArray> {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        return Pair(iv, encrypted)
    }

    fun decrypt(iv: ByteArray, encryptedData: ByteArray): ByteArray {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(encryptedData)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 5. ENCRYPTED STORAGE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * EncryptedSharedPreferences (AndroidX Security):
 *   - AES-256 encryption for keys and values
 *   - Backed by Android Keystore
 *   - Drop-in replacement for SharedPreferences
 */

fun createEncryptedPrefs(context: Context): SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

// Store sensitive tokens
fun storeAuthToken(prefs: SharedPreferences, token: String) {
    prefs.edit().putString("auth_token", token).apply()
    // Stored encrypted on disk — safe even if device is rooted
}


// ─────────────────────────────────────────────────────────────────────────────
// 6. BIOMETRIC AUTHENTICATION
// ─────────────────────────────────────────────────────────────────────────────

class BiometricHelper(private val activity: FragmentActivity) {

    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt(onSuccess, onError)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                onError("No biometric hardware")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                onError("Biometric hardware unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                onError("No biometrics enrolled")
        }
    }

    private fun showBiometricPrompt(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate")
            .setSubtitle("Verify your identity")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    // Don't close dialog — user can retry
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 7. COMMON VULNERABILITIES & MITIGATIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ┌─────────────────────────────┬──────────────────────────────────────────┐
 * │ Vulnerability               │ Mitigation                               │
 * ├─────────────────────────────┼──────────────────────────────────────────┤
 * │ Insecure data storage       │ EncryptedSharedPreferences, Keystore     │
 * │ (plaintext tokens)          │                                          │
 * ├─────────────────────────────┼──────────────────────────────────────────┤
 * │ Man-in-the-middle           │ Certificate pinning, HTTPS only          │
 * │                             │                                          │
 * ├─────────────────────────────┼──────────────────────────────────────────┤
 * │ SQL injection               │ Room parameterized queries (never        │
 * │                             │ concatenate user input in queries)       │
 * ├─────────────────────────────┼──────────────────────────────────────────┤
 * │ Intent spoofing             │ exported=false, explicit intents,        │
 * │                             │ signature permissions                    │
 * ├─────────────────────────────┼──────────────────────────────────────────┤
 * │ WebView vulnerabilities     │ Disable JavaScript when not needed,      │
 * │                             │ validate URLs, disable file access       │
 * ├─────────────────────────────┼──────────────────────────────────────────┤
 * │ Reverse engineering         │ R8 obfuscation, tamper detection,        │
 * │                             │ Play Integrity API                       │
 * ├─────────────────────────────┼──────────────────────────────────────────┤
 * │ Insecure broadcast          │ LocalBroadcastManager (deprecated) →     │
 * │                             │ use SharedFlow. Or signature permission  │
 * ├─────────────────────────────┼──────────────────────────────────────────┤
 * │ Clipboard data theft        │ Don't put sensitive data on clipboard    │
 * │                             │ Use FLAG_SECURE for sensitive screens    │
 * └─────────────────────────────┴──────────────────────────────────────────┘
 */


// ─────────────────────────────────────────────────────────────────────────────
// 8. PROGUARD / R8 — CODE OBFUSCATION
// ─────────────────────────────────────────────────────────────────────────────

/**
 * R8 is the default code shrinker/obfuscator in Android (replaced ProGuard).
 * Same rule format as ProGuard, but faster and Kotlin-aware.
 *
 * Three stages:
 *   1. Shrinking  — removes unused classes, methods, fields
 *   2. Obfuscation — renames classes/members to short meaningless names (a, b, c)
 *   3. Optimization — inlines methods, removes dead branches, simplifies code
 *
 * R8 full mode vs compat mode:
 *   Compat (default): behaves like ProGuard — safe, fewer surprises
 *   Full mode: more aggressive optimizations — may break reflection-based code
 *     Enable: android.enableR8.fullMode=true in gradle.properties
 *
 * Mapping files:
 *   R8 produces mapping.txt (maps obfuscated names → original names).
 *   Upload to Play Console → crash stack traces are auto-deobfuscated.
 *   Keep mapping files for every release build you ship.
 */

// Example proguard-rules.pro
/*
# ── Keep rules ──────────────────────────────────────────────────────

# Keep entire class (class name + all members)
-keep class com.example.model.** { *; }

# Keep class members only (class can be renamed)
-keepclassmembers class com.example.model.User {
    <fields>;
}

# Keep class name only (members can be renamed)
-keepnames class com.example.api.ApiResponse

# ── Common rules needed ─────────────────────────────────────────────

# Retrofit (uses reflection for interface proxies)
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.example.**$$serializer { *; }
-keepclassmembers class com.example.** {
    *** Companion;
}

# Parcelable
-keep class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep R8 from stripping Google Play Integrity / SafetyNet
-keep class com.google.android.play.core.** { *; }
*/


// ─────────────────────────────────────────────────────────────────────────────
// 9. CONTENT PROVIDER SECURITY
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Content Providers expose structured data between apps.
 * Major security concern: they're an IPC attack surface.
 *
 * Key attributes:
 *   exported="false" → only your app can access (default for API 31+ if no intent-filter)
 *   exported="true"  → other apps can query/insert/update/delete
 *
 * Permissions:
 *   android:readPermission  → required to query
 *   android:writePermission → required to insert/update/delete
 *   android:permission      → required for both read and write
 *
 * Path-level permissions (android:pathPermission):
 *   Different permissions for different URI paths.
 *
 * grantUriPermissions:
 *   Allows temporary access to specific URIs even without the full permission.
 *   Essential for sharing files via FileProvider.
 *
 * SQL injection prevention:
 *   NEVER concatenate user input into queries.
 *   Use selection args (parameterized queries) or Room.
 */

// Secure ContentProvider with FileProvider for sharing files
/*
<!-- AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
*/

// Sharing a file securely with another app
fun shareFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        // Grant read-only access to the receiving app
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share file"))
}

// Parameterized queries prevent SQL injection
class SecureProvider : ContentProvider() {

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,  // ← always use selection args
        sortOrder: String?
    ): Cursor? {
        // ✅ Safe: parameterized
        // SELECT * FROM notes WHERE author = ?   selectionArgs = ["Alice"]

        // ❌ Dangerous: string concatenation
        // "SELECT * FROM notes WHERE author = '$userInput'"
        // userInput = "'; DROP TABLE notes; --"

        return db.query("notes", projection, selection, selectionArgs, null, null, sortOrder)
    }

    // ... other required overrides
    override fun onCreate(): Boolean = true
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, sel: String?, selArgs: Array<String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, sel: String?, selArgs: Array<String>?): Int = 0
}


// ─────────────────────────────────────────────────────────────────────────────
// 10. WEBVIEW SECURITY
// ─────────────────────────────────────────────────────────────────────────────

/**
 * WebView is one of the most dangerous components on Android.
 * It's essentially a browser engine with access to native APIs.
 *
 * JavaScript interface risks:
 *   @JavascriptInterface exposes Kotlin/Java methods to JS code.
 *   Only annotated methods are exposed (API 17+), but any loaded page can call them.
 *   ⚠️ Never expose sensitive methods. Validate the calling URL.
 *
 * Common attack vectors:
 *   - XSS via injected scripts in loaded content
 *   - URL scheme hijacking (intent://, javascript://)
 *   - File access (file:// can read local files if enabled)
 *   - Mixed content (HTTPS page loading HTTP resources)
 *   - Deep link manipulation
 *
 * Cookie isolation:
 *   WebView cookies are shared across all WebViews in the app.
 *   Use CookieManager to clear cookies after sensitive sessions.
 *   Third-party cookies are blocked by default on API 33+.
 *
 * Safe Browsing (API 26+):
 *   Google Safe Browsing checks URLs against known malicious sites.
 *   Enabled by default. Shows an interstitial warning for dangerous sites.
 *   Set android:enableSafeBrowsing="true" in manifest (opt-in for older APIs).
 */

class SecureWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    fun setupSecureWebView() {
        webView = WebView(this)

        webView.settings.apply {
            // Only enable JavaScript if absolutely necessary
            javaScriptEnabled = true

            // ❌ Disable file access — prevents file:// attacks
            allowFileAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false

            // ❌ Disable content provider access via WebView
            allowContentAccess = false

            // Block mixed content (HTTPS page loading HTTP resources)
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

            // Disable geolocation
            setGeolocationEnabled(false)
        }

        // Validate URLs before loading
        webView.webViewClient = object : WebViewClient() {
            private val allowedHosts = setOf("example.com", "www.example.com")

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url
                // Block non-HTTPS and untrusted hosts
                if (url.scheme != "https" || url.host !in allowedHosts) {
                    return true  // Block navigation
                }
                return false  // Allow navigation
            }
        }

        // Safe JavaScript interface — minimal exposure
        webView.addJavascriptInterface(SafeBridge(), "AndroidBridge")
    }
}

class SafeBridge {
    @JavascriptInterface
    fun getAppVersion(): String {
        // ✅ Safe: returns non-sensitive info
        return BuildConfig.VERSION_NAME
    }

    // ❌ Never expose methods like: getAuthToken(), executeCommand(), readFile()
}


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: How do you securely store an auth token on Android?
 *    A: Use EncryptedSharedPreferences backed by Android Keystore.
 *       Keys are stored in hardware-backed security (TEE/Strongbox).
 *       Token is AES-256 encrypted at rest. Never store in plain SharedPreferences
 *       or in code/BuildConfig.
 *
 * ❓ Q2: What's the difference between certificate pinning in XML vs OkHttp?
 *    A: XML (network_security_config): declarative, handles backup pins,
 *       expiration dates, debug overrides. Recommended by Google.
 *       OkHttp CertificatePinner: programmatic, more control, but must
 *       handle updates manually. Both prevent MITM attacks.
 *
 * ❓ Q3: How do you handle the "Don't ask again" permission scenario?
 *    A: shouldShowRequestPermissionRationale returns false in two cases:
 *       1. First time asking (never denied) — just request
 *       2. User selected "Don't ask again" — show dialog directing to Settings
 *       Distinguish by tracking if you've asked before (DataStore flag).
 *
 * ❓ Q4: What is FLAG_SECURE and when would you use it?
 *    A: window.setFlags(FLAG_SECURE, FLAG_SECURE) prevents:
 *       - Screenshots of the Activity
 *       - Screen recording of the Activity
 *       - Preview in recent apps (shows blank)
 *       Use for: banking, password, sensitive data screens.
 *
 * ❓ Q5: How does Android Keystore differ from just encrypting with a password?
 *    A: Keystore keys never leave the secure hardware (TEE).
 *       Even root access can't extract the key material.
 *       Password-based encryption: key is in app memory — extractable.
 *       Keystore also supports biometric-gated access.
 *
 * ❓ Q6: How does Play Integrity differ from certificate pinning?
 *    A: Pinning protects transport (MITM). Play Integrity verifies app binary
 *       and device environment at runtime. Use both for layered security.
 *
 * ❓ Q7: What's your pin rotation strategy?
 *    A: Ship current + backup SPKI hash, set XML expiration, coordinate with infra,
 *       remote-config kill switch for emergencies. Never pin only one hash.
 */
