package com.onnetsolution.calldetect

import android.app.Service
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.onnetsolution.calldetect.data.ApiService
import com.onnetsolution.calldetect.data.di.NetworkModule
import com.onnetsolution.calldetect.data.local.CallLogDataClass
import com.onnetsolution.calldetect.data.local.TargetUserInfo
import com.onnetsolution.calldetect.navigation.NavGraph
import com.onnetsolution.calldetect.presentation.DashBoard
import com.onnetsolution.calldetect.presentation.UserPhnNumber
import com.onnetsolution.calldetect.presentation.fetchCallLogs
import com.onnetsolution.calldetect.presentation.formatDuration
import com.onnetsolution.calldetect.presentation.isLogAlreadyInserted
import com.onnetsolution.calldetect.presentation.viewmodel.CallLogViewModel
import com.onnetsolution.calldetect.presentation.viewmodel.CallLogViewModelFactory
import com.onnetsolution.calldetect.relaunch.AppLaunchWorker
import com.onnetsolution.calldetect.relaunch.InsertUploadCallLogToDatabaseAndServer
import com.onnetsolution.calldetect.relaunch.scheduleAppLaunch
import com.onnetsolution.calldetect.repository.CallLogRepo
import com.onnetsolution.calldetect.repository.TargetMobAndDateTimeRepo
import com.onnetsolution.calldetect.repository.UploadCallLogRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.sql.Date
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Schedule the app Launch task
        scheduleAppLaunch(context = this)
         observeWorkerStatus()
        // enableEdgeToEdge()

        setContent {
            // NavGraph(navController = rememberNavController())
            DashBoard()
        }
    }


    /*private var job: Job? = null

    val coroutine = CoroutineScope(Dispatchers.IO)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {
        super.onStop()
        Log.d("OnSTOP", "onStop executed.")
        job = coroutine.launch {
            Log.d("Execution", "Coroutine started.")
            while (true) {
                try {
                    InsertUploadCallLogToDatabaseAndServer(applicationContext)
                    Log.e("Execution", "executed.")
                    delay(5000)
                } catch (e: Exception) {
                    Log.e("Execution", "Error executing function: ${e.message}", e)
                }
            }
            Log.d("Execution", "Coroutine stopped.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()  // Cancel the job when the activity is destroyed
    }*/


    fun observeWorkerStatus() {
        WorkManager.getInstance(applicationContext)
            .getWorkInfosForUniqueWorkLiveData("AppLaunchWorker")
            .observeForever { workInfos ->
                workInfos?.forEach { workInfo ->
                    Log.d("MainActivity", "Worker State: ${workInfo.state}")
                    when (workInfo.state) {
                        WorkInfo.State.ENQUEUED -> Log.d("MainActivity", "Work is enqueued")
                        WorkInfo.State.RUNNING -> Log.d("MainActivity", "Work is running")
                        WorkInfo.State.SUCCEEDED -> Log.d("MainActivity", "Work succeeded")
                        WorkInfo.State.FAILED -> Log.d("MainActivity", "Work failed")
                        WorkInfo.State.CANCELLED -> Log.d("MainActivity", "Work cancelled")
                        else -> Log.d("MainActivity", "Work in unknown state")
                    }
                }
            }
    }

}

