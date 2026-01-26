package com.example.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapters.MoodAdapter
import com.example.myapplication.databinding.FragmentMoodJournalBinding
import com.example.myapplication.dialogs.AddMoodDialog
import com.example.myapplication.models.MoodEntry
import com.example.myapplication.utils.SharedPrefsManager

class MoodJournalFragment : Fragment() {

    private var _binding: FragmentMoodJournalBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: SharedPrefsManager
    private lateinit var moodAdapter: MoodAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoodJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPrefsManager(requireContext())
        setupRecyclerView()
        setupClickListeners()
        loadMoodEntries()
    }

    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(
            onMoodClick = { mood -> viewMoodDetails(mood) },
            onMoodDelete = { mood -> deleteMoodEntry(mood) },
            onMoodShare = { mood -> shareMoodEntry(mood) }
        )

        binding.recyclerViewMoods.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = moodAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddMood.setOnClickListener {
            showAddMoodDialog()
        }
    }

    private fun loadMoodEntries() {
        val moodEntries = prefsManager.getMoodEntries()
        moodAdapter.submitList(moodEntries)

        if (moodEntries.isEmpty()) {
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.recyclerViewMoods.visibility = View.GONE
        } else {
            binding.layoutEmptyState.visibility = View.GONE
            binding.recyclerViewMoods.visibility = View.VISIBLE
        }
    }

    private fun showAddMoodDialog() {
        val dialog = AddMoodDialog { moodEntry ->
            prefsManager.addMoodEntry(moodEntry)
            loadMoodEntries()
        }
        dialog.show(parentFragmentManager, "AddMoodDialog")
    }

    private fun viewMoodDetails(mood: MoodEntry) {
        val dialog = AddMoodDialog(mood) { updatedMood ->
            loadMoodEntries()
        }
        dialog.show(parentFragmentManager, "ViewMoodDialog")
    }

    private fun deleteMoodEntry(mood: MoodEntry) {
        prefsManager.deleteMoodEntry(mood.id)
        loadMoodEntries()
    }

    private fun shareMoodEntry(mood: MoodEntry) {
        val shareText = buildString {
            append("🌟 My Mood Today 🌟\n\n")
            append("${mood.emoji} ${getMoodLevelText(mood.moodLevel)}\n")
            append("Date: ${mood.date}\n")
            append("Time: ${mood.time}\n")
            if (!mood.note.isNullOrEmpty()) {
                append("Note: ${mood.note}\n")
            }
            append("\nShared from WellnessHub - Daily Wellness Companion 💚")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "My Mood - ${mood.date}")
        }

        startActivity(Intent.createChooser(shareIntent, "Share Mood Entry"))
    }

    private fun getMoodLevelText(level: Int): String {
        return when(level) {
            1 -> "Very Sad"
            2 -> "Sad"
            3 -> "Neutral"
            4 -> "Happy"
            5 -> "Very Happy"
            else -> "Unknown"
        }
    }

    override fun onResume() {
        super.onResume()
        loadMoodEntries()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
