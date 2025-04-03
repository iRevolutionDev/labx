package software.revolution.labx.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import software.revolution.labx.util.StoragePermissionManager
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor() : ViewModel() {
    
    private val _storagePermissionsGranted = MutableStateFlow(false)
    val storagePermissionsGranted: StateFlow<Boolean> = _storagePermissionsGranted.asStateFlow()
    
    private var permissionManager: StoragePermissionManager? = null
    
    fun initPermissionManager(permissionManager: StoragePermissionManager) {
        this.permissionManager = permissionManager
        checkPermissions()
    }
    
    fun checkPermissions() {
        viewModelScope.launch {
            permissionManager?.let {
                _storagePermissionsGranted.value = it.hasStoragePermissions()
            }
        }
    }
    
    fun requestPermissions() {
        permissionManager?.checkAndRequestPermissions()
    }
}