package com.onnetsolution.calldetect.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.provider.CallLog
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onnetsolution.calldetect.data.ApiService
import com.onnetsolution.calldetect.data.di.NetworkModule
import com.onnetsolution.calldetect.data.local.CallLogDataClass
import com.onnetsolution.calldetect.data.local.MobAndDateTimeDataClass
import com.onnetsolution.calldetect.data.local.TargetUserDatabase
import com.onnetsolution.calldetect.data.local.TargetUserInfo
import com.onnetsolution.calldetect.presentation.viewmodel.CallLogViewModel
import com.onnetsolution.calldetect.presentation.viewmodel.CallLogViewModelFactory
import com.onnetsolution.calldetect.presentation.viewmodel.TargetMobAndDateTimeViewModel
import com.onnetsolution.calldetect.presentation.viewmodel.TargetMobAndDateTimeViewModelFactory
import com.onnetsolution.calldetect.presentation.viewmodel.UploadCallLogViewModel
import com.onnetsolution.calldetect.presentation.viewmodel.UploadCallLogViewModelFactory
import com.onnetsolution.calldetect.presentation.viewmodel.UploadMobAndDateTimeViewModel
import com.onnetsolution.calldetect.presentation.viewmodel.UploadMobAndDateTimeViewModelFactory
import com.onnetsolution.calldetect.repository.CallLogRepo
import com.onnetsolution.calldetect.repository.TargetMobAndDateTimeRepo
import com.onnetsolution.calldetect.repository.UploadCallLogRepo
import com.onnetsolution.calldetect.repository.UploadMobAndDateTimeRepo
import com.onnetsolution.calldetect.usecase.UploadCallLogUseCase
import com.onnetsolution.calldetect.usecase.UploadMobAndDateTimeUseCase
import com.onnetsolution.calldetect.util.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.sql.Date
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.runtime.remember as remember

