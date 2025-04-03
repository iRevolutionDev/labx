package software.revolution.labx.presentation.viewmodel

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import software.revolution.labx.data.repository.FileRepository
import software.revolution.labx.domain.model.FileItem
import software.revolution.labx.domain.model.Result
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileExplorerViewModel @Inject constructor(
    private val fileRepository: FileRepository
) : ViewModel() {

    private val _currentDirectory =
        MutableStateFlow<String>(Environment.getExternalStorageDirectory().absolutePath)
    val currentDirectory: StateFlow<String> = _currentDirectory.asStateFlow()

    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadFilesInCurrentDirectory()
    }

    fun loadFilesInCurrentDirectory() {
        viewModelScope.launch {
            fileRepository.listFilesInDirectory(_currentDirectory.value).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                        _error.value = null
                    }

                    is Result.Success -> {
                        _isLoading.value = false
                        _files.value = result.data.sortedWith(
                            compareBy({ !it.isDirectory }, { it.name.lowercase() })
                        )
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _error.value = result.message
                        _files.value = emptyList()
                    }
                }
            }
        }
    }

    fun navigateToDirectory(path: String) {
        _currentDirectory.value = path
        loadFilesInCurrentDirectory()
    }

    fun navigateUp() {
        val currentPath = _currentDirectory.value
        val parent = File(currentPath).parent
        if (parent != null) {
            _currentDirectory.value = parent
            loadFilesInCurrentDirectory()
        }
    }

    fun createNewFile(fileName: String) {
        if (fileName.isBlank()) return

        viewModelScope.launch {
            fileRepository.createFile(_currentDirectory.value, fileName).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                    }

                    is Result.Success -> {
                        _isLoading.value = false
                        loadFilesInCurrentDirectory()
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _error.value = result.message
                    }
                }
            }
        }
    }

    fun deleteFile(fileItem: FileItem) {
        viewModelScope.launch {
            fileRepository.deleteFile(fileItem).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                    }

                    is Result.Success -> {
                        _isLoading.value = false
                        if (result.data) {
                            loadFilesInCurrentDirectory()
                        } else {
                            _error.value = "Failed to delete file"
                        }
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _error.value = result.message
                    }
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}