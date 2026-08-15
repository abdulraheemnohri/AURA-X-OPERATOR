package com.aurax.operator.ai.vision

import android.graphics.Bitmap
import com.aurax.operator.operator.accessibility.AccessibilityTree
import com.aurax.operator.operator.ocr.OcrEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

/**
 * Unit tests for VisionManager.
 */
class VisionManagerTest {
    
    private lateinit var visionManager: VisionManager
    private lateinit var mockVisionRuntime: VisionRuntime
    private lateinit var mockOcrEngine: OcrEngine
    private lateinit var mockAccessibilityTree: AccessibilityTree
    
    @Before
    fun setup() {
        mockVisionRuntime = mock(VisionRuntime::class.java)
        mockOcrEngine = mock(OcrEngine::class.java)
        mockAccessibilityTree = mock(AccessibilityTree::class.java)
        
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
        
        `when`(mockVisionRuntime.isAvailable()).thenReturn(true)
        `when`(mockVisionRuntime.analyze(ImageInput(bitmap, null))).thenReturn(visionResult)
        `when`(mockOcrEngine.extractText(bitmap)).thenReturn(ocrText)
        `when`(mockAccessibilityTree.getTree()).thenReturn(accessibilityTree)
        
        // When
        val screenContext = visionManager.analyzeScreen(bitmap)
        
        // Then
        assertEquals(visionResult, screenContext.vision)
        assertEquals(ocrText, screenContext.ocr)
        assertEquals(accessibilityTree, screenContext.accessibilityTree)
    }
    
    @Test
    fun `test VisionManager with unavailable vision runtime`() = runBlocking {
        // Given
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val ocrText = "Test OCR text"
        val accessibilityTree = "Test accessibility tree"
        
        `when`(mockVisionRuntime.isAvailable()).thenReturn(false)
        `when`(mockOcrEngine.extractText(bitmap)).thenReturn(ocrText)
        `when`(mockAccessibilityTree.getTree()).thenReturn(accessibilityTree)
        
        // When
        val screenContext = visionManager.analyzeScreen(bitmap)
        
        // Then
        assertEquals(null, screenContext.vision)
        assertEquals(ocrText, screenContext.ocr)
        assertEquals(accessibilityTree, screenContext.accessibilityTree)
    }
    
    @Test
    fun `test VisionManager isVisionAvailable`() {
        // Given
        `when`(mockVisionRuntime.isAvailable()).thenReturn(true)
        
        // When
        val isAvailable = visionManager.isVisionAvailable()
        
        // Then
        assertEquals(true, isAvailable)
    }
    
    @Test
    fun `test VisionManager loadVisionModel`() {
        // Given
        val modelPath = "test/model.gguf"
        `when`(mockVisionRuntime.load(modelPath)).thenReturn(true)
        
        // When
        val isLoaded = visionManager.loadVisionModel(modelPath)
        
        // Then
        assertEquals(true, isLoaded)
    }
    
    @Test
    fun `test VisionManager unloadVisionModel`() {
        // When
        visionManager.unloadVisionModel()
        
        // Then (no exception should be thrown)
        assertEquals(true, true)
    }
}
