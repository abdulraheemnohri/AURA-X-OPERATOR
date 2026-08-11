package com.aurax.operator.operator.actions
import android.view.accessibility.AccessibilityNodeInfo
import com.aurax.operator.operator.AccessibilityOperator
suspend fun AccessibilityOperator.click(node:AccessibilityNodeInfo)=safeClick(node)