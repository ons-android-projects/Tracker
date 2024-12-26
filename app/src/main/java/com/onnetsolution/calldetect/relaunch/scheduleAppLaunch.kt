package com.onnetsolution.calldetect.relaunch

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun scheduleAppLaunch(context: Context) {
    val workRequest= PeriodicWorkRequestBuilder<AppLaunchWorker>(15,TimeUnit.MINUTES)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Ensures network is available
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .build()
        )
        .build()
    Log.d("MainActivity", "Scheduling work with 15 minutes interval")
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "AppLaunchWorker",
        ExistingPeriodicWorkPolicy.REPLACE,
        workRequest
    )
    Log.d("MainActivity", "Work scheduled to refresh every 15 minutes")

    WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData("AppLaunchWorker")
        .observeForever { workInfos ->
            workInfos?.forEach { workInfo ->
                Log.d("MainActivity", "Worker State: ${workInfo.state}")
            }
        }
}
