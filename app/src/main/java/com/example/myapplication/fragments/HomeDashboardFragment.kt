package com.example.myapplication.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeDashboardBinding
import com.example.myapplication.adapters.HabitDashboardAdapter
import com.example.myapplication.dialogs.AddHabitDialog
import com.example.myapplication.dialogs.AddWaterDialog
import com.example.myapplication.models.Habit
import com.example.myapplication.models.MoodEntry
import com.example.myapplication.utils.SharedPrefsManager
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlinx.coroutines.*

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat

class HomeDashboardFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentHomeDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var habitAdapter: HabitDashboardAdapter
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    private val PERMISSION_REQUEST_CODE = 100

    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var lastUIUpdateTime = 0L
    private val UI_UPDATE_INTERVAL_MS = 2000L 

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWelcomeMessage()
        setupClickListeners()

        loadDataAsync()
    }

    private fun loadDataAsync() {
        backgroundScope.launch {
            try {
                val prefsManager = SharedPrefsManager(requireContext())

                val habits = prefsManager.getHabits()
                val moodEntries = prefsManager.getMoodEntries()
                val waterIntake = prefsManager.getWaterIntakeToday()
                val waterLimit = prefsManager.getDailyWaterLimit()

                withContext(Dispatchers.Main) {
                    if (_binding != null) {
                        updateProgressWithData(habits, waterIntake, waterLimit)
                        setupMoodChartWithData(moodEntries)
                        updateTodaysMood()
                        setupHabitsRecyclerView()
                        loadHabitsWithData(habits)
                        setupStepCounter()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateProgressWithData(habits: List<Habit>, waterIntake: Int, waterLimit: Int) {
        val completedToday = habits.count { it.isCompletedToday() }
        val totalHabits = habits.size
        val progressPercentage = if (totalHabits > 0) (completedToday.toFloat() / totalHabits * 100).toInt() else 0

        binding.progressHabits.progress = progressPercentage.toFloat()
        binding.tvHabitProgress.text = "$progressPercentage%"

        val waterPercentage = if (waterLimit > 0) (waterIntake.toFloat() / waterLimit * 100).toInt() else 0
        binding.progressWater.progress = waterPercentage.toFloat()
        binding.tvWaterPercentage.text = "$waterPercentage%"
        binding.tvWaterIntake.text = "${waterIntake}ml/${waterLimit}ml"
    }

    private fun setupWelcomeMessage() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            in 18..21 -> "Good Evening"
            else -> "Good Night"
        }

        backgroundScope.launch {
            val user = SharedPrefsManager(requireContext()).getUser()
            val name = user?.name?.substringBefore(" ") ?: "User"

            withContext(Dispatchers.Main) {
                if (_binding != null) {
                    binding.tvWelcomeMessage.text = "$greeting, $name! 👋"
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAddWater.setOnClickListener {
            showAddWaterDialog()
        }

        binding.btnLogMood.setOnClickListener {
            showAddMoodDialog()
        }

        binding.btnViewMoreWater.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_navigation_home_to_waterDetailsFragment)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.btnViewAllHabits.setOnClickListener {
            findNavController().navigate(R.id.navigation_habits)
        }

        binding.fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun updateProgress() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUIUpdateTime < UI_UPDATE_INTERVAL_MS) {
            return
        }
        lastUIUpdateTime = currentTime

        backgroundScope.launch {
            val prefsManager = SharedPrefsManager(requireContext())
            val habits = prefsManager.getHabits()
            val waterIntake = prefsManager.getWaterIntakeToday()
            val waterLimit = prefsManager.getDailyWaterLimit()

            withContext(Dispatchers.Main) {
                if (_binding != null) {
                    updateProgressWithData(habits, waterIntake, waterLimit)
                }
            }
        }
    }

    private fun showAddWaterDialog() {
        val dialog = AddWaterDialog {
            updateProgress()
        }
        dialog.show(parentFragmentManager, "AddWaterDialog")
    }

    private fun setupMoodChartWithData(moodEntries: List<MoodEntry>) {
        if (moodEntries.isEmpty()) {
            binding.chartMoodTrend.visibility = View.GONE
            return
        }

        binding.chartMoodTrend.visibility = View.VISIBLE

        backgroundScope.launch {
            val todayMoods = getTodaysMoodData(moodEntries)

            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext

                if (todayMoods.size > 1) {
                    val chartEntries = todayMoods.mapIndexed { index, moodEntry ->
                        Entry(index.toFloat(), moodEntry.moodLevel.toFloat())
                    }
                    val timeLabels = todayMoods.map { it.time }
                    setupChartUI(chartEntries, timeLabels)
                } else {
                    setupWeeklyMoodChart(moodEntries)
                }
            }
        }
    }

    private fun setupChartUI(chartEntries: List<Entry>, timeLabels: List<String>) {

        val dataSet = LineDataSet(chartEntries, "Today's Mood").apply {
            color = Color.parseColor("#81C784")
            setCircleColor(Color.parseColor("#81C784"))
            lineWidth = 2f
            circleRadius = 6f
            setDrawFilled(false) 
            valueTextSize = 12f
            setDrawValues(false) 
            mode = LineDataSet.Mode.LINEAR 
        }

        val lineData = LineData(dataSet)
        binding.chartMoodTrend.data = lineData

        binding.chartMoodTrend.apply {
            description.isEnabled = false
            setTouchEnabled(false) 
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(timeLabels.take(5)) 
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false) 
                granularity = 1f
                textSize = 10f
            }

            axisLeft.apply {
                axisMinimum = 0.5f
                axisMaximum = 5.5f
                setDrawGridLines(false) 
                textSize = 11f
                setLabelCount(5, true)
            }

            axisRight.isEnabled = false
            legend.isEnabled = false 

            invalidate()
        }
    }

    private fun getTodaysMoodData(allMoodEntries: List<MoodEntry>): List<MoodEntry> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val todayEntries = allMoodEntries.filter { it.date == today }
            .sortedBy { entry ->
                try {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    timeFormat.parse(entry.time)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }

        return todayEntries
    }

    private fun setupWeeklyMoodChart(moodEntries: List<MoodEntry>) {
        val last7Days = getLast7DaysData(moodEntries)
        val chartEntries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()

        last7Days.forEachIndexed { index, (date, avgMood) ->
            chartEntries.add(Entry(index.toFloat(), avgMood))
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = sdf.parse(date)
                val displayFormat = SimpleDateFormat("EEE dd", Locale.getDefault())
                dateLabels.add(displayFormat.format(parsedDate ?: Date()))
            } catch (e: Exception) {
                dateLabels.add(date.substring(5)) 
            }
        }

        val dataSet = LineDataSet(chartEntries, "Weekly Mood Trend").apply {
            color = Color.parseColor("#81C784") 
            setCircleColor(Color.parseColor("#81C784"))
            lineWidth = 3f
            circleRadius = 6f
            setDrawFilled(true)
            fillColor = Color.parseColor("#E8F5E8") 
            valueTextSize = 12f
            setDrawValues(true)

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when (value.toInt()) {
                        1 -> "😢"
                        2 -> "😔"
                        3 -> "😐"
                        4 -> "😊"
                        5 -> "😄"
                        else -> value.toInt().toString()
                    }
                }
            }
        }

        val lineData = LineData(dataSet)
        binding.chartMoodTrend.data = lineData

        binding.chartMoodTrend.apply {
            description.isEnabled = true
            description.text = "7-Day Mood Overview"
            description.textSize = 12f
            description.textColor = Color.parseColor("#666666")
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)  
            setPinchZoom(false)
            setDrawGridBackground(false)

            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dateLabels)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textSize = 10f
            }

            axisLeft.apply {
                axisMinimum = 0.5f
                axisMaximum = 5.5f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E0E0E0")
                textSize = 10f

                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (value.toInt()) {
                            1 -> "Very Sad"
                            2 -> "Sad"
                            3 -> "Neutral"
                            4 -> "Happy"
                            5 -> "Very Happy"
                            else -> ""
                        }
                    }
                }
            }

            axisRight.isEnabled = false
            legend.isEnabled = false

            animateX(1000)

            invalidate()
        }
    }

    private fun getLast7DaysData(moodEntries: List<MoodEntry>): List<Pair<String, Float>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val last7Days = mutableListOf<String>()
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            last7Days.add(dateFormat.format(calendar.time))
        }

        return last7Days.map { date ->
            val dayMoods = moodEntries.filter { it.date == date }
            val avgMood = if (dayMoods.isNotEmpty()) {
                dayMoods.map { it.moodLevel }.average().toFloat()
            } else {
                3.0f 
            }
            Pair(date, avgMood)
        }
    }

    private fun updateTodaysMood() {
        val prefsManager = com.example.myapplication.utils.SharedPrefsManager(requireContext())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val todayMoods = prefsManager.getMoodEntries().filter { it.date == today }

        if (todayMoods.isNotEmpty()) {
            val latestMood = todayMoods.first() 
            binding.tvTodaysMood.text = "${latestMood.emoji} ${getMoodText(latestMood.moodLevel)}"
            binding.tvMoodNote.text = latestMood.note ?: "No note added"
        } else {
            binding.tvTodaysMood.text = "😐 No mood logged today"
            binding.tvMoodNote.text = "Tap 'Log Mood' to add your mood"
        }
    }

    private fun getMoodText(level: Int): String {
        return when (level) {
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
        loadDataAsync() 
    }

    private fun setupHabitsRecyclerView() {
        habitAdapter = HabitDashboardAdapter(
            onHabitToggle = { habit ->
                com.example.myapplication.utils.SharedPrefsManager(requireContext()).updateHabit(habit)
                updateProgress()
                loadHabits()
            },
            onHabitDelete = { habit ->
                com.example.myapplication.utils.SharedPrefsManager(requireContext()).deleteHabit(habit.id)
                updateProgress()
                loadHabits() 
            }
        )
        binding.rvHabitsDashboard.apply {
            adapter = habitAdapter
            layoutManager = WrapContentLinearLayoutManager(requireContext()).apply {
                isAutoMeasureEnabled = true
            }
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
            itemAnimator = null 
        }
    }

    private fun loadHabits() {
        val habits = com.example.myapplication.utils.SharedPrefsManager(requireContext()).getHabits()
        habitAdapter.submitList(habits)
    }

    private fun loadHabitsWithData(habits: List<Habit>) {
        habitAdapter.submitList(habits)
    }

    private fun showAddHabitDialog() {
        val dialog = com.example.myapplication.dialogs.AddHabitDialog { habit: com.example.myapplication.models.Habit ->
            val prefsManagerLocal = com.example.myapplication.utils.SharedPrefsManager(requireContext())
            prefsManagerLocal.addHabit(habit)
            loadHabits() 
        }
        dialog.show(parentFragmentManager, "AddHabitDialog")
    }

    private fun showAddMoodDialog() {
        val dialog = com.example.myapplication.dialogs.AddMoodDialog { moodEntry: com.example.myapplication.models.MoodEntry ->
            val prefsManagerLocal = com.example.myapplication.utils.SharedPrefsManager(requireContext())
            prefsManagerLocal.addMoodEntry(moodEntry)
            loadDataAsync()
        }
        dialog.show(parentFragmentManager, "AddMoodDialog")
    }

    private fun setupStepCounter() {
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            binding.stepCounterLayout.tvStepCount.text = "Not available"
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), PERMISSION_REQUEST_CODE)
            } else {
                sensorManager?.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            }
        }

        updateStepCountUI()
    }

    private fun updateStepCountUI() {
        val prefsManager = com.example.myapplication.utils.SharedPrefsManager(requireContext())
        val stepCount = prefsManager.getStepCountToday()
        val stepGoal = 10000 
        val percentage = if (stepGoal > 0) (stepCount.toFloat() / stepGoal * 100).toInt() else 0

        binding.stepCounterLayout.tvStepCount.text = "$stepCount/$stepGoal steps"
        binding.stepCounterLayout.progressSteps.progress = stepCount.toFloat()
        binding.stepCounterLayout.tvStepsPercentage.text = "$percentage%"
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val steps = event.values[0].toInt()
            val prefsManager = com.example.myapplication.utils.SharedPrefsManager(requireContext())
            prefsManager.saveStepCount(steps)
            updateStepCountUI()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                sensorManager?.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            } else {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sensorManager?.unregisterListener(this)
        _binding = null
    }

    class WrapContentLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
        override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (e: IndexOutOfBoundsException) {
            }
        }

        override fun supportsPredictiveItemAnimations(): Boolean {
            return false 
        }
    }
}
