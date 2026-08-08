/**
 * ============================================================================
 * 17 — AMIT SHEKHAR'S ANDROID INTERVIEW QUESTIONS — DETAILED ANSWERS
 * Google SDE-2 Interview Prep
 * ============================================================================
 *
 * Topics Covered:
 *   Section 1:  Kotlin Coroutines (→ 04_coroutines)
 *   Section 2:  Kotlin Flow API (→ 04_coroutines)
 *   Section 3:  Kotlin Language
 *   Section 4:  Android — Base (→ 01_fundamentals)
 *   Section 5:  Activity and Fragment (→ 01_fundamentals)
 *   Section 6:  Views and ViewGroups
 *   Section 7:  RecyclerView
 *   Section 8:  Intents, Services, and IPC
 *   Section 9:  Long-Running Operations & Memory
 *   Section 10: Data Saving (→ 05_data_persistence)
 *   Section 11: Android Libraries
 *   Section 12: Android Architecture (→ 02_app_architecture)
 *   Section 13: Design Patterns
 *   Section 14: Android System Design
 *   Section 15: Android Unit Testing (→ 11_testing)
 *   Section 16: Android Tools and Technologies
 *   Section 17: Jetpack Compose (→ 03_jetpack_compose)
 *   Section 18: Java (Brief Interview Answers)
 *   Section 19: Android Jetpack
 *   Section 20: Other Topics
 *
 * TRIMMED: Sections 1,2,4,5,10,12,15,17 are stubs pointing to files 01–11.
 *         Unique content kept in sections 3,6,7,8,9,11,13,14,16,18,19,20.
 * Source: amitshekhar.me — curated and expanded with code examples.
 *
 * ── NAVIGATION ──────────────────────────────────────────────────────────────
 * Use Ctrl+F (or IDE symbol search) to jump to sections. Line numbers are
 * approximate anchors for quick navigation.
 *
 * | Section | Topic                        | Line  | Overlap with files 01–16?     |
 * |---------|------------------------------|-------|-------------------------------|
 * | 1       | Kotlin Coroutines            | ~34   | Yes → prefer 04_coroutines    |
 * | 2       | Kotlin Flow API              | ~736  | Yes → prefer 04_coroutines    |
 * | 3       | Kotlin Language              | ~1220 | Partial → see 14_design_patterns |
 * | 4       | Android — Base               | ~2362 | Yes → prefer 01_fundamentals  |
 * | 5       | Activity and Fragment        | ~2667 | Yes → prefer 01_fundamentals  |
 * | 6       | Views and ViewGroups         | ~3254 | Legacy Views — unique depth   |
 * | 7       | RecyclerView                 | ~3583 | Legacy Views — unique depth   |
 * | 8       | Intents, Services, and IPC   | ~4113 | Partial → see 01, 09          |
 * | 9       | Long-Running Ops & Memory    | ~4713 | Partial → see 07, 08          |
 * | 10      | Data Saving                  | ~5138 | Yes → prefer 05_data_persistence |
 * | 11      | Android Libraries            | ~5541 | Partial                       |
 * | 12      | Android Architecture         | ~6052 | Yes → prefer 02_app_architecture |
 * | 13      | Design Patterns              | ~6387 | Partial → see 14              |
 * | 14      | Android System Design        | ~6831 | Yes → prefer 15_system_design |
 * | 15      | Android Unit Testing         | ~7282 | Yes → prefer 11_testing       |
 * | 16      | Android Tools & Technologies | ~7680 | Unique — Gradle, Play Console |
 * | 17      | Jetpack Compose              | ~8094 | Yes → prefer 03_jetpack_compose |
 * | 18      | Java (Brief Answers)         | ~9083 | Unique — Java interop round   |
 * | 19      | Android Jetpack              | ~9454 | Partial → spread across 01–15 |
 * | 20      | Other Topics                 | ~9776 | Misc gap-fill                 |
 *
 * 💡 When to use this file vs 01–16:
 *    - Use 01–16 for primary study (better organized, interview tips, gotchas)
 *    - Use 17 for: RecyclerView internals, legacy View system, Java answers,
 *      Gradle/Play Console tooling, and quick Q&A lookup before interviews
 *    - Skip sections marked "Yes → prefer X" unless you need a second angle
 *
 * ============================================================================
 */
// ────────────────────────────────────────────────────────────────────────────
// SECTION 1: KOTLIN COROUTINES
// ⛔ STUB — Full content removed (duplicates 04_coroutines_and_flows.kt)
// 📖 Primary reference: 04_coroutines_and_flows.kt
// ────────────────────────────────────────────────────────────────────────────

/**
 * This section duplicated content from 04_coroutines_and_flows.kt.
 * Read that file for interview tips (💡), gotchas (⚠️), and follow-ups (❓).
 *
 * Quick topics covered there:
 *   - CPS transformation & suspend functions
 *   - Structured concurrency guarantees
 *   - Exception handling in coroutines
 *
 * Use this file (17) for UNIQUE sections only — see header navigation table.
 */

// ────────────────────────────────────────────────────────────────────────────
// SECTION 2: KOTLIN FLOW API
// ⛔ STUB — Full content removed (duplicates 04_coroutines_and_flows.kt)
// 📖 Primary reference: 04_coroutines_and_flows.kt
// ────────────────────────────────────────────────────────────────────────────

/**
 * This section duplicated content from 04_coroutines_and_flows.kt.
 * Read that file for interview tips (💡), gotchas (⚠️), and follow-ups (❓).
 *
 * Quick topics covered there:
 *   - Cold Flow vs hot StateFlow/SharedFlow
 *   - stateIn / shareIn operators
 *   - callbackFlow bridging
 *
 * Use this file (17) for UNIQUE sections only — see header navigation table.
 */

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 3: KOTLIN LANGUAGE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Q1: What is the advantage of using const in Kotlin?
 *
 * `const val` is a compile-time constant. The value is inlined at every usage
 * site at compile time, so there's no runtime field access or getter call.
 *
 * Requirements: must be a primitive type or String, must be at top-level or
 * in an object/companion object, must be initialized with a value (no function
 * calls).
 *
 * `val` is a runtime constant — the value is computed once but requires a
 * field read at each usage site.
 *
 * Advantage: const val avoids field access overhead and class loading — the
 * literal value is baked into the bytecode at the call site.
 */

// const — value "42" is inlined everywhere this is used
const val MAX_RETRIES = 42

object Config {
    const val API_URL = "https://api.example.com"  // inlined as literal string
    val startTime = System.currentTimeMillis()      // computed at runtime
}

// In bytecode, Config.API_URL becomes the literal "https://api.example.com"
// Config.startTime requires Config.INSTANCE.getStartTime() call

/**
 * Q2: When to use lateinit keyword in Kotlin?
 *
 * Use lateinit when you cannot initialize a var in the constructor but
 * guarantee it will be initialized before use. Common cases:
 *   - Dependency injection (Dagger/Hilt @Inject fields)
 *   - View binding in Activities (initialized in onCreate)
 *   - Test setup (@Before methods)
 *
 * Rules:
 *   - Only works with var (not val).
 *   - Only works with non-null, non-primitive types.
 *   - Accessing before initialization throws UninitializedPropertyAccessException.
 */

class MyActivity : AppCompatActivity() {
    @Inject lateinit var userRepository: UserRepository
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

/**
 * Q3: lateinit vs lazy in Kotlin
 *
 * lateinit:
 *   - var only, mutable, can be reassigned.
 *   - Non-null, non-primitive types only.
 *   - No backing initializer — you set it manually later.
 *   - Throws if accessed before initialization.
 *
 * lazy { }:
 *   - val only, immutable.
 *   - Works with any type including primitives and nullables.
 *   - Initializer runs on FIRST access, result is cached.
 *   - Thread-safe by default (LazyThreadSafetyMode.SYNCHRONIZED).
 *
 * Use lateinit when initialization timing is controlled externally (DI, lifecycle).
 * Use lazy when you want deferred, on-demand initialization.
 */

class Example {
    lateinit var injectedService: Service    // set by DI framework

    val heavyObject: ExpensiveParser by lazy {
        ExpensiveParser()                     // created on first access
    }

    val threadUnsafeLazy: Regex by lazy(LazyThreadSafetyMode.NONE) {
        Regex("[a-z]+")                       // faster, but not thread-safe
    }
}

/**
 * Q4: How to check if a lateinit variable has been initialized?
 *
 * Use the ::property.isInitialized check. This only works from inside the
 * class that declares the lateinit property (or from an extension of it).
 */

class LateinitCheckExample {
    lateinit var data: String

    fun printDataIfReady() {
        if (::data.isInitialized) {
            println(data)
        } else {
            println("Data not initialized yet")
        }
    }
}

/**
 * Q5: What is an inline function in Kotlin?
 *
 * An inline function's body (and its lambda parameters) are COPIED into the
 * call site at compile time. This eliminates:
 *   - The overhead of creating a Function object for each lambda.
 *   - The overhead of a virtual method call.
 *
 * Allows non-local returns from lambdas (return from the enclosing function).
 *
 * Use for: Higher-order functions with lambdas that are called frequently,
 * especially in performance-critical code. The standard library uses inline
 * extensively (let, apply, also, forEach, etc.).
 *
 * Tradeoff: Increases bytecode size. Don't inline large function bodies.
 */

inline fun measureTime(block: () -> Unit): Long {
    val start = System.nanoTime()
    block()  // lambda body is inlined at the call site
    return System.nanoTime() - start
}

// At the call site, there's NO Function object allocation:
fun usage() {
    val time = measureTime {
        heavyComputation()
    }
    // Compiled as if we wrote:
    // val start = System.nanoTime()
    // heavyComputation()
    // val time = System.nanoTime() - start
}

/**
 * Q6: What is the reified keyword in Kotlin?
 *
 * Normally, generic type parameters are erased at runtime (type erasure).
 * `reified` preserves the type information at runtime, but only works
 * with inline functions.
 *
 * This lets you use T::class, `is T`, and other runtime type operations
 * that are normally impossible with generics.
 */

inline fun <reified T> isInstanceOf(value: Any): Boolean {
    return value is T  // impossible without reified — T is erased normally
}

inline fun <reified T : Activity> Context.startActivity() {
    val intent = Intent(this, T::class.java)  // T::class requires reified
    startActivity(intent)
}

// Usage — no need to pass Class<T> explicitly:
// context.startActivity<DetailActivity>()

// Gson example:
inline fun <reified T> Gson.fromJson(json: String): T {
    return fromJson(json, T::class.java)
}
// val user: User = gson.fromJson(jsonString)

/**
 * Q7: noinline in Kotlin
 *
 * When a function is inline, ALL its lambda parameters are inlined by default.
 * Use `noinline` on a specific lambda parameter to prevent it from being inlined.
 *
 * Why? An inlined lambda cannot be stored in a variable, passed to another
 * function, or returned. If you need to do that, mark it noinline.
 */

inline fun doWork(
    inlinedAction: () -> Unit,
    noinline storedAction: () -> Unit  // won't be inlined — can be stored
) {
    inlinedAction()                    // inlined at call site
    saveLater(storedAction)            // passed to another function — must be noinline
}

/**
 * Q8: crossinline in Kotlin
 *
 * Inline lambdas allow non-local returns (return from the enclosing function).
 * If the lambda is invoked in a different execution context (e.g., inside
 * another lambda or a Runnable), non-local return is not safe.
 *
 * `crossinline` marks a lambda parameter as inlined BUT prohibits non-local
 * returns from it. The lambda body is still inlined for performance.
 */

inline fun runOnThread(crossinline action: () -> Unit) {
    Thread {
        action()  // inlined, but non-local return is forbidden
        // action() runs inside Thread's Runnable — a different context
    }.start()
}

// Without crossinline, a `return` inside action() would try to return
// from the calling function, which is impossible from a different thread.

/**
 * Q9: What are companion objects in Kotlin?
 *
 * A companion object is a singleton object tied to a class. It's Kotlin's
 * replacement for Java's static members. There is exactly one companion
 * object per class. Members can be accessed via the class name.
 *
 * Under the hood, companion object members become static members of the
 * enclosing class (when using @JvmStatic).
 */

class User private constructor(val name: String) {
    companion object Factory {
        fun create(name: String): User {
            require(name.isNotBlank()) { "Name must not be blank" }
            return User(name)
        }

        const val MAX_NAME_LENGTH = 50
    }
}

// Usage:
// val user = User.create("Alice")
// val maxLen = User.MAX_NAME_LENGTH

// Companion objects can implement interfaces:
interface JsonFactory<T> {
    fun fromJson(json: String): T
}

class Product(val id: Int) {
    companion object : JsonFactory<Product> {
        override fun fromJson(json: String): Product {
            return Product(json.hashCode())
        }
    }
}

/**
 * Q10: Extension functions in Kotlin
 *
 * Extension functions add new functionality to existing classes without
 * inheriting from them or using design patterns like Decorator.
 *
 * They are resolved STATICALLY at compile time (based on the declared type
 * of the variable, not the runtime type). This means they don't support
 * polymorphism.
 *
 * Under the hood, an extension function is compiled into a static method
 * with the receiver as the first parameter.
 */

// Extension function on String
fun String.isValidEmail(): Boolean {
    return this.matches(Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
}

// Extension property
val String.wordCount: Int
    get() = this.trim().split("\\s+".toRegex()).size

// Usage:
// "test@email.com".isValidEmail()  // true
// "hello world".wordCount          // 2

// Compiled to (simplified):
// public static boolean isValidEmail(String $this) { ... }

// IMPORTANT: Extension functions are resolved statically:
open class Shape
class Circle : Shape()

fun Shape.name() = "Shape"
fun Circle.name() = "Circle"

fun printName(s: Shape) {
    println(s.name())  // Always prints "Shape" — resolved on declared type Shape
}
// printName(Circle())  → prints "Shape", NOT "Circle"

/**
 * Q11: What is a data class in Kotlin?
 *
 * A data class auto-generates: equals(), hashCode(), toString(), copy(),
 * and componentN() functions based on the PRIMARY constructor properties.
 *
 * Rules:
 *   - Primary constructor must have at least one parameter.
 *   - All primary constructor parameters must be val or var.
 *   - Cannot be abstract, open, sealed, or inner.
 *   - Properties declared in the body are NOT included in generated methods.
 */

data class Point(val x: Double, val y: Double) {
    var label: String = ""  // NOT included in equals/hashCode/toString
}

fun dataClassDemo() {
    val p1 = Point(1.0, 2.0)
    val p2 = Point(1.0, 2.0)

    println(p1 == p2)           // true — structural equality (equals)
    println(p1)                  // Point(x=1.0, y=2.0) — toString
    val p3 = p1.copy(y = 5.0)  // Point(x=1.0, y=5.0) — copy with modification

    val (x, y) = p1             // destructuring via componentN()
}

/**
 * Q12: Remove duplicates from an array in Kotlin
 *
 * Multiple approaches depending on the need:
 */

fun removeDuplicatesExamples() {
    val list = listOf(1, 2, 2, 3, 3, 3, 4)

    val unique1 = list.distinct()               // [1, 2, 3, 4] — preserves order
    val unique2 = list.toSet().toList()          // [1, 2, 3, 4] — via Set
    val unique3 = list.toMutableSet()            // MutableSet {1, 2, 3, 4}

    // Remove duplicates by property:
    data class Person(val name: String, val age: Int)
    val people = listOf(Person("A", 25), Person("B", 25), Person("A", 30))
    val uniqueByName = people.distinctBy { it.name }  // [Person(A,25), Person(B,25)]
}

/**
 * Q13: What is a @JvmStatic Annotation in Kotlin?
 *
 * @JvmStatic generates an actual static method in the bytecode for a function
 * defined in a companion object or named object. Without it, Java callers must
 * go through the companion object instance: User.Companion.create().
 * With @JvmStatic, Java can call User.create() directly.
 */

class User2(val name: String) {
    companion object {
        @JvmStatic
        fun create(name: String) = User2(name)

        fun createNoStatic(name: String) = User2(name)
    }
}

// From Java:
// User2.create("Alice")               → works with @JvmStatic
// User2.Companion.createNoStatic("B") → without @JvmStatic

/**
 * Q14: What is a @JvmField Annotation in Kotlin?
 *
 * @JvmField exposes a Kotlin property as a public FIELD in Java (no getter/setter).
 * Normally, Kotlin properties are compiled with private fields + getter/setter.
 * @JvmField removes the getter/setter and makes the field public.
 *
 * Commonly used for: constants in companion objects, Parcelable CREATOR, and
 * public fields needed for frameworks that use reflection on fields.
 */

class Config2 {
    companion object {
        @JvmField val DEFAULT_TIMEOUT = 30_000L
    }
}

// From Java:
// Config2.DEFAULT_TIMEOUT  → direct field access (no getter)

/**
 * Q15: What is a @JvmOverloads Annotation in Kotlin?
 *
 * @JvmOverloads generates overloaded methods in Java for each default parameter.
 * Kotlin supports default arguments, but Java doesn't — this bridges the gap.
 */

class Dialog {
    @JvmOverloads
    fun show(
        title: String,
        message: String = "Default message",
        cancelable: Boolean = true
    ) { /* ... */ }
}

// Generates in Java:
// show(String title)
// show(String title, String message)
// show(String title, String message, boolean cancelable)

/**
 * Q16: Scope functions in Kotlin (let, run, with, apply, also)
 *
 * All five take an object and execute a block of code on it. They differ in:
 *   1. How they reference the object: `this` (receiver) vs `it` (argument)
 *   2. What they return: the lambda result vs the object itself
 *
 * |  Function  | Object ref | Returns       | Use case                              |
 * |------------|-----------|---------------|---------------------------------------|
 * | let        | it        | lambda result | null checks, transform, scoping       |
 * | run        | this      | lambda result | object config + compute result        |
 * | with       | this      | lambda result | grouping calls on an object           |
 * | apply      | this      | the object    | object configuration (builder-style)  |
 * | also       | it        | the object    | side effects (logging, validation)    |
 */

fun scopeFunctionExamples() {
    // let — null safety, transformation
    val length: Int? = nullableString?.let {
        println("Processing: $it")
        it.length  // returned
    }

    // apply — configure an object, returns the object
    val intent = Intent(context, DetailActivity::class.java).apply {
        putExtra("KEY_ID", 42)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    // also — side effects, returns the object
    val user = repository.getUser().also {
        Log.d("TAG", "Fetched user: ${it.name}")
    }

    // run — execute a block on an object, return the result
    val result = service.run {
        connect()
        fetchData()
    }

    // with — same as run but takes the object as argument
    val description = with(user) {
        "$name is $age years old"
    }
}

/**
 * Q17: What is an init block in Kotlin?
 *
 * The init block runs during object construction, after the primary constructor
 * parameters are available but as part of the initialization sequence.
 *
 * Execution order:
 *   1. Primary constructor parameters
 *   2. Property initializers and init blocks (in order of appearance)
 *   3. Secondary constructor body (if any)
 *
 * You can have multiple init blocks — they run top to bottom.
 */

class DatabaseHelper(private val context: Context, dbName: String) {
    private val db: Database

    init {
        require(dbName.isNotBlank()) { "DB name cannot be blank" }
        db = Database.open(context, dbName)
    }

    init {
        db.runMigrations()  // runs after the first init block
    }
}

/**
 * Q18: Difference between == and === in Kotlin
 *
 * ==  → Structural equality. Calls equals() under the hood.
 *       null == null is true. Null-safe: x == y where x is null returns false (not NPE).
 *
 * === → Referential equality. Checks if two references point to the same object
 *       in memory. Equivalent to Java's ==.
 */

fun equalityDemo() {
    val a = "Hello"
    val b = "Hello"
    val c = String("Hello".toCharArray())

    println(a == b)   // true  — same content (structural)
    println(a === b)  // true  — string pool optimization (same reference)
    println(a == c)   // true  — same content
    println(a === c)  // false — different objects in memory
}

/**
 * Q19: What are higher-order functions in Kotlin?
 *
 * A higher-order function is a function that takes another function as a
 * parameter or returns a function. This enables functional programming
 * patterns like map, filter, fold, and custom DSLs.
 *
 * Kotlin's standard library is full of higher-order functions: map, filter,
 * forEach, let, apply, etc.
 */

// Function that takes a function parameter
fun operateOnNumbers(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}

fun higherOrderDemo() {
    val sum = operateOnNumbers(5, 3) { a, b -> a + b }    // 8
    val product = operateOnNumbers(5, 3) { a, b -> a * b } // 15
}

/**
 * Q20: Write a function (Higher-Order Function) that returns a function
 */

fun multiplier(factor: Int): (Int) -> Int {
    return { number -> number * factor }
}

fun returnFunctionDemo() {
    val triple = multiplier(3)
    val double = multiplier(2)

    println(triple(5))  // 15
    println(double(5))  // 10

    // Can also call directly:
    println(multiplier(4)(10))  // 40
}

// More practical example — function factory for validators:
fun lengthValidator(min: Int, max: Int): (String) -> Boolean {
    return { input -> input.length in min..max }
}

// val usernameValidator = lengthValidator(3, 20)
// usernameValidator("Alice") → true

/**
 * Q21: What are Lambdas in Kotlin?
 *
 * A lambda is an anonymous function literal — a function without a name that
 * can be passed as an expression. Lambdas are enclosed in { }.
 *
 * Syntax: { parameters -> body }
 * If there's one parameter, you can use `it` as the implicit name.
 * The last expression is the return value.
 */

fun lambdaExamples() {
    // Full syntax
    val sum: (Int, Int) -> Int = { a: Int, b: Int -> a + b }

    // Type inference
    val multiply = { a: Int, b: Int -> a * b }

    // Single parameter — use `it`
    val doubled = listOf(1, 2, 3).map { it * 2 }

    // Trailing lambda — if lambda is the last parameter, move it outside ()
    listOf(1, 2, 3).filter { it > 1 }.map { it.toString() }

    // Lambda with receiver — `this` refers to the receiver
    val greet: StringBuilder.() -> Unit = {
        append("Hello, ")
        append("World!")
    }
    val message = StringBuilder().apply(greet).toString()
}

/**
 * Q22: associateBy — List to Map in Kotlin
 *
 * associateBy { } converts a list to a map using a key selector function.
 * If duplicate keys exist, the LAST value wins.
 */

fun associateByDemo() {
    data class Employee(val id: Int, val name: String, val dept: String)

    val employees = listOf(
        Employee(1, "Alice", "Eng"),
        Employee(2, "Bob", "Design"),
        Employee(3, "Carol", "Eng")
    )

    // Key = id, Value = Employee
    val byId: Map<Int, Employee> = employees.associateBy { it.id }
    // {1=Employee(1,Alice,Eng), 2=Employee(2,Bob,Design), 3=Employee(3,Carol,Eng)}

    // Key = id, Value = name (with value transform)
    val idToName: Map<Int, String> = employees.associateBy({ it.id }, { it.name })
    // {1=Alice, 2=Bob, 3=Carol}

    // Related: groupBy — returns Map<K, List<V>> (keeps all values per key)
    val byDept: Map<String, List<Employee>> = employees.groupBy { it.dept }
    // {Eng=[Alice, Carol], Design=[Bob]}
}

/**
 * Q23: open keyword in Kotlin
 *
 * In Kotlin, classes and members are FINAL by default (unlike Java).
 * The `open` keyword makes a class inheritable or a function/property overridable.
 *
 * This is by design — Effective Java Item 19: "Design and document for
 * inheritance or else prohibit it."
 */

open class Animal(val name: String) {
    open fun sound(): String = "..."     // can be overridden
    fun breathe() { /* ... */ }           // final — cannot be overridden
}

class Dog(name: String) : Animal(name) {
    override fun sound(): String = "Woof"
    // override fun breathe() { }  → ERROR: breathe is final
}

/**
 * Q24: internal visibility modifier in Kotlin
 *
 * `internal` means visible within the same MODULE. A module is a set of
 * Kotlin files compiled together:
 *   - An IntelliJ IDEA module
 *   - A Gradle source set (e.g., :app, :core, :network)
 *
 * This is more useful than Java's package-private because it respects
 * module boundaries rather than package boundaries.
 *
 * Kotlin visibility modifiers:
 *   public    — default, visible everywhere
 *   private   — visible inside the file (top-level) or class
 *   protected — visible in the class + subclasses
 *   internal  — visible within the same module
 */

internal class InternalApi {
    internal fun doWork() { /* visible within the module */ }
}

/**
 * Q25: partition — filtering function in Kotlin
 *
 * partition { } splits a collection into two lists based on a predicate:
 * the first list contains elements matching the predicate, the second doesn't.
 * Returns a Pair<List<T>, List<T>>.
 */

fun partitionDemo() {
    val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val (evens, odds) = numbers.partition { it % 2 == 0 }
    // evens = [2, 4, 6, 8, 10]
    // odds  = [1, 3, 5, 7, 9]

    val users = listOf(User("A", true), User("B", false), User("C", true))
    val (active, inactive) = users.partition { it.isActive }
}

/**
 * Q26: Infix notation in Kotlin
 *
 * Infix functions can be called without dots and parentheses, making code
 * read more naturally. Requirements:
 *   - Must be a member or extension function
 *   - Must have exactly one parameter
 *   - Marked with the `infix` keyword
 */

infix fun Int.power(exponent: Int): Long {
    return Math.pow(this.toDouble(), exponent.toDouble()).toLong()
}

fun infixDemo() {
    val result = 2 power 10  // 1024 — infix call
    // Same as: 2.power(10)

    // Standard library infix examples:
    val pair = "key" to "value"       // Pair("key", "value")
    val contains = 5 in listOf(1, 5)  // true
    val range = 1 until 10            // IntRange 1..9
}

/**
 * Q27: How does the Kotlin Multiplatform work?
 *
 * Kotlin Multiplatform (KMP) allows sharing code across platforms (Android,
 * iOS, Desktop, Web) while keeping platform-specific implementations separate.
 *
 * Architecture:
 *   - commonMain: Shared code (business logic, models, networking with Ktor,
 *     serialization, coroutines). Uses expect/actual for platform APIs.
 *   - androidMain: Android-specific implementations (actual declarations).
 *   - iosMain: iOS-specific implementations (compiled to native framework).
 *
 * expect/actual mechanism:
 *   - `expect` in common code declares an API contract.
 *   - `actual` in platform code provides the implementation.
 *
 * KMP compiles to JVM bytecode for Android and native code (LLVM) for iOS.
 */

// In commonMain:
expect fun platformName(): String

expect class PlatformLogger() {
    fun log(message: String)
}

// In androidMain:
actual fun platformName(): String = "Android"

actual class PlatformLogger actual constructor() {
    actual fun log(message: String) {
        android.util.Log.d("KMP", message)
    }
}

// In iosMain:
// actual fun platformName(): String = "iOS"

/**
 * Q28: String vs StringBuffer vs StringBuilder
 *
 * String      — Immutable. Every modification creates a new object.
 *               Use for fixed strings.
 *
 * StringBuilder — Mutable, NOT thread-safe. More efficient for building
 *                 strings in a single thread. Use for most cases.
 *
 * StringBuffer  — Mutable, THREAD-SAFE (synchronized methods).
 *                 Slower due to synchronization. Use only in multi-threaded contexts.
 *
 * In Kotlin: Use buildString { } which uses StringBuilder internally.
 */

fun stringBuildingDemo() {
    // Kotlin idiomatic way:
    val result = buildString {
        append("Hello")
        append(", ")
        append("World")
        appendLine("!")
    }

    // Equivalent to:
    val sb = StringBuilder()
    sb.append("Hello").append(", ").append("World").appendLine("!")
    val result2 = sb.toString()
}

/**
 * Q29: What is the difference between val and var?
 *
 * val — read-only reference (like Java's final). The reference cannot be
 *       reassigned, but the object it points to can still be mutable.
 *
 * var — mutable reference. Can be reassigned to a different object.
 *
 * val does NOT mean immutable — it means the variable can't be reassigned.
 * A val list can still have items added/removed.
 */

fun valVsVar() {
    val name = "Alice"
    // name = "Bob"  → ERROR: val cannot be reassigned

    var counter = 0
    counter = 1  // OK

    val list = mutableListOf(1, 2, 3)
    list.add(4)  // OK — the list object is mutable, val is the reference
    // list = mutableListOf(5)  → ERROR: val cannot be reassigned
}

/**
 * Q30: What are the visibility modifiers in Kotlin?
 *
 * public    — visible everywhere. DEFAULT in Kotlin (vs package-private in Java).
 * private   — top-level: visible within the file. Class member: visible within
 *             the class only.
 * protected — visible within the class and subclasses. NOT visible in the same
 *             package (unlike Java).
 * internal  — visible within the same module (Gradle module/source set).
 *
 * Key differences from Java:
 *   - Default is public (Java: package-private)
 *   - No package-private in Kotlin (use internal instead)
 *   - protected does NOT include same-package access
 */

/**
 * Q31: What is the equivalent of Java static methods in Kotlin?
 *
 * Kotlin doesn't have a `static` keyword. Alternatives:
 *   1. Companion object — most common, mimics static.
 *   2. Top-level functions — for utility functions, preferred when no class context.
 *   3. Object declaration — for singletons.
 *   4. @JvmStatic — makes companion object methods truly static in bytecode.
 */

// 1. Companion object
class MathUtils {
    companion object {
        @JvmStatic fun add(a: Int, b: Int) = a + b
    }
}
// MathUtils.add(1, 2)

// 2. Top-level function (in a file called Utils.kt)
fun formatCurrency(amount: Double): String = "$${String.format("%.2f", amount)}"
// Called as: formatCurrency(19.99) — in Java: UtilsKt.formatCurrency(19.99)

// 3. Object declaration
object Logger {
    fun log(message: String) { println(message) }
}
// Logger.log("Hello")

/**
 * Q32: How to create a Singleton class in Kotlin?
 *
 * Use the `object` keyword. Kotlin guarantees thread-safe lazy initialization.
 * Under the hood, it uses a static INSTANCE field with a static initializer.
 */

object AppConfig {
    var isDarkMode: Boolean = false
    val version: String = "1.0.0"

    fun reset() {
        isDarkMode = false
    }
}

// Usage: AppConfig.isDarkMode = true
// In Java: AppConfig.INSTANCE.setDarkMode(true)

/**
 * Q33: What is the difference between open and public in Kotlin?
 *
 * public — visibility modifier. Controls WHO can see the class/function.
 *          Default in Kotlin.
 *
 * open   — inheritance modifier. Controls whether a class can be SUBCLASSED
 *          or a function can be OVERRIDDEN. NOT default (classes are final).
 *
 * They are orthogonal — you need both for a publicly inheritable class:
 *   open class Foo          → public (default) + inheritable
 *   public open class Foo   → same, explicit public
 *   open internal class Foo → inheritable within the module only
 */

/**
 * Q34: Difference between List and Array types in Kotlin
 *
 * Array<T>:
 *   - Fixed size, mutable content (you can change elements, not size).
 *   - Compiled to Java arrays (primitive arrays with IntArray, etc.).
 *   - Use for performance-critical code or JNI/platform interop.
 *
 * List<T> (Kotlin's default):
 *   - An interface. Immutable by default (List = read-only, MutableList = read-write).
 *   - Backed by ArrayList in most implementations.
 *   - Preferred in Kotlin for collections — richer API, functional operations.
 *
 * Specialized primitive arrays: IntArray, LongArray, ByteArray, etc.
 * These avoid boxing overhead (no Integer wrappers).
 */

fun listVsArray() {
    val array = arrayOf(1, 2, 3)       // Array<Int> — boxes to Integer[]
    val intArray = intArrayOf(1, 2, 3) // IntArray — primitive int[]

    val list: List<Int> = listOf(1, 2, 3)           // immutable view
    val mutableList = mutableListOf(1, 2, 3)        // MutableList<Int>

    // Array has get/set, list has full collection API
    array[0] = 10
    // list[0] = 10  → ERROR: List is read-only
    mutableList[0] = 10  // OK
}

/**
 * Q35: What are Labels in Kotlin?
 *
 * Labels are identifiers followed by @ that mark loops or lambdas. They
 * let you control break, continue, and return in nested structures.
 */

fun labelDemo() {
    // Label on a loop:
    outer@ for (i in 1..5) {
        for (j in 1..5) {
            if (j == 3) continue@outer  // skips to next iteration of outer loop
            if (i == 4) break@outer     // breaks out of outer loop
            print("$i$j ")
        }
    }

    // Label on a lambda — return from the lambda, not the enclosing function:
    listOf(1, 2, 3, 4, 5).forEach lit@{
        if (it == 3) return@lit  // returns from this lambda iteration only
        println(it)
    }
    println("After forEach")  // this prints

    // Without label, return inside forEach returns from the enclosing function
}

/**
 * Q36: Explain inline classes in Kotlin (value classes)
 *
 * Inline/value classes wrap a single value without the overhead of an actual
 * wrapper object at runtime. The compiler inlines the wrapped value wherever
 * possible, avoiding heap allocation.
 *
 * Use case: Type safety without runtime cost (e.g., differentiating UserId
 * from PostId even though both are Ints).
 */

@JvmInline
value class UserId(val value: Long)

@JvmInline
value class PostId(val value: Long)

fun loadUser(id: UserId) { /* ... */ }
fun loadPost(id: PostId) { /* ... */ }

fun valueClassDemo() {
    val userId = UserId(42L)
    val postId = PostId(42L)

    loadUser(userId)
    // loadUser(postId)  → COMPILE ERROR: type mismatch
    // At runtime, userId is just a Long — no wrapper object allocated
}

/**
 * Q37: Sealed Classes in Kotlin
 *
 * Sealed classes restrict class hierarchies — all direct subclasses must be
 * defined in the SAME FILE (Kotlin 1.0) or SAME PACKAGE (Kotlin 1.5+).
 *
 * Benefits:
 *   - Exhaustive when expressions — compiler knows all subtypes.
 *   - Perfect for representing restricted state machines / result types.
 *   - Abstract by nature — cannot be instantiated directly.
 */

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}

fun handleResult(result: NetworkResult<User>) {
    when (result) {
        is NetworkResult.Success -> showUser(result.data)
        is NetworkResult.Error -> showError(result.message)
        is NetworkResult.Loading -> showLoading()
        // No else needed — compiler knows all subtypes
    }
}

/**
 * Q38: Sealed classes common use-cases in Android
 *
 * 1. UI State modeling:       sealed class UiState { Loading, Success, Error }
 * 2. Navigation events:       sealed class NavEvent { GoToDetail, GoBack, ... }
 * 3. Network results:         sealed class Result { Success, Failure }
 * 4. Intent/Action in MVI:    sealed class UserAction { Click, Swipe, Type }
 * 5. Recyclerview item types: sealed class ListItem { Header, Content, Footer }
 *
 * Sealed classes make `when` exhaustive, so adding a new state forces you
 * to handle it everywhere — the compiler catches missing branches.
 */

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable) : UiState<Nothing>()
    data object Empty : UiState<Nothing>()
}