@SuppressLint("SuspiciousIndentation")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserPhnNumber() {
    var phoneNumbers by remember { mutableStateOf("") }
    var callLogs by remember { mutableStateOf<List<CallLogEntry>>(emptyList()) }
    val context = LocalContext.current

    val targetApi = NetworkModule.getInstance().create(ApiService::class.java)
    val targetRepo = UploadMobAndDateTimeRepo(targetApi)
    val uploadMobAndDateTimeUseCase = UploadMobAndDateTimeUseCase(targetRepo)
    val targetViewModel: UploadMobAndDateTimeViewModel =
        viewModel(factory = UploadMobAndDateTimeViewModelFactory(uploadMobAndDateTimeUseCase))
    val uploadSate = targetViewModel.mobDateTimeFlow.collectAsState()

    val callLogRepo = UploadCallLogRepo(targetApi)
    val callLogUseCase = UploadCallLogUseCase(callLogRepo)
    val uploadCallLogViewModel:UploadCallLogViewModel= viewModel(factory = UploadCallLogViewModelFactory(callLogUseCase))
    val callLogState = uploadCallLogViewModel.callLogState.collectAsState()

    val db = TargetUserInfo.getDatabase(context)
    val repo = TargetMobAndDateTimeRepo(db)
    val targetUserMobAndDateTimeViewModel: TargetMobAndDateTimeViewModel =
        viewModel(factory = TargetMobAndDateTimeViewModelFactory(repo))
    val targetUserData = targetUserMobAndDateTimeViewModel.targetUserData.observeAsState()

    val callLogrepo = CallLogRepo(db)
    val callLogViewModel: CallLogViewModel =
        viewModel(factory = CallLogViewModelFactory(callLogrepo))
    val callLogData = callLogViewModel.callLogData.observeAsState()

    val currentDateTime = remember {
        val now = LocalDateTime.now()
        val formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDate = now.format(formatterDate)
        formattedDate
    }
    val updateCurrentDateTime = remember {
        val now = LocalDateTime.now().minusSeconds(1) // Subtract 1 second
        val formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        now.format(formatterDate) // Format the adjusted time
    }


    // Define the permissions to request
    val permissions = listOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Manifest.permission.READ_PHONE_NUMBERS
        } else {
            Manifest.permission.READ_PHONE_STATE
        },
        Manifest.permission.READ_CALL_LOG
    )

    // Use ActivityResultLauncher for multiple permissions
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        val allGranted = permissionsResult.values.all { it }
        if (allGranted) {
            // All permissions are granted
            phoneNumbers = getPhoneNumber(context)
            callLogs = fetchCallLogs(context)
        } else {
            // Handle permissions denied
            Log.d("PERMISSION", "One or more permissions were denied.")
        }
    }

    // Check and request permissions on first composition
    LaunchedEffect(Unit) {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            multiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // All permissions already granted
            phoneNumbers = getPhoneNumber(context)
            callLogs = fetchCallLogs(context)
        }
    }



    LaunchedEffect(uploadSate.value) {
        when (uploadSate.value) {
            is NetworkResponse.EmptyState -> {}
            is NetworkResponse.Error -> {
                Log.d("RESPONSE", uploadSate.value.message ?: "Unknown error ")
                // Toast.makeText(context,uploadSate.value.message?:"Unknown error ",Toast.LENGTH_LONG).show()
            }

            is NetworkResponse.Loading -> {}
            is NetworkResponse.Success -> {
               // Log.d("RESPONSE", "UPLOADED SUCCESSFULLY.")
                // Toast.makeText(context,"UPLOADED SUCCESSFULLY.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /*LaunchedEffect(callLogState.value) {
        when(callLogState.value){
            is NetworkResponse.EmptyState -> {}
            is NetworkResponse.Error -> {
                Log.d("CALL_LOG_RESPONSE", callLogState.value.message ?: "Unknown error ")
            }
            is NetworkResponse.Loading -> {}
            is NetworkResponse.Success -> {
                Log.d("CALL_LOG_RESPONSE", callLogState.value.message ?: "Success ")
                callLogViewModel.deleteCallLog()
            }
        }
    }*/

    targetUserMobAndDateTimeViewModel.getMobAndDateTime()

    val seenLogs = remember { hashSetOf<String>() }
    var hasInserted by remember { mutableStateOf(false) } // Ensures this persists across calls
    var callTime by remember { mutableStateOf("") }

    val callLogDataList = remember(callLogs) {
        callLogs.mapNotNull { log ->
            val logIdentifier = log.date_time
            if (seenLogs.contains(logIdentifier)) {
                null
            } else {
                seenLogs.add(logIdentifier)
                CallLogDataClass(
                    stat = 0,
                    number = log.number,
                    callType = log.type,
                    date = log.date,
                    duration = log.duration,
                    time = log.time,
                    phnDateTime = log.date_time
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        if (phoneNumbers.isEmpty()) {
            Log.d("PHONE_NUMBER", "No phone numbers found or permission not granted.")
        } else {
            //inserting in the db(targetUser table)
            targetUserMobAndDateTimeViewModel.insertUserMobAndDateTime(
                data = MobAndDateTimeDataClass(
                    target_mob = phoneNumbers,
                    current_date_time = ""
                )
            )

            val updatedDateTime = targetUserData.value?.current_date_time
            //inserting data with condition in (callLog table)
            LaunchedEffect(key1 = updatedDateTime, key2 = callLogDataList) {
                delay(5000)
                Log.d("UPDATED_T_D",updatedDateTime.toString())
                if (updatedDateTime == null || updatedDateTime.isEmpty()) {
                    Log.d("IF_ELSE", "If block executed.")
                    Log.d("callLogDataList_if",callLogDataList.toString())
                    if (callLogDataList.isNotEmpty() && !hasInserted) {
                        Log.d("CALLDATALIST", callLogDataList.toString())
                        Log.d("InsertChecks", "Inserting new logs in reverse order.")

                        // Reverse the order of callLogDataList before insertion
                        val reversedLogs = callLogDataList.reversed()

                        // Insert reversed logs
                        callLogViewModel.insertCallLog(reversedLogs)
                        targetUserMobAndDateTimeViewModel.updateDateTime(updateCurrentDateTime)
                        hasInserted = true // Mark that we've inserted the logs to prevent further insertions
                    } else {
                       // Log.d("InsertChecks", "No new logs to insert.")
                    }
                }

              /*  else {
                        Log.d("IF_ELSE", "else block executed.")
                    Log.d("callLogDataList_else",callLogDataList.toString())
                        //if(calltime>updatedtime)
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

// Parse updatedDateTime safely
                        val parsedUpdatedTime = try {
                            LocalDateTime.parse(updatedDateTime, formatter)
                        } catch (e: Exception) {
                            Log.e(
                                "UPDATED_DATETIME_ERROR",
                                "Error parsing updatedDateTime: $updatedDateTime",
                                e
                            )
                            null
                        }
                    if (callLogDataList.isNotEmpty()) {
// Proceed only if parsedUpdatedTime is valid
                        if (parsedUpdatedTime != null) {
                            Log.d("CLDL", callLogDataList.size.toString())

                            val filteredLogs = callLogDataList.filter { log ->
                                try {
                                    val logTime = LocalDateTime.parse(log.phnDateTime, formatter)
                                    logTime > parsedUpdatedTime
                                } catch (e: Exception) {
                                    Log.e(
                                        "LOG_TIME_ERROR",
                                        "Error parsing logTime: ${log.phnDateTime}",
                                        e
                                    )
                                    false
                                }
                            }


                            Log.d("FILTERED_LOGS", "Filtered Logs: $filteredLogs")

                            val uniqueFilteredLogs = filteredLogs.filter { log ->
                                !isLogAlreadyInserted(log, callLogData.value ?: emptyList())
                            }

                            Log.d(
                                "UNIQUE_FILTERED_LOGS",
                                "Unique Filtered Logs: $uniqueFilteredLogs"
                            )

                            if (uniqueFilteredLogs.isNotEmpty()) {
                                Log.d(
                                    "InsertCheck",
                                    "Inserting filtered logs: ${uniqueFilteredLogs.size}"
                                )
                                val reverseData=uniqueFilteredLogs.reversed()
                                callLogViewModel.insertCallLog(reverseData)
                                targetUserMobAndDateTimeViewModel.updateDateTime(updateCurrentDateTime)
                            } else {
                                Log.d("InsertCheck", "No new logs to insert after filtering.")
                            }
                        } else {
                            Log.e(
                                "FILTERING_ERROR",
                                "Failed to parse updatedDateTime. Skipping filtering."
                            )
                        }

                    }
                }*/
            }


        }


        //upload the data to server (from TargetUser table)
        targetUserData?.let {
            it.value?.target_mob?.let { it1 ->
                // it.value?.current_date_time?.let { it2 ->
                targetViewModel.uploadMobAndDateTime(
                    context,
                    it1, currentDateTime// it2
                )
                // }
            }
        }

        //upload all the callLog to server
       /* callLogViewModel.getAllCallLogs()

        val uploadedLogs = remember { hashSetOf<String>() }

        LaunchedEffect(callLogData.value) {
            callLogData.value?.forEach { data ->
                val logIdentifier = "${data.number}_${data.phnDateTime}"
                val dateformat=data.date
                val date=SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateformat))
                val durationformat=data.duration
                val duration=formatDuration(durationformat)
                    Log.d("DATE_DURATION","$date   $duration")
                if (!uploadedLogs.contains(logIdentifier)) {
                    uploadedLogs.add(logIdentifier)
                    uploadCallLogViewModel.uploadCallLogs(
                        context = context,
                        "${targetUserData.value?.target_mob}@${data.number}@${data.callType}@${date}@${data.time}@${data.phnDateTime}@${duration}"
                    )
                }
            }
        }*/
    }
}





@SuppressLint("DefaultLocale")
fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}

fun isLogAlreadyInserted(log: CallLogDataClass, callLogDataClasses: List<CallLogDataClass>): Boolean {
    return callLogDataClasses.any { existingLog ->
        existingLog.phnDateTime == log.phnDateTime
    }
}

@SuppressLint("HardwareIds")
fun getPhoneNumber(context: Context): String {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_NUMBERS
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {

        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.line1Number
    }
    return "Permission not granted or number not available" // Permission not granted or number not available
}

fun fetchCallLogs(context: Context): List<CallLogEntry> {
    val callLogList = mutableListOf<CallLogEntry>()
    val uri = CallLog.Calls.CONTENT_URI
    val projection = arrayOf(
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION,
    )

    // Query all call logs without the LIMIT clause
    val cursor = context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        "${CallLog.Calls.DATE} DESC"  // Sort by date in descending order
    )

    cursor?.use {
        var count = 0
        while (it.moveToNext() && count < 20) {  // Limit to 20 logs in memory
            val number = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
            val type = when (it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))) {
                CallLog.Calls.INCOMING_TYPE -> "Incoming"
                CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                CallLog.Calls.MISSED_TYPE -> "Missed"
                CallLog.Calls.REJECTED_TYPE -> "Rejected"
                else -> "Unknown"
            }
            val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
            val duration = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(date))
            val date_time =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(date))
            callLogList.add(CallLogEntry(number, type, date, duration, time, date_time))
            count++
        }
    }
    return callLogList
}


