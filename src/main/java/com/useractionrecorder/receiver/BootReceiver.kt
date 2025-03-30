package com.useractionrecorder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.useractionrecorder.service.ScheduleManager

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, restoring schedules")
            try {
                val scheduleManager = ScheduleManager.getInstance(context)
                val schedules = scheduleManager.listSchedules()
                
                schedules.forEach { schedule ->
                    if (schedule.isEnabled) {
                        // Переустанавливаем расписание
                        scheduleManager.toggleSchedule(schedule.id, true)
                    }
                }
                
                Log.d(TAG, "Successfully restored ${schedules.size} schedules")
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring schedules after boot", e)
            }
        }
    }
}