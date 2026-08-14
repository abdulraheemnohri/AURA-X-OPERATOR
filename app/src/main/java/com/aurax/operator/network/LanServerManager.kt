package com.aurax.operator.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/** Optional loopback HTTP companion endpoint. It deliberately never binds 0.0.0.0. */
class LanServerManager(
    private val scope: CoroutineScope,
    private val port: Int = 8080,
    private val authEnabled: Boolean = true,
    token: String = UUID.randomUUID().toString()
) {
    private val running = AtomicBoolean(false)
    private var serverSocket: ServerSocket? = null
    private var job: Job? = null
    val authToken: String = token

    fun start(): Result<Unit> = runCatching {
        if (running.getAndSet(true)) return@runCatching
        serverSocket = ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"))
        job = scope.launch(Dispatchers.IO) {
            while (isActive && running.get()) {
                val socket = runCatching { serverSocket?.accept() }.getOrNull() ?: break
                launch { handle(socket) }
            }
        }
    }

    fun stop() {
        running.set(false)
        job?.cancel()
        runCatching { serverSocket?.close() }
        serverSocket = null
    }

    private fun handle(socket: Socket) {
        socket.use { s ->
            val reader = BufferedReader(InputStreamReader(s.getInputStream()))
            val writer = OutputStreamWriter(s.getOutputStream())
            val requestLine = reader.readLine() ?: return
            val headers = mutableMapOf<String, String>()
            while (true) {
                val line = reader.readLine() ?: break
                if (line.isBlank()) break
                val split = line.indexOf(':')
                if (split > 0) headers[line.substring(0, split).trim().lowercase()] = line.substring(split + 1).trim()
            }
            val authorized = !authEnabled || headers["authorization"] == "Bearer $authToken"
            val path = requestLine.split(' ').getOrNull(1) ?: "/"
            val status = when {
                !authorized -> 401
                path == "/health" -> 200
                path == "/about" -> 200
                else -> 404
            }
            val body = when (status) {
                200 -> if (path == "/health") "{\"ok\":true,\"bind\":\"127.0.0.1\",\"port\":$port}" else "{\"name\":\"AURA-X-OPERATOR\",\"mode\":\"loopback\"}"
                401 -> "{\"error\":\"unauthorized\"}"
                else -> "{\"error\":\"not_found\"}"
            }
            writer.write("HTTP/1.1 $status ${if (status == 200) "OK" else "ERROR"}\r\nContent-Type: application/json\r\nContent-Length: ${body.toByteArray().size}\r\nConnection: close\r\n\r\n$body")
            writer.flush()
        }
    }
}
