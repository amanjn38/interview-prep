/**
 * ============================================================================
 * 14 — DESIGN PATTERNS & KOTLIN ADVANCED
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   1. Kotlin Sealed Classes & Interfaces
 *   2. Kotlin Delegates (by lazy, observable, custom)
 *   3. Extension Functions & Properties
 *   4. Generics & Variance (in, out, star projection)
 *   5. Kotlin DSL
 *   6. Inline Functions, Reified Types, Crossinline, Noinline
 *   7. Kotlin Scope Functions (let, run, with, apply, also)
 *   8. Design Patterns in Android
 *   9. Value Classes (inline classes)
 *  10. Kotlin Contracts
 *
 * ============================================================================
 */

// ─────────────────────────────────────────────────────────────────────────────
// 1. SEALED CLASSES & INTERFACES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Sealed = restricted hierarchy. All subclasses known at compile time.
 * Compiler can verify exhaustive when expressions.
 *
 * sealed class → can have state (constructor parameters)
 * sealed interface → multiple inheritance, no state in the sealed itself
 */

// Sealed interface for UI state — compiler enforces handling all cases
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
    data object Empty : UiState<Nothing>
}

// Usage — exhaustive when (no else needed)
fun <T> renderState(state: UiState<T>) {
    when (state) {
        is UiState.Loading -> showLoadingSpinner()
        is UiState.Success -> showData(state.data)
        is UiState.Error -> showError(state.message)
        is UiState.Empty -> showEmptyState()
        // Compiler error if any case is missing
    }
}

// Sealed class for network result with data
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class HttpError(val code: Int, val body: String) : NetworkResult<Nothing>()
    data class NetworkError(val exception: IOException) : NetworkResult<Nothing>()
    data class UnknownError(val exception: Throwable) : NetworkResult<Nothing>()

    val isSuccess get() = this is Success

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is HttpError -> throw HttpException(code, body)
        is NetworkError -> throw exception
        is UnknownError -> throw exception
    }

    fun <R> map(transform: (T) -> R): NetworkResult<R> = when (this) {
        is Success -> Success(transform(data))
        is HttpError -> this
        is NetworkError -> this
        is UnknownError -> this
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 2. KOTLIN DELEGATES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Kotlin delegates allow reusing property behavior.
 *
 * Built-in delegates:
 *   by lazy {}       → initialized on first access, thread-safe by default
 *   by Delegates.observable() → notified on every change
 *   by Delegates.vetoable()   → can reject changes
 *   by map            → reads from a Map
 *
 * Custom delegates: implement ReadOnlyProperty or ReadWriteProperty
 */

// by lazy — thread-safe lazy initialization
class ExpensiveClass {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "app.db").build()
    }
    // Created only when first accessed. Thread-safe (LazyThreadSafetyMode.SYNCHRONIZED)
}

// by Delegates.observable — react to changes
class UserPreferences {
    var theme: String by Delegates.observable("light") { _, old, new ->
        if (old != new) {
            applyTheme(new)
            analytics.logThemeChange(old, new)
        }
    }
}

