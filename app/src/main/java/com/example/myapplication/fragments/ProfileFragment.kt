package com.example.myapplication.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.myapplication.activities.AuthActivity
import com.example.myapplication.databinding.FragmentProfileBinding
import com.example.myapplication.utils.SharedPrefsManager
import com.example.myapplication.utils.HydrationScheduler
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: SharedPrefsManager
    private lateinit var hydrationScheduler: HydrationScheduler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPrefsManager(requireContext())
        hydrationScheduler = HydrationScheduler(requireContext())
        setupUI()
        setupClickListeners()
        setupMoodChart()
    }

    private fun setupUI() {
        val user = prefsManager.getUser()
        binding.tvUserName.text = user?.name ?: "User"
        binding.tvUserEmail.text = user?.email ?: "user@example.com"

        binding.switchHydrationReminders.isChecked = prefsManager.areHydrationRemindersEnabled()
        binding.switchDarkMode.isChecked = prefsManager.isDarkModeEnabled()

        val intervalSeconds = prefsManager.getReminderInterval()
        val intervalText = when(intervalSeconds) {
            30L -> "30 seconds"
            1800L -> "30 minutes"
            3600L -> "1 hour"
            7200L -> "2 hours"
            10800L -> "3 hours"
            else -> {
                when {
                    intervalSeconds < 60 -> "$intervalSeconds seconds"
                    intervalSeconds < 3600 -> "${intervalSeconds / 60} minutes"
                    else -> "${intervalSeconds / 3600} hours"
                }
            }
        }
        binding.tvReminderInterval.text = intervalText
    }

    private fun setupClickListeners() {
        binding.switchHydrationReminders.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setHydrationRemindersEnabled(isChecked)
            if (isChecked) {
                val interval = prefsManager.getReminderInterval()
                hydrationScheduler.scheduleHydrationReminders(interval)
            } else {
                hydrationScheduler.cancelHydrationReminders()
            }
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setDarkModeEnabled(isChecked)
            applyTheme(isChecked)
        }

        binding.layoutReminderInterval.setOnClickListener {
            showReminderIntervalDialog()
        }

        binding.layoutExportData.setOnClickListener {
            exportUserData()
        }

        binding.layoutLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setupMoodChart() {
        val chart = binding.chartMoodTrends
        val moodEntries = prefsManager.getMoodEntries()

        if (moodEntries.isEmpty()) {
            binding.tvChartDescription.text = "Start logging your moods to see trends here!"
            return
        }

        val last7Days = getLast7DaysMoodData(moodEntries)

        val entries = mutableListOf<Entry>()
        last7Days.forEachIndexed { index, moodLevel ->
            entries.add(Entry(index.toFloat(), moodLevel.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Mood Level").apply {
            color = Color.parseColor("#81C784") 
            setCircleColor(Color.parseColor("#81C784"))
            circleRadius = 4f
            lineWidth = 2f
            setDrawValues(false) 
            mode = LineDataSet.Mode.CUBIC_BEZIER 
        }

        chart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                valueFormatter = DayValueFormatter()
                granularity = 1f
                labelCount = 7
            }

            axisLeft.apply {
                axisMinimum = 0.5f
                axisMaximum = 5.5f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E0E0E0")
            }

            axisRight.isEnabled = false

            animateX(1000)
        }
    }

    private fun getLast7DaysMoodData(moodEntries: List<com.example.myapplication.models.MoodEntry>): List<Int> {
        val last7Days = mutableListOf<Int>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, -i)
            val dateStr = dateFormat.format(calendar.time)

            val moodEntry = moodEntries.find { it.date == dateStr }
            last7Days.add(moodEntry?.moodLevel ?: 3)
        }

        return last7Days
    }

    private fun showReminderIntervalDialog() {
        val options = arrayOf("30 seconds", "30 minutes", "1 hour", "2 hours", "3 hours")
        val values = arrayOf(30L, 1800L, 3600L, 7200L, 10800L) 
        val currentInterval = prefsManager.getReminderInterval()
        val currentIndex = values.indexOf(currentInterval).takeIf { it >= 0 } ?: 1

        AlertDialog.Builder(requireContext())
            .setTitle("Reminder Interval")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                prefsManager.setReminderInterval(values[which])
                binding.tvReminderInterval.text = options[which]
                
                if (prefsManager.areHydrationRemindersEnabled()) {
                    hydrationScheduler.scheduleHydrationReminders(values[which])
                }
                
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportUserData() {
        val habits = prefsManager.getHabits()
        val moods = prefsManager.getMoodEntries()
        val user = prefsManager.getUser()

        val exportData = """
            MindFlow Data Export
            ===================
            
            User: ${user?.name}
            Email: ${user?.email}
            
            Habits (${habits.size}):
            ${habits.joinToString("\n") { "- ${it.name} ${it.emoji}" }}
            
            Mood Entries (${moods.size}):
            ${moods.take(10).joinToString("\n") { "- ${it.date}: ${it.emoji} ${it.note ?: ""}" }}
            ${if (moods.size > 10) "\n... and ${moods.size - 10} more entries" else ""}
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, exportData)
            putExtra(Intent.EXTRA_SUBJECT, "MindFlow Data Export")
        }
        startActivity(Intent.createChooser(shareIntent, "Export MindFlow Data"))
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        prefsManager.logout()
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun applyTheme(isDarkModeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkModeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        requireActivity().recreate()
    }

    override fun onResume() {
        super.onResume()
        setupMoodChart() 
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class DayValueFormatter : ValueFormatter() {
    private val days = arrayOf("7d ago", "6d ago", "5d ago", "4d ago", "3d ago", "2d ago", "Today")

    override fun getFormattedValue(value: Float): String {
        return days.getOrNull(value.toInt()) ?: value.toString()
    }
}
