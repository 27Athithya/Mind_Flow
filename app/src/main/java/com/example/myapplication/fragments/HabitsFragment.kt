package com.example.myapplication.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import com.example.myapplication.R
import com.example.myapplication.adapters.HabitAdapter
import com.google.android.material.snackbar.Snackbar
import com.example.myapplication.databinding.FragmentHabitsBinding
import com.example.myapplication.dialogs.AddHabitDialog
import com.example.myapplication.models.Habit
import com.example.myapplication.utils.SharedPrefsManager
import java.time.LocalDate

class HabitsFragment : Fragment() {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!

    private lateinit var habitAdapter: HabitAdapter
    private lateinit var prefsManager: SharedPrefsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPrefsManager(requireContext())

        setupRecyclerView()
        setupFab()
        setupHabitChart()
        loadHabits()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            onHabitClick = { habit ->
                editHabit(habit)
            },
            onHabitToggle = { habit ->
                toggleHabitCompletion(habit)
            }
        )

        binding.recyclerViewHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val habit = habitAdapter.currentList[position]
                    deleteHabit(habit, position)
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewHabits)
    }

    private fun setupFab() {
        binding.fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun showAddHabitDialog() {
        val dialog = AddHabitDialog { habit ->
            addHabit(habit, null)
        }
        dialog.show(parentFragmentManager, "AddHabitDialog")
    }

    private fun editHabit(habit: Habit) {
        val dialog = AddHabitDialog(habit) { updatedHabit ->
            updateHabit(updatedHabit)
        }
        dialog.show(parentFragmentManager, "EditHabitDialog")
    }

    private fun loadHabits() {
        val habits = prefsManager.getHabits()
        habitAdapter.submitList(habits)
        checkEmptyState(habits)
    }

    private fun addHabit(habit: Habit, position: Int? = null) {
        prefsManager.addHabit(habit)
        val currentList = habitAdapter.currentList.toMutableList()
        if (position != null) {
            currentList.add(position, habit)
        } else {
            currentList.add(0, habit) 
        }
        habitAdapter.submitList(currentList)
        checkEmptyState(currentList)
        binding.recyclerViewHabits.scrollToPosition(position ?: 0)
    }

    private fun updateHabit(updatedHabit: Habit) {
        prefsManager.updateHabit(updatedHabit)
        loadHabits()
    }

    private fun deleteHabit(habit: Habit, position: Int) {
        prefsManager.deleteHabit(habit.id)
        val currentList = habitAdapter.currentList.toMutableList()
        currentList.removeAt(position)
        habitAdapter.submitList(currentList)
        checkEmptyState(currentList)

        Snackbar.make(binding.root, getString(R.string.habit_deleted), Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.undo)) { 
                addHabit(habit, position)
            }
            .show()
    }

    private fun toggleHabitCompletion(habit: Habit) {
        if (habit.isCompletedToday()) {
            habit.markIncomplete()
        } else {
            habit.markCompleted()
        }
        prefsManager.updateHabit(habit)
        loadHabits()
    }

    private fun setupHabitChart() {
        val habits = prefsManager.getHabits()
        val last7DaysData = getLast7DaysHabitData(habits)
        
        if (last7DaysData.isEmpty()) {
            binding.cardHabitChart.visibility = View.GONE
            return
        }
        
        binding.cardHabitChart.visibility = View.VISIBLE
        
        val chartEntries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()
        
        last7DaysData.forEachIndexed { index, (date, percentage) ->
            chartEntries.add(Entry(index.toFloat(), percentage))
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = sdf.parse(date)
                val displayFormat = SimpleDateFormat("EEE dd", Locale.getDefault())
                dateLabels.add(displayFormat.format(parsedDate ?: Date()))
            } catch (e: Exception) {
                dateLabels.add(date.substring(5)) 
            }
        }
        
        val dataSet = LineDataSet(chartEntries, "Daily Completion %").apply {
            color = Color.parseColor("#81C784") 
            setCircleColor(Color.parseColor("#81C784"))
            lineWidth = 3f
            circleRadius = 6f
            setDrawFilled(true)
            fillColor = Color.parseColor("#E8F5E8") 
            valueTextSize = 12f
            setDrawValues(true)
            mode = LineDataSet.Mode.CUBIC_BEZIER 
            
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}%"
                }
            }
        }
        
        val lineData = LineData(dataSet)
        binding.chartHabitProgress.data = lineData
        
        binding.chartHabitProgress.apply {
            description.isEnabled = true
            description.text = "7-Day Habit Completion Trend"
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
                axisMinimum = 0f
                axisMaximum = 100f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#E0E0E0")
                textSize = 10f
                
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}%"
                    }
                }
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = false
            
            animateX(1000)
            
            invalidate()
        }
    }
    
    private fun getLast7DaysHabitData(habits: List<Habit>): List<Pair<String, Float>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        
        val last7Days = mutableListOf<String>()
        for (i in 6 downTo 0) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            last7Days.add(dateFormat.format(calendar.time))
        }
        
        return last7Days.map { date ->
            val totalHabits = habits.size
            val completedHabits = habits.count { habit ->
                habit.completionDates.any { it.startsWith(date) }
            }
            val percentage = if (totalHabits > 0) {
                (completedHabits.toFloat() / totalHabits * 100)
            } else {
                0f
            }
            Pair(date, percentage)
        }
    }

    private fun checkEmptyState(habits: List<Habit>) {
        if (habits.isEmpty()) {
            binding.recyclerViewHabits.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
            binding.cardHabitChart.visibility = View.GONE
        } else {
            binding.recyclerViewHabits.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            binding.cardHabitChart.visibility = View.VISIBLE
            setupHabitChart() 
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