data class CallLogEntry(
    val number: String,
    val type: String,
    val date: Long,
    val duration: Long,
    val time: String,
    val date_time: String
)


//Can Fetch both the number if the phone contains dual sim card.

//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun PhoneNumbersComposable() {
//    val context= LocalContext.current
//    var phoneNumbers by remember { mutableStateOf(listOf<String>()) }
//    var hasPermission by remember { mutableStateOf(false) }
//
//    // Permission launcher for runtime permissions
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions(),
//        onResult = { permissions ->
//            hasPermission = permissions[Manifest.permission.READ_PHONE_NUMBERS] == true ||
//                    permissions[Manifest.permission.READ_PHONE_STATE] == true
//        }
//    )
//
//    // Check permissions when the composable is displayed
//    LaunchedEffect(Unit) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            permissionLauncher.launch(
//                arrayOf(
//                    Manifest.permission.READ_PHONE_NUMBERS,
//                    Manifest.permission.READ_PHONE_STATE
//                )
//            )
//        } else {
//            hasPermission = true // Permissions are granted by default on older Android versions
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "Phone Numbers",
//            style = MaterialTheme.typography.titleMedium,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        if (hasPermission) {
//            if (phoneNumbers.isEmpty()) {
//                phoneNumbers = getPhoneNumbers(context)
//            } else {
//                LazyColumn {
//                    items(phoneNumbers) { number ->
//                        Text(
//                            text = number,
//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.padding(8.dp)
//                        )
//                    }
//                }
//            }
//        } else {
//            Text(
//                text = "Permission is required to fetch phone numbers.",
//                style = MaterialTheme.typography.bodyMedium
//            )
//        }
//    }
//}
//
//
//@SuppressLint("HardwareIds")
//fun getPhoneNumbers(context: Context): List<String> {
//    val phoneNumbers = mutableListOf<String>()
//
//    // Check for permissions
//    if (ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.READ_PHONE_NUMBERS
//        ) == PackageManager.PERMISSION_GRANTED ||
//        ContextCompat.checkSelfPermission(
//            context,
//            Manifest.permission.READ_PHONE_STATE
//        ) == PackageManager.PERMISSION_GRANTED
//    ) {
//        // Access Subscription Manager
//        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
//        val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
//
//        // Retrieve numbers for each SIM
//        subscriptionInfoList?.forEach { subscriptionInfo ->
//            val phoneNumber = subscriptionInfo.number
//            if (!phoneNumber.isNullOrEmpty()) {
//                phoneNumbers.add(phoneNumber)
//            } else {
//                phoneNumbers.add("Number not available for SIM ${subscriptionInfo.simSlotIndex + 1}")
//            }
//        }
//    } else {
//        phoneNumbers.add("Permission not granted")
//    }
//
//    return phoneNumbers
//}
