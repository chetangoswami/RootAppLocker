package eu.hxreborn.biometricapplock

import android.content.SharedPreferences
import android.os.Process
import eu.hxreborn.biometricapplock.hook.lockedPackages
import eu.hxreborn.biometricapplock.hook.registerSystemServerHooks
import eu.hxreborn.biometricapplock.prefs.Prefs
import eu.hxreborn.biometricapplock.util.Logger
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam

@PublishedApi
internal lateinit var module: BiometricAppLockModule
    private set

class BiometricAppLockModule : XposedModule() {
    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        module = this
        Logger.info("loaded in ${param.processName}")
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        val locked = readLockedPackages()
        Logger.info("system_server starting pid=${Process.myPid()} locked=${locked.size}")
        Logger.debug { "locked=$locked" }
        runCatching { registerSystemServerHooks(param.classLoader, locked) }
            .onFailure {
                Logger.error("registerSystemServerHooks failed: ${it.message}", it)
            }
        registerPrefsListener()
    }

    private fun registerPrefsListener() {
        runCatching {
            val prefs = getRemotePreferences(Prefs.GROUP)
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
                    if (key != Prefs.LOCKED_PACKAGES.key) return@OnSharedPreferenceChangeListener
                    lockedPackages = parseLockedPackages(Prefs.LOCKED_PACKAGES.read(sp))
                    Logger.info("config updated locked=${lockedPackages.size}")
                    Logger.debug { "locked=$lockedPackages" }
                }
            prefsListener = listener
            prefs.registerOnSharedPreferenceChangeListener(listener)
        }.onFailure { Logger.warn("prefs listener failed: ${it.message}", it) }
    }

    private fun readLockedPackages(): Set<String> =
        runCatching {
            parseLockedPackages(Prefs.LOCKED_PACKAGES.read(getRemotePreferences(Prefs.GROUP)))
        }.getOrElse {
            Logger.error("failed to read locked packages: ${it.message}", it)
            emptySet()
        }

    private fun parseLockedPackages(raw: String): Set<String> =
        if (raw.isEmpty()) emptySet() else raw.split("|").toSet()
}
