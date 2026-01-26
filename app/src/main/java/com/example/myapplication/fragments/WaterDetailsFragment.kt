package com.example.myapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.graphics.Color
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentWaterDetailsBinding
import com.example.myapplication.utils.SharedPrefsManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class WaterDetailsFragment : Fragment() {

    private var _binding: FragmentWaterDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: SharedPrefsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaterDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPrefsManager(requireContext())
        setupToolbar()
        updateUI()
    }

    private fun updateUI() {
        updateTodaysProgress()
        setupChart()
        updateStatistics()
        updateSettings()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_waterDetailsFragment_to_navigation_home)
        }
    }

    private fun updateTodaysProgress() {
        val todayIntake = prefsManager.getWaterIntakeToday()
        val dailyGoal = prefsManager.getDailyWaterLimit()
        val progress = if (dailyGoal > 0) (todayIntake * 100 / dailyGoal) else 0

        binding.tvTodayIntake.text = "${todayIntake}ml/${dailyGoal}ml"
        binding.progressTodayWater.progress = progress
    }

    private fun setupChart() {
        val waterIntakeData = prefsManager.getWaterIntakeForLast7Days()

        val entries = waterIntakeData.mapIndexed { index, pair ->
            BarEntry(index.toFloat(), pair.second.toFloat())
        }

        val dataSet = BarDataSet(entries, "Water Intake").apply {
            color = ContextCompat.getColor(requireContext(), R.color.secondary)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
            valueTextSize = 12f
        }

        val barData = BarData(dataSet)
        binding.chartWaterIntake.data = barData

        val dates = waterIntakeData.map { it.first.substring(5) } 
        binding.chartWaterIntake.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dates)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        }

        binding.chartWaterIntake.axisLeft.apply {
            axisMinimum = 0f
            setDrawGridLines(true)
            gridColor = ContextCompat.getColor(requireContext(), R.color.surface_variant)
            textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        }

        binding.chartWaterIntake.axisRight.isEnabled = false
        binding.chartWaterIntake.description.isEnabled = false
        binding.chartWaterIntake.legend.isEnabled = false
        binding.chartWaterIntake.animateY(1200)
        binding.chartWaterIntake.invalidate()
    }

    private fun updateStatistics() {
        val last7DaysIntake = prefsManager.getWaterIntakeForLast7Days()
        val average = last7DaysIntake.map { it.second }.average()
        binding.tvWeeklyAverage.text = String.format("Weekly Average: %.0fml/day", average)
    }

    private fun updateSettings() {
        val dailyGoal = prefsManager.getDailyWaterLimit()
        binding.tvDailyGoal.text = "${dailyGoal}ml"

        binding.layoutDailyGoal.setOnClickListener {
            showEditGoalDialog()
        }
    }

    private fun showEditGoalDialog() {
        val editText = TextInputEditText(requireContext())
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        editText.setText(prefsManager.getDailyWaterLimit().toString())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Daily Water Goal")
            .setMessage("Enter your daily water intake goal in milliliters (ml).")
            .setView(editText, 50, 20, 50, 20)
            .setPositiveButton("Save") { dialog, _ ->
                val newGoal = editText.text.toString().toIntOrNull() ?: prefsManager.getDailyWaterLimit()
                prefsManager.setDailyWaterLimit(newGoal)
                updateUI()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
