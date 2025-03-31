package software.revolution.labx.util

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

/**
 * Utils for managing storage permissions in Android
 */
class StoragePermissionManager(
    private val activity: ComponentActivity,
    private val onPermissionGranted: () -> Unit
) {
    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            onPermissionGranted()
        }
    }

    private val requestManageStorageLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                onPermissionGranted()
            }
        }
    }

    /**
     * Verify if the app has the necessary permissions to access storage
     */
    fun checkAndRequestPermissions() {
        if (hasStoragePermissions()) {
            onPermissionGranted()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestManageStoragePermission()
        } else {
            requestLegacyStoragePermissions()
        }
    }

    /**
     * Verify if the app has the necessary permissions to access storage
     */
    fun hasStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val readPermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            val writePermission = ContextCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            readPermission && writePermission
        }
    }

    /**
     * Request permission to manage all files on Android 11 and above
     */
    private fun requestManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = "package:${activity.packageName}".toUri()
            requestManageStorageLauncher.launch(intent)
        }
    }

    /**
     * Request legacy storage permissions for Android 10 and below
     */
    private fun requestLegacyStoragePermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }
}