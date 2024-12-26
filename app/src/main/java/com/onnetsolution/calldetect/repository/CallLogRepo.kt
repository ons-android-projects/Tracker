package com.onnetsolution.calldetect.repository

import com.onnetsolution.calldetect.data.local.CallLogDataClass
import com.onnetsolution.calldetect.data.local.TargetUserDatabase

class CallLogRepo(private val db: TargetUserDatabase) {
    suspend fun insertCallLog(data:List<CallLogDataClass> ){
        db.getTargetUserDao().insertCallLogDetails(data)
    }

    fun getAllCallLogs(): List<CallLogDataClass>?{
        return db.getTargetUserDao().getAllCallLogs()
    }

    fun deleteAllCallLog(){
        return db.getTargetUserDao().deleteAllCallLog()
    }
}