package com.onnetsolution.calldetect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TargetUserDao {
@Insert(onConflict = OnConflictStrategy.IGNORE)
suspend fun insertUserMobAndDateTime(user:MobAndDateTimeDataClass)

@Query("select * from targetUser where sl=0")
fun getMobAndDateTime():MobAndDateTimeDataClass?

@Query("Update targetUser set current_date_time=:updated_date_time")
suspend fun updateDateTime(updated_date_time:String)

@Query("Delete from targetUser")
fun deleteTargetUser()

@Insert(onConflict = OnConflictStrategy.IGNORE)
suspend fun insertCallLogDetails(data:List<CallLogDataClass>)

@Query("select * from call_log")
fun getAllCallLogs():List<CallLogDataClass>?

@Query("Delete from call_log")
fun deleteAllCallLog()
}