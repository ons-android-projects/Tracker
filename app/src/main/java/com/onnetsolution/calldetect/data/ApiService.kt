package com.onnetsolution.calldetect.data

import com.onnetsolution.calldetect.data.response.CallLogResponse
import com.onnetsolution.calldetect.data.response.MobAndDateTimeResponse
import com.onnetsolution.calldetect.util.Constants.CALL_LOG
import com.onnetsolution.calldetect.util.Constants.TARGET_USER_MOB_DATE_TIME
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET(TARGET_USER_MOB_DATE_TIME)
    suspend fun uploadMobAndDateTime(
        @Query("target_mob_no") targetUserMob:String,
        @Query("signup_datetime") installationDateTime:String
    ):Response<MobAndDateTimeResponse>

    @POST(CALL_LOG)
    @FormUrlEncoded
    suspend fun uploadCallLog(
        @Field("var") callLogs:String
    ):Response<CallLogResponse>
}