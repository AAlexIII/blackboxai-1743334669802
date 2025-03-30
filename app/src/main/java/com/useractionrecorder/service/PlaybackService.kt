package com.useractionrecorder.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Service
import android.content.Intent
import android.graphics.Path
import android.os.IBinder
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.useractionrecorder.model.EventType
import com.useractionrecorder.model.Recording
import com.useractionrecorder.model.UserActionEvent
import kotlinx.coroutines.*
import java.lang.Exception

class PlaybackService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val recordingManager by lazy {
        RecordingManager.getInstance(applicationContext)
    }

    companion object {
        private const val TAG = "PlaybackService"
        private const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("recordingId")?.let { recordingId ->
            serviceScope.launch {
                playRecording(recordingId)
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun playRecording(recordingId: String) {
        try {
            val recording = recordingManager.getRecording(recordingId)
            if (recording == null) {
                Log.e(TAG, "Recording not found: $recordingId")
                stopSelf()
                return
            }

            withContext(Dispatchers.Default) {
                playEvents(recording)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing recording", e)
        } finally {
            stopSelf()
        }
    }

    private suspend fun playEvents(recording: Recording) {
        var lastEventTime = 0L
        
        recording.events.forEach { event ->
            try {
                // Вычисляем задержку между событиями
                val delay = if (lastEventTime == 0L) 0 else event.timestamp - lastEventTime
                if (delay > 0) {
                    delay(delay)
                }
                
                performAction(event)
                lastEventTime = event.timestamp
            } catch (e: Exception) {
                Log.e(TAG, "Error performing action", e)
            }
        }
    }

    private fun performAction(event: UserActionEvent) {
        when (event.type) {
            EventType.CLICK -> performClick(event)
            EventType.LONG_CLICK -> performLongClick(event)
            EventType.SCROLL -> performScroll(event)
            EventType.TEXT_CHANGE -> performTextInput(event)
            EventType.WINDOW_STATE_CHANGED -> handleWindowStateChange(event)
            EventType.BUTTON_PRESS -> performButtonPress(event)
            EventType.KEY_EVENT -> performKeyEvent(event)
        }
    }

    private fun performClick(event: UserActionEvent) {
        event.bounds?.let { bounds ->
            // Разбор строки с координатами и выполнение клика
            try {
                val coordinates = bounds.split(",").map { it.toInt() }
                if (coordinates.size >= 4) {
                    val x = coordinates[0] + (coordinates[2] - coordinates[0]) / 2
                    val y = coordinates[1] + (coordinates[3] - coordinates[1]) / 2
                    
                    val path = Path().apply {
                        moveTo(x.toFloat(), y.toFloat())
                    }
                    
                    val gesture = GestureDescription.Builder()
                        .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
                        .build()
                    
                    // Здесь нужно использовать AccessibilityService для выполнения жеста
                    // В реальном приложении это должно быть реализовано через взаимодействие с AccessibilityService
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error performing click", e)
            }
        }
    }

    private fun performLongClick(event: UserActionEvent) {
        event.bounds?.let { bounds ->
            try {
                val coordinates = bounds.split(",").map { it.toInt() }
                if (coordinates.size >= 4) {
                    val x = coordinates[0] + (coordinates[2] - coordinates[0]) / 2
                    val y = coordinates[1] + (coordinates[3] - coordinates[1]) / 2
                    
                    val path = Path().apply {
                        moveTo(x.toFloat(), y.toFloat())
                    }
                    
                    val gesture = GestureDescription.Builder()
                        .addStroke(GestureDescription.StrokeDescription(path, 0, 1000))
                        .build()
                    
                    // Аналогично click, требуется взаимодействие с AccessibilityService
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error performing long click", e)
            }
        }
    }

    private fun performScroll(event: UserActionEvent) {
        // Реализация прокрутки через AccessibilityService
        Log.d(TAG, "Performing scroll action")
    }

    private fun performTextInput(event: UserActionEvent) {
        // Реализация ввода текста через AccessibilityService
        Log.d(TAG, "Performing text input: ${event.text}")
    }

    private fun handleWindowStateChange(event: UserActionEvent) {
        // Обработка изменения состояния окна
        Log.d(TAG, "Handling window state change: ${event.packageName}")
    }

    private fun performButtonPress(event: UserActionEvent) {
        // Реализация нажатия кнопки через AccessibilityService
        Log.d(TAG, "Performing button press action")
    }

    private fun performKeyEvent(event: UserActionEvent) {
        // Реализация нажатия клавиш через AccessibilityService
        Log.d(TAG, "Performing key event action")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}