package com.example.myapplication.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentStepCounterBinding
import com.example.myapplication.utils.SharedPrefsManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class StepCounterFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentStepCounterBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefsManager: SharedPrefsManager
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null

    private var lastUpdateTime = 0L
    private var lastStepCount = 0
    private val UPDATE_INTERVAL_MS = 1000L 

    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStepCounterBinding.inflate(inflater, container, false)
        prefsManager = SharedPrefsManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStepCounter()
        setupClickListeners()
        updateAllUI()
        setupWeeklyChart()
    }

    private fun setupStepCounter() {
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            Toast.makeText(requireContext(), "Step counter sensor not available", Toast.LENGTH_SHORT).show()
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), PERMISSION_REQUEST_CODE)
            } else {
                sensorManager?.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSetGoal.setOnClickListener {
            val goalText = binding.etGoal.text.toString()
            if (goalText.isNotEmpty()) {
                val goal = goalText.toInt()
                prefsManager.setStepGoal(goal)
                updateAllUI()
                Toast.makeText(requireContext(), "Goal updated!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter a valid goal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateAllUI() {
        val stepCount = prefsManager.getStepCountToday()
        val stepGoal = prefsManager.getStepGoal()

        binding.tvStepCount.text = stepCount.toString()
        binding.tvStepGoal.text = "Goal: $stepGoal"
        binding.progressSteps.progress = stepCount.toFloat()
        binding.progressSteps.progressMax = stepGoal.toFloat()

        val distance = (stepCount * 0.762) / 1000 
        val calories = stepCount * 0.04 

        binding.tvKilometers.text = String.format("%.2f km", distance)
        binding.tvCalories.text = String.format("%.0f kcal", calories)
    }

    private fun setupWeeklyChart() {
        val weeklySteps = prefsManager.getStepCountForLast7Days()
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        weeklySteps.forEachIndexed { index: Int, pair: Pair<String, Int> ->
            entries.add(BarEntry(index.toFloat(), pair.second.toFloat()))
            labels.add(pair.first.substring(5)) 
        }

        val dataSet = BarDataSet(entries, "Daily Steps").apply {
            color = ContextCompat.getColor(requireContext(), com.example.myapplication.R.color.primary)
            valueTextColor = ContextCompat.getColor(requireContext(), com.example.myapplication.R.color.text_primary)
            valueTextSize = 12f
        }

        binding.barChart.data = BarData(dataSet)
        binding.barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
        }
        binding.barChart.axisLeft.axisMinimum = 0f
        binding.barChart.axisRight.isEnabled = false
        binding.barChart.description.isEnabled = false
        binding.barChart.animateY(1000)
        binding.barChart.invalidate()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            val currentTime = System.currentTimeMillis()

            if (steps != lastStepCount && currentTime - lastUpdateTime > UPDATE_INTERVAL_MS) {
                lastStepCount = steps
                lastUpdateTime = currentTime

                Thread {
                    prefsManager.saveStepCount(steps)
                }.start()

                activity?.runOnUiThread {
                    updateStepCountOnly(steps)
                }
            }
        }
    }

    private fun updateStepCountOnly(stepCount: Int) {
        val stepGoal = prefsManager.getStepGoal()

        binding.tvStepCount.text = stepCount.toString()
        binding.progressSteps.progress = stepCount.toFloat()

        if (binding.tvKilometers.visibility == View.VISIBLE) {
            val distance = (stepCount * 0.762) / 1000
            val calories = stepCount * 0.04

            binding.tvKilometers.text = String.format("%.2f km", distance)
            binding.tvCalories.text = String.format("%.0f kcal", calories)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sensorManager?.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        updateAllUI()
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
