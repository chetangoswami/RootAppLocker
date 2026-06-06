package com.example.rootapplocker.hook

import android.content.SharedPreferences
import android.os.SystemClock
import com.example.rootapplocker.BiometricAuthActivity
import com.example.rootapplocker.prefs.Prefs
import com.example.rootapplocker.util.Logger
import java.util.concurrent.ConcurrentHashMap

internal const val RELOCK_DELAY_NEVER = -1

internal data class TaskEntry(
    val packageName: String,
)

@Volatile
internal var lockedPackages: Set<String> = emptySet()

// pkg -> elapsedRealtime of last interaction; entry exists iff the pkg is currently considered unlocked
private val unlockedMap = ConcurrentHashMap<String, Long>()

internal val unlockedPackages: Set<String>
    get() = unlockedMap.keys.toSet()

internal fun isUnlocked(pkg: String): Boolean {
    val ts = unlockedMap[pkg] ?: return false
    val delay = getEffectiveRelockDelay(pkg)
    if (delay == RELOCK_DELAY_NEVER) return true
    if (delay == 0) return true
    return SystemClock.elapsedRealtime() - ts < delay * 1000L
}

internal fun shouldRelockOnTransition(
    pkg: String,
    now: Long,
): Boolean {
    val delay = getEffectiveRelockDelay(pkg)
    if (delay == RELOCK_DELAY_NEVER) return false
    if (delay == 0) return true
    val ts = unlockedMap[pkg] ?: return true
    return now - ts >= delay * 1000L
}

internal fun addUnlocked(pkg: String) {
    unlockedMap[pkg] = SystemClock.elapsedRealtime()
}

internal fun refreshUnlock(pkg: String) {
    unlockedMap.computeIfPresent(pkg) { _, _ -> SystemClock.elapsedRealtime() }
}

internal fun clearUnlocked() {
    unlockedMap.clear()
}

internal fun removeFromUnlocked(pkgs: Set<String>) {
    pkgs.forEach { unlockedMap.remove(it) }
}

internal val taskCache = ConcurrentHashMap<Int, TaskEntry>()

internal fun clearRuntimeStateForPackage(pkg: String) {
    unlockedMap.remove(pkg)
    taskCache.entries.removeIf { it.value.packageName == pkg }
}

internal fun relockOtherPackages(keepPkg: String?) {
    if (keepPkg == BiometricAuthActivity.MODULE_PACKAGE) return
    val now = SystemClock.elapsedRealtime()
    unlockedMap.entries.removeIf { (pkg, _) ->
        pkg != keepPkg && shouldRelockOnTransition(pkg, now)
    }
}

// prefs cache — loaded once at boot, read-only in hook interceptors

@Volatile private var globalRelockDelaySeconds: Int = 0

@Volatile private var globalBlockScreenshots: Boolean = false

@Volatile private var globalRelockOnScreenOff: Boolean = true

@Volatile private var globalRelockOnTaskRemoved: Boolean = true

@Volatile private var globalPreventModuleUninstall: Boolean = false

@Volatile private var globalUseOpaqueUnlockPrompt: Boolean = false

private val appRelockOverrides = ConcurrentHashMap<String, Int>()

private val appBlockScreenshotsOverrides = ConcurrentHashMap<String, Boolean>()

private val appAllowedActivities = ConcurrentHashMap<String, Set<String>>()

internal fun getEffectiveRelockDelay(pkg: String): Int =
    appRelockOverrides[pkg] ?: globalRelockDelaySeconds

internal fun shouldBlockScreenshots(pkg: String): Boolean =
    appBlockScreenshotsOverrides[pkg] ?: globalBlockScreenshots

internal fun isActivityAllowed(
    pkg: String,
    className: String?,
    targetActivity: String?,
): Boolean {
    val allowed = appAllowedActivities[pkg] ?: return false
    if (className != null && className in allowed) return true
    return targetActivity != null && targetActivity in allowed
}

internal fun shouldRelockOnScreenOff(): Boolean = globalRelockOnScreenOff

internal fun shouldRelockOnTaskRemoved(): Boolean = globalRelockOnTaskRemoved

internal fun shouldPreventModuleUninstall(): Boolean = globalPreventModuleUninstall

internal fun shouldUseOpaqueUnlockPrompt(): Boolean = globalUseOpaqueUnlockPrompt

internal fun loadHookPrefs(prefs: SharedPreferences) {
    globalRelockDelaySeconds = Prefs.RELOCK_DELAY_SECONDS.read(prefs)
    globalRelockOnScreenOff = Prefs.RELOCK_ON_SCREEN_OFF.read(prefs)
    globalRelockOnTaskRemoved = Prefs.RELOCK_ON_TASK_REMOVED.read(prefs)
    globalBlockScreenshots = Prefs.BLOCK_SCREENSHOTS.read(prefs)
    globalPreventModuleUninstall = Prefs.PREVENT_MODULE_UNINSTALL.read(prefs)
    globalUseOpaqueUnlockPrompt = Prefs.USE_OPAQUE_UNLOCK_PROMPT.read(prefs)
    appRelockOverrides.clear()
    appBlockScreenshotsOverrides.clear()
    appAllowedActivities.clear()
    prefs.all.keys.forEach { key ->
        if (!key.startsWith("app_override:")) return@forEach
        when {
            key.endsWith(":relock_delay_seconds") -> {
                val pkg = key.removePrefix("app_override:").removeSuffix(":relock_delay_seconds")
                appRelockOverrides[pkg] = prefs.getInt(key, 0)
            }

            key.endsWith(":block_screenshots") -> {
                val pkg = key.removePrefix("app_override:").removeSuffix(":block_screenshots")
                appBlockScreenshotsOverrides[pkg] = prefs.getBoolean(key, false)
            }

            key.endsWith(":allowed_activities") -> {
                val pkg = key.removePrefix("app_override:").removeSuffix(":allowed_activities")
                val activities =
                    prefs
                        .getString(key, "")
                        ?.split('\n')
                        ?.filterTo(mutableSetOf()) { it.isNotBlank() }
                        .orEmpty()
                if (activities.isNotEmpty()) appAllowedActivities[pkg] = activities
            }
        }
    }
    Logger.debug {
        "prefs loaded relockDelay=$globalRelockDelaySeconds " +
            "relockOnScreenOff=$globalRelockOnScreenOff " +
            "relockOnTaskRemoved=$globalRelockOnTaskRemoved " +
            "blockScreenshots=$globalBlockScreenshots " +
            "preventUninstall=$globalPreventModuleUninstall " +
            "opaquePrompt=$globalUseOpaqueUnlockPrompt " +
            "relockOverrides=${appRelockOverrides.size} " +
            "blockOverrides=${appBlockScreenshotsOverrides.size} " +
            "allowActivityOverrides=${appAllowedActivities.size}"
    }
}
