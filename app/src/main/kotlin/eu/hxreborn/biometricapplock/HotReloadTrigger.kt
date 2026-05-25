package eu.hxreborn.biometricapplock

import android.util.Log
import io.github.libxposed.service.XposedService

object HotReloadTrigger {
    private const val TAG = "BiometricAppLock"

    fun tryReload(service: XposedService) {
        try {
            val props = service.frameworkProperties
            if (props and XposedService.PROP_RT_HOT_RELOAD == 0L) return

            val targets = service.runningTargets
            val systemTarget = targets.firstOrNull { it.processName == "system" } ?: return

            service.hotReloadModule(systemTarget, null) { process, result ->
                Log.i(
                    TAG,
                    "hot reload done: process=${process.processName} status=${result.status}",
                )
            }
            Log.i(TAG, "hot reload requested for system_server pid=${systemTarget.pid}")
        } catch (e: Throwable) {
            Log.w(TAG, "hot reload unavailable: ${e.message}")
        }
    }
}
