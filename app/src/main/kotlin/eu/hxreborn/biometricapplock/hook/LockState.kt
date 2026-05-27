package eu.hxreborn.biometricapplock.hook

import android.content.SharedPreferences
import eu.hxreborn.biometricapplock.BiometricAuthActivity
import eu.hxreborn.biometricapplock.prefs.Prefs
import eu.hxreborn.biometricapplock.util.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

internal const val RELOCK_DELAY_NEVER = -1

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

internal fun removeFromUnlocked(pkgs: Set<String>) {
    unlockedRef.updateAndGet { it - pkgs }
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

// prefs cache — loaded once at boot, read-only in hook interceptors

@Volatile private var globalRelockDelaySeconds: Int = 0

@Volatile private var globalFlagSecureDisabled: Boolean = false

private val appRelockOverrides = ConcurrentHashMap<String, Int>()

private val appFlagSecureOverrides = ConcurrentHashMap<String, Boolean>()

@Volatile internal var screenOffElapsed = Long.MIN_VALUE

internal fun getEffectiveRelockDelay(pkg: String): Int =
    appRelockOverrides[pkg] ?: globalRelockDelaySeconds

internal fun isFlagSecureDisabled(pkg: String): Boolean =
    appFlagSecureOverrides[pkg] ?: globalFlagSecureDisabled

internal fun loadHookPrefs(prefs: SharedPreferences) {
    globalRelockDelaySeconds = Prefs.RELOCK_DELAY_SECONDS.read(prefs)
    globalFlagSecureDisabled = Prefs.DISABLE_FLAG_SECURE.read(prefs)
    appRelockOverrides.clear()
    appFlagSecureOverrides.clear()
    prefs.all.keys.forEach { key ->
        when {
            key.startsWith("app_override:") && key.endsWith(":relock_delay_seconds") -> {
                val pkg = key.removePrefix("app_override:").removeSuffix(":relock_delay_seconds")
                appRelockOverrides[pkg] = prefs.getInt(key, 0)
            }

            key.startsWith("app_override:") && key.endsWith(":flag_secure_disabled") -> {
                val pkg = key.removePrefix("app_override:").removeSuffix(":flag_secure_disabled")
                appFlagSecureOverrides[pkg] = prefs.getBoolean(key, false)
            }
        }
    }
    Logger.info(
        "prefs loaded relockDelay=$globalRelockDelaySeconds " +
            "flagSecure=$globalFlagSecureDisabled appOverrides=${appRelockOverrides.size}",
    )
}
