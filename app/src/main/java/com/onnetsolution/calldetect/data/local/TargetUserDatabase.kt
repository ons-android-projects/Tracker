package com.onnetsolution.calldetect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MobAndDateTimeDataClass::class,CallLogDataClass::class], version = 1)
abstract class TargetUserDatabase:RoomDatabase() {
    abstract fun getTargetUserDao():TargetUserDao
}

object TargetUserInfo{
    fun getDatabase(context: Context)= Room.databaseBuilder(context,TargetUserDatabase::class.java,"target_user_db").allowMainThreadQueries().fallbackToDestructiveMigration().build()
}