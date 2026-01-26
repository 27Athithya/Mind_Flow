package com.example.myapplication.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemMoodBinding
import com.example.myapplication.models.MoodEntry
import com.example.myapplication.utils.DateTimeUtils

class MoodAdapter(
    private val onMoodClick: (MoodEntry) -> Unit,
    private val onMoodDelete: (MoodEntry) -> Unit,
    private val onMoodShare: (MoodEntry) -> Unit
) : ListAdapter<MoodEntry, MoodAdapter.MoodViewHolder>(MoodDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MoodViewHolder(private val binding: ItemMoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(moodEntry: MoodEntry) {
            binding.apply {
                tvMoodEmoji.text = moodEntry.emoji
                tvMoodDate.text = DateTimeUtils.formatDateForDisplay(moodEntry.date)
                tvMoodTime.text = DateTimeUtils.formatTimeForDisplay(moodEntry.time)
                tvMoodNote.text = moodEntry.note ?: "No note"

                val moodLevelText = when(moodEntry.moodLevel) {
                    1 -> "Very Sad"
                    2 -> "Sad"
                    3 -> "Neutral"
                    4 -> "Happy"
                    5 -> "Very Happy"
                    else -> "Unknown"
                }
                tvMoodLevel.text = moodLevelText

                val backgroundColor = when(moodEntry.moodLevel) {
                    1 -> itemView.context.getColor(com.example.myapplication.R.color.mood_very_sad)
                    2 -> itemView.context.getColor(com.example.myapplication.R.color.mood_sad)
                    3 -> itemView.context.getColor(com.example.myapplication.R.color.mood_neutral)
                    4 -> itemView.context.getColor(com.example.myapplication.R.color.mood_happy)
                    5 -> itemView.context.getColor(com.example.myapplication.R.color.mood_very_happy)
                    else -> itemView.context.getColor(com.example.myapplication.R.color.surface)
                }
                moodIndicator.setBackgroundColor(backgroundColor)

                root.setOnClickListener { onMoodClick(moodEntry) }
                btnDeleteMood.setOnClickListener { onMoodDelete(moodEntry) }
                btnShareMood.setOnClickListener { onMoodShare(moodEntry) }
            }
        }
    }

    class MoodDiffCallback : DiffUtil.ItemCallback<MoodEntry>() {
        override fun areItemsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem == newItem
        }
    }
}
