package com.aurax.operator.testutils

import org.mockito.Mockito
import org.mockito.kotlin.mock

/**
 * Helper functions for Mockito in Kotlin tests.
 */

// Generic mock helper
inline fun <reified T : Any> mock(): T = mock()

// Mock with default answers
inline fun <reified T : Any> mock(defaultAnswer: Mockito.ReturnDefaultValues): T {
    return Mockito.mock(T::class.java, defaultAnswer)
}

// Verify a function was called exactly once
inline fun <reified T> verifyOnce(mock: T, block: T.() -> Unit) {
    Mockito.verify(mock, Mockito.times(1)).block()
}

// Verify a function was never called
inline fun <reified T> verifyNever(mock: T, block: T.() -> Unit) {
    Mockito.verify(mock, Mockito.never()).block()
}

// Verify a function was called exactly N times
inline fun <reified T> verifyTimes(mock: T, times: Int, block: T.() -> Unit) {
    Mockito.verify(mock, Mockito.times(times)).block()
}

// Helper for any() with nullable types
fun <T> anyNullable(): T = Mockito.any()
