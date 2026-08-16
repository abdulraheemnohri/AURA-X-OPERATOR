package com.aurax.operator.network.lan

import android.content.Context
import com.aurax.operator.ai.model.ModelHub
import com.aurax.operator.data.entities.ModelEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
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
        // Given mockContext for NSD service
        val mockNsdManager = org.mockito.kotlin.mock<android.net.nsd.NsdManager>()
        whenever(mockContext.getSystemService(Context.NSD_SERVICE)).thenReturn(mockNsdManager)

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
    fun `test LANServer handleRequest for models`() = runBlocking {
        // Given
        val models = listOf(
            ModelEntity(
                id = "1",
                name = "Qwen2.5-0.5B-Instruct",
                displayName = "Qwen 2.5 0.5B Instruct",
                category = "LLM",
                format = "GGUF",
                quantization = "Q4_K_M",
                sourceUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF",
                localPath = "/path/to/model.gguf",
                sizeBytes = 1000L,
                status = "READY"
            )
        )
        whenever(mockModelHub.models).thenReturn(flowOf(models))
        
        // When
        val response = lanServer.handleRequestForTesting("GET /models")
        
        // Then
        assertEquals("Qwen2.5-0.5B-Instruct", response)
    }
    
    @Test
    fun `test LANServer handleRequest for inference`() = runBlocking {
        // When
        val response = lanServer.handleRequestForTesting("POST /infer")
        
        // Then
        assertEquals("Inference unavailable: authenticated local inference endpoint is not configured", response)
    }
    
    @Test
    fun `test LANServer handleRequest for unknown`() = runBlocking {
        // When
        val response = lanServer.handleRequestForTesting("UNKNOWN")
        
        // Then
        assertEquals("Unknown request", response)
    }
}