// sealed interface (Kotlin 1.5+) allows multiple inheritance:
sealed interface DeepLinkAction {
    data class OpenProfile(val userId: String) : DeepLinkAction
    data class OpenPost(val postId: String) : DeepLinkAction
    data object OpenHome : DeepLinkAction
}

/**
 * Q39: Tell about the Collections in Kotlin
 *
 * Kotlin distinguishes between READ-ONLY and MUTABLE collections:
 *
 * Read-only (interfaces): List, Set, Map
 *   - Cannot add/remove/modify after creation.
 *   - Created via listOf(), setOf(), mapOf().
 *
 * Mutable (interfaces): MutableList, MutableSet, MutableMap
 *   - Support add, remove, put, clear, etc.
 *   - Created via mutableListOf(), mutableSetOf(), mutableMapOf().
 *
 * Kotlin uses Java's ArrayList, HashSet, HashMap under the hood but wraps
 * them in read-only interfaces for type safety.
 *
 * Key functional operations: map, filter, flatMap, groupBy, partition,
 * associate, fold, reduce, zip, windowed, chunked, etc.
 */

fun collectionsOverview() {
    // Immutable (read-only views)
    val list: List<Int> = listOf(1, 2, 3)
    val set: Set<String> = setOf("a", "b", "c")
    val map: Map<String, Int> = mapOf("one" to 1, "two" to 2)

    // Mutable
    val mutableList = mutableListOf(1, 2, 3)
    mutableList.add(4)

    // Useful operations
    val evens = list.filter { it % 2 == 0 }
    val doubled = list.map { it * 2 }
    val groups = list.groupBy { if (it % 2 == 0) "even" else "odd" }
    val chunks = (1..10).toList().chunked(3)   // [[1,2,3], [4,5,6], [7,8,9], [10]]
    val windows = (1..5).toList().windowed(3)  // [[1,2,3], [2,3,4], [3,4,5]]
}

/**
 * Q40: What does ?: do in Kotlin? (Elvis Operator)
 *
 * The Elvis operator ?: returns the left side if it's non-null, otherwise
 * returns the right side. It's a null-coalescing operator.
 *
 * Common patterns:
 *   - Default values: val name = input ?: "Unknown"
 *   - Early return: val user = getUser() ?: return
 *   - Throw on null: val user = getUser() ?: throw IllegalStateException("No user")
 */

fun elvisDemo(name: String?) {
    val displayName = name ?: "Guest"           // default value
    val length = name?.length ?: 0               // chain with safe call

    fun process(input: String?) {
        val value = input ?: return               // early return if null
        println(value.uppercase())
    }

    fun processStrict(input: String?) {
        val value = input ?: throw IllegalArgumentException("Input required")
        println(value)
    }
}

/**
 * Q41: Delegates in Kotlin
 *
 * Kotlin supports the delegation pattern at the language level:
 *
 * 1. Class delegation (by keyword) — delegate interface implementation:
 * 2. Property delegation (by keyword) — delegate get/set to another object.
 *
 * Built-in property delegates:
 *   - lazy { }              — initialize on first access
 *   - observable { }        — callback on change
 *   - vetoable { }          — reject changes based on condition
 *   - map delegation        — back properties by a Map
 */

// Class delegation — forward all Set methods to inner set
class CountingSet<T>(
    private val innerSet: MutableSet<T> = mutableSetOf()
) : MutableSet<T> by innerSet {
    var addCount = 0
        private set

    override fun add(element: T): Boolean {
        addCount++
        return innerSet.add(element)
    }
}

// Property delegates
class UserProfile {
    var name: String by Delegates.observable("") { _, old, new ->
        println("Name changed from '$old' to '$new'")
    }

    var age: Int by Delegates.vetoable(0) { _, _, newValue ->
        newValue >= 0   // reject negative ages
    }
}

// Map delegation — useful for JSON-like dynamic objects
class JsonBackedUser(map: Map<String, Any?>) {
    val name: String by map
    val age: Int by map
}
// val user = JsonBackedUser(mapOf("name" to "Alice", "age" to 30))

/**
 * Q42: How to do lazy initialization of variables in Kotlin?
 *
 * 1. by lazy { } — for val properties. Initializes on first access.
 *    Thread-safe by default (SYNCHRONIZED mode).
 *
 * 2. lateinit var — for vars that are initialized after construction
 *    but before use (DI, lifecycle callbacks).
 *
 * 3. Delegates.notNull<T>() — like lateinit but for primitives (since
 *    lateinit doesn't support primitive types).
 */

class LazyInitExample {
    // lazy — initialized on first access, thread-safe, cached
    val heavyObject: ExpensiveParser by lazy {
        println("Creating parser...")
        ExpensiveParser()
    }

    // lateinit — initialized later, before use
    lateinit var injectedService: ApiService

    // notNull — for primitive types that can't use lateinit
    var maxRetries: Int by Delegates.notNull()

    fun setup() {
        maxRetries = 3
        // Can now use maxRetries safely
    }
}


// ────────────────────────────────────────────────────────────────────────────
// SECTION 4: ANDROID — BASE
// ⛔ STUB — Full content removed (duplicates 01_android_fundamentals.kt)
// 📖 Primary reference: 01_android_fundamentals.kt
// ────────────────────────────────────────────────────────────────────────────

/**
 * This section duplicated content from 01_android_fundamentals.kt.
 * Read that file for interview tips (💡), gotchas (⚠️), and follow-ups (❓).
 *
 * Quick topics covered there:
 *   - Android app components overview
 *   - Application class lifecycle
 *   - Why Android app lags (frame budget)
 *
 * Use this file (17) for UNIQUE sections only — see header navigation table.
 */

// ────────────────────────────────────────────────────────────────────────────
// SECTION 5: ACTIVITY AND FRAGMENT
// ⛔ STUB — Full content removed (duplicates 01_android_fundamentals.kt)
// 📖 Primary reference: 01_android_fundamentals.kt
// ────────────────────────────────────────────────────────────────────────────

/**
 * This section duplicated content from 01_android_fundamentals.kt.
 * Read that file for interview tips (💡), gotchas (⚠️), and follow-ups (❓).
 *
 * Quick topics covered there:
 *   - Activity vs Fragment lifecycle interleaving
 *   - onSaveInstanceState timing
 *   - Fragment backstack memory leaks
 *
 * Use this file (17) for UNIQUE sections only — see header navigation table.
 */

// ═══════════════════════════════════════════════════════════════════════════════
// SECTION 6: VIEWS AND VIEWGROUPS
// ═══════════════════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────────────────────────
// Q: What is View in Android?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * View is the fundamental building block for UI in Android. Every UI element
 * (Button, TextView, ImageView, EditText) is a subclass of android.view.View.
 *
 * A View occupies a rectangular area on screen and is responsible for:
 *   - Drawing itself (onDraw(Canvas))
 *   - Handling events (touch, click, key)
 *   - Measuring its size (onMeasure(widthSpec, heightSpec))
 *   - Laying out its position (onLayout())
 *
 * The rendering pipeline for each frame:
 *   Measure → Layout → Draw
 *
 *   1. Measure pass: each View determines its own size. Parent calls
 *      child.measure() with MeasureSpec constraints.
 *   2. Layout pass: each View is positioned. Parent calls child.layout()
 *      with left, top, right, bottom coordinates.
 *   3. Draw pass: each View renders to Canvas.
 *      The hardware renderer draws the View tree to a Surface.
 *
 * Every View has: visibility, alpha, padding, elevation, background,
 * layout parameters, and a unique (optional) ID.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Difference between View.GONE and View.INVISIBLE?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * View.VISIBLE (0):
 *   - View is visible and participates in layout.
 *
 * View.INVISIBLE (4):
 *   - View is NOT visible but STILL takes up space in the layout.
 *   - It is measured and laid out normally; just not drawn.
 *   - Other views are positioned as if it were visible.
 *
 * View.GONE (8):
 *   - View is NOT visible and does NOT take up any space.
 *   - It is excluded from the measure/layout pass entirely.
 *   - Other views fill the space it would have occupied.
 *
 * Performance consideration: changing from GONE to VISIBLE triggers a full
 * measure + layout pass because the view hierarchy changes. INVISIBLE →
 * VISIBLE only requires a draw pass (cheaper). If you toggle visibility
 * frequently, INVISIBLE may be more performant.
 *
 * In ConstraintLayout specifically, GONE views still participate in
 * constraint resolution (other views' constraints to the GONE view are
 * honored as if it had zero size), which is a unique and useful behavior.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Can you create a custom view? How?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Yes. Three main approaches:
 *
 * 1. Extend an existing View (e.g., AppCompatTextView) — add behavior.
 * 2. Extend View directly — full custom drawing via Canvas.
 * 3. Extend ViewGroup — custom layout logic for children.
 *
 * Key methods to override:
 *   onMeasure(widthMeasureSpec, heightMeasureSpec) — determine size.
 *     Must call setMeasuredDimension() at the end.
 *   onDraw(canvas: Canvas) — render content using Canvas API.
 *   onTouchEvent(event: MotionEvent) — handle touch input.
 *   onSizeChanged(w, h, oldW, oldH) — react to size changes.
 *
 * Best practices:
 *   - Never allocate objects in onDraw() (causes GC jank).
 *   - Pre-allocate Paint, Path, Rect objects in init{} or constructor.
 *   - Support custom XML attributes via attrs.xml and obtainStyledAttributes.
 *   - Call invalidate() to trigger redraw, requestLayout() for size changes.
 *   - Implement onSaveInstanceState/onRestoreInstanceState for state.
 */

class CircleProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        color = Color.BLUE
        strokeCap = Paint.Cap.ROUND
    }

    private val rect = RectF()
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate() // trigger redraw
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = minOf(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val inset = paint.strokeWidth / 2
        rect.set(inset, inset, width - inset, height - inset)
        canvas.drawArc(rect, -90f, 360f * progress, false, paint)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: What are ViewGroups and how are they different from Views?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewGroup is a subclass of View that serves as a container for other Views
 * (children). It defines the layout structure.
 *
 * View:
 *   - A leaf node — draws itself, no children
 *   - Examples: TextView, Button, ImageView, EditText
 *
 * ViewGroup:
 *   - An internal node — contains and positions child Views
 *   - Responsible for measuring and laying out children
 *   - Examples: LinearLayout, RelativeLayout, ConstraintLayout,
 *     FrameLayout, RecyclerView, ScrollView
 *
 * Every ViewGroup must implement:
 *   onMeasure() — measure itself and all children
 *   onLayout() — position each child within its bounds
 *
 * The View tree (hierarchy) is a tree structure with ViewGroups as internal
 * nodes and Views as leaves. The root is DecorView, which contains the
 * system chrome (status bar, nav bar) and your content view.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is a Canvas?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Canvas is the drawing surface provided to a View in its onDraw() method.
 * It provides methods to draw shapes, text, bitmaps, and paths onto a
 * backing Bitmap or hardware-accelerated surface.
 *
 * Common Canvas operations:
 *   drawRect(), drawCircle(), drawArc() — geometric shapes
 *   drawText() — text rendering
 *   drawBitmap() — image rendering
 *   drawPath() — complex shapes from Path objects
 *   translate(), rotate(), scale(), skew() — transformations
 *   save() / restore() — save/restore transformation state
 *   clipRect(), clipPath() — restrict drawing area
 *
 * Canvas works with Paint objects that define HOW to draw:
 *   - Color, alpha, stroke width, style (fill vs stroke)
 *   - Text size, font, alignment
 *   - Anti-aliasing, shadows, color filters, shaders
 *
 * Under hardware acceleration (default since API 14), Canvas operations
 * are recorded into a DisplayList and rendered by the GPU. Some Canvas
 * operations are not hardware-accelerated (e.g., drawBitmapMesh).
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is a SurfaceView?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * SurfaceView provides a dedicated drawing Surface that is rendered on a
 * SEPARATE window layer from the normal View hierarchy. This allows drawing
 * on a background thread without blocking the UI thread.
 *
 * Normal View vs SurfaceView:
 *   - View: draws on the main thread in the app's window
 *   - SurfaceView: draws on ANY thread into its own Surface/window
 *
 * How it works:
 *   - SurfaceView creates a "hole" in the View hierarchy
 *   - A separate Surface is composited BELOW the app window
 *   - Drawing happens via SurfaceHolder.lockCanvas() → draw → unlockCanvasAndPost()
 *   - Can run a rendering loop on a background thread
 *
 * Use cases:
 *   - Camera preview
 *   - Video playback (MediaPlayer, ExoPlayer)
 *   - Game rendering loops
 *   - Heavy real-time graphics (charts, waveforms)
 *
 * Downsides:
 *   - Cannot be transformed (animated, scaled, rotated) like regular Views
 *   - Z-ordering limitations (always behind or in front of window)
 *
 * Alternative: TextureView — draws to a SurfaceTexture, so it CAN be
 * transformed and animated like a normal View, but is slightly less
 * performant than SurfaceView. Another option: GLSurfaceView for OpenGL.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Relative Layout vs Linear Layout
// ─────────────────────────────────────────────────────────────────────────────

/**
 * LinearLayout:
 *   - Arranges children in a single direction: horizontal or vertical
 *   - Uses layout_weight for proportional distribution of space
 *   - Simple, predictable positioning
 *   - Performance: single measure pass (unless weights are used — weights
 *     trigger a SECOND measure pass, doubling cost)
 *   - Best for: simple row/column arrangements
 *
 * RelativeLayout:
 *   - Positions children relative to each other or the parent
 *   - Rules: toLeftOf, below, alignParentEnd, centerInParent, etc.
 *   - Can achieve complex layouts without nesting
 *   - Performance: ALWAYS does TWO measure passes (to resolve relative
 *     dependencies), regardless of complexity
 *   - Best for: flat layouts where children have spatial relationships
 *
 * Performance comparison:
 *   - LinearLayout (no weights) < RelativeLayout (always 2 passes)
 *   - LinearLayout (with weights) ≈ RelativeLayout (both do 2 passes)
 *   - Deep nesting of either is bad — each level multiplies measure passes
 *
 * Modern recommendation: Use ConstraintLayout — it achieves complex
 * layouts with a FLAT hierarchy (no nesting) and has an optimized
 * constraint solver that typically does a single measure pass.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Tell about Constraint Layout optimization
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ConstraintLayout is optimized for flat hierarchies. Instead of nesting
 * multiple LinearLayouts/RelativeLayouts, you express relationships as
 * constraints, and the Cassowary-based solver resolves positions in a
 * single (or minimal) measure/layout pass.
 *
 * Key optimizations:
 *
 * 1. Flat hierarchy — eliminates nested measure/layout passes.
 *    Nested LinearLayout (3 levels) = 2^3 = 8 measure passes.
 *    ConstraintLayout (flat) = 1-2 measure passes.
 *
 * 2. Chains — replicate LinearLayout weight behavior without nesting.
 *    Spread, spread_inside, packed, and weighted chains.
 *
 * 3. Guidelines & Barriers — virtual views with zero rendering cost.
 *    Barriers adapt to the largest child dynamically.
 *
 * 4. ConstraintLayout 2.0 features:
 *    - Flow: virtual layout for wrapping rows/columns
 *    - MotionLayout: declarative animations with constraints
 *    - Layer: group transforms without nesting
 *
 * 5. match_constraint (0dp) — lets the solver determine size based on
 *    constraints rather than content, reducing measure iterations.
 *
 * When to prefer ConstraintLayout:
 *   - Complex layouts that would require 2+ levels of nesting
 *   - Responsive layouts that need percentage-based sizing
 *   - Animations between two constraint states (MotionLayout)
 *
 * When simpler layouts suffice:
 *   - Simple vertical/horizontal list → LinearLayout
 *   - Stacking overlapping views → FrameLayout
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Do you know what is the view tree? How can you optimize its depth?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The view tree is the hierarchical structure of all Views and ViewGroups
 * that make up the UI. The root is the DecorView (system-provided), which
 * contains the content view set by setContentView().
 *
 * Why depth matters:
 *   - Measure, layout, and draw passes traverse the tree recursively.
 *   - Each level of nesting multiplies the cost of these passes.
 *   - Deep trees cause jank, especially with RelativeLayout or LinearLayout
 *     with weights (which do 2 measure passes per level).
 *
 * Optimization strategies:
 *
 * 1. Use ConstraintLayout to flatten nested layouts into a single level.
 *
 * 2. Use <merge> tag — eliminates a redundant FrameLayout when including
 *    a layout via <include>.
 *
 * 3. Use <ViewStub> — lazy inflation. The view is not inflated until
 *    explicitly requested, saving measure/layout cost for hidden views.
 *
 * 4. Remove unnecessary wrappers — audit with Layout Inspector.
 *
 * 5. Use RecyclerView instead of ScrollView with many children.
 *
 * 6. Consider Jetpack Compose — Compose's layout system does NOT have
 *    multi-pass measurement. Each composable is measured exactly ONCE,
 *    regardless of nesting depth.
 *
 * Tools:
 *   - Layout Inspector (Android Studio) — visualize tree and measure times
 *   - lint (Android Lint) — warns about deep hierarchies (>10 levels)
 *   - Systrace / Perfetto — profile measure/layout/draw per frame
 */


// ═══════════════════════════════════════════════════════════════════════════════
// SECTION 7: RECYCLERVIEW
// ═══════════════════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────────────────────────
// Q: What is the difference between ListView and RecyclerView?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ListView (legacy):
 *   - ViewHolder pattern is optional (manual optimization)
 *   - Only supports vertical scrolling
 *   - Built-in click listeners (onItemClickListener)
 *   - Dividers are built-in
 *   - No built-in animation support for add/remove
 *   - addHeaderView/addFooterView for headers/footers
 *   - Adapter: BaseAdapter, ArrayAdapter, CursorAdapter
 *
 * RecyclerView (modern replacement):
 *   - ViewHolder pattern is ENFORCED (mandatory)
 *   - LayoutManager supports vertical, horizontal, grid, staggered
 *   - No built-in click listener (set on ViewHolder)
 *   - ItemDecoration for custom dividers, spacing, decorations
 *   - Built-in item animations (DefaultItemAnimator)
 *   - Supports DiffUtil for efficient list updates
 *   - ItemTouchHelper for drag-and-drop, swipe-to-dismiss
 *   - Pluggable architecture: Adapter, LayoutManager, ItemDecoration,
 *     ItemAnimator are all independently replaceable
 *   - ViewPool for sharing ViewHolders across nested RecyclerViews
 *
 * RecyclerView is always preferred. ListView is effectively deprecated.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: How does RecyclerView work?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * RecyclerView works by recycling ViewHolder objects as the user scrolls.
 *
 * Core loop:
 *   1. LayoutManager requests the next View from RecyclerView.
 *   2. RecyclerView checks the Recycler (cache) for a reusable ViewHolder:
 *      a. Scrap (attached views being re-laid out) — used as-is
 *      b. Cache (recently detached, by position) — used as-is, no rebind
 *      c. ViewCacheExtension (custom cache, optional)
 *      d. RecycledViewPool (by viewType) — must rebind (onBindViewHolder)
 *   3. If no cached ViewHolder found → Adapter.onCreateViewHolder() inflates a new one.
 *   4. Adapter.onBindViewHolder(holder, position) binds data to the ViewHolder.
 *   5. LayoutManager positions the View on screen.
 *   6. When a View scrolls off screen, it goes into the cache/pool.
 *
 * Cache hierarchy (in lookup order):
 *   Scrap → mCachedViews (default size 2) → ViewCacheExtension → RecycledViewPool
 *
 * Key insight: onCreateViewHolder() is expensive (inflation). onBindViewHolder()
 * is cheap (data binding). RecyclerView minimizes creates by recycling.
 * The pool holds up to 5 ViewHolders per viewType by default.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: RecyclerView Optimization — Scrolling Performance Improvement
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Key optimizations for smooth 60fps scrolling:
 *
 * 1. setHasFixedSize(true) — if the RecyclerView size doesn't depend on
 *    adapter content, skip re-measuring on every data change.
 *
 * 2. Use DiffUtil / ListAdapter — calculate minimal changes instead of
 *    notifyDataSetChanged() which rebinds everything.
 *
 * 3. Avoid inflation in onBindViewHolder() — all view creation should be
 *    in onCreateViewHolder(). onBind should only set data.
 *
 * 4. Avoid allocations in onBindViewHolder() — no new objects, no
 *    string concatenation, pre-format data in the ViewModel.
 *
 * 5. Use RecycledViewPool — share ViewHolders across nested RecyclerViews.
 *    pool.setMaxRecycledViews(viewType, count)
 *
 * 6. setItemViewCacheSize(n) — increase off-screen cache for faster
 *    reverse scroll (default is 2).
 *
 * 7. Efficient image loading — use Coil/Glide with proper sizing,
 *    placeholder, and cache policies. Cancel loads in onViewRecycled().
 *
 * 8. setRecycledViewPool() for nested horizontal RecyclerViews — reuse
 *    ViewHolders across rows.
 *
 * 9. Avoid nested RecyclerView in RecyclerView when possible; use
 *    ConcatAdapter or a flat list with multiple view types.
 *
 * 10. Use View.setHasTransientState(true) sparingly — it prevents recycling.
 */

class OptimizedAdapter(
    private val onClick: (Item) -> Unit
) : ListAdapter<Item, OptimizedAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(
        private val binding: ItemRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onClick(getItem(pos))
                }
            }
        }

        fun bind(item: Item) {
            binding.titleText.text = item.title
            binding.subtitleText.text = item.subtitle
        }
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(old: Item, new: Item) = old.id == new.id
    override fun areContentsTheSame(old: Item, new: Item) = old == new
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: Optimizing Nested RecyclerView
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Nested RecyclerViews (common pattern: vertical outer + horizontal inner)
 * have specific performance challenges.
 *
 * Optimizations:
 *
 * 1. Share RecycledViewPool — inner RecyclerViews should share a single pool
 *    so ViewHolders recycled from one row can be reused in another.
 *
 * 2. Set initial prefetch item count — tells the LayoutManager how many
 *    items the inner RecyclerView will need during the next frame.
 *    (innerLayoutManager as LinearLayoutManager).initialPrefetchItemCount = 4
 *
 * 3. Cache inner RecyclerView layout state — when the outer row is recycled,
 *    save the inner RecyclerView's scroll position and LayoutManager state
 *    in onViewRecycled(), restore in onBindViewHolder().
 *
 * 4. setRecycledViewPool(sharedPool) on each inner RecyclerView.
 *
 * 5. setItemViewCacheSize(0) on inner RecyclerViews — push items directly
 *    to the shared pool instead of per-RV cache.
 *
 * 6. Consider flattening — use ConcatAdapter with headers to avoid nesting.
 */

class OuterAdapter : RecyclerView.Adapter<OuterAdapter.OuterViewHolder>() {

    private val sharedPool = RecyclerView.RecycledViewPool()

    class OuterViewHolder(
        itemView: View,
        private val innerRv: RecyclerView
    ) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OuterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_outer, parent, false)
        val innerRv = view.findViewById<RecyclerView>(R.id.innerRecyclerView)
        innerRv.setRecycledViewPool(sharedPool)
        innerRv.setItemViewCacheSize(0)
        (innerRv.layoutManager as LinearLayoutManager).initialPrefetchItemCount = 4
        return OuterViewHolder(view, innerRv)
    }

    override fun onBindViewHolder(holder: OuterViewHolder, position: Int) {
        // bind inner adapter data
    }

    override fun getItemCount(): Int = 0 // placeholder
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: What are the components of a RecyclerView?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * RecyclerView has a pluggable architecture with these components:
 *
 * 1. RecyclerView.Adapter — bridges data to ViewHolders.
 *    Responsible for creating ViewHolders and binding data to them.
 *
 * 2. RecyclerView.ViewHolder — holds references to views in a single item.
 *    Avoids repeated findViewById calls.
 *
 * 3. RecyclerView.LayoutManager — positions items on screen.
 *    Built-in: LinearLayoutManager, GridLayoutManager, StaggeredGridLayoutManager.
 *    Custom LayoutManagers are possible for non-standard layouts.
 *
 * 4. RecyclerView.ItemDecoration — draws decorations (dividers, spacing,
 *    badges) around or over items without modifying item views.
 *
 * 5. RecyclerView.ItemAnimator — animates item changes (add, remove, move,
 *    change). Default: DefaultItemAnimator with fade/translate effects.
 *
 * 6. RecyclerView.Recycler — manages the caching of ViewHolders (scrap,
 *    cache, pool). Internal to RecyclerView but accessible for custom
 *    LayoutManagers.
 *
 * 7. SnapHelper — snaps items to specific positions after scroll.
 *    LinearSnapHelper (center snap), PagerSnapHelper (one-page snap).
 *
 * 8. ItemTouchHelper — handles drag-and-drop and swipe-to-dismiss gestures.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Explain the role of RecyclerView.Adapter and RecyclerView.ViewHolder
// ─────────────────────────────────────────────────────────────────────────────

/**
 * RecyclerView.Adapter<VH>:
 *   - Creates ViewHolder instances (onCreateViewHolder) — inflates the layout
 *   - Binds data to ViewHolders (onBindViewHolder) — sets text, images, etc.
 *   - Reports dataset size (getItemCount)
 *   - Handles multiple view types (getItemViewType)
 *   - Notifies RecyclerView of data changes (notifyItemInserted, etc.)
 *   - Modern: extend ListAdapter<T, VH> for built-in DiffUtil support
 *
 * RecyclerView.ViewHolder:
 *   - Wraps a single item view and caches references to its child views
 *   - Created once, then reused (recycled) for different data positions
 *   - Holds the itemView reference and knows its adapter position
 *   - Key properties: bindingAdapterPosition, absoluteAdapterPosition
 *   - Should set click listeners in init{} (not in onBind) for efficiency
 *
 * The separation of Adapter and ViewHolder enables the recycling pattern:
 * the Adapter knows HOW to create and bind, the ViewHolder knows WHAT views
 * exist. This avoids expensive inflation and findViewById on every scroll.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is a LayoutManager in RecyclerView?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * LayoutManager is responsible for measuring and positioning item views
 * within the RecyclerView and determining the policy for when to recycle
 * items that are no longer visible.
 *
 * Built-in LayoutManagers:
 *
 * 1. LinearLayoutManager
 *    - Vertical or horizontal scrolling list
 *    - Options: reverseLayout, stackFromEnd
 *
 * 2. GridLayoutManager
 *    - Grid with configurable span count
 *    - SpanSizeLookup for variable span sizes per item
 *
 * 3. StaggeredGridLayoutManager
 *    - Staggered grid where items can have different heights
 *    - Pinterest-style layout
 *
 * Key responsibilities:
 *   - Decide which items are visible (layout)
 *   - Position items (left, top, right, bottom)
 *   - Handle scroll events
 *   - Report scroll state and position
 *   - Support prefetching for smoother scrolling
 *   - Handle accessibility traversal order
 *
 * Custom LayoutManager: override onLayoutChildren() and scrollVerticallyBy()
 * for non-standard layouts (circular, carousel, etc.).
 */

fun setupRecyclerView(recyclerView: RecyclerView) {
    // Linear
    recyclerView.layoutManager = LinearLayoutManager(
        recyclerView.context,
        LinearLayoutManager.VERTICAL,
        false
    )

    // Grid with varying spans
    val gridLayoutManager = GridLayoutManager(recyclerView.context, 3)
    gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return if (position == 0) 3 else 1 // header spans full width
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: How do you handle multiple view types in a single RecyclerView?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Override getItemViewType(position) in the Adapter to return different
 * integer constants for different view types. Then create the appropriate
 * ViewHolder in onCreateViewHolder() based on the viewType parameter.
 *
 * RecyclerView uses viewType to ensure a ViewHolder of type A is never
 * reused for type B — they have separate pools.
 */

sealed class FeedItem {
    data class Header(val title: String) : FeedItem()
    data class Post(val id: Int, val content: String) : FeedItem()
    data class Ad(val imageUrl: String) : FeedItem()
}

class FeedAdapter : ListAdapter<FeedItem, RecyclerView.ViewHolder>(FeedDiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_POST = 1
        private const val TYPE_AD = 2
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is FeedItem.Header -> TYPE_HEADER
        is FeedItem.Post -> TYPE_POST
        is FeedItem.Ad -> TYPE_AD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(inflater.inflate(R.layout.item_header, parent, false))
            TYPE_POST -> PostViewHolder(inflater.inflate(R.layout.item_post, parent, false))
            TYPE_AD -> AdViewHolder(inflater.inflate(R.layout.item_ad, parent, false))
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is FeedItem.Header -> (holder as HeaderViewHolder).bind(item)
            is FeedItem.Post -> (holder as PostViewHolder).bind(item)
            is FeedItem.Ad -> (holder as AdViewHolder).bind(item)
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is DiffUtil and how does it improve RecyclerView performance?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * DiffUtil computes the minimal set of update operations (insert, remove,
 * move, change) needed to transform one list into another using Eugene
 * Myers' difference algorithm.
 *
 * Without DiffUtil: notifyDataSetChanged() rebinds EVERY visible item,
 * loses item animations, and is O(n) regardless of actual changes.
 *
 * With DiffUtil: only changed items are rebound; animations play for
 * inserts, removals, and moves. This is crucial for large lists.
 *
 * Two ways to use:
 *
 * 1. DiffUtil.calculateDiff(callback) — manual, runs on calling thread.
 *    Should be done on background thread for large lists.
 *
 * 2. ListAdapter / AsyncListDiffer — wraps DiffUtil with automatic
 *    background thread diffing. This is the recommended approach.
 *
 * DiffUtil.ItemCallback<T>:
 *   areItemsTheSame(old, new) → same identity? (compare IDs)
 *   areContentsTheSame(old, new) → same content? (compare all fields)
 *   getChangePayload(old, new) → optional partial bind info
 *
 * Performance: O(N + D^2) where N = list size, D = number of edits.
 * For very large lists (10k+), even this can be slow — paginate instead.
 */

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: User, newItem: User) =
        oldItem == newItem

    override fun getChangePayload(oldItem: User, newItem: User): Any? {
        // Return partial change info for efficient partial rebind
        return if (oldItem.name != newItem.name) "name_changed" else null
    }
}

class UserAdapter : ListAdapter<User, UserAdapter.UserVH>(UserDiffCallback()) {

    override fun onBindViewHolder(holder: UserVH, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains("name_changed")) {
            holder.updateName(getItem(position).name) // partial rebind
        } else {
            super.onBindViewHolder(holder, position, payloads) // full rebind
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is the purpose of RecyclerView.setHasFixedSize(true)?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * When setHasFixedSize(true) is called, you are telling RecyclerView that
 * changes in adapter content will NOT change the size of the RecyclerView
 * itself (only the number/content of items changes, not the RV dimensions).
 *
 * Why it helps performance:
 *   - Without it, every adapter notify call triggers requestLayout() on
 *     the RecyclerView, which in turn causes a full measure/layout pass
 *     of the RecyclerView AND its parent hierarchy.
 *   - With it, RecyclerView skips the requestLayout() call and only
 *     re-layouts its children internally.
 *
 * When to use: when the RecyclerView has match_parent or a fixed dp size.
 * When NOT to use: when the RecyclerView uses wrap_content (its size
 * depends on the number of items).
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: How do you update a specific item in RecyclerView?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Several approaches, from most to least efficient:
 *
 * 1. ListAdapter.submitList(newList) with DiffUtil (recommended):
 *    Automatically computes and dispatches minimal updates.
 *
 * 2. notifyItemChanged(position) — rebinds a single item.
 *    notifyItemChanged(position, payload) — partial rebind with payload.
 *
 * 3. notifyItemInserted(position) / notifyItemRemoved(position) /
 *    notifyItemMoved(fromPos, toPos) — specific structural changes.
 *
 * 4. notifyItemRangeChanged(positionStart, itemCount) — rebind a range.
 *
 * 5. notifyDataSetChanged() — WORST. Rebinds everything, no animations.
 *    Only use as last resort.
 *
 * Payload-based partial update for efficiency:
 */

fun partialUpdateExample(adapter: RecyclerView.Adapter<*>) {
    // Full rebind of position 5
    adapter.notifyItemChanged(5)

    // Partial rebind — only the "likes" count changed
    adapter.notifyItemChanged(5, "likes_updated")
}

// In ViewHolder:
// override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
//     if (payloads.contains("likes_updated")) {
//         holder.updateLikeCount(getItem(position).likes)  // cheap partial update
//     } else {
//         onBindViewHolder(holder, position)  // full bind fallback
//     }
// }


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is SnapHelper?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * SnapHelper helps RecyclerView snap items to specific positions after a
 * scroll or fling gesture completes. Without it, items can stop at any
 * arbitrary scroll position.
 *
 * Built-in SnapHelpers:
 *
 * 1. LinearSnapHelper — snaps the closest item to the CENTER of the
 *    RecyclerView. Used for carousel-like centered item selection.
 *
 * 2. PagerSnapHelper — snaps one page at a time (like ViewPager).
 *    Only one item is scrolled per fling, always fully visible.
 *
 * Usage:
 *   val snapHelper = LinearSnapHelper()
 *   snapHelper.attachToRecyclerView(recyclerView)
 *
 * Custom SnapHelper: override these methods:
 *   calculateDistanceToFinalSnap(layoutManager, targetView) → [dx, dy]
 *   findSnapView(layoutManager) → which View to snap to
 *   findTargetSnapPosition(layoutManager, velocityX, velocityY) → target pos
 *
 * Use cases: horizontal carousels, date pickers, time selectors,
 * and any paginated horizontal list.
 */

fun setupSnapHelper(recyclerView: RecyclerView) {
    val snapHelper = PagerSnapHelper()
    snapHelper.attachToRecyclerView(recyclerView)

    // Get the currently snapped position
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val snappedView = snapHelper.findSnapView(recyclerView.layoutManager!!)
                val position = recyclerView.layoutManager!!.getPosition(snappedView!!)
                // position is the currently centered item
            }
        }
    })
}


// ═══════════════════════════════════════════════════════════════════════════════
// SECTION 8: INTENTS, SERVICES, AND IPC
// ═══════════════════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────────────────────────
// Q: What is Intent? Implicit vs Explicit Intent?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * An Intent is a messaging object used to request an action from another
 * app component. It is the primary mechanism for inter-component
 * communication in Android.
 *
 * Explicit Intent:
 *   - Specifies the exact component (class name) to start.
 *   - Used for communication within your own app.
 *   - Example: starting a specific Activity or Service.
 *
 * Implicit Intent:
 *   - Declares a general action to perform (no specific component).
 *   - The system finds matching components via Intent Filters declared
 *     in their manifests.
 *   - Used for cross-app actions: open URL, share, send email, take photo.
 *   - Always check resolveActivity() before starting (no match = crash).
 *
 * Intent contains:
 *   - Action: the general action (ACTION_VIEW, ACTION_SEND, ACTION_DIAL)
 *   - Data: URI of data to act on (content://, https://, tel:)
 *   - Type: MIME type (text/plain, image/*)
 *   - Category: additional info (CATEGORY_BROWSABLE, CATEGORY_LAUNCHER)
 *   - Extras: key-value data (Bundle)
 *   - Flags: modify behavior (FLAG_ACTIVITY_NEW_TASK, etc.)
 *   - Component: explicit target (only for explicit intents)
 */

fun intentExamples(context: Context) {
    // Explicit: navigate within app
    val explicit = Intent(context, DetailActivity::class.java).apply {
        putExtra("ITEM_ID", "123")
    }
    context.startActivity(explicit)

    // Implicit: open a URL
    val implicit = Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
    if (implicit.resolveActivity(context.packageManager) != null) {
        context.startActivity(implicit)
    }

    // Implicit: share text
    val share = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Check this out!")
    }
    context.startActivity(Intent.createChooser(share, "Share via"))
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is a BroadcastReceiver? What are the different types of Broadcasts?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A BroadcastReceiver is a component that responds to system-wide broadcast
 * announcements. Broadcasts are sent via sendBroadcast(intent).
 *
 * Registration types:
 *   Static (manifest-declared): <receiver> in AndroidManifest.xml.
 *     - Receives broadcasts even if the app is not running.
 *     - Restricted since API 26: most implicit broadcasts are NOT delivered
 *       to manifest-registered receivers (to reduce battery drain).
 *   Dynamic (context-registered): registerReceiver() at runtime.
 *     - Only active while the component is alive.
 *     - Must unregister in corresponding lifecycle callback.
 *
 * Broadcast types:
 *
 * 1. Normal broadcasts (sendBroadcast):
 *    - Delivered to all receivers asynchronously, in undefined order.
 *    - Receivers cannot modify or abort the broadcast.
 *
 * 2. Ordered broadcasts (sendOrderedBroadcast):
 *    - Delivered to receivers one at a time, in priority order.
 *    - Each receiver can pass results to the next or abort the broadcast.
 *
 * 3. Local broadcasts (LocalBroadcastManager — DEPRECATED):
 *    - Only within the same app process. Efficient but deprecated.
 *    - Replacement: use LiveData, SharedFlow, or EventBus patterns.
 *
 * 4. Sticky broadcasts (DEPRECATED since API 21):
 *    - Stay around after being sent. New receivers get the last value.
 *
 * API 26+ restriction: Use explicit broadcasts or register dynamically.
 */

class NetworkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val isConnected = isNetworkAvailable(context)
            // Handle connectivity change
        }
    }
}

fun registerDynamicReceiver(context: Context) {
    val receiver = NetworkReceiver()
    val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    // API 33+ requires RECEIVER_NOT_EXPORTED or RECEIVER_EXPORTED flag
    context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is a PendingIntent?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A PendingIntent is a token that wraps an Intent and grants another
 * application (or the system) the permission to execute it AS IF it
 * were your app — using your app's identity and permissions.
 *
 * It's like giving someone a signed, sealed letter to deliver on your behalf.
 *
 * Common uses:
 *   - Notifications: the system fires the Intent when the user taps.
 *   - AlarmManager: the system fires the Intent at a scheduled time.
 *   - Widgets: the launcher fires the Intent on widget interaction.
 *   - Geofencing: location services fire on geofence events.
 *
 * Factory methods:
 *   PendingIntent.getActivity()    → starts an Activity
 *   PendingIntent.getService()     → starts a Service
 *   PendingIntent.getBroadcast()   → sends a Broadcast
 *
 * Flags:
 *   FLAG_IMMUTABLE (API 23+)     → cannot be modified by receiving app
 *   FLAG_MUTABLE                  → can be modified (needed for inline reply)
 *   FLAG_UPDATE_CURRENT           → update extras if PendingIntent already exists
 *   FLAG_ONE_SHOT                 → can only be used once
 *   FLAG_CANCEL_CURRENT           → cancel existing and create new
 *
 * API 31+ REQUIRES explicit FLAG_IMMUTABLE or FLAG_MUTABLE.
 *
 * Security: always use FLAG_IMMUTABLE unless you specifically need mutability.
 * A mutable PendingIntent could be modified by a malicious app.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Explain Android Service Lifecycle
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A Service runs in the background without a UI. Two lifecycle paths:
 *
 * Started Service (startService / startForegroundService):
 *   onCreate() → onStartCommand() → [runs] → onDestroy()
 *
 *   - Runs indefinitely until stopSelf() or stopService() is called.
 *   - onStartCommand() return values:
 *     START_NOT_STICKY    → don't recreate if killed (one-shot tasks)
 *     START_STICKY        → recreate with null intent (music player)
 *     START_REDELIVER_INTENT → recreate with last intent (downloads)
 *
 * Bound Service (bindService):
 *   onCreate() → onBind() → [clients bound] → onUnbind() → onDestroy()
 *
 *   - Lives only while clients are bound.
 *   - Returns an IBinder for client-server communication.
 *   - onUnbind() called when the last client unbinds.
 *
 * A service can be both started AND bound simultaneously.
 * It's destroyed only when it's both stopped AND unbound.
 *
 * Threading: Services run on the MAIN THREAD by default!
 * You must create background threads or use coroutines for long work.
 *
 * API 26+: Background services are restricted. Use Foreground Service
 * (with notification) or WorkManager for background work.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Service vs IntentService
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Service:
 *   - Runs on the MAIN THREAD
 *   - You must manage threading yourself
 *   - Runs indefinitely until explicitly stopped
 *   - Can handle multiple concurrent requests
 *   - Use for long-running operations that need to stay alive
 *
 * IntentService (DEPRECATED since API 30):
 *   - Creates a WORKER THREAD automatically
 *   - Processes intents one at a time in a queue (sequential)
 *   - Auto-stops when the work queue is empty
 *   - Cannot be interrupted easily (processes serially)
 *   - Simple to use: just override onHandleIntent()
 *
 * Why deprecated: IntentService doesn't work well with background execution
 * limits (API 26+). It cannot run as a foreground service easily.
 *
 * Modern replacements:
 *   - WorkManager — for deferrable, guaranteed background work
 *   - Foreground Service with coroutines — for user-initiated long tasks
 *   - CoroutineWorker — WorkManager + coroutines
 *
 * If you still need a Service with a worker thread, use
 * LifecycleService + coroutines instead of IntentService.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is a Foreground Service?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A Foreground Service performs work that is noticeable to the user and
 * displays a persistent notification. It has much higher priority than a
 * background service — the system is very unlikely to kill it.
 *
 * Use cases: music playback, navigation, file downloads, fitness tracking.
 *
 * Requirements:
 *   - Must call startForeground(id, notification) within 5 seconds of starting
 *   - Must display a visible notification
 *   - Declare android:foregroundServiceType in manifest (API 29+)
 *   - Request POST_NOTIFICATIONS permission (API 33+)
 *   - API 34+: must declare specific foreground service types and request
 *     corresponding permissions (camera, microphone, location, etc.)
 *
 * Lifecycle:
 *   startForegroundService() → onCreate() → onStartCommand()
 *   → startForeground(id, notification) → [runs] → stopForeground() → onDestroy()
 */

class MusicService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        startMusicPlayback()
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            "music", "Music Playback", NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        return NotificationCompat.Builder(this, "music")
            .setContentTitle("Now Playing")
            .setSmallIcon(R.drawable.ic_music)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is a JobScheduler?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * JobScheduler (API 21+) is a system service for scheduling background
 * jobs with conditions. The system batches jobs to optimize battery.
 *
 * Conditions you can set:
 *   - Network type (unmetered, connected, not roaming)
 *   - Charging state (requires charging / not)
 *   - Device idle
 *   - Storage not low
 *   - Periodic execution (minimum 15 minutes)
 *   - Persistence across reboots (setPersisted)
 *   - Deadline and latency
 *   - Back-off policy
 *
 * How it works:
 *   1. Create a JobInfo with conditions.
 *   2. Schedule with JobScheduler.schedule(jobInfo).
 *   3. System calls onStartJob() when conditions are met.
 *   4. Return true if work is async (must call jobFinished() when done).
 *   5. onStopJob() is called if conditions are no longer met.
 *
 * Modern recommendation: Use WorkManager instead. It uses JobScheduler
 * on API 23+, AlarmManager + BroadcastReceiver on older APIs, and adds
 * chaining, input/output, LiveData observation, and coroutine support.
 */

class SyncJobService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        // Return true = work is async, will call jobFinished() later
        CoroutineScope(Dispatchers.IO).launch {
            performSync()
            jobFinished(params, false) // false = don't reschedule
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true // true = reschedule the job
    }
}

