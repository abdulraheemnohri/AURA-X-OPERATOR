package com.aurax.operator.operator.actions
import android.view.accessibility.AccessibilityNodeInfo
import com.aurax.operator.operator.AccessibilityOperator
suspend fun AccessibilityOperator.type(node:AccessibilityNodeInfo,text:String)=safeType(node,text)