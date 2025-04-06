package software.revolution.labx.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageServer
import software.revolution.labx.util.logDebug
import software.revolution.labx.util.logError
import java.io.IOException
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * Background service for managing language server instances.
 * This service creates a server socket and delegates client connections
 * to the appropriate language server implementation.
 */
class LanguageServerService : Service() {

    companion object {
        private const val TAG = "LanguageServerService"
        private const val EXTRA_SERVER_TYPE = "server_type"
        private const val EXTRA_PORT = "port"
        private const val EXTRA_SERVER_PATH = "server_path"

        const val SERVER_TYPE_JAVA = "java"
        const val SERVER_TYPE_KOTLIN = "kotlin"

        val activePorts = ConcurrentHashMap<Int, String>()

        /**
         * Find an available port to use for a language server
         */
        fun findAvailablePort(): Int {
            val serverSocket = ServerSocket(0)

            val port = serverSocket.localPort
            serverSocket.close()
            return port
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val serverSockets = mutableListOf<ServerSocket>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return START_STICKY
    }

    private fun handleIntent(intent: Intent) {
        val serverType = intent.getStringExtra(EXTRA_SERVER_TYPE) ?: return
        val port = intent.getIntExtra(EXTRA_PORT, -1)
        intent.getStringExtra(EXTRA_SERVER_PATH)

        if (port <= 0) {
            logError("Invalid port: $port")
            return
        }

        serviceScope.launch {
            try {
                startServerSocket(serverType, port)
            } catch (e: Exception) {
                logError("Error starting language server: ${e.message}")
            }
        }
    }

    private fun startServerSocket(serverType: String, port: Int) {
        try {
            val socket = ServerSocket(port)
            serverSockets.add(socket)
            activePorts[port] = serverType

            logDebug("Starting language server socket on port $port for $serverType")

            thread {
                try {
                    while (!socket.isClosed) {
                        val client = socket.accept()
                        logDebug("Client connected to $serverType server on port $port")

                        thread {
                            try {
                                val server = createLanguageServer(serverType)
                                if (server != null) {
                                    val inputStream = client.getInputStream()
                                    val outputStream = client.getOutputStream()

                                    val launcher = LSPLauncher.createServerLauncher(
                                        server,
                                        inputStream,
                                        outputStream
                                    )

                                    val languageClient = launcher.remoteProxy
                                    connectServerToClient(server, languageClient)

                                    launcher.startListening().get()
                                }
                            } catch (e: Exception) {
                                logError("Error handling client connection: ${e.message}")
                            } finally {
                                try {
                                    client.close()
                                } catch (e: IOException) {
                                    logError("Error closing client: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (!socket.isClosed) {
                        logError("Error accepting connections: ${e.message}")
                    }
                } finally {
                    try {
                        socket.close()
                        activePorts.remove(port)
                    } catch (e: IOException) {
                        logError("Error closing server socket: ${e.message}")
                    }
                }
            }

        } catch (e: IOException) {
            logError("Failed to start server socket on port $port: ${e.message}")
        }
    }

    private fun createLanguageServer(serverType: String): LanguageServer? {
        return when (serverType) {
            SERVER_TYPE_JAVA -> {
                try {
                    JavaLanguageServerImpl()
                } catch (e: Exception) {
                    logError("Failed to create Java language server: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }

            SERVER_TYPE_KOTLIN -> {
                try {
                    KotlinLanguageServerImpl()
                } catch (e: Exception) {
                    logError("Failed to create Kotlin language server: ${e.message}")
                    e.printStackTrace()
                    null
                }
            }

            else -> {
                logError("Unsupported language server type: $serverType")
                null
            }
        }
    }

    private fun connectServerToClient(server: Any, client: LanguageClient) {
        try {
            when (server) {
                is LanguageServer -> {
                    val connectMethod =
                        server.javaClass.getDeclaredMethod("connect", LanguageClient::class.java)
                    connectMethod.isAccessible = true
                    connectMethod.invoke(server, client)
                }

                else -> logError("Unknown server type: ${server.javaClass.name}")
            }
        } catch (e: Exception) {
            logError("Failed to connect server to client: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        for (socket in serverSockets) {
            try {
                if (!socket.isClosed) {
                    socket.close()
                }
            } catch (e: IOException) {
                logError("Error closing server socket: ${e.message}")
            }
        }

        serverSockets.clear()
        activePorts.clear()
        serviceScope.cancel()
    }

    /**
     * Helper class to create intents for this service
     */
    object IntentBuilder {
        /**
         * Create an intent to start a language server on a specific port
         */
        fun createServerIntent(
            service: Service,
            serverType: String,
            port: Int,
            serverPath: String? = null
        ): Intent {
            return Intent(service, LanguageServerService::class.java).apply {
                putExtra(EXTRA_SERVER_TYPE, serverType)
                putExtra(EXTRA_PORT, port)
                if (serverPath != null) {
                    putExtra(EXTRA_SERVER_PATH, serverPath)
                }
            }
        }
    }
}