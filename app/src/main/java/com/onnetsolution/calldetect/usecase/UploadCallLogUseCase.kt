package com.onnetsolution.calldetect.usecase

import com.onnetsolution.calldetect.data.response.CallLogResponse
import com.onnetsolution.calldetect.repository.UploadCallLogRepo
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

class UploadCallLogUseCase(private val uploadCallLogRepo: UploadCallLogRepo) {
    suspend operator fun invoke(callLogsString: String):Flow<Response<CallLogResponse>>{
        return uploadCallLogRepo.uploadCallLog(callLogsString)
    }
}