package com.useractionrecorder.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.useractionrecorder.model.EventType
import com.useractionrecorder.model.UserActionEvent

class RecordingAccessibilityService : AccessibilityService() {
    private val recordingManager by lazy {
        RecordingManager.getInstance(applicationContext)
    }

    companion object {
        private const val TAG = "RecordingAccessibility"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        try {
            when (event.eventType) {
                AccessibilityEvent.TYPE_VIEW_CLICKED -> handleClickEvent(event)
                AccessibilityEvent.TYPE_VIEW_LONG_CLICKED -> handleLongClickEvent(event)
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> handleScrollEvent(event)
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> handleTextChangeEvent(event)
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> handleWindowStateChanged(event)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing accessibility event", e)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    private fun handleClickEvent(event: AccessibilityEvent) {
        val actionEvent = UserActionEvent(
            type = EventType.CLICK,
            timestamp = System.currentTimeMillis(),
            packageName = event.packageName?.toString(),
            className = event.className?.toString(),
            text = event.text?.joinToString(),
            contentDescription = event.contentDescription?.toString(),
            bounds = event.source?.boundsInScreen?.flattenToString(),
            action = event.action
        )
        recordingManager.addEvent(actionEvent)
    }

    private fun handleLongClickEvent(event: AccessibilityEvent) {
        val actionEvent = UserActionEvent(
            type = EventType.LONG_CLICK,
            timestamp = System.currentTimeMillis(),
            packageName = event.packageName?.toString(),
            className = event.className?.toString(),
            text = event.text?.joinToString(),
            contentDescription = event.contentDescription?.toString(),
            bounds = event.source?.boundsInScreen?.flattenToString(),
            action = event.action
        )
        recordingManager.addEvent(actionEvent)
    }

    private fun handleScrollEvent(event: AccessibilityEvent) {
        val actionEvent = UserActionEvent(
            type = EventType.SCROLL,
            timestamp = System.currentTimeMillis(),
            packageName = event.packageName?.toString(),
            className = event.className?.toString(),
            text = null,
            contentDescription = event.contentDescription?.toString(),
            bounds = event.source?.boundsInScreen?.flattenToString(),
            action = event.action
        )
        recordingManager.addEvent(actionEvent)
    }

    private fun handleTextChangeEvent(event: AccessibilityEvent) {
        val actionEvent = UserActionEvent(
            type = EventType.TEXT_CHANGE,
            timestamp = System.currentTimeMillis(),
            packageName = event.packageName?.toString(),
            className = event.className?.toString(),
            text = event.text?.joinToString(),
            contentDescription = null,
            bounds = event.source?.boundsInScreen?.flattenToString(),
            action = event.action
        )
        recordingManager.addEvent(actionEvent)
    }

    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val actionEvent = UserActionEvent(
            type = EventType.WINDOW_STATE_CHANGED,
            timestamp = System.currentTimeMillis(),
            packageName = event.packageName?.toString(),
            className = event.className?.toString(),
            text = event.text?.joinToString(),
            contentDescription = event.contentDescription?.toString(),
            bounds = null,
            action = event.action
        )
        recordingManager.addEvent(actionEvent)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
    }
}