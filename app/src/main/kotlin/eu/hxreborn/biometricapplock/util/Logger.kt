package eu.hxreborn.biometricapplock.util

import android.util.Log
import eu.hxreborn.biometricapplock.BuildConfig
import eu.hxreborn.biometricapplock.module

object Logger {
    const val TAG = "BiometricAppLock"

    fun log(
        level: Int,
        msg: String,
        t: Throwable? = null,
    ) = if (t != null) module.log(level, TAG, msg, t) else module.log(level, TAG, msg)

    fun info(msg: String) = module.log(Log.INFO, TAG, msg)

    fun warn(
        msg: String,
        t: Throwable? = null,
    ) = log(Log.WARN, msg, t)

    fun error(
        msg: String,
        t: Throwable? = null,
    ) = log(Log.ERROR, msg, t)

    inline fun debug(msg: () -> String) {
        if (BuildConfig.DEBUG) module.log(Log.DEBUG, TAG, msg())
    }
}