fun scheduleSync(context: Context) {
    val jobScheduler = context.getSystemService(JobScheduler::class.java)
    val jobInfo = JobInfo.Builder(1, ComponentName(context, SyncJobService::class.java))
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
        .setRequiresCharging(true)
        .setPeriodic(15 * 60 * 1000L) // minimum 15 minutes
        .setPersisted(true)
        .build()
    jobScheduler.schedule(jobInfo)
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: How Does WorkManager Guarantee Task Execution?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * WorkManager guarantees eventual execution of work even if the app exits
 * or the device restarts. It is the recommended solution for deferrable,
 * reliable background work.
 *
 * How it guarantees execution:
 *
 * 1. Persistence — work requests are stored in an internal Room database.
 *    Even after a reboot, WorkManager re-schedules pending work.
 *
 * 2. Platform delegation:
 *    - API 23+: uses JobScheduler (batches with system constraints)
 *    - API 14-22: uses AlarmManager + BroadcastReceiver as fallback
 *    - Respects Doze mode, App Standby, and battery optimization
 *
 * 3. Constraints: work only runs when constraints are met.
 *    NetworkType, BatteryNotLow, StorageNotLow, DeviceIdle, RequiresCharging
 *
 * 4. Retry policies: automatic retry with backoff on failure.
 *    Result.retry() + BackoffPolicy (LINEAR or EXPONENTIAL)
 *
 * 5. Chaining: chain dependent work with then(). If one fails, downstream
 *    work is cancelled.
 *
 * 6. Unique work: ensureUnique avoids duplicate work.
 *    enqueueUniqueWork(name, ExistingWorkPolicy.KEEP/REPLACE/APPEND)
 *
 * Guarantee caveat: WorkManager does NOT guarantee exact timing. The system
 * may delay execution based on Doze mode and battery optimization.
 * For exact-time tasks, use AlarmManager with setExactAndAllowWhileIdle().
 */

class UploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val fileUri = inputData.getString("file_uri") ?: return Result.failure()
        return try {
            uploadFile(fileUri)
            Result.success(workDataOf("upload_url" to "https://..."))
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

fun enqueueUpload(context: Context, fileUri: String) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    val request = OneTimeWorkRequestBuilder<UploadWorker>()
        .setInputData(workDataOf("file_uri" to fileUri))
        .setConstraints(constraints)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        .build()

    WorkManager.getInstance(context)
        .enqueueUniqueWork("upload_$fileUri", ExistingWorkPolicy.KEEP, request)
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: How can two distinct Android apps interact?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Android apps run in separate processes with isolated memory. Communication
 * between apps (IPC — Inter-Process Communication) can happen via:
 *
 * 1. Intents:
 *    - Implicit intents to invoke another app's Activity/Service.
 *    - Broadcast intents for system-wide events.
 *    - Most common and simplest form of IPC.
 *
 * 2. ContentProvider:
 *    - Structured data sharing via content:// URIs.
 *    - Example: Contacts, MediaStore, your own data to other apps.
 *    - Supports CRUD operations with permissions.
 *
 * 3. AIDL (Android Interface Definition Language):
 *    - For fine-grained, method-level IPC between processes.
 *    - Generates Binder proxy/stub classes for cross-process calls.
 *    - Supports concurrent calls from multiple threads.
 *
 * 4. Messenger:
 *    - Simpler IPC for serial message passing (wraps AIDL).
 *    - Message-based, single-threaded handler.
 *
 * 5. Bound Services:
 *    - App A binds to App B's Service and communicates via IBinder.
 *
 * 6. FileProvider:
 *    - Share files between apps via content:// URIs with temporary permissions.
 *
 * 7. Broadcast with permissions:
 *    - sendBroadcast(intent, permission) restricts who can receive.
 *
 * All IPC in Android ultimately goes through the Binder kernel driver.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Is it possible to run an Android app in multiple processes? How?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Yes. Any component (Activity, Service, Receiver, Provider) can be
 * configured to run in a separate process via the android:process
 * attribute in AndroidManifest.xml.
 *
 * <service android:name=".HeavyService"
 *          android:process=":background" />
 *
 * Name starting with ":" is private to the app.
 * Name starting with lowercase letter is a global process (shared across apps
 * with the same sharedUserId — deprecated).
 *
 * Implications of multi-process:
 *
 * 1. Separate memory space — each process gets its own Application instance.
 *    Application.onCreate() runs once PER PROCESS.
 *
 * 2. Singletons are NOT shared — each process has its own instances.
 *    Static variables, in-memory caches, and DI graphs are separate.
 *
 * 3. SharedPreferences is NOT process-safe — use DataStore or ContentProvider.
 *
 * 4. Communication between processes requires IPC (AIDL, Messenger,
 *    ContentProvider, or Binder-based mechanisms).
 *
 * 5. Each process has its own Binder thread pool.
 *
 * Use cases:
 *   - Isolating heavy/unstable work (WebView, native libraries)
 *   - Running a long-lived service separately from the UI process
 *   - Increasing available memory (each process gets its own heap limit)
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is AIDL?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * AIDL (Android Interface Definition Language) is an IDL for defining the
 * programming interface that both the client and service agree upon for
 * inter-process communication using the Binder IPC mechanism.
 *
 * How it works:
 * 1. Define an .aidl file with the interface methods.
 * 2. The Android build tools generate a Java/Kotlin interface with
 *    Stub (server-side) and Proxy (client-side) classes.
 * 3. The Service implements the Stub and returns it from onBind().
 * 4. The client receives the Proxy via ServiceConnection.onServiceConnected()
 *    and calls methods on it as if they were local.
 * 5. The Binder framework marshals the call across processes.
 *
 * Supported types: primitives, String, CharSequence, List, Map,
 * Parcelable objects, and other AIDL interfaces.
 *
 * Threading: AIDL calls are dispatched from a Binder thread pool on the
 * server side. The Stub implementation must be thread-safe.
 *
 * When to use AIDL:
 *   - When you need multi-threaded, concurrent cross-process method calls.
 *   - When Messenger (single-threaded) is too limiting.
 *
 * For most apps, ContentProvider or implicit Intents are simpler alternatives.
 */

// Example: IMyService.aidl
// interface IMyService {
//     int add(int a, int b);
//     List<String> getItems();
// }

// Service implementation:
class MyAidlService : Service() {
    private val binder = object : IMyService.Stub() {
        override fun add(a: Int, b: Int): Int = a + b
        override fun getItems(): List<String> = listOf("A", "B", "C")
    }

    override fun onBind(intent: Intent?): IBinder = binder
}

// Client binding:
fun bindToAidlService(context: Context) {
    val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val myService = IMyService.Stub.asInterface(service)
            val result = myService.add(3, 4) // cross-process call
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    val intent = Intent("com.example.MY_SERVICE_ACTION")
    intent.setPackage("com.example.service.app")
    context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is a ContentProvider and what is it typically used for?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A ContentProvider manages access to a structured set of data. It
 * encapsulates data and provides a standard interface for other
 * applications to access it securely.
 *
 * Core interface (CRUD via ContentResolver):
 *   query()    → SELECT (returns Cursor)
 *   insert()   → INSERT (returns Uri)
 *   update()   → UPDATE (returns count)
 *   delete()   → DELETE (returns count)
 *   getType()  → returns MIME type for a URI
 *
 * Addressing: content://authority/path/id
 *   authority: unique identifier (usually package name)
 *   path: table or collection name
 *   id: specific item (optional)
 *
 * Typical uses:
 *   - Sharing data between apps (Contacts, Calendar, MediaStore)
 *   - Providing data to widgets and search suggestions
 *   - Abstracting data storage behind a standard API
 *   - Process-safe data access (ContentProvider is IPC-safe by default)
 *   - Room exports a ContentProvider via @Fts4 and RoomDatabase
 *
 * Security:
 *   - android:exported="true/false" controls inter-app access
 *   - readPermission/writePermission restrict access
 *   - grantUriPermissions for temporary per-URI access
 *
 * ContentProvider runs on the main thread by default. For heavy queries,
 * use CursorLoader or query on a background thread.
 */

class NotesProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        // Initialize database
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?,
        selection: String?, selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        return db.query("notes", projection, selection, selectionArgs, null, null, sortOrder)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val db = dbHelper.writableDatabase
        val id = db.insert("notes", null, values)
        context?.contentResolver?.notifyChange(uri, null)
        return ContentUris.withAppendedId(uri, id)
    }

    override fun update(uri: Uri, values: ContentValues?, sel: String?, args: Array<String>?): Int = 0
    override fun delete(uri: Uri, sel: String?, args: Array<String>?): Int = 0
    override fun getType(uri: Uri): String = "vnd.android.cursor.dir/vnd.example.notes"
}


// ═══════════════════════════════════════════════════════════════════════════════
// SECTION 9: LONG-RUNNING OPERATIONS & MEMORY
// ═══════════════════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────────────────────────
// Q: What is ANR? How can the ANR be prevented?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ANR (Application Not Responding) is a system dialog shown when the main
 * (UI) thread is blocked for too long:
 *
 * Thresholds:
 *   - Input event (touch, key): 5 seconds
 *   - BroadcastReceiver.onReceive(): 10 seconds (foreground)
 *   - Service (foreground): 20 seconds for onCreate/onStartCommand
 *   - ContentProvider: 10 seconds
 *
 * Common causes:
 *   - Network calls on the main thread
 *   - Database reads/writes on the main thread
 *   - Heavy computation (JSON parsing, bitmap processing) on main thread
 *   - Deadlocks or long-held locks
 *   - Synchronous IPC with a hung remote service
 *
 * Prevention strategies:
 *
 * 1. Move I/O and computation to background threads:
 *    - Coroutines with Dispatchers.IO / Dispatchers.Default
 *    - RxJava with Schedulers.io()
 *
 * 2. Use WorkManager for deferrable background work.
 *
 * 3. Use async data loading (Room + Flow, Retrofit + coroutines).
 *
 * 4. Keep BroadcastReceiver.onReceive() lightweight — delegate to
 *    WorkManager or a Service for heavy work.
 *
 * 5. Use StrictMode in debug builds to detect accidental main-thread I/O.
 *
 * 6. Profile with Android Vitals (Play Console), Perfetto, or
 *    "adb shell dumpsys activity anr" for ANR traces.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Explain Looper, Handler, and HandlerThread
// ─────────────────────────────────────────────────────────────────────────────

/**
 * These form Android's message-passing mechanism for thread communication.
 *
 * Looper:
 *   - Runs a message loop for a thread. A thread can have at most one Looper.
 *   - Looper.prepare() creates the Looper; Looper.loop() starts it.
 *   - The main thread has a Looper by default (Looper.getMainLooper()).
 *   - The Looper continuously pulls Messages/Runnables from a MessageQueue
 *     and dispatches them to their target Handler.
 *
 * Handler:
 *   - Sends and processes Messages/Runnables on a specific Looper's thread.
 *   - post(Runnable) / sendMessage(Message) enqueue work.
 *   - postDelayed(Runnable, delayMillis) for delayed execution.
 *   - Each Handler is bound to one Looper (and thus one thread).
 *
 * HandlerThread:
 *   - A Thread that has its own Looper. Convenient for creating a
 *     background thread with a message queue.
 *   - Call start(), then getLooper() to create a Handler on it.
 *
 * Common pattern: post work to the main thread from a background thread.
 *
 * Modern alternatives: Coroutines (withContext(Dispatchers.Main)) have
 * largely replaced direct Handler/Looper usage in app code. But Handlers
 * are still used internally by Android framework and in low-level scenarios.
 */

fun handlerLooperExample() {
    // Post to main thread
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post {
        // Runs on main thread — update UI here
    }

    // Background HandlerThread
    val bgThread = HandlerThread("BackgroundThread")
    bgThread.start()
    val bgHandler = Handler(bgThread.looper)
    bgHandler.post {
        // Runs on background thread
        val result = performHeavyComputation()
        mainHandler.post {
            // Post result back to main thread
            updateUI(result)
        }
    }

    // Don't forget to quit when done
    // bgThread.quitSafely()
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: Garbage Collection in Android
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Android uses ART (Android Runtime) with a generational, concurrent,
 * mostly-mark-sweep garbage collector.
 *
 * Generational model:
 *   - Young generation: newly allocated objects. Collected frequently
 *     (minor GC) since most objects die young.
 *   - Old generation: objects that survived multiple young GC cycles.
 *     Collected less frequently (major GC).
 *
 * ART GC characteristics:
 *   - Concurrent: runs mostly in parallel with app threads.
 *   - Partial pause: short stop-the-world pauses (< 1ms typical on ART).
 *   - Compacting: moves objects to reduce fragmentation (on background GC).
 *   - Foreground GC is non-compacting for lower latency.
 *
 * GC impact on performance:
 *   - Even short pauses can cause jank if they happen during frame rendering.
 *   - High allocation rate → frequent GC → more pauses.
 *   - Large heap scans take longer.
 *
 * Best practices to reduce GC pressure:
 *   1. Avoid allocations in onDraw(), onBindViewHolder(), tight loops.
 *   2. Reuse objects (object pools, StringBuilder, pre-allocated arrays).
 *   3. Use primitives instead of boxed types where possible.
 *   4. Avoid temporary object creation (iterator allocations, lambda captures).
 *   5. Size collections appropriately to avoid reallocation.
 *
 * Profiling: Android Studio Memory Profiler shows real-time allocations,
 * heap dumps, and GC events.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Memory Leak vs Out of Memory (OOM) Error
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Memory Leak:
 *   - Objects that are no longer needed but cannot be GC'd because
 *     something still holds a reference to them.
 *   - The GC graph has a path from a GC root to the object.
 *   - Gradual: memory usage grows over time.
 *   - App doesn't crash immediately but becomes slower; eventually leads to OOM.
 *   - Common causes:
 *     * Activity referenced by singleton, static field, or long-lived object
 *     * Inner class (Handler, AsyncTask, Runnable) holding implicit reference
 *     * Unregistered listeners/callbacks
 *     * Fragment retaining View references after onDestroyView()
 *
 * Out of Memory (OOM):
 *   - The system cannot allocate more memory for the app.
 *   - Throws OutOfMemoryError (unrecoverable in most cases).
 *   - Causes: loading large bitmaps, accumulated memory leaks, excessive
 *     data cached in memory, or too many Activities in back stack.
 *   - Each app has a heap limit (typically 128MB-512MB depending on device).
 *
 * Relationship: memory leaks are a common CAUSE of OOM errors.
 * Fixing leaks prevents OOM.
 *
 * Detection:
 *   - Leaks: LeakCanary (library), Memory Profiler heap dump
 *   - OOM: crash reports, Android Vitals in Play Console
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Can you explain the difference between a Runnable and a Thread in Android?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Thread:
 *   - Represents an actual thread of execution (OS-level thread).
 *   - Extends java.lang.Thread class.
 *   - Has its own call stack and can run concurrently with other threads.
 *   - Creating a Thread allocates OS resources (stack memory ~1MB).
 *   - You override run() to define the work.
 *
 * Runnable:
 *   - A functional interface with a single run() method.
 *   - Represents a UNIT OF WORK, not a thread.
 *   - Does not create a thread by itself.
 *   - Must be passed to a Thread, Handler, Executor, or thread pool to execute.
 *   - More flexible: same Runnable can run on different threads.
 *   - Preferred over extending Thread because it supports composition
 *     and doesn't waste the single-inheritance slot.
 *
 * In modern Android, neither is commonly used directly:
 *   - Coroutines: launch { }, async { }
 *   - Executors: Executors.newFixedThreadPool(n)
 *   - Handler.post(Runnable)
 *   - WorkManager for persistent background work
 */

fun threadVsRunnable() {
    // Thread: defines and runs work
    val thread = Thread {
        // Work on a new OS thread
    }
    thread.start()

    // Runnable: defines work, separate from execution
    val work = Runnable {
        // Just the work definition
    }
    Thread(work).start()       // Execute on a new thread
    handler.post(work)         // Execute on handler's thread
    executor.execute(work)     // Execute on a thread pool thread
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: How do you handle bitmaps in Android?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Bitmaps are one of the biggest sources of OOM errors. A 12MP photo
 * (4000x3000) takes ~48MB of memory (ARGB_8888: 4 bytes/pixel).
 *
 * Key techniques:
 *
 * 1. Downsample before loading:
 *    Use inSampleSize to load a smaller version. A sample size of 4
 *    loads at 1/4 resolution (1/16th memory).
 *
 * 2. Use appropriate Bitmap.Config:
 *    ARGB_8888: 4 bytes/pixel (default, full quality)
 *    RGB_565:   2 bytes/pixel (no alpha, good for backgrounds)
 *    HARDWARE:  stored in GPU memory, not counted against app heap
 *
 * 3. Use image loading libraries (Coil, Glide):
 *    - Automatic caching (memory + disk)
 *    - Automatic resizing to ImageView dimensions
 *    - Lifecycle-aware (cancels loads when view is destroyed)
 *    - Bitmap pooling (reuses Bitmap memory)
 *
 * 4. Recycle bitmaps when done: bitmap.recycle() (less important on ART
 *    but still good practice for large bitmaps).
 *
 * 5. Use inBitmap for bitmap reuse (Bitmap pool).
 */

fun decodeSampledBitmap(res: Resources, resId: Int, reqWidth: Int, reqHeight: Int): Bitmap {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true // Only read dimensions, don't load pixels
    }
    BitmapFactory.decodeResource(res, resId, options)

    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeResource(res, resId, options)
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqW: Int, reqH: Int): Int {
    val (height, width) = options.outHeight to options.outWidth
    var inSampleSize = 1
    if (height > reqH || width > reqW) {
        val halfH = height / 2
        val halfW = width / 2
        while (halfH / inSampleSize >= reqH && halfW / inSampleSize >= reqW) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: Tell about the Bitmap pool
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A Bitmap pool is a cache of reusable Bitmap objects that avoids repeated
 * memory allocation and GC churn when loading many images.
 *
 * How it works:
 *   1. When a Bitmap is no longer needed, instead of letting GC collect it,
 *      it is put into the pool.
 *   2. When a new Bitmap is needed, the pool checks if it has one with a
 *      compatible size. If so, it reuses the memory via BitmapFactory.Options.inBitmap.
 *   3. The pool has a max size (LRU eviction).
 *
 * BitmapFactory.Options.inBitmap (API 11+):
 *   - Tells the decoder to reuse an existing Bitmap's memory.
 *   - API 19+: the reused Bitmap just needs to be >= in byte count.
 *   - API 11-18: must be exact same dimensions and config.
 *
 * Libraries like Glide and Coil have built-in Bitmap pools. Glide's
 * BitmapPool (LruBitmapPool) is particularly sophisticated:
 *   - Groups Bitmaps by size and config
 *   - Uses SizeConfigStrategy for efficient matching
 *   - Integrates with onTrimMemory() to shrink when system needs memory
 *
 * Benefits:
 *   - Reduces GC pressure (fewer allocations/deallocations)
 *   - Reduces jank from GC pauses
 *   - Lower peak memory usage in image-heavy apps (galleries, feeds)
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is the onTrimMemory() method?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * onTrimMemory(level: Int) is a callback on Application, Activity, Fragment,
 * Service, and ContentProvider. It is called by the system when the device's
 * overall memory is low and the app should reduce its memory footprint.
 *
 * Levels (progressive severity):
 *
 * While app is running (foreground/visible):
 *   TRIM_MEMORY_RUNNING_MODERATE (5) → device is getting low; trim non-critical
 *   TRIM_MEMORY_RUNNING_LOW (10)     → device is quite low; release more
 *   TRIM_MEMORY_RUNNING_CRITICAL (15)→ device is very low; release everything possible
 *
 * When app is backgrounded:
 *   TRIM_MEMORY_UI_HIDDEN (20)   → UI no longer visible; release UI resources
 *   TRIM_MEMORY_BACKGROUND (40)  → app in LRU list; may be killed soon
 *   TRIM_MEMORY_MODERATE (60)    → app in middle of LRU list
 *   TRIM_MEMORY_COMPLETE (80)    → app near end of LRU; will be killed next
 *
 * What to release:
 *   - Image caches (Bitmap pool, Glide/Coil memory cache)
 *   - Cached data structures
 *   - Unused views or large object graphs
 *   - WebView cache
 *
 * Implementing onTrimMemory well reduces the chance of process death.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: How to identify and fix OutOfMemory issues?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Identification:
 *
 * 1. Crash reports — OutOfMemoryError in stack traces (Play Console, Firebase).
 *
 * 2. Memory Profiler (Android Studio):
 *    - Monitor real-time heap usage.
 *    - Take heap dumps and inspect retained objects.
 *    - Record allocations to see what's consuming memory.
 *
 * 3. LeakCanary — automatically detects Activity/Fragment/View leaks in
 *    debug builds. Shows the reference chain holding the leak.
 *
 * 4. adb shell dumpsys meminfo <package> — shows PSS, heap size, objects.
 *
 * 5. Android Vitals (Play Console) — aggregated memory statistics.
 *
 * Common fixes:
 *
 * 1. Fix memory leaks (the #1 cause):
 *    - Remove static Activity references
 *    - Use WeakReference or Application Context in long-lived objects
 *    - Unregister listeners in the corresponding lifecycle callback
 *    - Null out ViewBinding in Fragment.onDestroyView()
 *
 * 2. Optimize bitmap handling:
 *    - Downsample images to display size
 *    - Use Bitmap.Config.RGB_565 or HARDWARE
 *    - Use image loading libraries with caching
 *
 * 3. Reduce retained data:
 *    - Paginate lists (Paging 3 library)
 *    - LRU cache with a size limit
 *    - Release caches in onTrimMemory()
 *
 * 4. Avoid loading everything at once:
 *    - Lazy initialization
 *    - On-demand resource loading
 *
 * 5. Use largeHeap="true" as a LAST RESORT (not a fix, just a band-aid).
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: How do you find memory leaks in Android applications?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 1. LeakCanary (primary tool for development):
 *    - Add dependency: debugImplementation("com.squareup.leakcanary:leakcanary-android:2.x")
 *    - Automatically watches Activities, Fragments, ViewModels, Views.
 *    - On leak detection, shows a notification with the leak trace.
 *    - The trace shows the chain of references preventing GC.
 *    - Zero configuration needed — just add the dependency.
 *
 * 2. Android Studio Memory Profiler:
 *    - Record allocations over time.
 *    - Take heap dump → analyze retained objects.
 *    - Look for multiple instances of Activity/Fragment that should be GC'd.
 *    - Force GC (trash can icon) then dump — anything retained is suspicious.
 *
 * 3. adb shell dumpsys meminfo:
 *    - Compare heap size before and after screen rotation.
 *    - Growing heap = possible leak.
 *
 * 4. StrictMode.VmPolicy:
 *    - detectActivityLeaks() logs when multiple Activity instances exist.
 *    - detectLeakedClosableObjects() finds unclosed streams/cursors.
 *
 * 5. Manual testing pattern:
 *    - Open Activity → rotate 20 times → force GC → heap dump.
 *    - If you see 20 Activity instances, you have a leak.
 *    - Navigate forward/back rapidly, then force GC and check heap.
 *
 * Common leak patterns to watch for:
 *   - Inner class holding outer class reference (Handler, Callback)
 *   - Static reference to Context/View
 *   - ViewBinding not cleared in Fragment.onDestroyView()
 *   - Unregistered listener/observer
 *   - ViewModel holding View or Activity reference
 */


// ────────────────────────────────────────────────────────────────────────────
// SECTION 10: DATA SAVING
// ⛔ STUB — Full content removed (duplicates 05_data_persistence.kt)
// 📖 Primary reference: 05_data_persistence.kt
// ────────────────────────────────────────────────────────────────────────────

/**
 * This section duplicated content from 05_data_persistence.kt.
 * Read that file for interview tips (💡), gotchas (⚠️), and follow-ups (❓).
 *
 * Quick topics covered there:
 *   - Room vs SQLite vs DataStore
 *   - SharedPreferences vs DataStore
 *   - ContentProvider basics
 *
 * Use this file (17) for UNIQUE sections only — see header navigation table.
 */

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SECTION 11 — ANDROID LIBRARIES
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// ─────────────────────────────────────────────────────────────────────────────
// Q: Explain OkHttp Interceptor
// ─────────────────────────────────────────────────────────────────────────────

/**
 * An OkHttp Interceptor is a mechanism that can intercept, monitor, modify,
 * and retry HTTP requests and responses as they pass through the OkHttp pipeline.
 *
 * Two types of interceptors:
 *
 * 1. APPLICATION INTERCEPTOR — added via addInterceptor()
 *    - Called once per logical request (not for redirects/retries).
 *    - Can short-circuit the chain and return a cached response.
 *    - Has access to the original, unmodified request.
 *    - Use case: adding auth headers, logging business-level requests.
 *
 * 2. NETWORK INTERCEPTOR — added via addNetworkInterceptor()
 *    - Called for every network call (including redirects/retries).
 *    - Has access to the Connection and the actual network request.
 *    - Use case: detailed network logging, modifying redirected requests.
 *
 * Execution order:
 *   App Interceptors → Cache → Network Interceptors → Server
 */

class AuthInterceptor(private val tokenProvider: () -> String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${tokenProvider()}")
            .header("Accept", "application/json")
            .build()

        val response = chain.proceed(authenticatedRequest)

        // Retry on 401 with refreshed token
        if (response.code == 401) {
            response.close()
            val refreshedToken = refreshToken()
            val retryRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $refreshedToken")
                .build()
            return chain.proceed(retryRequest)
        }

        return response
    }
}

class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()

        Log.d("HTTP", "→ ${request.method} ${request.url}")

        val response = chain.proceed(request)
        val duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)

        Log.d("HTTP", "← ${response.code} ${request.url} (${duration}ms)")
        return response
    }
}

val client = OkHttpClient.Builder()
    .addInterceptor(AuthInterceptor { getAccessToken() })      // app-level
    .addInterceptor(LoggingInterceptor())                       // app-level
    .addNetworkInterceptor(StethoInterceptor())                 // network-level
    .build()


// ─────────────────────────────────────────────────────────────────────────────
// Q: OkHttp — HTTP Caching
// ─────────────────────────────────────────────────────────────────────────────

/**
 * OkHttp supports HTTP caching by honoring standard Cache-Control headers.
 * It stores responses on disk and can serve them without hitting the network.
 *
 * How it works:
 * 1. Server sends Cache-Control headers (max-age, no-cache, no-store, etc.)
 * 2. OkHttp stores the response in a DiskLruCache.
 * 3. On subsequent requests:
 *    - If cache is fresh (within max-age) → return cached response directly.
 *    - If cache is stale → send conditional request with If-None-Match (ETag)
 *      or If-Modified-Since. Server returns 304 Not Modified if unchanged.
 *    - If no-store → never cache.
 *
 * Key headers:
 *   Cache-Control: max-age=3600        → fresh for 1 hour
 *   Cache-Control: no-cache            → always validate with server
 *   Cache-Control: no-store            → never store
 *   ETag: "abc123"                     → entity tag for conditional requests
 *   Cache-Control: max-stale=86400     → accept stale up to 1 day (client-side)
 *
 * For offline support, use CacheControl.FORCE_CACHE on the client side,
 * but note it will return 504 if nothing is cached.
 */

val cacheSize = 10L * 1024 * 1024 // 10 MB
val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)

