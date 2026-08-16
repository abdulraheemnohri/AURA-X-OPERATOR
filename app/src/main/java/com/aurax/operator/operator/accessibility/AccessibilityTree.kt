package com.aurax.operator.operator.accessibility

/** Compatibility contract for screen-perception consumers that need a serialized accessibility tree. */
interface AccessibilityTree {
    fun getTree(): String
}
