package eu.hxreborn.biometricapplock.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class AppOverrides(
    val relockDelaySeconds: Int?,
    val flagSecureDisabled: Boolean?,
    val showRecentsPreview: Boolean?,
)

private fun SharedPreferences.getIntOrNull(key: String): Int? =
    if (contains(key)) getInt(key, 0) else null

private fun SharedPreferences.getBooleanOrNull(key: String): Boolean? =
    if (contains(key)) getBoolean(key, false) else null

class AppOverridesRepository(
    private val local: SharedPreferences,
) {
    private fun relockKey(pkg: String) = "app_override:$pkg:relock_delay_seconds"

    private fun flagSecureKey(pkg: String) = "app_override:$pkg:flag_secure_disabled"

    private fun recentsPreviewKey(pkg: String) = "app_override:$pkg:show_recents_preview"

    private fun prefix(pkg: String) = "app_override:$pkg:"

    private fun currentOverrides(pkg: String) =
        AppOverrides(
            relockDelaySeconds = local.getIntOrNull(relockKey(pkg)),
            flagSecureDisabled = local.getBooleanOrNull(flagSecureKey(pkg)),
            showRecentsPreview = local.getBooleanOrNull(recentsPreviewKey(pkg)),
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

    fun setRelockDelaySeconds(
        pkg: String,
        seconds: Int?,
    ) {
        val key = relockKey(pkg)
        local.edit { if (seconds == null) remove(key) else putInt(key, seconds) }
    }

    fun setFlagSecureDisabled(
        pkg: String,
        disabled: Boolean?,
    ) {
        val key = flagSecureKey(pkg)
        local.edit { if (disabled == null) remove(key) else putBoolean(key, disabled) }
    }

    fun setShowRecentsPreview(
        pkg: String,
        enabled: Boolean?,
    ) {
        val key = recentsPreviewKey(pkg)
        local.edit { if (enabled == null) remove(key) else putBoolean(key, enabled) }
    }

    fun reset(pkg: String) =
        local.edit {
            remove(relockKey(pkg))
            remove(flagSecureKey(pkg))
            remove(recentsPreviewKey(pkg))
        }

    fun prune(installedPackages: Set<String>) {
        val keysToRemove =
            local.all.keys.filter { key ->
                if (!key.startsWith("app_override:")) return@filter false
                val pkg = key.removePrefix("app_override:").substringBefore(":")
                pkg !in installedPackages
            }
        if (keysToRemove.isEmpty()) return
        local.edit { keysToRemove.forEach { remove(it) } }
    }
}
