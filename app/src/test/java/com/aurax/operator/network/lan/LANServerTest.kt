package com.aurax.operator.network.lan

import android.content.Context
import com.aurax.operator.ai.model.ModelHub
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

/**
 * Unit tests for LANServer.
 */
class LANServerTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockModelHub: ModelHub
    
    private lateinit var lanServer: LANServer
    private lateinit var mockSettings: LANSettings
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        mockSettings = LANSettings(
            enabled = true,
            port = 8080,
            requireAuth = false,
            authToken = ""
        )
        
        lanServer = LANServer(mockContext, mockModelHub, mockSettings)
    }
    
    @Test
    fun `test LANServer start and stop`() {
        // When
        lanServer.start()
        
        // Then
        assertEquals(true, lanServer.isServerRunning())
        
        // When
        lanServer.stop()
        
        // Then
        assertEquals(false, lanServer.isServerRunning())
    }
    
    @Test
    fun `test LANServer getPort`() {
        // When
        val port = lanServer.getPort()
        
        // Then
        assertEquals(8080, port)
    }
    
    @Test
    fun `test LANServer processRequest for models`() {
        // Given
        val models = listOf(
            ModelEntity(
                id = 1,
                name = "Qwen2.5-0.5B-Instruct",
                path = "/path/to/model.gguf",
                status = "READY"
            )
        )
        whenever(mockModelHub.getInstalledModels()).thenReturn(models)
        
        // When
        val response = lanServer.processRequest("GET /models")
        
        // Then
        assertEquals("Qwen2.5-0.5B-Instruct", response)
    }
    
    @Test
    fun `test LANServer processRequest for inference`() {
        // When
        val response = lanServer.processRequest("POST /infer")
        
        // Then
        assertEquals("Inference result: Hello from AURA-X!", response)
    }
    
    @Test
    fun `test LANServer processRequest for unknown`() {
        // When
        val response = lanServer.processRequest("UNKNOWN")
        
        // Then
        assertEquals("Unknown request", response)
    }
}

// Mock ModelEntity for testing
data class ModelEntity(
    val id: Long,
    val name: String,
    val path: String,
    val status: String
)
