package com.example.myapplication.activities

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.MainActivity
import com.example.myapplication.MindFlowApplication
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.utils.SharedPrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var prefsManager: SharedPrefsManager
    private var isDestroyed = false
    private val splashTimeout = 10000L // 10 seconds timeout

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isDestroyed) {
            if (isGranted) {
                checkExactAlarmPermission()
            } else {
                showPermissionRationale(
                    "Notification Permission",
                    "Notifications are needed for reminders. Please enable them in settings."
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = (application as MindFlowApplication).sharedPrefsManager
        supportActionBar?.hide()

        startSplashSequence()
    }

    override fun onDestroy() {
        super.onDestroy()
        isDestroyed = true
    }

    private fun startSplashSequence() {
        lifecycleScope.launch {
            val result = withTimeoutOrNull(splashTimeout) {
                checkPermissionsAsync()
            }

            if (result == null && !isDestroyed) {
                proceedToAppAsync()
            }
        }
    }

    private suspend fun checkPermissionsAsync() {
        withContext(Dispatchers.Main) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!NotificationManagerCompat.from(this@SplashActivity).areNotificationsEnabled()) {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    checkExactAlarmPermission()
                }
            } else {
                checkExactAlarmPermission()
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (isDestroyed) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    showPermissionRationale(
                        "Exact Alarm Permission",
                        "This permission is required for accurate reminders. Please grant it in the next screen."
                    ) { 
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            startActivity(intent)
                        } catch (e: Exception) {
                            proceedToAppAsync()
                        }
                    }
                    return 
                }
            } catch (e: Exception) {
            }
        }
        proceedToAppAsync()
    }

    private fun showPermissionRationale(title: String, message: String, onPositive: (() -> Unit)? = null) {
        if (isDestroyed) return

        try {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Go to Settings") { _, _ ->
                    if (onPositive != null) {
                        onPositive.invoke()
                    } else {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:$packageName")
                            startActivity(intent)
                        } catch (e: Exception) {
                            proceedToAppAsync()
                        }
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    proceedToAppAsync() 
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            proceedToAppAsync()
        }
    }

    private fun proceedToAppAsync() {
        if (isDestroyed) return

        lifecycleScope.launch {
            try {
                val shouldGoToMain = withContext(Dispatchers.IO) {
                    withTimeoutOrNull(3000L) { 
                        prefsManager.isLoggedIn() && prefsManager.shouldRememberUser()
                    } ?: false 
                }

                withContext(Dispatchers.Main) {
                    if (!isDestroyed) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (!isDestroyed) {
                                navigateToNextActivity(shouldGoToMain)
                            }
                        }, 500)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isDestroyed) {
                        navigateToNextActivity(false)
                    }
                }
            }
        }
    }

    private fun navigateToNextActivity(shouldGoToMain: Boolean) {
        try {
            val intent = if (shouldGoToMain) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, AuthActivity::class.java)
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                proceedToAppAsync()
            }
        }
    }
}
