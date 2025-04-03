package software.revolution.labx.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import software.revolution.labx.di.IoDispatcher
import software.revolution.labx.domain.model.FileItem
import software.revolution.labx.domain.model.Result
import java.io.File
import java.io.IOException
import javax.inject.Inject

interface FileRepository {
    suspend fun getFileContent(fileItem: FileItem): Flow<Result<String>>
    suspend fun saveFileContent(fileItem: FileItem, content: String): Flow<Result<Unit>>
    suspend fun listFilesInDirectory(directoryPath: String): Flow<Result<List<FileItem>>>
    suspend fun createFile(directoryPath: String, fileName: String): Flow<Result<FileItem>>
    suspend fun deleteFile(fileItem: FileItem): Flow<Result<Boolean>>
}

class FileRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FileRepository {

    override suspend fun getFileContent(fileItem: FileItem): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            val content = withContext(ioDispatcher) {
                fileItem.file.readText()
            }
            emit(Result.Success(content))
        } catch (e: IOException) {
            emit(Result.Error("Error on read file: ${e.message}"))
        } catch (_: OutOfMemoryError) {
            emit(Result.Error("File is too large to read"))
        } catch (e: Exception) {
            emit(Result.Error("Unknown error: ${e.message}"))
        }
    }

    override suspend fun saveFileContent(fileItem: FileItem, content: String): Flow<Result<Unit>> =
        flow {
            emit(Result.Loading)
            try {
                withContext(ioDispatcher) {
                    fileItem.file.writeText(content)
                }
                emit(Result.Success(Unit))
            } catch (e: IOException) {
                emit(Result.Error("Failed to save file: ${e.message}"))
            } catch (e: Exception) {
                emit(Result.Error("Unknown error: ${e.message}"))
            }
        }

    override suspend fun listFilesInDirectory(directoryPath: String): Flow<Result<List<FileItem>>> =
        flow {
            emit(Result.Loading)
            try {
                val directory = File(directoryPath)
                if (!directory.exists() || !directory.isDirectory) {
                    emit(Result.Error("Directory does not exist or is not a directory"))
                    return@flow
                }

                val fileItems = withContext(ioDispatcher) {
                    directory.listFiles()?.mapNotNull { file ->
                        val extension = file.extension.lowercase()
                        FileItem(
                            id = file.absolutePath,
                            name = file.name,
                            path = file.absolutePath,
                            extension = extension,
                            isDirectory = file.isDirectory,
                            size = file.length(),
                            lastModified = file.lastModified(),
                            file = file
                        )
                    } ?: emptyList()
                }

                emit(Result.Success(fileItems))
            } catch (e: Exception) {
                emit(Result.Error("Error listing files: ${e.message}"))
            }
        }

    override suspend fun createFile(
        directoryPath: String,
        fileName: String
    ): Flow<Result<FileItem>> = flow {
        emit(Result.Loading)
        try {
            val directory = File(directoryPath)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            if (!file.exists()) {
                val created = withContext(ioDispatcher) {
                    file.createNewFile()
                }

                if (!created) {
                    emit(Result.Error("Failed to create file"))
                    return@flow
                }
            }

            val fileItem = FileItem(
                id = file.absolutePath,
                name = file.name,
                path = file.absolutePath,
                extension = file.extension.lowercase(),
                isDirectory = file.isDirectory,
                size = file.length(),
                lastModified = file.lastModified(),
                file = file
            )

            emit(Result.Success(fileItem))
        } catch (e: IOException) {
            emit(Result.Error("Error creating file: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("Unknown error: ${e.message}"))
        }
    }

    override suspend fun deleteFile(fileItem: FileItem): Flow<Result<Boolean>> = flow {
        emit(Result.Loading)
        try {
            val deleted = withContext(ioDispatcher) {
                if (fileItem.isDirectory) {
                    fileItem.file.deleteRecursively()
                } else {
                    fileItem.file.delete()
                }
            }

            emit(Result.Success(deleted))
        } catch (e: Exception) {
            emit(Result.Error("Error deleting file: ${e.message}"))
        }
    }
}