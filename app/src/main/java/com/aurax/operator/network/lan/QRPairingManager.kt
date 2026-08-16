package com.aurax.operator.network.lan

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.json.JSONObject

/**
 * Manages QR code pairing for LAN server connections.
 */
class QRPairingManager(
    private val context: Context,
    private val lanServer: LANServer
) {
    
    companion object {
        private const val TAG = "QRPairingManager"
    }
    
    /**
     * Generates a QR code for pairing with the LAN server.
     * @return Bitmap of the QR code.
     */
    fun generatePairingQR(): Bitmap? {
        try {
            val pairingInfo = PairingInfo(
                ip = getLocalIpAddress(),
                port = lanServer.getPort(),
                authToken = generateAuthToken()
            )
            
            val jsonString = JSONObject().apply {
                put("ip", pairingInfo.ip)
                put("port", pairingInfo.port)
                put("authToken", pairingInfo.authToken)
            }.toString()
            
            // Generate QR code
            val bitMatrix = MultiFormatWriter().encode(
                jsonString,
                BarcodeFormat.QR_CODE,
                500,
                500
            )
            
            val barcodeEncoder = BarcodeEncoder()
            return barcodeEncoder.createBitmap(bitMatrix)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate QR code: ${e.message}")
            return null
        }
    }
    
    /**
     * Scans a QR code and extracts pairing information.
     * @param qrData The data from the scanned QR code.
     * @return PairingInfo if successful, null otherwise.
     */
    fun parsePairingQR(qrData: String): PairingInfo? {
        return try {
            val json = JSONObject(qrData)
            PairingInfo(
                ip = json.getString("ip"),
                port = json.getInt("port"),
                authToken = json.getString("authToken")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse QR code: ${e.message}")
            null
        }
    }
    
    /**
     * Gets the local IP address of the device.
     */
    private fun getLocalIpAddress(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        return address.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local IP address: ${e.message}")
        }
        return "127.0.0.1"
    }
    
    /**
     * Generates a random auth token for pairing.
     */
    private fun generateAuthToken(): String {
        return java.util.UUID.randomUUID().toString()
    }
}

/**
 * Data class for pairing information.
 */
data class PairingInfo(
    val ip: String,
    val port: Int,
    val authToken: String
)
