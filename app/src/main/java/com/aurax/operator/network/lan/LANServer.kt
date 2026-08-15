package com.aurax.operator.network.lan

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.aurax.operator.ai.model.ModelHub
import java.io.*
import java.net.ServerSocket
import java.net.Socket

/**
 * LAN server for AURA-X Operator.
 * Supports mDNS discovery and model inference over the local network.
 */
class LANServer(
    private val context: Context,
    private val modelHub: ModelHub,
    private val settings: LANSettings
) {
    
    private var serverSocket: ServerSocket? = null
    private var nsdManager: NsdManager? = null
    private var nsdServiceInfo: NsdServiceInfo? = null
    private var isRunning = false
    private var isRegistered = false
    
    companion object {
        private const val TAG = "LANServer"
        private const val SERVICE_TYPE = "_aura-x._tcp"
    }
    
    /**
     * Starts the LAN server.
     */
    fun start() {
        if (isRunning) {
            Log.d(TAG, "LAN server is already running")
            return
        }
        
        try {
            // Initialize mDNS
            nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
            registerMdnsService()
            
            // Start server socket
            serverSocket = ServerSocket(settings.port)
            isRunning = true
            
            Log.d(TAG, "LAN server started on port ${settings.port}")
            
            // Start accepting connections in a background thread
            Thread {
                while (isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept()
                        clientSocket?.let { handleClient(it) }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error accepting client connection: ${e.message}")
                    }
                }
            }.start()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start LAN server: ${e.message}")
            isRunning = false
        }
    }
    
    /**
     * Stops the LAN server.
     */
    fun stop() {
        isRunning = false
        
        try {
            serverSocket?.close()
            unregisterMdnsService()
            Log.d(TAG, "LAN server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping LAN server: ${e.message}")
        }
    }
    
    /**
     * Registers the mDNS service for discovery.
     */
    private fun registerMdnsService() {
        if (isRegistered) return
        
        nsdServiceInfo = NsdServiceInfo().apply {
            serviceName = "AURA-X-${android.os.Build.SERIAL}"
            serviceType = SERVICE_TYPE
            port = settings.port
        }
        
        nsdManager?.registerService(
            nsdServiceInfo,
            NsdManager.PROTOCOL_DNS_SD,
            object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                    Log.d(TAG, "mDNS service registered: ${serviceInfo.serviceName}")
                    isRegistered = true
                }
                
                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.e(TAG, "mDNS registration failed: $errorCode")
                    isRegistered = false
                }
                
                override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                    Log.d(TAG, "mDNS service unregistered")
                    isRegistered = false
                }
                
                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.e(TAG, "mDNS unregistration failed: $errorCode")
                }
            }
        )
    }
    
    /**
     * Unregisters the mDNS service.
     */
    private fun unregisterMdnsService() {
        if (!isRegistered) return
        
        nsdManager?.unregisterService(object : NsdManager.RegistrationListener {
            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "mDNS service unregistered")
                isRegistered = false
            }
            
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "mDNS unregistration failed: $errorCode")
            }
        })
    }
    
    /**
     * Handles a client connection.
     */
    private fun handleClient(clientSocket: Socket) {
        Log.d(TAG, "Handling client connection from ${clientSocket.inetAddress}")
        
        try {
            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val writer = PrintWriter(clientSocket.getOutputStream(), true)
            
            // Read request
            val request = reader.readLine()
            Log.d(TAG, "Received request: $request")
            
            // Process request (e.g., model list, inference)
            val response = processRequest(request)
            writer.println(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error handling client: ${e.message}")
        } finally {
            try {
                clientSocket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing client socket: ${e.message}")
            }
        }
    }
    
    /**
     * Processes a client request.
     */
    private fun processRequest(request: String): String {
        return when {
            request.startsWith("GET /models") -> {
                // Return list of available models
                val models = modelHub.getInstalledModels()
                models.joinToString(",") { it.name }
            }
            request.startsWith("POST /infer") -> {
                // Perform inference (placeholder)
                "Inference result: Hello from AURA-X!"
            }
            else -> "Unknown request"
        }
    }
    
    /**
     * Checks if the server is running.
     */
    fun isServerRunning(): Boolean = isRunning
    
    /**
     * Gets the port the server is running on.
     */
    fun getPort(): Int = settings.port
}

/**
 * Settings for LAN server.
 */
data class LANSettings(
    val enabled: Boolean = false,
    val port: Int = 8080,
    val requireAuth: Boolean = false,
    val authToken: String = ""
)
