package com.useractionrecorder.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.useractionrecorder.model.Schedule
import java.io.File
import java.util.*

class ScheduleManager private constructor(private val context: Context) {
    private val schedules = mutableListOf<Schedule>()
    private val gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create()
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val schedulesDir: File
        get() = File(context.filesDir, "schedules").apply { mkdirs() }

    companion object {
        private const val TAG = "ScheduleManager"
        private const val ALARM_REQUEST_CODE_BASE = 1000
        @Volatile
        private var instance: ScheduleManager? = null

        fun getInstance(context: Context): ScheduleManager {
            return instance ?: synchronized(this) {
                instance ?: ScheduleManager(context.applicationContext).also {
                    instance = it
                    it.loadSchedules()
                }
            }
        }
    }

    fun addSchedule(schedule: Schedule): Boolean {
        return try {
            schedules.add(schedule)
            saveScheduleToFile(schedule)
            if (schedule.isEnabled) {
                scheduleAlarm(schedule)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding schedule", e)
            false
        }
    }

    fun deleteSchedule(id: String): Boolean {
        return try {
            val schedule = schedules.find { it.id == id }
            schedule?.let {
                schedules.remove(it)
                getScheduleFile(it.id).delete()
                cancelAlarm(it)
                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting schedule", e)
            false
        }
    }

    fun toggleSchedule(id: String, isEnabled: Boolean): Boolean {
        return try {
            val schedule = schedules.find { it.id == id }
            schedule?.let {
                it.isEnabled = isEnabled
                saveScheduleToFile(it)
                if (isEnabled) {
                    scheduleAlarm(it)
                } else {
                    cancelAlarm(it)
                }
                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling schedule", e)
            false
        }
    }

    fun listSchedules(): List<Schedule> {
        return schedules.sortedBy { it.hour * 60 + it.minute }
    }

    private fun loadSchedules() {
        try {
            schedules.clear()
            schedulesDir.listFiles()?.forEach { file ->
                try {
                    val json = file.readText()
                    val schedule = gson.fromJson(json, Schedule::class.java)
                    schedules.add(schedule)
                    if (schedule.isEnabled) {
                        scheduleAlarm(schedule)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading schedule from file: ${file.name}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading schedules", e)
        }
    }

    private fun saveScheduleToFile(schedule: Schedule) {
        try {
            val json = gson.toJson(schedule)
            getScheduleFile(schedule.id).writeText(json)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving schedule to file", e)
            throw e
        }
    }

    private fun getScheduleFile(id: String): File {
        return File(schedulesDir, "$id.json")
    }

    private fun scheduleAlarm(schedule: Schedule) {
        val intent = Intent(context, PlaybackService::class.java).apply {
            putExtra("recordingId", schedule.recordingId)
        }

        val pendingIntent = PendingIntent.getService(
            context,
            getRequestCode(schedule),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
            
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelAlarm(schedule: Schedule) {
        val intent = Intent(context, PlaybackService::class.java)
        val pendingIntent = PendingIntent.getService(
            context,
            getRequestCode(schedule),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun getRequestCode(schedule: Schedule): Int {
        return ALARM_REQUEST_CODE_BASE + schedule.id.hashCode()
    }
}