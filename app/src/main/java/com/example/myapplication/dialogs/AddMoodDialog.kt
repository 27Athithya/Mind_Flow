package com.example.myapplication.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogAddMoodBinding
import com.example.myapplication.models.MoodEntry
import com.example.myapplication.utils.DateTimeUtils

class AddMoodDialog(
    private val existingMood: MoodEntry? = null,
    private val onMoodSaved: (MoodEntry) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddMoodBinding? = null
    private val binding get() = _binding!!
    private var selectedMoodLevel = 3 // Default to neutral
    private var selectedEmoji = "😐"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMoodSelector()

        if (existingMood != null) {
            populateExistingMood()
            binding.tvDialogTitle.text = "View Mood Entry"
            binding.btnSave.text = getString(R.string.done)
        } else {
            binding.tvDialogTitle.text = getString(R.string.add_mood_entry)
            binding.btnSave.text = getString(R.string.save_mood)
        }

        setupClickListeners()
    }

    private fun setupMoodSelector() {
        val moodEmojis = MoodEntry.getMoodEmojis()

        binding.emoji1.text = moodEmojis[0]
        binding.emoji2.text = moodEmojis[1]
        binding.emoji3.text = moodEmojis[2]
        binding.emoji4.text = moodEmojis[3]
        binding.emoji5.text = moodEmojis[4]

        binding.emoji1.setOnClickListener { selectMood(1, moodEmojis[0]) }
        binding.emoji2.setOnClickListener { selectMood(2, moodEmojis[1]) }
        binding.emoji3.setOnClickListener { selectMood(3, moodEmojis[2]) }
        binding.emoji4.setOnClickListener { selectMood(4, moodEmojis[3]) }
        binding.emoji5.setOnClickListener { selectMood(5, moodEmojis[4]) }

        selectMood(3, moodEmojis[2])
    }

    private fun selectMood(level: Int, emoji: String) {
        selectedMoodLevel = level
        selectedEmoji = emoji

        binding.emoji1.setBackgroundResource(android.R.color.transparent)
        binding.emoji2.setBackgroundResource(android.R.color.transparent)
        binding.emoji3.setBackgroundResource(android.R.color.transparent)
        binding.emoji4.setBackgroundResource(android.R.color.transparent)
        binding.emoji5.setBackgroundResource(android.R.color.transparent)

        val selectedView = when(level) {
            1 -> binding.emoji1
            2 -> binding.emoji2
            3 -> binding.emoji3
            4 -> binding.emoji4
            5 -> binding.emoji5
            else -> binding.emoji3
        }
        selectedView.setBackgroundResource(R.drawable.mood_selector_background)

        val moodLevelText = when(level) {
            1 -> "Very Sad"
            2 -> "Sad"
            3 -> "Neutral"
            4 -> "Happy"
            5 -> "Very Happy"
            else -> "Neutral"
        }
        binding.tvMoodLevel.text = moodLevelText
    }

    private fun populateExistingMood() {
        existingMood?.let { mood ->
            selectMood(mood.moodLevel, mood.emoji)
            binding.etMoodNote.setText(mood.note ?: "")
            binding.tvDate.text = DateTimeUtils.formatDateForDisplay(mood.date)
            binding.tvTime.text = DateTimeUtils.formatTimeForDisplay(mood.time)
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveMood()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun saveMood() {
        val note = binding.etMoodNote.text.toString().trim().ifEmpty { null }

        val moodEntry = MoodEntry(
            date = DateTimeUtils.getCurrentDate(),
            time = DateTimeUtils.getCurrentTime(),
            emoji = selectedEmoji,
            moodLevel = selectedMoodLevel,
            note = note,
            tags = listOf() 
        )

        onMoodSaved(moodEntry)
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
