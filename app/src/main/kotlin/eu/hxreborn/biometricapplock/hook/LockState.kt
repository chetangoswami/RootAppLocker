package eu.hxreborn.biometricapplock.hook

import android.content.SharedPreferences
import android.os.SystemClock
import eu.hxreborn.biometricapplock.BiometricAuthActivity
import eu.hxreborn.biometricapplock.prefs.Prefs
import eu.hxreborn.biometricapplock.util.Logger
import java.util.concurrent.ConcurrentHashMap

internal const val RELOCK_DELAY_NEVER = -1

internal data class TaskEntry(
    val packageName: String,
    val topActivityClassName: String,
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

private val appRelockOverrides = ConcurrentHashMap<String, Int>()

private val appBlockScreenshotsOverrides = ConcurrentHashMap<String, Boolean>()

internal fun getEffectiveRelockDelay(pkg: String): Int =
    appRelockOverrides[pkg] ?: globalRelockDelaySeconds

internal fun shouldBlockScreenshots(pkg: String): Boolean =
    appBlockScreenshotsOverrides[pkg] ?: globalBlockScreenshots

internal fun loadHookPrefs(prefs: SharedPreferences) {
    globalRelockDelaySeconds = Prefs.RELOCK_DELAY_SECONDS.read(prefs)
    globalBlockScreenshots = Prefs.BLOCK_SCREENSHOTS.read(prefs)
    appRelockOverrides.clear()
    appBlockScreenshotsOverrides.clear()
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
        }
    }
    Logger.info(
        "prefs loaded relockDelay=$globalRelockDelaySeconds " +
            "blockScreenshots=$globalBlockScreenshots " +
            "relockOverrides=${appRelockOverrides.size} " +
            "blockOverrides=${appBlockScreenshotsOverrides.size}",
    )
}
