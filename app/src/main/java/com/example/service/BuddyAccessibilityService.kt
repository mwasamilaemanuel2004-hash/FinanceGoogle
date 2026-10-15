package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BuddyAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "BuddyAccessibility"
        private var instance: BuddyAccessibilityService? = null
        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        fun getInstance(): BuddyAccessibilityService? = instance

        fun simulateClick(x: Float, y: Float): Boolean {
            val svc = instance ?: return false
            return svc.dispatchGestureClick(x, y)
        }

        fun executeSwipe(startX: Float, startY: Float, endX: Float, endY: Float): Boolean {
            val svc = instance ?: return false
            return svc.dispatchGestureSwipe(startX, startY, endX, endY)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceConnected.value = true
        Log.i(TAG, "Buddy AI Autonomous Accessibility Harness Connected Successfully.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        instance = null
        _isServiceConnected.value = false
        Log.w(TAG, "Buddy AI Autonomous Accessibility Harness Interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceConnected.value = false
    }

    fun dispatchGestureClick(x: Float, y: Float): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 50L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                Log.d(TAG, "Simulated touch click completed at ($x, $y)")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.w(TAG, "Simulated touch click cancelled at ($x, $y)")
            }
        }, null)
    }

    fun dispatchGestureSwipe(startX: Float, startY: Float, endX: Float, endY: Float): Boolean {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 300L)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    fun getRootScreenText(): String {
        val rootNode = rootInActiveWindow ?: return ""
        val sb = StringBuilder()
        traverseNode(rootNode, sb)
        return sb.toString()
    }

    private fun traverseNode(node: AccessibilityNodeInfo, sb: StringBuilder) {
        if (node.text != null) {
            sb.append(node.text).append(" ")
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                traverseNode(child, sb)
                child.recycle()
            }
        }
    }
}
