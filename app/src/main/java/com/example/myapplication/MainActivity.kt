package com.example.myapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.utils.SharedPrefsManager
import com.example.myapplication.utils.HydrationScheduler
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: SharedPrefsManager

    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefsManager = SharedPrefsManager(this)
        
        applyTheme()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setupWithNavController(navController)

        navView.setOnItemSelectedListener { item ->
            if (navController.currentDestination?.id != item.itemId) {
                navController.navigate(item.itemId)
            }
            true
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> navView.selectedItemId = R.id.navigation_home
                R.id.navigation_habits -> navView.selectedItemId = R.id.navigation_habits
                R.id.navigation_mood -> navView.selectedItemId = R.id.navigation_mood
                R.id.navigation_steps -> navView.selectedItemId = R.id.navigation_steps
                R.id.navigation_profile -> navView.selectedItemId = R.id.navigation_profile
                R.id.waterDetailsFragment -> {
                    navView.selectedItemId = R.id.navigation_home
                }
            }
        }

        initializeHydrationRemindersAsync()

        if (intent.getBooleanExtra("open_hydration", false)) {
            navController.navigate(R.id.navigation_home)
        }
    }
    
    private fun initializeHydrationRemindersAsync() {
        backgroundScope.launch {
            try {
                if (prefsManager.areHydrationRemindersEnabled()) {
                    val hydrationScheduler = HydrationScheduler(this@MainActivity)
                    val interval = prefsManager.getReminderInterval()
                    hydrationScheduler.scheduleHydrationReminders(interval)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyTheme() {
        val isDarkModeEnabled = prefsManager.isDarkModeEnabled()
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkModeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundScope.cancel()
    }
}