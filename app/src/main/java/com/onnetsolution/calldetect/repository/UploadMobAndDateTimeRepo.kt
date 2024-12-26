package com.onnetsolution.calldetect.repository

import com.onnetsolution.calldetect.data.ApiService
import com.onnetsolution.calldetect.data.di.NetworkModule
import com.onnetsolution.calldetect.data.response.MobAndDateTimeResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class UploadMobAndDateTimeRepo(private val provideApiService:ApiService) {

    private val provideapiService:ApiService by lazy {
        NetworkModule.getInstance().create(ApiService::class.java)
    }

    suspend fun uploadMobAndDateTime(targetUserMob:String,installationDateTime:String): Flow<retrofit2.Response<MobAndDateTimeResponse>> =
        flow {
            try {
                val response= provideApiService.uploadMobAndDateTime(targetUserMob,installationDateTime)
                emit(response)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
}