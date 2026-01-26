package com.example.myapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemHabitDashboardBinding
import com.example.myapplication.models.Habit

class HabitDashboardAdapter(
    private val onHabitToggle: (Habit) -> Unit,
    private val onHabitDelete: (Habit) -> Unit
) : ListAdapter<Habit, HabitDashboardAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitDashboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = getItem(position)
        holder.bind(habit)
    }

    inner class HabitViewHolder(private val binding: ItemHabitDashboardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(habit: Habit) {
            binding.tvHabitName.text = habit.name
            binding.cbHabitDone.isChecked = habit.isCompletedToday()

            val streak = habit.getStreak()
            binding.tvHabitStreak.text = "🔥 ${streak} day streak"
            binding.tvHabitStreak.visibility = if (streak > 0) android.view.View.VISIBLE else android.view.View.GONE

            binding.tvHabitProgressText.text = habit.getWeeklyProgress()
            binding.tvHabitProgressText.visibility = android.view.View.VISIBLE

            val progress = habit.getProgressPercentage().toInt()
            binding.pbHabitProgress.progress = progress

            val isCompleted = habit.isCompletedToday()
            updateVisualState(isCompleted)

            binding.cbHabitDone.setOnClickListener {
                val wasCompleted = habit.isCompletedToday()
                if (wasCompleted) {
                    habit.markIncomplete()
                } else {
                    habit.markCompleted()
                }
                
                updateVisualState(!wasCompleted)
                
                binding.tvHabitProgressText.text = habit.getWeeklyProgress()
                val newProgress = habit.getProgressPercentage().toInt()
                binding.pbHabitProgress.progress = newProgress
                
                val streak = habit.getStreak()
                binding.tvHabitStreak.text = "🔥 ${streak} day streak"
                binding.tvHabitStreak.visibility = if (streak > 0) android.view.View.VISIBLE else android.view.View.GONE

                onHabitToggle(habit)
            }

            binding.btnDeleteHabit.setOnClickListener {
                onHabitDelete(habit)
            }

            binding.tvHabitEmoji.text = habit.emoji
        }

        private fun updateVisualState(isCompleted: Boolean) {
            binding.cbHabitDone.isChecked = isCompleted
            
            binding.root.alpha = if (isCompleted) 0.9f else 1.0f
            
            val colorRes = if (isCompleted) com.example.myapplication.R.color.success else com.example.myapplication.R.color.primary
            binding.pbHabitProgress.progressTintList = binding.root.context.getColorStateList(colorRes)
            
            val textColorRes = if (isCompleted) com.example.myapplication.R.color.success else com.example.myapplication.R.color.text_secondary
            binding.tvHabitProgressText.setTextColor(binding.root.context.getColor(textColorRes))
            
            val nameColorRes = if (isCompleted) com.example.myapplication.R.color.text_secondary else com.example.myapplication.R.color.text_primary
            binding.tvHabitName.setTextColor(binding.root.context.getColor(nameColorRes))
            
            binding.root.animate()
                .scaleX(if (isCompleted) 0.98f else 1.0f)
                .scaleY(if (isCompleted) 0.98f else 1.0f)
                .setDuration(200)
                .withEndAction {
                    binding.root.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .start()
                }
                .start()
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
