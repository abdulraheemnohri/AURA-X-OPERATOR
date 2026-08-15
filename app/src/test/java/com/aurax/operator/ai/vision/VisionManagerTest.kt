package com.aurax.operator.ai.vision

import android.graphics.Bitmap
import com.aurax.operator.operator.accessibility.AccessibilityTree
import com.aurax.operator.operator.ocr.OcrEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for VisionManager.
 */
class VisionManagerTest {
    
    @Mock
    private lateinit var mockVisionRuntime: VisionRuntime
    
    @Mock
    private lateinit var mockOcrEngine: OcrEngine
    
    @Mock
    private lateinit var mockAccessibilityTree: AccessibilityTree
    
    private lateinit var visionManager: VisionManager
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        visionManager = VisionManager(
            mockVisionRuntime,
            mockOcrEngine,
            mockAccessibilityTree
        )
    }
    
    @Test
    fun `test VisionManager with available vision runtime`() = runBlocking {
        // Given
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val visionResult = VisionResult(
            description = "Test description",
            labels = listOf("label1", "label2"),
            confidence = 0.9f
        )
        val ocrText = "Test OCR text"
        val accessibilityTree = "Test accessibility tree"
        
        whenever(mockVisionRuntime.isAvailable()).thenReturn(true)
        whenever(mockVisionRuntime.analyze(any())).thenReturn(visionResult)
        whenever(mockOcrEngine.extractText(any())).thenReturn(ocrText)
        whenever(mockAccessibilityTree.getTree()).thenReturn(accessibilityTree)
        
        // When
        val screenContext = visionManager.analyzeScreen(bitmap)
        
        // Then
        assertEquals(visionResult, screenContext.vision)
        assertEquals(ocrText, screenContext.ocr)
        assertEquals(accessibilityTree, screenContext.accessibilityTree)
        
        verify(mockVisionRuntime).isAvailable()
        verify(mockVisionRuntime).analyze(eq(ImageInput(bitmap, null)))
        verify(mockOcrEngine).extractText(eq(bitmap))
        verify(mockAccessibilityTree).getTree()
    }
    
    @Test
    fun `test VisionManager with unavailable vision runtime`() = runBlocking {
        // Given
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val ocrText = "Test OCR text"
        val accessibilityTree = "Test accessibility tree"
        
        whenever(mockVisionRuntime.isAvailable()).thenReturn(false)
        whenever(mockOcrEngine.extractText(any())).thenReturn(ocrText)
        whenever(mockAccessibilityTree.getTree()).thenReturn(accessibilityTree)
        
        // When
        val screenContext = visionManager.analyzeScreen(bitmap)
        
        // Then
        assertEquals(null, screenContext.vision)
        assertEquals(ocrText, screenContext.ocr)
        assertEquals(accessibilityTree, screenContext.accessibilityTree)
        
        verify(mockVisionRuntime).isAvailable()
        verify(mockVisionRuntime).analyze(any()) // Should not be called
        verify(mockOcrEngine).extractText(eq(bitmap))
        verify(mockAccessibilityTree).getTree()
    }
    
    @Test
    fun `test VisionManager isVisionAvailable`() {
        // Given
        whenever(mockVisionRuntime.isAvailable()).thenReturn(true)
        
        // When
        val isAvailable = visionManager.isVisionAvailable()
        
        // Then
        assertEquals(true, isAvailable)
        verify(mockVisionRuntime).isAvailable()
    }
    
    @Test
    fun `test VisionManager loadVisionModel`() {
        // Given
        val modelPath = "test/model.gguf"
        whenever(mockVisionRuntime.load(modelPath)).thenReturn(true)
        
        // When
        val isLoaded = visionManager.loadVisionModel(modelPath)
        
        // Then
        assertEquals(true, isLoaded)
        verify(mockVisionRuntime).load(modelPath)
    }
    
    @Test
    fun `test VisionManager unloadVisionModel`() {
        // When
        visionManager.unloadVisionModel()
        
        // Then
        verify(mockVisionRuntime).unload()
    }
    
    @Test
    fun `test VisionManager getVisionStatus`() {
        // Given
        val expectedStatus = VisionRuntimeStatus.READY
        whenever(mockVisionRuntime.getStatus()).thenReturn(expectedStatus)
        
        // When
        val status = visionManager.getVisionStatus()
        
        // Then
        assertEquals(expectedStatus, status)
        verify(mockVisionRuntime).getStatus()
    }
}

// Mock interfaces for testing
interface OcrEngine {
    fun extractText(bitmap: Bitmap): String
}

interface AccessibilityTree {
    fun getTree(): String
}
