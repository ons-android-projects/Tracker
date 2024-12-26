package com.onnetsolution.calldetect.repository

import com.onnetsolution.calldetect.data.ApiService
import com.onnetsolution.calldetect.data.response.CallLogResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

class UploadCallLogRepo(private val provideApiService: ApiService) {
    suspend fun uploadCallLog(callLogString: String):Flow<Response<CallLogResponse>> =
        flow {
            try {
                val response=provideApiService.uploadCallLog(callLogString)
                emit(response)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
}