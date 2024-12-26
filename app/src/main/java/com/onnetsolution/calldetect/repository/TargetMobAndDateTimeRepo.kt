package com.onnetsolution.calldetect.repository

import com.onnetsolution.calldetect.data.local.MobAndDateTimeDataClass
import com.onnetsolution.calldetect.data.local.TargetUserDatabase

class TargetMobAndDateTimeRepo(private val db: TargetUserDatabase) {
    suspend fun insertUserMobAndDateTime(data:MobAndDateTimeDataClass){
        db.getTargetUserDao().insertUserMobAndDateTime(data)
    }

    fun getMobAndDateTime():MobAndDateTimeDataClass?{
        return db.getTargetUserDao().getMobAndDateTime()
    }

    suspend fun updateDateTime(updatedDateTime:String){
        db.getTargetUserDao().updateDateTime(updatedDateTime)
    }

    fun deleteTargetUser(){
        return db.getTargetUserDao().deleteTargetUser()
    }

}