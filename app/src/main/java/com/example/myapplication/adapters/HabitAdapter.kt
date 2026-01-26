package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemHabitBinding
import com.example.myapplication.models.Habit
import com.mikhaellopez.circularprogressbar.CircularProgressBar

class HabitAdapter(
    private val onHabitClick: (Habit) -> Unit,
    private val onHabitToggle: (Habit) -> Unit
) : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(private val binding: ItemHabitBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            binding.apply {
                tvHabitName.text = habit.name ?: "Unnamed Habit"
                tvHabitDescription.text = if (!habit.emoji.isNullOrEmpty()) habit.emoji else "🎯"
                tvHabitProgress.text = "${habit.completionDates.size}/7 days completed this week"
                tvStreak.text = "🔥 ${habit.getStreak()}"

                val progress = habit.getProgressPercentage()
                circularProgress.progress = progress

                val isCompletedToday = habit.isCompletedToday()
                ivCheckmark.visibility = if (isCompletedToday) ViewGroup.VISIBLE else ViewGroup.GONE

                if (isCompletedToday) {
                }

                btnHabitCompleted.setIconResource(
                    if (isCompletedToday) com.example.myapplication.R.drawable.ic_check_circle else com.example.myapplication.R.drawable.ic_check_circle_outline
                )
                btnHabitCompleted.setIconTintResource(
                    if (isCompletedToday) com.example.myapplication.R.color.success else com.example.myapplication.R.color.primary
                )

                root.setOnClickListener { onHabitClick(habit) }
                btnHabitCompleted.setOnClickListener { onHabitToggle(habit) }
            }
        }
    }

    class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }
    }
}