val cachedClient = OkHttpClient.Builder()
    .cache(cache)
    .addInterceptor { chain ->
        var request = chain.request()
        // If offline, force reading from cache
        if (!isNetworkAvailable()) {
            request = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
        }
        chain.proceed(request)
    }
    .addNetworkInterceptor { chain ->
        val response = chain.proceed(chain.request())
        val cacheControl = CacheControl.Builder()
            .maxAge(5, TimeUnit.MINUTES)
            .build()
        response.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .removeHeader("Pragma")
            .build()
    }
    .build()


// ─────────────────────────────────────────────────────────────────────────────
// Q: Why do we use Dependency Injection Framework like Dagger?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dependency Injection (DI) is a design pattern where objects receive their
 * dependencies from external sources rather than creating them internally.
 *
 * Without DI:
 *   class UserRepository {
 *       val api = RetrofitClient.create()  // tightly coupled
 *       val db  = RoomDatabase.create()    // hard to test, hard to swap
 *   }
 *
 * With DI:
 *   class UserRepository(val api: ApiService, val db: UserDao)
 *   // dependencies provided externally — testable, flexible
 *
 * Why use a framework like Dagger instead of manual DI?
 *
 * 1. COMPILE-TIME SAFETY — Dagger generates code at compile time.
 *    Missing dependencies cause build errors, not runtime crashes.
 *
 * 2. SCOPING — Dagger manages object lifetimes (@Singleton, @ActivityScoped).
 *    No need to manually track which objects should be shared.
 *
 * 3. REDUCES BOILERPLATE — For large apps, manual DI requires a massive
 *    "dependency container" class. Dagger auto-generates the wiring.
 *
 * 4. TESTABILITY — Easily swap real implementations with fakes/mocks
 *    by providing a test module.
 *
 * 5. PERFORMANCE — Dagger uses zero reflection (unlike Guice/Koin).
 *    All dependency wiring is generated at compile time as plain Java code.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: How does Dagger work? Explain @Inject, @Module, @Provides, @Component
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dagger uses annotation processing to generate a dependency graph at compile time.
 *
 * @Inject — Marks a constructor, field, or method to request dependency injection.
 *   - On a constructor: tells Dagger "you can create this object."
 *   - On a field: tells Dagger "fill this field with the appropriate dependency."
 *
 * @Module — A class that provides dependencies that Dagger can't construct
 *           directly (interfaces, third-party classes, configured objects).
 *
 * @Provides — A method inside a @Module that tells Dagger how to create
 *             a specific type.
 *
 * @Component — An interface that bridges @Modules and @Inject sites.
 *              Dagger generates an implementation (DaggerXxxComponent) that
 *              wires everything together.
 *
 * @Binds — A more efficient alternative to @Provides for binding an
 *          interface to its implementation (abstract method in abstract module).
 *
 * Flow:
 *   1. Dagger scans @Inject constructors → knows how to create those classes.
 *   2. For things it can't auto-create, it looks at @Module/@Provides.
 *   3. @Component defines the entry points and which modules to include.
 *   4. At compile time, Dagger generates DaggerAppComponent which contains
 *      the full object graph and factory methods.
 */

// Constructor injection — Dagger knows how to create this
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao
) {
    fun getUser(id: String): Flow<User> = flow {
        val cached = userDao.getById(id)
        if (cached != null) emit(cached)
        val remote = apiService.fetchUser(id)
        userDao.insert(remote)
        emit(remote)
    }
}

// Module for things Dagger can't auto-construct
@Module
class NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideApiService(client: OkHttpClient): ApiService =
        Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
}

@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindUserRepo(impl: UserRepository): IUserRepository
}

