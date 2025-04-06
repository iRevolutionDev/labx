package software.revolution.labx.util

import android.util.Log

private const val TAG = "SoraLSP"

/**
 * Log a debug message to logcat.
 */
fun logDebug(message: String) {
    Log.d(TAG, message)
}

/**
 * Log an error message to logcat.
 */
fun logError(message: String) {
    Log.e(TAG, message)
}

/**
 * Log an info message to logcat.
 */
fun logInfo(message: String) {
    Log.i(TAG, message)
}

/**
 * Log a warning message to logcat.
 */
fun logWarning(message: String) {
    Log.w(TAG, message)
}