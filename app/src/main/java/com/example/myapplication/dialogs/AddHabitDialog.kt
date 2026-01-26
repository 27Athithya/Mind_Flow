package com.example.myapplication.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.DialogAddHabitBinding
import com.example.myapplication.models.Habit
import com.example.myapplication.utils.EmojiPickerDialog
import java.util.*

class AddHabitDialog(
    private val existingHabit: Habit? = null,
    private val onHabitSaved: (Habit) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddHabitBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFrequencySpinner()

        if (existingHabit != null) {
            populateExistingHabit()
        }

        setupClickListeners()
    }

    private fun setupFrequencySpinner() {
        val frequencies = arrayOf("Daily", "Weekly", "Monthly")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, frequencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFrequency.adapter = adapter
    }

    private fun populateExistingHabit() {
        existingHabit?.let { habit ->
            binding.etHabitName.setText(habit.name)
            binding.tvSelectedEmoji.text = habit.emoji

            val frequencyIndex = when (habit.frequency.lowercase()) {
                "daily" -> 0
                "weekly" -> 1
                "monthly" -> 2
                else -> 0
            }
            binding.spinnerFrequency.setSelection(frequencyIndex)
        }
    }

    private fun setupClickListeners() {
        var selectedEmoji = if (existingHabit != null) existingHabit?.emoji ?: "🎯" else "🎯"

        binding.tvSelectedEmoji.text = selectedEmoji
        binding.btnPickEmoji.setOnClickListener {
            val emojiPicker = EmojiPickerDialog(requireContext()) { emoji ->
                selectedEmoji = emoji
                binding.tvSelectedEmoji.text = emoji
            }
            emojiPicker.show()
        }

        binding.btnAdd.setOnClickListener {
            saveHabit(selectedEmoji)
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun saveHabit(selectedEmoji: String) {
        val name = binding.etHabitName.text.toString().trim()

        if (name.isEmpty()) {
            binding.etHabitName.error = "Please enter a habit name"
            return
        }

        val frequency = binding.spinnerFrequency.selectedItem.toString()

        val habit = if (existingHabit != null) {
            existingHabit.copy(
                name = name,
                emoji = selectedEmoji,
                frequency = frequency
            )
        } else {
            Habit(
                name = name,
                emoji = selectedEmoji,
                icon = selectedEmoji,
                frequency = frequency.lowercase(),
                reminderTime = null,
                createdDate = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
        }

        onHabitSaved(habit)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
