package eu.hxreborn.biometricapplock.hook

import eu.hxreborn.biometricapplock.BiometricAuthActivity
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

internal data class TaskEntry(
    val packageName: String,
    val topActivityClassName: String,
)

@Volatile
internal var lockedPackages: Set<String> = emptySet()

private val unlockedRef = AtomicReference<Set<String>>(emptySet())

internal val unlockedPackages: Set<String>
    get() = unlockedRef.get()

internal fun addUnlocked(packageName: String) {
    unlockedRef.updateAndGet { it + packageName }
}

internal fun clearUnlocked() {
    unlockedRef.set(emptySet())
}

internal val taskCache = ConcurrentHashMap<Int, TaskEntry>()

internal fun relockOtherPackages(keepPackageName: String?) {
    if (keepPackageName == BiometricAuthActivity.MODULE_PACKAGE) return
    unlockedRef.updateAndGet { current ->
        when {
            current.isEmpty() -> current
            keepPackageName != null && keepPackageName in current -> setOf(keepPackageName)
            else -> emptySet()
        }
    }
}
