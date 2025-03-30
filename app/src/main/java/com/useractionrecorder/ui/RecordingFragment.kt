package com.useractionrecorder.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.useractionrecorder.R
import com.useractionrecorder.adapter.RecordingsAdapter
import com.useractionrecorder.model.Recording
import com.useractionrecorder.service.RecordingManager
import java.util.*

class RecordingFragment : Fragment() {
    private lateinit var recordButton: MaterialButton
    private lateinit var recordingsList: RecyclerView
    private lateinit var recordingIndicator: CircularProgressIndicator
    private lateinit var recordingsAdapter: RecordingsAdapter
    private var isRecording = false

    private val recordingManager: RecordingManager by lazy {
        RecordingManager.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recording, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupRecyclerView()
        loadRecordings()
    }

    private fun setupViews(view: View) {
        recordButton = view.findViewById(R.id.recordButton)
        recordingsList = view.findViewById(R.id.recordingsList)
        recordingIndicator = view.findViewById(R.id.recordingIndicator)

        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }
    }

    private fun setupRecyclerView() {
        recordingsAdapter = RecordingsAdapter(
            onDeleteClick = { recording ->
                showDeleteConfirmationDialog(recording)
            },
            onEditClick = { recording ->
                showEditDialog(recording)
            }
        )

        recordingsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordingsAdapter
        }
    }

    private fun loadRecordings() {
        val recordings = recordingManager.listRecordings()
        recordingsAdapter.submitList(recordings)
    }

    private fun startRecording() {
        if (recordingManager.startRecording()) {
            isRecording = true
            recordButton.text = getString(R.string.stop_recording)
            recordingIndicator.visibility = View.VISIBLE
            showSnackbar(getString(R.string.recording_started))
        } else {
            showSnackbar("Ошибка при запуске записи")
        }
    }

    private fun stopRecording() {
        recordingManager.stopRecording()
        isRecording = false
        recordButton.text = getString(R.string.start_recording)
        recordingIndicator.visibility = View.GONE
        showSaveRecordingDialog()
    }

    private fun showSaveRecordingDialog() {
        val input = TextView(context).apply {
            hint = getString(R.string.enter_recording_name)
            setPadding(32, 16, 32, 16)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.save_recording))
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = input.text.toString().takeIf { it.isNotEmpty() }
                    ?: "Запись ${Date()}"
                saveRecording(name)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun saveRecording(name: String) {
        if (recordingManager.saveRecording(name)) {
            showSnackbar(getString(R.string.recording_saved))
            loadRecordings()
        } else {
            showSnackbar("Ошибка при сохранении записи")
        }
    }

    private fun showDeleteConfirmationDialog(recording: Recording) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить запись?")
            .setMessage("Вы уверены, что хотите удалить запись '${recording.name}'?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteRecording(recording)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteRecording(recording: Recording) {
        if (recordingManager.deleteRecording(recording.id)) {
            loadRecordings()
            showSnackbar("Запись удалена")
        } else {
            showSnackbar("Ошибка при удалении записи")
        }
    }

    private fun showEditDialog(recording: Recording) {
        val input = TextView(context).apply {
            text = recording.name
            setPadding(32, 16, 32, 16)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Переименовать запись")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val newName = input.text.toString()
                if (newName.isNotEmpty()) {
                    renameRecording(recording, newName)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun renameRecording(recording: Recording, newName: String) {
        if (recordingManager.renameRecording(recording.id, newName)) {
            loadRecordings()
            showSnackbar("Запись переименована")
        } else {
            showSnackbar("Ошибка при переименовании записи")
        }
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }
}