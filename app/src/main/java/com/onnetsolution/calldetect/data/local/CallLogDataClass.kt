package com.onnetsolution.calldetect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("call_log")
data class CallLogDataClass(
    @PrimaryKey(autoGenerate = true)
    val sl:Int=0,
    val stat:Int=0,
    val number: String,
    val callType: String,
    val date: Long,
    val duration: Long,
    val time:String,
    val phnDateTime:String
)