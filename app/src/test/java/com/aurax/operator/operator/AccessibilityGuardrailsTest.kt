package com.aurax.operator.operator

import android.text.InputType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccessibilityGuardrailsTest {
    @Test fun passwordVariationsAreBlocked() {
        assertTrue(AccessibilityGuardrails.isPasswordLikeInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        assertTrue(AccessibilityGuardrails.isPasswordLikeInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD))
        assertTrue(AccessibilityGuardrails.isPasswordLikeInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD))
    }

    @Test fun ordinaryTextIsNotPassword() {
        assertFalse(AccessibilityGuardrails.isPasswordLikeInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL))
    }

    @Test fun securityPackagesAreBlocked() {
        assertTrue(AccessibilityGuardrails.isBlockedPackage("com.google.android.apps.authenticator2"))
        assertTrue(AccessibilityGuardrails.isBlockedPackage("com.android.settings.security.foo"))
        assertFalse(AccessibilityGuardrails.isBlockedPackage("com.android.chrome"))
    }

    @Test fun destructiveActionsRequireConfirmation() {
        assertTrue(AccessibilityGuardrails.requiresConfirmation("Delete this file"))
        assertTrue(AccessibilityGuardrails.requiresConfirmation("Transfer money"))
        assertFalse(AccessibilityGuardrails.requiresConfirmation("Scroll down"))
    }

    @Test fun youtubeEngagementIsBlocked() {
        assertTrue(AccessibilityGuardrails.isBlockedYouTubeAction("Subscribe to channel"))
        assertTrue(AccessibilityGuardrails.isBlockedYouTubeAction("Post comment"))
        assertFalse(AccessibilityGuardrails.isBlockedYouTubeAction("Play video"))
    }
}
