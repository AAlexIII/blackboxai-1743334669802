package com.useractionrecorder.service

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.useractionrecorder.model.Recording
import com.useractionrecorder.model.UserActionEvent
import java.io.File
import java.util.*

class RecordingManager private constructor(private val context: Context) {
    private var currentRecording: Recording? = null
    private val recordings = mutableListOf<Recording>()
    private val gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create()
    private val recordingsDir: File
        get() = File(context.filesDir, "recordings").apply { mkdirs() }

    companion object {
        private const val TAG = "RecordingManager"
        @Volatile
        private var instance: RecordingManager? = null

        fun getInstance(context: Context): RecordingManager {
            return instance ?: synchronized(this) {
                instance ?: RecordingManager(context.applicationContext).also {
                    instance = it
                    it.loadRecordings()
                }
            }
        }
    }

    fun startRecording(): Boolean {
        return try {
            currentRecording = Recording(name = "Временная запись")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            false
        }
    }

    fun stopRecording() {
        currentRecording?.let {
            it.duration = System.currentTimeMillis() - it.createdAt.time
        }
        currentRecording = null
    }

    fun addEvent(event: UserActionEvent) {
        currentRecording?.events?.add(event)
    }

    fun saveRecording(name: String): Boolean {
        return try {
            currentRecording?.let { recording ->
                recording.name = name
                recordings.add(recording)
                saveRecordingToFile(recording)
                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error saving recording", e)
            false
        }
    }

    fun deleteRecording(id: String): Boolean {
        return try {
            val recording = recordings.find { it.id == id }
            recording?.let {
                recordings.remove(it)
                getRecordingFile(it.id).delete()
                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting recording", e)
            false
        }
    }

    fun renameRecording(id: String, newName: String): Boolean {
        return try {
            val recording = recordings.find { it.id == id }
            recording?.let {
                it.name = newName
                saveRecordingToFile(it)
                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming recording", e)
            false
        }
    }

    fun listRecordings(): List<Recording> {
        return recordings.sortedByDescending { it.createdAt }
    }

    fun getRecording(id: String): Recording? {
        return recordings.find { it.id == id }
    }

    private fun loadRecordings() {
        try {
            recordings.clear()
            recordingsDir.listFiles()?.forEach { file ->
                try {
                    val json = file.readText()
                    val recording = gson.fromJson(json, Recording::class.java)
                    recordings.add(recording)
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading recording from file: ${file.name}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading recordings", e)
        }
    }

    private fun saveRecordingToFile(recording: Recording) {
        try {
            val json = gson.toJson(recording)
            getRecordingFile(recording.id).writeText(json)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving recording to file", e)
            throw e
        }
    }

    private fun getRecordingFile(id: String): File {
        return File(recordingsDir, "$id.json")
    }
}