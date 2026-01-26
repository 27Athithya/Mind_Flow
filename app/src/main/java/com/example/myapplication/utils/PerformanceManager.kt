package com.example.myapplication.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadFactory

/**
 * Performance manager to prevent ANR issues and system resource exhaustion
 */
object PerformanceManager {

    private const val MAX_BACKGROUND_THREADS = 3
    private const val KEEP_ALIVE_TIME = 30L

    // Shared thread pool with limited capacity to prevent resource exhaustion
    val backgroundExecutor: ThreadPoolExecutor by lazy {
        ThreadPoolExecutor(
            1, // Core pool size
            MAX_BACKGROUND_THREADS, // Maximum pool size
            KEEP_ALIVE_TIME, // Keep alive time
            TimeUnit.SECONDS,
            LinkedBlockingQueue(50), // Bounded queue to prevent memory issues
            ThreadFactory { runnable ->
                Thread(runnable, "WellnessHub-Background").apply {
                    priority = Thread.MIN_PRIORITY // Low priority to not interfere with system
                    isDaemon = true
                }
            },
            RejectedExecutionHandler { runnable, executor ->
                // If queue is full, run on calling thread as fallback
                // This prevents dropping tasks but may cause slight blocking
                runnable.run()
            }
        )
    }

    /**
     * Check if the device is under memory pressure
     */
    fun isMemoryPressureHigh(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        // Consider memory pressure high if less than 200MB available
        return memoryInfo.availMem < 200 * 1024 * 1024
    }

    /**
     * Check if CPU usage is high
     */
    fun isCpuUsageHigh(): Boolean {
        // Simple heuristic: if we have many background tasks queued, assume high CPU usage
        return backgroundExecutor.queue.size > 20
    }

    /**
     * Throttle operations based on system conditions
     */
    fun shouldThrottleOperations(context: Context): Boolean {
        return isMemoryPressureHigh(context) || isCpuUsageHigh()
    }

    /**
     * Execute task with performance considerations
     */
    fun executeWithThrottling(context: Context, task: Runnable) {
        if (shouldThrottleOperations(context)) {
            // Delay execution if system is under pressure
            backgroundExecutor.schedule(task, 1000, TimeUnit.MILLISECONDS)
        } else {
            backgroundExecutor.execute(task)
        }
    }

    /**
     * Schedule a task with delay if needed
     */
    private fun ThreadPoolExecutor.schedule(task: Runnable, delay: Long, unit: TimeUnit) {
        execute {
            try {
                Thread.sleep(unit.toMillis(delay))
                task.run()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    /**
     * Reduce app priority when in background to free up system resources
     */
    fun reduceAppPriority() {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        } catch (e: Exception) {
            // Ignore if we can't set priority
        }
    }

    /**
     * Restore normal app priority when in foreground
     */
    fun restoreAppPriority() {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT)
        } catch (e: Exception) {
            // Ignore if we can't set priority
        }
    }

    /**
     * Clear any cached data if memory pressure is high
     */
    fun clearCacheIfNeeded(context: Context, sharedPrefsManager: SharedPrefsManager) {
        if (isMemoryPressureHigh(context)) {
            // Clear internal caches
            sharedPrefsManager.clearCache()
        }
    }
}
