package com.onnetsolution.calldetect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "targetUser")
data class MobAndDateTimeDataClass(
    @PrimaryKey(autoGenerate = false)
    val sl:Int=0,
    val target_mob:String,
    val current_date_time:String
)