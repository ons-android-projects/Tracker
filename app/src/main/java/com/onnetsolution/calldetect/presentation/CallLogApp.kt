package com.onnetsolution.calldetect.presentation

import android.annotation.SuppressLint
import android.util.Log
import com.onnetsolution.calldetect.presentation.viewmodel.CallLogViewModel
import com.onnetsolution.calldetect.presentation.viewmodel.CallLogViewModelFactory
import com.onnetsolution.calldetect.repository.CallLogRepo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.provider.CallLog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onnetsolution.calldetect.R
import com.onnetsolution.calldetect.data.local.CallLogDataClass
import com.onnetsolution.calldetect.data.local.TargetUserInfo
import com.onnetsolution.calldetect.presentation.viewmodel.TargetMobAndDateTimeViewModel
import com.onnetsolution.calldetect.presentation.viewmodel.TargetMobAndDateTimeViewModelFactory
import com.onnetsolution.calldetect.repository.TargetMobAndDateTimeRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.sql.Date
import java.util.Locale

//@Composable
//fun CallLogApp() {
//    val context = LocalContext.current
//    var hasPermission by remember { mutableStateOf(false) }
//    var callLogs by remember { mutableStateOf<List<CallLogEntry>>(emptyList()) }
//    var isLoading by remember { mutableStateOf(false) }
//    val coroutineScope = rememberCoroutineScope()
//
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        hasPermission = isGranted
//    }
//
//    LaunchedEffect(Unit) {
//        if (ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.READ_CALL_LOG
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            hasPermission = true
//        } else {
//            permissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
//        }
//    }
//
//    Column {
//        Text(text = "Allowed.")
//    }
//
//    if (hasPermission) {
//        LaunchedEffect(hasPermission) {
//            isLoading = true
//            coroutineScope.launch(Dispatchers.IO) {
//                callLogs = fetchCallLogs(context)
//                isLoading = false
//            }
//        }
//        CallLogScreen(callLogs, isLoading)
//    } else {
//        PermissionRequestScreen { permissionLauncher.launch(Manifest.permission.READ_CALL_LOG) }
//    }
//}
//
//@Composable
//fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
//    Box(
//        contentAlignment = Alignment.Center,
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            Text("This app requires permission to read call logs.")
//            Spacer(modifier = Modifier.height(8.dp))
//            Button(onClick = onRequestPermission) {
//                Text("Grant Permission")
//            }
//        }
//    }
//}
//
//@SuppressLint("NewApi")
//@Composable
//fun CallLogScreen(callLogs: List<CallLogEntry>, isLoading: Boolean) {
//
//    val context = LocalContext.current
//    val db = TargetUserInfo.getDatabase(context)
//    val repo = TargetMobAndDateTimeRepo(db)
//    val targetUserMobAndDateTimeViewModel: TargetMobAndDateTimeViewModel =
//        viewModel(factory = TargetMobAndDateTimeViewModelFactory(repo))
//    val targetUserData = targetUserMobAndDateTimeViewModel.targetUserData.observeAsState()
//
//    val callLogrepo = CallLogRepo(db)
//    val callLogViewModel: CallLogViewModel =
//        viewModel(factory = CallLogViewModelFactory(callLogrepo))
//    val callLogData = callLogViewModel.callLogData.observeAsState()
//
//    targetUserMobAndDateTimeViewModel.getMobAndDateTime()
//
//    val currentDateTime = remember {
//        val now = LocalDateTime.now()
//        val formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//        val formattedDate = now.format(formatterDate)
//        formattedDate
//    }
//
//    if (isLoading) {
//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier.fillMaxSize()
//        ) {
//            CircularProgressIndicator()
//        }
//    } else {
//
//        // Move these variables outside the block so they persist across invocations
//        val seenLogs = remember { hashSetOf<String>() }
//        var hasInserted by remember { mutableStateOf(false) } // Ensures this persists across calls
//        var callTime by remember{ mutableStateOf("") }
//
//// Your logic for inserting logs
//        val updatedDateTime = targetUserData.value?.current_date_time
//        Log.d("CALL_LOGS", "callLogs size: ${callLogs.size}, callLogs: $callLogs")
//
//        val callLogDataList = callLogs.mapNotNull { log ->
//            val logIdentifier = log.date_time // Use a unique identifier
//            callTime=log.date_time
//            if (seenLogs.contains(logIdentifier)) {
//                Log.d("InsertCheck", "Skipping duplicate log: ${log.number}, ${log.date_time}")
//                null
//            } else {
//                seenLogs.add(logIdentifier) // Add to seenLogs to track duplicates
//                Log.d("InsertCheck", "Inserting log: ${log.number}, ${log.date_time}")
//                CallLogDataClass(
//                    stat = 0,
//                    number = log.number,
//                    callType = log.type,
//                    date = log.date,
//                    duration = log.duration,
//                    time = log.time,
//                    phnDateTime = log.date_time
//                )
//            }
//        }.toList()
//
//
//// Only insert if necessary
//        Log.d("CALLDATALISTS",callLogDataList.toString())
//        Log.d("UPDATED_TIME",updatedDateTime?:"No Value")
//
//        LaunchedEffect(key1 = updatedDateTime) {
//            delay(5000)
//            Log.d("UPDATED_T_D",updatedDateTime.toString())
//            if (updatedDateTime == null || updatedDateTime.isEmpty()) {
//                Log.d("IF_ELSE", "If block executed.")
//                if (callLogDataList.isNotEmpty() && !hasInserted) {
//                    Log.d("CALLDATALIST", callLogDataList.toString())
//                    Log.d("InsertChecks", "Inserting new logs in reverse order.")
//
//                    // Reverse the order of callLogDataList before insertion
//                    val reversedLogs = callLogDataList.reversed()
//
//                    // Insert reversed logs
//                    callLogViewModel.insertCallLog(reversedLogs)
//                    targetUserMobAndDateTimeViewModel.updateDateTime(currentDateTime)
//                    hasInserted = true // Mark that we've inserted the logs to prevent further insertions
//                } else {
//                    Log.d("InsertChecks", "No new logs to insert.")
//                }
//            }
//
//            else {
//                Log.d("IF_ELSE","else block executed.")
//                //if(calltime>updatedtime)
//                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//
//// Parse updatedDateTime safely
//                val parsedUpdatedTime = try {
//                    LocalDateTime.parse(updatedDateTime, formatter)
//                } catch (e: Exception) {
//                    Log.e("UPDATED_DATETIME_ERROR", "Error parsing updatedDateTime: $updatedDateTime", e)
//                    null
//                }
//
//// Proceed only if parsedUpdatedTime is valid
//                if (parsedUpdatedTime != null) {
//                    Log.d("CLDL",callLogDataList.size.toString())
//                    val filteredLogs = callLogDataList.filter { log ->
//                        try {
//                            val logTime = LocalDateTime.parse(log.phnDateTime, formatter)
//                            logTime > parsedUpdatedTime
//                        } catch (e: Exception) {
//                            Log.e("LOG_TIME_ERROR", "Error parsing logTime: ${log.phnDateTime}", e)
//                            false
//                        }
//                    }
//
//                    Log.d("FILTERED_LOGS", "Filtered Logs: $filteredLogs")
//
//                    val uniqueFilteredLogs = filteredLogs.filter { log ->
//                        !isLogAlreadyInserted(log, callLogData.value ?: emptyList())
//                    }
//
//                    Log.d("UNIQUE_FILTERED_LOGS", "Unique Filtered Logs: $uniqueFilteredLogs")
//
//                    if (uniqueFilteredLogs.isNotEmpty()) {
//                        Log.d("InsertCheck", "Inserting filtered logs: ${uniqueFilteredLogs.size}")
//                        callLogViewModel.insertCallLog(uniqueFilteredLogs)
//                    } else {
//                        Log.d("InsertCheck", "No new logs to insert after filtering.")
//                    }
//                } else {
//                    Log.e("FILTERING_ERROR", "Failed to parse updatedDateTime. Skipping filtering.")
//                }
//                targetUserMobAndDateTimeViewModel.updateDateTime(currentDateTime)
//            }
//        }
//        Log.d("CURR_TIME",currentDateTime)
//
//    }
//}
//
//fun isLogAlreadyInserted(log: CallLogDataClass, existingLogs: List<CallLogDataClass>): Boolean {
//    return existingLogs.any { existingLog ->
//        existingLog.phnDateTime == log.phnDateTime
//    }
//}
//
////@Composable
////fun CallLogItem(callLog: CallLogEntry) {
////    Card(
////        modifier = Modifier.fillMaxWidth(),
////        elevation = CardDefaults.cardElevation(4.dp)
////    ) {
////
////        Column(modifier = Modifier.padding(8.dp)) {
////            Row(
////                modifier = Modifier.fillMaxWidth(),
////                verticalAlignment = Alignment.CenterVertically
////            ) {
////                Icon(imageVector = Icons.Default.Call, contentDescription = "call")
////                Text("Number: ", fontWeight = FontWeight.Bold)
////                Text(text = callLog.number)
////            }
////            Row(
////                modifier = Modifier.fillMaxWidth(),
////                verticalAlignment = Alignment.CenterVertically
////            ) {
////                Icon(
////                    painter = painterResource(id = R.drawable.arrows),
////                    contentDescription = "call",
////                    modifier = Modifier.size(30.dp)
////                )
////                Text("Type: ", fontWeight = FontWeight.Bold)
////                Text(text = callLog.type)
////            }
////            Row(
////                modifier = Modifier.fillMaxWidth(),
////                verticalAlignment = Alignment.CenterVertically
////            ) {
////                Icon(
////                    painter = painterResource(id = R.drawable.time),
////                    contentDescription = "call",
////                    modifier = Modifier.size(20.dp)
////                )
////                Text(" Time: ", fontWeight = FontWeight.Bold)
////                Text(text = callLog.time)
////            }
////            Row(
////                modifier = Modifier.fillMaxWidth(),
////                verticalAlignment = Alignment.CenterVertically
////            ) {
////                Icon(imageVector = Icons.Default.DateRange, contentDescription = "call")
////                Text("Date: ", fontWeight = FontWeight.Bold)
////                Text(text = DateFormat.getDateInstance().format(Date(callLog.date)))
////            }
////            Row(
////                modifier = Modifier.fillMaxWidth(),
////                verticalAlignment = Alignment.CenterVertically
////            ) {
////                Icon(
////                    painter = painterResource(id = R.drawable.duration),
////                    contentDescription = "call",
////                    modifier = Modifier.size(30.dp)
////                )
////                Text("Duration: ", fontWeight = FontWeight.Bold)
////                Text(text = formatDuration(callLog.duration))
////            }
////
////        }
////    }
////}


