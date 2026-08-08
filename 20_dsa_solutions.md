# 20 — DSA — Coding Round Solutions

> Google phone screen & coding round prep for SDE-2 (4+ YOE).
> **DP problems:** Recursion → Memoization → Tabulation → Space Optimization
> **Other patterns:** Key insight → Optimal solution → TC/SC → Edge cases
> Code in **Kotlin** (new) and **Java** (DP sections).

---

## Table of Contents

### Dynamic Programming
1. [Target Sum (LC 494)](#1-target-sum-lc-494)
2. [Coin Change 2 (LC 518)](#2-coin-change-2-lc-518)
3. [Climbing Stairs (LC 70)](#3-climbing-stairs-lc-70)
4. [Longest Increasing Subsequence (LC 300)](#4-longest-increasing-subsequence-lc-300)

### Arrays & Hash Maps
5. [Two Sum (LC 1)](#5-two-sum-lc-1)
6. [Product of Array Except Self (LC 238)](#6-product-of-array-except-self-lc-238)
7. [Merge Intervals (LC 56)](#7-merge-intervals-lc-56)

### Sliding Window
8. [Longest Substring Without Repeating (LC 3)](#8-longest-substring-without-repeating-lc-3)
9. [Sliding Window Maximum (LC 239)](#9-sliding-window-maximum-lc-239)

### Trees
10. [Binary Tree Level Order Traversal (LC 102)](#10-binary-tree-level-order-traversal-lc-102)
11. [Validate Binary Search Tree (LC 98)](#11-validate-binary-search-tree-lc-98)
12. [Lowest Common Ancestor of BST (LC 236)](#12-lowest-common-ancestor-of-bst-lc-236)

### Graphs
13. [Number of Islands (LC 200)](#13-number-of-islands-lc-200)
14. [Course Schedule (LC 207)](#14-course-schedule-lc-207)

### Binary Search
15. [Binary Search (LC 704)](#15-binary-search-lc-704)
16. [Search in Rotated Sorted Array (LC 33)](#16-search-in-rotated-sorted-array-lc-33)

### Heaps
17. [Kth Largest Element (LC 215)](#17-kth-largest-element-lc-215)
18. [Top K Frequent Elements (LC 347)](#18-top-k-frequent-elements-lc-347)

### Backtracking
19. [Subsets (LC 78)](#19-subsets-lc-78)
20. [Permutations (LC 46)](#20-permutations-lc-46)
21. [Word Search (LC 79)](#21-word-search-lc-79)

### Greedy
22. [Meeting Rooms II (LC 253)](#22-meeting-rooms-ii-lc-253)

### Linked Lists
23. [Reverse Linked List (LC 206)](#23-reverse-linked-list-lc-206)
24. [Merge Two Sorted Lists (LC 21)](#24-merge-two-sorted-lists-lc-21)

### Hard — Google Favorites
25. [LRU Cache (LC 146)](#25-lru-cache-lc-146)
26. [Serialize and Deserialize Binary Tree (LC 297)](#26-serialize-and-deserialize-binary-tree-lc-297)
27. [Median of Two Sorted Arrays (LC 4)](#27-median-of-two-sorted-arrays-lc-4)

---

## 1. Target Sum (LC 494)

**Problem:** Assign `+` or `-` to each num. Count ways to reach `target`.

### Key Insight
At each index, two choices: add or subtract. Brute force = 2^n paths.

**Subset-sum conversion:**
- Let `P` = sum of positives, `N` = sum of negatives
- `P - N = target` and `P + N = totalSum`
- → `P = (totalSum + target) / 2`
- Reduces to: **count subsets with sum = S**

---

### 1. Recursion

```java
public int findTargetSumWays(int[] nums, int target) {
    return solve(nums, 0, target);
}

private int solve(int[] nums, int idx, int remaining) {
    if (idx == nums.length) return remaining == 0 ? 1 : 0;
    return solve(nums, idx + 1, remaining - nums[idx])   // assign +
         + solve(nums, idx + 1, remaining + nums[idx]);  // assign -
}
```

- **TC:** O(2^n) — binary tree of calls
- **SC:** O(n) — call stack depth

---

### 2. Memoization (Top-Down)

```java
Map<String, Integer> memo = new HashMap<>();

public int findTargetSumWays(int[] nums, int target) {
    memo.clear();
    return solve(nums, 0, target);
}

private int solve(int[] nums, int idx, int remaining) {
    String key = idx + "," + remaining;
    if (memo.containsKey(key)) return memo.get(key);
    if (idx == nums.length) return remaining == 0 ? 1 : 0;

    int result = solve(nums, idx + 1, remaining - nums[idx])
               + solve(nums, idx + 1, remaining + nums[idx]);
    memo.put(key, result);
    return result;
}
```

- **Intuition:** Same recursion, cache `(idx, remaining)` to avoid recomputation.
- **TC:** O(n × totalSum)
- **SC:** O(n × totalSum) — memo + stack

---

### 3. Tabulation (Bottom-Up)

```java
public int findTargetSumWays(int[] nums, int target) {
    int totalSum = 0;
    for (int n : nums) totalSum += n;

    if (Math.abs(target) > totalSum || (totalSum + target) % 2 != 0) return 0;

    int S = (totalSum + target) / 2;
    int n = nums.length;
    int[][] dp = new int[n + 1][S + 1];
    dp[0][0] = 1; // 0 elements, sum=0 → 1 way

    for (int i = 1; i <= n; i++) {
        for (int j = 0; j <= S; j++) {
            dp[i][j] = dp[i-1][j];                                    // skip num
            if (j >= nums[i-1]) dp[i][j] += dp[i-1][j - nums[i-1]]; // take num
        }
    }
    return dp[n][S];
}
```

- **Intuition:** `dp[i][j]` = ways to pick subset from first `i` nums summing to `j`.
- **TC:** O(n × S)
- **SC:** O(n × S)

---

### 4. Space Optimization

```java
public int findTargetSumWays(int[] nums, int target) {
    int totalSum = 0;
    for (int n : nums) totalSum += n;

    if (Math.abs(target) > totalSum || (totalSum + target) % 2 != 0) return 0;

    int S = (totalSum + target) / 2;
    int[] dp = new int[S + 1];
    dp[0] = 1;

    for (int num : nums) {
        for (int j = S; j >= num; j--) { // right-to-left: read i-1 values (0/1 knapsack)
            dp[j] += dp[j - num];
        }
    }
    return dp[S];
}
```

- **Intuition:** Only previous row needed → collapse to 1D. Traverse right-to-left so we read `i-1` values, not `i`.
- **TC:** O(n × S)
- **SC:** O(S)

### Edge Cases
- `(totalSum + target) % 2 != 0` → no valid split, return 0
- `|target| > totalSum` → impossible, return 0
- Zeros in array → handled correctly

---

## 2. Coin Change 2 (LC 518)

**Problem:** Given coins of infinite supply and an `amount`, return number of combinations to make up that amount.

### Key Insight
Unbounded knapsack — each coin can be used unlimited times.
- At each coin, two choices: **skip it** or **use it (and stay at same coin index)**.
- Unlike 0/1 knapsack (move to next item after taking), here we stay because coin is reusable.
- Order doesn't matter → combinations, not permutations (outer loop = items, not amount).

---

### 1. Recursion

```java
public int change(int amount, int[] coins) {
    return solve(coins, 0, amount);
}

private int solve(int[] coins, int idx, int remaining) {
    if (remaining == 0) return 1;                     // exact match
    if (idx == coins.length || remaining < 0) return 0;

    // skip current coin OR use current coin (stay at idx — unbounded)
    return solve(coins, idx + 1, remaining)
         + solve(coins, idx, remaining - coins[idx]);
}
```

- **TC:** O(2^amount) — exponential
- **SC:** O(amount) — call stack depth

---

### 2. Memoization (Top-Down)

```java
int[][] memo;

public int change(int amount, int[] coins) {
    memo = new int[coins.length][amount + 1];
    for (int[] row : memo) Arrays.fill(row, -1);
    return solve(coins, 0, amount);
}

private int solve(int[] coins, int idx, int remaining) {
    if (remaining == 0) return 1;
    if (idx == coins.length || remaining < 0) return 0;
    if (memo[idx][remaining] != -1) return memo[idx][remaining];

    memo[idx][remaining] = solve(coins, idx + 1, remaining)
                         + solve(coins, idx, remaining - coins[idx]);
    return memo[idx][remaining];
}
```

- **Intuition:** Cache `(idx, remaining)`. State space = coins × amount unique subproblems.
- **TC:** O(n × amount)
- **SC:** O(n × amount) — memo + stack

---

### 3. Tabulation (Bottom-Up)

```java
public int change(int amount, int[] coins) {
    int n = coins.length;
    int[][] dp = new int[n + 1][amount + 1];

    // base case: 0 amount → 1 way (use no coins) for all rows
    for (int i = 0; i <= n; i++) dp[i][0] = 1;

    for (int i = 1; i <= n; i++) {
        for (int j = 0; j <= amount; j++) {
            dp[i][j] = dp[i-1][j];                                    // skip coin i
            if (j >= coins[i-1]) dp[i][j] += dp[i][j - coins[i-1]]; // use coin i (same row = reuse!)
        }
    }
    return dp[n][amount];
}
```

- **Intuition:** `dp[i][j]` = combinations using first `i` coins for amount `j`.
  Key difference from 0/1 knapsack: use `dp[i][j - coins[i-1]]` (same row) not `dp[i-1][...]` (prev row) — allows reuse.
- **TC:** O(n × amount)
- **SC:** O(n × amount)

---

### 4. Space Optimization

```java
public int change(int amount, int[] coins) {
    int[] dp = new int[amount + 1];
    dp[0] = 1;

    for (int coin : coins) {
        for (int j = coin; j <= amount; j++) { // left-to-right: reads updated values → models reuse
            dp[j] += dp[j - coin];
        }
    }
    return dp[amount];
}
```

- **Intuition:** 1D array. Traverse **left-to-right** (opposite of 0/1 knapsack!) so `dp[j - coin]` is already updated for current coin → models unbounded reuse.
- **TC:** O(n × amount)
- **SC:** O(amount)

### Key Distinction from 0/1 Knapsack

| | 0/1 Knapsack | Unbounded (Coin Change 2) |
|---|---|---|
| Traversal | Right → Left | Left → Right |
| Reuse | No | Yes |
| Takes from | Previous row | Current row |

### Edge Cases
- `amount == 0` → return 1 (one way: use nothing)
- Coin larger than amount → skip naturally
- No valid combo → return 0

---

## 3. Climbing Stairs (LC 70)

**Problem:** `n` steps, can climb 1 or 2 at a time. How many distinct ways?

### Key Insight
Same as Fibonacci: `ways(n) = ways(n-1) + ways(n-2)`. Classic 1D DP intro.

### Optimal (Space-Optimized)

```kotlin
fun climbStairs(n: Int): Int {
    if (n <= 2) return n
    var prev2 = 1  // ways(1)
    var prev1 = 2  // ways(2)
    for (i in 3..n) {
        val curr = prev1 + prev2
        prev2 = prev1
        prev1 = curr
    }
    return prev1
}
```

- **TC:** O(n) | **SC:** O(1)
- **Edge cases:** `n = 1` → 1, `n = 2` → 2

---

## 4. Longest Increasing Subsequence (LC 300)

**Problem:** Length of longest strictly increasing subsequence (not necessarily contiguous).

### Key Insight
**Patience sorting / binary search:** Maintain `tails[i]` = smallest tail of an LIS of length `i+1`.
For each num, binary search where to place it. O(n log n).

### Optimal

```kotlin
fun lengthOfLIS(nums: IntArray): Int {
    val tails = IntArray(nums.size)
    var size = 0
    for (num in nums) {
        var lo = 0
        var hi = size
        while (lo < hi) {
            val mid = (lo + hi) / 2
            if (tails[mid] < num) lo = mid + 1 else hi = mid
        }
        tails[lo] = num
        if (lo == size) size++
    }
    return size
}
```

- **TC:** O(n log n) | **SC:** O(n)
- **Follow-up:** DP O(n²) is easier to explain in interviews; mention both.

---

## 5. Two Sum (LC 1)

**Problem:** Return indices of two numbers that add to `target`.

### Key Insight
One pass with HashMap: for each `num`, check if `target - num` exists.

```kotlin
fun twoSum(nums: IntArray, target: Int): IntArray {
    val seen = HashMap<Int, Int>()
    for (i in nums.indices) {
        val complement = target - nums[i]
        seen[complement]?.let { return intArrayOf(it, i) }
        seen[nums[i]] = i
    }
    return intArrayOf()
}
```

- **TC:** O(n) | **SC:** O(n)
- **Edge cases:** Exactly one solution guaranteed; don't reuse same element

---

## 6. Product of Array Except Self (LC 238)

**Problem:** Return array where `output[i]` = product of all elements except `nums[i]`. No division.

### Key Insight
Prefix products left-to-right, then suffix products right-to-left in one output array.

```kotlin
fun productExceptSelf(nums: IntArray): IntArray {
    val n = nums.size
    val result = IntArray(n) { 1 }
    var prefix = 1
    for (i in 0 until n) {
        result[i] = prefix
        prefix *= nums[i]
    }
    var suffix = 1
    for (i in n - 1 downTo 0) {
        result[i] *= suffix
        suffix *= nums[i]
    }
    return result
}
```

- **TC:** O(n) | **SC:** O(1) excluding output
- **Follow-up:** Handle zeros — zero in array makes most outputs zero

---

## 7. Merge Intervals (LC 56)

**Problem:** Merge overlapping intervals.

### Key Insight
Sort by start, then merge if current overlaps with last merged.

```kotlin
fun merge(intervals: Array<IntArray>): Array<IntArray> {
    if (intervals.isEmpty()) return intervals
    intervals.sortBy { it[0] }
    val merged = mutableListOf<IntArray>()
    merged.add(intervals[0])
    for (i in 1 until intervals.size) {
        val last = merged.last()
        if (intervals[i][0] <= last[1]) {
            last[1] = maxOf(last[1], intervals[i][1])
        } else {
            merged.add(intervals[i])
        }
    }
    return merged.toTypedArray()
}
```

- **TC:** O(n log n) | **SC:** O(n)
- **Edge cases:** Single interval, all overlapping, no overlap

---

## 8. Longest Substring Without Repeating (LC 3)

**Problem:** Length of longest substring without repeating characters.

### Key Insight
Sliding window + HashMap of last seen index. Shrink left when duplicate found.

```kotlin
fun lengthOfLongestSubstring(s: String): Int {
    val lastSeen = HashMap<Char, Int>()
    var maxLen = 0
    var left = 0
    for (right in s.indices) {
        val c = s[right]
        if (c in lastSeen && lastSeen[c]!! >= left) {
            left = lastSeen[c]!! + 1
        }
        lastSeen[c] = right
        maxLen = maxOf(maxLen, right - left + 1)
    }
    return maxLen
}
```

- **TC:** O(n) | **SC:** O(min(n, charset))
- **Pattern:** Variable-size sliding window

---

## 9. Sliding Window Maximum (LC 239)

**Problem:** Max in each sliding window of size `k`.

### Key Insight
Monotonic deque stores indices in decreasing value order. Front = max for current window.

```kotlin
fun maxSlidingWindow(nums: IntArray, k: Int): IntArray {
    val deque = ArrayDeque<Int>()
    val result = IntArray(nums.size - k + 1)
    for (i in nums.indices) {
        while (deque.isNotEmpty() && deque.first() < i - k + 1) deque.removeFirst()
        while (deque.isNotEmpty() && nums[deque.last()] < nums[i]) deque.removeLast()
        deque.addLast(i)
        if (i >= k - 1) result[i - k + 1] = nums[deque.first()]
    }
    return result
}
```

- **TC:** O(n) | **SC:** O(k)
- **Hard variant:** Know this for Google — tests deque + window reasoning

---

## 10. Binary Tree Level Order Traversal (LC 102)

**Problem:** Return level-order traversal (BFS).

```kotlin
fun levelOrder(root: TreeNode?): List<List<Int>> {
    if (root == null) return emptyList()
    val result = mutableListOf<List<Int>>()
    val queue = ArrayDeque<TreeNode>()
    queue.add(root)
    while (queue.isNotEmpty()) {
        val level = mutableListOf<Int>()
        repeat(queue.size) {
            val node = queue.removeFirst()
            level.add(node.`val`)
            node.left?.let { queue.add(it) }
            node.right?.let { queue.add(it) }
        }
        result.add(level)
    }
    return result
}
```

- **TC:** O(n) | **SC:** O(n)
- **Follow-up:** Zigzag level order (LC 103) — reverse every other level

---

## 11. Validate Binary Search Tree (LC 98)

**Problem:** Determine if a binary tree is a valid BST.

### Key Insight
Pass valid `(min, max)` range down recursively. Not enough to check only parent-child.

```kotlin
fun isValidBST(root: TreeNode?): Boolean {
    fun validate(node: TreeNode?, min: Long, max: Long): Boolean {
        if (node == null) return true
        if (node.`val`.toLong() <= min || node.`val`.toLong() >= max) return false
        return validate(node.left, min, node.`val`.toLong()) &&
               validate(node.right, node.`val`.toLong(), max)
    }
    return validate(root, Long.MIN_VALUE, Long.MAX_VALUE)
}
```

- **TC:** O(n) | **SC:** O(h)
- **Gotcha:** Use `Long` — `Int.MIN_VALUE` node breaks `val < min` check

---

## 12. Lowest Common Ancestor of BST (LC 236)

**Problem:** Find LCA of two nodes in a BST.

### Key Insight
If both values smaller → go left. Both larger → go right. Otherwise current node is LCA.

```kotlin
fun lowestCommonAncestor(root: TreeNode?, p: TreeNode?, q: TreeNode?): TreeNode? {
    var curr = root
    while (curr != null) {
        when {
            p!!.`val` < curr.`val` && q!!.`val` < curr.`val` -> curr = curr.left
            p.`val` > curr.`val` && q.`val` > curr.`val` -> curr = curr.right
            else -> return curr
        }
    }
    return null
}
```

- **TC:** O(h) | **SC:** O(1)
- **General tree variant (LC 236):** Recursive post-order — different problem, know both

---

## 13. Number of Islands (LC 200)

**Problem:** Count connected components of `'1'` in a 2D grid.

### Key Insight
DFS or BFS from each unvisited `'1'`, mark visited. Increment count per new DFS start.

```kotlin
fun numIslands(grid: Array<CharArray>): Int {
    if (grid.isEmpty()) return 0
    val rows = grid.size
    val cols = grid[0].size
    var count = 0

    fun dfs(r: Int, c: Int) {
        if (r !in 0 until rows || c !in 0 until cols || grid[r][c] != '1') return
        grid[r][c] = '0'  // mark visited
        dfs(r + 1, c); dfs(r - 1, c); dfs(r, c + 1); dfs(r, c - 1)
    }

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            if (grid[r][c] == '1') {
                count++
                dfs(r, c)
            }
        }
    }
    return count
}
```

- **TC:** O(rows × cols) | **SC:** O(rows × cols) worst-case recursion stack
- **Variants:** Max area of island (LC 695), surrounded regions (LC 130)

---

## 14. Course Schedule (LC 207)

**Problem:** Can you finish all courses? `prerequisites[i] = [a, b]` means take `b` before `a`.

### Key Insight
Topological sort — detect cycle in directed graph. Kahn's algorithm (BFS indegree) or DFS with 3-color.

```kotlin
fun canFinish(numCourses: Int, prerequisites: Array<IntArray>): Boolean {
    val adj = Array(numCourses) { mutableListOf<Int>() }
    val indegree = IntArray(numCourses)
    for ((course, prereq) in prerequisites) {
        adj[prereq].add(course)
        indegree[course]++
    }
    val queue = ArrayDeque<Int>()
    for (i in 0 until numCourses) {
        if (indegree[i] == 0) queue.add(i)
    }
    var visited = 0
    while (queue.isNotEmpty()) {
        val node = queue.removeFirst()
        visited++
        for (next in adj[node]) {
            if (--indegree[next] == 0) queue.add(next)
        }
    }
    return visited == numCourses
}
```

- **TC:** O(V + E) | **SC:** O(V + E)
- **Follow-up:** Return ordering (LC 210) — append to result in Kahn's loop

---

## 15. Binary Search (LC 704)

**Problem:** Find `target` in sorted array, return index or -1.

### Key Insight
Classic template: `lo`, `hi`, `mid = lo + (hi - lo) / 2`. Narrow based on comparison.

```kotlin
fun search(nums: IntArray, target: Int): Int {
    var lo = 0
    var hi = nums.size - 1
    while (lo <= hi) {
        val mid = lo + (hi - lo) / 2
        when {
            nums[mid] == target -> return mid
            nums[mid] < target -> lo = mid + 1
            else -> hi = mid - 1
        }
    }
    return -1
}
```

- **TC:** O(log n) | **SC:** O(1)
- **Template:** Use `while (lo <= hi)` for exact match; `while (lo < hi)` for boundary problems

---

## 16. Search in Rotated Sorted Array (LC 33)

**Problem:** Sorted array rotated at unknown pivot. Search in O(log n).

### Key Insight
One half is always sorted. Check which half contains `target`.

```kotlin
fun search(nums: IntArray, target: Int): Int {
    var lo = 0
    var hi = nums.size - 1
    while (lo <= hi) {
        val mid = lo + (hi - lo) / 2
        if (nums[mid] == target) return mid
        if (nums[lo] <= nums[mid]) {
            if (target in nums[lo]..<nums[mid]) hi = mid - 1 else lo = mid + 1
        } else {
            if (target in nums[mid + 1]..nums[hi]) lo = mid + 1 else hi = mid - 1
        }
    }
    return -1
}
```

- **TC:** O(log n) | **SC:** O(1)
- **Follow-up:** With duplicates (LC 81) — worst case O(n) when all same

---

## 17. Kth Largest Element (LC 215)

**Problem:** Find the kth largest element in unsorted array.

### Key Insight
Min-heap of size k — root is kth largest. Quickselect is O(n) average but heap is easier under interview pressure.

```kotlin
fun findKthLargest(nums: IntArray, k: Int): Int {
    val heap = PriorityQueue<Int>()  // min-heap by default in Java PQ
    for (num in nums) {
        heap.offer(num)
        if (heap.size > k) heap.poll()
    }
    return heap.peek()
}
```

- **TC:** O(n log k) | **SC:** O(k)
- **Follow-up:** Quickselect O(n) average — mention if asked about optimization

---

## 18. Top K Frequent Elements (LC 347)

**Problem:** Return k most frequent elements.

### Key Insight
Count with HashMap, then min-heap of size k on frequency. Bucket sort is O(n) if range is bounded.

```kotlin
fun topKFrequent(nums: IntArray, k: Int): IntArray {
    val freq = nums.groupingBy { it }.eachCount()
    val heap = PriorityQueue<Pair<Int, Int>>(compareBy { it.second })  // min-heap by freq
    for ((num, count) in freq) {
        heap.offer(num to count)
        if (heap.size > k) heap.poll()
    }
    return heap.map { it.first }.toIntArray()
}
```

- **TC:** O(n log k) | **SC:** O(n)
- **Google favorite:** Combines hash map + heap — very common phone screen pattern

---

## 19. Subsets (LC 78)

**Problem:** Return all subsets (power set) of distinct integers.

### Key Insight
Backtracking: at each index, choose include or exclude. Or iterative — double result set for each new element.

```kotlin
fun subsets(nums: IntArray): List<List<Int>> {
    val result = mutableListOf<List<Int>>()
    fun backtrack(start: Int, path: MutableList<Int>) {
        result.add(path.toList())
        for (i in start until nums.size) {
            path.add(nums[i])
            backtrack(i + 1, path)
            path.removeAt(path.lastIndex)
        }
    }
    backtrack(0, mutableListOf())
    return result
}
```

- **TC:** O(n × 2^n) | **SC:** O(n) recursion depth
- **Pattern:** Include/exclude branching — base for many backtracking problems

---

## 20. Permutations (LC 46)

**Problem:** Return all permutations of distinct integers.

### Key Insight
Backtracking with `used` boolean array. Swap-based approach also works (LC 47 with duplicates).

```kotlin
fun permute(nums: IntArray): List<List<Int>> {
    val result = mutableListOf<List<Int>>()
    val used = BooleanArray(nums.size)
    fun backtrack(path: MutableList<Int>) {
        if (path.size == nums.size) {
            result.add(path.toList())
            return
        }
        for (i in nums.indices) {
            if (used[i]) continue
            used[i] = true
            path.add(nums[i])
            backtrack(path)
            path.removeAt(path.lastIndex)
            used[i] = false
        }
    }
    backtrack(mutableListOf())
    return result
}
```

- **TC:** O(n × n!) | **SC:** O(n)
- **Follow-up:** Permutations II (LC 47) — sort + skip duplicates at same level

---

## 21. Word Search (LC 79)

**Problem:** Find if word exists in 2D board (adjacent cells, no reuse).

### Key Insight
DFS backtracking with in-place marking (`#`) to avoid extra `visited` array.

```kotlin
fun exist(board: Array<CharArray>, word: String): Boolean {
    val rows = board.size
    val cols = board[0].size
    fun dfs(r: Int, c: Int, idx: Int): Boolean {
        if (idx == word.length) return true
        if (r !in 0 until rows || c !in 0 until cols || board[r][c] != word[idx]) return false
        val temp = board[r][c]
        board[r][c] = '#'
        val found = dfs(r + 1, c, idx + 1) || dfs(r - 1, c, idx + 1) ||
                    dfs(r, c + 1, idx + 1) || dfs(r, c - 1, idx + 1)
        board[r][c] = temp
        return found
    }
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            if (dfs(r, c, 0)) return true
        }
    }
    return false
}
```

- **TC:** O(rows × cols × 4^L) | **SC:** O(L) stack
- **Pattern:** Grid DFS + undo — same as islands with path constraint

---

## 22. Meeting Rooms II (LC 253)

**Problem:** Minimum conference rooms needed for overlapping intervals.

### Key Insight
Sort by start time. Min-heap tracks end times of ongoing meetings. If earliest end ≤ new start, reuse room.

```kotlin
fun minMeetingRooms(intervals: Array<IntArray>): Int {
    if (intervals.isEmpty()) return 0
    intervals.sortBy { it[0] }
    val heap = PriorityQueue<Int>()  // end times
    heap.offer(intervals[0][1])
    for (i in 1 until intervals.size) {
        if (heap.peek() <= intervals[i][0]) heap.poll()
        heap.offer(intervals[i][1])
    }
    return heap.size
}
```

- **TC:** O(n log n) | **SC:** O(n)
- **Alternative:** Chronological ordering of all start/end events — same complexity, different intuition

---

## 23. Reverse Linked List (LC 206)

**Problem:** Reverse a singly linked list in-place.

### Key Insight
Three pointers: `prev`, `curr`, `next`. Iterative is O(1) space; recursive is O(n) stack.

```kotlin
fun reverseList(head: ListNode?): ListNode? {
    var prev: ListNode? = null
    var curr = head
    while (curr != null) {
        val next = curr.next
        curr.next = prev
        prev = curr
        curr = next
    }
    return prev
}
```

- **TC:** O(n) | **SC:** O(1)
- **Follow-ups:** Reverse between m-n (LC 92), palindrome linked list (LC 234)

---

## 24. Merge Two Sorted Lists (LC 21)

**Problem:** Merge two sorted linked lists into one sorted list.

### Key Insight
Dummy head + tail pointer. Compare heads, attach smaller, advance.

```kotlin
fun mergeTwoLists(l1: ListNode?, l2: ListNode?): ListNode? {
    val dummy = ListNode(0)
    var tail = dummy
    var a = l1
    var b = l2
    while (a != null && b != null) {
        if (a.`val` <= b.`val`) {
            tail.next = a
            a = a.next
        } else {
            tail.next = b
            b = b.next
        }
        tail = tail.next!!
    }
    tail.next = a ?: b
    return dummy.next
}
```

- **TC:** O(n + m) | **SC:** O(1)
- **Pattern:** Dummy node simplifies head insertion — use for many linked list problems

---

## 25. LRU Cache (LC 146)

**Problem:** Design LRU cache with `get` and `put` in O(1).

### Key Insight
`HashMap` for O(1) lookup + **doubly linked list** for O(1) eviction of least recently used.
On access: move node to front. On capacity: remove tail.

```kotlin
class LRUCache(capacity: Int) {
    private val cap = capacity
    private val map = HashMap<Int, Node>()
    private val head = Node(0, 0)
    private val tail = Node(0, 0)

    init {
        head.next = tail
        tail.prev = head
    }

    private class Node(val key: Int, var value: Int) {
        var prev: Node? = null
        var next: Node? = null
    }

    fun get(key: Int): Int {
        val node = map[key] ?: return -1
        moveToFront(node)
        return node.value
    }

    fun put(key: Int, value: Int) {
        if (map.containsKey(key)) {
            map[key]!!.value = value
            moveToFront(map[key]!!)
            return
        }
        if (map.size == cap) {
            val lru = tail.prev!!
            remove(lru)
            map.remove(lru.key)
        }
        val node = Node(key, value)
        map[key] = node
        insertAfterHead(node)
    }

    private fun moveToFront(node: Node) {
        remove(node)
        insertAfterHead(node)
    }

    private fun remove(node: Node) {
        node.prev?.next = node.next
        node.next?.prev = node.prev
    }

    private fun insertAfterHead(node: Node) {
        node.next = head.next
        node.prev = head
        head.next?.prev = node
        head.next = node
    }
}
```

- **TC:** O(1) per operation | **SC:** O(capacity)
- **Google favorite:** Tests data structure design — explain why HashMap + DLL, not HashMap alone

---

## 26. Serialize and Deserialize Binary Tree (LC 297)

**Problem:** Serialize binary tree to string and deserialize back.

### Key Insight
**Preorder with null markers** — unambiguous reconstruction. Use `#` for null.

```kotlin
class Codec {
    fun serialize(root: TreeNode?): String {
        if (root == null) return "#"
        return "${root.`val`},${serialize(root.left)},${serialize(root.right)}"
    }

    fun deserialize(data: String): TreeNode? {
        val tokens = data.split(",").toMutableList()
        return build(tokens)
    }

    private fun build(tokens: MutableList<String>): TreeNode? {
        if (tokens.isEmpty()) return null
        val first = tokens.removeAt(0)
        if (first == "#") return null
        val node = TreeNode(first.toInt())
        node.left = build(tokens)
        node.right = build(tokens)
        return node
    }
}
```

- **TC:** O(n) serialize + deserialize | **SC:** O(n)
- **Follow-up:** BFS level-order serialization — know both; preorder+null is simpler to code

---

## 27. Median of Two Sorted Arrays (LC 4)

**Problem:** Find median of two sorted arrays in O(log(m+n)).

### Key Insight
Binary search on the **shorter** array's partition. Partition both arrays so left halves form the lower half of merged array. Median depends on max(left) and min(right).

```kotlin
fun findMedianSortedArrays(nums1: IntArray, nums2: IntArray): Double {
    if (nums1.size > nums2.size) return findMedianSortedArrays(nums2, nums1)
    val m = nums1.size
    val n = nums2.size
    var lo = 0
    var hi = m
    while (lo <= hi) {
        val i = (lo + hi) / 2
        val j = (m + n + 1) / 2 - i
        val maxLeft1 = if (i == 0) Int.MIN_VALUE else nums1[i - 1]
        val minRight1 = if (i == m) Int.MAX_VALUE else nums1[i]
        val maxLeft2 = if (j == 0) Int.MIN_VALUE else nums2[j - 1]
        val minRight2 = if (j == n) Int.MAX_VALUE else nums2[j]
        if (maxLeft1 <= minRight2 && maxLeft2 <= minRight1) {
            return if ((m + n) % 2 == 1) {
                maxOf(maxLeft1, maxLeft2).toDouble()
            } else {
                (maxOf(maxLeft1, maxLeft2) + minOf(minRight1, minRight2)) / 2.0
            }
        } else if (maxLeft1 > minRight2) {
            hi = i - 1
        } else {
            lo = i + 1
        }
    }
    return 0.0
}
```

- **TC:** O(log(min(m,n))) | **SC:** O(1)
- **Interview tip:** Hard problem — explain intuition (partitioning merged array) even if code is shaky. Merge approach O(m+n) is acceptable warm-up.

---

## Pattern Quick Reference

| Pattern | When to Use | Problems Above |
|---------|-------------|----------------|
| Hash Map | O(1) lookup, complement counting | Two Sum, Longest Substring |
| Prefix/Suffix | Avoid division, range products | Product Except Self |
| Sort + Merge | Intervals, scheduling | Merge Intervals |
| Sliding Window | Contiguous subarray/substring | LC 3, LC 239 |
| Monotonic Deque | Window min/max | Sliding Window Maximum |
| BFS/DFS | Trees, grids, graphs | Level Order, Islands |
| BST property | Range validation, LCA | Validate BST, LCA |
| Topological Sort | Dependencies, ordering | Course Schedule |
| 1D DP | Fibonacci-like recurrence | Climbing Stairs |
| 1D/2D DP | Subset, knapsack, paths | Target Sum, Coin Change 2 |
| Binary Search on answer | LIS tails, capacity | LIS |
| Binary Search | Sorted array, rotated array | LC 704, LC 33 |
| Min-Heap (size k) | Kth largest, top K frequent | LC 215, LC 347 |
| Backtracking | Subsets, permutations, grid paths | LC 78, 46, 79 |
| Greedy + Heap | Interval scheduling | Meeting Rooms II |
| Linked List | Dummy head, reverse in-place | LC 206, 21 |
| HashMap + DLL | O(1) get/put with eviction | LRU Cache |
| Tree Serialize | Preorder + null marker | LC 297 |
| Binary Search (partition) | Median of two sorted arrays | LC 4 |

---

## Study Order for Google Coding Round

1. **Arrays + Hash Maps** (5–7) — most phone screens start here
2. **Sliding Window** (8–9) — very common at Google
3. **Binary Search** (15–16) — know the template cold
4. **Trees** (10–12) — BFS/DFS + BST property
5. **Graphs** (13–14) — islands + topological sort
6. **Heaps** (17–18) — top K / kth element pattern
7. **Backtracking** (19–21) — subsets → permutations → grid DFS
8. **Linked Lists** (23–24) — reverse, merge, dummy head pattern
9. **DP** (1–4) — go deep on 1–2 patterns, not every variant
10. **Hard** (25–27) — LRU Cache, Serialize Tree, Median (onsite-level)

---
