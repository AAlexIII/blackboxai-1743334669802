package com.useractionrecorder.model

import java.util.*

data class Schedule(
    val id: String = UUID.randomUUID().toString(),
    val recordingId: String,
    val recordingName: String,
    var hour: Int,
    var minute: Int,
    var isEnabled: Boolean = true,
    val createdAt: Date = Date()
) {
    fun getTimeString(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}