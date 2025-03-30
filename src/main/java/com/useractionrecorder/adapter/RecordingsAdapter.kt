package com.useractionrecorder.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.useractionrecorder.R
import com.useractionrecorder.model.Recording
import java.text.SimpleDateFormat
import java.util.*

class RecordingsAdapter(
    private val onDeleteClick: (Recording) -> Unit,
    private val onEditClick: (Recording) -> Unit
) : ListAdapter<Recording, RecordingsAdapter.RecordingViewHolder>(RecordingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recording, parent, false)
        return RecordingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecordingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.recordingName)
        private val dateText: TextView = itemView.findViewById(R.id.recordingDate)
        private val durationText: TextView = itemView.findViewById(R.id.recordingDuration)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        fun bind(recording: Recording) {
            nameText.text = recording.name
            dateText.text = dateFormat.format(recording.createdAt)
            durationText.text = formatDuration(recording.duration)

            editButton.setOnClickListener { onEditClick(recording) }
            deleteButton.setOnClickListener { onDeleteClick(recording) }
        }

        private fun formatDuration(durationMs: Long): String {
            val seconds = durationMs / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    private class RecordingDiffCallback : DiffUtil.ItemCallback<Recording>() {
        override fun areItemsTheSame(oldItem: Recording, newItem: Recording): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recording, newItem: Recording): Boolean {
            return oldItem == newItem
        }
    }
}