// Helper function to fetch call logs
//fun fetchCallLogs(context: Context): List<CallLogEntry> {
//    val callLogList = mutableListOf<CallLogEntry>()
//    val uri = CallLog.Calls.CONTENT_URI
//    val projection = arrayOf(
//        CallLog.Calls.NUMBER,
//        CallLog.Calls.TYPE,
//        CallLog.Calls.DATE,
//        CallLog.Calls.DURATION,
//    )
//
//    // Query all call logs without the LIMIT clause
//    val cursor = context.contentResolver.query(
//        uri,
//        projection,
//        null,
//        null,
//        "${CallLog.Calls.DATE} DESC"  // Sort by date in descending order
//    )
//
//    cursor?.use {
//        var count = 0
//        while (it.moveToNext() && count < 20) {  // Limit to 20 logs in memory
//            val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
//            val type = when (it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))) {
//                CallLog.Calls.INCOMING_TYPE -> "Incoming"
//                CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
//                CallLog.Calls.MISSED_TYPE -> "Missed"
//                CallLog.Calls.REJECTED_TYPE -> "Rejected"
//                else -> "Unknown"
//            }
//            val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
//            val duration = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
//            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(date))
//            val date_time =
//                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(date))
//            callLogList.add(CallLogEntry(number, type, date, duration, time, date_time))
//            count++
//        }
//    }
//    return callLogList
//}

//fun formatDuration(seconds: Long): String {
//    val hours = seconds / 3600
//    val minutes = (seconds % 3600) / 60
//    val secs = seconds % 60
//
//    return buildString {
//        if (hours > 0) append("$hours hrs ")
//        if (minutes > 0 || hours > 0) append("$minutes mins ")
//        append("$secs secs")
//    }.trim()
//}

// Data class for call log entries
//data class CallLogEntry(
//    val number: String,
//    val type: String,
//    val date: Long,
//    val duration: Long,
//    val time: String,
//    val date_time: String
//)

