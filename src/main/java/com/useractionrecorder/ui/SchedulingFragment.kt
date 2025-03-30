package com.useractionrecorder.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.useractionrecorder.R
import com.useractionrecorder.adapter.SchedulesAdapter
import com.useractionrecorder.model.Recording
import com.useractionrecorder.model.Schedule
import com.useractionrecorder.service.RecordingManager
import com.useractionrecorder.service.ScheduleManager
import java.util.*

class SchedulingFragment : Fragment() {
    private lateinit var schedulesList: RecyclerView
    private lateinit var addScheduleButton: ExtendedFloatingActionButton
    private lateinit var emptyView: TextView
    private lateinit var schedulesAdapter: SchedulesAdapter

    private val recordingManager: RecordingManager by lazy {
        RecordingManager.getInstance(requireContext())
    }

    private val scheduleManager: ScheduleManager by lazy {
        ScheduleManager.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scheduling, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupRecyclerView()
        loadSchedules()
    }

    private fun setupViews(view: View) {
        schedulesList = view.findViewById(R.id.schedulesList)
        addScheduleButton = view.findViewById(R.id.addScheduleButton)
        emptyView = view.findViewById(R.id.emptyView)

        addScheduleButton.setOnClickListener {
            showAddScheduleDialog()
        }
    }

    private fun setupRecyclerView() {
        schedulesAdapter = SchedulesAdapter(
            onDeleteClick = { schedule ->
                showDeleteConfirmationDialog(schedule)
            },
            onSwitchChanged = { schedule, isEnabled ->
                toggleSchedule(schedule, isEnabled)
            }
        )

        schedulesList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = schedulesAdapter
        }
    }

    private fun loadSchedules() {
        val schedules = scheduleManager.listSchedules()
        schedulesAdapter.submitList(schedules)
        updateEmptyView(schedules.isEmpty())
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        schedulesList.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showAddScheduleDialog() {
        val recordings = recordingManager.listRecordings()
        if (recordings.isEmpty()) {
            showSnackbar("Нет доступных записей")
            return
        }

        val recordingNames = recordings.map { it.name }.toTypedArray()
        var selectedRecording: Recording? = null

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выберите запись")
            .setItems(recordingNames) { _, which ->
                selectedRecording = recordings[which]
                showTimePickerDialog { hour, minute ->
                    selectedRecording?.let { recording ->
                        createSchedule(recording, hour, minute)
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showTimePickerDialog(onTimeSelected: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun createSchedule(recording: Recording, hour: Int, minute: Int) {
        val schedule = Schedule(
            recordingId = recording.id,
            recordingName = recording.name,
            hour = hour,
            minute = minute,
            isEnabled = true
        )

        if (scheduleManager.addSchedule(schedule)) {
            loadSchedules()
            showSnackbar("Расписание добавлено")
        } else {
            showSnackbar("Ошибка при добавлении расписания")
        }
    }

    private fun showDeleteConfirmationDialog(schedule: Schedule) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить расписание?")
            .setMessage("Вы уверены, что хотите удалить расписание для '${schedule.recordingName}'?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteSchedule(schedule)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteSchedule(schedule: Schedule) {
        if (scheduleManager.deleteSchedule(schedule.id)) {
            loadSchedules()
            showSnackbar("Расписание удалено")
        } else {
            showSnackbar("Ошибка при удалении расписания")
        }
    }

    private fun toggleSchedule(schedule: Schedule, isEnabled: Boolean) {
        if (scheduleManager.toggleSchedule(schedule.id, isEnabled)) {
            loadSchedules()
            showSnackbar(if (isEnabled) "Расписание включено" else "Расписание отключено")
        } else {
            showSnackbar("Ошибка при изменении состояния расписания")
        }
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }
}