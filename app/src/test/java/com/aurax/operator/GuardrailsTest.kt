package com.aurax.operator

import com.aurax.operator.operator.AccessibilityGuardrails
import org.junit.Assert.*
import org.junit.Test

class GuardrailsTest {
    @Test fun passwordIsSensitive() {
        assertTrue(AccessibilityGuardrails.isSensitiveText("Enter password"))
    }

    @Test fun paymentRequiresConfirmation() {
        assertTrue(AccessibilityGuardrails.requiresConfirmation("purchase item"))
    }

    @Test fun youtubeSubscriptionIsBlocked() {
        assertTrue(AccessibilityGuardrails.isBlockedYouTubeAction("Subscribe"))
    }
}
