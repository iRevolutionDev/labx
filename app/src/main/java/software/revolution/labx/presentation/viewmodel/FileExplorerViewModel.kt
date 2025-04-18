package software.revolution.labx.presentation.viewmodel

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
        MutableStateFlow("")
    val currentDirectory: StateFlow<String> = _currentDirectory.asStateFlow()

    private val _files = MutableStateFlow<List<FileItem>>(emptyList())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadFilesInCurrentDirectory() {
        if (_currentDirectory.value.isEmpty()) return

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
        if (path.isEmpty() || path == _currentDirectory.value) return

        val normalizedPath = if (path.endsWith(File.separator)) path else "$path${File.separator}"

        _currentDirectory.value = normalizedPath
        loadFilesInCurrentDirectory()
    }

    fun setRootDirectory(path: String) {
        if (path.isBlank()) return

        val normalizedPath = if (path.endsWith(File.separator)) path else "$path${File.separator}"
        _currentDirectory.value = normalizedPath
        loadFilesInCurrentDirectory()
    }

    fun navigateUp() {
        val currentPath = _currentDirectory.value
        if (currentPath.isEmpty()) return

        val parent = File(currentPath).parent
        if (parent != null) {
            _currentDirectory.value = parent
            loadFilesInCurrentDirectory()
        }
    }

    fun createNewFile(fileName: String) {
        if (fileName.isBlank() || _currentDirectory.value.isEmpty()) return

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

    fun listFilesInDirectory(directoryPath: String) {
        if (directoryPath.isEmpty()) return
    }
    
    suspend fun listFilesInDirectoryFlow(directoryPath: String) =
        fileRepository.listFilesInDirectory(directoryPath)

    fun clearError() {
        _error.value = null
    }
}