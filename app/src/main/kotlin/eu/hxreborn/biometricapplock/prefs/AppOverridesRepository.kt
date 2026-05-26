package eu.hxreborn.biometricapplock.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class AppOverrides(
    val relockDelaySeconds: Int?,
    val flagSecureDisabled: Boolean?,
)

private fun SharedPreferences.getIntOrNull(key: String): Int? =
    if (contains(key)) getInt(key, 0) else null

private fun SharedPreferences.getBooleanOrNull(key: String): Boolean? =
    if (contains(key)) getBoolean(key, false) else null

class AppOverridesRepository(
    private val local: SharedPreferences,
    private val remoteProvider: () -> SharedPreferences? = { null },
) {
    private fun relockKey(pkg: String) = "app_override:$pkg:relock_delay_seconds"

    private fun flagSecureKey(pkg: String) = "app_override:$pkg:flag_secure_disabled"

    private fun prefix(pkg: String) = "app_override:$pkg:"

    private fun currentOverrides(pkg: String) =
        AppOverrides(
            relockDelaySeconds = local.getIntOrNull(relockKey(pkg)),
            flagSecureDisabled = local.getBooleanOrNull(flagSecureKey(pkg)),
        )

    fun observe(pkg: String): Flow<AppOverrides> =
        callbackFlow {
            trySend(currentOverrides(pkg))
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key?.startsWith(prefix(pkg)) == true) trySend(currentOverrides(pkg))
                }
            local.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { local.unregisterOnSharedPreferenceChangeListener(listener) }
        }

    private fun editBoth(block: SharedPreferences.Editor.() -> Unit) {
        local.edit(action = block)
        remoteProvider()?.edit(commit = true, action = block)
    }

    fun setRelockDelaySeconds(
        pkg: String,
        seconds: Int?,
    ) {
        val key = relockKey(pkg)
        editBoth { if (seconds == null) remove(key) else putInt(key, seconds) }
    }

    fun setFlagSecureDisabled(
        pkg: String,
        disabled: Boolean?,
    ) {
        val key = flagSecureKey(pkg)
        editBoth { if (disabled == null) remove(key) else putBoolean(key, disabled) }
    }

    fun reset(pkg: String) =
        editBoth {
            remove(relockKey(pkg))
            remove(flagSecureKey(pkg))
        }

    fun prune(installedPackages: Set<String>) {
        val keysToRemove =
            local.all.keys.filter { key ->
                if (!key.startsWith("app_override:")) return@filter false
                val pkg = key.removePrefix("app_override:").substringBefore(":")
                pkg !in installedPackages
            }
        if (keysToRemove.isEmpty()) return
        editBoth { keysToRemove.forEach { remove(it) } }
    }
}
