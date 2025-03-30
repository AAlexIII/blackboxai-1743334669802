package com.useractionrecorder.model

import java.util.*

data class Recording(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    val createdAt: Date = Date(),
    var duration: Long = 0,
    val events: MutableList<UserActionEvent> = mutableListOf()
)

data class UserActionEvent(
    val type: EventType,
    val timestamp: Long,
    val packageName: String?,
    val className: String?,
    val text: String?,
    val contentDescription: String?,
    val bounds: String?,
    val action: Int
)

enum class EventType {
    CLICK,
    LONG_CLICK,
    SCROLL,
    TEXT_CHANGE,
    WINDOW_STATE_CHANGED,
    BUTTON_PRESS,
    KEY_EVENT
}