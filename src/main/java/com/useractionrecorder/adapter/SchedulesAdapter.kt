package com.useractionrecorder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.useractionrecorder.R
import com.useractionrecorder.model.Schedule

class SchedulesAdapter(
    private val onDeleteClick: (Schedule) -> Unit,
    private val onSwitchChanged: (Schedule, Boolean) -> Unit
) : ListAdapter<Schedule, SchedulesAdapter.ScheduleViewHolder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recordingNameText: TextView = itemView.findViewById(R.id.recordingNameText)
        private val scheduleTimeText: TextView = itemView.findViewById(R.id.scheduleTimeText)
        private val scheduleSwitch: SwitchMaterial = itemView.findViewById(R.id.scheduleSwitch)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteScheduleButton)

        fun bind(schedule: Schedule) {
            recordingNameText.text = schedule.recordingName
            scheduleTimeText.text = schedule.getTimeString()
            
            scheduleSwitch.isChecked = schedule.isEnabled
            scheduleSwitch.setOnCheckedChangeListener { _, isChecked ->
                onSwitchChanged(schedule, isChecked)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(schedule)
            }
        }
    }

    private class ScheduleDiffCallback : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem == newItem
        }
    }
}