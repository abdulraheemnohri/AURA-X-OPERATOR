package com.aurax.operator.network.lan

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.aurax.operator.ai.model.ModelHub
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import kotlinx.coroutines.flow.first

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

    fun start() {
        if (isRunning) {
            Log.d(TAG, "LAN server is already running")
            return
        }
        try {
            nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
            registerMdnsService()
            serverSocket = ServerSocket(settings.port)
            isRunning = true
            Log.d(TAG, "LAN server started on port ${settings.port}")
            Thread {
                while (isRunning) {
                    try {
                        serverSocket?.accept()?.let { handleClient(it) }
                    } catch (e: Exception) {
                        if (isRunning) Log.e(TAG, "Error accepting client connection: ${e.message}")
                    }
                }
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start LAN server: ${e.message}")
            isRunning = false
        }
    }

    fun stop() {
        isRunning = false
        runCatching { serverSocket?.close() }
        unregisterMdnsService()
        serverSocket = null
        Log.d(TAG, "LAN server stopped")
    }

    private fun registerMdnsService() {
        if (isRegistered) return
        nsdServiceInfo = NsdServiceInfo().apply {
            serviceName = "AURA-X-${System.identityHashCode(this@LANServer)}"
            serviceType = SERVICE_TYPE
            port = settings.port
        }
        nsdManager?.registerService(
            nsdServiceInfo,
            NsdManager.PROTOCOL_DNS_SD,
            object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                    isRegistered = true
                    Log.d(TAG, "mDNS service registered: ${serviceInfo.serviceName}")
                }
                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    isRegistered = false
                    Log.e(TAG, "mDNS registration failed: $errorCode")
                }
                override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                    isRegistered = false
                }
                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.e(TAG, "mDNS unregistration failed: $errorCode")
                }
            }
        )
    }

    private fun unregisterMdnsService() {
        if (!isRegistered) return
        nsdManager?.unregisterService(object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) = Unit
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit
            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                isRegistered = false
            }
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "mDNS unregistration failed: $errorCode")
                isRegistered = false
            }
        })
    }

    private fun handleClient(clientSocket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val writer = PrintWriter(clientSocket.getOutputStream(), true)
            val request = reader.readLine().orEmpty()
            val response = runBlocking { processRequest(request) }
            writer.println(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling client: ${e.message}")
        } finally {
            runCatching { clientSocket.close() }
        }
    }

    private suspend fun processRequest(request: String): String {
        return when {
            request.startsWith("GET /models") -> {
                modelHub.models.first()
                    .filter { !it.localPath.isNullOrBlank() }
                    .joinToString(",") { it.name }
            }
            request.startsWith("POST /infer") ->
                "Inference unavailable: authenticated local inference endpoint is not configured"
            else -> "Unknown request"
        }
    }

    fun isServerRunning(): Boolean = isRunning
    fun getPort(): Int = settings.port
}

data class LANSettings(
    val enabled: Boolean = false,
    val port: Int = 8080,
    val requireAuth: Boolean = false,
    val authToken: String = ""
)
