package com.onnetsolution.calldetect.relaunch

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class AppLaunchWorker(context:Context,workerParams: WorkerParameters): CoroutineWorker(context,workerParams) {
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        return try {
            //Thread.sleep(2000)
            Log.d("AppLaunchWorker", "Worker started")
            InsertUploadCallLogToDatabaseAndServer(applicationContext)
            //scheduleAppLaunch(applicationContext)
            Log.d("AppLaunchWorker", "Worker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("AppLaunchWorker", "Error: ${e.message}")
            Result.retry()
        }

    }
}

