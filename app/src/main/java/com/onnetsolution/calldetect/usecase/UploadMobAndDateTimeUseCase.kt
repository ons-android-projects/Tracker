package com.onnetsolution.calldetect.usecase

import com.onnetsolution.calldetect.data.response.MobAndDateTimeResponse
import com.onnetsolution.calldetect.repository.UploadMobAndDateTimeRepo
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class UploadMobAndDateTimeUseCase(private val uploadMobAndDateTimeRepo: UploadMobAndDateTimeRepo) {
    suspend operator fun invoke(targetUserMob:String,installationDateTime:String):Flow<Response<MobAndDateTimeResponse>>{
        return uploadMobAndDateTimeRepo.uploadMobAndDateTime(targetUserMob, installationDateTime)
    }
}