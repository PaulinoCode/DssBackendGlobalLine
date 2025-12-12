# Quick Reference: Copilot Plugin Crash Summary

## TL;DR

**What happened**: IntelliJ IDEA's GitHub Copilot plugin crashed with `UninitializedPropertyAccessException` when accessing `lateinit var user` before initialization.

**Where**: `AbstractSessionManager.getUser()` at line 28

**Why**: Race condition - coroutine on `Dispatchers.Default` accessed `user` property before the main thread initialized it.

**Fix**: Choose one:
1. ✅ **Quick**: Make `user` nullable → `var user: User?`
2. ✅ **Better**: Use `CompletableDeferred<User>` for thread-safe async init
3. ✅ **Best**: Pass `user` in constructor → `AbstractSessionManager(user: User)`

## Stack Trace Summary

```
UninitializedPropertyAccessException: lateinit property user has not been initialized
  at AbstractSessionManager.getUser(AbstractSessionManager.kt:28)
  at CopilotAgentSessionManager.getLastActiveSession(CopilotAgentSessionManager.kt:114)
  at AbstractSessionManager.getOrCreateSessionController(AbstractSessionManager.kt:79)
  at AbstractSessionManager.invalidateAllSessionControllers(AbstractSessionManager.kt:226)
  at CopilotAgentSessionManager$2$1$1.invokeSuspend(CopilotAgentSessionManager.kt:102)
```

## Files to Fix

| File | Line | Action |
|------|------|--------|
| `AbstractSessionManager.kt` | 28 | Fix `getUser()` - add null check or use deferred |
| `CopilotAgentSessionManager.kt` | 102 | Fix coroutine - wait for user init before launch |
| `CopilotAgentSessionManager.kt` | 114 | Handle null user in `getLastActiveSession()` |

## Quick Fix Code

### Option 1: Nullable (Fastest)
```kotlin
// AbstractSessionManager.kt
- lateinit var user: User
+ private var user: User? = null

- fun getUser(): User = user
+ fun getUser(): User = user ?: error("User not initialized")
```

### Option 2: Deferred (Best for async)
```kotlin
// AbstractSessionManager.kt
+ private val userDeferred = CompletableDeferred<User>()

+ fun setUser(u: User) = userDeferred.complete(u)
+ suspend fun getUser(): User = userDeferred.await()

// CopilotAgentSessionManager.kt line 102
scope.launch {
+   userDeferred.await()  // Wait for user
    // ... rest of code
}
```

### Option 3: Constructor (Safest)
```kotlin
// AbstractSessionManager.kt
- abstract class AbstractSessionManager {
-     lateinit var user: User
+ abstract class AbstractSessionManager(
+     protected val user: User
+ ) {
```

## Testing
```kotlin
@Test(expected = IllegalStateException::class)
fun `getUser before init throws`() {
    val mgr = TestSessionManager()
    mgr.getUser()  // Should throw
}
```

## Next Steps

1. ✅ Choose a fix strategy (recommend Option 2 or 3)
2. ✅ Apply the fix to AbstractSessionManager.kt
3. ✅ Update CopilotAgentSessionManager.kt initialization
4. ✅ Add tests for initialization ordering
5. ✅ Deploy and monitor crash reports

---
See [COPILOT_CRASH_ANALYSIS.md](./COPILOT_CRASH_ANALYSIS.md) for detailed analysis.