// Custom delegate — SharedPreferences delegate
class PreferenceDelegate<T>(
    private val prefs: SharedPreferences,
    private val key: String,
    private val defaultValue: T
) : ReadWriteProperty<Any?, T> {

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (defaultValue) {
            is Boolean -> prefs.getBoolean(key, defaultValue) as T
            is String -> prefs.getString(key, defaultValue) as T
            is Int -> prefs.getInt(key, defaultValue) as T
            is Long -> prefs.getLong(key, defaultValue) as T
            is Float -> prefs.getFloat(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        prefs.edit().apply {
            when (value) {
                is Boolean -> putBoolean(key, value)
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                else -> throw IllegalArgumentException("Unsupported type")
            }
            apply()
        }
    }
}

// Usage
class Settings(prefs: SharedPreferences) {
    var darkMode: Boolean by PreferenceDelegate(prefs, "dark_mode", false)
    var username: String by PreferenceDelegate(prefs, "username", "")
    var fontSize: Int by PreferenceDelegate(prefs, "font_size", 14)
}


// ─────────────────────────────────────────────────────────────────────────────
// 3. EXTENSION FUNCTIONS & PROPERTIES
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Extensions add functions/properties to existing classes without inheritance.
 * Resolved STATICALLY (at compile time), not dynamically.
 *
 * ⚠️ If the class has a member function with the same signature,
 *    the member function wins (extensions don't override members).
 */

// Context extensions — commonly used in Android
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.dpToPx(dp: Float): Int {
    return (dp * resources.displayMetrics.density).roundToInt()
}

val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

// Flow extensions
fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> = flow {
    var lastEmissionTime = 0L
    collect { value ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEmissionTime >= windowDuration) {
            lastEmissionTime = currentTime
            emit(value)
        }
    }
}

// Result extensions for cleaner error handling
inline fun <T> Result<T>.onEach(
    onSuccess: (T) -> Unit,
    onFailure: (Throwable) -> Unit
): Result<T> {
    this.onSuccess(onSuccess)
    this.onFailure(onFailure)
    return this
}


// ─────────────────────────────────────────────────────────────────────────────
// 4. GENERICS & VARIANCE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Variance defines subtyping relationship for generic types.
 *
 * Invariant (default):
 *   MutableList<Dog> is NOT a subtype of MutableList<Animal>
 *   → Can read AND write
 *
 * Covariant (out):
 *   List<Dog> IS a subtype of List<Animal>
 *   → Can only READ (produce), not write
 *   → Use for return types, immutable collections
 *
 * Contravariant (in):
 *   Comparator<Animal> IS a subtype of Comparator<Dog>
 *   → Can only WRITE (consume), not read
 *   → Use for parameter types, consumers
 *
 * Star projection (*):
 *   List<*> → unknown type, can read as Any?, can't write
 */

// Covariant — produces T
interface Source<out T> {
    fun next(): T  // ✅ Can return T
    // fun add(item: T)  // ❌ Compile error — can't consume T
}

// Contravariant — consumes T
interface Sink<in T> {
    fun put(item: T)  // ✅ Can accept T
    // fun get(): T    // ❌ Compile error — can't produce T
}

// Real-world example
class Repository<out T>(private val items: List<T>) {
    fun getAll(): List<T> = items
    fun getFirst(): T = items.first()
}

// Can assign Repository<Dog> to Repository<Animal>
// val animalRepo: Repository<Animal> = Repository<Dog>(listOf(Dog()))  // ✅

// Generic function with upper bound
fun <T : Comparable<T>> findMax(list: List<T>): T {
    return list.max()
}

// Reified type parameters (requires inline)
inline fun <reified T> isInstanceOf(value: Any): Boolean {
    return value is T  // Only works with reified
}

inline fun <reified T : Activity> Context.startActivity() {
    startActivity(Intent(this, T::class.java))
}
// Usage: context.startActivity<DetailActivity>()


// ─────────────────────────────────────────────────────────────────────────────
// 5. KOTLIN DSL
// ─────────────────────────────────────────────────────────────────────────────

/**
 * DSL = Domain-Specific Language built with Kotlin's lambda-with-receiver.
 * Used extensively: Gradle build scripts, Compose, Ktor, testing.
 */

// Building a simple DSL for network request configuration
class RequestBuilder {
    var url: String = ""
    var method: String = "GET"
    private val headers = mutableMapOf<String, String>()
    private var body: String? = null

    fun headers(block: HeaderBuilder.() -> Unit) {
        HeaderBuilder(headers).apply(block)
    }

    fun body(content: String) { body = content }

    fun build(): Request = Request(url, method, headers, body)
}

class HeaderBuilder(private val headers: MutableMap<String, String>) {
    infix fun String.to(value: String) { headers[this] = value }
}

fun request(block: RequestBuilder.() -> Unit): Request {
    return RequestBuilder().apply(block).build()
}

// Usage — reads like English
val req = request {
    url = "https://api.example.com/articles"
    method = "POST"
    headers {
        "Authorization" to "Bearer token123"
        "Content-Type" to "application/json"
    }
    body("""{"title": "New Article"}""")
}


// ─────────────────────────────────────────────────────────────────────────────
// 6. INLINE FUNCTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * inline → function body is inserted at the call site (no function object created)
 *
 * Benefits:
 *   - No lambda allocation overhead
 *   - Can use reified type parameters
 *   - Non-local returns allowed
 *
 * noinline → prevents specific lambda parameter from being inlined
 * crossinline → prevents non-local returns in lambda used in another context
 */

// inline with reified — avoids reflection
inline fun <reified T> fromJson(json: String): T {
    return Json.decodeFromString<T>(json)  // T is known at compile time
}

// crossinline — lambda used in another execution context
inline fun executeOnBackground(crossinline block: () -> Unit) {
    Thread {
        block()  // crossinline prevents `return` here from exiting outer function
    }.start()
}

// noinline — when you need to store the lambda
inline fun logAndExecute(
    tag: String,
    noinline callback: () -> Unit  // Stored, so can't be inlined
) {
    Log.d(tag, "Executing callback")
    handler.post(callback)  // Stored for later execution
}


// ─────────────────────────────────────────────────────────────────────────────
// 7. SCOPE FUNCTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ┌──────────┬───────────┬────────────┬──────────────────────────┐
 * │ Function │ Object    │ Return     │ Use Case                 │
 * │          │ Reference │ Value      │                          │
 * ├──────────┼───────────┼────────────┼──────────────────────────┤
 * │ let      │ it        │ Lambda     │ Null checks, transforms  │
 * │ run      │ this      │ Lambda     │ Object config + compute  │
 * │ with     │ this      │ Lambda     │ Grouping calls on object │
 * │ apply    │ this      │ Object     │ Object configuration     │
 * │ also     │ it        │ Object     │ Side effects (logging)   │
 * └──────────┴───────────┴────────────┴──────────────────────────┘
 */

fun scopeFunctionExamples() {
    // let — null-safe operations, transformations
    val name: String? = user?.name?.let { name ->
        "Hello, $name"
    }

    // apply — configure objects (returns the object)
    val intent = Intent(context, DetailActivity::class.java).apply {
        putExtra("ID", articleId)
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
    }

    // also — side effects without changing the chain
    val articles = repository.getArticles()
        .also { Log.d("TAG", "Fetched ${it.size} articles") }
        .filter { it.isPublished }

    // run — execute block and return result
    val result = service.run {
        authenticate(username, password)
        fetchData()
    }

    // with — multiple operations on same object
    with(binding) {
        titleText.text = article.title
        bodyText.text = article.content
        authorText.text = article.author
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// 8. DESIGN PATTERNS IN ANDROID
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Observer Pattern:
 *   LiveData, StateFlow, Flow — observe data changes
 *   Compose's state system — UI recomposes on state change
 *
 * Factory Pattern:
 *   ViewModelProvider.Factory — creates ViewModels with dependencies
 *   Retrofit.create() — creates API service implementations
 *
 * Builder Pattern:
 *   OkHttpClient.Builder, Notification.Builder, Room.databaseBuilder
 *   Kotlin: use default parameters + apply() instead of explicit builders
 *
 * Adapter Pattern:
 *   RecyclerView.Adapter — adapts data model to views
 *   Retrofit Converter — adapts JSON to Kotlin objects
 *
 * Repository Pattern:
 *   Abstract data access, provide single source of truth
 *
 * Strategy Pattern:
 *   Different Dispatchers for different work types
 *   Different serializers based on content type
 *
 * Singleton Pattern:
 *   Kotlin's `object` keyword
 *   Hilt's @Singleton scope
 *   Room.databaseBuilder with .build() cached
 *
 * Decorator Pattern:
 *   OkHttp Interceptor chain — each interceptor wraps the next
 *   Compose Modifier chain — each modifier decorates the composable
 */


// ─────────────────────────────────────────────────────────────────────────────
// 9. VALUE CLASSES (INLINE CLASSES)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Value classes wrap a single value without runtime overhead.
 * The wrapper is erased at compile time — zero-cost abstraction.
 *
 * Use cases: type-safe IDs, units, branded types
 */

@JvmInline
value class UserId(val value: String) {
    init { require(value.isNotBlank()) { "UserId cannot be blank" } }
}

@JvmInline
value class ArticleId(val value: String)

@JvmInline
value class Pixels(val value: Int)

@JvmInline
value class Dp(val value: Float)

// Type safety: can't accidentally pass ArticleId where UserId is expected
fun getUser(id: UserId): User = TODO()
fun getArticle(id: ArticleId): Article = TODO()

// At runtime, these are just String/Int — no wrapper object allocated


// ─────────────────────────────────────────────────────────────────────────────
// 10. KOTLIN CONTRACTS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Contracts = compiler hints that tell the Kotlin compiler about
 * function behavior it can't otherwise infer.
 *
 * Why they exist:
 *   The compiler can't always analyze function bodies — especially for
 *   higher-order functions or custom check functions. Contracts bridge this gap.
 *
 * Two main contract types:
 *
 * 1. callsInPlace — tells compiler how many times a lambda is invoked
 *    → Enables smart initialization analysis
 *    → Used by: run, let, apply, also, with, synchronized
 *
 * 2. returns(true/false) implies (condition) — conditional effects
 *    → Enables smart casts after custom check functions
 *    → Used by: require(), check(), isNullOrEmpty(), isNullOrBlank()
 */

// callsInPlace contract — enables definite initialization
@OptIn(ExperimentalContracts::class)
inline fun <R> executeExactlyOnce(block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}

// Without contract, this would be a compile error:
//   "Variable 'result' must be initialized"
fun demonstrateCallsInPlace() {
    val result: String
    executeExactlyOnce {
        result = "initialized"  // Compiler trusts this runs exactly once
    }
    println(result)  // ✅ Compiles — compiler knows result is initialized
}

// returns implies contract — enables smart casts
@OptIn(ExperimentalContracts::class)
fun requireNotBlank(value: String?): Boolean {
    contract {
        returns(true) implies (value != null)
    }
    return value != null && value.isNotBlank()
}

fun demonstrateReturnsImplies(name: String?) {
    if (requireNotBlank(name)) {
        // ✅ Smart cast: name is String (not String?) here
        println(name.length)
    }
}

// Custom type check with contract
@OptIn(ExperimentalContracts::class)
fun Any?.isNonNullString(): Boolean {
    contract {
        returns(true) implies (this@isNonNullString is String)
    }
    return this is String
}

fun demonstrateTypeCheck(value: Any?) {
    if (value.isNonNullString()) {
        // ✅ Smart cast: value is String here
        println(value.uppercase())
    }
}

/**
 * How stdlib uses contracts (why require/check enable smart casts):
 *
 *   public inline fun require(value: Boolean) {
 *       contract { returns() implies value }
 *       if (!value) throw IllegalArgumentException()
 *   }
 *
 *   public inline fun checkNotNull<T>(value: T?): T {
 *       contract { returns() implies (value != null) }
 *       if (value == null) throw IllegalStateException()
 *       return value
 *   }
 *
 * ⚠️ Contracts are experimental (as of Kotlin 2.x) and unchecked by the compiler —
 *   the compiler trusts them. A wrong contract can lead to unsound code.
 *
 * 💡 Interview tip: You don't need to write contracts often, but understanding
 *   them explains why require(), check(), and scope functions work so smoothly
 *   with Kotlin's type system.
 */


// ─────────────────────────────────────────────────────────────────────────────
// PRACTICE QUESTIONS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ❓ Q1: When would you use sealed interface vs sealed class?
 *    A: sealed interface when you need multiple inheritance or don't need
 *       shared state. sealed class when subtypes share common properties.
 *       Prefer sealed interface for ADTs (algebraic data types) like UiState.
 *
 * ❓ Q2: Explain covariance with a real Android example.
 *    A: List<Button> can be assigned to List<View> because List is covariant
 *       (declared as List<out E>). You can read Views from it but can't add
 *       Buttons to a List<View> reference.
 *
 * ❓ Q3: Why is lazy initialization important on Android?
 *    A: Reduces cold start time — don't initialize expensive objects until
 *       needed. Example: by lazy for Database, Retrofit, heavy computations.
 *       Thread-safe by default (SYNCHRONIZED mode).
 *
 * ❓ Q4: What's the difference between object and companion object?
 *    A: object: singleton, exists independently. Has its own class.
 *       companion object: associated with a class, one per class.
 *       Members accessible via ClassName.member. Can implement interfaces.
 *
 * ❓ Q5: How do extension functions work under the hood?
 *    A: Compiled to static functions with the receiver as the first parameter.
 *       fun String.addExclamation() → StringKt.addExclamation(String str)
 *       Resolved statically — no virtual dispatch. If class has member with
 *       same signature, member wins.
 */
