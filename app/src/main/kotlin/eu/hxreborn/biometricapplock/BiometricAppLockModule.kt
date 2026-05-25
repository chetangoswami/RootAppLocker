package eu.hxreborn.biometricapplock

import android.content.SharedPreferences
import android.os.Process
import android.util.Log
import eu.hxreborn.biometricapplock.hook.lockedPackages
import eu.hxreborn.biometricapplock.hook.registerSystemServerHooks
import eu.hxreborn.biometricapplock.prefs.Prefs
import eu.hxreborn.biometricapplock.util.Logger
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.HotReloadedParam
import io.github.libxposed.api.XposedModuleInterface.HotReloadingParam
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam

@PublishedApi
internal lateinit var module: BiometricAppLockModule
    private set

class BiometricAppLockModule : XposedModule() {
    private var systemServerClassLoader: ClassLoader? = null

    @Suppress("unused")
    private var prefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        module = this
        Logger.log(Log.INFO, "loaded in ${param.processName}")
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        Logger.inSystemServer = true
        systemServerClassLoader = param.classLoader
        val locked = readLockedPackages()
        Logger.log(Log.INFO, "system_server starting pid=${Process.myPid()} locked=${locked.size}")
        Logger.debug { "locked=$locked" }
        runCatching { registerSystemServerHooks(param.classLoader, locked) }
            .onFailure {
                Logger.log(Log.ERROR, "registerSystemServerHooks failed: ${it.message}", it)
            }
        registerPrefsListener()
    }

    override fun onHotReloading(param: HotReloadingParam): Boolean {
        systemServerClassLoader?.let { param.setSavedInstanceState(it) }
        return true
    }

    // onModuleLoaded + onSystemServerStarting are not replayed on hot reload
    override fun onHotReloaded(param: HotReloadedParam) {
        module = this
        Logger.inSystemServer = param.isSystemServer

        if (!param.isSystemServer) {
            param.oldHookHandles.forEach { runCatching { it.unhook() } }
            Logger.log(Log.INFO, "hot reloaded in ${param.processName}")
            return
        }

        val classLoader =
            param.savedInstanceState as? ClassLoader
                ?: param.oldHookHandles
                    .firstOrNull()
                    ?.executable
                    ?.declaringClass
                    ?.classLoader
                ?: resolveSystemServerClassLoader()
        param.oldHookHandles.forEach { runCatching { it.unhook() } }

        if (classLoader == null) {
            Logger.log(Log.ERROR, "hot reload: system_server classLoader not available")
            return
        }
        systemServerClassLoader = classLoader

        val locked = readLockedPackages()
        Logger.log(Log.INFO, "hot reloaded pid=${Process.myPid()} locked=${locked.size}")
        Logger.debug { "locked=$locked" }
        runCatching { registerSystemServerHooks(classLoader, locked) }
            .onFailure { Logger.log(Log.ERROR, "hot reload reregister failed: ${it.message}", it) }
        registerPrefsListener()
    }

    private fun resolveSystemServerClassLoader(): ClassLoader? =
        runCatching {
            Class
                .forName(
                    "com.android.server.wm.ActivityStartInterceptor",
                    false,
                    Thread.currentThread().contextClassLoader,
                ).classLoader
        }.getOrNull()

    // TODO: not firing on 102.0.0-SNAPSHOT + LSPosed v2.0.3-it (7736), remove HotReloadTrigger call in ScopeViewModel.saveLockedPackages when it does
    private fun registerPrefsListener() {
        runCatching {
            val prefs = getRemotePreferences(Prefs.GROUP)
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
                    if (key != Prefs.LOCKED_PACKAGES.key) return@OnSharedPreferenceChangeListener
                    lockedPackages = parseLockedPackages(Prefs.LOCKED_PACKAGES.read(sp))
                    Logger.log(Log.INFO, "config updated locked=${lockedPackages.size}")
                    Logger.debug { "locked=$lockedPackages" }
                }
            prefsListener = listener
            prefs.registerOnSharedPreferenceChangeListener(listener)
        }.onFailure { Logger.log(Log.WARN, "prefs listener failed: ${it.message}", it) }
    }

    private fun readLockedPackages(): Set<String> =
        runCatching {
            parseLockedPackages(Prefs.LOCKED_PACKAGES.read(getRemotePreferences(Prefs.GROUP)))
        }.getOrElse {
            Logger.log(Log.ERROR, "failed to read locked packages: ${it.message}", it)
            emptySet()
        }

    private fun parseLockedPackages(raw: String): Set<String> =
        if (raw.isEmpty()) emptySet() else raw.split("|").toSet()
}
