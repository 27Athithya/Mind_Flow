package com.example.myapplication

import android.app.Application
import com.example.myapplication.utils.SharedPrefsManager

class MindFlowApplication : Application() {

    lateinit var sharedPrefsManager: SharedPrefsManager
        private set

    override fun onCreate() {
        super.onCreate()

        sharedPrefsManager = SharedPrefsManager(this)
    }

    override fun onTerminate() {
        super.onTerminate()

        if (::sharedPrefsManager.isInitialized) {
            sharedPrefsManager.cleanup()
        }
    }
}