// Component ties it all together
@Singleton
@Component(modules = [NetworkModule::class, RepositoryModule::class, DatabaseModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
    fun userRepository(): UserRepository

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}

// Usage in Application class
// val appComponent = DaggerAppComponent.factory().create(applicationContext)


// ─────────────────────────────────────────────────────────────────────────────
// Q: How will you choose between Dagger 2 and Dagger-Hilt?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dagger-Hilt is built on top of Dagger 2, adding Android-specific conventions.
 *
 * ┌──────────────────┬─────────────────────────┬──────────────────────────┐
 * │ Criterion        │ Dagger 2                │ Dagger-Hilt              │
 * ├──────────────────┼─────────────────────────┼──────────────────────────┤
 * │ Setup            │ Manual Components,      │ Predefined components,   │
 * │                  │ Subcomponents, Scopes   │ @HiltAndroidApp, auto    │
 * ├──────────────────┼─────────────────────────┼──────────────────────────┤
 * │ Boilerplate      │ High — define every     │ Low — annotations do     │
 * │                  │ component + scope       │ most of the work         │
 * ├──────────────────┼─────────────────────────┼──────────────────────────┤
 * │ Scoping          │ Custom scopes per       │ Predefined: Singleton,   │
 * │                  │ component               │ ActivityScoped, etc.     │
 * ├──────────────────┼─────────────────────────┼──────────────────────────┤
 * │ Testing          │ Manual test components  │ Built-in @UninstallModules│
 * │                  │                         │ @BindValue, HiltRule     │
 * ├──────────────────┼─────────────────────────┼──────────────────────────┤
 * │ ViewModel inject │ Manual ViewModelFactory │ @HiltViewModel auto-     │
 * │                  │                         │ injects via hiltViewModel│
 * ├──────────────────┼─────────────────────────┼──────────────────────────┤
 * │ Flexibility      │ Full control over graph │ Less flexible but covers │
 * │                  │                         │ 95% of Android use cases │
 * └──────────────────┴─────────────────────────┴──────────────────────────┘
 *
 * CHOOSE HILT when:
 *   - Starting a new Android project (Google's recommended approach).
 *   - You want standardized scoping tied to Android lifecycles.
 *   - You need easy ViewModel injection and testing support.
 *
 * CHOOSE DAGGER 2 when:
 *   - Multi-platform (KMM) project — Hilt is Android-only.
 *   - You need fine-grained custom scopes beyond Hilt's predefined ones.
 *   - Migrating an existing large Dagger 2 codebase incrementally.
 */

@HiltAndroidApp
class MyApp : Application()

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    // Hilt auto-creates the ViewModel with all dependencies
    private val viewModel: UserViewModel by viewModels()
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // ...
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is Flow in Kotlin?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Flow is Kotlin's cold asynchronous data stream, built on coroutines.
 * It's the reactive streams replacement for RxJava in the Kotlin ecosystem.
 *
 * KEY PROPERTIES:
 *
 * 1. COLD — A Flow doesn't execute until it's collected. Each collector
 *    gets its own independent execution of the flow body.
 *
 * 2. SEQUENTIAL — Emissions and transformations happen in order.
 *
 * 3. CANCELLABLE — Flow respects structured concurrency. When the
 *    coroutine collecting is cancelled, the flow stops.
 *
 * 4. CONTEXT PRESERVATION — Flow preserves the coroutine context.
 *    Use flowOn() to change the upstream dispatcher (never downstream).
 *
 * Flow vs StateFlow vs SharedFlow:
 *
 *   Flow       — Cold. Each collector triggers execution.
 *   StateFlow  — Hot. Always has a value. Conflates (skips intermediate).
 *                Equivalent to LiveData but lifecycle-unaware.
 *   SharedFlow — Hot. No initial value. Configurable replay/buffer.
 *                Useful for events (navigation, snackbars).
 *
 * Common operators: map, filter, flatMapLatest, combine, zip, catch,
 *                   onEach, debounce, distinctUntilChanged, flowOn
 */

// Basic Flow
fun fetchUsers(): Flow<List<User>> = flow {
    val users = apiService.getUsers()
    emit(users)
}.flowOn(Dispatchers.IO)

// Flow with Room (auto-emits on DB changes)
@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun observeAll(): Flow<List<User>>
}

// Combining Flows
fun observeUserWithPosts(userId: String): Flow<UserWithPosts> =
    combine(
        userDao.observeById(userId),
        postDao.observeByUserId(userId)
    ) { user, posts ->
        UserWithPosts(user, posts)
    }

// StateFlow in ViewModel
class SearchViewModel @Inject constructor(
    private val searchRepo: SearchRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val results: StateFlow<List<SearchResult>> = _query
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else searchRepo.search(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChanged(text: String) { _query.value = text }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: App Startup Library
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The AndroidX App Startup library provides a performant way to initialize
 * components at application startup by consolidating ContentProvider-based
 * initialization into a single ContentProvider.
 *
 * THE PROBLEM:
 * Many libraries (WorkManager, Firebase, etc.) use a ContentProvider to
 * auto-initialize. Each ContentProvider adds ~2ms to cold start. With 10+
 * libraries, that's 20+ms wasted just on ContentProvider instantiation.
 *
 * THE SOLUTION:
 * App Startup merges all initializations into a single ContentProvider
 * (InitializationProvider) and provides a dependency graph so components
 * initialize in the correct order.
 *
 * KEY CONCEPTS:
 * - Initializer<T> — Interface for defining a component's initialization.
 * - dependencies() — Declare which initializers must run first.
 * - Lazy initialization — Components can be initialized on demand, not at startup.
 */

class WorkManagerInitializer : Initializer<WorkManager> {

    override fun create(context: Context): WorkManager {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
        WorkManager.initialize(context, config)
        return WorkManager.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

class AnalyticsInitializer : Initializer<AnalyticsTracker> {

    override fun create(context: Context): AnalyticsTracker {
        // This depends on WorkManager being initialized first
        return AnalyticsTracker.init(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf(WorkManagerInitializer::class.java)
}

// In AndroidManifest.xml:
// <provider
//     android:name="androidx.startup.InitializationProvider"
//     android:authorities="${applicationId}.androidx-startup"
//     android:exported="false"
//     tools:node="merge">
//     <meta-data
//         android:name="com.example.AnalyticsInitializer"
//         android:value="androidx.startup" />
// </provider>
//
// For lazy init: AppInitializer.getInstance(context)
//     .initializeComponent(AnalyticsInitializer::class.java)


// ─────────────────────────────────────────────────────────────────────────────
// Q: How The Android Image Loading Library Glide and Fresco Works?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Both Glide and Fresco load images efficiently, but use different strategies.
 *
 * ╔═══════════════════════════════════════════════════════════════════════╗
 * ║                         GLIDE ARCHITECTURE                          ║
 * ╠═══════════════════════════════════════════════════════════════════════╣
 * ║                                                                     ║
 * ║  Request → MemoryCache (ActiveResources + LruResourceCache)         ║
 * ║         → DiskCache (Resource / Data)                               ║
 * ║         → Network (HttpUrlConnection / OkHttp)                      ║
 * ║         → Decode → Transform → Encode to Cache → Display           ║
 * ║                                                                     ║
 * ║  Key features:                                                      ║
 * ║  • Lifecycle-aware — ties to Activity/Fragment, auto-pauses/cancels ║
 * ║  • Two-level memory cache: active resources (weak refs) + LRU      ║
 * ║  • Disk cache stores both original and transformed images           ║
 * ║  • BitmapPool — recycles Bitmap objects to reduce GC pressure       ║
 * ║  • Thumbnail support, crossfade transitions, GIF/video frames       ║
 * ║  • Caches images at the size of the target ImageView (saves memory) ║
 * ╚═══════════════════════════════════════════════════════════════════════╝
 *
 * ╔═══════════════════════════════════════════════════════════════════════╗
 * ║                        FRESCO ARCHITECTURE                          ║
 * ╠═══════════════════════════════════════════════════════════════════════╣
 * ║                                                                     ║
 * ║  Uses a three-level cache:                                          ║
 * ║  1. Bitmap Cache (decoded, in Java heap / Ashmem on < API 21)      ║
 * ║  2. Encoded Memory Cache (compressed bytes in memory)               ║
 * ║  3. Disk Cache (two tiers: small + default)                         ║
 * ║                                                                     ║
 * ║  Key features:                                                      ║
 * ║  • On pre-Lollipop: stores Bitmaps in Ashmem (native memory),      ║
 * ║    avoiding OOM crashes that plague the Java heap.                   ║
 * ║  • SimpleDraweeView — custom view with built-in placeholder,        ║
 * ║    failure image, retry, progress bar, rounded corners.             ║
 * ║  • Image pipeline architecture — producer/consumer pattern.         ║
 * ║  • Progressive JPEG rendering (shows image as it downloads).        ║
 * ║  • Better for apps with many large images on older devices.         ║
 * ╚═══════════════════════════════════════════════════════════════════════╝
 *
 * GLIDE vs FRESCO decision:
 *   - Glide: Lighter, simpler API, works with standard ImageView,
 *            better for most apps. Google's recommended library.
 *   - Fresco: Better for image-heavy apps (social media feeds) on older
 *             devices, progressive JPEG, but heavier footprint and requires
 *             SimpleDraweeView (can't use standard ImageView).
 */


// ────────────────────────────────────────────────────────────────────────────
// SECTION 12: ANDROID ARCHITECTURE
// ⛔ STUB — Full content removed (duplicates 02_app_architecture.kt)
// 📖 Primary reference: 02_app_architecture.kt
// ────────────────────────────────────────────────────────────────────────────

/**
 * This section duplicated content from 02_app_architecture.kt.
 * Read that file for interview tips (💡), gotchas (⚠️), and follow-ups (❓).
 *
 * Quick topics covered there:
 *   - MVVM with LiveData/Flow
 *   - Repository pattern
 *   - Clean Architecture layers
 *
 * Use this file (17) for UNIQUE sections only — see header navigation table.
 */

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SECTION 13 — DESIGN PATTERNS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// ─────────────────────────────────────────────────────────────────────────────
// Q: Builder Pattern
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Builder Pattern separates construction of a complex object from its
 * representation, allowing step-by-step construction with a fluent API.
 *
 * Android examples: AlertDialog.Builder, Notification.Builder, OkHttpClient.Builder,
 *                   Retrofit.Builder, Room.databaseBuilder
 *
 * In Kotlin, often replaced by data classes with default parameters + copy(),
 * or by DSL builders using lambda-with-receiver.
 */

// Java-style Builder
class ImageRequest private constructor(
    val url: String,
    val width: Int,
    val height: Int,
    val placeholder: Int?,
    val cacheStrategy: CacheStrategy
) {
    class Builder(private val url: String) {
        private var width: Int = 0
        private var height: Int = 0
        private var placeholder: Int? = null
        private var cacheStrategy: CacheStrategy = CacheStrategy.DEFAULT

        fun size(w: Int, h: Int) = apply { width = w; height = h }
        fun placeholder(res: Int) = apply { placeholder = res }
        fun cacheStrategy(s: CacheStrategy) = apply { cacheStrategy = s }

        fun build(): ImageRequest =
            ImageRequest(url, width, height, placeholder, cacheStrategy)
    }
}

// Kotlin DSL Builder (preferred in Kotlin)
class NotificationConfig {
    var title: String = ""
    var body: String = ""
    var channelId: String = "default"
    var priority: Int = NotificationCompat.PRIORITY_DEFAULT
    var autoCancel: Boolean = true
}

fun notification(block: NotificationConfig.() -> Unit): NotificationConfig =
    NotificationConfig().apply(block)

// Usage: val config = notification { title = "Hello"; body = "World"; autoCancel = true }


// ─────────────────────────────────────────────────────────────────────────────
// Q: Singleton Pattern
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Singleton ensures a class has only one instance and provides a global
 * access point. Used for shared resources (database, network client, logger).
 *
 * Android examples: Application class, Room database instance, OkHttpClient,
 *                   AnalyticsTracker, SharedPreferences wrappers.
 *
 * In Kotlin, `object` declaration is a thread-safe singleton by default
 * (initialized lazily on first access, backed by static holder pattern).
 *
 * ⚠️ Caution: Singletons holding Activity Context → memory leak.
 *              Use Application Context for singletons.
 */

// Kotlin object — thread-safe singleton
object AppLogger {
    fun d(tag: String, message: String) { Log.d(tag, message) }
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }
}

// Double-checked locking (when you need constructor params)
class DatabaseProvider private constructor(context: Context) {
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "app.db"
    ).build()

    companion object {
        @Volatile private var INSTANCE: DatabaseProvider? = null

        fun getInstance(context: Context): DatabaseProvider =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseProvider(context).also { INSTANCE = it }
            }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: Factory Pattern
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Factory Pattern creates objects without exposing creation logic.
 * The caller specifies what type is needed; the factory decides how to build it.
 *
 * Android examples: ViewModelProvider.Factory, LayoutInflater, Fragment.instantiate,
 *                   BitmapFactory, Retrofit.create()
 */

// Simple Factory
sealed class AnalyticsEvent {
    data class ScreenView(val name: String) : AnalyticsEvent()
    data class ButtonClick(val id: String) : AnalyticsEvent()
    data class Purchase(val amount: Double) : AnalyticsEvent()
}

interface AnalyticsProvider {
    fun logEvent(event: AnalyticsEvent)
}

object AnalyticsFactory {
    fun create(type: String): AnalyticsProvider = when (type) {
        "firebase" -> FirebaseAnalyticsProvider()
        "mixpanel" -> MixpanelAnalyticsProvider()
        "debug"    -> DebugAnalyticsProvider()
        else       -> throw IllegalArgumentException("Unknown: $type")
    }
}

// Abstract Factory — ViewModelProvider.Factory
class UserViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: Observer Pattern
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Observer Pattern defines a one-to-many dependency: when one object (Subject)
 * changes state, all its dependents (Observers) are notified automatically.
 *
 * Android examples: LiveData, StateFlow, SharedFlow, OnClickListener,
 *                   BroadcastReceiver, ContentObserver, lifecycle observers.
 *
 * LiveData is a lifecycle-aware observer — it only notifies ACTIVE observers
 * (STARTED or RESUMED), preventing crashes from updating destroyed views.
 */

// Custom observable pattern
class EventBus {
    private val listeners = mutableMapOf<String, MutableList<(Any) -> Unit>>()

    fun subscribe(event: String, listener: (Any) -> Unit) {
        listeners.getOrPut(event) { mutableListOf() }.add(listener)
    }

    fun publish(event: String, data: Any) {
        listeners[event]?.forEach { it(data) }
    }

    fun unsubscribe(event: String, listener: (Any) -> Unit) {
        listeners[event]?.remove(listener)
    }
}

// StateFlow as observer pattern (preferred in modern Android)
class ThemeManager {
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleTheme() { _isDarkMode.value = !_isDarkMode.value }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: Repository Pattern
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Repository Pattern abstracts data access behind a clean API.
 * The ViewModel asks the Repository for data without knowing whether
 * it comes from network, database, cache, or any combination.
 *
 * Single Source of Truth (SSOT):
 *   Network → Cache to DB → Expose DB as Flow → UI observes DB
 *   This ensures the UI always shows consistent data.
 *
 * Benefits:
 *   • Decouples business logic from data sources.
 *   • Easy to swap data sources (e.g., migrate from Realm to Room).
 *   • Centralized caching strategy.
 *   • Testable — mock the repository in ViewModel tests.
 */

class NewsRepository(
    private val api: NewsApiService,
    private val dao: NewsDao,
    private val prefs: PreferencesManager
) {
    fun getArticles(category: String): Flow<Resource<List<Article>>> = flow {
        emit(Resource.Loading())

        val cached = dao.getByCategory(category)
        if (cached.isNotEmpty()) emit(Resource.Success(cached))

        try {
            val remote = api.getArticles(category)
            dao.deleteByCategory(category)
            dao.insertAll(remote.map { it.toEntity() })
            val updated = dao.getByCategory(category)
            emit(Resource.Success(updated))
        } catch (e: Exception) {
            if (cached.isEmpty()) emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }
}

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: Adapter Pattern
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Adapter Pattern converts the interface of a class into another interface
 * that the client expects. Allows incompatible interfaces to work together.
 *
 * Android examples:
 *   RecyclerView.Adapter — adapts data to RecyclerView's ViewHolder system.
 *   ListAdapter — adapts DiffUtil to RecyclerView.Adapter.
 *   ArrayAdapter — adapts arrays/lists to ListView.
 *   CursorAdapter — adapts database Cursor to ListView.
 *
 * Also used in data mapping: API DTO → Domain Model → UI Model.
 */

// DTO → Domain model adapter/mapper
data class UserDto(val user_id: String, val full_name: String, val avatar_url: String?)

data class UserDomain(val id: String, val name: String, val avatarUrl: String)

class UserDtoAdapter {
    fun toDomain(dto: UserDto): UserDomain = UserDomain(
        id = dto.user_id,
        name = dto.full_name,
        avatarUrl = dto.avatar_url ?: "https://default-avatar.png"
    )

    fun toDto(domain: UserDomain): UserDto = UserDto(
        user_id = domain.id,
        full_name = domain.name,
        avatar_url = domain.avatarUrl
    )
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: Facade Pattern
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Facade Pattern provides a simplified interface to a complex subsystem.
 * Hides complexity behind a single entry point.
 *
 * Android examples:
 *   Retrofit — facades complex HTTP, serialization, threading logic.
 *   MediaPlayer — facades codec, audio focus, surface management.
 *   Context — facades system services, resources, package manager.
 *   Glide.with().load().into() — facades caching, decoding, threading.
 */

class MediaFacade(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun playAudio(uri: Uri) {
        requestAudioFocus()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, uri).apply {
            setOnCompletionListener { releaseAudioFocus() }
            start()
        }
    }

    fun pause() { mediaPlayer?.pause() }
    fun resume() { mediaPlayer?.start() }
    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
        releaseAudioFocus()
    }

    private fun requestAudioFocus() { /* AudioFocusRequest logic */ }
    private fun releaseAudioFocus() { /* abandon focus */ }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: Strategy Pattern
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Strategy Pattern defines a family of algorithms, encapsulates each one,
 * and makes them interchangeable at runtime.
 *
 * Android examples:
 *   RecyclerView.LayoutManager — LinearLayout, Grid, StaggeredGrid strategies.
 *   Interpolator — AccelerateInterpolator, DecelerateInterpolator, etc.
 *   Comparator<T> — different sorting strategies.
 *   OkHttp Authenticator — different auth strategies.
 */

interface CompressionStrategy {
    fun compress(file: File): File
}

class JpegCompression(private val quality: Int = 80) : CompressionStrategy {
    override fun compress(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        val output = File(file.parent, "compressed_${file.name}")
        FileOutputStream(output).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
        }
        return output
    }
}

class PngCompression : CompressionStrategy {
    override fun compress(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        val output = File(file.parent, "compressed_${file.name}")
        FileOutputStream(output).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return output
    }
}

class WebPCompression(private val quality: Int = 75) : CompressionStrategy {
    override fun compress(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        val output = File(file.parent, "compressed_${file.name}")
        FileOutputStream(output).use {
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, it)
        }
        return output
    }
}

// Context class that uses the strategy
class ImageUploader(private var compressionStrategy: CompressionStrategy) {
    fun setStrategy(strategy: CompressionStrategy) {
        compressionStrategy = strategy
    }
    fun upload(file: File) {
        val compressed = compressionStrategy.compress(file)
        // upload compressed file...
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: Dependency Injection Pattern
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dependency Injection (DI) is an implementation of Inversion of Control (IoC).
 * A class receives its dependencies from an external source instead of creating them.
 *
 * Three types of DI:
 *
 * 1. CONSTRUCTOR INJECTION (preferred) — deps passed via constructor.
 *    class ViewModel(private val repo: Repository)
 *
 * 2. FIELD/PROPERTY INJECTION — deps assigned to fields after construction.
 *    @Inject lateinit var repo: Repository  (used by Dagger for Activities)
 *
 * 3. METHOD INJECTION — deps passed via method parameters.
 *    fun process(validator: Validator) { ... }
 *
 * Constructor injection is preferred because:
 *   • Immutability — dependencies can be val.
 *   • Completeness — object is fully initialized at construction.
 *   • Testability — easy to pass fakes/mocks.
 *   • No reflection or framework needed (can do manual DI).
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Design Patterns used in Android (Retrofit, Glide, AOSP)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * RETROFIT:
 *   • Builder        — Retrofit.Builder() constructs configured instance
 *   • Factory        — create() generates API interface implementations
 *   • Adapter        — CallAdapter converts Call<T> to Observable/Flow/etc.
 *   • Strategy       — Converter.Factory (Gson, Moshi, Protobuf)
 *   • Proxy          — Dynamic Proxy creates interface implementation at runtime
 *   • Facade         — Hides OkHttp, serialization, threading complexity
 *
 * GLIDE:
 *   • Builder        — RequestBuilder chain: with().load().transform().into()
 *   • Singleton      — Glide.get() returns single instance
 *   • Facade         — Simple API hides disk/memory caching, decoding, threading
 *   • Strategy       — DiskCacheStrategy (ALL, NONE, DATA, RESOURCE, AUTOMATIC)
 *   • Observer       — Lifecycle-aware, auto-pauses on onStop
 *   • Flyweight      — BitmapPool recycles Bitmap objects
 *
 * AOSP (Android Open Source Project):
 *   • Observer       — BroadcastReceiver, ContentObserver, OnClickListener
 *   • Singleton      — Application, SystemServices (ActivityManager, etc.)
 *   • Factory        — LayoutInflater.Factory, ViewModelProvider.Factory
 *   • Mediator       — Activity mediates between Fragments
 *   • Template       — AsyncTask (doInBackground → onPostExecute)
 *   • Command        — Intent (encapsulates action + data for later execution)
 *   • Bridge         — View system (platform View ↔ rendering backends)
 *   • Composite      — ViewGroup contains Views (tree structure)
 *   • Iterator       — Cursor iterates over database rows
 *   • Chain of Resp. — Touch event dispatching (dispatchTouchEvent chain)
 */


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SECTION 14 — ANDROID SYSTEM DESIGN
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// ─────────────────────────────────────────────────────────────────────────────
// Q: Design an Image Loading Library
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Requirements: Load images from URL → display in ImageView, with caching.
 *
 * ARCHITECTURE:
 *
 *   Request → MemoryCache → DiskCache → Network → Decode → Display
 *
 * COMPONENTS:
 *
 * 1. IMAGE REQUEST
 *    - URL, target ImageView, placeholder, error image, transformations
 *    - Keyed by URL + size + transformations (hashed)
 *
 * 2. MEMORY CACHE (LruCache)
 *    - Stores decoded Bitmaps, keyed by request key.
 *    - Size = 1/8 of available heap (Runtime.maxMemory() / 8).
 *    - Eviction: Least-Recently-Used.
 *    - Use WeakReference pool for "active" images currently displayed.
 *
 * 3. DISK CACHE (DiskLruCache)
 *    - Stores encoded image bytes on disk.
 *    - Size ~50–100 MB.
 *    - Two caches: original data + transformed/resized result.
 *
 * 4. NETWORK FETCHER
 *    - OkHttp / HttpURLConnection.
 *    - Supports cancellation, redirects, streaming.
 *    - Concurrent request coalescing: if same URL is in-flight, wait
 *      instead of making a duplicate network call.
 *
 * 5. DECODER
 *    - BitmapFactory.decodeStream with inSampleSize for downsampling.
 *    - Calculate inSampleSize from target ImageView size to avoid OOM.
 *    - EXIF rotation handling.
 *
 * 6. BITMAP POOL
 *    - Recycles Bitmap objects (Bitmap.reconfigure / BitmapFactory.Options.inBitmap).
 *    - Reduces GC pressure and allocation cost.
 *
 * 7. LIFECYCLE MANAGEMENT
 *    - Tie requests to Activity/Fragment lifecycle.
 *    - Cancel pending requests on onStop/onDestroy.
 *    - Use an invisible Fragment or LifecycleObserver.
 *
 * 8. DISPATCHER / THREAD POOL
 *    - IO thread pool for network + disk reads.
 *    - Main thread for ImageView.setImageBitmap().
 *    - Priority queue: visible items > prefetch.
 *
 * KEY DESIGN DECISIONS:
 *   - Cache key includes size → same URL at different sizes = different entries.
 *   - Request coalescing prevents thundering herd.
 *   - Placeholder shown immediately, crossfade on load.
 *   - Error handling with retry and fallback image.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Design File Downloader Library
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Requirements: Download files from URL with pause/resume, progress, queue.
 *
 * ARCHITECTURE:
 *
 *   DownloadRequest → Queue → Scheduler → DownloadTask → Storage
 *                                ↓
 *                          Progress/Status → Observer (UI)
 *
 * COMPONENTS:
 *
 * 1. DOWNLOAD REQUEST
 *    - URL, destination path, headers, tag, priority, network type constraint.
 *
 * 2. DOWNLOAD QUEUE (PriorityBlockingQueue)
 *    - Enqueue/dequeue by priority.
 *    - Max concurrent downloads (e.g., 3 parallel).
 *    - FIFO within same priority.
 *
 * 3. SCHEDULER
 *    - ThreadPoolExecutor with bounded threads.
 *    - Respects network type constraint (WiFi-only for large files).
 *    - Listens for connectivity changes to pause/resume.
 *
 * 4. DOWNLOAD TASK (actual download logic)
 *    - HTTP Range header for resume: "Range: bytes=<downloaded>-"
 *    - Server must return 206 Partial Content for resume to work.
 *    - Store ETag/Last-Modified for validation.
 *    - Write to temp file → rename on completion (atomic).
 *    - Chunk-based writing with progress callback.
 *
 * 5. PERSISTENCE (Room DB)
 *    - Store download metadata: URL, status, bytesDownloaded, totalBytes,
 *      filePath, ETag. Survives process death.
 *
 * 6. STATUS REPORTING
 *    - States: QUEUED → DOWNLOADING → PAUSED → COMPLETED / FAILED
 *    - Progress callback with bytesDownloaded / totalBytes.
 *    - Use LiveData/Flow for reactive UI updates.
 *
 * 7. RETRY STRATEGY
 *    - Exponential backoff: 1s → 2s → 4s → 8s → ... (capped at 60s).
 *    - Max retries per download (e.g., 3).
 *    - Distinguish retryable (timeout, 503) vs permanent (404) failures.
 *
 * 8. INTEGRITY
 *    - Optional MD5/SHA-256 checksum verification after download.
 *    - Content-Length validation.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Design WhatsApp / Chat App
// ─────────────────────────────────────────────────────────────────────────────

/**
 * HIGH-LEVEL ARCHITECTURE:
 *
 *   Client ←──WebSocket──→ Chat Server ←──→ Message Queue ←──→ DB
 *      │                       │
 *      └── Local DB ←──────── Push Notification (FCM) when offline
 *
 * CLIENT-SIDE ARCHITECTURE:
 *
 * 1. TRANSPORT LAYER
 *    - WebSocket for real-time bidirectional messaging.
 *    - Fallback to HTTP long-polling if WebSocket fails.
 *    - Automatic reconnection with exponential backoff.
 *    - Heartbeat/ping to detect dead connections.
 *
 * 2. LOCAL DATABASE (Room)
 *    - Messages table: id, chatId, senderId, content, timestamp, status
 *    - Status: SENDING → SENT → DELIVERED → READ
 *    - Chats table: id, participants, lastMessage, unreadCount
 *    - Full-text search on message content (FTS4).
 *
 * 3. MESSAGE FLOW (send):
 *    a. User sends → Insert into local DB with status SENDING
 *    b. Send via WebSocket → Server acknowledges → Update status to SENT
 *    c. Recipient receives → sends delivery receipt → DELIVERED
 *    d. Recipient reads → sends read receipt → READ
 *    e. If offline → Queue locally, sync when connected
 *
 * 4. MESSAGE FLOW (receive):
 *    a. WebSocket message arrives → Insert into local DB
 *    b. Show notification if app is in background
 *    c. Send delivery receipt back to server
 *    d. UI observes Room Flow → auto-updates
 *
 * 5. SYNC STRATEGY
 *    - On app launch: fetch messages since lastSyncTimestamp
 *    - Pagination for chat history (cursor-based, not offset)
 *    - Conflict resolution: server timestamp wins
 *
 * 6. MEDIA HANDLING
 *    - Upload media to S3/CDN → get URL → send URL in message
 *    - Thumbnail generation (client-side before upload)
 *    - Progressive download for images
 *    - Chunked upload with resume for large files
 *
 * 7. END-TO-END ENCRYPTION (E2EE)
 *    - Signal Protocol (Double Ratchet)
 *    - Key exchange on first contact
 *    - Per-message encryption keys
 *    - Keys stored in Android Keystore
 *
 * 8. PUSH NOTIFICATIONS
 *    - FCM high-priority for messages
 *    - Data message (not notification) for custom handling
 *    - Silent push to wake app for sync
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: How to build an offline-first app?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Offline-first means the app works fully without network; sync when available.
 *
 * PRINCIPLES:
 *
 * 1. LOCAL DATABASE AS SINGLE SOURCE OF TRUTH
 *    - Room database holds all data the UI needs.
 *    - UI always reads from local DB (via Flow/LiveData).
 *    - Network fetches write to DB, never directly to UI.
 *
 * 2. SYNC STRATEGY
 *    Pattern: Network → DB → UI (never Network → UI directly)
 *
 *    fun getArticles(): Flow<List<Article>> {
 *        // Always emit local first
 *        return dao.observeAll()
 *            .onStart { refreshFromNetwork() }  // update DB in background
 *    }
 *
 * 3. WRITE-AHEAD QUEUE
 *    - User creates/edits offline → store in local "pending changes" table.
 *    - When network returns, sync pending changes to server.
 *    - Use WorkManager for guaranteed execution.
 *    - Handle conflicts (last-write-wins, or merge strategy).
 *
 * 4. OPTIMISTIC UI UPDATES
 *    - Show changes immediately in local DB.
 *    - Mark as "pending sync" (show subtle indicator).
 *    - On successful sync → mark as synced.
 *    - On failure → revert or show error.
 *
 * 5. CONNECTIVITY MONITORING
 *    - ConnectivityManager + NetworkCallback for real-time status.
 *    - Trigger sync when network becomes available.
 *
 * 6. CACHE INVALIDATION
 *    - TTL (Time To Live) per data type (news = 15 min, profile = 1 hour).
 *    - ETags for conditional requests (avoid re-downloading unchanged data).
 *    - Last-sync timestamp per entity type.
 *
 * 7. TOOLS:
 *    - Room — local database
 *    - WorkManager — background sync
 *    - DataStore — preferences and small key-value data
 *    - OkHttp Cache — HTTP-level caching
 *    - NetworkBoundResource pattern — encapsulates fetch-and-cache logic
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Design LRU Cache
// ─────────────────────────────────────────────────────────────────────────────

/**
 * LRU (Least Recently Used) Cache evicts the least recently accessed item
 * when the cache reaches capacity.
 *
 * Android's LruCache uses a LinkedHashMap with access-order = true.
 * LinkedHashMap maintains a doubly-linked list of entries in access order.
 * On get() or put(), the entry moves to the tail (most recently used).
 * On eviction, the head entry (least recently used) is removed.
 *
 * Time complexity: O(1) for get, put, remove.
 * Space complexity: O(capacity).
 */

class LruCache<K, V>(private val maxSize: Int) {
    private val map = LinkedHashMap<K, V>(maxSize, 0.75f, true) // accessOrder=true
    private var currentSize = 0

    @Synchronized
    fun get(key: K): V? {
        return map[key] // access reorders to most-recent
    }

    @Synchronized
    fun put(key: K, value: V): V? {
        val previous = map.put(key, value)
        currentSize += sizeOf(value)
        if (previous != null) currentSize -= sizeOf(previous)
        trimToSize()
        return previous
    }

    private fun trimToSize() {
        while (currentSize > maxSize && map.isNotEmpty()) {
            val eldest = map.entries.iterator().next() // head = LRU
            map.remove(eldest.key)
            currentSize -= sizeOf(eldest.value)
            onEntryRemoved(eldest.key, eldest.value)
        }
    }

    @Synchronized
    fun remove(key: K): V? {
        val removed = map.remove(key)
        if (removed != null) currentSize -= sizeOf(removed)
        return removed
    }

    open fun sizeOf(value: V): Int = 1
    open fun onEntryRemoved(key: K, value: V) {}
}

// Android Bitmap LRU Cache
class BitmapLruCache(maxSizeBytes: Int) : android.util.LruCache<String, Bitmap>(maxSizeBytes) {
    override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: Design Analytics Library
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Requirements: Track events, batch them, send to server reliably.
 *
 * ARCHITECTURE:
 *
 *   Track Event → Enrich → Store Locally → Batch Upload → Server
 *
 * COMPONENTS:
 *
 * 1. PUBLIC API
 *    Analytics.track("button_click", mapOf("screen" to "home", "id" to "cta"))
 *    Analytics.screenView("HomeScreen")
 *    Analytics.identify(userId, traits)
 *
 * 2. EVENT ENRICHMENT
 *    Automatically add: timestamp, session ID, device info (model, OS, screen),
 *    app version, locale, network type, user ID.
 *
 * 3. LOCAL STORAGE (Room / SQLite)
 *    - Events table: id, name, properties (JSON), timestamp, uploaded (boolean)
 *    - Acts as write-ahead log — events never lost even if app crashes.
 *    - Max local storage limit (e.g., 10,000 events) — drop oldest on overflow.
 *
 * 4. BATCH UPLOADER
 *    - WorkManager periodic task (e.g., every 15 minutes).
 *    - Also triggered when batch reaches threshold (e.g., 50 events).
 *    - Constraints: requiresNetwork, battery not low.
 *    - Gzip compress the batch payload.
 *    - On success: delete uploaded events from local DB.
 *    - On failure: exponential backoff retry.
 *
 * 5. SESSION MANAGEMENT
 *    - New session after 30 minutes of inactivity.
 *    - Session ID links events for funnel analysis.
 *    - Track session_start / session_end events.
 *
 * 6. PRIVACY & CONSENT
 *    - Opt-in/opt-out toggle.
 *    - Data anonymization mode.
 *    - GDPR: delete user's events on request.
 *
 * 7. PLUGGABLE BACKENDS
 *    - Strategy pattern for different destinations (Firebase, Mixpanel, custom).
 *    - Fan-out: send same event to multiple backends.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: HTTP Request vs HTTP Long-Polling vs WebSocket vs Server-Sent Events
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ┌─────────────────┬──────────────────┬──────────────────┬──────────────────┐
 * │                 │ Direction        │ Connection       │ Use Case         │
 * ├─────────────────┼──────────────────┼──────────────────┼──────────────────┤
 * │ HTTP Request    │ Client → Server  │ Short-lived,     │ REST APIs, CRUD  │
 * │ (Polling)       │ (request/response│ new connection   │ Fetch data on    │
 * │                 │  per call)       │ per request      │ demand or timer  │
 * ├─────────────────┼──────────────────┼──────────────────┼──────────────────┤
 * │ Long-Polling    │ Client → Server  │ Held open until  │ Chat (fallback), │
 * │                 │ (server holds    │ server has data   │ notifications   │
 * │                 │  until event)    │ or timeout        │ when WS unavail │
 * ├─────────────────┼──────────────────┼──────────────────┼──────────────────┤
 * │ WebSocket       │ Bidirectional    │ Persistent,      │ Chat, gaming,    │
 * │                 │ (full-duplex)    │ single TCP       │ real-time collab,│
 * │                 │                  │ connection       │ live updates     │
 * ├─────────────────┼──────────────────┼──────────────────┼──────────────────┤
 * │ Server-Sent     │ Server → Client  │ Persistent,      │ Live feeds,      │
 * │ Events (SSE)    │ (one-way)        │ HTTP/1.1 or      │ stock tickers,   │
 * │                 │                  │ HTTP/2           │ notifications    │
 * └─────────────────┴──────────────────┴──────────────────┴──────────────────┘
 *
 * DETAILED COMPARISON:
 *
 * HTTP Polling: Client sends requests at intervals (e.g., every 5s).
 *   Pros: Simple, works everywhere, stateless.
 *   Cons: High latency, wasted bandwidth on empty responses, server load.
 *
 * Long-Polling: Client sends request, server holds until data arrives or
 *   timeout (30-60s). On response, client immediately sends new request.
 *   Pros: Near real-time, simpler than WebSocket.
 *   Cons: Head-of-line blocking, overhead of reconnecting, server holds threads.
 *
 * WebSocket: Persistent full-duplex connection. Starts as HTTP upgrade,
 *   then switches to WS protocol.
 *   Pros: True real-time, low latency, bidirectional, low overhead per message.
 *   Cons: Stateful (harder to scale), proxy issues, needs reconnection logic.
 *
 * SSE: Server pushes events over a single HTTP connection using text/event-stream.
 *   Pros: Auto-reconnect, simpler than WS, works with HTTP/2 multiplexing.
 *   Cons: Unidirectional (server→client only), text-only, limited browser connections.
 *
 * For Android:
 *   - Chat app → WebSocket (OkHttp has WebSocket support)
 *   - News feed updates → SSE or polling
 *   - REST API calls → standard HTTP
 *   - Push notifications → FCM (not HTTP-based, uses persistent connection)
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: How do Voice And Video Call Work?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Voice/video calls use WebRTC (Web Real-Time Communication) for peer-to-peer
 * media streaming, with a signaling server for connection setup.
 *
 * CALL SETUP FLOW:
 *
 * 1. SIGNALING (via WebSocket or FCM)
 *    - Caller creates an SDP Offer (Session Description Protocol) describing
 *      media capabilities (codecs, resolution, etc.)
 *    - Offer sent to signaling server → forwarded to callee
 *    - Callee creates SDP Answer → sent back to caller
 *    - Both exchange ICE Candidates (network path options)
 *
 * 2. ICE (Interactive Connectivity Establishment)
 *    - Discovers possible network paths between peers.
 *    - STUN server: discovers public IP and port (NAT traversal).
 *    - TURN server: relay server for when direct connection fails
 *      (~10-15% of calls need TURN, behind symmetric NAT).
 *    - Tries paths: local → STUN (server reflexive) → TURN (relay).
 *
 * 3. MEDIA STREAMING (P2P after connection established)
 *    - Audio: Opus codec (adaptive bitrate, 6-510 kbps)
 *    - Video: VP8/VP9/H.264 codec
 *    - RTP (Real-time Transport Protocol) for media packets
 *    - RTCP for quality feedback and bitrate adaptation
 *    - SRTP for encryption
 *
 * 4. QUALITY ADAPTATION
 *    - Bandwidth estimation → adjust resolution/framerate/bitrate
 *    - Jitter buffer to smooth packet delivery variance
 *    - FEC (Forward Error Correction) for packet loss recovery
 *    - Simulcast: send multiple quality layers, receiver picks best
 *
 * 5. ANDROID IMPLEMENTATION
 *    - Google's WebRTC library (libwebrtc)
 *    - Camera2 API / CameraX for video capture
 *    - AudioManager for audio routing (speaker, earpiece, Bluetooth)
 *    - Foreground Service with notification for ongoing call
 *    - ConnectionService + Telecom framework for system integration
 *    - CallStyle notification (Android 12+)
 *
 * For GROUP CALLS: Use an SFU (Selective Forwarding Unit) server instead
 * of P2P. Each participant sends one stream to SFU, SFU forwards to all.
 * Much more efficient than mesh (N*N connections).
 */


// ────────────────────────────────────────────────────────────────────────────
// SECTION 15: ANDROID UNIT TESTING
// ⛔ STUB — Full content removed (duplicates 11_testing.kt)
// 📖 Primary reference: 11_testing.kt
// ────────────────────────────────────────────────────────────────────────────

/**
 * This section duplicated content from 11_testing.kt.
 * Read that file for interview tips (💡), gotchas (⚠️), and follow-ups (❓).
 *
 * Quick topics covered there:
 *   - ViewModel unit testing with coroutines
 *   - Mockito vs MockK
 *   - Testing LiveData/Flow
 *
 * Use this file (17) for UNIQUE sections only — see header navigation table.
 */

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SECTION 16 — ANDROID TOOLS AND TECHNOLOGIES
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// ─────────────────────────────────────────────────────────────────────────────
// Q: CI/CD Pipeline for Android
// ─────────────────────────────────────────────────────────────────────────────

/**
 * CI (Continuous Integration) — automatically build and test on every commit.
 * CD (Continuous Delivery/Deployment) — automatically deliver builds to testers/store.
 *
 * TYPICAL ANDROID CI/CD PIPELINE:
 *
 * 1. CODE PUSH → Trigger pipeline (GitHub Actions, GitLab CI, Jenkins, Bitrise)
 *
 * 2. BUILD STAGE
 *    - Checkout code
 *    - Cache Gradle dependencies (~/.gradle/caches)
 *    - Run ./gradlew assembleDebug (or assembleRelease)
 *    - Store APK/AAB as artifact
 *
 * 3. STATIC ANALYSIS
 *    - Lint: ./gradlew lint
 *    - Detekt / ktlint for Kotlin code style
 *    - Dependency vulnerability scan (Dependabot, Snyk)
 *
 * 4. UNIT TESTS
 *    - ./gradlew testDebugUnitTest
 *    - Generate JaCoCo coverage report
 *    - Fail pipeline if coverage drops below threshold
 *
 * 5. INSTRUMENTED TESTS
 *    - Firebase Test Lab or emulator in CI
 *    - ./gradlew connectedDebugAndroidTest
 *    - Screenshot tests (Paparazzi for JVM, Shot for device)
 *
 * 6. RELEASE BUILD
 *    - Sign APK/AAB with release keystore (stored as CI secret)
 *    - ProGuard/R8 shrinking and obfuscation
 *    - Build AAB for Play Store
 *
 * 7. DISTRIBUTION
 *    - Internal testing: Firebase App Distribution / Play Internal Testing
 *    - Staging: Play Closed Testing track
 *    - Production: Play Production track (with staged rollout %)
 *
 * 8. POST-DEPLOY
 *    - Monitor crash-free rate (Firebase Crashlytics)
 *    - If crash rate spikes → auto-halt rollout
 *    - Release notes generation from commit messages
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is ADB?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ADB (Android Debug Bridge) is a command-line tool for communicating
 * with an Android device or emulator. It consists of three components:
 *
 * 1. CLIENT — runs on your development machine (adb command).
 * 2. DAEMON (adbd) — runs on the device, executes commands.
 * 3. SERVER — runs on dev machine, manages client-daemon communication.
 *
 * COMMON COMMANDS:
 *
 *   adb devices                          — List connected devices
 *   adb install app.apk                  — Install APK
 *   adb uninstall com.example.app        — Uninstall app
 *   adb logcat                           — View device logs
 *   adb logcat -s "TAG"                  — Filter logs by tag
 *   adb shell                            — Open device shell
 *   adb shell am start -n com.pkg/.Act   — Start an Activity
 *   adb shell pm list packages           — List installed packages
 *   adb shell dumpsys activity           — Dump Activity stack
 *   adb shell dumpsys meminfo <pkg>      — Memory usage for app
 *   adb push local remote                — Copy file to device
 *   adb pull remote local                — Copy file from device
 *   adb forward tcp:8080 tcp:8080        — Port forwarding
 *   adb reverse tcp:3000 tcp:3000        — Reverse port forwarding
 *   adb shell input text "hello"         — Simulate text input
 *   adb shell screencap /sdcard/s.png    — Take screenshot
 *   adb shell setprop debug.layout true  — Enable layout bounds
 *
 * Wireless ADB (Android 11+):
 *   adb pair <ip>:<port>    — Pair with device
 *   adb connect <ip>:<port> — Connect wirelessly
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is StrictMode?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * StrictMode is a developer tool that detects accidental disk/network
 * operations on the main thread and other performance anti-patterns.
 *
 * Two policies:
 *
 * 1. THREAD POLICY — Detects operations on the main thread that shouldn't be there.
 *    - Disk reads/writes
 *    - Network operations
 *    - Custom slow calls
 *
 * 2. VM POLICY — Detects app-wide anti-patterns.
 *    - Leaked SQLite Cursor objects
 *    - Leaked Closeable objects
 *    - Activity leaks (instances not GC'd)
 *    - Cleartext network traffic
 *    - Untagged sockets
 *
 * Penalty options: Log, Dialog, Crash, DropBox, FlashScreen
 *
 * ⚠️ Enable only in DEBUG builds. Never ship with StrictMode penalties.
 */

fun enableStrictMode() {
    if (BuildConfig.DEBUG) {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .penaltyDeath() // crash on violation — forces you to fix it
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .detectLeakedRegistrationObjects()
                .penaltyLog()
                .build()
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is Lint?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Android Lint is a static analysis tool that scans code for potential bugs,
 * performance issues, security vulnerabilities, usability problems, and
 * accessibility issues WITHOUT running the code.
 *
 * Categories of checks:
 *   • Correctness — incorrect API usage, missing permissions
 *   • Performance — overdraw, unnecessary allocations, HashMap → ArrayMap
 *   • Security — hardcoded passwords, cleartext traffic, SQL injection
 *   • Usability — missing translations, wrong icon sizes
 *   • Accessibility — missing contentDescription on ImageViews
 *   • Interoperability — Java/Kotlin interop issues
 *
 * Run: ./gradlew lint → generates HTML report in build/reports/lint-results.html
 *
 * Configuration:
 *   - lint.xml — suppress or change severity of specific issues.
 *   - @SuppressLint("IssueId") — suppress in code.
 *   - tools:ignore="IssueId" — suppress in XML.
 *   - lintOptions { abortOnError true } — fail build on errors.
 *
 * Custom Lint rules: Create a lint module, extend Detector, register with
 * IssueRegistry. Use for enforcing project-specific conventions.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is ProGuard used for? ProGuard vs R8
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ProGuard (and its successor R8) perform three key optimizations on your APK:
 *
 * 1. CODE SHRINKING (Tree Shaking) — Removes unused classes, methods, fields.
 *    Example: If you use only 10% of a library, the other 90% is removed.
 *
 * 2. OBFUSCATION — Renames classes/methods to short names (a, b, c).
 *    Makes reverse engineering harder. com.example.UserRepository → a.b.c
 *
 * 3. OPTIMIZATION — Inlines methods, removes dead code branches,
 *    merges classes, simplifies control flow.
 *
 * 4. RESOURCE SHRINKING — (R8 + Gradle) Removes unused resources (images,
 *    layouts, strings) not referenced by kept code.
 *
 * ProGuard vs R8:
 *
 * ┌────────────────┬─────────────────────┬──────────────────────────┐
 * │                │ ProGuard            │ R8                       │
 * ├────────────────┼─────────────────────┼──────────────────────────┤
 * │ Maintainer     │ GuardSquare         │ Google (part of AGP)     │
 * │ Speed          │ Slower              │ Faster (single pass)     │
 * │ Integration    │ Separate tool       │ Built into Gradle plugin │
 * │ Compatibility  │ Uses its own rules  │ Supports ProGuard rules  │
 * │ Desugaring     │ Separate step       │ Integrated with D8       │
 * │ Kotlin support │ Basic               │ Better Kotlin awareness  │
 * │ Default since  │ Legacy              │ AGP 3.4+ (default)       │
 * └────────────────┴─────────────────────┴──────────────────────────┘
 *
 * R8 is the default and recommended. It uses ProGuard rule format
 * but is faster and produces smaller output.
 *
 * Key rules:
 *   -keep class com.example.Model { *; }       — Don't shrink/obfuscate
 *   -keepclassmembers                           — Keep members only
 *   -dontwarn                                   — Suppress warnings
 *   @Keep annotation                            — Shorthand for -keep
 *
 * Enable in build.gradle:
 *   buildTypes {
 *       release {
 *           minifyEnabled true
 *           shrinkResources true
 *           proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'),
 *                         'proguard-rules.pro'
 *       }
 *   }
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: implementation vs api in Gradle
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Both are dependency configurations in Gradle, but they differ in visibility:
 *
 * implementation — The dependency is available ONLY to the declaring module.
 *   Consumers of this module CANNOT see the dependency.
 *   → Leaks nothing. Faster builds (changes don't trigger recompilation of consumers).
 *
 * api — The dependency is available to the declaring module AND all its consumers.
 *   → Transitive exposure. Changes trigger recompilation of all dependent modules.
 *
 * Example:
 *   Module :core:network uses Retrofit internally.
 *   Module :feature:home depends on :core:network.
 *
 *   // In :core:network build.gradle
 *   implementation "com.squareup.retrofit2:retrofit:2.9.0"
 *   // → :feature:home CANNOT use Retrofit classes directly. ✓ Encapsulated.
 *
 *   api "com.squareup.retrofit2:retrofit:2.9.0"
 *   // → :feature:home CAN use Retrofit classes. Leaks implementation detail.
 *
 * RULE: Use implementation by default. Use api only when your module's
 * public API exposes types from the dependency (e.g., a function returns
 * a Retrofit Response<T>).
 *
 * Other configurations:
 *   compileOnly  — Available at compile time only, not in APK (like Java's provided).
 *   runtimeOnly  — Available at runtime only, not at compile time.
 *   testImplementation — Available only for test source set.
 *   androidTestImplementation — Available only for instrumented test source set.
 *   kapt / ksp   — Annotation processor dependencies.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is Gradle? Build Variants in Android
// ─────────────────────────────────────────────────────────────────────────────

/**
 * GRADLE is an open-source build automation tool that Android uses to
 * compile code, manage dependencies, run tests, and produce APK/AAB files.
 * Android uses the Android Gradle Plugin (AGP) which adds Android-specific
 * build tasks (dexing, resource merging, signing, etc.).
 *
 * Key files:
 *   settings.gradle.kts  — defines modules in the project
 *   build.gradle.kts     — root project config (plugin versions, repositories)
 *   app/build.gradle.kts — app module config (dependencies, SDK versions, etc.)
 *   gradle.properties    — JVM args, project-wide flags
 *
 * BUILD VARIANTS = Build Type × Product Flavor
 *
 * BUILD TYPES — How the app is built:
 *   debug   — debuggable, no shrinking, debug signing
 *   release — minified, shrunk, signed with release key
 *   staging — custom type, e.g., debuggable release with staging server
 *
 * PRODUCT FLAVORS — What version of the app:
 *   Dimension "environment": dev, staging, production
 *   Dimension "tier": free, premium
 *
 * Result: devFreeDebug, devPremiumDebug, prodFreeRelease, etc.
 *
 * Each variant can have:
 *   - Different applicationId (install side-by-side)
 *   - Different source sets (src/dev/, src/premium/)
 *   - Different resources, manifest entries
 *   - Different BuildConfig fields
 */

// build.gradle.kts example:
// android {
//     buildTypes {
//         release {
//             isMinifyEnabled = true
//             proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//         }
//         create("staging") {
//             initWith(getByName("release"))
//             isDebuggable = true
//             applicationIdSuffix = ".staging"
//         }
//     }
//     flavorDimensions += "tier"
//     productFlavors {
//         create("free") { dimension = "tier"; applicationIdSuffix = ".free" }
//         create("premium") { dimension = "tier"; applicationIdSuffix = ".premium" }
//     }
// }


// ─────────────────────────────────────────────────────────────────────────────
// Q: APK Size Reduction techniques
// ─────────────────────────────────────────────────────────────────────────────

/**
 * TECHNIQUES TO REDUCE APK SIZE:
 *
 * 1. ENABLE R8/PROGUARD
 *    minifyEnabled = true   → removes unused code (tree shaking)
 *    shrinkResources = true → removes unused resources
 *    Typical savings: 30-50% of code size.
 *
 * 2. USE ANDROID APP BUNDLE (AAB)
 *    Google Play generates optimized APKs per device configuration.
 *    Users download only the code, resources, and native libs for their device.
 *    Savings: 15-30% smaller downloads compared to universal APK.
 *
 * 3. OPTIMIZE IMAGES
 *    - Use WebP instead of PNG/JPEG (30% smaller at same quality).
 *    - Use Vector Drawables instead of PNGs for icons/illustrations.
 *    - Remove unused density variants (keep only xxhdpi + vector).
 *    - Compress PNGs with pngcrush/zopfli.
 *
 * 4. NATIVE LIBRARIES
 *    - Use ABI splits or AAB to ship only needed architectures.
 *    - ndk { abiFilters "arm64-v8a", "armeabi-v7a" } — drop x86 if not needed.
 *
 * 5. REMOVE UNUSED DEPENDENCIES
 *    - Analyze with: ./gradlew dependencies
 *    - Replace large libraries with smaller alternatives.
 *    - Exclude transitive deps you don't use.
 *
 * 6. SHRINK DEBUG INFO
 *    - debuggable = false for release.
 *    - Strip debug symbols from native libs.
 *
 * 7. USE DYNAMIC FEATURE MODULES
 *    - Deliver features on-demand (e.g., QR scanner, AR features).
 *    - Users download them only when needed.
 *
 * 8. ANALYZE WITH APK ANALYZER
 *    Android Studio → Build → Analyze APK
 *    Shows size breakdown by: DEX, resources, assets, native libs.
 *    Compare two APKs to see what changed.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: How can you speed up the Gradle build?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * GRADLE BUILD SPEED OPTIMIZATIONS:
 *
 * 1. GRADLE DAEMON — Enabled by default. Keeps JVM warm between builds.
 *    org.gradle.daemon=true (default)
 *
 * 2. PARALLEL EXECUTION — Build independent modules simultaneously.
 *    org.gradle.parallel=true
 *
 * 3. CONFIGURATION CACHE — Caches the result of the configuration phase.
 *    org.gradle.configuration-cache=true
 *
 * 4. BUILD CACHE — Reuses outputs from previous builds (even across machines).
 *    org.gradle.caching=true
 *
 * 5. INCREASE HEAP SIZE
 *    org.gradle.jvmargs=-Xmx4g -XX:+UseParallelGC
 *
 * 6. USE IMPLEMENTATION (not api) — Reduces recompilation cascade.
 *
 * 7. AVOID DYNAMIC VERSIONS — Don't use "1.+" or "latest.release".
 *    Forces Gradle to check for updates on every build.
 *
 * 8. MODULARIZE — Multi-module builds compile only changed modules.
 *    Unchanged modules use cached outputs.
 *
 * 9. USE KSP OVER KAPT — KSP is 2x faster than KAPT for annotation processing.
 *    Dagger/Hilt, Room, Moshi all support KSP.
 *
 * 10. NON-TRANSITIVE R CLASSES
 *     android.nonTransitiveRClass=true
 *     Each module only sees its own R class, not all transitive R classes.
 *
 * 11. PROFILE YOUR BUILD
 *     ./gradlew assembleDebug --scan → generates build scan at scans.gradle.com
 *     ./gradlew assembleDebug --profile → local HTML report
 *     Identifies slowest tasks and configuration bottlenecks.
 */


// ────────────────────────────────────────────────────────────────────────────
// SECTION 17: JETPACK COMPOSE
// ⛔ STUB — Full content removed (duplicates 03_jetpack_compose.kt)
// 📖 Primary reference: 03_jetpack_compose.kt
// ────────────────────────────────────────────────────────────────────────────

/**
 * This section duplicated content from 03_jetpack_compose.kt.
 * Read that file for interview tips (💡), gotchas (⚠️), and follow-ups (❓).
 *
 * Quick topics covered there:
 *   - Compose vs View system
 *   - Recomposition mechanics
 *   - Compose state hoisting
 *
 * Use this file (17) for UNIQUE sections only — see header navigation table.
 */

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SECTION 18 — JAVA (Brief Interview Answers)
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// ─────────────────────────────────────────────────────────────────────────────
// Q: SOLID Principles
// ─────────────────────────────────────────────────────────────────────────────

/**
 * S — Single Responsibility Principle:
 *   A class should have only one reason to change. Each class handles one
 *   concern. Example: UserRepository handles data access, UserValidator handles
 *   validation — not both in one class. In Android, ViewModel handles UI state,
 *   UseCase handles business logic, Repository handles data.
 *
 * O — Open/Closed Principle:
 *   Classes should be open for extension but closed for modification. Add
 *   new behavior through inheritance or composition, not by modifying existing
 *   code. Example: Define an AnalyticsProvider interface; add new providers
 *   (Firebase, Mixpanel) without changing existing code.
 *
 * L — Liskov Substitution Principle:
 *   Subtypes must be substitutable for their base types without breaking
 *   behavior. If a function accepts Shape, passing Circle or Rectangle must
 *   work correctly. Violating LSP: Square extends Rectangle but breaks
 *   setWidth/setHeight expectations.
 *
 * I — Interface Segregation Principle:
 *   Clients should not be forced to depend on interfaces they don't use.
 *   Prefer multiple small, focused interfaces over one fat interface.
 *   Example: Instead of one Repository interface with 20 methods, split into
 *   ReadRepository and WriteRepository.
 *
 * D — Dependency Inversion Principle:
 *   High-level modules should not depend on low-level modules; both should
 *   depend on abstractions. ViewModel depends on Repository interface (abstraction),
 *   not on RoomRepository (concrete). This is the foundation of DI frameworks.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: OOP Concepts, abstract class vs interface, overloading vs overriding
// ─────────────────────────────────────────────────────────────────────────────

/**
 * FOUR PILLARS OF OOP:
 *   Encapsulation  — Hide internal state, expose through methods. (private fields + public getters)
 *   Inheritance    — Reuse code by extending a parent class. (class Dog : Animal)
 *   Polymorphism   — Same interface, different implementations. (animal.speak() → bark/meow)
 *   Abstraction    — Hide complexity, expose essential features. (interface vs implementation)
 *
 * ABSTRACT CLASS vs INTERFACE:
 *
 * ┌──────────────────┬──────────────────────┬──────────────────────────┐
 * │                  │ Abstract Class       │ Interface                │
 * ├──────────────────┼──────────────────────┼──────────────────────────┤
 * │ Constructor      │ Can have             │ Cannot have              │
 * │ State            │ Can have fields      │ No backing fields (in    │
 * │                  │                      │ Java), can in Kotlin     │
 * │ Methods          │ Abstract + concrete  │ Abstract + default       │
 * │ Inheritance      │ Single (one parent)  │ Multiple (many interfaces│
 * │ Use when         │ IS-A + shared state  │ CAN-DO (capability)      │
 * └──────────────────┴──────────────────────┴──────────────────────────┘
 *
 * METHOD OVERLOADING — Same name, different parameters (compile-time polymorphism).
 *   fun log(message: String) { ... }
 *   fun log(message: String, level: Level) { ... }
 *
 * METHOD OVERRIDING — Subclass provides specific implementation of a parent method
 *   (runtime polymorphism). Uses `override` keyword in Kotlin.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: String Pool, String immutability
// ─────────────────────────────────────────────────────────────────────────────

/**
 * STRING POOL (String Intern Pool):
 *   The JVM maintains a special memory area where String literals are stored.
 *   When you create "hello", JVM checks the pool first — if it exists, returns
 *   the existing reference. This saves memory when the same string appears often.
 *
 *   val a = "hello"        // stored in pool
 *   val b = "hello"        // reuses same reference from pool
 *   println(a === b)       // true (same reference)
 *
 *   val c = String("hello".toCharArray()) // new object, NOT in pool
 *   println(a === c)       // false (different reference)
 *   println(a == c)        // true (same content)
 *
 * STRING IMMUTABILITY:
 *   Strings cannot be modified after creation. Any "modification" creates
 *   a new String object. This ensures:
 *   1. Thread safety — no synchronization needed for shared strings.
 *   2. Hashcode caching — computed once, reused (efficient HashMap keys).
 *   3. Security — connection strings, class names can't be tampered with.
 *   4. String Pool works — shared references are safe because strings don't change.
 *
 *   For mutable string operations, use StringBuilder (not thread-safe, faster)
 *   or StringBuffer (thread-safe, slower).
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Access modifiers (private, default, protected, public)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * JAVA ACCESS MODIFIERS:
 *
 * ┌──────────────┬───────┬─────────┬────────────┬───────────┐
 * │              │ Class │ Package │ Subclass   │ World     │
 * ├──────────────┼───────┼─────────┼────────────┼───────────┤
 * │ private      │  ✓    │  ✗      │  ✗         │  ✗        │
 * │ default      │  ✓    │  ✓      │  ✗         │  ✗        │
 * │ protected    │  ✓    │  ✓      │  ✓         │  ✗        │
 * │ public       │  ✓    │  ✓      │  ✓         │  ✓        │
 * └──────────────┴───────┴─────────┴────────────┴───────────┘
 *
 * KOTLIN ACCESS MODIFIERS:
 *
 * private     — Visible within the file (top-level) or class.
 * internal    — Visible within the same MODULE (Gradle module).
 * protected   — Visible in the class and subclasses (not package).
 * public      — Default in Kotlin, visible everywhere.
 *
 * Key Kotlin difference: No "package-private" (Java's default).
 * Kotlin adds "internal" which is module-scoped — much more useful
 * for multi-module Android projects.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Generics in Java
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Generics provide type safety at compile time while allowing code reuse
 * across different types. Introduced in Java 5 via type erasure.
 *
 * TYPE ERASURE: Generic type information is removed at runtime.
 *   List<String> and List<Integer> are both just List at runtime.
 *   This is why you can't do `if (obj is List<String>)` in Java/Kotlin.
 *
 * VARIANCE (Kotlin perspective, applies to Java's ? extends / ? super):
 *
 *   COVARIANCE (out / ? extends) — Producer. Can read T, not write T.
 *     List<out Animal> = can assign List<Dog> to it.
 *     Java: List<? extends Animal>
 *
 *   CONTRAVARIANCE (in / ? super) — Consumer. Can write T, not read T.
 *     Comparator<in Animal> = can assign Comparator<Any> to it.
 *     Java: Comparator<? super Animal>
 *
 *   INVARIANT — Neither in nor out. MutableList<T> — exact type only.
 *
 * MNEMONIC: PECS — Producer Extends, Consumer Super.
 *
 * Reified generics (Kotlin only): inline functions can access generic type
 * at runtime using `reified` keyword. Useful for type checks and reflection.
 *   inline fun <reified T> isType(obj: Any) = obj is T
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is garbage collector? How does it work?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The Garbage Collector (GC) automatically frees memory by reclaiming objects
 * that are no longer reachable from any GC root.
 *
 * GC ROOTS: Stack variables, static fields, JNI references, thread objects.
 *
 * HOW IT WORKS (Generational Collection):
 *
 * 1. YOUNG GENERATION — Newly created objects.
 *    - Eden space: all new objects start here.
 *    - Survivor spaces (S0, S1): objects that survived a minor GC.
 *    - Minor GC: fast, collects Eden + one Survivor, copies survivors to other.
 *
 * 2. OLD GENERATION (Tenured) — Objects that survived many minor GCs.
 *    - Major GC: slower, collects old generation.
 *    - Triggered when old gen fills up.
 *
 * ANDROID-SPECIFIC (ART Runtime):
 *   - Concurrent Copying GC (default on ART) — minimal pause times.
 *   - Generational approach: young gen collected more frequently.
 *   - Compacting GC reduces fragmentation.
 *   - GC pauses are typically < 2ms on modern devices.
 *
 * MEMORY LEAK: When objects that should be GC'd are still referenced
 * (e.g., Activity referenced by a static field). The GC can't collect them.
 * Use LeakCanary to detect leaks in Android apps.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: synchronized keyword, volatile modifier, ThreadPoolExecutor
// ─────────────────────────────────────────────────────────────────────────────

/**
 * SYNCHRONIZED — Ensures only one thread can execute a block/method at a time.
 *   Provides both mutual exclusion and memory visibility.
 *   @Synchronized fun increment() { count++ } // Kotlin
 *   synchronized(lock) { sharedList.add(item) }
 *
 * VOLATILE — Ensures visibility of changes across threads. A write to a
 *   volatile variable is immediately visible to all other threads.
 *   Does NOT provide atomicity — count++ on volatile is still not thread-safe.
 *   Use for flags: @Volatile var isRunning = true
 *
 * THREAD POOL EXECUTOR — Manages a pool of worker threads.
 *   Avoids thread creation overhead. Configurable:
 *     - corePoolSize: minimum threads kept alive
 *     - maxPoolSize: maximum threads
 *     - keepAliveTime: idle thread lifetime
 *     - workQueue: BlockingQueue for pending tasks
 *
 *   Android's AsyncTask used a ThreadPoolExecutor internally.
 *   Kotlin coroutines use thread pools via Dispatchers (IO = 64 threads max,
 *   Default = CPU core count).
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Checked vs Unchecked Exception
// ─────────────────────────────────────────────────────────────────────────────

/**
 * CHECKED EXCEPTIONS (Java only — Kotlin has no checked exceptions):
 *   Must be caught or declared in method signature (throws clause).
 *   Extends Exception (but not RuntimeException).
 *   Examples: IOException, SQLException, FileNotFoundException.
 *   Purpose: recoverable errors that the caller should handle.
 *
 * UNCHECKED EXCEPTIONS:
 *   Don't need to be caught or declared.
 *   Extends RuntimeException.
 *   Examples: NullPointerException, IllegalArgumentException,
 *             IndexOutOfBoundsException, ClassCastException.
 *   Purpose: programming errors (bugs).
 *
 * KOTLIN APPROACH: All exceptions are unchecked. No throws clause.
 *   Use @Throws annotation for Java interop only.
 *   Kotlin philosophy: checked exceptions add boilerplate without improving safety.
 *
 * ERROR vs EXCEPTION:
 *   Error — JVM-level problems, not recoverable (OutOfMemoryError, StackOverflowError).
 *   Exception — Application-level problems, potentially recoverable.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: final, finally, finalize
// ─────────────────────────────────────────────────────────────────────────────

/**
 * FINAL (Kotlin: val, no open):
 *   - final class — cannot be extended (all Kotlin classes are final by default).
 *   - final method — cannot be overridden.
 *   - final variable — cannot be reassigned (val in Kotlin).
 *
 * FINALLY:
 *   Block that executes after try/catch regardless of outcome.
 *   Used for cleanup: closing streams, releasing resources.
 *   try { read() } catch (e: IOException) { handle() } finally { close() }
 *   Modern alternative: use() extension (auto-closes resources).
 *
 * FINALIZE (DEPRECATED since Java 9):
 *   Method called by GC before collecting an object. Historically used for
 *   cleanup, but unreliable — no guarantee when (or if) it runs.
 *   Problems: delays GC, unpredictable timing, performance overhead.
 *   Alternative: Cleaner class (Java 9+) or explicit close()/release().
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: static keyword in Java
// ─────────────────────────────────────────────────────────────────────────────

/**
 * STATIC in Java means the member belongs to the CLASS, not to instances.
 *
 *   static field  — Shared across all instances. One copy in memory.
 *   static method — Called on the class, not on an instance. No access to `this`.
 *   static block  — Runs once when the class is loaded. Used for initialization.
 *   static class  — Nested class that doesn't hold a reference to the outer class.
 *
 * KOTLIN EQUIVALENTS:
 *   static field/method  → companion object { } or top-level functions
 *   static constant      → companion object { const val X = 1 }
 *   static nested class  → class Nested (default, no inner keyword)
 *   static initializer   → init { } in companion object
 *
 * ⚠️ Android gotcha: Static fields holding Activity/Context → memory leaks.
 *    The static field lives for the entire process lifetime.
 *
 * @JvmStatic annotation makes companion object methods/properties appear
 * as actual Java static members for interop.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Serialization and Deserialization, Parcelable vs Serializable
// ─────────────────────────────────────────────────────────────────────────────

/**
 * SERIALIZATION: Converting an object into a byte stream for storage/transfer.
 * DESERIALIZATION: Reconstructing an object from a byte stream.
 *
 * SERIALIZABLE (Java):
 *   - Marker interface (no methods to implement).
 *   - Uses reflection → slow.
 *   - Creates many temporary objects → GC pressure.
 *   - Simple to implement: just `implements Serializable`.
 *
 * PARCELABLE (Android):
 *   - Android-specific interface optimized for IPC (Inter-Process Communication).
 *   - No reflection — manual or generated marshalling/unmarshalling.
 *   - ~10x faster than Serializable on Android.
 *   - Used for: Intent extras, Bundle data, Activity/Fragment arguments.
 *   - With @Parcelize (kotlin-parcelize plugin): auto-generated, zero boilerplate.
 *
 * WHEN TO USE:
 *   Parcelable → passing data between Android components (Intent, Bundle, AIDL).
 *   Serializable → saving to disk, network serialization (but prefer Gson/Moshi/Protobuf).
 *
 * Kotlin Serialization (kotlinx.serialization):
 *   Modern alternative for JSON/Protobuf/CBOR. Compile-time, no reflection.
 *   @Serializable data class User(val name: String, val age: Int)
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: HashMap vs ArrayMap vs SparseArray
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ┌─────────────────┬─────────────────┬─────────────────┬─────────────────┐
 * │                 │ HashMap         │ ArrayMap         │ SparseArray     │
 * ├─────────────────┼─────────────────┼─────────────────┼─────────────────┤
 * │ Backing         │ Array of linked │ Two sorted       │ Two parallel    │
 * │ structure       │ lists/trees     │ arrays (hashes + │ arrays (int keys│
 * │                 │ (hash table)    │ key-value pairs) │ + Object values)│
 * ├─────────────────┼─────────────────┼─────────────────┼─────────────────┤
 * │ Key type        │ Any Object      │ Any Object       │ int only        │
 * ├─────────────────┼─────────────────┼─────────────────┼─────────────────┤
 * │ Lookup time     │ O(1) amortized  │ O(log n) binary  │ O(log n) binary │
 * │                 │                 │ search           │ search          │
 * ├─────────────────┼─────────────────┼─────────────────┼─────────────────┤
 * │ Memory          │ High (entry     │ Lower (no Entry  │ Lowest (no      │
 * │                 │ objects, load   │ objects, compact)│ autoboxing of   │
 * │                 │ factor padding) │                  │ int keys)       │
 * ├─────────────────┼─────────────────┼─────────────────┼─────────────────┤
 * │ Best for        │ Large datasets  │ Small-medium maps│ Small maps with │
 * │                 │ (>1000 entries) │ (<1000 entries)  │ int keys        │
 * └─────────────────┴─────────────────┴─────────────────┴─────────────────┘
 *
 * Android recommendation:
 *   - ArrayMap for small maps (Bundles, attribute sets, JSON objects).
 *   - SparseArray when keys are ints (view IDs, positions).
 *     Also: SparseBooleanArray, SparseIntArray, SparseLongArray.
 *   - HashMap for large datasets where O(1) lookup matters.
 *
 * ArrayMap is used internally by Android for Bundle, Intent extras,
 * and SharedPreferences.
 */


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SECTION 19 — ANDROID JETPACK
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// ─────────────────────────────────────────────────────────────────────────────
// Q: What is Android Jetpack?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Android Jetpack is a suite of libraries, tools, and guidance from Google
 * to help developers build high-quality Android apps.
 *
 * Categories:
 *
 * ARCHITECTURE:
 *   ViewModel, LiveData, Room, DataStore, WorkManager, Navigation,
 *   Paging, Lifecycle, SavedState
 *
 * UI:
 *   Jetpack Compose, Fragment, RecyclerView, ViewPager2, ConstraintLayout,
 *   Material Design Components, AppCompat
 *
 * BEHAVIOR:
 *   CameraX, Notifications, Permissions, Sharing, Slices, Media
 *
 * FOUNDATION:
 *   AppCompat, Android KTX, Multidex, Test
 *
 * Benefits:
 *   • Backwards compatible — works across Android versions.
 *   • Follows best practices — lifecycle awareness, testability.
 *   • Reduces boilerplate — e.g., Room vs raw SQLite.
 *   • Consistent APIs — all libraries work well together.
 *   • Maintained by Google — regular updates and bug fixes.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is a ViewModel and how is it useful? How does ViewModel work internally?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel is a lifecycle-aware component that stores and manages UI-related
 * data. It survives configuration changes (rotation, locale change, etc.).
 *
 * WHY USEFUL:
 *   • Survives config changes — no data loss on rotation.
 *   • Separation of concerns — UI logic out of Activity/Fragment.
 *   • Scope — lives as long as its ViewModelStoreOwner (Activity/Fragment/NavBackStackEntry).
 *   • Coroutine scope — viewModelScope auto-cancels on onCleared().
 *
 * HOW IT WORKS INTERNALLY:
 *
 * 1. ViewModelStoreOwner (Activity/Fragment) has a ViewModelStore.
 *    ViewModelStore is a HashMap<String, ViewModel>.
 *
 * 2. On config change, the Activity is destroyed and recreated, BUT:
 *    - The NonConfigurationInstances object is retained by the framework.
 *    - ViewModelStore is stored in NonConfigurationInstances.
 *    - The new Activity retrieves the same ViewModelStore.
 *
 * 3. ViewModelProvider.get(MyViewModel::class.java):
 *    - Checks ViewModelStore for existing instance → returns it.
 *    - If not found, uses ViewModelProvider.Factory to create one → stores it.
 *
 * 4. When the Activity TRULY finishes (not config change):
 *    - Activity.isFinishing == true
 *    - ViewModelStore.clear() is called
 *    - Each ViewModel's onCleared() is called
 *    - viewModelScope is cancelled
 *
 * Lifecycle:
 *   Activity created → ViewModel created (if first time)
 *   Config change → Activity destroyed → Activity recreated → same ViewModel
 *   Activity finished → ViewModel.onCleared() → GC'd
 */

class MyViewModel(
    private val repository: UserRepository,
    private val savedStateHandle: SavedStateHandle // survives process death
) : ViewModel() {

    // Survives config change
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Survives process death
    val searchQuery = savedStateHandle.getStateFlow("query", "")

    fun setSearchQuery(query: String) {
        savedStateHandle["query"] = query
    }

    init {
        viewModelScope.launch {
            repository.getUsers().collect { _users.value = it }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // cleanup if needed (viewModelScope is auto-cancelled)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: SharedViewModel in Android
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A SharedViewModel is a ViewModel scoped to an Activity (or Navigation graph)
 * that is shared between multiple Fragments. This allows Fragments to
 * communicate without direct references to each other.
 *
 * Use case: Master-detail layout, multi-step forms, filter panel.
 *
 * Key: Both Fragments request the ViewModel scoped to the SAME owner.
 *   In Fragment A: val vm by activityViewModels<SharedViewModel>()
 *   In Fragment B: val vm by activityViewModels<SharedViewModel>()
 *   Both get the SAME instance because the owner is the Activity.
 *
 * With Navigation: scope to a navigation graph.
 *   val vm by navGraphViewModels<SharedViewModel>(R.id.checkout_graph)
 */

class SharedFilterViewModel : ViewModel() {
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    fun selectCategory(category: String) { _selectedCategory.value = category }
}

// Fragment A (list) — scoped to activity
class ProductListFragment : Fragment() {
    private val filterViewModel: SharedFilterViewModel by activityViewModels()

    // observes filterViewModel.selectedCategory to filter products
}

// Fragment B (filter panel) — same ViewModel instance
class FilterFragment : Fragment() {
    private val filterViewModel: SharedFilterViewModel by activityViewModels()

    // calls filterViewModel.selectCategory("Electronics")
}


// ─────────────────────────────────────────────────────────────────────────────
// Q: StateFlow vs LiveData
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ┌─────────────────────┬──────────────────────┬──────────────────────────┐
 * │                     │ LiveData             │ StateFlow                │
 * ├─────────────────────┼──────────────────────┼──────────────────────────┤
 * │ Origin              │ Android Jetpack      │ Kotlin Coroutines        │
 * ├─────────────────────┼──────────────────────┼──────────────────────────┤
 * │ Platform            │ Android-only         │ Multiplatform (KMM)      │
 * ├─────────────────────┼──────────────────────┼──────────────────────────┤
 * │ Lifecycle-aware     │ Yes (built-in)       │ No (use repeatOnLifecycle│
 * │                     │                      │ or collectAsStateWith-   │
 * │                     │                      │ Lifecycle in Compose)    │
 * ├─────────────────────┼──────────────────────┼──────────────────────────┤
 * │ Initial value       │ Optional (nullable)  │ Required                 │
 * ├─────────────────────┼──────────────────────┼──────────────────────────┤
 * │ Null safety         │ Allows null values   │ Can be non-null if typed │
 * ├─────────────────────┼──────────────────────┼──────────────────────────┤
 * │ Operators           │ map, switchMap,      │ Full Flow operators:     │
 * │                     │ MediatorLiveData     │ map, filter, combine,    │
 * │                     │ (limited)            │ flatMapLatest, etc.      │
 * ├─────────────────────┼──────────────────────┼──────────────────────────┤
 * │ Thread safety       │ setValue (main),     │ Thread-safe (atomic)     │
 * │                     │ postValue (any)      │                          │
 * ├─────────────────────┼──────────────────────┼──────────────────────────┤
 * │ Conflation          │ No (postValue may    │ Yes (conflated by design)│
 * │                     │  drop intermediate)  │                          │
 * ├─────────────────────┼──────────────────────┼──────────────────────────┤
 * │ Testing             │ Needs InstantTask-   │ Works with runTest,      │
 * │                     │ ExecutorRule          │ Turbine                  │
 * └─────────────────────┴──────────────────────┴──────────────────────────┘
 *
 * Modern recommendation: Use StateFlow in ViewModels. Google is moving
 * towards Flow-based APIs. LiveData is still fine for existing codebases.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is LiveData? setValue vs postValue
// ─────────────────────────────────────────────────────────────────────────────

/**
 * LiveData is a lifecycle-aware observable data holder class from Android Jetpack.
 *
 * KEY PROPERTIES:
 *   • Lifecycle-aware: only notifies ACTIVE observers (STARTED or RESUMED).
 *   • No memory leaks: observers are auto-removed when lifecycle is DESTROYED.
 *   • No crashes from stopped activities: inactive observers don't receive updates.
 *   • Configuration change safe: observers re-receive the latest value on reattach.
 *   • Always has the latest data: new observer immediately gets the current value.
 *
 * setValue() vs postValue():
 *
 *   setValue(value) — MUST be called from MAIN THREAD.
 *     Sets the value and immediately dispatches to observers.
 *     Throws if called from background thread.
 *
 *   postValue(value) — Can be called from ANY THREAD.
 *     Posts a task to main thread to set the value.
 *     If called multiple times before main thread processes, only the
 *     LAST value is dispatched (intermediate values are dropped).
 *
 * ⚠️ postValue() dropping intermediate values is a common interview gotcha.
 *   If you postValue("A") then postValue("B") rapidly, observers may only see "B".
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Explain WorkManager and its use cases
// ─────────────────────────────────────────────────────────────────────────────

/**
 * WorkManager is the recommended API for deferrable, guaranteed background work.
 * It runs even if the app exits or the device restarts.
 *
 * WHEN TO USE:
 *   • Upload logs/analytics to server
 *   • Sync local data with remote
 *   • Process/compress images
 *   • Periodic data cleanup
 *   • Download content for offline use
 *
 * TYPES OF WORK:
 *   OneTimeWorkRequest  — Runs once.
 *   PeriodicWorkRequest — Runs at intervals (min 15 minutes).
 *
 * CONSTRAINTS:
 *   NetworkType (CONNECTED, UNMETERED), RequiresCharging,
 *   RequiresBatteryNotLow, RequiresStorageNotLow, RequiresDeviceIdle
 *
 * FEATURES:
 *   • Chaining: workA.then(workB).then(workC) — sequential pipeline.
 *   • Parallel: WorkManager.enqueue(listOf(workA, workB)) — run together.
 *   • Unique work: enqueueUniqueWork() — prevent duplicates.
 *   • Input/Output data: pass Data objects between chained workers.
 *   • Backoff policy: LINEAR or EXPONENTIAL retry on failure.
 *   • Expedited work: run immediately with foreground service.
 *
 * UNDER THE HOOD:
 *   Uses JobScheduler (API 23+), GcmNetworkManager, or AlarmManager + BroadcastReceiver
 *   as the backing scheduler depending on API level. Work metadata is stored in
 *   a Room database for persistence across reboots.
 */

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val repository = /* get from DI */ SyncRepository()
            repository.syncPendingChanges()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry()
            else Result.failure()
        }
    }
}

// Enqueue work
val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
    )
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
    .addTag("sync")
    .build()

// WorkManager.getInstance(context).enqueueUniqueWork("sync", ExistingWorkPolicy.KEEP, syncRequest)


// ─────────────────────────────────────────────────────────────────────────────
// Q: How Does WorkManager Guarantee Task Execution?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * WorkManager guarantees task execution through several mechanisms:
 *
 * 1. PERSISTENCE — Work requests are stored in a Room database.
 *    Even if the app is killed or the device reboots, pending work
 *    is persisted and re-scheduled on next boot.
 *
 * 2. PLATFORM DELEGATION — Delegates to the best available scheduler:
 *    - API 23+: JobScheduler (system-managed job scheduling)
 *    - API 14-22: GcmNetworkManager (if GMS available) or
 *                 AlarmManager + BroadcastReceiver (fallback)
 *    These system APIs survive app death and device reboot.
 *
 * 3. BOOT RECEIVER — WorkManager registers a BroadcastReceiver for
 *    BOOT_COMPLETED. On reboot, it re-reads the Room DB and re-schedules
 *    all pending work with the platform scheduler.
 *
 * 4. CONSTRAINT MONITORING — Continuously monitors constraints
 *    (network, battery, idle). When all constraints are met, work runs.
 *
 * 5. RETRY POLICY — On worker failure returning Result.retry(),
 *    WorkManager re-enqueues with backoff (linear or exponential).
 *
 * IMPORTANT CAVEATS:
 *   - "Guaranteed" means it WILL run eventually, not immediately.
 *   - Doze mode and App Standby can delay execution.
 *   - For immediate work, use setExpedited() (foreground service).
 *   - OEM battery optimizations may still interfere (Xiaomi, Samsung, Huawei).
 *     Direct users to dontkillmyapp.com settings.
 */


// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// SECTION 20 — OTHER TOPICS
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

// ─────────────────────────────────────────────────────────────────────────────
// Q: Serializable vs Parcelable
// ─────────────────────────────────────────────────────────────────────────────

/**
 * (Covered in Section 18 — brief recap)
 *
 * Serializable:
 *   - Java standard interface. Uses reflection. Slow on Android.
 *   - Simple: just implement interface, no methods needed.
 *   - Creates many temporary objects → GC pressure.
 *
 * Parcelable:
 *   - Android-specific. No reflection. ~10x faster.
 *   - Use @Parcelize in Kotlin for zero-boilerplate implementation.
 *   - Required for passing data in Intents, Bundles, and IPC.
 *
 *   @Parcelize
 *   data class User(val name: String, val age: Int) : Parcelable
 *
 * Rule: Always use Parcelable for Android component communication.
 *       Use kotlinx.serialization for JSON/disk/network serialization.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: Why Bundle class is used for data passing?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Bundle is a type-safe key-value container optimized for Android IPC
 * (Inter-Process Communication via Binder).
 *
 * WHY BUNDLE (not HashMap)?
 *
 * 1. PARCELABLE — Bundle implements Parcelable, so it can be sent across
 *    process boundaries (Activity ↔ Activity, even across apps).
 *
 * 2. TYPE-SAFE ACCESSORS — getInt(), getString(), getParcelable<T>(), etc.
 *    Prevents ClassCastException that HashMap<String, Any> would allow.
 *
 * 3. ANDROID FRAMEWORK INTEGRATION — Used by:
 *    - Intent extras (intent.putExtra / intent.extras)
 *    - Fragment arguments (fragment.arguments)
 *    - onSaveInstanceState / onRestoreInstanceState
 *    - DialogFragment arguments
 *    - Activity result data
 *
 * 4. INTERNALLY USES ARRAYMAP — Memory efficient for small datasets
 *    (typical for component arguments). Not HashMap.
 *
 * 5. BINDER TRANSACTION LIMIT — ~1MB per process. Bundle warns you
 *    to keep data small. Don't store large objects — store IDs and
 *    retrieve from ViewModel/database.
 *
 * ALTERNATIVE: Safe Args (Navigation Component) generates type-safe
 * classes for passing arguments between destinations.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: How to secure API keys in Android App?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ⚠️ No client-side secret is 100% secure. The goal is to make extraction
 *    as difficult as possible and minimize damage if keys are compromised.
 *
 * STRATEGIES (from least to most secure):
 *
 * 1. GRADLE/BUILDCONFIG (basic — still in APK)
 *    Store in local.properties (gitignored), read in build.gradle.
 *    buildConfigField("String", "API_KEY", "\"${localProps['apiKey']}\"")
 *    ⚠️ Key is still in the APK as a string. Can be extracted with apktool.
 *
 * 2. NDK / NATIVE CODE
 *    Store key in C/C++ .so file.
 *    Harder to extract than BuildConfig (needs binary analysis).
 *    Still not truly secure — determined attacker can disassemble native code.
 *
 * 3. OBFUSCATION
 *    ProGuard/R8 obfuscates class/method names.
 *    Encrypt the key string and decrypt at runtime.
 *    Adds a layer but doesn't prevent determined attackers.
 *
 * 4. BACKEND PROXY (RECOMMENDED)
 *    Never put the real API key on the client. Instead:
 *    Client → Your Backend (with user auth) → Third-party API (with real key).
 *    The client authenticates with your server; your server holds the API key.
 *    Benefits: rate limiting, key rotation without app update, revocation.
 *
 * 5. ANDROID KEYSTORE
 *    Use for encryption keys / signing keys (not API keys).
 *    Hardware-backed secure storage. Keys can't be extracted.
 *
 * 6. GOOGLE CLOUD SECRET MANAGER / FIREBASE REMOTE CONFIG
 *    Fetch keys at runtime (still transmitted to client, but not in APK).
 *    Use with certificate pinning and encrypted transport.
 *
 * BEST PRACTICE: Sensitive API keys should NEVER be in the APK.
 * Use a backend proxy. For less sensitive keys (Maps, Analytics),
 * use BuildConfig + NDK + restrict key scope (API restrictions on console).
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: How does the Android Push Notification system work?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Android Push Notifications use Firebase Cloud Messaging (FCM).
 *
 * ARCHITECTURE:
 *
 *   Your Server → FCM Server → Device (Google Play Services) → Your App
 *
 * FLOW:
 *
 * 1. REGISTRATION:
 *    a. App starts → FirebaseMessaging.getInstance().token → gets FCM token.
 *    b. Token is a unique device+app identifier.
 *    c. App sends token to your backend server for storage.
 *    d. Token can change — handle onNewToken() callback.
 *
 * 2. SENDING:
 *    a. Your server sends HTTP POST to FCM endpoint with:
 *       - Target: device token, topic, or condition
 *       - Payload: notification (title/body) and/or data (key-value pairs)
 *    b. FCM routes the message to the correct device.
 *
 * 3. RECEIVING:
 *    a. Google Play Services maintains a persistent connection to FCM.
 *    b. Message arrives → delivered to FirebaseMessagingService.
 *    c. Two types of messages:
 *
 *       NOTIFICATION MESSAGE — Handled by system when app is in background.
 *         Shows notification automatically. onMessageReceived() only called
 *         when app is in foreground.
 *
 *       DATA MESSAGE — Always delivered to onMessageReceived() regardless
 *         of app state. You build the notification manually. More control.
 *
 *       Recommendation: Use DATA messages for full control.
 *
 * 4. HIGH vs NORMAL PRIORITY:
 *    HIGH — Delivered immediately, can wake device from Doze. Use for messages
 *           the user should see promptly (chat messages).
 *    NORMAL — May be batched/delayed by Doze. Use for non-urgent (sync triggers).
 *
 * TOPICS: Subscribe devices to topics (e.g., "sports_news"). Send to all
 *         subscribers at once without managing individual tokens.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What is Cleartext traffic?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Cleartext traffic is unencrypted HTTP communication (not HTTPS).
 * Data travels as plain text and can be intercepted by anyone on the network
 * (man-in-the-middle attack).
 *
 * ANDROID 9 (API 28)+ DEFAULT:
 *   Cleartext traffic is BLOCKED by default. All HTTP requests must use HTTPS.
 *   Violating this causes: java.net.UnknownServiceException:
 *   "CLEARTEXT communication to example.com not permitted"
 *
 * TO ALLOW (development only):
 *
 * 1. Network Security Config (recommended):
 *    res/xml/network_security_config.xml:
 *    <network-security-config>
 *        <domain-config cleartextTrafficPermitted="true">
 *            <domain includeSubdomains="true">10.0.2.2</domain> <!-- emulator localhost -->
 *        </domain-config>
 *    </network-security-config>
 *
 *    AndroidManifest.xml:
 *    android:networkSecurityConfig="@xml/network_security_config"
 *
 * 2. Global flag (NOT recommended for production):
 *    android:usesCleartextTraffic="true" in <application> tag.
 *
 * BEST PRACTICE: Always use HTTPS in production. Use Network Security Config
 * to allow cleartext only for specific development domains.
 * Also consider Certificate Pinning for high-security apps.
 */


// ─────────────────────────────────────────────────────────────────────────────
// Q: What are Annotations? How to create custom Annotation?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Annotations are metadata attached to code elements (classes, functions,
 * properties, parameters) that can be read at compile time or runtime.
 *
 * CATEGORIES:
 *
 * 1. SOURCE-LEVEL — Read by compiler, discarded after. (@Suppress, @SuppressLint)
 * 2. CLASS-LEVEL — Stored in .class file but not available at runtime.
 * 3. RUNTIME — Available via reflection at runtime. (@Retention(RUNTIME))
 *
 * COMMON ANDROID ANNOTATIONS:
 *   @NonNull, @Nullable, @IntDef, @StringDef — null safety & type safety
 *   @WorkerThread, @MainThread, @UiThread — thread enforcement
 *   @LayoutRes, @DrawableRes, @StringRes — resource type safety
 *   @VisibleForTesting — documents that visibility is widened for tests
 *   @Keep — prevent ProGuard from removing/obfuscating
 */

// Custom annotation — compile-time validation
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresPermission(val permission: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Analytics(val screenName: String)

// Usage
@Analytics(screenName = "HomeScreen")
class HomeActivity : AppCompatActivity() {

    @RequiresPermission(permission = "android.permission.CAMERA")
    fun openCamera() { /* ... */ }
}

// Reading annotations at runtime via reflection
fun trackScreen(activity: AppCompatActivity) {
    val annotation = activity::class.java.getAnnotation(Analytics::class.java)
    annotation?.let {
        AnalyticsTracker.logScreenView(it.screenName)
    }
}

// Compile-time annotation processing (KAPT/KSP) can generate code
// based on annotations — this is how Room, Dagger, Moshi work.


// ─────────────────────────────────────────────────────────────────────────────
// Q: How to show local Notification at an exact time?
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Use AlarmManager for exact-time scheduling + BroadcastReceiver + NotificationManager.
 *
 * STEPS:
 *
 * 1. Schedule alarm with AlarmManager.setExactAndAllowWhileIdle()
 *    (works in Doze mode for exact timing).
 *
 * 2. AlarmManager fires PendingIntent → BroadcastReceiver.
 *
 * 3. BroadcastReceiver creates and shows the Notification.
 *
 * IMPORTANT ANDROID 12+ (API 31):
 *   Must declare SCHEDULE_EXACT_ALARM permission.
 *   Must check canScheduleExactAlarms() before scheduling.
 *   <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
 *
 * ANDROID 13+ (API 33):
 *   Must request POST_NOTIFICATIONS runtime permission.
 *
 * Alternative: WorkManager with initialDelay for approximate timing.
 *   WorkManager is NOT suitable for exact-time notifications because
 *   it's subject to batching and Doze delays.
 */

class NotificationScheduler(private val context: Context) {

    fun scheduleNotification(
        title: String,
        message: String,
        triggerTimeMillis: Long,
        notificationId: Int
    ) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notificationId", notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent
                )
            } else {
                // Fallback to inexact or guide user to Settings → Alarms & Reminders
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent
            )
        }
    }
}

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: return
        val message = intent.getStringExtra("message") ?: return
        val notificationId = intent.getIntExtra("notificationId", 0)

        val channelId = "scheduled_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Scheduled", NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// END OF PART 3
// ─────────────────────────────────────────────────────────────────────────────
